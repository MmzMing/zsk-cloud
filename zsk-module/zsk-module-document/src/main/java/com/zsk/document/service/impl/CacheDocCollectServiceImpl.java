package com.zsk.document.service.impl;

import com.zsk.common.core.constant.CacheConstants;
import com.zsk.common.redis.service.RedisService;
import com.zsk.document.domain.DocNote;
import com.zsk.document.domain.DocVideoDetail;
import com.zsk.document.domain.DocUserInteraction;
import com.zsk.document.domain.context.DocUserInteractionContext;
import com.zsk.document.enums.CacheDocCollectTypeEnum;
import com.zsk.document.mapper.DocUserInteractionMapper;
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
 * 使用三个Redis键实现收藏功能：
 * 1. collect:user:{user_id} Hash 存储用户收藏的所有内容 {targetId:type}
 * 2. collect:count:{target_id}:{type} String 存储内容的实时收藏数
 * 3. collect:lock:{user_id}:{target_id} String 分布式锁（防止重复收藏，过期时间1分钟）
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheDocCollectServiceImpl implements ICacheDocCollectService {

    private final RedisService redisService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final IDocNoteService docNoteService;
    private final IDocVideoDetailService docVideoDetailService;
    private final DocUserInteractionMapper docUserInteractionMapper;

    /**
     * 分布式锁过期时间（秒）
     */
    private static final long LOCK_EXPIRE_SECONDS = 60;

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

        String lockKey = buildLockKey(userId, targetId);
        String userKey = buildUserKey(userId);
        String countKey = buildCountKey(targetId, collectType);
        String hashField = targetId + ":" + type;

        Boolean locked = redisService.getCacheObject(lockKey);
        if (Boolean.TRUE.equals(locked)) {
            log.debug("用户 {} 对目标 {} 操作频繁，请稍后再试", userId, targetId);
            return false;
        }

        Object existingType = redisTemplate.opsForHash().get(userKey, hashField);
        if (existingType != null) {
            log.debug("用户 {} 已收藏目标 {}", userId, targetId);
            return false;
        }

        redisService.setCacheObject(lockKey, true, LOCK_EXPIRE_SECONDS, TimeUnit.SECONDS);
        redisTemplate.opsForHash().put(userKey, hashField, type.toString());
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

        String lockKey = buildLockKey(userId, targetId);
        String userKey = buildUserKey(userId);
        String countKey = buildCountKey(targetId, collectType);
        String hashField = targetId + ":" + type;

        Boolean locked = redisService.getCacheObject(lockKey);
        if (Boolean.TRUE.equals(locked)) {
            log.debug("用户 {} 对目标 {} 操作频繁，请稍后再试", userId, targetId);
            return false;
        }

        Object existingType = redisTemplate.opsForHash().get(userKey, hashField);
        if (existingType == null) {
            log.debug("用户 {} 未收藏目标 {}", userId, targetId);
            return false;
        }

        redisService.setCacheObject(lockKey, true, LOCK_EXPIRE_SECONDS, TimeUnit.SECONDS);
        redisTemplate.opsForHash().delete(userKey, hashField);
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

        String countKey = buildCountKey(targetId, collectType);
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

        String userKey = buildUserKey(userId);
        String hashField = targetId + ":" + type;
        Object existingType = redisTemplate.opsForHash().get(userKey, hashField);
        if (existingType != null) {
            return true;
        }

        DocUserInteraction interaction = docUserInteractionMapper.selectByUserAndTarget(
            userId, getTargetType(collectType), targetId, DocUserInteractionContext.INTERACTION_TYPE_FAVORITE);
        return interaction != null && interaction.getStatus() == 1;
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

        String pattern = CacheConstants.CACHE_COLLECT_USER + "*";
        Collection<String> keys = redisService.keys(pattern);

        if (keys == null || keys.isEmpty()) {
            log.info("没有需要同步的收藏数据");
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

            Map<Object, Object> collectMap = redisTemplate.opsForHash().entries(userKey);
            for (Map.Entry<Object, Object> entry : collectMap.entrySet()) {
                String field = entry.getKey().toString();
                String[] parts = field.split(":");
                if (parts.length >= 2) {
                    Long targetId = Long.parseLong(parts[0]);
                    Integer type = Integer.parseInt(parts[1]);
                    CacheDocCollectTypeEnum collectType = CacheDocCollectTypeEnum.getByCode(type);
                    if (collectType != null) {
                        saveInteractionToDb(userId, getTargetType(collectType), targetId,
                            DocUserInteractionContext.INTERACTION_TYPE_FAVORITE);
                        updateCollectCountToDb(collectType, targetId, 1L);
                        syncCount++;
                    }
                }
            }
            redisService.deleteObject(userKey);
        }

        log.info("收藏数据同步完成，共同步 {} 条记录", syncCount);
    }

    /**
     * 构建用户收藏记录键
     *
     * @param userId 用户ID
     * @return Redis键
     */
    private String buildUserKey(Long userId) {
        return CacheConstants.CACHE_COLLECT_USER + userId;
    }

    /**
     * 构建收藏计数键
     *
     * @param targetId 目标ID
     * @param type     收藏类型
     * @return Redis键
     */
    private String buildCountKey(Long targetId, CacheDocCollectTypeEnum type) {
        return CacheConstants.CACHE_COLLECT_COUNT + targetId + ":" + type.getType();
    }

    /**
     * 构建分布式锁键
     *
     * @param userId   用户ID
     * @param targetId 目标ID
     * @return Redis键
     */
    private String buildLockKey(Long userId, Long targetId) {
        return CacheConstants.CACHE_COLLECT_USER + "lock:" + userId + ":" + targetId;
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
                return note != null ? note.getCollectCount() : 0L;
            case VIDEO:
                DocVideoDetail video = docVideoDetailService.getById(targetId);
                return video != null ? video.getCollectCount() : 0L;
            default:
                return 0L;
        }
    }

    /**
     * 获取目标类型
     *
     * @param collectType 收藏类型枚举
     * @return 目标类型
     */
    private Integer getTargetType(CacheDocCollectTypeEnum collectType) {
        switch (collectType) {
            case NOTE:
                return DocUserInteractionContext.TARGET_TYPE_NOTE;
            case VIDEO:
                return DocUserInteractionContext.TARGET_TYPE_VIDEO;
            default:
                return null;
        }
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
                        note.setCollectCount(note.getCollectCount() != null ? note.getCollectCount() + increment : increment);
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
