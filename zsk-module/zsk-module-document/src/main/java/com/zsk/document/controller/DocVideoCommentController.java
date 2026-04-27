package com.zsk.document.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsk.common.core.domain.R;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.common.security.utils.SecurityUtils;
import com.zsk.document.domain.DocVideoComment;
import com.zsk.document.domain.dto.VideoCommentRequestDTO;
import com.zsk.document.domain.vo.DocVideoCommentVo;
import com.zsk.document.domain.vo.InteractionResultVo;
import com.zsk.document.service.IDocVideoCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 视频详情评论Controller
 * <p>
 * 提供视频评论的增删改查、分页查询、评论列表获取、发表评论、评论点赞等功能。
 * 采用B站式评论结构：所有回复统一挂在根评论下，不存在层级嵌套。
 * 评论点赞数从Redis获取，不再存储在数据库表中。
 * </p>
 * <p>
 * 本层仅负责接收HTTP请求、参数校验、调用Service层和封装返回结果，
 * 所有业务逻辑已下沉到 {@link IDocVideoCommentService} 中。
 * </p>
 *
 * @author wuhuaming
 * @version 3.0
 * @date 2026-04-27
 */
@Slf4j
@Tag(name = "视频详情评论管理")
@RestController
@RequestMapping("/docVideoComment")
@RequiredArgsConstructor
public class DocVideoCommentController {

    /**
     * 视频评论服务
     */
    private final IDocVideoCommentService docVideoCommentService;

    /**
     * 查询视频详情评论列表
     *
     * @param docVideoComment 查询条件
     * @return 评论列表
     */
    @Operation(summary = "查询视频详情评论列表")
    @GetMapping("/list")
    public R<List<DocVideoComment>> list(DocVideoComment docVideoComment) {
        LambdaQueryWrapper<DocVideoComment> lqw = new LambdaQueryWrapper<>(docVideoComment);
        return R.ok(docVideoCommentService.list(lqw));
    }

    /**
     * 分页查询视频详情评论列表
     *
     * @param docVideoComment 查询条件
     * @param pageQuery       分页参数
     * @return 分页评论列表
     */
    @Operation(summary = "分页查询视频详情评论列表")
    @GetMapping("/page")
    public R<PageResult<DocVideoComment>> page(DocVideoComment docVideoComment, PageQuery pageQuery) {
        Page<DocVideoComment> page = pageQuery.build();
        LambdaQueryWrapper<DocVideoComment> lqw = new LambdaQueryWrapper<>(docVideoComment);
        return R.ok(PageResult.build(docVideoCommentService.page(page, lqw)));
    }

    /**
     * 获取视频详情评论详细信息
     *
     * @param id 评论ID
     * @return 评论详情
     */
    @Operation(summary = "获取视频详情评论详细信息")
    @GetMapping(value = "/{id}")
    public R<DocVideoComment> getInfo(@PathVariable("id") Long id) {
        return R.ok(docVideoCommentService.getById(id));
    }

    /**
     * 新增视频详情评论
     *
     * @param docVideoComment 评论实体
     * @return 是否成功
     */
    @Operation(summary = "新增视频详情评论")
    @PostMapping
    public R<Boolean> add(@RequestBody DocVideoComment docVideoComment) {
        return R.ok(docVideoCommentService.save(docVideoComment));
    }

    /**
     * 修改视频详情评论
     *
     * @param docVideoComment 评论实体
     * @return 是否成功
     */
    @Operation(summary = "修改视频详情评论")
    @PutMapping
    public R<Boolean> edit(@RequestBody DocVideoComment docVideoComment) {
        return R.ok(docVideoCommentService.updateById(docVideoComment));
    }

    /**
     * 删除视频详情评论
     *
     * @param ids 评论ID列表
     * @return 是否成功
     */
    @Operation(summary = "删除视频详情评论")
    @DeleteMapping("/{ids}")
    public R<Boolean> remove(@PathVariable List<Long> ids) {
        return R.ok(docVideoCommentService.removeBatchByIds(ids));
    }

    /**
     * 获取视频评论列表（支持热门/最新排序）
     * <p>
     * 查询视频的根评论列表，每个根评论包含其下的所有回复。
     * 采用B站式评论结构：所有回复统一挂在根评论下，不存在层级嵌套。
     * </p>
     *
     * @param videoId   视频ID
     * @param pageQuery 分页查询参数
     * @param sort      排序方式（hot/new）
     * @return 评论分页列表
     */
    @Operation(summary = "获取视频评论列表（前台）")
    @GetMapping("/comments/{videoId}")
    public R<PageResult<DocVideoCommentVo>> getComments(
            @PathVariable("videoId") Long videoId,
            PageQuery pageQuery,
            @RequestParam(value = "sort", required = false) String sort) {
        log.info("获取视频评论列表请求, videoId={}, pageQuery={}, sort={}", videoId, pageQuery, sort);

        // 获取当前登录用户ID并调用Service层
        Long userId = getCurrentUserId();
        PageResult<DocVideoCommentVo> pageResult = docVideoCommentService.getCommentList(videoId, pageQuery, sort, userId);

        log.info("获取视频评论列表成功, videoId={}, total={}", videoId, pageResult.getTotal());
        return R.ok(pageResult);
    }

    /**
     * 发表视频评论
     * <p>
     * 用户发表视频评论，支持回复其他评论。
     * 采用B站式评论结构：parentCommentId统一记录根评论ID，replyUserId记录被回复的用户ID。
     * </p>
     *
     * @param commentRequest 评论请求参数
     * @return 评论结果
     */
    @Operation(summary = "发表视频评论（前台）")
    @PostMapping("/comment")
    public R<DocVideoCommentVo> postComment(@RequestBody VideoCommentRequestDTO commentRequest) {
        log.info("发表视频评论请求, videoId={}", commentRequest.getVideoId());

        // 获取当前登录用户ID
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }

        // 调用Service层处理业务逻辑（包含参数校验）
        DocVideoCommentVo result = docVideoCommentService.postCommentWithValidation(commentRequest, userId);

        log.info("发表视频评论成功, commentId={}", result.getId());
        return R.ok(result);
    }

    /**
     * 切换评论点赞状态
     * <p>
     * 用户点赞或取消点赞视频评论。点赞数从Redis获取，不再同步到数据库。
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
        InteractionResultVo result = docVideoCommentService.toggleCommentLike(commentId, userId);

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
