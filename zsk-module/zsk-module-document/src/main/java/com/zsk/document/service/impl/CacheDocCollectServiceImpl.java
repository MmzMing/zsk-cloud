package com.zsk.document.service.impl;

import com.zsk.common.core.constant.CacheConstants;
import com.zsk.common.redis.service.RedisService;
import com.zsk.document.domain.DocNote;
import com.zsk.document.domain.DocVideoDetail;
import com.zsk.document.enums.CacheDocCollectTypeEnum;
import com.zsk.document.service.ICacheDocCollectService;
import com.zsk.document.service.IDocNoteService;
import com.zsk.document.service.IDocVideoDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 缓存文档收藏服务实现类
 * <p>
 * 使用两个Redis键实现收藏功能：
 * 1. COLLECT_COUNT_KEY: 存储收藏计数
 * 2. COLLECT_USER_KEY: 记录用户是否已收藏（用于判断是否需要入库）
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheDocCollectServiceImpl implements ICacheDocCollectService {

    /**
     * Redis服务
     */
    private final RedisService redisService;

    /**
     * Redis模板
     */
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 笔记服务
     */
    private final IDocNoteService docNoteService;

    /**
     * 视频详情服务
     */
    private final IDocVideoDetailService docVideoDetailService;

    /**
     * 用户收藏记录过期时间（小时）
     */
    private static final long COLLECT_USER_EXPIRE_HOURS = 24;

    /**
     * 收藏
     *
     * @param type     收藏类型
     * @param targetId 目标ID
     * @param userId   用户ID
     * @return 是否收藏成功
     */
    @Override
    public boolean collect(Integer type, Long targetId, Long userId) {
        CacheDocCollectTypeEnum collectType = CacheDocCollectTypeEnum.getByCode(type);
        if (collectType == null || targetId == null || userId == null) {
            return false;
        }

        String userKey = buildUserKey(collectType, targetId, userId);
        String countKey = buildCountKey(collectType, targetId);

        Boolean hasCollected = redisService.getCacheObject(userKey);
        if (Boolean.TRUE.equals(hasCollected)) {
            return false;
        }

        redisService.setCacheObject(userKey, true, COLLECT_USER_EXPIRE_HOURS, TimeUnit.HOURS);
        redisTemplate.opsForValue().increment(countKey, 1);

        log.debug("用户 {} 收藏 {} 类型目标 {}", userId, collectType.getDesc(), targetId);
        return true;
    }

    /**
     * 取消收藏
     *
     * @param type     收藏类型
     * @param targetId 目标ID
     * @param userId   用户ID
     * @return 是否取消成功
     */
    @Override
    public boolean uncollect(Integer type, Long targetId, Long userId) {
        CacheDocCollectTypeEnum collectType = CacheDocCollectTypeEnum.getByCode(type);
        if (collectType == null || targetId == null || userId == null) {
            return false;
        }

        String userKey = buildUserKey(collectType, targetId, userId);
        String countKey = buildCountKey(collectType, targetId);

        Boolean hasCollected = redisService.getCacheObject(userKey);
        if (!Boolean.TRUE.equals(hasCollected)) {
            return false;
        }

        redisService.deleteObject(userKey);
        redisTemplate.opsForValue().decrement(countKey, 1);

        log.debug("用户 {} 取消收藏 {} 类型目标 {}", userId, collectType.getDesc(), targetId);
        return true;
    }

    /**
     * 获取收藏数量
     *
     * @param type     收藏类型
     * @param targetId 目标ID
     * @return 收藏数量
     */
    @Override
    public Long getCollectCount(Integer type, Long targetId) {
        CacheDocCollectTypeEnum collectType = CacheDocCollectTypeEnum.getByCode(type);
        if (collectType == null || targetId == null) {
            return 0L;
        }

        String countKey = buildCountKey(collectType, targetId);
        Object count = redisTemplate.opsForValue().get(countKey);
        if (count != null) {
            return Long.parseLong(count.toString());
        }

        Long dbCount = getCollectCountFromDb(collectType, targetId);
        if (dbCount != null && dbCount > 0) {
            redisService.setCacheObject(countKey, dbCount);
        }
        return dbCount != null ? dbCount : 0L;
    }

    /**
     * 判断用户是否已收藏
     *
     * @param type     收藏类型
     * @param targetId 目标ID
     * @param userId   用户ID
     * @return 是否已收藏
     */
    @Override
    public boolean hasCollected(Integer type, Long targetId, Long userId) {
        CacheDocCollectTypeEnum collectType = CacheDocCollectTypeEnum.getByCode(type);
        if (collectType == null || targetId == null || userId == null) {
            return false;
        }

        String userKey = buildUserKey(collectType, targetId, userId);
        Boolean hasCollected = redisService.getCacheObject(userKey);
        return Boolean.TRUE.equals(hasCollected);
    }

    /**
     * 批量获取收藏数量
     *
     * @param type      收藏类型
     * @param targetIds 目标ID列表
     * @return 目标ID与收藏数量的映射
     */
    @Override
    public Map<Long, Long> getCollectCountBatch(Integer type, Iterable<Long> targetIds) {
        Map<Long, Long> result = new HashMap<>();
        CacheDocCollectTypeEnum collectType = CacheDocCollectTypeEnum.getByCode(type);
        if (collectType == null || targetIds == null) {
            return result;
        }

        for (Long targetId : targetIds) {
            result.put(targetId, getCollectCount(type, targetId));
        }
        return result;
    }

    /**
     * 同步收藏数据到数据库
     */
    @Override
    public void syncCollectDataToDb() {
        log.info("开始同步收藏数据到数据库...");
        int syncCount = 0;

        for (CacheDocCollectTypeEnum type : CacheDocCollectTypeEnum.values()) {
            syncCount += syncCollectDataByType(type);
        }

        log.info("收藏数据同步完成，共同步 {} 条记录", syncCount);
    }

    /**
     * 按类型同步收藏数据
     *
     * @param type 收藏类型
     * @return 同步数量
     */
    private int syncCollectDataByType(CacheDocCollectTypeEnum type) {
        String pattern = CacheConstants.CACHE_COLLECT_USER + type.getType() + ":*";
        Collection<String> keys = redisService.keys(pattern);

        if (keys == null || keys.isEmpty()) {
            return 0;
        }

        int syncCount = 0;
        Map<Long, Long> countMap = new HashMap<>();

        for (String key : keys) {
            Boolean hasCollected = redisService.getCacheObject(key);
            if (Boolean.TRUE.equals(hasCollected)) {
                Long targetId = extractTargetIdFromKey(key, type.getType());
                if (targetId != null) {
                    countMap.merge(targetId, 1L, Long::sum);
                    redisService.deleteObject(key);
                    syncCount++;
                }
            }
        }

        for (Map.Entry<Long, Long> entry : countMap.entrySet()) {
            updateCollectCountToDb(type, entry.getKey(), entry.getValue());
        }

        log.info("同步 {} 类型收藏数据 {} 条", type.getDesc(), syncCount);
        return syncCount;
    }

    /**
     * 构建用户收藏记录键
     *
     * @param type     收藏类型
     * @param targetId 目标ID
     * @param userId   用户ID
     * @return Redis键
     */
    private String buildUserKey(CacheDocCollectTypeEnum type, Long targetId, Long userId) {
        return CacheConstants.CACHE_COLLECT_USER + type.getType() + ":" + targetId + ":" + userId;
    }

    /**
     * 构建收藏计数键
     *
     * @param type     收藏类型
     * @param targetId 目标ID
     * @return Redis键
     */
    private String buildCountKey(CacheDocCollectTypeEnum type, Long targetId) {
        return CacheConstants.CACHE_COLLECT_COUNT + type.getType() + ":" + targetId;
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
     * 从数据库获取收藏数量
     *
     * @param type     收藏类型
     * @param targetId 目标ID
     * @return 收藏数量
     */
    private Long getCollectCountFromDb(CacheDocCollectTypeEnum type, Long targetId) {
        switch (type) {
            case NOTE:
                DocNote note = docNoteService.getById(targetId);
                return note != null ? note.getLikeCount() : 0L;
            case VIDEO:
                DocVideoDetail video = docVideoDetailService.getById(targetId);
                return video != null ? video.getCollectCount() : 0L;
            default:
                return 0L;
        }
    }

    /**
     * 更新收藏数量到数据库
     *
     * @param type      收藏类型
     * @param targetId  目标ID
     * @param increment 增量
     */
    private void updateCollectCountToDb(CacheDocCollectTypeEnum type, Long targetId, Long increment) {
        try {
            switch (type) {
                case NOTE:
                    DocNote note = docNoteService.getById(targetId);
                    if (note != null) {
                        note.setLikeCount(note.getLikeCount() != null ? note.getLikeCount() + increment : increment);
                        docNoteService.updateById(note);
                    }
                    break;
                case VIDEO:
                    DocVideoDetail video = docVideoDetailService.getById(targetId);
                    if (video != null) {
                        video.setCollectCount(video.getCollectCount() != null ? video.getCollectCount() + increment : increment);
                        docVideoDetailService.updateById(video);
                    }
                    break;
                default:
                    break;
            }
            log.debug("更新 {} 类型目标 {} 收藏数 +{}", type.getDesc(), targetId, increment);
        } catch (Exception e) {
            log.error("更新收藏数失败: type={}, targetId={}, increment={}", type, targetId, increment, e);
        }
    }
}
