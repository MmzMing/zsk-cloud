package com.zsk.document.controller;

import com.zsk.common.core.domain.R;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.common.security.utils.SecurityUtils;
import com.zsk.document.domain.dto.CommentRequestDTO;
import com.zsk.document.domain.vo.DocCommentVo;
import com.zsk.document.domain.vo.DocNoteHomeDetailVo;
import com.zsk.document.domain.vo.DocStatsInfoVo;
import com.zsk.document.domain.vo.InteractionResultVo;
import com.zsk.document.service.IDocNoteHomeDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 前台笔记首页详情控制器
 * <p>
 * 提供笔记详情查询、交互操作（点赞、收藏、关注）、评论管理等功能。
 * 所有交互数据（浏览量、点赞数、收藏数）均通过 Redis 缓存服务获取，不再依赖主表字段。
 * </p>
 *
 * @author wuhuaming
 * @version 2.0
 * @date 2026-04-25
 */
@Slf4j
@Tag(name = "前台笔记首页详情")
@RestController
@RequestMapping("/docNoteHomeDetail")
@RequiredArgsConstructor
public class DocNoteHomeDetailController {

    /**
     * 笔记首页详情服务
     */
    private final IDocNoteHomeDetailService noteHomeDetailService;

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
        log.info("获取笔记详情请求, id={}", id);

        // 获取当前登录用户ID
        Long userId = getCurrentUserId();

        // 调用Service层获取笔记详情
        DocNoteHomeDetailVo vo = noteHomeDetailService.getNoteDetail(id, userId);
        if (vo == null) {
            return R.fail("笔记不存在");
        }

        log.info("获取笔记详情成功, id={}", id);
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
    public R<DocStatsInfoVo> getInteraction(@PathVariable("id") Long id) {
        log.info("获取笔记交互详情请求, id={}", id);

        // 获取当前登录用户ID
        Long userId = getCurrentUserId();

        // 调用Service层获取交互详情
        DocStatsInfoVo stats = noteHomeDetailService.getNoteInteraction(id, userId);
        if (stats == null) {
            return R.fail("笔记不存在");
        }

        log.info("获取笔记交互详情成功, id={}", id);
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
        log.info("切换笔记点赞状态请求, id={}", id);

        // 获取当前登录用户ID
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }

        // 调用Service层执行点赞操作
        InteractionResultVo result = noteHomeDetailService.toggleNoteLike(id, userId);

        log.info("切换笔记点赞状态成功, id={}, status={}", id, result.isStatus());
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
        log.info("切换笔记收藏状态请求, id={}", id);

        // 获取当前登录用户ID
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }

        // 调用Service层执行收藏操作
        InteractionResultVo result = noteHomeDetailService.toggleNoteFavorite(id, userId);

        log.info("切换笔记收藏状态成功, id={}, status={}", id, result.isStatus());
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
        log.info("切换关注作者状态请求, authorId={}", authorId);

        // 获取当前登录用户ID
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }

        // 检查是否关注自己
        if (authorId.equals(userId)) {
            return R.fail("不能关注自己");
        }

        // 调用Service层执行关注操作
        InteractionResultVo result = noteHomeDetailService.toggleFollowAuthor(authorId, userId);

        log.info("切换关注作者状态成功, authorId={}, status={}", authorId, result.isStatus());
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
        log.info("获取笔记评论列表请求, id={}, pageQuery={}, sort={}", id, pageQuery, sort);

        // 获取当前登录用户ID
        Long userId = getCurrentUserId();

        // 调用Service层获取评论列表
        PageResult<DocCommentVo> pageResult = noteHomeDetailService.getNoteComments(id, pageQuery, sort, userId);

        log.info("获取笔记评论列表成功, id={}, total={}", id, pageResult.getTotal());
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
    public R<DocCommentVo> postComment(@RequestBody CommentRequestDTO commentRequest) {
        log.info("发表笔记评论请求, docId={}", commentRequest.getDocId());

        // 获取当前登录用户ID
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }

        // 获取请求参数
        Long docId = commentRequest.getDocId();
        String content = commentRequest.getContent();
        Long parentId = commentRequest.getParentId();

        // 参数校验
        if (docId == null) {
            return R.fail("笔记ID不能为空");
        }
        if (content == null || content.isEmpty()) {
            return R.fail("评论内容不能为空");
        }

        // 调用Service层发表评论
        DocCommentVo commentVo = noteHomeDetailService.postComment(docId, content, parentId, userId);

        log.info("发表笔记评论成功, commentId={}", commentVo.getId());
        return R.ok(commentVo);
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
        log.info("切换评论点赞状态请求, commentId={}", commentId);

        // 获取当前登录用户ID
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }

        // 调用Service层执行评论点赞操作
        InteractionResultVo result = noteHomeDetailService.toggleCommentLike(commentId, userId);

        log.info("切换评论点赞状态成功, commentId={}, status={}", commentId, result.isStatus());
        return R.ok(result);
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
            log.warn("获取当前用户ID失败", e);
            return null;
        }
    }
}