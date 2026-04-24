package com.zsk.document.service.impl;

import com.zsk.common.core.constant.CacheConstants;
import com.zsk.common.redis.service.RedisService;
import com.zsk.document.domain.DocUserInteraction;
import com.zsk.document.domain.context.DocUserInteractionContext;
import com.zsk.document.enums.CacheDocLikeTypeEnum;
import com.zsk.document.mapper.DocUserInteractionMapper;
import com.zsk.document.service.ICacheDocLikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 缓存文档点赞服务实现类
 * <p>
 * 使用三个Redis键实现点赞功能：
 * 1. like:user:{user_id} Hash 存储用户点赞的所有内容 {targetId:type}
 * 2. like:count:{target_id}:{type} String 存储内容的实时点赞数
 * 3. like:lock:{user_id}:{target_id} String 分布式锁（防止重复点赞，过期时间1分钟）
 * <p>
 * 点赞数据先写入Redis，后由定时任务 {@link com.zsk.document.job.CacheDocSocialSyncJob} 同步到数据库。
 *
 * @author wuhuaming
 * @version 2.0
 * @date 2026-04-25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheDocLikeServiceImpl implements ICacheDocLikeService {

    /**
     * Redis服务
     */
    private final RedisService redisService;

    /**
     * Redis模板
     */
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 用户交互Mapper
     */
    private final DocUserInteractionMapper docUserInteractionMapper;

    /**
     * 分布式锁过期时间（秒），1分钟内防止重复操作
     */
    private static final long LOCK_EXPIRE_SECONDS = 60;

    /**
     * 点赞
     * <p>
     * 用户对目标内容进行点赞操作。
     * 操作会先检查分布式锁防止重复操作，然后更新Redis中的用户点赞记录和点赞计数。
     * </p>
     *
     * @param type     点赞类型（1-笔记 2-笔记评论 3-视频 4-视频评论）
     * @param targetId 目标ID
     * @param userId   用户ID
     * @return 是否点赞成功（重复点赞返回false）
     */
    @Override
    public boolean like(Integer type, Long targetId, Long userId) {
        // 1. 验证参数并获取点赞类型枚举
        CacheDocLikeTypeEnum likeType = CacheDocLikeTypeEnum.getByCode(type);
        if (likeType == null || targetId == null || userId == null) {
            log.warn("点赞参数无效: type={}, targetId={}, userId={}", type, targetId, userId);
            return false;
        }

        // 2. 构建Redis键
        String lockKey = buildLockKey(userId, targetId);
        String userKey = buildUserKey(userId);
        String countKey = buildCountKey(targetId, likeType);
        String hashField = targetId + ":" + type;

        // 3. 检查分布式锁，防止重复操作
        Boolean locked = redisService.getCacheObject(lockKey);
        if (Boolean.TRUE.equals(locked)) {
            log.debug("用户 {} 对目标 {} 操作频繁", userId, targetId);
            return false;
        }

        // 4. 检查是否已点赞
        Object existingType = redisTemplate.opsForHash().get(userKey, hashField);
        if (existingType != null) {
            log.debug("用户 {} 已点赞目标 {}", userId, targetId);
            return false;
        }

        // 5. 执行点赞操作：设置锁、记录用户点赞、增加计数
        redisService.setCacheObject(lockKey, true, LOCK_EXPIRE_SECONDS, TimeUnit.SECONDS);
        redisTemplate.opsForHash().put(userKey, hashField, type.toString());
        redisTemplate.opsForValue().increment(countKey, 1);

        log.debug("用户 {} 点赞 {} 类型目标 {}", userId, likeType.getDesc(), targetId);
        return true;
    }

    /**
     * 取消点赞
     * <p>
     * 用户对目标内容取消点赞操作。
     * 操作会先检查分布式锁防止重复操作，然后更新Redis中的用户点赞记录和点赞计数。
     * </p>
     *
     * @param type     点赞类型
     * @param targetId 目标ID
     * @param userId   用户ID
     * @return 是否取消成功（未点赞返回false）
     */
    @Override
    public boolean unlike(Integer type, Long targetId, Long userId) {
        // 1. 验证参数并获取点赞类型枚举
        CacheDocLikeTypeEnum likeType = CacheDocLikeTypeEnum.getByCode(type);
        if (likeType == null || targetId == null || userId == null) {
            log.warn("取消点赞参数无效: type={}, targetId={}, userId={}", type, targetId, userId);
            return false;
        }

        // 2. 构建Redis键
        String lockKey = buildLockKey(userId, targetId);
        String userKey = buildUserKey(userId);
        String countKey = buildCountKey(targetId, likeType);
        String hashField = targetId + ":" + type;

        // 3. 检查分布式锁，防止重复操作
        Boolean locked = redisService.getCacheObject(lockKey);
        if (Boolean.TRUE.equals(locked)) {
            log.debug("用户 {} 对目标 {} 操作频繁", userId, targetId);
            return false;
        }

        // 4. 检查是否已点赞
        Object existingType = redisTemplate.opsForHash().get(userKey, hashField);
        if (existingType == null) {
            log.debug("用户 {} 未点赞目标 {}", userId, targetId);
            return false;
        }

        // 5. 执行取消点赞操作：设置锁、删除用户点赞记录、减少计数
        redisService.setCacheObject(lockKey, true, LOCK_EXPIRE_SECONDS, TimeUnit.SECONDS);
        redisTemplate.opsForHash().delete(userKey, hashField);
        redisTemplate.opsForValue().decrement(countKey, 1);

        log.debug("用户 {} 取消点赞 {} 类型目标 {}", userId, likeType.getDesc(), targetId);
        return true;
    }

    /**
     * 获取用户点赞数
     * <p>
     * 获取指定用户总共点赞的数量。
     * 先查询Redis缓存，如未命中则从数据库统计。
     * </p>
     *
     * @param userId 用户ID
     * @return 用户点赞数量
     */
    @Override
    public Long getUserLikeCount(Long userId) {
        if (userId == null) {
            return 0L;
        }

        // 1. 构建用户点赞记录Redis键
        String userKey = buildUserKey(userId);

        // 2. 从Redis获取用户点赞记录数量
        Long count = redisTemplate.opsForHash().size(userKey);
        if (count != null && count > 0) {
            return count;
        }

        // 3. 缓存未命中，从数据库统计
        Long dbCount = docUserInteractionMapper.countByUser(userId, DocUserInteractionContext.INTERACTION_TYPE_LIKE);
        return dbCount != null ? dbCount : 0L;
    }

    /**
     * 获取来源点赞数
     * <p>
     * 获取指定目标内容的总点赞数量。
     * 先查询Redis缓存，如未命中则从数据库加载并写入缓存。
     * </p>
     *
     * @param type     点赞类型
     * @param targetId 目标ID
     * @return 来源点赞数量
     */
    @Override
    public Long getLikeCount(Integer type, Long targetId) {
        // 1. 验证参数并获取点赞类型枚举
        CacheDocLikeTypeEnum likeType = CacheDocLikeTypeEnum.getByCode(type);
        if (likeType == null || targetId == null) {
            return 0L;
        }

        // 2. 构建点赞计数Redis键
        String countKey = buildCountKey(targetId, likeType);

        // 3. 尝试从Redis获取点赞数
        Object count = redisTemplate.opsForValue().get(countKey);
        if (count != null) {
            return Long.parseLong(count.toString());
        }

        // 4. 缓存未命中，从数据库加载
        Long dbCount = getLikeCountFromDb(likeType, targetId);
        if (dbCount != null && dbCount > 0) {
            // 写入缓存，后续请求可直接从缓存读取
            redisService.setCacheObject(countKey, dbCount);
        }
        return dbCount != null ? dbCount : 0L;
    }

    /**
     * 查询用户是否点赞
     * <p>
     * 判断指定用户是否对目标内容已点赞。
     * 先检查Redis缓存中的用户点赞记录，如未命中则查询数据库。
     * </p>
     *
     * @param type     点赞类型
     * @param targetId 目标ID
     * @param userId   用户ID
     * @return 是否已点赞
     */
    @Override
    public boolean hasLiked(Integer type, Long targetId, Long userId) {
        // 1. 验证参数并获取点赞类型枚举
        CacheDocLikeTypeEnum likeType = CacheDocLikeTypeEnum.getByCode(type);
        if (likeType == null || targetId == null || userId == null) {
            return false;
        }

        // 2. 构建用户点赞记录Redis键
        String userKey = buildUserKey(userId);
        String hashField = targetId + ":" + type;

        // 3. 尝试从Redis获取点赞状态
        Object existingType = redisTemplate.opsForHash().get(userKey, hashField);
        if (existingType != null) {
            return true;
        }

        // 4. 缓存未命中，从数据库查询
        DocUserInteraction interaction = docUserInteractionMapper.selectByUserAndTarget(
            userId, getTargetType(likeType), targetId, DocUserInteractionContext.INTERACTION_TYPE_LIKE);
        return interaction != null && interaction.getStatus() == 1;
    }

    /**
     * 根据多个来源，批量获取点赞数
     * <p>
     * 批量查询多个目标内容的点赞数量。
     * </p>
     *
     * @param type      点赞类型
     * @param targetIds 目标ID列表
     * @return 目标ID与点赞数量的映射
     */
    @Override
    public Map<Long, Long> getLikeCountBatch(Integer type, Iterable<Long> targetIds) {
        Map<Long, Long> result = new HashMap<>();
        // 1. 验证参数
        CacheDocLikeTypeEnum likeType = CacheDocLikeTypeEnum.getByCode(type);
        if (likeType == null || targetIds == null) {
            return result;
        }

        // 2. 逐个查询点赞数
        for (Long targetId : targetIds) {
            result.put(targetId, getLikeCount(type, targetId));
        }
        return result;
    }

    /**
     * 同步点赞数据到数据库
     * <p>
     * 由定时任务调用，将Redis中的用户点赞记录同步到数据库持久化。
     * 同步完成后，删除已同步的Redis键，但保留计数键继续累加新的点赞。
     * </p>
     */
    @Override
    public void syncLikeDataToDb() {
        log.info("开始同步点赞数据到数据库...");
        int syncCount = 0;

        // 1. 扫描所有用户点赞记录Redis键
        String pattern = CacheConstants.CACHE_LIKE_USER + "*";
        Collection<String> keys = redisService.keys(pattern);

        if (keys == null || keys.isEmpty()) {
            log.info("没有需要同步的点赞数据");
            return;
        }

        // 2. 遍历所有用户点赞键，同步到数据库
        for (String userKey : keys) {
            // 跳过锁键
            if (userKey.contains("lock:")) {
                continue;
            }

            // 从Redis键中提取用户ID
            Long userId = extractUserIdFromKey(userKey);
            if (userId == null) {
                continue;
            }

            // 获取用户的所有点赞记录
            Map<Object, Object> likeMap = redisTemplate.opsForHash().entries(userKey);
            for (Map.Entry<Object, Object> entry : likeMap.entrySet()) {
                String field = entry.getKey().toString();
                String[] parts = field.split(":");
                if (parts.length >= 2) {
                    Long targetId = Long.parseLong(parts[0]);
                    Integer type = Integer.parseInt(parts[1]);
                    CacheDocLikeTypeEnum likeType = CacheDocLikeTypeEnum.getByCode(type);
                    if (likeType != null) {
                        // 保存交互记录到数据库
                        saveInteractionToDb(userId, getTargetType(likeType), targetId,
                            DocUserInteractionContext.INTERACTION_TYPE_LIKE);
                        syncCount++;
                    }
                }
            }
            // 删除已同步的Redis键
            redisService.deleteObject(userKey);
        }

        log.info("点赞数据同步完成，共同步 {} 条记录", syncCount);
    }

    /**
     * 构建用户点赞记录键
     * <p>
     * Redis键格式: zsk:like:user:{userId}
     * </p>
     *
     * @param userId 用户ID
     * @return Redis键
     */
    private String buildUserKey(Long userId) {
        return CacheConstants.CACHE_LIKE_USER + userId;
    }

    /**
     * 构建点赞计数键
     * <p>
     * Redis键格式: zsk:like:count:{targetId}:{typeCode}
     * </p>
     *
     * @param targetId 目标ID
     * @param type     点赞类型枚举
     * @return Redis键
     */
    private String buildCountKey(Long targetId, CacheDocLikeTypeEnum type) {
        return CacheConstants.CACHE_LIKE_COUNT + targetId + ":" + type.getCode();
    }

    /**
     * 构建分布式锁键
     * <p>
     * Redis键格式: zsk:like:user:lock:{userId}:{targetId}
     * </p>
     *
     * @param userId   用户ID
     * @param targetId 目标ID
     * @return Redis键
     */
    private String buildLockKey(Long userId, Long targetId) {
        return CacheConstants.CACHE_LIKE_USER + "lock:" + userId + ":" + targetId;
    }

    /**
     * 从Redis键中提取用户ID
     * <p>
     * 从形如 "zsk:like:user:123" 的键中提取用户ID "123"。
     * </p>
     *
     * @param key Redis键
     * @return 用户ID
     */
    private Long extractUserIdFromKey(String key) {
        try {
            String[] parts = key.split(":");
            if (parts.length >= 4) {
                return Long.parseLong(parts[3]);
            }
        } catch (Exception e) {
            log.warn("解析用户ID失败: {}", key, e);
        }
        return null;
    }

    /**
     * 从数据库获取点赞数量
     * <p>
     * 通过统计 doc_user_interaction 表中交互类型为点赞的记录数。
     * </p>
     *
     * @param type     点赞类型枚举
     * @param targetId 目标ID
     * @return 点赞数量
     */
    private Long getLikeCountFromDb(CacheDocLikeTypeEnum type, Long targetId) {
        return docUserInteractionMapper.countByTarget(
            getTargetType(type), targetId, DocUserInteractionContext.INTERACTION_TYPE_LIKE);
    }

    /**
     * 获取目标类型
     * <p>
     * 将点赞类型枚举转换为交互表中的目标类型编码。
     * </p>
     *
     * @param likeType 点赞类型枚举
     * @return 目标类型编码（1-笔记 2-评论 3-视频）
     */
    private Integer getTargetType(CacheDocLikeTypeEnum likeType) {
        switch (likeType) {
            case NOTE:
                return DocUserInteractionContext.TARGET_TYPE_NOTE;
            case NOTE_COMMENT:
            case VIDEO_COMMENT:
                return DocUserInteractionContext.TARGET_TYPE_COMMENT;
            case VIDEO:
                return DocUserInteractionContext.TARGET_TYPE_VIDEO;
            default:
                return null;
        }
    }

    /**
     * 保存交互记录到数据库
     * <p>
     * 将用户点赞记录持久化到 doc_user_interaction 表。
     * 如果记录已存在则更新状态，否则创建新记录。
     * </p>
     *
     * @param userId          用户ID
     * @param targetType      目标类型
     * @param targetId        目标ID
     * @param interactionType 交互类型
     */
    private void saveInteractionToDb(Long userId, Integer targetType, Long targetId, Integer interactionType) {
        // 检查是否已存在交互记录
        DocUserInteraction existing = docUserInteractionMapper.selectByUserAndTarget(
            userId, targetType, targetId, interactionType);
        if (existing != null) {
            // 更新状态为已点赞
            existing.setStatus(1);
            docUserInteractionMapper.updateById(existing);
        } else {
            // 创建新的交互记录
            DocUserInteraction interaction = new DocUserInteraction();
            interaction.setUserId(userId);
            interaction.setTargetType(targetType);
            interaction.setTargetId(targetId);
            interaction.setInteractionType(interactionType);
            interaction.setStatus(1);
            docUserInteractionMapper.insert(interaction);
        }
    }
}
