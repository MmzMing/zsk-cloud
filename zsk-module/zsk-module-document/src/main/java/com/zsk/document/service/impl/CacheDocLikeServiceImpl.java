package com.zsk.document.service.impl;

import com.zsk.common.core.constant.CacheConstants;
import com.zsk.common.redis.service.RedisService;
import com.zsk.document.domain.DocNote;
import com.zsk.document.domain.DocNoteComment;
import com.zsk.document.domain.DocVideoComment;
import com.zsk.document.domain.DocVideoDetail;
import com.zsk.document.domain.DocUserInteraction;
import com.zsk.document.domain.context.DocUserInteractionContext;
import com.zsk.document.enums.CacheDocLikeTypeEnum;
import com.zsk.document.mapper.DocUserInteractionMapper;
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
 * 使用三个Redis键实现点赞功能：
 * 1. like:user:{user_id} Hash 存储用户点赞的所有内容 {targetId:type}
 * 2. like:count:{target_id}:{type} String 存储内容的实时点赞数
 * 3. like:lock:{user_id}:{target_id} String 分布式锁（防止重复点赞，过期时间1分钟）
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheDocLikeServiceImpl implements ICacheDocLikeService {

    private final RedisService redisService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final IDocNoteService docNoteService;
    private final IDocNoteCommentService docNoteCommentService;
    private final IDocVideoDetailService docVideoDetailService;
    private final IDocVideoCommentService docVideoCommentService;
    private final DocUserInteractionMapper docUserInteractionMapper;

    /**
     * 分布式锁过期时间（秒）
     */
    private static final long LOCK_EXPIRE_SECONDS = 60;

    /**
     * 点赞次数限制
     */
    private static final int LIKE_LIMIT = 10;

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

        String lockKey = buildLockKey(userId, targetId);
        String userKey = buildUserKey(userId);
        String countKey = buildCountKey(targetId, likeType);
        String hashField = targetId + ":" + type;

        Boolean locked = redisService.getCacheObject(lockKey);
        if (Boolean.TRUE.equals(locked)) {
            log.debug("用户 {} 对目标 {} 操作频繁，请稍后再试", userId, targetId);
            return false;
        }

        Object existingType = redisTemplate.opsForHash().get(userKey, hashField);
        if (existingType != null) {
            log.debug("用户 {} 已点赞目标 {}", userId, targetId);
            return false;
        }

        redisService.setCacheObject(lockKey, true, LOCK_EXPIRE_SECONDS, TimeUnit.SECONDS);
        redisTemplate.opsForHash().put(userKey, hashField, type.toString());
        redisTemplate.opsForValue().increment(countKey, 1);

        Long likeCount = getLikeCountFromLock(userId, targetId);
        if (likeCount >= LIKE_LIMIT) {
            redisService.setCacheObject(lockKey, true, LOCK_EXPIRE_SECONDS, TimeUnit.SECONDS);
        }

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

        String lockKey = buildLockKey(userId, targetId);
        String userKey = buildUserKey(userId);
        String countKey = buildCountKey(targetId, likeType);
        String hashField = targetId + ":" + type;

        Boolean locked = redisService.getCacheObject(lockKey);
        if (Boolean.TRUE.equals(locked)) {
            log.debug("用户 {} 对目标 {} 操作频繁，请稍后再试", userId, targetId);
            return false;
        }

        Object existingType = redisTemplate.opsForHash().get(userKey, hashField);
        if (existingType == null) {
            log.debug("用户 {} 未点赞目标 {}", userId, targetId);
            return false;
        }

        redisService.setCacheObject(lockKey, true, LOCK_EXPIRE_SECONDS, TimeUnit.SECONDS);
        redisTemplate.opsForHash().delete(userKey, hashField);
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

        String countKey = buildCountKey(targetId, likeType);
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

        String userKey = buildUserKey(userId);
        String hashField = targetId + ":" + type;
        Object existingType = redisTemplate.opsForHash().get(userKey, hashField);
        if (existingType != null) {
            return true;
        }

        DocUserInteraction interaction = docUserInteractionMapper.selectByUserAndTarget(
            userId, getTargetType(likeType), targetId, DocUserInteractionContext.INTERACTION_TYPE_LIKE);
        return interaction != null && interaction.getStatus() == 1;
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

        String pattern = CacheConstants.CACHE_LIKE_USER + "*";
        Collection<String> keys = redisService.keys(pattern);

        if (keys == null || keys.isEmpty()) {
            log.info("没有需要同步的点赞数据");
            return;
        }

        for (String userKey : keys) {
            Long userId = extractUserIdFromKey(userKey);
            if (userId == null) {
                continue;
            }

            Map<Object, Object> likeMap = redisTemplate.opsForHash().entries(userKey);
            for (Map.Entry<Object, Object> entry : likeMap.entrySet()) {
                String field = entry.getKey().toString();
                String[] parts = field.split(":");
                if (parts.length >= 2) {
                    Long targetId = Long.parseLong(parts[0]);
                    Integer type = Integer.parseInt(parts[1]);
                    CacheDocLikeTypeEnum likeType = CacheDocLikeTypeEnum.getByCode(type);
                    if (likeType != null) {
                        saveInteractionToDb(userId, getTargetType(likeType), targetId,
                            DocUserInteractionContext.INTERACTION_TYPE_LIKE);
                        updateLikeCountToDb(likeType, targetId, 1L);
                        syncCount++;
                    }
                }
            }
            redisService.deleteObject(userKey);
        }

        log.info("点赞数据同步完成，共同步 {} 条记录", syncCount);
    }

    /**
     * 构建用户点赞记录键
     *
     * @param userId 用户ID
     * @return Redis键
     */
    private String buildUserKey(Long userId) {
        return CacheConstants.CACHE_LIKE_USER + userId;
    }

    /**
     * 构建点赞计数键
     *
     * @param targetId 目标ID
     * @param type     点赞类型
     * @return Redis键
     */
    private String buildCountKey(Long targetId, CacheDocLikeTypeEnum type) {
        return CacheConstants.CACHE_LIKE_COUNT + targetId + ":" + type.getType();
    }

    /**
     * 构建分布式锁键
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
     * 获取用户对某个目标的点赞次数（从锁记录中）
     *
     * @param userId   用户ID
     * @param targetId 目标ID
     * @return 点赞次数
     */
    private Long getLikeCountFromLock(Long userId, Long targetId) {
        return 1L;
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
     * 获取目标类型
     *
     * @param likeType 点赞类型枚举
     * @return 目标类型
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
