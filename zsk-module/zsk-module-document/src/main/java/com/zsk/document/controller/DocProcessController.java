package com.zsk.document.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsk.common.core.domain.R;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.document.domain.DocProcess;
import com.zsk.document.service.IDocProcessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文件处理任务Controller
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-14
 */
@Tag(name = "文件处理任务管理")
@RestController
@RequestMapping("/process")
@RequiredArgsConstructor
public class DocProcessController {

    private final IDocProcessService docProcessService;

    /**
     * 查询文件处理任务列表
     */
    @Operation(summary = "查询文件处理任务列表")
    @GetMapping("/list")
    public R<List<DocProcess>> list(DocProcess docProcess) {
        LambdaQueryWrapper<DocProcess> lqw = new LambdaQueryWrapper<>(docProcess);
        return R.ok(docProcessService.list(lqw));
    }

    /**
     * 分页查询文件处理任务列表
     */
    @Operation(summary = "分页查询文件处理任务列表")
    @GetMapping("/page")
    public R<PageResult<DocProcess>> page(DocProcess docProcess, PageQuery pageQuery) {
        Page<DocProcess> page = pageQuery.build();
        LambdaQueryWrapper<DocProcess> lqw = new LambdaQueryWrapper<>(docProcess);
        return R.ok(PageResult.build(docProcessService.page(page, lqw)));
    }

    /**
     * 获取文件处理任务详细信息
     */
    @Operation(summary = "获取文件处理任务详细信息")
    @GetMapping(value = "/{id}")
    public R<DocProcess> getInfo(@PathVariable("id") Long id) {
        return R.ok(docProcessService.getById(id));
    }

    /**
     * 新增文件处理任务
     */
    @Operation(summary = "新增文件处理任务")
    @PostMapping
    public R<Boolean> add(@RequestBody DocProcess docProcess) {
        return R.ok(docProcessService.save(docProcess));
    }

    /**
     * 修改文件处理任务
     */
    @Operation(summary = "修改文件处理任务")
    @PutMapping
    public R<Boolean> edit(@RequestBody DocProcess docProcess) {
        return R.ok(docProcessService.updateById(docProcess));
    }

    /**
     * 删除文件处理任务
     */
    @Operation(summary = "删除文件处理任务")
    @DeleteMapping("/{ids}")
    public R<Boolean> remove(@PathVariable List<Long> ids) {
        return R.ok(docProcessService.removeBatchByIds(ids));
    }
}
