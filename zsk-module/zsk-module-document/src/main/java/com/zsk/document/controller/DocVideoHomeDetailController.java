package com.zsk.document.controller;

import com.zsk.common.core.domain.R;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.common.security.utils.SecurityUtils;
import com.zsk.document.domain.dto.VideoCommentRequestDTO;
import com.zsk.document.domain.vo.*;
import com.zsk.document.enums.CacheDocViewTypeEnum;
import com.zsk.document.service.ICacheDocViewService;
import com.zsk.document.service.IDocVideoHomeDetailService;
import com.zsk.document.service.IDocVideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 前台视频首页详情控制器
 * <p>
 * 提供视频详情查询、交互操作（点赞、收藏、关注）、评论管理等功能。
 * 所有业务逻辑已下沉到 Service 层，本层仅负责参数校验和结果封装。
 * </p>
 *
 * @author wuhuaming
 * @version 2.0
 * @date 2026-04-25
 */
@Tag(name = "前台视频首页详情")
@RestController
@RequestMapping("/docVideoHomeDetail")
@RequiredArgsConstructor
public class DocVideoHomeDetailController {

    private final IDocVideoService videoService;
    private final IDocVideoHomeDetailService videoHomeDetailService;
    private final ICacheDocViewService cacheDocViewService;

    /**
     * 获取视频详情
     * <p>
     * 根据视频ID查询视频详情，并增加浏览量。
     * 如果用户已登录，会查询用户的点赞、收藏状态以及是否关注作者。
     * </p>
     *
     * @param id 视频ID
     * @return 视频详情
     */
    @Operation(summary = "获取视频详情")
    @GetMapping("/detail/{id}")
    public R<DocVideoHomeDetailVo> getDetail(@PathVariable("id") Long id) {
        DocVideoListVo video = videoService.getByIdWithFileUrl(id);
        if (video == null) {
            return R.fail("视频不存在");
        }

        Long userId = getCurrentUserId();
        cacheDocViewService.view(CacheDocViewTypeEnum.VIDEO.getCode(), id, userId);

        DocVideoHomeDetailVo vo = new DocVideoHomeDetailVo();
        vo.setId(video.getId());
        vo.setTitle(video.getVideoTitle());
        vo.setDescription(video.getFileContent());

        if (video.getVideoFile() != null) {
            if (video.getVideoFile().getVideo() != null) {
                vo.setVideoUrl(video.getVideoFile().getVideo().getFileUrl());
            }
            if (video.getVideoFile().getThumbnail() != null) {
                vo.setCoverUrl(video.getVideoFile().getThumbnail().getFileUrl());
            }
        }

        DocVideoHomeDetailVo detail = videoHomeDetailService.getVideoDetail(id, userId);
        vo.setAuthor(detail.getAuthor());
        vo.setStats(detail.getStats());
        vo.setTags(detail.getTags());

        return R.ok(vo);
    }

    /**
     * 获取视频交互详情
     * <p>
     * 独立查询视频的交互统计数据，包括浏览量、点赞数、收藏数、评论数以及当前用户的交互状态。
     * </p>
     *
     * @param id 视频ID
     * @return 视频交互详情
     */
    @Operation(summary = "获取视频交互详情")
    @GetMapping("/interaction/{id}")
    public R<DocVideoHomeDetailStatsInfoVo> getInteraction(@PathVariable("id") Long id) {
        if (videoService.getById(id) == null) {
            return R.fail("视频不存在");
        }

        Long userId = getCurrentUserId();
        DocVideoHomeDetailStatsInfoVo stats = videoHomeDetailService.getVideoInteraction(id, userId);
        return R.ok(stats);
    }

    /**
     * 切换视频点赞状态
     * <p>
     * 用户点赞或取消点赞视频。
     * </p>
     *
     * @param id 视频ID
     * @return 点赞操作结果
     */
    @Operation(summary = "切换视频点赞状态")
    @PostMapping("/like/{id}")
    public R<InteractionResultVo> toggleLike(@PathVariable("id") Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }

        InteractionResultVo result = videoHomeDetailService.toggleVideoLike(id, userId);
        return R.ok(result);
    }

    /**
     * 切换视频收藏状态
     * <p>
     * 用户收藏或取消收藏视频。
     * </p>
     *
     * @param id 视频ID
     * @return 收藏操作结果
     */
    @Operation(summary = "切换视频收藏状态")
    @PostMapping("/favorite/{id}")
    public R<InteractionResultVo> toggleFavorite(@PathVariable("id") Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }

        InteractionResultVo result = videoHomeDetailService.toggleVideoFavorite(id, userId);
        return R.ok(result);
    }

    /**
     * 切换关注作者状态
     * <p>
     * 用户关注或取消关注视频作者。
     * </p>
     *
     * @param authorId 作者ID
     * @return 关注操作结果
     */
    @Operation(summary = "切换关注作者状态")
    @PostMapping("/follow/{authorId}")
    public R<InteractionResultVo> toggleFollow(@PathVariable("authorId") Long authorId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }

        if (authorId.equals(userId)) {
            return R.fail("不能关注自己");
        }

        InteractionResultVo result = videoHomeDetailService.toggleFollowAuthor(authorId, userId);
        return R.ok(result);
    }

    /**
     * 获取视频评论列表
     * <p>
     * 查询视频的评论列表，支持热门排序和最新排序。
     * </p>
     *
     * @param id        视频ID
     * @param pageQuery 分页查询参数
     * @param sort      排序方式（hot/new）
     * @return 评论分页列表
     */
    @Operation(summary = "获取视频评论列表")
    @GetMapping("/comments/{id}")
    public R<PageResult<DocVideoCommentVo>> getComments(
            @PathVariable("id") Long id,
            PageQuery pageQuery,
            @RequestParam(value = "sort", required = false) String sort) {

        Long userId = getCurrentUserId();
        PageResult<DocVideoCommentVo> result = videoHomeDetailService.getVideoComments(id, pageQuery, sort, userId);
        return R.ok(result);
    }

    /**
     * 发表视频评论
     * <p>
     * 用户发表视频评论，支持回复其他评论。
     * </p>
     *
     * @param commentRequest 评论请求参数
     * @return 评论结果
     */
    @Operation(summary = "发表视频评论")
    @PostMapping("/comment")
    public R<DocVideoCommentVo> postComment(@RequestBody VideoCommentRequestDTO commentRequest) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }

        if (commentRequest.getVideoId() == null) {
            return R.fail("视频ID不能为空");
        }
        if (commentRequest.getContent() == null || commentRequest.getContent().isEmpty()) {
            return R.fail("评论内容不能为空");
        }

        DocVideoCommentVo result = videoHomeDetailService.postComment(
                commentRequest.getVideoId(),
                commentRequest.getContent(),
                commentRequest.getParentId(),
                userId
        );
        return R.ok(result);
    }

    /**
     * 切换评论点赞状态
     * <p>
     * 用户点赞或取消点赞视频评论。
     * </p>
     *
     * @param commentId 评论ID
     * @return 点赞操作结果
     */
    @Operation(summary = "切换评论点赞状态")
    @PostMapping("/comment/like/{commentId}")
    public R<InteractionResultVo> toggleCommentLike(@PathVariable("commentId") Long commentId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }

        InteractionResultVo result = videoHomeDetailService.toggleCommentLike(commentId, userId);
        return R.ok(result);
    }

    private Long getCurrentUserId() {
        try {
            return SecurityUtils.getUserId();
        } catch (Exception e) {
            return null;
        }
    }
}