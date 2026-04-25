package com.zsk.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zsk.document.domain.DocNote;
import com.zsk.document.domain.DocNoteComment;
import com.zsk.document.domain.DocVideo;
import com.zsk.document.domain.DocVideoComment;
import com.zsk.document.domain.vo.AllStatsVo;
import com.zsk.document.domain.vo.UserStatsVo;
import com.zsk.document.enums.CacheDocCollectTypeEnum;
import com.zsk.document.enums.CacheDocFollowTypeEnum;
import com.zsk.document.enums.CacheDocLikeTypeEnum;
import com.zsk.document.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.WeekFields;

/**
 * 统计信息Service业务层处理
 * <p>
 * 实现文档系统统计信息的查询逻辑，包含用户统计和内容统计两大模块。
 * 用户统计数据优先从 Redis 缓存获取，内容统计数据从数据库查询。
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocStatsServiceImpl implements IDocStatsService {

    /**
     * 文档点赞缓存服务，用于获取用户点赞统计数据
     */
    private final ICacheDocLikeService cacheDocLikeService;

    /**
     * 文档收藏缓存服务，用于获取用户收藏统计数据
     */
    private final ICacheDocCollectService cacheDocCollectService;

    /**
     * 文档关注缓存服务，用于获取用户关注统计数据
     */
    private final ICacheDocFollowService cacheDocFollowService;

    /**
     * 笔记评论服务，用于查询用户发表的评论数量
     */
    private final IDocNoteCommentService commentService;

    /**
     * 笔记服务，用于查询笔记相关统计数据
     */
    private final IDocNoteService noteService;

    /**
     * 视频服务，用于查询视频相关统计数据
     */
    private final IDocVideoService videoService;

    /**
     * 视频评论服务，用于查询视频评论相关统计数据
     */
    private final IDocVideoCommentService videoCommentService;

    /**
     * 获取用户统计信息
     * <p>
     * 从缓存服务获取用户的点赞、关注、收藏统计数据，从数据库查询评论数量。
     * 各项统计数据均做非空判断，确保返回值不为null。
     * </p>
     *
     * @param userId 用户ID
     * @return 用户统计信息VO
     */
    @Override
    public UserStatsVo getUserStats(Long userId) {
        log.info("开始获取用户统计信息, userId={}", userId);

        // 1. 计算点赞总数（笔记点赞 + 视频点赞）
        Long noteLikeCount = cacheDocLikeService.getLikeCount(CacheDocLikeTypeEnum.NOTE.getCode(), userId);
        Long videoLikeCount = cacheDocLikeService.getLikeCount(CacheDocLikeTypeEnum.VIDEO.getCode(), userId);
        Long likeCount = (noteLikeCount != null ? noteLikeCount : 0) + (videoLikeCount != null ? videoLikeCount : 0);

        // 2. 计算关注总数（用户关注 + 笔记作者关注 + 视频作者关注）
        Long userFollowCount = cacheDocFollowService.getFollowCount(CacheDocFollowTypeEnum.USER.getCode(), userId);
        Long noteAuthorFollowCount = cacheDocFollowService.getFollowCount(CacheDocFollowTypeEnum.NOTE_AUTHOR.getCode(), userId);
        Long videoAuthorFollowCount = cacheDocFollowService.getFollowCount(CacheDocFollowTypeEnum.VIDEO_AUTHOR.getCode(), userId);
        Long followCount = (userFollowCount != null ? userFollowCount : 0)
                + (noteAuthorFollowCount != null ? noteAuthorFollowCount : 0)
                + (videoAuthorFollowCount != null ? videoAuthorFollowCount : 0);

        // 3. 计算收藏总数（笔记收藏 + 视频收藏）
        Long noteCollectCount = cacheDocCollectService.getCollectCount(CacheDocCollectTypeEnum.NOTE.getCode(), userId);
        Long videoCollectCount = cacheDocCollectService.getCollectCount(CacheDocCollectTypeEnum.VIDEO.getCode(), userId);
        Long collectCount = (noteCollectCount != null ? noteCollectCount : 0) + (videoCollectCount != null ? videoCollectCount : 0);

        // 4. 查询用户发表的评论总数
        Long commentCount = commentService.count(
                new LambdaQueryWrapper<DocNoteComment>()
                        .eq(DocNoteComment::getDeleted, 0)
                        .eq(DocNoteComment::getCommentUserId, String.valueOf(userId))
        );

        // 5. 构建用户统计信息VO
        UserStatsVo statsVo = UserStatsVo.builder()
                .userId(userId)
                .likeCount(likeCount)
                .fanCount(followCount)
                .collectCount(collectCount)
                .commentCount(commentCount != null ? commentCount : 0)
                .build();

        log.info("获取用户统计信息完成, userId={}, likeCount={}, fanCount={}, collectCount={}, commentCount={}",
                userId, likeCount, followCount, collectCount, commentCount);

        return statsVo;
    }

    /**
     * 获取内容统计信息
     * <p>
     * 从数据库查询文档系统的全局统计数据，包括总数统计和上周增量统计。
     * 使用 LambdaQueryWrapper 构建查询条件，仅查询未删除的内容。
     * </p>
     *
     * @return 内容统计信息VO
     */
    @Override
    public AllStatsVo getContentStats() {
        log.info("开始获取内容统计信息");

        // 1. 查询文章总数
        Long articleCount = noteService.count(
                new LambdaQueryWrapper<DocNote>()
                        .eq(DocNote::getDeleted, 0)
        );

        // 2. 查询视频总数
        Long videoCount = videoService.count(
                new LambdaQueryWrapper<DocVideo>()
                        .eq(DocVideo::getDeleted, 0)
        );

        // 3. 查询评论总数（笔记评论 + 视频评论）
        Long noteCommentCount = commentService.count(
                new LambdaQueryWrapper<DocNoteComment>()
                        .eq(DocNoteComment::getDeleted, 0)
        );
        Long videoCommentCount = videoCommentService.count(
                new LambdaQueryWrapper<DocVideoComment>()
                        .eq(DocVideoComment::getDeleted, 0)
        );
        Long totalCommentCount = (noteCommentCount != null ? noteCommentCount : 0)
                + (videoCommentCount != null ? videoCommentCount : 0);

        // 4. 计算上周时间范围（周一 00:00 到下周一 00:00）
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastWeekStart = now.minusWeeks(1).with(WeekFields.ISO.dayOfWeek(), 1).toLocalDate().atStartOfDay();
        LocalDateTime lastWeekEnd = lastWeekStart.plusDays(7);

        // 5. 查询上周新增文章数
        Long lastWeekArticleCount = noteService.count(
                new LambdaQueryWrapper<DocNote>()
                        .eq(DocNote::getDeleted, 0)
                        .between(DocNote::getCreateTime, lastWeekStart, lastWeekEnd)
        );

        // 6. 查询上周新增视频数
        Long lastWeekVideoCount = videoService.count(
                new LambdaQueryWrapper<DocVideo>()
                        .eq(DocVideo::getDeleted, 0)
                        .between(DocVideo::getCreateTime, lastWeekStart, lastWeekEnd)
        );

        // 7. 查询上周新增评论数
        Long lastWeekNoteCommentCount = commentService.count(
                new LambdaQueryWrapper<DocNoteComment>()
                        .eq(DocNoteComment::getDeleted, 0)
                        .between(DocNoteComment::getCreateTime, lastWeekStart, lastWeekEnd)
        );
        Long lastWeekVideoCommentCount = videoCommentService.count(
                new LambdaQueryWrapper<DocVideoComment>()
                        .eq(DocVideoComment::getDeleted, 0)
                        .between(DocVideoComment::getCreateTime, lastWeekStart, lastWeekEnd)
        );
        Long lastWeekTotalCommentCount = (lastWeekNoteCommentCount != null ? lastWeekNoteCommentCount : 0)
                + (lastWeekVideoCommentCount != null ? lastWeekVideoCommentCount : 0);

        // 8. 构建内容统计信息VO
        AllStatsVo statsVo = AllStatsVo.builder()
                .docCount(articleCount != null ? articleCount : 0)
                .videoCount(videoCount != null ? videoCount : 0)
                .commentCount(totalCommentCount)
                .lastWeekDocCount(lastWeekArticleCount != null ? lastWeekArticleCount : 0)
                .lastWeekVideoCount(lastWeekVideoCount != null ? lastWeekVideoCount : 0)
                .lastWeekCommentCount(lastWeekTotalCommentCount)
                .build();

        log.info("获取内容统计信息完成, docCount={}, videoCount={}, commentCount={}, " +
                        "lastWeekDocCount={}, lastWeekVideoCount={}, lastWeekCommentCount={}",
                statsVo.getDocCount(), statsVo.getVideoCount(), statsVo.getCommentCount(),
                statsVo.getLastWeekDocCount(), statsVo.getLastWeekVideoCount(), statsVo.getLastWeekCommentCount());

        return statsVo;
    }
}