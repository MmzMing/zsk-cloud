package com.zsk.document.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zsk.common.core.domain.R;
import com.zsk.common.security.utils.SecurityUtils;
import com.zsk.document.domain.DocNote;
import com.zsk.document.domain.DocNoteComment;
import com.zsk.document.domain.vo.DocCommentVo;
import com.zsk.document.domain.vo.DocNoteDetailVo;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        DocNote note = noteService.getById(id);
        if (note == null) {
            return R.fail("文档不存在");
        }

        note.setViewCount(note.getViewCount() != null ? note.getViewCount() + 1 : 1L);
        noteService.updateById(note);

        DocNoteDetailVo vo = buildNoteDetailVo(note);

        Long userId = getCurrentUserId();

        if (userId != null) {
            boolean isLiked = cacheDocLikeService.hasLiked(CacheDocLikeTypeEnum.NOTE.getCode(), id, userId);
            vo.getStats().setIsLiked(isLiked);

            boolean isFavorited = cacheDocCollectService.hasCollected(CacheDocCollectTypeEnum.NOTE.getCode(), id, userId);
            vo.getStats().setIsFavorited(isFavorited);

            if (vo.getAuthor() != null && vo.getAuthor().getId() != null) {
                boolean isFollowing = cacheDocFollowService.hasFollowed(
                    CacheDocFollowTypeEnum.NOTE_AUTHOR.getCode(), Long.parseLong(vo.getAuthor().getId()), userId);
                vo.getAuthor().setIsFollowing(isFollowing);
            }
        } else {
            vo.getStats().setIsLiked(false);
            vo.getStats().setIsFavorited(false);
            if (vo.getAuthor() != null) {
                vo.getAuthor().setIsFollowing(false);
            }
        }

        Long likeCount = cacheDocLikeService.getLikeCount(CacheDocLikeTypeEnum.NOTE.getCode(), id);
        vo.getStats().setLikes(likeCount.intValue());

        Long collectCount = cacheDocCollectService.getCollectCount(CacheDocCollectTypeEnum.NOTE.getCode(), id);
        vo.getStats().setFavorites(collectCount.intValue());

        List<DocNote> recommendNotes = noteService.list(
            new LambdaQueryWrapper<DocNote>()
                .eq(DocNote::getDeleted, 0)
                .eq(DocNote::getStatus, 1)
                .eq(DocNote::getIsRecommended, 1)
                .ne(DocNote::getId, id)
                .orderByDesc(DocNote::getViewCount)
                .last("LIMIT 5")
        );
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
     *
     * @param id 文档ID
     * @return 点赞结果
     */
    @Operation(summary = "切换文档点赞状态")
    @PostMapping("/like/{id}")
    public R<Map<String, Object>> toggleLike(@PathVariable("id") Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }

        boolean isLiked = cacheDocLikeService.hasLiked(CacheDocLikeTypeEnum.NOTE.getCode(), id, userId);
        boolean result;
        if (isLiked) {
            result = cacheDocLikeService.unlike(CacheDocLikeTypeEnum.NOTE.getCode(), id, userId);
        } else {
            result = cacheDocLikeService.like(CacheDocLikeTypeEnum.NOTE.getCode(), id, userId);
        }

        Long likeCount = cacheDocLikeService.getLikeCount(CacheDocLikeTypeEnum.NOTE.getCode(), id);

        DocNote note = noteService.getById(id);
        if (note != null) {
            note.setLikeCount(likeCount);
            noteService.updateById(note);
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("isLiked", result && !isLiked);
        resultMap.put("count", likeCount);
        return R.ok(resultMap);
    }

    /**
     * 切换文档收藏状态
     *
     * @param id 文档ID
     * @return 收藏结果
     */
    @Operation(summary = "切换文档收藏状态")
    @PostMapping("/favorite/{id}")
    public R<Map<String, Object>> toggleFavorite(@PathVariable("id") Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }

        boolean isFavorited = cacheDocCollectService.hasCollected(CacheDocCollectTypeEnum.NOTE.getCode(), id, userId);
        boolean result;
        if (isFavorited) {
            result = cacheDocCollectService.uncollect(CacheDocCollectTypeEnum.NOTE.getCode(), id, userId);
        } else {
            result = cacheDocCollectService.collect(CacheDocCollectTypeEnum.NOTE.getCode(), id, userId);
        }

        Long collectCount = cacheDocCollectService.getCollectCount(CacheDocCollectTypeEnum.NOTE.getCode(), id);

        DocNote note = noteService.getById(id);
        if (note != null) {
            note.setCollectCount(collectCount);
            noteService.updateById(note);
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("isFavorited", result && !isFavorited);
        resultMap.put("count", collectCount);
        return R.ok(resultMap);
    }

    /**
     * 获取文档评论列表
     *
     * @param id 文档ID
     * @param page 页码
     * @param pageSize 每页数量
     * @param sort 排序方式（hot/new）
     * @return 评论列表
     */
    @Operation(summary = "获取文档评论列表")
    @GetMapping("/comments/{id}")
    public R<Map<String, Object>> getComments(
        @PathVariable("id") Long id,
        @RequestParam(value = "page", defaultValue = "1") Integer page,
        @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
        @RequestParam(value = "sort", required = false) String sort) {

        LambdaQueryWrapper<DocNoteComment> wrapper = new LambdaQueryWrapper<DocNoteComment>()
            .eq(DocNoteComment::getDeleted, 0)
            .eq(DocNoteComment::getNoteId, id)
            .isNull(DocNoteComment::getParentCommentId);

        if ("hot".equals(sort)) {
            wrapper.orderByDesc(DocNoteComment::getLikeCount);
        } else {
            wrapper.orderByDesc(DocNoteComment::getCreateTime);
        }

        List<DocNoteComment> comments = commentService.list(wrapper);

        Long userId = getCurrentUserId();
        List<DocCommentVo> commentVos = new ArrayList<>();
        for (DocNoteComment comment : comments) {
            DocCommentVo vo = buildCommentVo(comment, userId);
            List<DocNoteComment> replies = commentService.list(
                new LambdaQueryWrapper<DocNoteComment>()
                    .eq(DocNoteComment::getDeleted, 0)
                    .eq(DocNoteComment::getParentCommentId, comment.getId())
                    .orderByAsc(DocNoteComment::getCreateTime)
            );
            List<DocCommentVo> replyVos = new ArrayList<>();
            for (DocNoteComment reply : replies) {
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
     * 发表文档评论
     *
     * @param params 评论参数
     * @return 评论结果
     */
    @Operation(summary = "发表文档评论")
    @PostMapping("/comment")
    public R<DocCommentVo> postComment(@RequestBody Map<String, Object> params) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }

        String docId = (String) params.get("docId");
        String content = (String) params.get("content");
        String parentId = (String) params.get("parentId");
        String replyToId = (String) params.get("replyToId");

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
     * @return 点赞结果
     */
    @Operation(summary = "切换评论点赞状态")
    @PostMapping("/comment/like/{commentId}")
    public R<Map<String, Object>> toggleCommentLike(@PathVariable("commentId") Long commentId) {
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

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("isLiked", result && !isLiked);
        resultMap.put("likes", likeCount);
        return R.ok(resultMap);
    }

    /**
     * 构建文档详情VO
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
     */
    private Long getCurrentUserId() {
        try {
            return SecurityUtils.getUserId();
        } catch (Exception e) {
            return null;
        }
    }
}
