package com.zsk.document.service.impl;

import com.zsk.document.domain.DocNote;
import com.zsk.document.domain.DocNoteDtl;
import com.zsk.document.domain.vo.*;
import com.zsk.document.enums.CacheDocCollectTypeEnum;
import com.zsk.document.enums.CacheDocFollowTypeEnum;
import com.zsk.document.enums.CacheDocLikeTypeEnum;
import com.zsk.document.enums.CacheDocViewTypeEnum;
import com.zsk.document.mapper.DocNoteMapper;
import com.zsk.document.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * 笔记首页详情服务实现类
 * <p>
 * 实现笔记详情查询、交互操作（点赞、收藏、关注）等功能。
 * 评论相关功能已解耦到 {@link DocNoteCommentServiceImpl} 中，本类不再处理评论业务。
 * 所有交互数据（浏览量、点赞数、收藏数）均通过 Redis 缓存服务获取。
 * </p>
 *
 * @author wuhuaming
 * @version 3.0
 * @date 2026-04-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocNoteHomeDetailServiceImpl implements IDocNoteHomeDetailService {

    /**
     * 笔记Mapper
     */
    private final DocNoteMapper noteMapper;

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
     * 笔记详情服务（用于获取笔记内容）
     */
    private final IDocNoteDtlService noteDtlService;

    /**
     * 日期格式化器
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取笔记详情
     * <p>
     * 根据笔记ID查询笔记详情，并增加浏览量。
     * 如果用户已登录，会查询用户的点赞、收藏状态以及是否关注作者。
     * 所有统计数据（浏览量、点赞数、收藏数）均从 Redis 缓存获取。
     * </p>
     *
     * @param noteId 笔记ID
     * @param userId 当前用户ID（可为null，未登录时传null）
     * @return 笔记详情VO，如果笔记不存在返回null
     */
    @Override
    public DocNoteHomeDetailVo getNoteDetail(Long noteId, Long userId) {
        log.info("获取笔记详情, noteId={}, userId={}", noteId, userId);

        // 1. 根据ID查询笔记
        DocNote note = noteMapper.selectById(noteId);
        if (note == null) {
            log.warn("笔记不存在, noteId={}", noteId);
            return null;
        }

        // 2. 增加浏览量（用户未登录也记录匿名浏览）
        cacheDocViewService.view(CacheDocViewTypeEnum.NOTE.getCode(), noteId, userId);
        log.debug("浏览量已增加, noteId={}", noteId);

        // 3. 构建笔记详情VO
        DocNoteHomeDetailVo vo = buildNoteHomeDetailVo(noteId);

        // 4. 查询并设置统计数据
        DocStatsInfoVo stats = buildNoteStatsInfo(noteId, userId);
        vo.setStats(stats);

        // 5. 查询并设置作者信息（包含关注状态）
        DocUserVo author = buildNoteAuthorInfo(noteId, userId);
        vo.setAuthor(author);

        log.info("获取笔记详情成功, noteId={}", noteId);
        return vo;
    }

    /**
     * 获取笔记交互详情
     * <p>
     * 独立查询笔记的交互统计数据，包括浏览量、点赞数、收藏数以及当前用户的交互状态。
     * </p>
     *
     * @param noteId 笔记ID
     * @param userId 当前用户ID（可为null，未登录时传null）
     * @return 笔记交互详情VO，如果笔记不存在返回null
     */
    @Override
    public DocStatsInfoVo getNoteInteraction(Long noteId, Long userId) {
        log.info("获取笔记交互详情, noteId={}, userId={}", noteId, userId);

        // 验证笔记是否存在
        DocNote note = noteMapper.selectById(noteId);
        if (note == null) {
            log.warn("笔记不存在, noteId={}", noteId);
            return null;
        }

        return buildNoteStatsInfo(noteId, userId);
    }

    /**
     * 切换笔记点赞状态
     * <p>
     * 用户点赞或取消点赞笔记。先查询当前点赞状态，然后执行相反操作。
     * </p>
     *
     * @param noteId 笔记ID
     * @param userId 当前用户ID（不能为空）
     * @return 点赞操作结果
     */
    @Override
    public InteractionResultVo toggleNoteLike(Long noteId, Long userId) {
        log.info("切换笔记点赞状态, noteId={}, userId={}", noteId, userId);

        // 查询当前点赞状态
        boolean isLiked = cacheDocLikeService.hasLiked(CacheDocLikeTypeEnum.NOTE.getCode(), noteId, userId);

        // 执行相反操作
        if (isLiked) {
            cacheDocLikeService.unlike(CacheDocLikeTypeEnum.NOTE.getCode(), noteId, userId);
            log.info("取消笔记点赞成功, noteId={}, userId={}", noteId, userId);
        } else {
            cacheDocLikeService.like(CacheDocLikeTypeEnum.NOTE.getCode(), noteId, userId);
            log.info("笔记点赞成功, noteId={}, userId={}", noteId, userId);
        }

        // 获取最新的点赞数量
        Long likeCount = cacheDocLikeService.getLikeCount(CacheDocLikeTypeEnum.NOTE.getCode(), noteId);

        // 构建并返回结果
        return InteractionResultVo.builder()
                .success(true)
                .status(!isLiked)
                .count(likeCount)
                .build();
    }

    /**
     * 切换笔记收藏状态
     * <p>
     * 用户收藏或取消收藏笔记。先查询当前收藏状态，然后执行相反操作。
     * </p>
     *
     * @param noteId 笔记ID
     * @param userId 当前用户ID（不能为空）
     * @return 收藏操作结果
     */
    @Override
    public InteractionResultVo toggleNoteFavorite(Long noteId, Long userId) {
        log.info("切换笔记收藏状态, noteId={}, userId={}", noteId, userId);

        // 查询当前收藏状态
        boolean isFavorited = cacheDocCollectService.hasCollected(CacheDocCollectTypeEnum.NOTE.getCode(), noteId, userId);

        // 执行相反操作
        if (isFavorited) {
            cacheDocCollectService.uncollect(CacheDocCollectTypeEnum.NOTE.getCode(), noteId, userId);
            log.info("取消笔记收藏成功, noteId={}, userId={}", noteId, userId);
        } else {
            cacheDocCollectService.collect(CacheDocCollectTypeEnum.NOTE.getCode(), noteId, userId);
            log.info("笔记收藏成功, noteId={}, userId={}", noteId, userId);
        }

        // 获取最新的收藏数量
        Long collectCount = cacheDocCollectService.getCollectCount(CacheDocCollectTypeEnum.NOTE.getCode(), noteId);

        // 构建并返回结果
        return InteractionResultVo.builder()
                .success(true)
                .status(!isFavorited)
                .count(collectCount)
                .build();
    }

    /**
     * 切换关注作者状态
     * <p>
     * 用户关注或取消关注笔记作者。
     * </p>
     *
     * @param authorId 作者ID
     * @param userId   当前用户ID（不能为空）
     * @return 关注操作结果
     */
    @Override
    public InteractionResultVo toggleFollowAuthor(Long authorId, Long userId) {
        log.info("切换关注作者状态, authorId={}, userId={}", authorId, userId);

        // 查询当前关注状态
        boolean isFollowing = cacheDocFollowService.hasFollowed(CacheDocFollowTypeEnum.NOTE_AUTHOR.getCode(), authorId, userId);

        // 执行相反操作
        if (isFollowing) {
            cacheDocFollowService.unfollow(CacheDocFollowTypeEnum.NOTE_AUTHOR.getCode(), authorId, userId);
            log.info("取消关注作者成功, authorId={}, userId={}", authorId, userId);
        } else {
            cacheDocFollowService.follow(CacheDocFollowTypeEnum.NOTE_AUTHOR.getCode(), authorId, userId);
            log.info("关注作者成功, authorId={}, userId={}", authorId, userId);
        }

        // 获取最新的粉丝数量
        Long followCount = cacheDocFollowService.getFollowCount(CacheDocFollowTypeEnum.NOTE_AUTHOR.getCode(), authorId);

        // 构建并返回结果
        return InteractionResultVo.builder()
                .success(true)
                .status(!isFollowing)
                .count(followCount)
                .build();
    }

    /**
     * 构建笔记首页详情VO
     * <p>
     * 将笔记实体转换为笔记首页详情VO，包含基本信息。
     * </p>
     *
     * @param noteId 笔记ID
     * @return 笔记首页详情VO，如果笔记不存在返回null
     */
    @Override
    public DocNoteHomeDetailVo buildNoteHomeDetailVo(Long noteId) {
        log.debug("构建笔记首页详情VO, noteId={}", noteId);

        DocNote note = noteMapper.selectById(noteId);
        if (note == null) {
            log.warn("笔记不存在, noteId={}", noteId);
            return null;
        }

        DocNoteHomeDetailVo vo = new DocNoteHomeDetailVo();
        vo.setId(note.getId());
        vo.setTitle(note.getNoteName());

        // 从笔记详情表获取内容
        String content = "";
        if (note.getId() != null) {
            DocNoteDtl dtl = noteDtlService.getByNoteId(note.getId());
            if (dtl != null && dtl.getContent() != null) {
                content = dtl.getContent();
            }
        }
        vo.setContent(content);

        vo.setCategory(note.getBroadCode());
        vo.setDate(note.getCreateTime() != null ? note.getCreateTime().format(DATE_FORMATTER) : "");

        return vo;
    }

    /**
     * 构建笔记统计信息VO
     * <p>
     * 从 Redis 缓存服务获取笔记的浏览量、点赞数、收藏数，
     * 以及当前用户的点赞、收藏状态。
     * </p>
     *
     * @param noteId 笔记ID
     * @param userId 当前用户ID（可为null）
     * @return 笔记统计信息VO
     */
    @Override
    public DocStatsInfoVo buildNoteStatsInfo(Long noteId, Long userId) {
        log.debug("构建笔记统计信息VO, noteId={}, userId={}", noteId, userId);

        DocStatsInfoVo stats = new DocStatsInfoVo();

        // 1. 获取浏览量
        Long viewCount = cacheDocViewService.getViewCount(CacheDocViewTypeEnum.NOTE.getCode(), noteId);
        stats.setViews(viewCount.intValue());

        // 2. 获取点赞数
        Long likeCount = cacheDocLikeService.getLikeCount(CacheDocLikeTypeEnum.NOTE.getCode(), noteId);
        stats.setLikes(likeCount.intValue());

        // 3. 获取收藏数
        Long collectCount = cacheDocCollectService.getCollectCount(CacheDocCollectTypeEnum.NOTE.getCode(), noteId);
        stats.setFavorites(collectCount.intValue());

        // 4. 查询当前用户的交互状态
        if (userId != null) {
            boolean isLiked = cacheDocLikeService.hasLiked(CacheDocLikeTypeEnum.NOTE.getCode(), noteId, userId);
            stats.setIsLiked(isLiked);

            boolean isFavorited = cacheDocCollectService.hasCollected(CacheDocCollectTypeEnum.NOTE.getCode(), noteId, userId);
            stats.setIsFavorited(isFavorited);
        } else {
            stats.setIsLiked(false);
            stats.setIsFavorited(false);
        }

        return stats;
    }

    /**
     * 构建笔记作者信息VO
     * <p>
     * 根据笔记实体构建作者信息，并查询当前用户是否关注该作者。
     * </p>
     *
     * @param noteId 笔记ID
     * @param userId 当前用户ID（可为null）
     * @return 笔记作者信息VO，如果笔记不存在返回null
     */
    @Override
    public DocUserVo buildNoteAuthorInfo(Long noteId, Long userId) {
        log.debug("构建笔记作者信息VO, noteId={}, userId={}", noteId, userId);

        DocNote note = noteMapper.selectById(noteId);
        if (note == null) {
            log.warn("笔记不存在, noteId={}", noteId);
            return null;
        }

        DocUserVo author = new DocUserVo();
        author.setId(note.getUserId());
        author.setName("作者" + note.getUserId());
        author.setAvatar("");

        // 获取作者粉丝数
        Long fansCount = cacheDocFollowService.getFollowCount(
                CacheDocFollowTypeEnum.NOTE_AUTHOR.getCode(), note.getUserId());
        author.setFans(fansCount.intValue());

        // 查询当前用户是否关注作者
        if (userId != null) {
            boolean isFollowing = cacheDocFollowService.hasFollowed(
                    CacheDocFollowTypeEnum.NOTE_AUTHOR.getCode(), note.getUserId(), userId);
            author.setIsFollowing(isFollowing);
        } else {
            author.setIsFollowing(false);
        }

        return author;
    }
}
