package com.zsk.document.service.impl;

import com.zsk.document.domain.DocVideo;
import com.zsk.document.domain.vo.*;
import com.zsk.document.enums.CacheDocCollectTypeEnum;
import com.zsk.document.enums.CacheDocFollowTypeEnum;
import com.zsk.document.enums.CacheDocLikeTypeEnum;
import com.zsk.document.enums.CacheDocViewTypeEnum;
import com.zsk.document.mapper.DocVideoMapper;
import com.zsk.document.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 视频首页详情服务实现类
 * <p>
 * 实现视频详情查询、交互操作（点赞、收藏、关注）等功能。
 * 评论相关功能已解耦到 {@link DocVideoCommentServiceImpl} 中，本类不再处理评论业务。
 * 所有交互数据（浏览量、点赞数、收藏数）均通过 Redis 缓存服务获取，
 * 确保数据的实时性和高性能。
 * </p>
 *
 * @author wuhuaming
 * @version 2.0
 * @date 2026-04-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocVideoHomeDetailServiceImpl implements IDocVideoHomeDetailService {

    /**
     * 视频数据访问对象，用于视频基础信息的数据库操作
     */
    private final DocVideoMapper videoMapper;

    /**
     * 文档浏览缓存服务，用于统计和查询视频浏览量
     */
    private final ICacheDocViewService cacheDocViewService;

    /**
     * 文档点赞缓存服务，用于管理视频的点赞状态
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
}
