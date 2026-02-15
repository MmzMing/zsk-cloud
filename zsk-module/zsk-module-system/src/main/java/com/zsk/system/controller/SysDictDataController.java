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
 * @date 2026-02-15
 * @version 1.0
 */
@Tag(name = "字典数据")
@RestController
@RequestMapping("/dict/data")
@RequiredArgsConstructor
public class SysDictDataController {

    private final ISysDictDataService dictDataService;

    /**
     * 查询字典数据列表
     *
     * @param dictData 查询条件
     * @return 字典数据列表
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
     *
     * @param dictType 字典类型
     * @return 字典数据列表
     */
    @Operation(summary = "根据字典类型查询字典数据信息")
    @GetMapping(value = "/type/{dictType}")
    public R<List<SysDictData>> dictType(@PathVariable String dictType) {
        return R.ok(dictDataService.selectDictDataByType(dictType));
    }

    /**
     * 获取字典数据详细信息
     *
     * @param id 字典ID
     * @return 字典数据详情
     */
    @Operation(summary = "获取字典数据详细信息")
    @GetMapping(value = "/{id}")
    public R<SysDictData> getInfo(@PathVariable Long id) {
        return R.ok(dictDataService.getById(id));
    }

    /**
     * 新增字典数据
     *
     * @param dictData 字典数据
     * @return 是否成功
     */
    @Operation(summary = "新增字典数据")
    @PostMapping
    public R<Void> add(@RequestBody SysDictData dictData) {
        return dictDataService.save(dictData) ? R.ok() : R.fail();
    }

    /**
     * 修改字典数据
     *
     * @param dictData 字典数据
     * @return 是否成功
     */
    @Operation(summary = "修改字典数据")
    @PutMapping
    public R<Void> edit(@RequestBody SysDictData dictData) {
        return dictDataService.updateById(dictData) ? R.ok() : R.fail();
    }

    /**
     * 删除字典数据
     *
     * @param ids 字典ID列表
     * @return 是否成功
     */
    @Operation(summary = "删除字典数据")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        return dictDataService.removeByIds(ids) ? R.ok() : R.fail();
    }

    /**
     * 切换字典状态
     *
     * @param id 字典ID
     * @param status 状态（0正常 1停用）
     * @return 是否成功
     */
    @Operation(summary = "切换字典状态")
    @PutMapping("/toggleStatus")
    public R<Void> toggleStatus(@RequestParam Long id, @RequestParam String status) {
        return dictDataService.toggleStatus(id, status) ? R.ok() : R.fail();
    }

    /**
     * 批量切换字典状态
     *
     * @param ids 字典ID列表
     * @param status 状态（0正常 1停用）
     * @return 是否成功
     */
    @Operation(summary = "批量切换字典状态")
    @PutMapping("/batchToggleStatus")
    public R<Void> batchToggleStatus(@RequestBody List<Long> ids, @RequestParam String status) {
        return dictDataService.batchToggleStatus(ids, status) ? R.ok() : R.fail();
    }
}
