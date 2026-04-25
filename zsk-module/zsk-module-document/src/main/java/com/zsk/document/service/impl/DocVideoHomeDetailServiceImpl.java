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
 * 所有交互数据（浏览量、点赞数、收藏数）均通过 Redis 缓存服务获取，
 * 确保数据的实时性和高性能。
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

    /**
     * 视频数据访问对象，用于视频基础信息的数据库操作
     */
    private final DocVideoMapper videoMapper;

    /**
     * 视频评论数据访问对象，用于评论的增删改查操作
     */
    private final DocVideoCommentMapper commentMapper;

    /**
     * 文档浏览缓存服务，用于统计和查询视频浏览量
     */
    private final ICacheDocViewService cacheDocViewService;

    /**
     * 文档点赞缓存服务，用于管理视频和评论的点赞状态
     */
    private final ICacheDocLikeService cacheDocLikeService;

    /**
     * 文档收藏缓存服务，用于管理视频的收藏状态
     */
    private final ICacheDocCollectService cacheDocCollectService;

    /**
     * 文档关注缓存服务，用于管理用户之间的关注关系
     */
    private final ICacheDocFollowService cacheDocFollowService;

    /**
     * 视频分类缓存服务，用于获取视频分类信息
     */
    private final IVideoCategoryCacheService videoCategoryCacheService;

    /**
     * 获取视频详情
     * <p>
     * 根据视频ID查询视频详情，包括视频基本信息、作者信息和统计数据。
     * 统计数据从 Redis 缓存获取，确保数据实时更新。
     * </p>
     *
     * @param id     视频ID
     * @param userId 当前用户ID（可为null，未登录时传null）
     * @return 视频详情VO，如果视频不存在返回null
     */
    @Override
    public DocVideoHomeDetailVo getVideoDetail(Long id, Long userId) {
        log.info("获取视频详情, id={}, userId={}", id, userId);

        // 1. 根据ID查询视频基础信息
        DocVideo video = videoMapper.selectById(id);
        if (video == null) {
            log.warn("视频不存在, id={}", id);
            return null;
        }

        // 2. 构建视频详情VO对象
        DocVideoHomeDetailVo vo = new DocVideoHomeDetailVo();
        vo.setId(video.getId());
        vo.setTitle(video.getVideoTitle());
        vo.setDescription(video.getFileContent());

        // 3. 构建作者信息（包含关注状态）
        DocUserVo author = buildVideoAuthorInfo(video.getUserId(), userId);
        vo.setAuthor(author);

        // 4. 获取并设置交互统计数据
        DocStatsInfoVo stats = getVideoInteraction(id, userId);
        vo.setStats(stats);

        // 5. 解析标签列表（逗号分隔的字符串转为列表）
        if (video.getTags() != null && !video.getTags().isEmpty()) {
            vo.setTags(List.of(video.getTags().split(",")));
        }

        log.info("获取视频详情成功, id={}", id);
        return vo;
    }

    /**
     * 获取视频交互详情
     * <p>
     * 独立查询视频的交互统计数据，包括浏览量、点赞数、收藏数以及当前用户的交互状态。
     * 所有数据均从 Redis 缓存获取，保证实时性和高性能。
     * </p>
     *
     * @param id     视频ID
     * @param userId 当前用户ID（可为null，未登录时传null）
     * @return 视频交互统计信息VO
     */
    @Override
    public DocStatsInfoVo getVideoInteraction(Long id, Long userId) {
        log.info("获取视频交互详情, id={}, userId={}", id, userId);

        // 1. 创建统计信息VO对象
        DocStatsInfoVo stats = new DocStatsInfoVo();

        // 2. 从缓存获取浏览量
        Long viewCount = cacheDocViewService.getViewCount(CacheDocViewTypeEnum.VIDEO.getCode(), id);
        stats.setViews(viewCount.intValue());

        // 3. 从缓存获取点赞数
        Long likeCount = cacheDocLikeService.getLikeCount(CacheDocLikeTypeEnum.VIDEO.getCode(), id);
        stats.setLikes(likeCount.intValue());

        // 4. 从缓存获取收藏数
        Long collectCount = cacheDocCollectService.getCollectCount(CacheDocCollectTypeEnum.VIDEO.getCode(), id);
        stats.setFavorites(collectCount.intValue());

        // 5. 查询当前用户的交互状态（仅当用户已登录时）
        if (userId != null) {
            stats.setIsLiked(cacheDocLikeService.hasLiked(CacheDocLikeTypeEnum.VIDEO.getCode(), id, userId));
            stats.setIsFavorited(cacheDocCollectService.hasCollected(CacheDocCollectTypeEnum.VIDEO.getCode(), id, userId));
        } else {
            // 用户未登录时，默认设置为未点赞、未收藏
            stats.setIsLiked(false);
            stats.setIsFavorited(false);
        }

        log.info("获取视频交互详情成功, id={}", id);
        return stats;
    }

    /**
     * 切换视频点赞状态
     * <p>
     * 用户点赞或取消点赞视频。先查询当前点赞状态，然后执行相反操作。
     * 点赞操作通过 Redis 缓存服务实现，保证高性能和数据一致性。
     * </p>
     *
     * @param id     视频ID
     * @param userId 当前用户ID
     * @return 点赞操作结果，包含操作是否成功、当前状态和最新点赞数
     */
    @Override
    public InteractionResultVo toggleVideoLike(Long id, Long userId) {
        log.info("切换视频点赞状态, id={}, userId={}", id, userId);

        // 1. 查询当前点赞状态
        boolean currentStatus = cacheDocLikeService.hasLiked(CacheDocLikeTypeEnum.VIDEO.getCode(), id, userId);

        if (currentStatus) {
            // 2. 当前已点赞，执行取消点赞操作
            cacheDocLikeService.unlike(CacheDocLikeTypeEnum.VIDEO.getCode(), id, userId);
            log.info("取消视频点赞成功, id={}, userId={}", id, userId);

            // 3. 构建取消点赞结果
            return InteractionResultVo.builder()
                    .success(true)
                    .status(false)
                    .count(cacheDocLikeService.getLikeCount(CacheDocLikeTypeEnum.VIDEO.getCode(), id))
                    .build();
        } else {
            // 2. 当前未点赞，执行点赞操作
            cacheDocLikeService.like(CacheDocLikeTypeEnum.VIDEO.getCode(), id, userId);
            log.info("点赞视频成功, id={}, userId={}", id, userId);

            // 3. 构建点赞结果
            return InteractionResultVo.builder()
                    .success(true)
                    .status(true)
                    .count(cacheDocLikeService.getLikeCount(CacheDocLikeTypeEnum.VIDEO.getCode(), id))
                    .build();
        }
    }

    /**
     * 切换视频收藏状态
     * <p>
     * 用户收藏或取消收藏视频。先查询当前收藏状态，然后执行相反操作。
     * 收藏操作通过 Redis 缓存服务实现，支持用户收藏列表的管理。
     * </p>
     *
     * @param id     视频ID
     * @param userId 当前用户ID
     * @return 收藏操作结果，包含操作是否成功、当前状态和最新收藏数
     */
    @Override
    public InteractionResultVo toggleVideoFavorite(Long id, Long userId) {
        log.info("切换视频收藏状态, id={}, userId={}", id, userId);

        // 1. 查询当前收藏状态
        boolean currentStatus = cacheDocCollectService.hasCollected(CacheDocCollectTypeEnum.VIDEO.getCode(), id, userId);

        if (currentStatus) {
            // 2. 当前已收藏，执行取消收藏操作
            cacheDocCollectService.uncollect(CacheDocCollectTypeEnum.VIDEO.getCode(), id, userId);
            log.info("取消视频收藏成功, id={}, userId={}", id, userId);

            // 3. 构建取消收藏结果
            return InteractionResultVo.builder()
                    .success(true)
                    .status(false)
                    .count(cacheDocCollectService.getCollectCount(CacheDocCollectTypeEnum.VIDEO.getCode(), id))
                    .build();
        } else {
            // 2. 当前未收藏，执行收藏操作
            cacheDocCollectService.collect(CacheDocCollectTypeEnum.VIDEO.getCode(), id, userId);
            log.info("收藏视频成功, id={}, userId={}", id, userId);

            // 3. 构建收藏结果
            return InteractionResultVo.builder()
                    .success(true)
                    .status(true)
                    .count(cacheDocCollectService.getCollectCount(CacheDocCollectTypeEnum.VIDEO.getCode(), id))
                    .build();
        }
    }

    /**
     * 切换关注作者状态
     * <p>
     * 用户关注或取消关注视频作者。先查询当前关注状态，然后执行相反操作。
     * 关注操作通过 Redis 缓存服务实现，支持粉丝数统计和关注列表管理。
     * </p>
     *
     * @param authorId 作者ID
     * @param userId   当前用户ID
     * @return 关注操作结果，包含操作是否成功和当前关注状态
     */
    @Override
    public InteractionResultVo toggleFollowAuthor(Long authorId, Long userId) {
        log.info("切换关注作者状态, authorId={}, userId={}", authorId, userId);

        // 1. 查询当前关注状态
        boolean currentStatus = cacheDocFollowService.hasFollowed(CacheDocFollowTypeEnum.USER.getCode(), authorId, userId);

        if (currentStatus) {
            // 2. 当前已关注，执行取消关注操作
            cacheDocFollowService.unfollow(CacheDocFollowTypeEnum.USER.getCode(), authorId, userId);
            log.info("取消关注作者成功, authorId={}, userId={}", authorId, userId);

            // 3. 构建取消关注结果
            return InteractionResultVo.builder()
                    .success(true)
                    .status(false)
                    .build();
        } else {
            // 2. 当前未关注，执行关注操作
            cacheDocFollowService.follow(CacheDocFollowTypeEnum.USER.getCode(), authorId, userId);
            log.info("关注作者成功, authorId={}, userId={}", authorId, userId);

            // 3. 构建关注结果
            return InteractionResultVo.builder()
                    .success(true)
                    .status(true)
                    .build();
        }
    }

    /**
     * 获取视频评论列表
     * <p>
     * 查询视频的评论列表，支持热门排序和最新排序。
     * 返回分页结果，包含评论内容、作者信息、点赞数和回复列表。
     * </p>
     *
     * @param id        视频ID
     * @param pageQuery 分页查询参数
     * @param sort      排序方式（hot-热门优先，其他值-最新优先）
     * @param userId    当前用户ID（可为null，未登录时传null）
     * @return 评论分页结果
     */
    @Override
    public PageResult<DocVideoCommentVo> getVideoComments(Long id, PageQuery pageQuery, String sort, Long userId) {
        log.info("获取视频评论列表, id={}, pageQuery={}, sort={}, userId={}", id, pageQuery, sort, userId);

        // 1. 构建分页对象
        Page<DocVideoComment> page = pageQuery.build();

        // 2. 构建查询条件：查询未删除的顶级评论（parentCommentId为null）
        LambdaQueryWrapper<DocVideoComment> wrapper = Wrappers.<DocVideoComment>lambdaQuery()
                .eq(DocVideoComment::getDeleted, 0)
                .eq(DocVideoComment::getVideoId, id)
                .isNull(DocVideoComment::getParentCommentId);

        // 3. 根据排序参数设置排序方式
        if ("hot".equals(sort)) {
            // 热门排序：按点赞数降序
            wrapper.orderByDesc(DocVideoComment::getLikeCount);
        } else {
            // 默认排序：按创建时间降序（最新优先）
            wrapper.orderByDesc(DocVideoComment::getCreateTime);
        }

        // 4. 执行分页查询
        Page<DocVideoComment> resultPage = commentMapper.selectPage(page, wrapper);

        // 5. 构建评论VO列表（包含回复信息）
        List<DocVideoCommentVo> voList = buildCommentVoList(resultPage.getRecords(), userId);

        // 6. 构建并返回分页结果
        return PageResult.of(voList, resultPage.getTotal(), resultPage.getCurrent(), resultPage.getSize());
    }

    /**
     * 发表视频评论
     * <p>
     * 用户发表视频评论，支持回复其他评论。评论成功后返回构建好的评论VO。
     * 使用事务保证数据一致性。
     * </p>
     *
     * @param videoId  视频ID
     * @param content  评论内容
     * @param parentId 父评论ID（可为null，顶级评论时传null）
     * @param userId   当前用户ID
     * @return 评论VO，包含评论详情和作者信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DocVideoCommentVo postComment(Long videoId, String content, Long parentId, Long userId) {
        log.info("发表视频评论, videoId={}, content={}, parentId={}, userId={}", videoId, content, parentId, userId);

        // 1. 构建评论实体
        DocVideoComment comment = new DocVideoComment();
        comment.setVideoId(videoId);
        comment.setCommentUserId(userId);
        comment.setCommentContent(content);
        comment.setLikeCount(0L);

        // 2. 设置父评论ID（如果是回复评论）
        if (parentId != null) {
            comment.setParentCommentId(parentId);
        }

        // 3. 保存评论到数据库
        commentMapper.insert(comment);
        log.info("评论发表成功, id={}", comment.getId());

        // 4. 构建并返回评论VO
        return buildCommentVo(comment, userId);
    }

    /**
     * 切换评论点赞状态
     * <p>
     * 用户点赞或取消点赞视频评论。操作成功后同步更新数据库中的点赞数字段，
     * 用于热门排序。使用事务保证缓存和数据库的数据一致性。
     * </p>
     *
     * @param commentId 评论ID
     * @param userId    当前用户ID
     * @return 点赞操作结果，包含操作是否成功和当前点赞状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public InteractionResultVo toggleCommentLike(Long commentId, Long userId) {
        log.info("切换评论点赞状态, commentId={}, userId={}", commentId, userId);

        // 1. 查询当前点赞状态
        boolean currentStatus = cacheDocLikeService.hasLiked(CacheDocLikeTypeEnum.VIDEO_COMMENT.getCode(), commentId, userId);

        if (currentStatus) {
            // 2. 当前已点赞，执行取消点赞操作
            cacheDocLikeService.unlike(CacheDocLikeTypeEnum.VIDEO_COMMENT.getCode(), commentId, userId);

            // 3. 同步更新数据库点赞数（减1）
            commentMapper.update(Wrappers.<DocVideoComment>lambdaUpdate()
                    .eq(DocVideoComment::getId, commentId)
                    .setSql("like_count = like_count - 1"));

            log.info("取消评论点赞成功, commentId={}, userId={}", commentId, userId);

            // 4. 构建取消点赞结果
            return InteractionResultVo.builder()
                    .success(true)
                    .status(false)
                    .build();
        } else {
            // 2. 当前未点赞，执行点赞操作
            cacheDocLikeService.like(CacheDocLikeTypeEnum.VIDEO_COMMENT.getCode(), commentId, userId);

            // 3. 同步更新数据库点赞数（加1）
            commentMapper.update(Wrappers.<DocVideoComment>lambdaUpdate()
                    .eq(DocVideoComment::getId, commentId)
                    .setSql("like_count = like_count + 1"));

            log.info("点赞评论成功, commentId={}, userId={}", commentId, userId);

            // 4. 构建点赞结果
            return InteractionResultVo.builder()
                    .success(true)
                    .status(true)
                    .build();
        }
    }

    /**
     * 构建视频作者信息VO
     * <p>
     * 根据作者ID构建作者信息，包含作者基本信息、粉丝数和当前用户的关注状态。
     * 粉丝数从缓存获取，关注状态根据当前用户ID查询。
     * </p>
     *
     * @param authorId 作者ID
     * @param userId   当前用户ID（可为null，未登录时传null）
     * @return 作者信息VO
     */
    private DocUserVo buildVideoAuthorInfo(Long authorId, Long userId) {
        log.debug("构建视频作者信息, authorId={}, userId={}", authorId, userId);

        // 1. 创建作者信息VO对象
        DocUserVo author = new DocUserVo();
        author.setId(authorId);
        author.setName("作者" + authorId);
        author.setAvatar("");

        // 2. 从缓存获取作者粉丝数
        Long fansCount = cacheDocFollowService.getFollowCount(CacheDocFollowTypeEnum.USER.getCode(), authorId);
        author.setFans(fansCount.intValue());

        // 3. 查询当前用户是否关注该作者
        boolean isFollowing = userId != null && cacheDocFollowService.hasFollowed(CacheDocFollowTypeEnum.USER.getCode(), authorId, userId);
        author.setIsFollowing(isFollowing);

        return author;
    }

    /**
     * 构建评论VO列表
     * <p>
     * 将评论实体列表转换为评论VO列表，包含每个评论的回复信息。
     * 先查询所有顶级评论，再批量查询对应的回复评论。
     * </p>
     *
     * @param comments 评论实体列表（顶级评论）
     * @param userId   当前用户ID（可为null，未登录时传null）
     * @return 评论VO列表，每个VO包含其回复列表
     */
    private List<DocVideoCommentVo> buildCommentVoList(List<DocVideoComment> comments, Long userId) {
        log.debug("构建评论VO列表, commentCount={}, userId={}", comments != null ? comments.size() : 0, userId);

        // 1. 处理空列表情况
        if (comments == null || comments.isEmpty()) {
            return List.of();
        }

        // 2. 提取所有顶级评论ID，用于查询回复
        List<Long> commentIds = comments.stream().map(DocVideoComment::getId).collect(Collectors.toList());

        // 3. 查询所有回复评论
        List<DocVideoComment> allReplies = commentMapper.selectList(
                Wrappers.<DocVideoComment>lambdaQuery()
                        .in(DocVideoComment::getParentCommentId, commentIds)
                        .eq(DocVideoComment::getDeleted, 0)
                        .orderByDesc(DocVideoComment::getCreateTime)
        );

        // 4. 将回复按父评论ID分组
        Map<Long, List<DocVideoComment>> replyMap = new HashMap<>();
        for (DocVideoComment reply : allReplies) {
            replyMap.computeIfAbsent(reply.getParentCommentId(), k -> new java.util.ArrayList<>()).add(reply);
        }

        // 5. 构建评论VO列表（包含回复信息）
        return comments.stream()
                .map(comment -> buildCommentVoWithReplies(comment, replyMap.getOrDefault(comment.getId(), List.of()), userId))
                .collect(Collectors.toList());
    }

    /**
     * 构建包含回复的评论VO
     * <p>
     * 将评论实体转换为评论VO，并添加其回复列表。
     * </p>
     *
     * @param comment  评论实体
     * @param replies  回复评论列表
     * @param userId   当前用户ID（可为null，未登录时传null）
     * @return 评论VO，包含回复列表
     */
    private DocVideoCommentVo buildCommentVoWithReplies(DocVideoComment comment, List<DocVideoComment> replies, Long userId) {
        log.debug("构建包含回复的评论VO, commentId={}, replyCount={}", comment.getId(), replies != null ? replies.size() : 0);

        // 1. 构建基础评论VO
        DocVideoCommentVo vo = buildCommentVo(comment, userId);

        // 2. 构建回复VO列表
        List<DocVideoCommentVo> replyVos = replies.stream()
                .map(reply -> buildReplyCommentVo(reply, userId))
                .collect(Collectors.toList());
        vo.setReplies(replyVos);

        return vo;
    }

    /**
     * 构建评论VO
     * <p>
     * 将评论实体转换为评论VO，包含作者信息、点赞数和当前用户的点赞状态。
     * 使用 DocUserVo 作为作者信息类型，保持与原有VO结构一致。
     * </p>
     *
     * @param comment 评论实体
     * @param userId  当前用户ID（可为null，未登录时传null）
     * @return 评论VO
     */
    private DocVideoCommentVo buildCommentVo(DocVideoComment comment, Long userId) {
        log.debug("构建评论VO, commentId={}, userId={}", comment.getId(), userId);

        // 1. 创建评论VO对象
        DocVideoCommentVo vo = new DocVideoCommentVo();
        vo.setId(comment.getId());
        vo.setContent(comment.getCommentContent());
        vo.setCreatedAt(comment.getCreateTime() != null ? comment.getCreateTime().toString() : null);
        vo.setLikes(comment.getLikeCount() != null ? comment.getLikeCount().intValue() : 0);

        // 2. 查询当前用户是否点赞该评论
        boolean isLiked = userId != null && cacheDocLikeService.hasLiked(CacheDocLikeTypeEnum.VIDEO_COMMENT.getCode(), comment.getId(), userId);
        vo.setIsLiked(isLiked);

        // 3. 构建评论作者信息（使用 DocUserVo）
        DocUserVo author = new DocUserVo();
        author.setId(comment.getCommentUserId());
        author.setName("用户" + comment.getCommentUserId());
        author.setAvatar("");
        vo.setAuthor(author);

        return vo;
    }

    /**
     * 构建回复评论VO
     * <p>
     * 将回复评论实体转换为回复评论VO，包含回复目标用户信息。
     * 使用 DocUserVo 作为回复对象类型，保持与原有VO结构一致。
     * </p>
     *
     * @param reply  回复评论实体
     * @param userId 当前用户ID（可为null，未登录时传null）
     * @return 回复评论VO，包含回复目标信息
     */
    private DocVideoCommentVo buildReplyCommentVo(DocVideoComment reply, Long userId) {
        log.debug("构建回复评论VO, replyId={}, userId={}", reply.getId(), userId);

        // 1. 先构建基础评论VO
        DocVideoCommentVo vo = buildCommentVo(reply, userId);

        // 2. 如果是回复评论，查询被回复用户信息（使用 DocUserVo）
        if (reply.getParentCommentId() != null) {
            DocVideoComment parentComment = commentMapper.selectById(reply.getParentCommentId());
            if (parentComment != null) {
                DocUserVo replyTo = new DocUserVo();
                replyTo.setId(parentComment.getCommentUserId());
                replyTo.setName("用户" + parentComment.getCommentUserId());
                vo.setReplyTo(replyTo);
            }
        }

        return vo;
    }
}