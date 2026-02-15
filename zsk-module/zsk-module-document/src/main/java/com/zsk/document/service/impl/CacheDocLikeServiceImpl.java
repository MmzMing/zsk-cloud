package com.zsk.document.service.impl;

import com.zsk.common.core.constant.CacheConstants;
import com.zsk.common.redis.service.RedisService;
import com.zsk.document.domain.DocNote;
import com.zsk.document.domain.DocNoteComment;
import com.zsk.document.domain.DocVideoComment;
import com.zsk.document.domain.DocVideoDetail;
import com.zsk.document.enums.CacheDocLikeTypeEnum;
import com.zsk.document.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 缓存文档点赞服务实现类
 * <p>
 * 使用两个Redis键实现点赞功能：
 * 1. LIKE_COUNT_KEY: 存储点赞计数
 * 2. LIKE_USER_KEY: 记录用户是否已点赞（用于判断是否需要入库）
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
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
     * 笔记服务
     */
    private final IDocNoteService docNoteService;

    /**
     * 笔记评论服务
     */
    private final IDocNoteCommentService docNoteCommentService;

    /**
     * 视频详情服务
     */
    private final IDocVideoDetailService docVideoDetailService;

    /**
     * 视频评论服务
     */
    private final IDocVideoCommentService docVideoCommentService;

    /**
     * 用户点赞记录过期时间（小时）
     */
    private static final long LIKE_USER_EXPIRE_HOURS = 24;

    /**
     * 点赞
     *
     * @param type     点赞类型
     * @param targetId 目标ID
     * @param userId   用户ID
     * @return 是否点赞成功
     */
    @Override
    public boolean like(Integer type, Long targetId, Long userId) {
        CacheDocLikeTypeEnum likeType = CacheDocLikeTypeEnum.getByCode(type);
        if (likeType == null || targetId == null || userId == null) {
            return false;
        }

        String userKey = buildUserKey(likeType, targetId, userId);
        String countKey = buildCountKey(likeType, targetId);

        Boolean hasLiked = redisService.getCacheObject(userKey);
        if (Boolean.TRUE.equals(hasLiked)) {
            return false;
        }

        redisService.setCacheObject(userKey, true, LIKE_USER_EXPIRE_HOURS, TimeUnit.HOURS);
        redisTemplate.opsForValue().increment(countKey, 1);

        log.debug("用户 {} 点赞 {} 类型目标 {}", userId, likeType.getDesc(), targetId);
        return true;
    }

    /**
     * 取消点赞
     *
     * @param type     点赞类型
     * @param targetId 目标ID
     * @param userId   用户ID
     * @return 是否取消成功
     */
    @Override
    public boolean unlike(Integer type, Long targetId, Long userId) {
        CacheDocLikeTypeEnum likeType = CacheDocLikeTypeEnum.getByCode(type);
        if (likeType == null || targetId == null || userId == null) {
            return false;
        }

        String userKey = buildUserKey(likeType, targetId, userId);
        String countKey = buildCountKey(likeType, targetId);

        Boolean hasLiked = redisService.getCacheObject(userKey);
        if (!Boolean.TRUE.equals(hasLiked)) {
            return false;
        }

        redisService.deleteObject(userKey);
        redisTemplate.opsForValue().decrement(countKey, 1);

        log.debug("用户 {} 取消点赞 {} 类型目标 {}", userId, likeType.getDesc(), targetId);
        return true;
    }

    /**
     * 获取点赞数量
     *
     * @param type     点赞类型
     * @param targetId 目标ID
     * @return 点赞数量
     */
    @Override
    public Long getLikeCount(Integer type, Long targetId) {
        CacheDocLikeTypeEnum likeType = CacheDocLikeTypeEnum.getByCode(type);
        if (likeType == null || targetId == null) {
            return 0L;
        }

        String countKey = buildCountKey(likeType, targetId);
        Object count = redisTemplate.opsForValue().get(countKey);
        if (count != null) {
            return Long.parseLong(count.toString());
        }

        Long dbCount = getLikeCountFromDb(likeType, targetId);
        if (dbCount != null && dbCount > 0) {
            redisService.setCacheObject(countKey, dbCount);
        }
        return dbCount != null ? dbCount : 0L;
    }

    /**
     * 判断用户是否已点赞
     *
     * @param type     点赞类型
     * @param targetId 目标ID
     * @param userId   用户ID
     * @return 是否已点赞
     */
    @Override
    public boolean hasLiked(Integer type, Long targetId, Long userId) {
        CacheDocLikeTypeEnum likeType = CacheDocLikeTypeEnum.getByCode(type);
        if (likeType == null || targetId == null || userId == null) {
            return false;
        }

        String userKey = buildUserKey(likeType, targetId, userId);
        Boolean hasLiked = redisService.getCacheObject(userKey);
        return Boolean.TRUE.equals(hasLiked);
    }

    /**
     * 批量获取点赞数量
     *
     * @param type      点赞类型
     * @param targetIds 目标ID列表
     * @return 目标ID与点赞数量的映射
     */
    @Override
    public Map<Long, Long> getLikeCountBatch(Integer type, Iterable<Long> targetIds) {
        Map<Long, Long> result = new HashMap<>();
        CacheDocLikeTypeEnum likeType = CacheDocLikeTypeEnum.getByCode(type);
        if (likeType == null || targetIds == null) {
            return result;
        }

        for (Long targetId : targetIds) {
            result.put(targetId, getLikeCount(type, targetId));
        }
        return result;
    }

    /**
     * 同步点赞数据到数据库
     */
    @Override
    public void syncLikeDataToDb() {
        log.info("开始同步点赞数据到数据库...");
        int syncCount = 0;

        for (CacheDocLikeTypeEnum type : CacheDocLikeTypeEnum.values()) {
            syncCount += syncLikeDataByType(type);
        }

        log.info("点赞数据同步完成，共同步 {} 条记录", syncCount);
    }

    /**
     * 按类型同步点赞数据
     *
     * @param type 点赞类型
     * @return 同步数量
     */
    private int syncLikeDataByType(CacheDocLikeTypeEnum type) {
        String pattern = CacheConstants.CACHE_LIKE_USER + type.getType() + ":*";
        Collection<String> keys = redisService.keys(pattern);

        if (keys == null || keys.isEmpty()) {
            return 0;
        }

        int syncCount = 0;
        Map<Long, Long> countMap = new HashMap<>();

        for (String key : keys) {
            Boolean hasLiked = redisService.getCacheObject(key);
            if (Boolean.TRUE.equals(hasLiked)) {
                Long targetId = extractTargetIdFromKey(key, type.getType());
                if (targetId != null) {
                    countMap.merge(targetId, 1L, Long::sum);
                    redisService.deleteObject(key);
                    syncCount++;
                }
            }
        }

        for (Map.Entry<Long, Long> entry : countMap.entrySet()) {
            updateLikeCountToDb(type, entry.getKey(), entry.getValue());
        }

        log.info("同步 {} 类型点赞数据 {} 条", type.getDesc(), syncCount);
        return syncCount;
    }

    /**
     * 构建用户点赞记录键
     *
     * @param type     点赞类型
     * @param targetId 目标ID
     * @param userId   用户ID
     * @return Redis键
     */
    private String buildUserKey(CacheDocLikeTypeEnum type, Long targetId, Long userId) {
        return CacheConstants.CACHE_LIKE_USER + type.getType() + ":" + targetId + ":" + userId;
    }

    /**
     * 构建点赞计数键
     *
     * @param type     点赞类型
     * @param targetId 目标ID
     * @return Redis键
     */
    private String buildCountKey(CacheDocLikeTypeEnum type, Long targetId) {
        return CacheConstants.CACHE_LIKE_COUNT + type.getType() + ":" + targetId;
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
     * 从数据库获取点赞数量
     *
     * @param type     点赞类型
     * @param targetId 目标ID
     * @return 点赞数量
     */
    private Long getLikeCountFromDb(CacheDocLikeTypeEnum type, Long targetId) {
        switch (type) {
            case NOTE:
                DocNote note = docNoteService.getById(targetId);
                return note != null ? note.getLikeCount() : 0L;
            case NOTE_COMMENT:
                DocNoteComment noteComment = docNoteCommentService.getById(targetId);
                return noteComment != null ? noteComment.getLikeCount() : 0L;
            case VIDEO:
                DocVideoDetail video = docVideoDetailService.getById(targetId);
                return video != null ? video.getLikeCount() : 0L;
            case VIDEO_COMMENT:
                DocVideoComment videoComment = docVideoCommentService.getById(targetId);
                return videoComment != null ? videoComment.getLikeCount() : 0L;
            default:
                return 0L;
        }
    }

    /**
     * 更新点赞数量到数据库
     *
     * @param type      点赞类型
     * @param targetId  目标ID
     * @param increment 增量
     */
    private void updateLikeCountToDb(CacheDocLikeTypeEnum type, Long targetId, Long increment) {
        try {
            switch (type) {
                case NOTE:
                    DocNote note = docNoteService.getById(targetId);
                    if (note != null) {
                        note.setLikeCount(note.getLikeCount() != null ? note.getLikeCount() + increment : increment);
                        docNoteService.updateById(note);
                    }
                    break;
                case NOTE_COMMENT:
                    DocNoteComment noteComment = docNoteCommentService.getById(targetId);
                    if (noteComment != null) {
                        noteComment.setLikeCount(noteComment.getLikeCount() != null ? noteComment.getLikeCount() + increment : increment);
                        docNoteCommentService.updateById(noteComment);
                    }
                    break;
                case VIDEO:
                    DocVideoDetail video = docVideoDetailService.getById(targetId);
                    if (video != null) {
                        video.setLikeCount(video.getLikeCount() != null ? video.getLikeCount() + increment : increment);
                        docVideoDetailService.updateById(video);
                    }
                    break;
                case VIDEO_COMMENT:
                    DocVideoComment videoComment = docVideoCommentService.getById(targetId);
                    if (videoComment != null) {
                        videoComment.setLikeCount(videoComment.getLikeCount() != null ? videoComment.getLikeCount() + increment : increment);
                        docVideoCommentService.updateById(videoComment);
                    }
                    break;
                default:
                    break;
            }
            log.debug("更新 {} 类型目标 {} 点赞数 +{}", type.getDesc(), targetId, increment);
        } catch (Exception e) {
            log.error("更新点赞数失败: type={}, targetId={}, increment={}", type, targetId, increment, e);
        }
    }
}
