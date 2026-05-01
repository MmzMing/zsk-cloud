package com.zsk.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsk.common.core.domain.R;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
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
 * <p>
 * 提供字典数据的增删改查及状态切换等接口，对应 sys_dict_data 表。
 * 字典数据是某个字典类型下的具体键值项，例如「性别」类型下的「男/女」。
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Tag(name = "字典数据")
@RestController
@RequestMapping("/dict/data")
@RequiredArgsConstructor
public class SysDictDataController {

    private final ISysDictDataService dictDataService;

    // ==================== 查询 ====================

    /**
     * 查询字典数据列表（不分页）
     * <p>
     * 根据字典类型（精确匹配）、字典标签（模糊匹配）、状态（精确匹配）筛选，
     * 结果按排序字段 dictSort 升序排列。
     *
     * @param dictData 查询条件对象，支持 dictType / dictLabel / status
     * @return 符合条件的字典数据列表
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
     * 分页查询字典数据列表
     * <p>
     * 筛选条件与 {@link #list} 一致，额外支持分页参数。
     *
     * @param pageQuery 分页参数（页码、每页条数）
     * @param dictData  查询条件对象，支持 dictType / dictLabel / status
     * @return 分页结果，包含总条数与当前页数据
     */
    @Operation(summary = "分页查询字典数据列表")
    @GetMapping("/page")
    public R<PageResult<SysDictData>> page(PageQuery pageQuery, SysDictData dictData) {
        LambdaQueryWrapper<SysDictData> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.hasText(dictData.getDictType()), SysDictData::getDictType, dictData.getDictType());
        lqw.like(StringUtils.hasText(dictData.getDictLabel()), SysDictData::getDictLabel, dictData.getDictLabel());
        lqw.eq(StringUtils.hasText(dictData.getStatus()), SysDictData::getStatus, dictData.getStatus());
        lqw.orderByAsc(SysDictData::getDictSort);

        Page<SysDictData> page = dictDataService.page(pageQuery.build(), lqw);
        return R.ok(PageResult.build(page));
    }

    /**
     * 根据字典类型查询字典数据
     * <p>
     * 通过字典类型编码（如 sys_user_sex）获取该类型下所有正常状态的字典数据，
     * 通常用于前端下拉框、单选框等组件的数据源。
     *
     * @param dictType 字典类型编码
     * @return 该类型下的字典数据列表
     */
    @Operation(summary = "根据字典类型查询字典数据信息")
    @GetMapping(value = "/type/{dictType}")
    public R<List<SysDictData>> dictType(@PathVariable String dictType) {
        return R.ok(dictDataService.selectDictDataByType(dictType));
    }

    /**
     * 根据字典类型和字典值查询字典标签（内部调用接口）
     * <p>
     * 通过字典类型和字典值查询对应的中文标签，用于 Feign 远程调用。
     * 例如：查询 sys_user_sex 的 "1" 对应 "男"。
     * 业务服务可调用此接口将字典值转换为标签返回给前端，
     * 前端无需再进行字典转换。
     *
     * @param dictType  字典类型（如 sys_user_sex）
     * @param dictValue 字典值（如 1）
     * @return 字典标签（如 男），未找到时返回 null
     */
    @Operation(summary = "根据字典类型和值查询字典标签")
    @GetMapping(value = "/label/{dictType}/{dictValue}")
    public R<String> getDictLabel(@PathVariable String dictType, @PathVariable String dictValue) {
        return R.ok(dictDataService.selectDictLabel(dictType, dictValue));
    }

    /**
     * 获取字典数据详细信息
     * <p>
     * 根据字典数据主键 ID 查询单条记录的完整信息，用于编辑回显等场景。
     *
     * @param id 字典数据主键 ID
     * @return 字典数据详情
     */
    @Operation(summary = "获取字典数据详细信息")
    @GetMapping(value = "/{id}")
    public R<SysDictData> getInfo(@PathVariable Long id) {
        return R.ok(dictDataService.getById(id));
    }

    // ==================== 新增 ====================

    /**
     * 新增字典数据
     * <p>
     * 在指定字典类型下新增一条字典数据项，需保证同一类型下 dictValue 不重复。
     *
     * @param dictData 字典数据对象（JSON 请求体）
     * @return 操作结果
     */
    @Operation(summary = "新增字典数据")
    @PostMapping
    public R<Void> add(@RequestBody SysDictData dictData) {
        return dictDataService.save(dictData) ? R.ok() : R.fail();
    }

    // ==================== 修改 ====================

    /**
     * 修改字典数据
     * <p>
     * 根据主键 ID 更新字典数据信息，仅更新请求体中非空字段。
     *
     * @param dictData 字典数据对象（JSON 请求体，必须包含 id）
     * @return 操作结果
     */
    @Operation(summary = "修改字典数据")
    @PutMapping
    public R<Void> edit(@RequestBody SysDictData dictData) {
        return dictDataService.updateById(dictData) ? R.ok() : R.fail();
    }

    /**
     * 切换字典数据状态
     * <p>
     * 将指定字典数据的状态在「正常(0)」和「停用(1)」之间切换，
     * 停用后该字典项在前端下拉框等组件中将不再展示。
     *
     * @param id     字典数据主键 ID
     * @param status 目标状态（0-正常，1-停用）
     * @return 操作结果
     */
    @Operation(summary = "切换字典状态")
    @PutMapping("/toggleStatus")
    public R<Void> toggleStatus(@RequestParam Long id, @RequestParam String status) {
        return dictDataService.toggleStatus(id, status) ? R.ok() : R.fail();
    }

    /**
     * 批量切换字典数据状态
     * <p>
     * 批量将多个字典数据项切换为指定状态，适用于列表页批量启用/停用操作。
     *
     * @param ids    字典数据主键 ID 列表（JSON 请求体）
     * @param status 目标状态（0-正常，1-停用）
     * @return 操作结果
     */
    @Operation(summary = "批量切换字典状态")
    @PutMapping("/batchToggleStatus")
    public R<Void> batchToggleStatus(@RequestBody List<Long> ids, @RequestParam String status) {
        return dictDataService.batchToggleStatus(ids, status) ? R.ok() : R.fail();
    }

    // ==================== 删除 ====================

    /**
     * 删除字典数据
     * <p>
     * 根据主键 ID 列表批量删除字典数据，支持单个删除（传一个 ID）和批量删除（传多个 ID）。
     * 删除前应确认该字典项未被业务引用。
     *
     * @param ids 字典数据主键 ID 列表（路径参数，逗号分隔）
     * @return 操作结果
     */
    @Operation(summary = "删除字典数据")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        return dictDataService.removeByIds(ids) ? R.ok() : R.fail();
    }
}
