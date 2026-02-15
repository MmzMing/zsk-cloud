package com.zsk.document.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsk.common.core.domain.R;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.common.log.annotation.Log;
import com.zsk.common.log.enums.BusinessType;
import com.zsk.document.domain.DocNote;
import com.zsk.document.service.IDocNoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 笔记信息Controller
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-14
 */
@Tag(name = "笔记管理")
@RestController
@RequestMapping("/note")
@RequiredArgsConstructor
public class DocNoteController {

    private final IDocNoteService docNoteService;

    /**
     * 查询笔记列表
     */
    @Log(title = "笔记管理", businessType = BusinessType.QUERY)
    @Operation(summary = "查询笔记列表")
    @GetMapping("/list")
    public R<List<DocNote>> list(DocNote docNote) {
        LambdaQueryWrapper<DocNote> lqw = new LambdaQueryWrapper<>(docNote);
        return R.ok(docNoteService.list(lqw));
    }

    /**
     * 分页查询笔记列表
     */
    @Log(title = "笔记管理", businessType = BusinessType.QUERY)
    @Operation(summary = "分页查询笔记列表")
    @GetMapping("/page")
    public R<PageResult<DocNote>> page(DocNote docNote, PageQuery pageQuery) {
        Page<DocNote> page = pageQuery.build();
        LambdaQueryWrapper<DocNote> lqw = new LambdaQueryWrapper<>(docNote);
        return R.ok(PageResult.build(docNoteService.page(page, lqw)));
    }

    /**
     * 获取笔记详细信息
     */
    @Log(title = "笔记管理", businessType = BusinessType.QUERY)
    @Operation(summary = "获取笔记详细信息")
    @GetMapping(value = "/{id}")
    public R<DocNote> getInfo(@PathVariable("id") Long id) {
        return R.ok(docNoteService.getById(id));
    }

    /**
     * 新增笔记
     */
    @Log(title = "笔记管理", businessType = BusinessType.INSERT)
    @Operation(summary = "新增笔记")
    @PostMapping
    public R<Boolean> add(@RequestBody DocNote docNote) {
        return R.ok(docNoteService.save(docNote));
    }

    /**
     * 修改笔记
     */
    @Log(title = "笔记管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改笔记")
    @PutMapping
    public R<Boolean> edit(@RequestBody DocNote docNote) {
        return R.ok(docNoteService.updateById(docNote));
    }

    /**
     * 删除笔记
     */
    @Log(title = "笔记管理", businessType = BusinessType.DELETE)
    @Operation(summary = "删除笔记")
    @DeleteMapping("/{ids}")
    public R<Boolean> remove(@PathVariable List<Long> ids) {
        return R.ok(docNoteService.removeBatchByIds(ids));
    }
}
