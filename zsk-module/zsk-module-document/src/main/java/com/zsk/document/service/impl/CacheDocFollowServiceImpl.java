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

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 缓存文档关注服务实现类
 * <p>
 * 使用三个Redis键实现关注功能：
 * 1. follow:user:{user_id} Hash 存储用户关注的所有目标 {targetId:type}
 * 2. follow:count:{target_id}:{type} String 存储目标的实时粉丝数
 * 3. follow:lock:{user_id}:{target_id} String 分布式锁（防止重复关注，过期时间1分钟）
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheDocFollowServiceImpl implements ICacheDocFollowService {

    private final RedisService redisService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final DocUserInteractionMapper docUserInteractionMapper;

    /**
     * 分布式锁过期时间（秒）
     */
    private static final long LOCK_EXPIRE_SECONDS = 60;

    /**
     * 关注
     *
     * @param type     关注类型
     * @param targetId 目标ID（被关注者ID）
     * @param userId   用户ID（关注者ID）
     * @return 是否关注成功
     */
    @Override
    public boolean follow(Integer type, Long targetId, Long userId) {
        CacheDocFollowTypeEnum followType = CacheDocFollowTypeEnum.getByCode(type);
        if (followType == null || targetId == null || userId == null) {
            return false;
        }

        if (targetId.equals(userId)) {
            log.warn("用户不能关注自己: userId={}", userId);
            return false;
        }

        String lockKey = buildLockKey(userId, targetId);
        String userKey = buildUserKey(userId);
        String countKey = buildCountKey(targetId, followType);
        String hashField = targetId + ":" + type;

        Boolean locked = redisService.getCacheObject(lockKey);
        if (Boolean.TRUE.equals(locked)) {
            log.debug("用户 {} 对目标 {} 操作频繁，请稍后再试", userId, targetId);
            return false;
        }

        Object existingType = redisTemplate.opsForHash().get(userKey, hashField);
        if (existingType != null) {
            log.debug("用户 {} 已关注目标 {}", userId, targetId);
            return false;
        }

        redisService.setCacheObject(lockKey, true, LOCK_EXPIRE_SECONDS, TimeUnit.SECONDS);
        redisTemplate.opsForHash().put(userKey, hashField, type.toString());
        redisTemplate.opsForValue().increment(countKey, 1);

        log.debug("用户 {} 关注 {} 类型目标 {}", userId, followType.getDesc(), targetId);
        return true;
    }

    /**
     * 取消关注
     *
     * @param type     关注类型
     * @param targetId 目标ID
     * @param userId   用户ID
     * @return 是否取消成功
     */
    @Override
    public boolean unfollow(Integer type, Long targetId, Long userId) {
        CacheDocFollowTypeEnum followType = CacheDocFollowTypeEnum.getByCode(type);
        if (followType == null || targetId == null || userId == null) {
            return false;
        }

        String lockKey = buildLockKey(userId, targetId);
        String userKey = buildUserKey(userId);
        String countKey = buildCountKey(targetId, followType);
        String hashField = targetId + ":" + type;

        Boolean locked = redisService.getCacheObject(lockKey);
        if (Boolean.TRUE.equals(locked)) {
            log.debug("用户 {} 对目标 {} 操作频繁，请稍后再试", userId, targetId);
            return false;
        }

        Object existingType = redisTemplate.opsForHash().get(userKey, hashField);
        if (existingType == null) {
            log.debug("用户 {} 未关注目标 {}", userId, targetId);
            return false;
        }

        redisService.setCacheObject(lockKey, true, LOCK_EXPIRE_SECONDS, TimeUnit.SECONDS);
        redisTemplate.opsForHash().delete(userKey, hashField);
        redisTemplate.opsForValue().decrement(countKey, 1);

        log.debug("用户 {} 取消关注 {} 类型目标 {}", userId, followType.getDesc(), targetId);
        return true;
    }

    /**
     * 获取关注数量（粉丝数）
     *
     * @param type     关注类型
     * @param targetId 目标ID（被关注者ID）
     * @return 关注数量
     */
    @Override
    public Long getFollowCount(Integer type, Long targetId) {
        CacheDocFollowTypeEnum followType = CacheDocFollowTypeEnum.getByCode(type);
        if (followType == null || targetId == null) {
            return 0L;
        }

        String countKey = buildCountKey(targetId, followType);
        Object count = redisTemplate.opsForValue().get(countKey);
        if (count != null) {
            return Long.parseLong(count.toString());
        }

        Long dbCount = docUserInteractionMapper.countByTarget(
            DocUserInteractionContext.TARGET_TYPE_USER, targetId, DocUserInteractionContext.INTERACTION_TYPE_FOLLOW);
        if (dbCount != null && dbCount > 0) {
            redisService.setCacheObject(countKey, dbCount);
        }
        return dbCount != null ? dbCount : 0L;
    }

    /**
     * 判断用户是否已关注
     *
     * @param type     关注类型
     * @param targetId 目标ID
     * @param userId   用户ID
     * @return 是否已关注
     */
    @Override
    public boolean hasFollowed(Integer type, Long targetId, Long userId) {
        CacheDocFollowTypeEnum followType = CacheDocFollowTypeEnum.getByCode(type);
        if (followType == null || targetId == null || userId == null) {
            return false;
        }

        String userKey = buildUserKey(userId);
        String hashField = targetId + ":" + type;
        Object existingType = redisTemplate.opsForHash().get(userKey, hashField);
        if (existingType != null) {
            return true;
        }

        DocUserInteraction interaction = docUserInteractionMapper.selectByUserAndTarget(
            userId, DocUserInteractionContext.TARGET_TYPE_USER, targetId, DocUserInteractionContext.INTERACTION_TYPE_FOLLOW);
        return interaction != null && interaction.getStatus() == 1;
    }

    /**
     * 批量获取关注数量
     *
     * @param type      关注类型
     * @param targetIds 目标ID列表
     * @return 目标ID与关注数量的映射
     */
    @Override
    public Map<Long, Long> getFollowCountBatch(Integer type, Iterable<Long> targetIds) {
        Map<Long, Long> result = new HashMap<>();
        CacheDocFollowTypeEnum followType = CacheDocFollowTypeEnum.getByCode(type);
        if (followType == null || targetIds == null) {
            return result;
        }

        for (Long targetId : targetIds) {
            result.put(targetId, getFollowCount(type, targetId));
        }
        return result;
    }

    /**
     * 同步关注数据到数据库
     */
    @Override
    public void syncFollowDataToDb() {
        log.info("开始同步关注数据到数据库...");
        int syncCount = 0;

        String pattern = CacheConstants.CACHE_FOLLOW_USER + "*";
        Collection<String> keys = redisService.keys(pattern);

        if (keys == null || keys.isEmpty()) {
            log.info("没有需要同步的关注数据");
            return;
        }

        for (String userKey : keys) {
            if (userKey.contains("lock:")) {
                continue;
            }
            Long userId = extractUserIdFromKey(userKey);
            if (userId == null) {
                continue;
            }

            Map<Object, Object> followMap = redisTemplate.opsForHash().entries(userKey);
            for (Map.Entry<Object, Object> entry : followMap.entrySet()) {
                String field = entry.getKey().toString();
                String[] parts = field.split(":");
                if (parts.length >= 2) {
                    Long targetId = Long.parseLong(parts[0]);
                    Integer type = Integer.parseInt(parts[1]);
                    CacheDocFollowTypeEnum followType = CacheDocFollowTypeEnum.getByCode(type);
                    if (followType != null) {
                        saveInteractionToDb(userId, DocUserInteractionContext.TARGET_TYPE_USER, targetId,
                            DocUserInteractionContext.INTERACTION_TYPE_FOLLOW);
                        syncCount++;
                    }
                }
            }
            redisService.deleteObject(userKey);
        }

        log.info("关注数据同步完成，共同步 {} 条记录", syncCount);
    }

    /**
     * 构建用户关注记录键
     *
     * @param userId 用户ID
     * @return Redis键
     */
    private String buildUserKey(Long userId) {
        return CacheConstants.CACHE_FOLLOW_USER + userId;
    }

    /**
     * 构建关注计数键
     *
     * @param targetId 目标ID
     * @param type     关注类型
     * @return Redis键
     */
    private String buildCountKey(Long targetId, CacheDocFollowTypeEnum type) {
        return CacheConstants.CACHE_FOLLOW_COUNT + targetId + ":" + type.getType();
    }

    /**
     * 构建分布式锁键
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
     *
     * @param userId          用户ID
     * @param targetType      目标类型
     * @param targetId        目标ID
     * @param interactionType 交互类型
     */
    private void saveInteractionToDb(Long userId, Integer targetType, Long targetId, Integer interactionType) {
        DocUserInteraction existing = docUserInteractionMapper.selectByUserAndTarget(userId, targetType, targetId, interactionType);
        if (existing != null) {
            existing.setStatus(1);
            docUserInteractionMapper.updateById(existing);
        } else {
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
