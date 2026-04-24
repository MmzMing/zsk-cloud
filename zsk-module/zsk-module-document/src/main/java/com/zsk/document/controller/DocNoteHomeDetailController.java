package com.zsk.document.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zsk.common.core.domain.R;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.common.security.utils.SecurityUtils;
import com.zsk.document.domain.DocNote;
import com.zsk.document.domain.DocNoteComment;
import com.zsk.document.domain.vo.CommentRequestVo;
import com.zsk.document.domain.vo.DocCommentVo;
import com.zsk.document.domain.vo.DocNoteHomeDetailAuthorVo;
import com.zsk.document.domain.vo.DocNoteHomeDetailStatsInfoVo;
import com.zsk.document.domain.vo.DocNoteHomeDetailVo;
import com.zsk.document.domain.vo.InteractionResultVo;
import com.zsk.document.enums.CacheDocCollectTypeEnum;
import com.zsk.document.enums.CacheDocFollowTypeEnum;
import com.zsk.document.enums.CacheDocLikeTypeEnum;
import com.zsk.document.enums.CacheDocViewTypeEnum;
import com.zsk.document.service.ICacheDocCollectService;
import com.zsk.document.service.ICacheDocFollowService;
import com.zsk.document.service.ICacheDocLikeService;
import com.zsk.document.service.ICacheDocViewService;
import com.zsk.document.service.IDocNoteCommentService;
import com.zsk.document.service.IDocNoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 前台笔记首页详情控制器
 * <p>
 * 提供笔记详情查询、交互操作（点赞、收藏、关注）、评论管理等功能。
 * 所有交互数据（浏览量、点赞数、收藏数）均通过 Redis 缓存服务获取，不再依赖主表字段。
 * </p>
 *
 * @author wuhuaming
 * @date 2026-04-25
 * @version 2.0
 */
@Tag(name = "前台笔记首页详情")
@RestController
@RequestMapping("/docNoteHomeDetail")
@RequiredArgsConstructor
public class DocNoteHomeDetailController {

    /**
     * 笔记服务
     */
    private final IDocNoteService noteService;

    /**
     * 笔记评论服务
     */
    private final IDocNoteCommentService commentService;

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
     * 缓存浏览服务
     */
    private final ICacheDocViewService cacheDocViewService;

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
     * @param id 笔记ID
     * @return 笔记详情
     */
    @Operation(summary = "获取笔记详情")
    @GetMapping("/detail/{id}")
    public R<DocNoteHomeDetailVo> getDetail(@PathVariable("id") Long id) {
        // 1. 根据ID查询笔记
        DocNote note = noteService.getById(id);
        if (note == null) {
            return R.fail("笔记不存在");
        }

        // 2. 获取当前登录用户ID
        Long userId = getCurrentUserId();

        // 3. 增加浏览量（用户未登录也记录匿名浏览）
        cacheDocViewService.view(CacheDocViewTypeEnum.NOTE.getCode(), id, userId);

        // 4. 构建笔记详情VO
        DocNoteHomeDetailVo vo = buildNoteHomeDetailVo(note);

        // 5. 查询并设置统计数据
        DocNoteHomeDetailStatsInfoVo stats = buildNoteStatsInfo(id, userId);
        vo.setStats(stats);

        // 6. 查询并设置作者信息（包含关注状态）
        DocNoteHomeDetailAuthorVo author = buildNoteAuthorInfo(note, userId);
        vo.setAuthor(author);

        return R.ok(vo);
    }

    /**
     * 获取笔记交互详情
     * <p>
     * 独立查询笔记的交互统计数据，包括浏览量、点赞数、收藏数、评论数以及当前用户的交互状态。
     * </p>
     *
     * @param id 笔记ID
     * @return 笔记交互详情
     */
    @Operation(summary = "获取笔记交互详情")
    @GetMapping("/interaction/{id}")
    public R<DocNoteHomeDetailStatsInfoVo> getInteraction(@PathVariable("id") Long id) {
        // 1. 验证笔记是否存在
        DocNote note = noteService.getById(id);
        if (note == null) {
            return R.fail("笔记不存在");
        }

        // 2. 获取当前登录用户ID
        Long userId = getCurrentUserId();

        // 3. 构建并返回交互统计信息
        DocNoteHomeDetailStatsInfoVo stats = buildNoteStatsInfo(id, userId);
        return R.ok(stats);
    }

    /**
     * 切换笔记点赞状态
     * <p>
     * 用户点赞或取消点赞笔记。
     * 先查询当前点赞状态，然后执行相反操作。
     * </p>
     *
     * @param id 笔记ID
     * @return 点赞操作结果
     */
    @Operation(summary = "切换笔记点赞状态")
    @PostMapping("/like/{id}")
    public R<InteractionResultVo> toggleLike(@PathVariable("id") Long id) {
        // 1. 获取当前登录用户ID
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }

        // 2. 查询当前点赞状态
        boolean isLiked = cacheDocLikeService.hasLiked(CacheDocLikeTypeEnum.NOTE.getCode(), id, userId);

        // 3. 执行相反操作
        if (isLiked) {
            cacheDocLikeService.unlike(CacheDocLikeTypeEnum.NOTE.getCode(), id, userId);
        } else {
            cacheDocLikeService.like(CacheDocLikeTypeEnum.NOTE.getCode(), id, userId);
        }

        // 4. 获取最新的点赞数量
        Long likeCount = cacheDocLikeService.getLikeCount(CacheDocLikeTypeEnum.NOTE.getCode(), id);

        // 5. 构建并返回结果
        InteractionResultVo result = InteractionResultVo.builder()
            .success(true)
            .status(!isLiked)
            .count(likeCount)
            .build();
        return R.ok(result);
    }

    /**
     * 切换笔记收藏状态
     * <p>
     * 用户收藏或取消收藏笔记。
     * 先查询当前收藏状态，然后执行相反操作。
     * </p>
     *
     * @param id 笔记ID
     * @return 收藏操作结果
     */
    @Operation(summary = "切换笔记收藏状态")
    @PostMapping("/favorite/{id}")
    public R<InteractionResultVo> toggleFavorite(@PathVariable("id") Long id) {
        // 1. 获取当前登录用户ID
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }

        // 2. 查询当前收藏状态
        boolean isFavorited = cacheDocCollectService.hasCollected(CacheDocCollectTypeEnum.NOTE.getCode(), id, userId);

        // 3. 执行相反操作
        if (isFavorited) {
            cacheDocCollectService.uncollect(CacheDocCollectTypeEnum.NOTE.getCode(), id, userId);
        } else {
            cacheDocCollectService.collect(CacheDocCollectTypeEnum.NOTE.getCode(), id, userId);
        }

        // 4. 获取最新的收藏数量
        Long collectCount = cacheDocCollectService.getCollectCount(CacheDocCollectTypeEnum.NOTE.getCode(), id);

        // 5. 构建并返回结果
        InteractionResultVo result = InteractionResultVo.builder()
            .success(true)
            .status(!isFavorited)
            .count(collectCount)
            .build();
        return R.ok(result);
    }

    /**
     * 切换关注作者状态
     * <p>
     * 用户关注或取消关注笔记作者。
     * </p>
     *
     * @param authorId 作者ID
     * @return 关注操作结果
     */
    @Operation(summary = "切换关注作者状态")
    @PostMapping("/follow/{authorId}")
    public R<InteractionResultVo> toggleFollow(@PathVariable("authorId") Long authorId) {
        // 1. 获取当前登录用户ID
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }

        // 2. 检查是否关注自己
        if (authorId.equals(userId)) {
            return R.fail("不能关注自己");
        }

        // 3. 查询当前关注状态
        boolean isFollowing = cacheDocFollowService.hasFollowed(CacheDocFollowTypeEnum.NOTE_AUTHOR.getCode(), authorId, userId);

        // 4. 执行相反操作
        if (isFollowing) {
            cacheDocFollowService.unfollow(CacheDocFollowTypeEnum.NOTE_AUTHOR.getCode(), authorId, userId);
        } else {
            cacheDocFollowService.follow(CacheDocFollowTypeEnum.NOTE_AUTHOR.getCode(), authorId, userId);
        }

        // 5. 获取最新的粉丝数量
        Long followCount = cacheDocFollowService.getFollowCount(CacheDocFollowTypeEnum.NOTE_AUTHOR.getCode(), authorId);

        // 6. 构建并返回结果
        InteractionResultVo result = InteractionResultVo.builder()
            .success(true)
            .status(!isFollowing)
            .count(followCount)
            .build();
        return R.ok(result);
    }

    /**
     * 获取笔记评论列表
     * <p>
     * 查询笔记的评论列表，支持热门排序和最新排序。
     * 使用通用分页组件 {@link PageQuery} 进行分页。
     * </p>
     *
     * @param id        笔记ID
     * @param pageQuery 分页查询参数
     * @param sort      排序方式（hot/new）
     * @return 评论分页列表
     */
    @Operation(summary = "获取笔记评论列表")
    @GetMapping("/comments/{id}")
    public R<PageResult<DocCommentVo>> getComments(
        @PathVariable("id") Long id,
        PageQuery pageQuery,
        @RequestParam(value = "sort", required = false) String sort) {

        // 1. 构建查询条件：查询未被删除的顶级评论
        LambdaQueryWrapper<DocNoteComment> wrapper = new LambdaQueryWrapper<DocNoteComment>()
            .eq(DocNoteComment::getDeleted, 0)
            .eq(DocNoteComment::getNoteId, id)
            .isNull(DocNoteComment::getParentCommentId);

        // 2. 根据排序参数设置排序方式
        if ("hot".equals(sort)) {
            wrapper.orderByDesc(DocNoteComment::getLikeCount);
        } else {
            wrapper.orderByDesc(DocNoteComment::getCreateTime);
        }

        // 3. 查询评论总数
        long total = commentService.count(wrapper);

        // 4. 应用分页参数
        wrapper.last("LIMIT " + pageQuery.getOffset() + ", " + pageQuery.getPageSize());

        // 5. 查询顶级评论列表
        List<DocNoteComment> comments = commentService.list(wrapper);

        // 6. 获取当前登录用户ID
        Long userId = getCurrentUserId();

        // 7. 构建评论VO列表
        List<DocCommentVo> commentVos = new ArrayList<>();
        for (DocNoteComment comment : comments) {
            // 7.1 构建当前评论VO
            DocCommentVo vo = buildCommentVo(comment, userId);

            // 7.2 查询该评论的回复列表
            List<DocNoteComment> replies = commentService.list(
                new LambdaQueryWrapper<DocNoteComment>()
                    .eq(DocNoteComment::getDeleted, 0)
                    .eq(DocNoteComment::getParentCommentId, comment.getId())
                    .orderByAsc(DocNoteComment::getCreateTime)
            );

            // 7.3 构建回复VO列表
            List<DocCommentVo> replyVos = new ArrayList<>();
            for (DocNoteComment reply : replies) {
                replyVos.add(buildCommentVo(reply, userId));
            }
            vo.setReplies(replyVos);

            commentVos.add(vo);
        }

        // 8. 构建分页结果
        PageResult<DocCommentVo> pageResult = PageResult.of(
            commentVos,
            total,
            pageQuery.getPageNum(),
            pageQuery.getPageSize()
        );

        return R.ok(pageResult);
    }

    /**
     * 发表笔记评论
     * <p>
     * 用户发表笔记评论，支持回复其他评论。
     * 评论成功后返回构建好的评论VO。
     * </p>
     *
     * @param commentRequest 评论请求参数
     * @return 评论结果
     */
    @Operation(summary = "发表笔记评论")
    @PostMapping("/comment")
    public R<DocCommentVo> postComment(@RequestBody CommentRequestVo commentRequest) {
        // 1. 获取当前登录用户ID
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }

        // 2. 获取请求参数
        String docId = commentRequest.getDocId();
        String content = commentRequest.getContent();
        String parentId = commentRequest.getParentId();

        // 3. 参数校验
        if (docId == null || docId.isEmpty()) {
            return R.fail("笔记ID不能为空");
        }
        if (content == null || content.isEmpty()) {
            return R.fail("评论内容不能为空");
        }

        // 4. 构建评论实体
        DocNoteComment comment = new DocNoteComment();
        comment.setNoteId(Long.parseLong(docId));
        comment.setCommentUserId(String.valueOf(userId));
        comment.setCommentContent(content);
        comment.setLikeCount(0L);

        // 5. 设置父评论ID（如果有）
        if (parentId != null && !parentId.isEmpty()) {
            comment.setParentCommentId(Long.parseLong(parentId));
        }

        // 7. 保存评论
        commentService.save(comment);

        // 8. 构建并返回评论VO
        return R.ok(buildCommentVo(comment, userId));
    }

    /**
     * 切换评论点赞状态
     * <p>
     * 用户点赞或取消点赞笔记评论。
     * </p>
     *
     * @param commentId 评论ID
     * @return 点赞操作结果
     */
    @Operation(summary = "切换评论点赞状态")
    @PostMapping("/comment/like/{commentId}")
    public R<InteractionResultVo> toggleCommentLike(@PathVariable("commentId") Long commentId) {
        // 1. 获取当前登录用户ID
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }

        // 2. 查询当前点赞状态
        boolean isLiked = cacheDocLikeService.hasLiked(CacheDocLikeTypeEnum.NOTE_COMMENT.getCode(), commentId, userId);

        // 3. 执行相反操作
        boolean result;
        if (isLiked) {
            result = cacheDocLikeService.unlike(CacheDocLikeTypeEnum.NOTE_COMMENT.getCode(), commentId, userId);
        } else {
            result = cacheDocLikeService.like(CacheDocLikeTypeEnum.NOTE_COMMENT.getCode(), commentId, userId);
        }

        // 4. 获取最新的点赞数量
        Long likeCount = cacheDocLikeService.getLikeCount(CacheDocLikeTypeEnum.NOTE_COMMENT.getCode(), commentId);

        // 5. 更新评论表的点赞数（用于排序）
        DocNoteComment comment = commentService.getById(commentId);
        if (comment != null) {
            comment.setLikeCount(likeCount);
            commentService.updateById(comment);
        }

        // 6. 构建并返回结果
        InteractionResultVo resultVo = InteractionResultVo.builder()
            .success(result)
            .status(result && !isLiked)
            .count(likeCount)
            .build();
        return R.ok(resultVo);
    }

    /**
     * 构建笔记首页详情VO
     * <p>
     * 将笔记实体转换为笔记首页详情VO，包含基本信息。
     * </p>
     *
     * @param note 笔记实体
     * @return 笔记首页详情VO
     */
    private DocNoteHomeDetailVo buildNoteHomeDetailVo(DocNote note) {
        DocNoteHomeDetailVo vo = new DocNoteHomeDetailVo();
        vo.setId(String.valueOf(note.getId()));
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
    private DocNoteHomeDetailStatsInfoVo buildNoteStatsInfo(Long noteId, Long userId) {
        DocNoteHomeDetailStatsInfoVo stats = new DocNoteHomeDetailStatsInfoVo();

        // 1. 获取浏览量
        Long viewCount = cacheDocViewService.getViewCount(CacheDocViewTypeEnum.NOTE.getCode(), noteId);
        stats.setViews(String.valueOf(viewCount));

        // 2. 获取点赞数
        Long likeCount = cacheDocLikeService.getLikeCount(CacheDocLikeTypeEnum.NOTE.getCode(), noteId);
        stats.setLikes(likeCount.intValue());

        // 3. 获取收藏数
        Long collectCount = cacheDocCollectService.getCollectCount(CacheDocCollectTypeEnum.NOTE.getCode(), noteId);
        stats.setFavorites(collectCount.intValue());

        // 4. 获取评论数
        Long commentCount = commentService.count(
            new LambdaQueryWrapper<DocNoteComment>()
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
     * @param note   笔记实体
     * @param userId 当前用户ID（可为null）
     * @return 笔记作者信息VO
     */
    private DocNoteHomeDetailAuthorVo buildNoteAuthorInfo(DocNote note, Long userId) {
        DocNoteHomeDetailAuthorVo author = new DocNoteHomeDetailAuthorVo();
        author.setId(String.valueOf(note.getUserId()));
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
     * @param comment       评论实体
     * @param currentUserId 当前登录用户ID
     * @return 评论VO
     */
    private DocCommentVo buildCommentVo(DocNoteComment comment, Long currentUserId) {
        DocCommentVo vo = new DocCommentVo();
        vo.setId(String.valueOf(comment.getId()));
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
            DocNoteComment parentComment = commentService.getById(comment.getParentCommentId());
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

    /**
     * 获取当前用户ID
     * <p>
     * 尝试从安全工具类获取当前登录用户ID，如果获取失败则返回null。
     * </p>
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
