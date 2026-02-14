package com.zsk.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zsk.common.core.domain.R;
import com.zsk.system.domain.SysDictData;
import com.zsk.system.service.ISysDictDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典数据 控制器
 *
 * @author wuhuaming
 */
@Tag(name = "字典数据")
@RestController
@RequestMapping("/dict/data")
@RequiredArgsConstructor
public class SysDictDataController {

    private final ISysDictDataService dictDataService;

    /**
     * 查询字典数据列表
     */
    @Operation(summary = "查询字典数据列表")
    @GetMapping("/list")
    public R<List<SysDictData>> list(SysDictData dictData) {
        LambdaQueryWrapper<SysDictData> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.hasText(dictData.getDictType()), SysDictData::getDictType, dictData.getDictType());
        lqw.like(StringUtils.hasText(dictData.getDictLabel()), SysDictData::getDictLabel, dictData.getDictLabel());
        lqw.eq(StringUtils.hasText(dictData.getStatus()), SysDictData::getStatus, dictData.getStatus());
        lqw.orderByAsc(SysDictData::getDictSort);
        return R.ok(dictDataService.list(lqw));
    }

    /**
     * 根据字典类型查询字典数据信息
     */
    @Operation(summary = "根据字典类型查询字典数据信息")
    @GetMapping(value = "/type/{dictType}")
    public R<List<SysDictData>> dictType(@PathVariable String dictType) {
        return R.ok(dictDataService.selectDictDataByType(dictType));
    }

    /**
     * 获取字典数据详细信息
     */
    @Operation(summary = "获取字典数据详细信息")
    @GetMapping(value = "/{id}")
    public R<SysDictData> getInfo(@PathVariable Long id) {
        return R.ok(dictDataService.getById(id));
    }

    /**
     * 新增字典数据
     */
    @Operation(summary = "新增字典数据")
    @PostMapping
    public R<Void> add(@RequestBody SysDictData dictData) {
        return dictDataService.save(dictData) ? R.ok() : R.fail();
    }

    /**
     * 修改字典数据
     */
    @Operation(summary = "修改字典数据")
    @PutMapping
    public R<Void> edit(@RequestBody SysDictData dictData) {
        return dictDataService.updateById(dictData) ? R.ok() : R.fail();
    }

    /**
     * 删除字典数据
     */
    @Operation(summary = "删除字典数据")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        return dictDataService.removeByIds(ids) ? R.ok() : R.fail();
    }
}
