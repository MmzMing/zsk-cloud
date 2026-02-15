package com.zsk.document.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsk.common.core.domain.R;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.document.domain.DocNotePic;
import com.zsk.document.service.IDocNotePicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 笔记图片Controller
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-14
 */
@Tag(name = "笔记图片管理")
@RestController
@RequestMapping("/note/pic")
@RequiredArgsConstructor
public class DocNotePicController {

    private final IDocNotePicService docNotePicService;

    /**
     * 查询笔记图片列表
     */
    @Operation(summary = "查询笔记图片列表")
    @GetMapping("/list")
    public R<List<DocNotePic>> list(DocNotePic docNotePic) {
        LambdaQueryWrapper<DocNotePic> lqw = new LambdaQueryWrapper<>(docNotePic);
        return R.ok(docNotePicService.list(lqw));
    }

    /**
     * 分页查询笔记图片列表
     */
    @Operation(summary = "分页查询笔记图片列表")
    @GetMapping("/page")
    public R<PageResult<DocNotePic>> page(DocNotePic docNotePic, PageQuery pageQuery) {
        Page<DocNotePic> page = pageQuery.build();
        LambdaQueryWrapper<DocNotePic> lqw = new LambdaQueryWrapper<>(docNotePic);
        return R.ok(PageResult.build(docNotePicService.page(page, lqw)));
    }

    /**
     * 获取笔记图片详细信息
     */
    @Operation(summary = "获取笔记图片详细信息")
    @GetMapping(value = "/{id}")
    public R<DocNotePic> getInfo(@PathVariable("id") Long id) {
        return R.ok(docNotePicService.getById(id));
    }

    /**
     * 新增笔记图片
     */
    @Operation(summary = "新增笔记图片")
    @PostMapping
    public R<Boolean> add(@RequestBody DocNotePic docNotePic) {
        return R.ok(docNotePicService.save(docNotePic));
    }

    /**
     * 修改笔记图片
     */
    @Operation(summary = "修改笔记图片")
    @PutMapping
    public R<Boolean> edit(@RequestBody DocNotePic docNotePic) {
        return R.ok(docNotePicService.updateById(docNotePic));
    }

    /**
     * 删除笔记图片
     */
    @Operation(summary = "删除笔记图片")
    @DeleteMapping("/{ids}")
    public R<Boolean> remove(@PathVariable List<Long> ids) {
        return R.ok(docNotePicService.removeBatchByIds(ids));
    }
}
