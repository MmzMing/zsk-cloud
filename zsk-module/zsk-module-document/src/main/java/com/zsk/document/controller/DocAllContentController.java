package com.zsk.document.controller;

import com.zsk.common.core.domain.R;
import com.zsk.common.security.utils.SecurityUtils;
import com.zsk.document.domain.vo.AllStatsVo;
import com.zsk.document.domain.vo.UserStatsVo;
import com.zsk.document.enums.CacheDocCollectTypeEnum;
import com.zsk.document.enums.CacheDocFollowTypeEnum;
import com.zsk.document.enums.CacheDocLikeTypeEnum;
import com.zsk.document.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.temporal.WeekFields;

/**
 * 用户统计信息 控制器
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Tag(name = "用户统计信息")
@RestController
@RequestMapping("/docAllContent")
@RequiredArgsConstructor
public class DocAllContentController {

    private final ICacheDocLikeService cacheDocLikeService;
    private final ICacheDocCollectService cacheDocCollectService;
    private final ICacheDocFollowService cacheDocFollowService;
    private final IDocNoteCommentService commentService;
    private final IDocNoteService noteService;
    private final IDocVideoService videoService;
    private final IDocVideoCommentService videoCommentService;

    /**
     * 获取用户统计信息（点赞、关注、收藏总数）
     * 先从缓存获取，缓存不存在则从数据库获取
     *
     * @return 用户统计信息
     */
    @Operation(summary = "获取用户统计信息")
    @GetMapping("/user/stats")
    public R<UserStatsVo> getUserStats() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }

        // 计算点赞总数（包含笔记和视频）
        Long noteLikeCount = cacheDocLikeService.getLikeCount(CacheDocLikeTypeEnum.NOTE.getCode(), userId);
        Long videoLikeCount = cacheDocLikeService.getLikeCount(CacheDocLikeTypeEnum.VIDEO.getCode(), userId);
        Long likeCount = (noteLikeCount != null ? noteLikeCount : 0) + (videoLikeCount != null ? videoLikeCount : 0);

        // 计算关注总数（包含用户、笔记作者和视频作者）
        Long userFollowCount = cacheDocFollowService.getFollowCount(CacheDocFollowTypeEnum.USER.getCode(), userId);
        Long noteAuthorFollowCount = cacheDocFollowService.getFollowCount(CacheDocFollowTypeEnum.NOTE_AUTHOR.getCode(), userId);
        Long videoAuthorFollowCount = cacheDocFollowService.getFollowCount(CacheDocFollowTypeEnum.VIDEO_AUTHOR.getCode(), userId);
        Long followCount = (userFollowCount != null ? userFollowCount : 0) + (noteAuthorFollowCount != null ? noteAuthorFollowCount : 0) + (videoAuthorFollowCount != null ? videoAuthorFollowCount : 0);

        // 计算收藏总数（包含笔记和视频）
        Long noteCollectCount = cacheDocCollectService.getCollectCount(CacheDocCollectTypeEnum.NOTE.getCode(), userId);
        Long videoCollectCount = cacheDocCollectService.getCollectCount(CacheDocCollectTypeEnum.VIDEO.getCode(), userId);
        Long collectCount = (noteCollectCount != null ? noteCollectCount : 0) + (videoCollectCount != null ? videoCollectCount : 0);

        // 计算评论总数（当前用户发表的评论）
        Long commentCount = commentService.count(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.zsk.document.domain.DocNoteComment>()
                        .eq(com.zsk.document.domain.DocNoteComment::getDeleted, 0)
                        .eq(com.zsk.document.domain.DocNoteComment::getCommentUserId, String.valueOf(userId))
        );

        UserStatsVo statsVo = UserStatsVo.builder()
                .userId(userId)
                .likeCount(likeCount)
                .fanCount(followCount)
                .collectCount(collectCount)
                .commentCount(commentCount != null ? commentCount : 0)
                .build();

        return R.ok(statsVo);
    }

    /**
     * 获取内容统计信息（文章总数、视频总数、评论总数）
     *
     * @return 内容统计信息
     */
    @Operation(summary = "获取内容统计信息")
    @GetMapping("/content/stats")
    public R<AllStatsVo> getContentStats() {
        // 文章总数
        Long articleCount = noteService.count(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.zsk.document.domain.DocNote>()
                        .eq(com.zsk.document.domain.DocNote::getDeleted, 0)
        );

        // 视频总数
        Long videoCount = videoService.count(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.zsk.document.domain.DocVideo>()
                        .eq(com.zsk.document.domain.DocVideo::getDeleted, 0)
        );

        // 评论总数（文章评论 + 视频评论）
        Long noteCommentCount = commentService.count(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.zsk.document.domain.DocNoteComment>()
                        .eq(com.zsk.document.domain.DocNoteComment::getDeleted, 0)
        );
        Long videoCommentCount = videoCommentService.count(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.zsk.document.domain.DocVideoComment>()
                        .eq(com.zsk.document.domain.DocVideoComment::getDeleted, 0)
        );
        Long totalCommentCount = (noteCommentCount != null ? noteCommentCount : 0)
                + (videoCommentCount != null ? videoCommentCount : 0);

        // 计算上周时间范围
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastWeekStart = now.minusWeeks(1).with(WeekFields.ISO.dayOfWeek(), 1).toLocalDate().atStartOfDay();
        LocalDateTime lastWeekEnd = lastWeekStart.plusDays(7);

        // 上周新增文章数
        Long lastWeekArticleCount = noteService.count(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.zsk.document.domain.DocNote>()
                        .eq(com.zsk.document.domain.DocNote::getDeleted, 0)
                        .between(com.zsk.document.domain.DocNote::getCreateTime, lastWeekStart, lastWeekEnd)
        );

        // 上周新增视频数
        Long lastWeekVideoCount = videoService.count(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.zsk.document.domain.DocVideo>()
                        .eq(com.zsk.document.domain.DocVideo::getDeleted, 0)
                        .between(com.zsk.document.domain.DocVideo::getCreateTime, lastWeekStart, lastWeekEnd)
        );

        // 上周新增评论数
        Long lastWeekNoteCommentCount = commentService.count(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.zsk.document.domain.DocNoteComment>()
                        .eq(com.zsk.document.domain.DocNoteComment::getDeleted, 0)
                        .between(com.zsk.document.domain.DocNoteComment::getCreateTime, lastWeekStart, lastWeekEnd)
        );
        Long lastWeekVideoCommentCount = videoCommentService.count(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.zsk.document.domain.DocVideoComment>()
                        .eq(com.zsk.document.domain.DocVideoComment::getDeleted, 0)
                        .between(com.zsk.document.domain.DocVideoComment::getCreateTime, lastWeekStart, lastWeekEnd)
        );
        Long lastWeekTotalCommentCount = (lastWeekNoteCommentCount != null ? lastWeekNoteCommentCount : 0)
                + (lastWeekVideoCommentCount != null ? lastWeekVideoCommentCount : 0);

        AllStatsVo statsVo = AllStatsVo.builder()
                .docCount(articleCount != null ? articleCount : 0)
                .videoCount(videoCount != null ? videoCount : 0)
                .commentCount(totalCommentCount)
                .lastWeekDocCount(lastWeekArticleCount != null ? lastWeekArticleCount : 0)
                .lastWeekVideoCount(lastWeekVideoCount != null ? lastWeekVideoCount : 0)
                .lastWeekCommentCount(lastWeekTotalCommentCount)
                .build();

        return R.ok(statsVo);
    }

    /**
     * 获取当前用户ID
     * 尝试从安全工具类获取当前登录用户ID，如果获取失败则返回null
     *
     * @return 当前用户ID，未登录或获取失败返回null
     */
    private Long getCurrentUserId() {
        try {
            return SecurityUtils.getUserId();
        } catch (Exception e) {
            return null;
        }
    }
}
