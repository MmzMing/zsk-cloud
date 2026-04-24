package com.zsk.document.service.impl;

import com.zsk.common.core.constant.CacheConstants;
import com.zsk.common.redis.service.RedisService;
import com.zsk.document.domain.DocUserInteraction;
import com.zsk.document.domain.context.DocUserInteractionContext;
import com.zsk.document.enums.CacheDocFollowTypeEnum;
import com.zsk.document.mapper.DocUserInteractionMapper;
import com.zsk.document.service.ICacheDocFollowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 缓存文档关注服务实现类
 * <p>
 * 使用三个Redis键实现关注功能：
 * 1. follow:user:{user_id} Hash 存储用户关注的所有目标 {targetId:type}
 * 2. follow:count:{target_id}:{type} String 存储目标的实时粉丝数
 * 3. follow:lock:{user_id}:{target_id} String 分布式锁（防止重复关注，过期时间1分钟）
 * <p>
 * 关注数据先写入Redis，后由定时任务 {@link com.zsk.document.job.CacheDocSocialSyncJob} 同步到数据库。
 *
 * @author wuhuaming
 * @version 2.0
 * @date 2026-04-25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheDocFollowServiceImpl implements ICacheDocFollowService {

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
     * 关注
     * <p>
     * 用户关注目标用户。
     * 操作会先检查分布式锁防止重复操作，然后更新Redis中的用户关注记录和粉丝计数。
     * 用户不能关注自己。
     * </p>
     *
     * @param type     关注类型（1-用户）
     * @param targetId 目标用户ID（被关注者ID）
     * @param userId   用户ID（关注者ID）
     * @return 是否关注成功（重复关注或关注自己返回false）
     */
    @Override
    public boolean follow(Integer type, Long targetId, Long userId) {
        // 1. 验证参数并获取关注类型枚举
        CacheDocFollowTypeEnum followType = CacheDocFollowTypeEnum.getByCode(type);
        if (followType == null || targetId == null || userId == null) {
            log.warn("关注参数无效: type={}, targetId={}, userId={}", type, targetId, userId);
            return false;
        }

        // 2. 检查是否关注自己
        if (targetId.equals(userId)) {
            log.warn("用户不能关注自己: userId={}", userId);
            return false;
        }

        // 3. 构建Redis键
        String lockKey = buildLockKey(userId, targetId);
        String userKey = buildUserKey(userId);
        String countKey = buildCountKey(targetId, followType);
        String hashField = targetId + ":" + type;

        // 4. 检查分布式锁，防止重复操作
        Boolean locked = redisService.getCacheObject(lockKey);
        if (Boolean.TRUE.equals(locked)) {
            log.debug("用户 {} 对目标 {} 操作频繁", userId, targetId);
            return false;
        }

        // 5. 检查是否已关注
        Object existingType = redisTemplate.opsForHash().get(userKey, hashField);
        if (existingType != null) {
            log.debug("用户 {} 已关注目标 {}", userId, targetId);
            return false;
        }

        // 6. 执行关注操作：设置锁、记录用户关注、增加粉丝计数
        redisService.setCacheObject(lockKey, true, LOCK_EXPIRE_SECONDS, TimeUnit.SECONDS);
        redisTemplate.opsForHash().put(userKey, hashField, type.toString());
        redisTemplate.opsForValue().increment(countKey, 1);

        log.debug("用户 {} 关注 {} 类型目标 {}", userId, followType.getDesc(), targetId);
        return true;
    }

    /**
     * 取消关注
     * <p>
     * 用户取消关注目标用户。
     * 操作会先检查分布式锁防止重复操作，然后更新Redis中的用户关注记录和粉丝计数。
     * </p>
     *
     * @param type     关注类型
     * @param targetId 目标用户ID
     * @param userId   用户ID
     * @return 是否取消成功（未关注返回false）
     */
    @Override
    public boolean unfollow(Integer type, Long targetId, Long userId) {
        // 1. 验证参数并获取关注类型枚举
        CacheDocFollowTypeEnum followType = CacheDocFollowTypeEnum.getByCode(type);
        if (followType == null || targetId == null || userId == null) {
            log.warn("取消关注参数无效: type={}, targetId={}, userId={}", type, targetId, userId);
            return false;
        }

        // 2. 构建Redis键
        String lockKey = buildLockKey(userId, targetId);
        String userKey = buildUserKey(userId);
        String countKey = buildCountKey(targetId, followType);
        String hashField = targetId + ":" + type;

        // 3. 检查分布式锁，防止重复操作
        Boolean locked = redisService.getCacheObject(lockKey);
        if (Boolean.TRUE.equals(locked)) {
            log.debug("用户 {} 对目标 {} 操作频繁", userId, targetId);
            return false;
        }

        // 4. 检查是否已关注
        Object existingType = redisTemplate.opsForHash().get(userKey, hashField);
        if (existingType == null) {
            log.debug("用户 {} 未关注目标 {}", userId, targetId);
            return false;
        }

        // 5. 执行取消关注操作：设置锁、删除用户关注记录、减少粉丝计数
        redisService.setCacheObject(lockKey, true, LOCK_EXPIRE_SECONDS, TimeUnit.SECONDS);
        redisTemplate.opsForHash().delete(userKey, hashField);
        redisTemplate.opsForValue().decrement(countKey, 1);

        log.debug("用户 {} 取消关注 {} 类型目标 {}", userId, followType.getDesc(), targetId);
        return true;
    }

    /**
     * 获取用户关注数
     * <p>
     * 获取指定用户总共关注了多少人。
     * 先查询Redis缓存，如未命中则从数据库统计。
     * </p>
     *
     * @param userId 用户ID
     * @return 用户关注数量
     */
    @Override
    public Long getUserFollowCount(Long userId) {
        if (userId == null) {
            return 0L;
        }

        // 1. 构建用户关注记录Redis键
        String userKey = buildUserKey(userId);

        // 2. 从Redis获取用户关注记录数量
        Long count = redisTemplate.opsForHash().size(userKey);
        if (count != null && count > 0) {
            return count;
        }

        // 3. 缓存未命中，从数据库统计
        Long dbCount = docUserInteractionMapper.countByUser(userId, DocUserInteractionContext.INTERACTION_TYPE_FOLLOW);
        return dbCount != null ? dbCount : 0L;
    }

    /**
     * 获取来源粉丝数
     * <p>
     * 获取指定目标用户的粉丝数量。
     * 先查询Redis缓存，如未命中则从数据库加载并写入缓存。
     * </p>
     *
     * @param type     关注类型
     * @param targetId 目标用户ID（被关注者ID）
     * @return 粉丝数量
     */
    @Override
    public Long getFollowCount(Integer type, Long targetId) {
        // 1. 验证参数并获取关注类型枚举
        CacheDocFollowTypeEnum followType = CacheDocFollowTypeEnum.getByCode(type);
        if (followType == null || targetId == null) {
            return 0L;
        }

        // 2. 构建粉丝计数Redis键
        String countKey = buildCountKey(targetId, followType);

        // 3. 尝试从Redis获取粉丝数
        Object count = redisTemplate.opsForValue().get(countKey);
        if (count != null) {
            return Long.parseLong(count.toString());
        }

        // 4. 缓存未命中，从数据库加载
        Long dbCount = docUserInteractionMapper.countByTarget(
            DocUserInteractionContext.TARGET_TYPE_USER, targetId, DocUserInteractionContext.INTERACTION_TYPE_FOLLOW);
        if (dbCount != null && dbCount > 0) {
            // 写入缓存，后续请求可直接从缓存读取
            redisService.setCacheObject(countKey, dbCount);
        }
        return dbCount != null ? dbCount : 0L;
    }

    /**
     * 查询用户是否关注
     * <p>
     * 判断指定用户是否已关注目标用户。
     * 先检查Redis缓存中的用户关注记录，如未命中则查询数据库。
     * </p>
     *
     * @param type     关注类型
     * @param targetId 目标用户ID
     * @param userId   用户ID
     * @return 是否已关注
     */
    @Override
    public boolean hasFollowed(Integer type, Long targetId, Long userId) {
        // 1. 验证参数并获取关注类型枚举
        CacheDocFollowTypeEnum followType = CacheDocFollowTypeEnum.getByCode(type);
        if (followType == null || targetId == null || userId == null) {
            return false;
        }

        // 2. 构建用户关注记录Redis键
        String userKey = buildUserKey(userId);
        String hashField = targetId + ":" + type;

        // 3. 尝试从Redis获取关注状态
        Object existingType = redisTemplate.opsForHash().get(userKey, hashField);
        if (existingType != null) {
            return true;
        }

        // 4. 缓存未命中，从数据库查询
        DocUserInteraction interaction = docUserInteractionMapper.selectByUserAndTarget(
            userId, DocUserInteractionContext.TARGET_TYPE_USER, targetId, DocUserInteractionContext.INTERACTION_TYPE_FOLLOW);
        return interaction != null && interaction.getStatus() == 1;
    }

    /**
     * 根据多个来源，批量获取粉丝数
     * <p>
     * 批量查询多个目标用户的粉丝数量。
     * </p>
     *
     * @param type      关注类型
     * @param targetIds 目标用户ID列表
     * @return 目标ID与粉丝数量的映射
     */
    @Override
    public Map<Long, Long> getFollowCountBatch(Integer type, Iterable<Long> targetIds) {
        Map<Long, Long> result = new HashMap<>();
        // 1. 验证参数
        CacheDocFollowTypeEnum followType = CacheDocFollowTypeEnum.getByCode(type);
        if (followType == null || targetIds == null) {
            return result;
        }

        // 2. 逐个查询粉丝数
        for (Long targetId : targetIds) {
            result.put(targetId, getFollowCount(type, targetId));
        }
        return result;
    }

    /**
     * 同步关注数据到数据库
     * <p>
     * 由定时任务调用，将Redis中的用户关注记录同步到数据库持久化。
     * 同步完成后，删除已同步的Redis键，但保留计数键继续累加新的关注。
     * </p>
     */
    @Override
    public void syncFollowDataToDb() {
        log.info("开始同步关注数据到数据库...");
        int syncCount = 0;

        // 1. 扫描所有用户关注记录Redis键
        String pattern = CacheConstants.CACHE_FOLLOW_USER + "*";
        Collection<String> keys = redisService.keys(pattern);

        if (keys == null || keys.isEmpty()) {
            log.info("没有需要同步的关注数据");
            return;
        }

        // 2. 遍历所有用户关注键，同步到数据库
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

            // 获取用户的所有关注记录
            Map<Object, Object> followMap = redisTemplate.opsForHash().entries(userKey);
            for (Map.Entry<Object, Object> entry : followMap.entrySet()) {
                String field = entry.getKey().toString();
                String[] parts = field.split(":");
                if (parts.length >= 2) {
                    Long targetId = Long.parseLong(parts[0]);
                    Integer type = Integer.parseInt(parts[1]);
                    CacheDocFollowTypeEnum followType = CacheDocFollowTypeEnum.getByCode(type);
                    if (followType != null) {
                        // 保存交互记录到数据库
                        saveInteractionToDb(userId, DocUserInteractionContext.TARGET_TYPE_USER, targetId,
                            DocUserInteractionContext.INTERACTION_TYPE_FOLLOW);
                        syncCount++;
                    }
                }
            }
            // 删除已同步的Redis键
            redisService.deleteObject(userKey);
        }

        log.info("关注数据同步完成，共同步 {} 条记录", syncCount);
    }

    /**
     * 构建用户关注记录键
     * <p>
     * Redis键格式: zsk:follow:user:{userId}
     * </p>
     *
     * @param userId 用户ID
     * @return Redis键
     */
    private String buildUserKey(Long userId) {
        return CacheConstants.CACHE_FOLLOW_USER + userId;
    }

    /**
     * 构建粉丝计数键
     * <p>
     * Redis键格式: zsk:follow:count:{targetId}:{typeCode}
     * </p>
     *
     * @param targetId 目标ID
     * @param type     关注类型枚举
     * @return Redis键
     */
    private String buildCountKey(Long targetId, CacheDocFollowTypeEnum type) {
        return CacheConstants.CACHE_FOLLOW_COUNT + targetId + ":" + type.getCode();
    }

    /**
     * 构建分布式锁键
     * <p>
     * Redis键格式: zsk:follow:user:lock:{userId}:{targetId}
     * </p>
     *
     * @param userId   用户ID
     * @param targetId 目标ID
     * @return Redis键
     */
    private String buildLockKey(Long userId, Long targetId) {
        return CacheConstants.CACHE_FOLLOW_USER + "lock:" + userId + ":" + targetId;
    }

    /**
     * 从Redis键中提取用户ID
     * <p>
     * 从形如 "zsk:follow:user:123" 的键中提取用户ID "123"。
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
     * 保存交互记录到数据库
     * <p>
     * 将用户关注记录持久化到 doc_user_interaction 表。
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
            // 更新状态为已关注
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
