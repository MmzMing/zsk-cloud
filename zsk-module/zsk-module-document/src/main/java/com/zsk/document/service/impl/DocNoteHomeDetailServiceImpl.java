package com.zsk.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.document.domain.DocNote;
import com.zsk.document.domain.DocNoteComment;
import com.zsk.document.domain.vo.*;
import com.zsk.document.enums.CacheDocCollectTypeEnum;
import com.zsk.document.enums.CacheDocFollowTypeEnum;
import com.zsk.document.enums.CacheDocLikeTypeEnum;
import com.zsk.document.enums.CacheDocViewTypeEnum;
import com.zsk.document.mapper.DocNoteCommentMapper;
import com.zsk.document.mapper.DocNoteMapper;
import com.zsk.document.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 笔记首页详情服务实现类
 * <p>
 * 实现笔记详情查询、交互操作（点赞、收藏、关注）、评论管理等功能。
 * 所有交互数据（浏览量、点赞数、收藏数）均通过 Redis 缓存服务获取。
 * </p>
 *
 * @author wuhuaming
 * @version 2.0
 * @date 2026-04-25
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
     * 笔记评论Mapper
     */
    private final DocNoteCommentMapper commentMapper;

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
     * 日期格式化器
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取笔记详情
     * <p>
     * 根据笔记ID查询笔记详情，并增加浏览量。
     * 如果用户已登录，会查询用户的点赞、收藏状态以及是否关注作者。
     * 所有统计数据（浏览量、点赞数、收藏数、评论数）均从 Redis 缓存获取。
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
        DocNoteHomeDetailStatsInfoVo stats = buildNoteStatsInfo(noteId, userId);
        vo.setStats(stats);

        // 5. 查询并设置作者信息（包含关注状态）
        DocNoteHomeDetailAuthorVo author = buildNoteAuthorInfo(noteId, userId);
        vo.setAuthor(author);

        log.info("获取笔记详情成功, noteId={}", noteId);
        return vo;
    }

    /**
     * 获取笔记交互详情
     * <p>
     * 独立查询笔记的交互统计数据，包括浏览量、点赞数、收藏数、评论数以及当前用户的交互状态。
     * </p>
     *
     * @param noteId 笔记ID
     * @param userId 当前用户ID（可为null，未登录时传null）
     * @return 笔记交互详情VO，如果笔记不存在返回null
     */
    @Override
    public DocNoteHomeDetailStatsInfoVo getNoteInteraction(Long noteId, Long userId) {
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
     * 获取笔记评论列表
     * <p>
     * 查询笔记的评论列表，支持热门排序和最新排序。
     * </p>
     *
     * @param noteId    笔记ID
     * @param pageQuery 分页查询参数
     * @param sort      排序方式（hot/new）
     * @param userId    当前用户ID（可为null，未登录时传null）
     * @return 评论分页列表
     */
    @Override
    public PageResult<DocCommentVo> getNoteComments(Long noteId, PageQuery pageQuery, String sort, Long userId) {
        log.info("获取笔记评论列表, noteId={}, pageQuery={}, sort={}, userId={}", noteId, pageQuery, sort, userId);

        // 1. 构建查询条件：查询未被删除的顶级评论
        LambdaQueryWrapper<DocNoteComment> wrapper = Wrappers.<DocNoteComment>lambdaQuery()
                .eq(DocNoteComment::getDeleted, 0)
                .eq(DocNoteComment::getNoteId, noteId)
                .isNull(DocNoteComment::getParentCommentId);

        // 2. 根据排序参数设置排序方式
        if ("hot".equals(sort)) {
            wrapper.orderByDesc(DocNoteComment::getLikeCount);
        } else {
            wrapper.orderByDesc(DocNoteComment::getCreateTime);
        }

        // 3. 查询评论总数
        long total = commentMapper.selectCount(wrapper);

        // 4. 应用分页参数
        wrapper.last("LIMIT " + pageQuery.getOffset() + ", " + pageQuery.getPageSize());

        // 5. 查询顶级评论列表
        List<DocNoteComment> comments = commentMapper.selectList(wrapper);

        // 6. 构建评论VO列表
        List<DocCommentVo> commentVos = new ArrayList<>();
        for (DocNoteComment comment : comments) {
            // 构建当前评论VO
            DocCommentVo vo = buildCommentVo(comment.getId(), userId);

            // 查询该评论的回复列表
            List<DocNoteComment> replies = commentMapper.selectList(
                    Wrappers.<DocNoteComment>lambdaQuery()
                            .eq(DocNoteComment::getDeleted, 0)
                            .eq(DocNoteComment::getParentCommentId, comment.getId())
                            .orderByAsc(DocNoteComment::getCreateTime)
            );

            // 构建回复VO列表
            List<DocCommentVo> replyVos = new ArrayList<>();
            for (DocNoteComment reply : replies) {
                replyVos.add(buildCommentVo(reply.getId(), userId));
            }
            vo.setReplies(replyVos);

            commentVos.add(vo);
        }

        // 7. 构建分页结果
        PageResult<DocCommentVo> pageResult = PageResult.of(
                commentVos,
                total,
                pageQuery.getPageNum(),
                pageQuery.getPageSize()
        );

        log.info("获取笔记评论列表成功, noteId={}, total={}", noteId, total);
        return pageResult;
    }

    /**
     * 发表笔记评论
     * <p>
     * 用户发表笔记评论，支持回复其他评论。评论成功后返回构建好的评论VO。
     * </p>
     *
     * @param noteId   笔记ID
     * @param content  评论内容
     * @param parentId 父评论ID（可为null，顶级评论时传null）
     * @param userId   当前用户ID（不能为空）
     * @return 评论VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DocCommentVo postComment(Long noteId, String content, Long parentId, Long userId) {
        log.info("发表笔记评论, noteId={}, content={}, parentId={}, userId={}", noteId, content, parentId, userId);

        // 构建评论实体
        DocNoteComment comment = new DocNoteComment();
        comment.setNoteId(noteId);
        comment.setCommentUserId(userId);
        comment.setCommentContent(content);
        comment.setLikeCount(0L);

        // 设置父评论ID（如果有）
        if (parentId != null) {
            comment.setParentCommentId(parentId);
        }

        // 保存评论
        commentMapper.insert(comment);
        log.info("评论发表成功, commentId={}", comment.getId());

        // 构建并返回评论VO
        return buildCommentVo(comment.getId(), userId);
    }

    /**
     * 切换评论点赞状态
     * <p>
     * 用户点赞或取消点赞笔记评论。
     * </p>
     *
     * @param commentId 评论ID
     * @param userId    当前用户ID（不能为空）
     * @return 点赞操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public InteractionResultVo toggleCommentLike(Long commentId, Long userId) {
        log.info("切换评论点赞状态, commentId={}, userId={}", commentId, userId);

        // 查询当前点赞状态
        boolean isLiked = cacheDocLikeService.hasLiked(CacheDocLikeTypeEnum.NOTE_COMMENT.getCode(), commentId, userId);

        // 执行相反操作
        boolean result;
        if (isLiked) {
            result = cacheDocLikeService.unlike(CacheDocLikeTypeEnum.NOTE_COMMENT.getCode(), commentId, userId);
            log.info("取消评论点赞成功, commentId={}, userId={}", commentId, userId);
        } else {
            result = cacheDocLikeService.like(CacheDocLikeTypeEnum.NOTE_COMMENT.getCode(), commentId, userId);
            log.info("评论点赞成功, commentId={}, userId={}", commentId, userId);
        }

        // 获取最新的点赞数量
        Long likeCount = cacheDocLikeService.getLikeCount(CacheDocLikeTypeEnum.NOTE_COMMENT.getCode(), commentId);

        // 更新评论表的点赞数（用于排序）
        DocNoteComment comment = commentMapper.selectById(commentId);
        if (comment != null) {
            comment.setLikeCount(likeCount);
            commentMapper.updateById(comment);
            log.debug("评论点赞数已更新, commentId={}, likeCount={}", commentId, likeCount);
        }

        // 构建并返回结果
        return InteractionResultVo.builder()
                .success(result)
                .status(result && !isLiked)
                .count(likeCount)
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
        vo.setContent(note.getContent());
        vo.setCategory(note.getBroadCode());
        vo.setDate(note.getCreateTime() != null ? note.getCreateTime().format(DATE_FORMATTER) : "");
        vo.setCoverUrl(note.getCover());

        return vo;
    }

    /**
     * 构建笔记统计信息VO
     * <p>
     * 从 Redis 缓存服务获取笔记的浏览量、点赞数、收藏数、评论数，
     * 以及当前用户的点赞、收藏状态。
     * </p>
     *
     * @param noteId 笔记ID
     * @param userId 当前用户ID（可为null）
     * @return 笔记统计信息VO
     */
    @Override
    public DocNoteHomeDetailStatsInfoVo buildNoteStatsInfo(Long noteId, Long userId) {
        log.debug("构建笔记统计信息VO, noteId={}, userId={}", noteId, userId);

        DocNoteHomeDetailStatsInfoVo stats = new DocNoteHomeDetailStatsInfoVo();

        // 1. 获取浏览量
        Long viewCount = cacheDocViewService.getViewCount(CacheDocViewTypeEnum.NOTE.getCode(), noteId);
        stats.setViews(viewCount.intValue());

        // 2. 获取点赞数
        Long likeCount = cacheDocLikeService.getLikeCount(CacheDocLikeTypeEnum.NOTE.getCode(), noteId);
        stats.setLikes(likeCount.intValue());

        // 3. 获取收藏数
        Long collectCount = cacheDocCollectService.getCollectCount(CacheDocCollectTypeEnum.NOTE.getCode(), noteId);
        stats.setFavorites(collectCount.intValue());

        // 4. 获取评论数
        Long commentCount = commentMapper.selectCount(
                Wrappers.<DocNoteComment>lambdaQuery()
                        .eq(DocNoteComment::getDeleted, 0)
                        .eq(DocNoteComment::getNoteId, noteId)
        );
        stats.setComments(commentCount.intValue());

        // 5. 查询当前用户的交互状态
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
    public DocNoteHomeDetailAuthorVo buildNoteAuthorInfo(Long noteId, Long userId) {
        log.debug("构建笔记作者信息VO, noteId={}, userId={}", noteId, userId);

        DocNote note = noteMapper.selectById(noteId);
        if (note == null) {
            log.warn("笔记不存在, noteId={}", noteId);
            return null;
        }

        DocNoteHomeDetailAuthorVo author = new DocNoteHomeDetailAuthorVo();
        author.setId(note.getUserId());
        author.setName("作者" + note.getUserId());
        author.setAvatar("");

        // 获取作者粉丝数
        Long fansCount = cacheDocFollowService.getFollowCount(
                CacheDocFollowTypeEnum.NOTE_AUTHOR.getCode(), note.getUserId());
        author.setFans(String.valueOf(fansCount));

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

    /**
     * 构建评论VO
     * <p>
     * 将评论实体转换为评论VO，包含作者信息、点赞数、点赞状态等。
     * </p>
     *
     * @param commentId     评论ID
     * @param currentUserId 当前登录用户ID（可为null）
     * @return 评论VO，如果评论不存在返回null
     */
    @Override
    public DocCommentVo buildCommentVo(Long commentId, Long currentUserId) {
        log.debug("构建评论VO, commentId={}, currentUserId={}", commentId, currentUserId);

        DocNoteComment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            log.warn("评论不存在, commentId={}", commentId);
            return null;
        }

        DocCommentVo vo = new DocCommentVo();
        vo.setId(comment.getId());
        vo.setContent(comment.getCommentContent());
        vo.setCreatedAt(comment.getCreateTime() != null ? comment.getCreateTime().format(DATE_FORMATTER) : "");

        // 获取评论点赞数
        Long likeCount = cacheDocLikeService.getLikeCount(CacheDocLikeTypeEnum.NOTE_COMMENT.getCode(), comment.getId());
        vo.setLikes(likeCount.intValue());

        // 构建评论作者信息
        DocCommentVo.AuthorInfo author = new DocCommentVo.AuthorInfo();
        author.setId(comment.getCommentUserId());
        author.setName("用户" + comment.getCommentUserId());
        author.setAvatar("");
        vo.setAuthor(author);

        // 设置回复对象信息（通过parentCommentId关联）
        if (comment.getParentCommentId() != null) {
            DocNoteComment parentComment = commentMapper.selectById(comment.getParentCommentId());
            if (parentComment != null) {
                DocCommentVo.ReplyToInfo replyTo = new DocCommentVo.ReplyToInfo();
                replyTo.setId(parentComment.getCommentUserId());
                replyTo.setName("用户" + parentComment.getCommentUserId());
                vo.setReplyTo(replyTo);
            }
        }

        // 查询当前用户是否点赞该评论
        if (currentUserId != null) {
            vo.setIsLiked(cacheDocLikeService.hasLiked(
                    CacheDocLikeTypeEnum.NOTE_COMMENT.getCode(), comment.getId(), currentUserId));
        } else {
            vo.setIsLiked(false);
        }

        return vo;
    }
}