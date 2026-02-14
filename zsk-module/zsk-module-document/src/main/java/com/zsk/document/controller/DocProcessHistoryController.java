package com.zsk.document.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsk.common.core.domain.R;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.document.domain.DocProcessHistory;
import com.zsk.document.service.IDocProcessHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文件处理历史Controller
 * 
 * @author wuhuaming
 * @date 2026-02-14
 * @version 1.0
 */
@Tag(name = "文件处理历史管理")
@RestController
@RequestMapping("/process/history")
@RequiredArgsConstructor
public class DocProcessHistoryController {

    private final IDocProcessHistoryService docProcessHistoryService;

    /**
     * 查询文件处理历史列表
     */
    @Operation(summary = "查询文件处理历史列表")
    @GetMapping("/list")
    public R<List<DocProcessHistory>> list(DocProcessHistory docProcessHistory) {
        LambdaQueryWrapper<DocProcessHistory> lqw = new LambdaQueryWrapper<>(docProcessHistory);
        return R.ok(docProcessHistoryService.list(lqw));
    }

    /**
     * 分页查询文件处理历史列表
     */
    @Operation(summary = "分页查询文件处理历史列表")
    @GetMapping("/page")
    public R<PageResult<DocProcessHistory>> page(DocProcessHistory docProcessHistory, PageQuery pageQuery) {
        Page<DocProcessHistory> page = pageQuery.build();
        LambdaQueryWrapper<DocProcessHistory> lqw = new LambdaQueryWrapper<>(docProcessHistory);
        return R.ok(PageResult.build(docProcessHistoryService.page(page, lqw)));
    }

    /**
     * 获取文件处理历史详细信息
     */
    @Operation(summary = "获取文件处理历史详细信息")
    @GetMapping(value = "/{id}")
    public R<DocProcessHistory> getInfo(@PathVariable("id") Long id) {
        return R.ok(docProcessHistoryService.getById(id));
    }

    /**
     * 新增文件处理历史
     */
    @Operation(summary = "新增文件处理历史")
    @PostMapping
    public R<Boolean> add(@RequestBody DocProcessHistory docProcessHistory) {
        return R.ok(docProcessHistoryService.save(docProcessHistory));
    }

    /**
     * 修改文件处理历史
     */
    @Operation(summary = "修改文件处理历史")
    @PutMapping
    public R<Boolean> edit(@RequestBody DocProcessHistory docProcessHistory) {
        return R.ok(docProcessHistoryService.updateById(docProcessHistory));
    }

    /**
     * 删除文件处理历史
     */
    @Operation(summary = "删除文件处理历史")
    @DeleteMapping("/{ids}")
    public R<Boolean> remove(@PathVariable List<Long> ids) {
        return R.ok(docProcessHistoryService.removeBatchByIds(ids));
    }
}
