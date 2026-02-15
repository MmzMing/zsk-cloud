package com.zsk.document.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsk.common.core.domain.R;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.document.domain.DocNoteComment;
import com.zsk.document.service.IDocNoteCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 笔记评论Controller
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-14
 */
@Tag(name = "笔记评论管理")
@RestController
@RequestMapping("/note/comment")
@RequiredArgsConstructor
public class DocNoteCommentController {

    private final IDocNoteCommentService docNoteCommentService;

    /**
     * 查询笔记评论列表
     */
    @Operation(summary = "查询笔记评论列表")
    @GetMapping("/list")
    public R<List<DocNoteComment>> list(DocNoteComment docNoteComment) {
        LambdaQueryWrapper<DocNoteComment> lqw = new LambdaQueryWrapper<>(docNoteComment);
        return R.ok(docNoteCommentService.list(lqw));
    }

    /**
     * 分页查询笔记评论列表
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
     */
    @Operation(summary = "获取笔记评论详细信息")
    @GetMapping(value = "/{id}")
    public R<DocNoteComment> getInfo(@PathVariable("id") Long id) {
        return R.ok(docNoteCommentService.getById(id));
    }

    /**
     * 新增笔记评论
     */
    @Operation(summary = "新增笔记评论")
    @PostMapping
    public R<Boolean> add(@RequestBody DocNoteComment docNoteComment) {
        return R.ok(docNoteCommentService.save(docNoteComment));
    }

    /**
     * 修改笔记评论
     */
    @Operation(summary = "修改笔记评论")
    @PutMapping
    public R<Boolean> edit(@RequestBody DocNoteComment docNoteComment) {
        return R.ok(docNoteCommentService.updateById(docNoteComment));
    }

    /**
     * 删除笔记评论
     */
    @Operation(summary = "删除笔记评论")
    @DeleteMapping("/{ids}")
    public R<Boolean> remove(@PathVariable List<Long> ids) {
        return R.ok(docNoteCommentService.removeBatchByIds(ids));
    }
}
