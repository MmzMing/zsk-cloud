package com.zsk.document.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsk.common.core.domain.R;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.common.security.utils.SecurityUtils;
import com.zsk.document.domain.DocNoteComment;
import com.zsk.document.domain.dto.CommentRequestDTO;
import com.zsk.document.domain.vo.DocCommentVo;
import com.zsk.document.domain.vo.InteractionResultVo;
import com.zsk.document.service.IDocNoteCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 笔记评论Controller
 * <p>
 * 提供笔记评论的增删改查、分页查询、评论列表获取、发表评论、评论点赞等功能。
 * 采用B站式评论结构：所有回复统一挂在根评论下，不存在层级嵌套。
 * 评论点赞数从Redis获取，不再存储在数据库表中。
 * </p>
 * <p>
 * 本层仅负责接收HTTP请求、参数校验、调用Service层和封装返回结果，
 * 所有业务逻辑已下沉到 {@link IDocNoteCommentService} 中。
 * </p>
 *
 * @author wuhuaming
 * @version 3.0
 * @date 2026-04-27
 */
@Slf4j
@Tag(name = "笔记评论管理")
@RestController
@RequestMapping("/docNoteComment")
@RequiredArgsConstructor
public class DocNoteCommentController {

    /**
     * 笔记评论服务
     */
    private final IDocNoteCommentService docNoteCommentService;

    /**
     * 查询笔记评论列表
     *
     * @param docNoteComment 查询条件
     * @return 评论列表
     */
    @Operation(summary = "查询笔记评论列表")
    @GetMapping("/list")
    public R<List<DocNoteComment>> list(DocNoteComment docNoteComment) {
        LambdaQueryWrapper<DocNoteComment> lqw = new LambdaQueryWrapper<>(docNoteComment);
        return R.ok(docNoteCommentService.list(lqw));
    }

    /**
     * 分页查询笔记评论列表
     *
     * @param docNoteComment 查询条件
     * @param pageQuery      分页参数
     * @return 分页评论列表
     */
    @Operation(summary = "分页查询笔记评论列表")
    @GetMapping("/page")
    public R<PageResult<DocNoteComment>> page(DocNoteComment docNoteComment, PageQuery pageQuery) {
        Page<DocNoteComment> page = pageQuery.build();
        LambdaQueryWrapper<DocNoteComment> lqw = new LambdaQueryWrapper<>(docNoteComment);
        return R.ok(PageResult.build(docNoteCommentService.page(page, lqw)));
    }

    /**
     * 获取笔记评论详细信息
     *
     * @param id 评论ID
     * @return 评论详情
     */
    @Operation(summary = "获取笔记评论详细信息")
    @GetMapping(value = "/{id}")
    public R<DocNoteComment> getInfo(@PathVariable("id") Long id) {
        return R.ok(docNoteCommentService.getById(id));
    }

    /**
     * 新增笔记评论
     *
     * @param docNoteComment 评论实体
     * @return 是否成功
     */
    @Operation(summary = "新增笔记评论")
    @PostMapping
    public R<Boolean> add(@RequestBody DocNoteComment docNoteComment) {
        return R.ok(docNoteCommentService.save(docNoteComment));
    }

    /**
     * 修改笔记评论
     *
     * @param docNoteComment 评论实体
     * @return 是否成功
     */
    @Operation(summary = "修改笔记评论")
    @PutMapping
    public R<Boolean> edit(@RequestBody DocNoteComment docNoteComment) {
        return R.ok(docNoteCommentService.updateById(docNoteComment));
    }

    /**
     * 删除笔记评论
     *
     * @param ids 评论ID列表
     * @return 是否成功
     */
    @Operation(summary = "删除笔记评论")
    @DeleteMapping("/{ids}")
    public R<Boolean> remove(@PathVariable List<Long> ids) {
        return R.ok(docNoteCommentService.removeBatchByIds(ids));
    }

    /**
     * 获取笔记评论列表（支持热门/最新排序）
     * <p>
     * 查询笔记的根评论列表，每个根评论包含其下的所有回复。
     * 采用B站式评论结构：所有回复统一挂在根评论下，不存在层级嵌套。
     * </p>
     *
     * @param noteId    笔记ID
     * @param pageQuery 分页查询参数
     * @param sort      排序方式（hot/new）
     * @return 评论分页列表
     */
    @Operation(summary = "获取笔记评论列表（前台）")
    @GetMapping("/comments/{noteId}")
    public R<PageResult<DocCommentVo>> getComments(
            @PathVariable("noteId") Long noteId,
            PageQuery pageQuery,
            @RequestParam(value = "sort", required = false) String sort) {
        log.info("获取笔记评论列表请求, noteId={}, pageQuery={}, sort={}", noteId, pageQuery, sort);

        // 获取当前登录用户ID并调用Service层
        Long userId = getCurrentUserId();
        PageResult<DocCommentVo> pageResult = docNoteCommentService.getCommentList(noteId, pageQuery, sort, userId);

        log.info("获取笔记评论列表成功, noteId={}, total={}", noteId, pageResult.getTotal());
        return R.ok(pageResult);
    }

    /**
     * 发表笔记评论
     * <p>
     * 用户发表笔记评论，支持回复其他评论。
     * 采用B站式评论结构：parentCommentId统一记录根评论ID，replyUserId记录被回复的用户ID。
     * </p>
     *
     * @param commentRequest 评论请求参数
     * @return 评论结果
     */
    @Operation(summary = "发表笔记评论（前台）")
    @PostMapping("/comment")
    public R<DocCommentVo> postComment(@RequestBody CommentRequestDTO commentRequest) {
        log.info("发表笔记评论请求, docId={}", commentRequest.getDocId());

        // 获取当前登录用户ID
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }

        // 调用Service层处理业务逻辑（包含参数校验）
        DocCommentVo commentVo = docNoteCommentService.postCommentWithValidation(commentRequest, userId);

        log.info("发表笔记评论成功, commentId={}", commentVo.getId());
        return R.ok(commentVo);
    }

    /**
     * 切换评论点赞状态
     * <p>
     * 用户点赞或取消点赞笔记评论。点赞数从Redis获取，不再同步到数据库。
     * </p>
     *
     * @param commentId 评论ID
     * @return 点赞操作结果
     */
    @Operation(summary = "切换评论点赞状态（前台）")
    @PostMapping("/comment/like/{commentId}")
    public R<InteractionResultVo> toggleCommentLike(@PathVariable("commentId") Long commentId) {
        log.info("切换评论点赞状态请求, commentId={}", commentId);

        // 获取当前登录用户ID
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }

        // 调用Service层处理业务逻辑
        InteractionResultVo result = docNoteCommentService.toggleCommentLike(commentId, userId);

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
