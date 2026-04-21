package com.zsk.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsk.common.core.domain.R;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.system.domain.SysDictType;
import com.zsk.system.service.ISysDictTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典管理 控制器
 *
 * @author wuhuaming
 */
@Tag(name = "字典管理")
@RestController
@RequestMapping("/dict/type")
@RequiredArgsConstructor
public class SysDictTypeController {

    private final ISysDictTypeService dictTypeService;

    /**
     * 查询字典类型列表
     */
    @Operation(summary = "查询字典类型列表")
    @GetMapping("/list")
    public R<List<SysDictType>> list(SysDictType dictType) {
        return R.ok(dictTypeService.list());
    }

    /**
     * 分页查询字典类型列表
     *
     * @param pageQuery 分页参数
     * @param dictType  查询条件
     * @return 分页结果
     */
    @Operation(summary = "分页查询字典类型列表")
    @GetMapping("/page")
    public R<PageResult<SysDictType>> page(PageQuery pageQuery, SysDictType dictType) {
        LambdaQueryWrapper<SysDictType> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.hasText(dictType.getDictName()), SysDictType::getDictName, dictType.getDictName());
        lqw.eq(StringUtils.hasText(dictType.getDictType()), SysDictType::getDictType, dictType.getDictType());
        lqw.eq(StringUtils.hasText(dictType.getStatus()), SysDictType::getStatus, dictType.getStatus());

        Page<SysDictType> page = dictTypeService.page(pageQuery.build(), lqw);
        return R.ok(PageResult.build(page));
    }

    /**
     * 获取字典类型详细信息
     */
    @Operation(summary = "获取字典类型详细信息")
    @GetMapping("/{id}")
    public R<SysDictType> getInfo(@PathVariable Long id) {
        return R.ok(dictTypeService.getById(id));
    }

    /**
     * 新增字典类型
     */
    @Operation(summary = "新增字典类型")
    @PostMapping
    public R<Void> add(@RequestBody SysDictType dictType) {
        return dictTypeService.save(dictType) ? R.ok() : R.fail();
    }

    /**
     * 修改字典类型
     */
    @Operation(summary = "修改字典类型")
    @PutMapping
    public R<Void> edit(@RequestBody SysDictType dictType) {
        return dictTypeService.updateById(dictType) ? R.ok() : R.fail();
    }

    /**
     * 删除字典类型
     */
    @Operation(summary = "删除字典类型")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        return dictTypeService.removeByIds(ids) ? R.ok() : R.fail();
    }
}
