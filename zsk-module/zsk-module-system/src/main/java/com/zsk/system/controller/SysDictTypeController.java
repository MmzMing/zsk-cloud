package com.zsk.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsk.common.core.domain.R;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.system.domain.SysDictData;
import com.zsk.system.domain.SysDictType;
import com.zsk.system.service.ISysDictTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

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

    // ==================== 缓存管理接口 ====================

    /**
     * 手动触发缓存预热
     */
    @Operation(summary = "缓存预热", description = "手动触发缓存预热，加载所有正常状态的字典到Redis")
    @PostMapping("/cache/warmUp")
    public R<Void> warmUpCache() {
        dictTypeService.warmUpCache();
        return R.ok();
    }

    /**
     * 获取所有已缓存的字典类型标签集合
     */
    @Operation(summary = "获取缓存标签", description = "获取所有已缓存的字典类型标签（dictType）集合")
    @GetMapping("/cache/tags")
    public R<Set<String>> getCacheTags() {
        return R.ok(dictTypeService.getCacheTags());
    }

    /**
     * 根据标签获取缓存的字典数据
     */
    @Operation(summary = "按标签获取缓存", description = "根据字典类型标签获取缓存的字典数据列表")
    @GetMapping("/cache/tag/{tag}")
    public R<List<SysDictData>> getCacheByTag(@PathVariable String tag) {
        return R.ok(dictTypeService.getCacheByTag(tag));
    }

    /**
     * 获取所有缓存数据（按标签分组）
     */
    @Operation(summary = "获取全部缓存数据", description = "获取所有已缓存的字典数据，按字典类型分组")
    @GetMapping("/cache/all")
    public R<Map<String, List<SysDictData>>> getAllCacheData() {
        return R.ok(dictTypeService.getAllCacheData());
    }

    /**
     * 刷新指定字典类型的缓存
     */
    @Operation(summary = "刷新缓存", description = "刷新指定字典类型的缓存，先删除后重新加载")
    @PostMapping("/cache/refresh/{dictType}")
    public R<Void> refreshCache(@PathVariable String dictType) {
        dictTypeService.refreshCache(dictType);
        return R.ok();
    }

    /**
     * 删除指定字典类型的缓存
     */
    @Operation(summary = "删除缓存", description = "删除指定字典类型的缓存")
    @DeleteMapping("/cache/{dictType}")
    public R<Void> deleteCache(@PathVariable String dictType) {
        dictTypeService.deleteCache(dictType);
        return R.ok();
    }

    /**
     * 清空所有字典缓存
     */
    @Operation(summary = "清空所有缓存", description = "清空所有字典类型的缓存")
    @DeleteMapping("/cache/all")
    public R<Void> clearAllCache() {
        dictTypeService.clearAllCache();
        return R.ok();
    }
}
