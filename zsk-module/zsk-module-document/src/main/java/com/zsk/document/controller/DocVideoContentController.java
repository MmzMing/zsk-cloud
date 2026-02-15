package com.zsk.document.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zsk.common.core.domain.R;
import com.zsk.common.security.utils.SecurityUtils;
import com.zsk.document.domain.DocVideoComment;
import com.zsk.document.domain.DocVideoDetail;
import com.zsk.document.domain.vo.DocVideoCommentVo;
import com.zsk.document.domain.vo.DocVideoDetailVo;
import com.zsk.document.enums.CacheDocCollectTypeEnum;
import com.zsk.document.enums.CacheDocFollowTypeEnum;
import com.zsk.document.enums.CacheDocLikeTypeEnum;
import com.zsk.document.service.ICacheDocCollectService;
import com.zsk.document.service.ICacheDocFollowService;
import com.zsk.document.service.ICacheDocLikeService;
import com.zsk.document.service.IDocVideoCommentService;
import com.zsk.document.service.IDocVideoDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 前台视频详情 控制器
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Tag(name = "前台视频详情")
@RestController
@RequestMapping("/content/video")
@RequiredArgsConstructor
public class DocVideoContentController {

    private final IDocVideoDetailService videoService;
    private final IDocVideoCommentService commentService;
    private final ICacheDocLikeService cacheDocLikeService;
    private final ICacheDocCollectService cacheDocCollectService;
    private final ICacheDocFollowService cacheDocFollowService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取视频详情
     *
     * @param id 视频ID
     * @return 视频详情
     */
    @Operation(summary = "获取视频详情")
    @GetMapping("/detail/{id}")
    public R<DocVideoDetailVo> getDetail(@PathVariable("id") Long id) {
        DocVideoDetail video = videoService.getById(id);
        if (video == null) {
            return R.fail("视频不存在");
        }

        video.setViewCount(video.getViewCount() != null ? video.getViewCount() + 1 : 1L);
        videoService.updateById(video);

        DocVideoDetailVo vo = buildVideoDetailVo(video);

        Long userId = getCurrentUserId();

        if (userId != null) {
            boolean isLiked = cacheDocLikeService.hasLiked(CacheDocLikeTypeEnum.VIDEO.getCode(), id, userId);
            vo.getStats().setIsLiked(isLiked);

            boolean isFavorited = cacheDocCollectService.hasCollected(CacheDocCollectTypeEnum.VIDEO.getCode(), id, userId);
            vo.getStats().setIsFavorited(isFavorited);

            if (vo.getAuthor() != null && vo.getAuthor().getId() != null) {
                boolean isFollowing = cacheDocFollowService.hasFollowed(
                    CacheDocFollowTypeEnum.VIDEO_AUTHOR.getCode(), Long.parseLong(vo.getAuthor().getId()), userId);
                vo.getAuthor().setIsFollowing(isFollowing);
            }
        } else {
            vo.getStats().setIsLiked(false);
            vo.getStats().setIsFavorited(false);
            if (vo.getAuthor() != null) {
                vo.getAuthor().setIsFollowing(false);
            }
        }

        Long likeCount = cacheDocLikeService.getLikeCount(CacheDocLikeTypeEnum.VIDEO.getCode(), id);
        vo.getStats().setLikes(likeCount.intValue());

        Long collectCount = cacheDocCollectService.getCollectCount(CacheDocCollectTypeEnum.VIDEO.getCode(), id);
        vo.getStats().setFavorites(collectCount.intValue());

        List<DocVideoDetail> recommendVideos = videoService.list(
            new LambdaQueryWrapper<DocVideoDetail>()
                .eq(DocVideoDetail::getDeleted, 0)
                .eq(DocVideoDetail::getStatus, 1)
                .eq(DocVideoDetail::getIsRecommended, 1)
                .ne(DocVideoDetail::getId, id)
                .orderByDesc(DocVideoDetail::getViewCount)
                .last("LIMIT 5")
        );
        List<DocVideoDetailVo.RecommendVideo> recommendations = new ArrayList<>();
        for (DocVideoDetail recommend : recommendVideos) {
            DocVideoDetailVo.RecommendVideo rec = new DocVideoDetailVo.RecommendVideo();
            rec.setId(String.valueOf(recommend.getId()));
            rec.setTitle(recommend.getVideoTitle());
            rec.setCoverUrl(recommend.getCoverUrl());
            rec.setViews(String.valueOf(recommend.getViewCount() != null ? recommend.getViewCount() : 0));
            rec.setDescription(recommend.getFileContent());
            rec.setDate(recommend.getCreateTime() != null ? recommend.getCreateTime().format(DATE_FORMATTER) : "");
            recommendations.add(rec);
        }
        vo.setRecommendations(recommendations);

        return R.ok(vo);
    }

    /**
     * 切换视频点赞状态
     *
     * @param id 视频ID
     * @return 点赞结果
     */
    @Operation(summary = "切换视频点赞状态")
    @PostMapping("/like/{id}")
    public R<Map<String, Object>> toggleLike(@PathVariable("id") Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }

        boolean isLiked = cacheDocLikeService.hasLiked(CacheDocLikeTypeEnum.VIDEO.getCode(), id, userId);
        boolean result;
        if (isLiked) {
            result = cacheDocLikeService.unlike(CacheDocLikeTypeEnum.VIDEO.getCode(), id, userId);
        } else {
            result = cacheDocLikeService.like(CacheDocLikeTypeEnum.VIDEO.getCode(), id, userId);
        }

        Long likeCount = cacheDocLikeService.getLikeCount(CacheDocLikeTypeEnum.VIDEO.getCode(), id);

        DocVideoDetail video = videoService.getById(id);
        if (video != null) {
            video.setLikeCount(likeCount);
            videoService.updateById(video);
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("isLiked", result && !isLiked);
        resultMap.put("count", likeCount);
        return R.ok(resultMap);
    }

    /**
     * 切换视频收藏状态
     *
     * @param id 视频ID
     * @return 收藏结果
     */
    @Operation(summary = "切换视频收藏状态")
    @PostMapping("/favorite/{id}")
    public R<Map<String, Object>> toggleFavorite(@PathVariable("id") Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }

        boolean isFavorited = cacheDocCollectService.hasCollected(CacheDocCollectTypeEnum.VIDEO.getCode(), id, userId);
        boolean result;
        if (isFavorited) {
            result = cacheDocCollectService.uncollect(CacheDocCollectTypeEnum.VIDEO.getCode(), id, userId);
        } else {
            result = cacheDocCollectService.collect(CacheDocCollectTypeEnum.VIDEO.getCode(), id, userId);
        }

        Long collectCount = cacheDocCollectService.getCollectCount(CacheDocCollectTypeEnum.VIDEO.getCode(), id);

        DocVideoDetail video = videoService.getById(id);
        if (video != null) {
            video.setCollectCount(collectCount);
            videoService.updateById(video);
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("isFavorited", result && !isFavorited);
        resultMap.put("count", collectCount);
        return R.ok(resultMap);
    }

    /**
     * 获取视频评论列表
     *
     * @param id 视频ID
     * @param page 页码
     * @param pageSize 每页数量
     * @param sort 排序方式（hot/new）
     * @return 评论列表
     */
    @Operation(summary = "获取视频评论列表")
    @GetMapping("/comments/{id}")
    public R<Map<String, Object>> getComments(
        @PathVariable("id") String id,
        @RequestParam(value = "page", defaultValue = "1") Integer page,
        @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
        @RequestParam(value = "sort", required = false) String sort) {

        LambdaQueryWrapper<DocVideoComment> wrapper = new LambdaQueryWrapper<DocVideoComment>()
            .eq(DocVideoComment::getDeleted, 0)
            .eq(DocVideoComment::getVideoId, id)
            .isNull(DocVideoComment::getParentCommentId);

        if ("hot".equals(sort)) {
            wrapper.orderByDesc(DocVideoComment::getLikeCount);
        } else {
            wrapper.orderByDesc(DocVideoComment::getCreateTime);
        }

        List<DocVideoComment> comments = commentService.list(wrapper);

        Long userId = getCurrentUserId();
        List<DocVideoCommentVo> commentVos = new ArrayList<>();
        for (DocVideoComment comment : comments) {
            DocVideoCommentVo vo = buildCommentVo(comment, userId);
            List<DocVideoComment> replies = commentService.list(
                new LambdaQueryWrapper<DocVideoComment>()
                    .eq(DocVideoComment::getDeleted, 0)
                    .eq(DocVideoComment::getParentCommentId, comment.getId())
                    .orderByAsc(DocVideoComment::getCreateTime)
            );
            List<DocVideoCommentVo> replyVos = new ArrayList<>();
            for (DocVideoComment reply : replies) {
                replyVos.add(buildCommentVo(reply, userId));
            }
            vo.setReplies(replyVos);
            commentVos.add(vo);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", commentVos);
        result.put("total", comments.size());
        return R.ok(result);
    }

    /**
     * 发表视频评论
     *
     * @param params 评论参数
     * @return 评论结果
     */
    @Operation(summary = "发表视频评论")
    @PostMapping("/comment")
    public R<DocVideoCommentVo> postComment(@RequestBody Map<String, Object> params) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }

        String videoId = (String) params.get("videoId");
        String content = (String) params.get("content");
        String parentId = (String) params.get("parentId");

        DocVideoComment comment = new DocVideoComment();
        comment.setVideoId(videoId);
        comment.setCommentUserId(String.valueOf(userId));
        comment.setCommentContent(content);
        comment.setLikeCount(0L);
        if (parentId != null && !parentId.isEmpty()) {
            comment.setParentCommentId(Long.parseLong(parentId));
        }
        commentService.save(comment);

        DocVideoDetail video = videoService.getById(Long.parseLong(videoId));
        if (video != null) {
            Long commentCount = commentService.count(
                new LambdaQueryWrapper<DocVideoComment>()
                    .eq(DocVideoComment::getDeleted, 0)
                    .eq(DocVideoComment::getVideoId, videoId)
            );
            video.setCommentCount(commentCount);
            videoService.updateById(video);
        }

        return R.ok(buildCommentVo(comment, userId));
    }

    /**
     * 切换评论点赞状态
     *
     * @param commentId 评论ID
     * @return 点赞结果
     */
    @Operation(summary = "切换评论点赞状态")
    @PostMapping("/comment/like/{commentId}")
    public R<Map<String, Object>> toggleCommentLike(@PathVariable("commentId") Long commentId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }

        boolean isLiked = cacheDocLikeService.hasLiked(CacheDocLikeTypeEnum.VIDEO_COMMENT.getCode(), commentId, userId);
        boolean result;
        if (isLiked) {
            result = cacheDocLikeService.unlike(CacheDocLikeTypeEnum.VIDEO_COMMENT.getCode(), commentId, userId);
        } else {
            result = cacheDocLikeService.like(CacheDocLikeTypeEnum.VIDEO_COMMENT.getCode(), commentId, userId);
        }

        Long likeCount = cacheDocLikeService.getLikeCount(CacheDocLikeTypeEnum.VIDEO_COMMENT.getCode(), commentId);

        DocVideoComment comment = commentService.getById(commentId);
        if (comment != null) {
            comment.setLikeCount(likeCount);
            commentService.updateById(comment);
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("isLiked", result && !isLiked);
        resultMap.put("likes", likeCount);
        return R.ok(resultMap);
    }

    /**
     * 构建视频详情VO
     */
    private DocVideoDetailVo buildVideoDetailVo(DocVideoDetail video) {
        DocVideoDetailVo vo = new DocVideoDetailVo();
        vo.setId(String.valueOf(video.getId()));
        vo.setTitle(video.getVideoTitle());
        vo.setDescription(video.getFileContent());
        vo.setVideoUrl(video.getVideoUrl());
        vo.setCoverUrl(video.getCoverUrl());

        if (video.getTags() != null && !video.getTags().isEmpty()) {
            vo.setTags(Arrays.asList(video.getTags().split(",")));
        } else {
            vo.setTags(new ArrayList<>());
        }

        DocVideoDetailVo.AuthorInfo author = new DocVideoDetailVo.AuthorInfo();
        author.setId(String.valueOf(video.getUserId()));
        author.setName("作者" + video.getUserId());
        author.setAvatar("");
        author.setFans("0");
        author.setIsFollowing(false);
        vo.setAuthor(author);

        DocVideoDetailVo.StatsInfo stats = new DocVideoDetailVo.StatsInfo();
        stats.setViews(String.valueOf(video.getViewCount() != null ? video.getViewCount() : 0));
        stats.setLikes(video.getLikeCount() != null ? video.getLikeCount().intValue() : 0);
        stats.setFavorites(video.getCollectCount() != null ? video.getCollectCount().intValue() : 0);
        stats.setDate(video.getCreateTime() != null ? video.getCreateTime().format(DATE_FORMATTER) : "");
        stats.setIsLiked(false);
        stats.setIsFavorited(false);
        vo.setStats(stats);

        vo.setEpisodes(new ArrayList<>());

        return vo;
    }

    /**
     * 构建评论VO
     */
    private DocVideoCommentVo buildCommentVo(DocVideoComment comment, Long currentUserId) {
        DocVideoCommentVo vo = new DocVideoCommentVo();
        vo.setId(String.valueOf(comment.getId()));
        vo.setContent(comment.getCommentContent());
        vo.setCreatedAt(comment.getCreateTime() != null ? comment.getCreateTime().format(DATE_FORMATTER) : "");

        Long likeCount = cacheDocLikeService.getLikeCount(CacheDocLikeTypeEnum.VIDEO_COMMENT.getCode(), comment.getId());
        vo.setLikes(likeCount.intValue());

        DocVideoCommentVo.AuthorInfo author = new DocVideoCommentVo.AuthorInfo();
        author.setId(comment.getCommentUserId());
        author.setName("用户" + comment.getCommentUserId());
        author.setAvatar("");
        vo.setAuthor(author);

        if (currentUserId != null) {
            vo.setIsLiked(cacheDocLikeService.hasLiked(
                CacheDocLikeTypeEnum.VIDEO_COMMENT.getCode(), comment.getId(), currentUserId));
        } else {
            vo.setIsLiked(false);
        }

        return vo;
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        try {
            return SecurityUtils.getUserId();
        } catch (Exception e) {
            return null;
        }
    }
}
