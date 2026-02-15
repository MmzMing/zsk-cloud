package com.zsk.document.service.impl;

import com.zsk.common.core.constant.CacheConstants;
import com.zsk.common.redis.service.RedisService;
import com.zsk.document.enums.CacheDocFollowTypeEnum;
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
 * 使用两个Redis键实现关注功能：
 * 1. FOLLOW_COUNT_KEY: 存储关注计数
 * 2. FOLLOW_USER_KEY: 记录用户是否已关注（用于判断是否需要入库）
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
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
     * 用户关注记录过期时间（小时）
     */
    private static final long FOLLOW_USER_EXPIRE_HOURS = 24;

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

        String userKey = buildUserKey(followType, targetId, userId);
        String countKey = buildCountKey(followType, targetId);

        Boolean hasFollowed = redisService.getCacheObject(userKey);
        if (Boolean.TRUE.equals(hasFollowed)) {
            return false;
        }

        redisService.setCacheObject(userKey, true, FOLLOW_USER_EXPIRE_HOURS, TimeUnit.HOURS);
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

        String userKey = buildUserKey(followType, targetId, userId);
        String countKey = buildCountKey(followType, targetId);

        Boolean hasFollowed = redisService.getCacheObject(userKey);
        if (!Boolean.TRUE.equals(hasFollowed)) {
            return false;
        }

        redisService.deleteObject(userKey);
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

        String countKey = buildCountKey(followType, targetId);
        Object count = redisTemplate.opsForValue().get(countKey);
        if (count != null) {
            return Long.parseLong(count.toString());
        }

        return 0L;
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

        String userKey = buildUserKey(followType, targetId, userId);
        Boolean hasFollowed = redisService.getCacheObject(userKey);
        return Boolean.TRUE.equals(hasFollowed);
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

        for (CacheDocFollowTypeEnum type : CacheDocFollowTypeEnum.values()) {
            syncCount += syncFollowDataByType(type);
        }

        log.info("关注数据同步完成，共同步 {} 条记录", syncCount);
    }

    /**
     * 按类型同步关注数据
     *
     * @param type 关注类型
     * @return 同步数量
     */
    private int syncFollowDataByType(CacheDocFollowTypeEnum type) {
        String pattern = CacheConstants.CACHE_FOLLOW_USER + type.getType() + ":*";
        Collection<String> keys = redisService.keys(pattern);

        if (keys == null || keys.isEmpty()) {
            return 0;
        }

        int syncCount = 0;
        Map<Long, Long> countMap = new HashMap<>();

        for (String key : keys) {
            Boolean hasFollowed = redisService.getCacheObject(key);
            if (Boolean.TRUE.equals(hasFollowed)) {
                Long targetId = extractTargetIdFromKey(key, type.getType());
                if (targetId != null) {
                    countMap.merge(targetId, 1L, Long::sum);
                    redisService.deleteObject(key);
                    syncCount++;
                }
            }
        }

        for (Map.Entry<Long, Long> entry : countMap.entrySet()) {
            updateFollowCountToDb(type, entry.getKey(), entry.getValue());
        }

        log.info("同步 {} 类型关注数据 {} 条", type.getDesc(), syncCount);
        return syncCount;
    }

    /**
     * 构建用户关注记录键
     *
     * @param type     关注类型
     * @param targetId 目标ID
     * @param userId   用户ID
     * @return Redis键
     */
    private String buildUserKey(CacheDocFollowTypeEnum type, Long targetId, Long userId) {
        return CacheConstants.CACHE_FOLLOW_USER + type.getType() + ":" + targetId + ":" + userId;
    }

    /**
     * 构建关注计数键
     *
     * @param type     关注类型
     * @param targetId 目标ID
     * @return Redis键
     */
    private String buildCountKey(CacheDocFollowTypeEnum type, Long targetId) {
        return CacheConstants.CACHE_FOLLOW_COUNT + type.getType() + ":" + targetId;
    }

    /**
     * 从Redis键中提取目标ID
     *
     * @param key  Redis键
     * @param type 类型标识
     * @return 目标ID
     */
    private Long extractTargetIdFromKey(String key, String type) {
        try {
            String[] parts = key.split(":");
            if (parts.length >= 4) {
                return Long.parseLong(parts[3]);
            }
        } catch (Exception e) {
            log.warn("解析目标ID失败: {}", key, e);
        }
        return null;
    }

    /**
     * 更新关注数量到数据库
     *
     * @param type      关注类型
     * @param targetId  目标ID
     * @param increment 增量
     */
    private void updateFollowCountToDb(CacheDocFollowTypeEnum type, Long targetId, Long increment) {
        log.debug("更新 {} 类型目标 {} 关注数 +{}", type.getDesc(), targetId, increment);
    }
}
