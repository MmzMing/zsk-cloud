package com.zsk.document.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsk.common.core.domain.R;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.document.domain.DocVideoComment;
import com.zsk.document.service.IDocVideoCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 视频详情评论Controller
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-14
 */
@Tag(name = "视频详情评论管理")
@RestController
@RequestMapping("/video/comment")
@RequiredArgsConstructor
public class DocVideoCommentController {

    private final IDocVideoCommentService docVideoCommentService;

    /**
     * 查询视频详情评论列表
     */
    @Operation(summary = "查询视频详情评论列表")
    @GetMapping("/list")
    public R<List<DocVideoComment>> list(DocVideoComment docVideoComment) {
        LambdaQueryWrapper<DocVideoComment> lqw = new LambdaQueryWrapper<>(docVideoComment);
        return R.ok(docVideoCommentService.list(lqw));
    }

    /**
     * 分页查询视频详情评论列表
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
     */
    @Operation(summary = "获取视频详情评论详细信息")
    @GetMapping(value = "/{id}")
    public R<DocVideoComment> getInfo(@PathVariable("id") Long id) {
        return R.ok(docVideoCommentService.getById(id));
    }

    /**
     * 新增视频详情评论
     */
    @Operation(summary = "新增视频详情评论")
    @PostMapping
    public R<Boolean> add(@RequestBody DocVideoComment docVideoComment) {
        return R.ok(docVideoCommentService.save(docVideoComment));
    }

    /**
     * 修改视频详情评论
     */
    @Operation(summary = "修改视频详情评论")
    @PutMapping
    public R<Boolean> edit(@RequestBody DocVideoComment docVideoComment) {
        return R.ok(docVideoCommentService.updateById(docVideoComment));
    }

    /**
     * 删除视频详情评论
     */
    @Operation(summary = "删除视频详情评论")
    @DeleteMapping("/{ids}")
    public R<Boolean> remove(@PathVariable List<Long> ids) {
        return R.ok(docVideoCommentService.removeBatchByIds(ids));
    }
}
