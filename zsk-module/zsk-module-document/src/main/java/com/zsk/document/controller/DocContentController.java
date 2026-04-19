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
import com.zsk.document.domain.vo.DocNoteDetailVo;
import com.zsk.document.domain.vo.InteractionResultVo;
import com.zsk.document.domain.vo.UserStatsVo;
import com.zsk.document.enums.CacheDocCollectTypeEnum;
import com.zsk.document.enums.CacheDocFollowTypeEnum;
import com.zsk.document.enums.CacheDocLikeTypeEnum;
import com.zsk.document.service.ICacheDocCollectService;
import com.zsk.document.service.ICacheDocFollowService;
import com.zsk.document.service.ICacheDocLikeService;
import com.zsk.document.service.IDocNoteCommentService;
import com.zsk.document.service.IDocNoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 前台文档详情 控制器
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Tag(name = "前台文档详情")
@RestController
@RequestMapping("/content/doc")
@RequiredArgsConstructor
public class DocContentController {

    private final IDocNoteService noteService;
    private final IDocNoteCommentService commentService;
    private final ICacheDocLikeService cacheDocLikeService;
    private final ICacheDocCollectService cacheDocCollectService;
    private final ICacheDocFollowService cacheDocFollowService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取文档详情
     *
     * @param id 文档ID
     * @return 文档详情
     */
    @Operation(summary = "获取文档详情")
    @GetMapping("/detail/{id}")
    public R<DocNoteDetailVo> getDetail(@PathVariable("id") Long id) {
        // 根据ID查询文档
        DocNote note = noteService.getById(id);
        if (note == null) {
            return R.fail("文档不存在");
        }

        // 增加文档浏览次数
        note.setViewCount(note.getViewCount() != null ? note.getViewCount() + 1 : 1L);
        noteService.updateById(note);

        // 构建文档详情VO
        DocNoteDetailVo vo = buildNoteDetailVo(note);

        // 获取当前登录用户ID
        Long userId = getCurrentUserId();

        // 如果用户已登录，查询用户的点赞、收藏和关注状态
        if (userId != null) {
            // 查询用户是否点赞
            boolean isLiked = cacheDocLikeService.hasLiked(CacheDocLikeTypeEnum.NOTE.getCode(), id, userId);
            vo.getStats().setIsLiked(isLiked);

            // 查询用户是否收藏
            boolean isFavorited = cacheDocCollectService.hasCollected(CacheDocCollectTypeEnum.NOTE.getCode(), id, userId);
            vo.getStats().setIsFavorited(isFavorited);

            // 查询用户是否关注作者
            if (vo.getAuthor() != null && vo.getAuthor().getId() != null) {
                boolean isFollowing = cacheDocFollowService.hasFollowed(
                    CacheDocFollowTypeEnum.NOTE_AUTHOR.getCode(), Long.parseLong(vo.getAuthor().getId()), userId);
                vo.getAuthor().setIsFollowing(isFollowing);
            }
        } else {
            // 未登录用户，默认设置为未点赞、未收藏、未关注
            vo.getStats().setIsLiked(false);
            vo.getStats().setIsFavorited(false);
            if (vo.getAuthor() != null) {
                vo.getAuthor().setIsFollowing(false);
            }
        }

        // 获取文档点赞总数
        Long likeCount = cacheDocLikeService.getLikeCount(CacheDocLikeTypeEnum.NOTE.getCode(), id);
        vo.getStats().setLikes(likeCount.intValue());

        // 获取文档收藏总数
        Long collectCount = cacheDocCollectService.getCollectCount(CacheDocCollectTypeEnum.NOTE.getCode(), id);
        vo.getStats().setFavorites(collectCount.intValue());

        // 查询推荐文档列表（取浏览量最高的5篇推荐文档）
        List<DocNote> recommendNotes = noteService.list(
            new LambdaQueryWrapper<DocNote>()
                .eq(DocNote::getDeleted, 0)
                .eq(DocNote::getStatus, 1)
                .eq(DocNote::getIsRecommended, 1)
                .ne(DocNote::getId, id)
                .orderByDesc(DocNote::getViewCount)
                .last("LIMIT 5")
        );
        
        // 构建推荐文档列表
        List<DocNoteDetailVo.RecommendDoc> recommendations = new ArrayList<>();
        for (DocNote recommend : recommendNotes) {
            DocNoteDetailVo.RecommendDoc rec = new DocNoteDetailVo.RecommendDoc();
            rec.setId(String.valueOf(recommend.getId()));
            rec.setTitle(recommend.getNoteName());
            rec.setViews(String.valueOf(recommend.getViewCount() != null ? recommend.getViewCount() : 0));
            recommendations.add(rec);
        }
        vo.setRecommendations(recommendations);

        return R.ok(vo);
    }

    /**
     * 切换文档点赞状态
     * 先查询当前点赞状态，然后执行相反操作
     *
     * @param id 文档ID
     * @return 点赞操作结果
     */
    @Operation(summary = "切换文档点赞状态")
    @PostMapping("/like/{id}")
    public R<InteractionResultVo> toggleLike(@PathVariable("id") Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }

        boolean isLiked = cacheDocLikeService.hasLiked(CacheDocLikeTypeEnum.NOTE.getCode(), id, userId);
        if (isLiked) {
            cacheDocLikeService.unlike(CacheDocLikeTypeEnum.NOTE.getCode(), id, userId);
        } else {
            cacheDocLikeService.like(CacheDocLikeTypeEnum.NOTE.getCode(), id, userId);
        }

        Long likeCount = cacheDocLikeService.getLikeCount(CacheDocLikeTypeEnum.NOTE.getCode(), id);

        DocNote note = noteService.getById(id);
        if (note != null) {
            note.setLikeCount(likeCount);
            noteService.updateById(note);
        }

        InteractionResultVo result = InteractionResultVo.builder()
                .success(true)
                .status(!isLiked)
                .count(likeCount)
                .build();
        return R.ok(result);
    }

    /**
     * 切换文档收藏状态
     * 先查询当前收藏状态，然后执行相反操作
     *
     * @param id 文档ID
     * @return 收藏操作结果
     */
    @Operation(summary = "切换文档收藏状态")
    @PostMapping("/favorite/{id}")
    public R<InteractionResultVo> toggleFavorite(@PathVariable("id") Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }

        boolean isFavorited = cacheDocCollectService.hasCollected(CacheDocCollectTypeEnum.NOTE.getCode(), id, userId);
        if (isFavorited) {
            cacheDocCollectService.uncollect(CacheDocCollectTypeEnum.NOTE.getCode(), id, userId);
        } else {
            cacheDocCollectService.collect(CacheDocCollectTypeEnum.NOTE.getCode(), id, userId);
        }

        Long collectCount = cacheDocCollectService.getCollectCount(CacheDocCollectTypeEnum.NOTE.getCode(), id);

        DocNote note = noteService.getById(id);
        if (note != null) {
            note.setCollectCount(collectCount);
            noteService.updateById(note);
        }

        InteractionResultVo result = InteractionResultVo.builder()
                .success(true)
                .status(!isFavorited)
                .count(collectCount)
                .build();
        return R.ok(result);
    }

    /**
     * 获取文档评论列表
     *
     * @param id 文档ID
     * @param pageQuery 分页查询参数
     * @param sort 排序方式（hot/new）
     * @return 评论列表
     */
    @Operation(summary = "获取文档评论列表")
    @GetMapping("/comments/{id}")
    public R<PageResult<DocCommentVo>> getComments(
        @PathVariable("id") Long id,
        PageQuery pageQuery,
        @RequestParam(value = "sort", required = false) String sort) {

        // 构建查询条件：查询未被删除的顶级评论（parentCommentId为空的评论）
        LambdaQueryWrapper<DocNoteComment> wrapper = new LambdaQueryWrapper<DocNoteComment>()
            .eq(DocNoteComment::getDeleted, 0)
            .eq(DocNoteComment::getNoteId, id)
            .isNull(DocNoteComment::getParentCommentId);

        // 根据排序参数设置排序方式：热门（按点赞数）或最新（按创建时间）
        if ("hot".equals(sort)) {
            wrapper.orderByDesc(DocNoteComment::getLikeCount);
        } else {
            wrapper.orderByDesc(DocNoteComment::getCreateTime);
        }

        // 查询总数
        long total = commentService.count(wrapper);
        
        // 应用分页
        wrapper.last("LIMIT " + pageQuery.getOffset() + ", " + pageQuery.getPageSize());
        
        // 查询顶级评论列表
        List<DocNoteComment> comments = commentService.list(wrapper);

        // 获取当前登录用户ID，用于判断用户是否点赞了某条评论
        Long userId = getCurrentUserId();
        List<DocCommentVo> commentVos = new ArrayList<>();
        
        // 遍历每条顶级评论，构建评论VO对象
        for (DocNoteComment comment : comments) {
            // 构建当前评论的VO对象
            DocCommentVo vo = buildCommentVo(comment, userId);
            
            // 查询该评论的所有回复（子评论）
            List<DocNoteComment> replies = commentService.list(
                new LambdaQueryWrapper<DocNoteComment>()
                    .eq(DocNoteComment::getDeleted, 0)
                    .eq(DocNoteComment::getParentCommentId, comment.getId())
                    .orderByAsc(DocNoteComment::getCreateTime)
            );
            
            // 构建回复列表的VO对象
            List<DocCommentVo> replyVos = new ArrayList<>();
            for (DocNoteComment reply : replies) {
                replyVos.add(buildCommentVo(reply, userId));
            }
            
            // 将回复列表设置到评论VO中
            vo.setReplies(replyVos);
            commentVos.add(vo);
        }

        // 构建分页结果
        PageResult<DocCommentVo> pageResult = PageResult.of(
            commentVos, 
            total, 
            pageQuery.getPageNum(), 
            pageQuery.getPageSize()
        );
        
        return R.ok(pageResult);
    }

    /**
     * 发表文档评论
     *
     * @param commentRequest 评论请求参数
     * @return 评论结果
     */
    @Operation(summary = "发表文档评论")
    @PostMapping("/comment")
    public R<DocCommentVo> postComment(@RequestBody CommentRequestVo commentRequest) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }

        String docId = commentRequest.getDocId();
        String content = commentRequest.getContent();
        String parentId = commentRequest.getParentId();
        String replyToId = commentRequest.getReplyToId();

        DocNoteComment comment = new DocNoteComment();
        comment.setNoteId(Long.parseLong(docId));
        comment.setCommentUserId(String.valueOf(userId));
        comment.setCommentContent(content);
        comment.setLikeCount(0L);
        if (parentId != null && !parentId.isEmpty()) {
            comment.setParentCommentId(Long.parseLong(parentId));
        }
        commentService.save(comment);

        DocNote note = noteService.getById(Long.parseLong(docId));
        if (note != null) {
            Long commentCount = commentService.count(
                new LambdaQueryWrapper<DocNoteComment>()
                    .eq(DocNoteComment::getDeleted, 0)
                    .eq(DocNoteComment::getNoteId, docId)
            );
            note.setCommentCount(commentCount);
            noteService.updateById(note);
        }

        return R.ok(buildCommentVo(comment, userId));
    }

    /**
     * 切换评论点赞状态
     *
     * @param commentId 评论ID
     * @return 评论点赞操作结果
     */
    @Operation(summary = "切换评论点赞状态")
    @PostMapping("/comment/like/{commentId}")
    public R<InteractionResultVo> toggleCommentLike(@PathVariable("commentId") Long commentId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }

        boolean isLiked = cacheDocLikeService.hasLiked(CacheDocLikeTypeEnum.NOTE_COMMENT.getCode(), commentId, userId);
        boolean result;
        if (isLiked) {
            result = cacheDocLikeService.unlike(CacheDocLikeTypeEnum.NOTE_COMMENT.getCode(), commentId, userId);
        } else {
            result = cacheDocLikeService.like(CacheDocLikeTypeEnum.NOTE_COMMENT.getCode(), commentId, userId);
        }

        Long likeCount = cacheDocLikeService.getLikeCount(CacheDocLikeTypeEnum.NOTE_COMMENT.getCode(), commentId);

        DocNoteComment comment = commentService.getById(commentId);
        if (comment != null) {
            comment.setLikeCount(likeCount);
            commentService.updateById(comment);
        }

        InteractionResultVo resultVo = InteractionResultVo.builder()
                .success(result)
                .status(result && !isLiked)
                .count(likeCount)
                .build();
        return R.ok(resultVo);
    }

    /**
     * 构建文档详情VO
     *
     * @param note 文档实体
     * @return 文档详情VO对象
     */
    private DocNoteDetailVo buildNoteDetailVo(DocNote note) {
        DocNoteDetailVo vo = new DocNoteDetailVo();
        vo.setId(String.valueOf(note.getId()));
        vo.setTitle(note.getNoteName());
        vo.setContent(note.getContent());
        vo.setCategory(note.getBroadCode());
        vo.setDate(note.getCreateTime() != null ? note.getCreateTime().format(DATE_FORMATTER) : "");
        vo.setCoverUrl(note.getCover());

        DocNoteDetailVo.AuthorInfo author = new DocNoteDetailVo.AuthorInfo();
        author.setId(String.valueOf(note.getUserId()));
        author.setName("作者" + note.getUserId());
        author.setAvatar("");
        author.setFans("0");
        author.setIsFollowing(false);
        vo.setAuthor(author);

        DocNoteDetailVo.StatsInfo stats = new DocNoteDetailVo.StatsInfo();
        stats.setViews(String.valueOf(note.getViewCount() != null ? note.getViewCount() : 0));
        stats.setLikes(note.getLikeCount() != null ? note.getLikeCount().intValue() : 0);
        stats.setFavorites(0);
        stats.setIsLiked(false);
        stats.setIsFavorited(false);
        vo.setStats(stats);

        return vo;
    }

    /**
     * 构建评论VO
     *
     * @param comment 评论实体
     * @param currentUserId 当前登录用户ID
     * @return 评论VO对象
     */
    private DocCommentVo buildCommentVo(DocNoteComment comment, Long currentUserId) {
        DocCommentVo vo = new DocCommentVo();
        vo.setId(String.valueOf(comment.getId()));
        vo.setContent(comment.getCommentContent());
        vo.setCreatedAt(comment.getCreateTime() != null ? comment.getCreateTime().format(DATE_FORMATTER) : "");

        Long likeCount = cacheDocLikeService.getLikeCount(CacheDocLikeTypeEnum.NOTE_COMMENT.getCode(), comment.getId());
        vo.setLikes(likeCount.intValue());

        DocCommentVo.AuthorInfo author = new DocCommentVo.AuthorInfo();
        author.setId(comment.getCommentUserId());
        author.setName("用户" + comment.getCommentUserId());
        author.setAvatar("");
        vo.setAuthor(author);

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

        UserStatsVo statsVo = UserStatsVo.builder()
                .userId(userId)
                .likeCount(likeCount)
                .fanCount(followCount)
                .collectCount(collectCount)
                .build();

        return R.ok(statsVo);
    }
}
