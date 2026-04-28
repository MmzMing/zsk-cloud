package com.zsk.document.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zsk.common.core.exception.BusinessException;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.document.domain.DocVideo;
import com.zsk.document.domain.DocVideoCollection;
import com.zsk.document.domain.DocVideoCollectionItem;
import com.zsk.document.domain.dto.DocHomeVideoCommentPostDto;
import com.zsk.document.domain.dto.VideoCommentRequestDTO;
import com.zsk.document.domain.vo.*;
import com.zsk.document.enums.CacheDocCollectTypeEnum;
import com.zsk.document.enums.CacheDocFollowTypeEnum;
import com.zsk.document.enums.CacheDocLikeTypeEnum;
import com.zsk.document.enums.CacheDocViewTypeEnum;
import com.zsk.document.mapper.DocVideoCollectionItemMapper;
import com.zsk.document.mapper.DocVideoCollectionMapper;
import com.zsk.document.mapper.DocVideoMapper;
import com.zsk.document.service.*;
import com.zsk.system.api.RemoteUserService;
import com.zsk.system.api.domain.SysUserApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 前台首页视频聚合服务实现类
 * <p>
 * 前台首页视频详情的聚合服务实现，负责从数据库和Redis缓存中拼装数据。
 * 本服务是前台聚合层，仅调用其他已有Service方法进行数据拼装，
 * 不在其他Service中编写方法供本服务调用，确保前后台逻辑隔离。
 * </p>
 * <p>
 * 四大区域化接口：
 * 1. 视频元信息+详情：从数据库查询视频基本信息，优先增加浏览量
 * 2. 点赞收藏+作者关注信息：从Redis缓存获取交互数据和作者信息
 * 3. 评论区域：调用评论Service获取二级结构评论列表
 * 4. 视频合集：查询包含该视频的公开合集列表
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-29
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocHomeVideoServiceImpl implements IDocHomeVideoService {

    /**
     * 视频Mapper
     */
    private final DocVideoMapper videoMapper;

    /**
     * 缓存浏览服务
     */
    private final ICacheDocViewService cacheDocViewService;

    /**
     * 缓存点赞服务
     */
    private final ICacheDocLikeService cacheDocLikeService;

    /**
     * 缓存收藏服务
     */
    private final ICacheDocCollectService cacheDocCollectService;

    /**
     * 缓存关注服务
     */
    private final ICacheDocFollowService cacheDocFollowService;

    /**
     * 视频服务（用于获取视频文件URL）
     */
    private final IDocVideoService videoService;

    /**
     * 视频评论服务
     */
    private final IDocVideoCommentService docVideoCommentService;

    /**
     * 远程用户服务
     */
    private final RemoteUserService remoteUserService;

    /**
     * 视频合集Mapper
     */
    private final DocVideoCollectionMapper videoCollectionMapper;

    /**
     * 视频合集关联项Mapper
     */
    private final DocVideoCollectionItemMapper videoCollectionItemMapper;

    // ==================== 区域一：视频元信息+详情 ====================

    /**
     * 获取视频元信息+详情
     * <p>
     * 查询视频的基本信息和内容详情，仅返回前台展示所需字段。
     * 获取元信息时优先增加浏览量（Redis），确保浏览量统计准确。
     * 不包含审核状态、版本号、后台标记等敏感字段。
     * </p>
     *
     * @param videoId 视频ID
     * @param userId  当前用户ID（可为null，未登录时传null）
     * @return 视频详情VO，如果视频不存在返回null
     */
    @Override
    public DocHomeVideoDetailVo getVideoDetail(Long videoId, Long userId) {
        log.info("获取视频元信息+详情, videoId={}, userId={}", videoId, userId);

        // 1. 优先增加浏览量（Redis），无论后续查询是否成功都记录浏览
        cacheDocViewService.view(CacheDocViewTypeEnum.VIDEO.getCode(), videoId, userId);
        log.debug("浏览量已增加, videoId={}", videoId);

        // 2. 查询视频基本信息
        DocVideo video = videoMapper.selectById(videoId);
        if (video == null) {
            log.warn("视频不存在, videoId={}", videoId);
            return null;
        }

        // 3. 获取视频文件URL（通过视频Service获取）
        DocVideoDetailVo detailVo = videoService.getDetailWithFileUrl(videoId);

        // 4. 组装视频详情VO（仅包含前台展示字段，不含敏感数据）
        DocHomeVideoDetailVo vo = new DocHomeVideoDetailVo();
        vo.setId(video.getId());
        vo.setTitle(video.getVideoTitle());
        vo.setDescription(video.getFileContent());
        vo.setCategory(video.getBroadCode());

        // 5. 设置视频播放地址和封面地址
        if (detailVo != null && detailVo.getVideoFile() != null) {
            if (detailVo.getVideoFile().getVideo() != null) {
                vo.setVideoUrl(detailVo.getVideoFile().getVideo().getFileUrl());
            }
            if (detailVo.getVideoFile().getThumbnail() != null) {
                vo.setCoverUrl(detailVo.getVideoFile().getThumbnail().getFileUrl());
            }
        }

        // 6. 解析标签列表（逗号分隔的字符串转为列表）
        if (video.getTags() != null && !video.getTags().isEmpty()) {
            vo.setTags(List.of(video.getTags().split(",")));
        }

        log.info("获取视频元信息+详情成功, videoId={}", videoId);
        return vo;
    }

    // ==================== 区域二：点赞收藏+作者关注信息 ====================

    /**
     * 获取视频交互信息（点赞收藏+作者关注）
     * <p>
     * 独立查询视频的交互统计数据和作者关注信息。
     * 浏览量、点赞数、收藏数分别从Redis缓存独立查询。
     * 作者信息（昵称、头像）从远程用户服务获取，粉丝数和关注状态从Redis获取。
     * </p>
     *
     * @param videoId 视频ID
     * @param userId  当前用户ID（可为null，未登录时传null）
     * @return 视频交互信息VO，如果视频不存在返回null
     */
    @Override
    public DocHomeVideoInteractionVo getVideoInteraction(Long videoId, Long userId) {
        log.info("获取视频交互信息, videoId={}, userId={}", videoId, userId);

        // 1. 验证视频是否存在
        DocVideo video = videoMapper.selectById(videoId);
        if (video == null) {
            log.warn("视频不存在, videoId={}", videoId);
            return null;
        }

        // 2. 从Redis独立查询浏览量
        Long viewCount = cacheDocViewService.getViewCount(CacheDocViewTypeEnum.VIDEO.getCode(), videoId);
        log.debug("浏览量查询完成, videoId={}, viewCount={}", videoId, viewCount);

        // 3. 从Redis独立查询点赞数和点赞状态
        Long likeCount = cacheDocLikeService.getLikeCount(CacheDocLikeTypeEnum.VIDEO.getCode(), videoId);
        boolean isLiked = userId != null && cacheDocLikeService.hasLiked(CacheDocLikeTypeEnum.VIDEO.getCode(), videoId, userId);
        log.debug("点赞数据查询完成, videoId={}, likeCount={}, isLiked={}", videoId, likeCount, isLiked);

        // 4. 从Redis独立查询收藏数和收藏状态
        Long favoriteCount = cacheDocCollectService.getCollectCount(CacheDocCollectTypeEnum.VIDEO.getCode(), videoId);
        boolean isFavorited = userId != null && cacheDocCollectService.hasCollected(CacheDocCollectTypeEnum.VIDEO.getCode(), videoId, userId);
        log.debug("收藏数据查询完成, videoId={}, favoriteCount={}, isFavorited={}", videoId, favoriteCount, isFavorited);

        // 5. 构建作者信息（包含粉丝数和关注状态）
        DocHomeNoteAuthorVo authorVo = buildAuthorVo(video.getUserId(), userId);

        // 6. 组装交互信息VO
        DocHomeVideoInteractionVo vo = new DocHomeVideoInteractionVo();
        vo.setViewCount(viewCount);
        vo.setLikeCount(likeCount);
        vo.setFavoriteCount(favoriteCount);
        vo.setIsLiked(isLiked);
        vo.setIsFavorited(isFavorited);
        vo.setAuthor(authorVo);

        log.info("获取视频交互信息成功, videoId={}", videoId);
        return vo;
    }

    // ==================== 区域三：评论区域 ====================

    /**
     * 获取视频评论列表（分页，二级结构）
     * <p>
     * 调用评论Service获取二级结构评论列表，然后转换为前台展示VO。
     * 采用B站式二级评论结构：根评论包含回复列表，回复统一挂在根评论下。
     * </p>
     *
     * @param videoId   视频ID
     * @param pageQuery 分页查询参数
     * @param sort      排序方式（hot-按点赞数降序，new-按创建时间降序）
     * @param userId    当前用户ID（可为null，未登录时传null）
     * @return 评论分页结果
     */
    @Override
    public PageResult<DocHomeVideoCommentVo> getVideoComments(Long videoId, PageQuery pageQuery, String sort, Long userId) {
        log.info("获取视频评论列表, videoId={}, pageQuery={}, sort={}, userId={}", videoId, pageQuery, sort, userId);

        // 调用评论Service获取评论列表（已包含二级结构和用户信息）
        PageResult<DocVideoCommentVo> commentPage = docVideoCommentService.getCommentList(videoId, pageQuery, sort, userId);

        // 转换为前台首页评论VO
        List<DocHomeVideoCommentVo> homeCommentVos = commentPage.getList().stream()
                .map(this::convertToHomeCommentVo)
                .collect(Collectors.toList());

        log.info("获取视频评论列表成功, videoId={}, total={}", videoId, commentPage.getTotal());
        return PageResult.of(homeCommentVos, commentPage.getTotal(), commentPage.getPageNum(), commentPage.getPageSize());
    }

    // ==================== 区域四：视频合集 ====================

    /**
     * 获取视频所属的公开合集列表
     * <p>
     * 查询包含该视频的所有公开合集，每个合集包含其视频列表。
     * 仅返回公开状态（status=1）的合集，私密合集不对外展示。
     * 合集数据从合集Mapper和Service获取，视频列表仅包含前台展示字段。
     * </p>
     *
     * @param videoId 视频ID
     * @return 公开合集列表
     */
    @Override
    public List<DocHomeVideoCollectionVo> getVideoCollections(Long videoId) {
        log.info("获取视频所属公开合集列表, videoId={}", videoId);

        // 1. 查询该视频关联的所有合集ID
        List<DocVideoCollectionItem> items = videoCollectionItemMapper.selectList(
                Wrappers.<DocVideoCollectionItem>lambdaQuery()
                        .eq(DocVideoCollectionItem::getVideoId, videoId)
                        .eq(DocVideoCollectionItem::getDeleted, 0)
        );

        if (items.isEmpty()) {
            log.info("该视频未关联任何合集, videoId={}", videoId);
            return new ArrayList<>();
        }

        // 2. 提取合集ID列表
        List<Long> collectionIds = items.stream()
                .map(DocVideoCollectionItem::getCollectionId)
                .distinct()
                .collect(Collectors.toList());

        // 3. 查询公开状态的合集（status=1表示公开）
        List<DocVideoCollection> collections = videoCollectionMapper.selectList(
                Wrappers.<DocVideoCollection>lambdaQuery()
                        .in(DocVideoCollection::getId, collectionIds)
                        .eq(DocVideoCollection::getStatus, 1)
                        .eq(DocVideoCollection::getDeleted, 0)
        );

        // 4. 转换为前台首页合集VO
        List<DocHomeVideoCollectionVo> result = collections.stream()
                .map(this::convertToHomeCollectionVo)
                .collect(Collectors.toList());

        log.info("获取视频所属公开合集列表成功, videoId={}, count={}", videoId, result.size());
        return result;
    }

    // ==================== 交互操作：点赞、收藏、关注 ====================

    /**
     * 切换视频点赞状态
     * <p>
     * 通过Redis缓存服务操作点赞状态，先查询当前状态再执行相反操作。
     * </p>
     *
     * @param videoId 视频ID
     * @param userId  当前用户ID（不能为空）
     * @return 点赞操作结果
     */
    @Override
    public DocHomeVideoInteractionResultVo toggleVideoLike(Long videoId, Long userId) {
        log.info("切换视频点赞状态, videoId={}, userId={}", videoId, userId);

        boolean isLiked = cacheDocLikeService.hasLiked(CacheDocLikeTypeEnum.VIDEO.getCode(), videoId, userId);

        if (isLiked) {
            cacheDocLikeService.unlike(CacheDocLikeTypeEnum.VIDEO.getCode(), videoId, userId);
            log.info("取消视频点赞成功, videoId={}, userId={}", videoId, userId);
        } else {
            cacheDocLikeService.like(CacheDocLikeTypeEnum.VIDEO.getCode(), videoId, userId);
            log.info("视频点赞成功, videoId={}, userId={}", videoId, userId);
        }

        Long likeCount = cacheDocLikeService.getLikeCount(CacheDocLikeTypeEnum.VIDEO.getCode(), videoId);

        return DocHomeVideoInteractionResultVo.builder()
                .success(true)
                .status(!isLiked)
                .count(likeCount)
                .build();
    }

    /**
     * 切换视频收藏状态
     * <p>
     * 通过Redis缓存服务操作收藏状态，先查询当前状态再执行相反操作。
     * </p>
     *
     * @param videoId 视频ID
     * @param userId  当前用户ID（不能为空）
     * @return 收藏操作结果
     */
    @Override
    public DocHomeVideoInteractionResultVo toggleVideoFavorite(Long videoId, Long userId) {
        log.info("切换视频收藏状态, videoId={}, userId={}", videoId, userId);

        boolean isFavorited = cacheDocCollectService.hasCollected(CacheDocCollectTypeEnum.VIDEO.getCode(), videoId, userId);

        if (isFavorited) {
            cacheDocCollectService.uncollect(CacheDocCollectTypeEnum.VIDEO.getCode(), videoId, userId);
            log.info("取消视频收藏成功, videoId={}, userId={}", videoId, userId);
        } else {
            cacheDocCollectService.collect(CacheDocCollectTypeEnum.VIDEO.getCode(), videoId, userId);
            log.info("视频收藏成功, videoId={}, userId={}", videoId, userId);
        }

        Long collectCount = cacheDocCollectService.getCollectCount(CacheDocCollectTypeEnum.VIDEO.getCode(), videoId);

        return DocHomeVideoInteractionResultVo.builder()
                .success(true)
                .status(!isFavorited)
                .count(collectCount)
                .build();
    }

    /**
     * 切换关注作者状态
     * <p>
     * 通过Redis缓存服务操作关注状态，先查询当前状态再执行相反操作。
     * 视频作者关注使用 CacheDocFollowTypeEnum.VIDEO_AUTHOR 类型。
     * </p>
     *
     * @param authorId 作者ID
     * @param userId   当前用户ID（不能为空）
     * @return 关注操作结果
     */
    @Override
    public DocHomeVideoInteractionResultVo toggleFollowAuthor(Long authorId, Long userId) {
        log.info("切换关注作者状态, authorId={}, userId={}", authorId, userId);

        boolean isFollowing = cacheDocFollowService.hasFollowed(CacheDocFollowTypeEnum.VIDEO_AUTHOR.getCode(), authorId, userId);

        if (isFollowing) {
            cacheDocFollowService.unfollow(CacheDocFollowTypeEnum.VIDEO_AUTHOR.getCode(), authorId, userId);
            log.info("取消关注作者成功, authorId={}, userId={}", authorId, userId);
        } else {
            cacheDocFollowService.follow(CacheDocFollowTypeEnum.VIDEO_AUTHOR.getCode(), authorId, userId);
            log.info("关注作者成功, authorId={}, userId={}", authorId, userId);
        }

        Long followCount = cacheDocFollowService.getFollowCount(CacheDocFollowTypeEnum.VIDEO_AUTHOR.getCode(), authorId);

        return DocHomeVideoInteractionResultVo.builder()
                .success(true)
                .status(!isFollowing)
                .count(followCount)
                .build();
    }

    // ==================== 评论操作：发表/回复、点赞 ====================

    /**
     * 发表/回复视频评论
     * <p>
     * 发表根评论和回复评论使用同一个接口。
     * 转换为评论Service的DTO后调用已有方法处理业务逻辑。
     * </p>
     *
     * @param dto    评论请求DTO
     * @param userId 当前用户ID（不能为空）
     * @return 评论VO
     */
    @Override
    public DocHomeVideoCommentVo postComment(DocHomeVideoCommentPostDto dto, Long userId) {
        log.info("发表/回复视频评论, videoId={}, userId={}", dto.getVideoId(), userId);

        if (dto.getVideoId() == null) {
            throw new BusinessException("视频ID不能为空");
        }
        if (dto.getContent() == null || dto.getContent().isEmpty()) {
            throw new BusinessException("评论内容不能为空");
        }

        VideoCommentRequestDTO commentRequest = new VideoCommentRequestDTO();
        commentRequest.setVideoId(dto.getVideoId());
        commentRequest.setContent(dto.getContent());
        commentRequest.setParentId(dto.getParentId());
        commentRequest.setReplyToId(dto.getReplyToId());

        DocVideoCommentVo commentVo = docVideoCommentService.postCommentWithValidation(commentRequest, userId);

        DocHomeVideoCommentVo homeCommentVo = convertToHomeCommentVo(commentVo);

        log.info("发表/回复视频评论成功, commentId={}", homeCommentVo.getId());
        return homeCommentVo;
    }

    /**
     * 切换评论点赞状态
     * <p>
     * 通过评论Service操作评论点赞，然后转换为前台结果VO。
     * </p>
     *
     * @param commentId 评论ID
     * @param userId    当前用户ID（不能为空）
     * @return 点赞操作结果
     */
    @Override
    public DocHomeVideoInteractionResultVo toggleCommentLike(Long commentId, Long userId) {
        log.info("切换评论点赞状态, commentId={}, userId={}", commentId, userId);

        InteractionResultVo result = docVideoCommentService.toggleCommentLike(commentId, userId);

        return DocHomeVideoInteractionResultVo.builder()
                .success(result.isSuccess())
                .status(result.isStatus())
                .count(result.getCount())
                .build();
    }

    // ==================== 私有方法：数据组装 ====================

    /**
     * 构建作者信息VO
     * <p>
     * 从远程用户服务获取作者昵称和头像，从Redis获取粉丝数和关注状态。
     * 视频作者关注使用 CacheDocFollowTypeEnum.VIDEO_AUTHOR 类型。
     * </p>
     *
     * @param authorId 作者用户ID
     * @param userId   当前用户ID（可为null）
     * @return 作者信息VO
     */
    private DocHomeNoteAuthorVo buildAuthorVo(Long authorId, Long userId) {
        log.debug("构建作者信息VO, authorId={}, userId={}", authorId, userId);

        DocHomeNoteAuthorVo authorVo = new DocHomeNoteAuthorVo();
        authorVo.setId(authorId);

        SysUserApi authorUser = fetchUserById(authorId);
        if (authorUser != null) {
            authorVo.setName(authorUser.getNickName() != null ? authorUser.getNickName() : authorUser.getUserName());
            authorVo.setAvatar(authorUser.getAvatar());
        } else {
            authorVo.setName("用户" + authorId);
            authorVo.setAvatar("");
        }

        Long fansCount = cacheDocFollowService.getFollowCount(CacheDocFollowTypeEnum.VIDEO_AUTHOR.getCode(), authorId);
        authorVo.setFans(fansCount.intValue());

        if (userId != null) {
            boolean isFollowing = cacheDocFollowService.hasFollowed(CacheDocFollowTypeEnum.VIDEO_AUTHOR.getCode(), authorId, userId);
            authorVo.setIsFollowing(isFollowing);
        } else {
            authorVo.setIsFollowing(false);
        }

        return authorVo;
    }

    /**
     * 转换评论VO为前台首页评论VO
     * <p>
     * 将评论Service返回的DocVideoCommentVo转换为前台首页的DocHomeVideoCommentVo，
     * 同时递归转换回复列表和作者信息。
     * </p>
     *
     * @param commentVo 评论VO
     * @return 前台首页评论VO
     */
    private DocHomeVideoCommentVo convertToHomeCommentVo(DocVideoCommentVo commentVo) {
        if (commentVo == null) {
            return null;
        }

        DocHomeVideoCommentVo vo = new DocHomeVideoCommentVo();
        vo.setId(commentVo.getId());
        vo.setContent(commentVo.getContent());
        vo.setCreatedAt(commentVo.getCreatedAt());
        vo.setLikes(commentVo.getLikes());
        vo.setIsLiked(commentVo.getIsLiked());

        vo.setAuthor(convertToHomeAuthorVo(commentVo.getAuthor()));
        vo.setReplyTo(convertToHomeAuthorVo(commentVo.getReplyTo()));

        if (commentVo.getReplies() != null && !commentVo.getReplies().isEmpty()) {
            List<DocHomeVideoCommentVo> replyVos = commentVo.getReplies().stream()
                    .map(this::convertToHomeCommentVo)
                    .collect(Collectors.toList());
            vo.setReplies(replyVos);
        } else {
            vo.setReplies(new ArrayList<>());
        }

        return vo;
    }

    /**
     * 转换用户VO为前台首页作者VO
     * <p>
     * 将DocUserVo转换为DocHomeNoteAuthorVo，保持字段映射一致。
     * </p>
     *
     * @param userVo 用户VO
     * @return 前台首页作者VO
     */
    private DocHomeNoteAuthorVo convertToHomeAuthorVo(DocUserVo userVo) {
        if (userVo == null) {
            return null;
        }

        DocHomeNoteAuthorVo authorVo = new DocHomeNoteAuthorVo();
        authorVo.setId(userVo.getId());
        authorVo.setName(userVo.getName());
        authorVo.setAvatar(userVo.getAvatar());
        authorVo.setFans(userVo.getFans());
        authorVo.setIsFollowing(userVo.getIsFollowing());
        return authorVo;
    }

    /**
     * 转换合集实体为前台首页合集VO
     * <p>
     * 将DocVideoCollection转换为DocHomeVideoCollectionVo，
     * 并查询合集中的视频列表，仅包含前台展示字段。
     * </p>
     *
     * @param collection 合集实体
     * @return 前台首页合集VO
     */
    private DocHomeVideoCollectionVo convertToHomeCollectionVo(DocVideoCollection collection) {
        DocHomeVideoCollectionVo vo = new DocHomeVideoCollectionVo();
        vo.setId(collection.getId());
        vo.setCollectionName(collection.getCollectionName());
        vo.setDescription(collection.getDescription());
        vo.setVideoCount(collection.getVideoCount());

        // 查询合集中的视频关联项
        List<DocVideoCollectionItem> items = videoCollectionItemMapper.selectList(
                Wrappers.<DocVideoCollectionItem>lambdaQuery()
                        .eq(DocVideoCollectionItem::getCollectionId, collection.getId())
                        .eq(DocVideoCollectionItem::getDeleted, 0)
                        .orderByAsc(DocVideoCollectionItem::getSortOrder)
        );

        if (!items.isEmpty()) {
            List<Long> videoIds = items.stream()
                    .map(DocVideoCollectionItem::getVideoId)
                    .collect(Collectors.toList());

            // 查询视频基本信息
            List<DocVideo> videos = videoMapper.selectList(
                    Wrappers.<DocVideo>lambdaQuery()
                            .in(DocVideo::getId, videoIds)
                            .eq(DocVideo::getDeleted, 0)
            );

            // 转换为合集视频项VO
            List<DocHomeVideoCollectionItemVo> videoVos = videos.stream()
                    .map(this::convertToHomeCollectionItemVo)
                    .collect(Collectors.toList());
            vo.setVideos(videoVos);
        } else {
            vo.setVideos(new ArrayList<>());
        }

        return vo;
    }

    /**
     * 转换视频实体为合集视频项VO
     * <p>
     * 将DocVideo转换为DocHomeVideoCollectionItemVo，
     * 仅包含前台展示字段，浏览量从Redis获取。
     * </p>
     *
     * @param video 视频实体
     * @return 合集视频项VO
     */
    private DocHomeVideoCollectionItemVo convertToHomeCollectionItemVo(DocVideo video) {
        DocHomeVideoCollectionItemVo vo = new DocHomeVideoCollectionItemVo();
        vo.setId(video.getId());
        vo.setTitle(video.getVideoTitle());
        vo.setDescription(video.getFileContent());

        // 从Redis获取浏览量
        Long viewCount = cacheDocViewService.getViewCount(CacheDocViewTypeEnum.VIDEO.getCode(), video.getId());
        vo.setViewCount(viewCount);

        // 获取视频文件URL
        DocVideoDetailVo detailVo = videoService.getDetailWithFileUrl(video.getId());
        if (detailVo != null && detailVo.getVideoFile() != null) {
            if (detailVo.getVideoFile().getVideo() != null) {
                vo.setVideoUrl(detailVo.getVideoFile().getVideo().getFileUrl());
            }
            if (detailVo.getVideoFile().getThumbnail() != null) {
                vo.setCoverUrl(detailVo.getVideoFile().getThumbnail().getFileUrl());
            }
        }

        return vo;
    }

    /**
     * 根据用户ID获取用户信息
     * <p>
     * 通过远程用户服务获取用户公开信息（昵称、头像）。
     * 调用失败时返回null，由调用方使用降级信息。
     * </p>
     *
     * @param userId 用户ID
     * @return 用户信息，获取失败返回null
     */
    private SysUserApi fetchUserById(Long userId) {
        if (userId == null) {
            return null;
        }
        try {
            var result = remoteUserService.getUserById(userId);
            if (result != null && result.getData() != null) {
                return result.getData();
            }
        } catch (Exception e) {
            log.error("获取用户信息失败, userId={}", userId, e);
        }
        return null;
    }
}
