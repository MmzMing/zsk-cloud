package com.zsk.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.document.domain.DocVideo;
import com.zsk.document.domain.DocVideoComment;
import com.zsk.document.domain.vo.*;
import com.zsk.document.enums.CacheDocCollectTypeEnum;
import com.zsk.document.enums.CacheDocFollowTypeEnum;
import com.zsk.document.enums.CacheDocLikeTypeEnum;
import com.zsk.document.enums.CacheDocViewTypeEnum;
import com.zsk.document.mapper.DocVideoCommentMapper;
import com.zsk.document.mapper.DocVideoMapper;
import com.zsk.document.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 视频首页详情服务实现类
 * <p>
 * 实现视频详情查询、交互操作（点赞、收藏、关注）、评论管理等功能。
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocVideoHomeDetailServiceImpl implements com.zsk.document.service.IDocVideoHomeDetailService {

    private final DocVideoMapper videoMapper;
    private final DocVideoCommentMapper commentMapper;
    private final ICacheDocViewService cacheDocViewService;
    private final ICacheDocLikeService cacheDocLikeService;
    private final ICacheDocCollectService cacheDocCollectService;
    private final ICacheDocFollowService cacheDocFollowService;
    private final IVideoCategoryCacheService videoCategoryCacheService;

    @Override
    public DocVideoHomeDetailVo getVideoDetail(Long id, Long userId) {
        log.info("获取视频详情, id={}, userId={}", id, userId);
        DocVideo video = videoMapper.selectById(id);
        if (video == null) {
            log.warn("视频不存在, id={}", id);
            return null;
        }

        DocVideoHomeDetailVo vo = new DocVideoHomeDetailVo();
        vo.setId(video.getId());
        vo.setTitle(video.getVideoTitle());
        vo.setDescription(video.getFileContent());

        DocVideoHomeDetailAuthorVo author = buildVideoAuthorInfo(video.getUserId(), userId);
        vo.setAuthor(author);

        DocVideoHomeDetailStatsInfoVo stats = getVideoInteraction(id, userId);
        vo.setStats(stats);

        if (video.getTags() != null && !video.getTags().isEmpty()) {
            vo.setTags(List.of(video.getTags().split(",")));
        }

        return vo;
    }

    @Override
    public DocVideoHomeDetailStatsInfoVo getVideoInteraction(Long id, Long userId) {
        log.info("获取视频交互详情, id={}, userId={}", id, userId);
        DocVideoHomeDetailStatsInfoVo stats = new DocVideoHomeDetailStatsInfoVo();

        Long viewCount = cacheDocViewService.getViewCount(CacheDocViewTypeEnum.VIDEO.getCode(), id);
        stats.setViews(viewCount.intValue());

        Long likeCount = cacheDocLikeService.getLikeCount(CacheDocLikeTypeEnum.VIDEO.getCode(), id);
        stats.setLikes(likeCount.intValue());

        Long collectCount = cacheDocCollectService.getCollectCount(CacheDocCollectTypeEnum.VIDEO.getCode(), id);
        stats.setFavorites(collectCount.intValue());

        Long commentCount = commentMapper.selectCount(
                Wrappers.<DocVideoComment>lambdaQuery()
                        .eq(DocVideoComment::getDeleted, 0)
                        .eq(DocVideoComment::getVideoId, id)
        );
        stats.setComments(commentCount.intValue());

        if (userId != null) {
            stats.setIsLiked(cacheDocLikeService.hasLiked(CacheDocLikeTypeEnum.VIDEO.getCode(), id, userId));
            stats.setIsFavorited(cacheDocCollectService.hasCollected(CacheDocCollectTypeEnum.VIDEO.getCode(), id, userId));
        } else {
            stats.setIsLiked(false);
            stats.setIsFavorited(false);
        }

        return stats;
    }

    @Override
    public InteractionResultVo toggleVideoLike(Long id, Long userId) {
        log.info("切换视频点赞状态, id={}, userId={}", id, userId);

        boolean currentStatus = cacheDocLikeService.hasLiked(CacheDocLikeTypeEnum.VIDEO.getCode(), id, userId);
        if (currentStatus) {
            cacheDocLikeService.unlike(CacheDocLikeTypeEnum.VIDEO.getCode(), id, userId);
            log.info("取消视频点赞, id={}, userId={}", id, userId);
            return InteractionResultVo.builder()
                    .success(true)
                    .status(false)
                    .count(cacheDocLikeService.getLikeCount(CacheDocLikeTypeEnum.VIDEO.getCode(), id))
                    .build();
        } else {
            cacheDocLikeService.like(CacheDocLikeTypeEnum.VIDEO.getCode(), id, userId);
            log.info("点赞视频, id={}, userId={}", id, userId);
            return InteractionResultVo.builder()
                    .success(true)
                    .status(true)
                    .count(cacheDocLikeService.getLikeCount(CacheDocLikeTypeEnum.VIDEO.getCode(), id))
                    .build();
        }
    }

    @Override
    public InteractionResultVo toggleVideoFavorite(Long id, Long userId) {
        log.info("切换视频收藏状态, id={}, userId={}", id, userId);

        boolean currentStatus = cacheDocCollectService.hasCollected(CacheDocCollectTypeEnum.VIDEO.getCode(), id, userId);
        if (currentStatus) {
            cacheDocCollectService.uncollect(CacheDocCollectTypeEnum.VIDEO.getCode(), id, userId);
            log.info("取消视频收藏, id={}, userId={}", id, userId);
            return InteractionResultVo.builder()
                    .success(true)
                    .status(false)
                    .count(cacheDocCollectService.getCollectCount(CacheDocCollectTypeEnum.VIDEO.getCode(), id))
                    .build();
        } else {
            cacheDocCollectService.collect(CacheDocCollectTypeEnum.VIDEO.getCode(), id, userId);
            log.info("收藏视频, id={}, userId={}", id, userId);
            return InteractionResultVo.builder()
                    .success(true)
                    .status(true)
                    .count(cacheDocCollectService.getCollectCount(CacheDocCollectTypeEnum.VIDEO.getCode(), id))
                    .build();
        }
    }

    @Override
    public InteractionResultVo toggleFollowAuthor(Long authorId, Long userId) {
        log.info("切换关注作者状态, authorId={}, userId={}", authorId, userId);

        boolean currentStatus = cacheDocFollowService.hasFollowed(CacheDocFollowTypeEnum.USER.getCode(), authorId, userId);
        if (currentStatus) {
            cacheDocFollowService.unfollow(CacheDocFollowTypeEnum.USER.getCode(), authorId, userId);
            log.info("取消关注作者, authorId={}, userId={}", authorId, userId);
            return InteractionResultVo.builder()
                    .success(true)
                    .status(false)
                    .build();
        } else {
            cacheDocFollowService.follow(CacheDocFollowTypeEnum.USER.getCode(), authorId, userId);
            log.info("关注作者, authorId={}, userId={}", authorId, userId);
            return InteractionResultVo.builder()
                    .success(true)
                    .status(true)
                    .build();
        }
    }

    @Override
    public PageResult<DocVideoCommentVo> getVideoComments(Long id, PageQuery pageQuery, String sort, Long userId) {
        log.info("获取视频评论列表, id={}, pageQuery={}, sort={}, userId={}", id, pageQuery, sort, userId);

        Page<DocVideoComment> page = pageQuery.build();
        LambdaQueryWrapper<DocVideoComment> wrapper = Wrappers.<DocVideoComment>lambdaQuery()
                .eq(DocVideoComment::getDeleted, 0)
                .eq(DocVideoComment::getVideoId, id)
                .isNull(DocVideoComment::getParentCommentId);

        if ("hot".equals(sort)) {
            wrapper.orderByDesc(DocVideoComment::getLikeCount);
        } else {
            wrapper.orderByDesc(DocVideoComment::getCreateTime);
        }

        Page<DocVideoComment> resultPage = commentMapper.selectPage(page, wrapper);
        List<DocVideoCommentVo> voList = buildCommentVoList(resultPage.getRecords(), userId);

        return PageResult.of(voList, resultPage.getTotal(), resultPage.getCurrent(), resultPage.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DocVideoCommentVo postComment(Long videoId, String content, Long parentId, Long userId) {
        log.info("发表视频评论, videoId={}, content={}, parentId={}, userId={}", videoId, content, parentId, userId);

        DocVideoComment comment = new DocVideoComment();
        comment.setVideoId(videoId);
        comment.setCommentUserId(userId);
        comment.setCommentContent(content);
        comment.setLikeCount(0L);

        if (parentId != null) {
            comment.setParentCommentId(parentId);
        }

        commentMapper.insert(comment);
        log.info("评论发表成功, id={}", comment.getId());

        return buildCommentVo(comment, userId);
    }

    @Override
    public InteractionResultVo toggleCommentLike(Long commentId, Long userId) {
        log.info("切换评论点赞状态, commentId={}, userId={}", commentId, userId);

        boolean currentStatus = cacheDocLikeService.hasLiked(CacheDocLikeTypeEnum.VIDEO_COMMENT.getCode(), commentId, userId);
        if (currentStatus) {
            cacheDocLikeService.unlike(CacheDocLikeTypeEnum.VIDEO_COMMENT.getCode(), commentId, userId);
            commentMapper.update(Wrappers.<DocVideoComment>lambdaUpdate()
                    .eq(DocVideoComment::getId, commentId)
                    .setSql("like_count = like_count - 1"));
            log.info("取消评论点赞, commentId={}, userId={}", commentId, userId);
            return InteractionResultVo.builder()
                    .success(true)
                    .status(false)
                    .build();
        } else {
            cacheDocLikeService.like(CacheDocLikeTypeEnum.VIDEO_COMMENT.getCode(), commentId, userId);
            commentMapper.update(Wrappers.<DocVideoComment>lambdaUpdate()
                    .eq(DocVideoComment::getId, commentId)
                    .setSql("like_count = like_count + 1"));
            log.info("点赞评论, commentId={}, userId={}", commentId, userId);
            return InteractionResultVo.builder()
                    .success(true)
                    .status(true)
                    .build();
        }
    }

    private DocVideoHomeDetailAuthorVo buildVideoAuthorInfo(Long authorId, Long userId) {
        DocVideoHomeDetailAuthorVo author = new DocVideoHomeDetailAuthorVo();
        author.setId(authorId);
        author.setName("作者" + authorId);
        author.setAvatar("");

        Long fansCount = cacheDocFollowService.getFollowCount(CacheDocFollowTypeEnum.USER.getCode(), authorId);
        author.setFans(String.valueOf(fansCount));

        boolean isFollowing = userId != null && cacheDocFollowService.hasFollowed(CacheDocFollowTypeEnum.USER.getCode(), authorId, userId);
        author.setIsFollowing(isFollowing);

        return author;
    }

    private List<DocVideoCommentVo> buildCommentVoList(List<DocVideoComment> comments, Long userId) {
        if (comments == null || comments.isEmpty()) {
            return List.of();
        }

        List<Long> commentIds = comments.stream().map(DocVideoComment::getId).collect(Collectors.toList());
        List<DocVideoComment> allReplies = commentMapper.selectList(
                Wrappers.<DocVideoComment>lambdaQuery()
                        .in(DocVideoComment::getParentCommentId, commentIds)
                        .eq(DocVideoComment::getDeleted, 0)
                        .orderByDesc(DocVideoComment::getCreateTime)
        );

        Map<Long, List<DocVideoComment>> replyMap = new HashMap<>();
        for (DocVideoComment reply : allReplies) {
            replyMap.computeIfAbsent(reply.getParentCommentId(), k -> new java.util.ArrayList<>()).add(reply);
        }

        return comments.stream()
                .map(comment -> buildCommentVoWithReplies(comment, replyMap.getOrDefault(comment.getId(), List.of()), userId))
                .collect(Collectors.toList());
    }

    private DocVideoCommentVo buildCommentVoWithReplies(DocVideoComment comment, List<DocVideoComment> replies, Long userId) {
        DocVideoCommentVo vo = buildCommentVo(comment, userId);

        List<DocVideoCommentVo> replyVos = replies.stream()
                .map(reply -> buildReplyCommentVo(reply, userId))
                .collect(Collectors.toList());
        vo.setReplies(replyVos);

        return vo;
    }

    private DocVideoCommentVo buildCommentVo(DocVideoComment comment, Long userId) {
        DocVideoCommentVo vo = new DocVideoCommentVo();
        vo.setId(comment.getId());
        vo.setContent(comment.getCommentContent());
        vo.setCreatedAt(comment.getCreateTime() != null ? comment.getCreateTime().toString() : null);
        vo.setLikes(comment.getLikeCount() != null ? comment.getLikeCount().intValue() : 0);

        boolean isLiked = userId != null && cacheDocLikeService.hasLiked(CacheDocLikeTypeEnum.VIDEO_COMMENT.getCode(), comment.getId(), userId);
        vo.setIsLiked(isLiked);

        DocVideoCommentVo.AuthorInfo author = new DocVideoCommentVo.AuthorInfo();
        author.setId(comment.getCommentUserId());
        author.setName("用户" + comment.getCommentUserId());
        author.setAvatar("");
        vo.setAuthor(author);

        return vo;
    }

    private DocVideoCommentVo buildReplyCommentVo(DocVideoComment reply, Long userId) {
        DocVideoCommentVo vo = buildCommentVo(reply, userId);

        if (reply.getParentCommentId() != null) {
            DocVideoComment parentComment = commentMapper.selectById(reply.getParentCommentId());
            if (parentComment != null) {
                DocVideoCommentVo.ReplyToInfo replyTo = new DocVideoCommentVo.ReplyToInfo();
                replyTo.setId(parentComment.getCommentUserId());
                replyTo.setName("用户" + parentComment.getCommentUserId());
                vo.setReplyTo(replyTo);
            }
        }

        return vo;
    }
}