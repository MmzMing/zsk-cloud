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
 * 字典类型 控制器
 * <p>
 * 提供字典类型的增删改查及缓存管理等接口，对应 sys_dict_type 表。
 * 字典类型是字典数据的分组容器，例如「性别」「状态」「是否」等类型，
 * 每个类型下包含多条具体的字典数据项（由 SysDictDataController 管理）。
 *
 * @author wuhuaming
 */
@Tag(name = "字典管理")
@RestController
@RequestMapping("/dict/type")
@RequiredArgsConstructor
public class SysDictTypeController {

    private final ISysDictTypeService dictTypeService;

    // ==================== 查询 ====================

    /**
     * 查询字典类型列表（不分页）
     * <p>
     * 返回所有字典类型记录，不做筛选，适用于下拉选择等简单场景。
     *
     * @return 全部字典类型列表
     */
    @Operation(summary = "查询字典类型列表")
    @GetMapping("/list")
    public R<List<SysDictType>> list(SysDictType dictType) {
        return R.ok(dictTypeService.list());
    }

    /**
     * 分页查询字典类型列表
     * <p>
     * 根据字典名称（模糊匹配）、字典类型编码（精确匹配）、状态（精确匹配）筛选，
     * 支持分页参数。
     *
     * @param pageQuery 分页参数（页码、每页条数）
     * @param dictType  查询条件对象，支持 dictName / dictType / status
     * @return 分页结果，包含总条数与当前页数据
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
     * <p>
     * 根据主键 ID 查询单条字典类型记录的完整信息，用于编辑回显等场景。
     *
     * @param id 字典类型主键 ID
     * @return 字典类型详情
     */
    @Operation(summary = "获取字典类型详细信息")
    @GetMapping("/{id}")
    public R<SysDictType> getInfo(@PathVariable Long id) {
        return R.ok(dictTypeService.getById(id));
    }

    // ==================== 新增 ====================

    /**
     * 新增字典类型
     * <p>
     * 创建一个新的字典类型，需保证 dictType 编码全局唯一。
     * 新增后可调用缓存预热接口将数据加载到 Redis。
     *
     * @param dictType 字典类型对象（JSON 请求体）
     * @return 操作结果
     */
    @Operation(summary = "新增字典类型")
    @PostMapping
    public R<Void> add(@RequestBody SysDictType dictType) {
        return dictTypeService.save(dictType) ? R.ok() : R.fail();
    }

    // ==================== 修改 ====================

    /**
     * 修改字典类型
     * <p>
     * 根据主键 ID 更新字典类型信息，仅更新请求体中非空字段。
     * 若修改了 dictType 编码，需注意同步更新关联的字典数据及缓存。
     *
     * @param dictType 字典类型对象（JSON 请求体，必须包含 id）
     * @return 操作结果
     */
    @Operation(summary = "修改字典类型")
    @PutMapping
    public R<Void> edit(@RequestBody SysDictType dictType) {
        return dictTypeService.updateById(dictType) ? R.ok() : R.fail();
    }

    // ==================== 删除 ====================

    /**
     * 删除字典类型
     * <p>
     * 根据主键 ID 列表批量删除字典类型，支持单个删除和批量删除。
     * 删除类型前应确认该类型下无关联的字典数据，避免数据孤立。
     *
     * @param ids 字典类型主键 ID 列表（路径参数，逗号分隔）
     * @return 操作结果
     */
    @Operation(summary = "删除字典类型")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        return dictTypeService.removeByIds(ids) ? R.ok() : R.fail();
    }

    // ==================== 版本控制 ====================

    /**
     * 获取字典缓存全局版本号
     * <p>
     * 前端可在每次加载时调用此接口，与本地缓存的版本号比较，
     * 若不一致则重新拉取字典数据，实现前端缓存自动刷新。
     *
     * @return 全局版本号
     */
    @Operation(summary = "获取字典缓存版本号", description = "获取字典缓存全局版本号，前端用于判断是否需要重新拉取字典数据")
    @GetMapping("/version")
    public R<Long> getDictVersion() {
        return R.ok(dictTypeService.getDictVersion());
    }

    /**
     * 获取指定字典类型的缓存版本号
     * <p>
     * 前端可按类型检查版本号，仅重新拉取发生变更的字典类型数据，
     * 实现增量更新，减少网络传输量。
     *
     * @param dictType 字典类型编码
     * @return 该类型的版本号
     */
    @Operation(summary = "获取指定字典类型版本号", description = "获取指定字典类型的缓存版本号，前端用于增量更新")
    @GetMapping("/version/{dictType}")
    public R<Long> getDictVersion(@PathVariable String dictType) {
        return R.ok(dictTypeService.getDictVersion(dictType));
    }

    // ==================== 缓存管理 ====================

    /**
     * 缓存预热
     * <p>
     * 手动触发缓存预热，将所有正常状态的字典类型及其下属字典数据加载到 Redis。
     * 适用于系统启动后首次加载、缓存全部丢失后恢复等场景。
     *
     * @return 操作结果
     */
    @Operation(summary = "缓存预热", description = "手动触发缓存预热，加载所有正常状态的字典到Redis")
    @PostMapping("/cache/warmUp")
    public R<Void> warmUpCache() {
        dictTypeService.warmUpCache();
        return R.ok();
    }

    /**
     * 获取所有已缓存的字典类型标签
     * <p>
     * 返回 Redis 中当前已缓存的所有字典类型编码（dictType）集合，
     * 用于排查缓存状态或前端按需加载。
     *
     * @return 已缓存的字典类型编码集合
     */
    @Operation(summary = "获取缓存标签", description = "获取所有已缓存的字典类型标签（dictType）集合")
    @GetMapping("/cache/tags")
    public R<Set<String>> getCacheTags() {
        return R.ok(dictTypeService.getCacheTags());
    }

    /**
     * 按标签获取缓存的字典数据
     * <p>
     * 根据字典类型编码从 Redis 缓存中获取对应的字典数据列表，
     * 若缓存未命中则返回空列表（不会回源查库）。
     *
     * @param tag 字典类型编码
     * @return 该类型下的缓存字典数据列表
     */
    @Operation(summary = "按标签获取缓存", description = "根据字典类型标签获取缓存的字典数据列表")
    @GetMapping("/cache/tag/{tag}")
    public R<List<SysDictData>> getCacheByTag(@PathVariable String tag) {
        return R.ok(dictTypeService.getCacheByTag(tag));
    }

    /**
     * 获取全部缓存数据（按类型分组）
     * <p>
     * 返回 Redis 中所有已缓存的字典数据，按字典类型编码分组，
     * 适用于前端一次性加载所有字典的场景。
     *
     * @return 按字典类型编码分组的字典数据 Map
     */
    @Operation(summary = "获取全部缓存数据", description = "获取所有已缓存的字典数据，按字典类型分组")
    @GetMapping("/cache/all")
    public R<Map<String, List<SysDictData>>> getAllCacheData() {
        return R.ok(dictTypeService.getAllCacheData());
    }

    /**
     * 刷新指定字典类型的缓存
     * <p>
     * 先删除指定字典类型的 Redis 缓存，再从数据库重新加载该类型的字典数据写入缓存，
     * 适用于修改字典数据后需要立即生效的场景。
     *
     * @param dictType 字典类型编码
     * @return 操作结果
     */
    @Operation(summary = "刷新缓存", description = "刷新指定字典类型的缓存，先删除后重新加载")
    @PostMapping("/cache/refresh/{dictType}")
    public R<Void> refreshCache(@PathVariable String dictType) {
        dictTypeService.refreshCache(dictType);
        return R.ok();
    }

    /**
     * 删除指定字典类型的缓存
     * <p>
     * 仅删除 Redis 中指定字典类型的缓存数据，不重新加载。
     * 下次查询该类型时将触发缓存回源（如果配置了自动回源逻辑）。
     *
     * @param dictType 字典类型编码
     * @return 操作结果
     */
    @Operation(summary = "删除缓存", description = "删除指定字典类型的缓存")
    @DeleteMapping("/cache/{dictType}")
    public R<Void> deleteCache(@PathVariable String dictType) {
        dictTypeService.deleteCache(dictType);
        return R.ok();
    }

    /**
     * 清空所有字典缓存
     * <p>
     * 删除 Redis 中所有字典类型的缓存数据，适用于批量数据变更后统一清理的场景。
     * 清空后可调用 {@link #warmUpCache()} 重新预热。
     *
     * @return 操作结果
     */
    @Operation(summary = "清空所有缓存", description = "清空所有字典类型的缓存")
    @DeleteMapping("/cache/all")
    public R<Void> clearAllCache() {
        dictTypeService.clearAllCache();
        return R.ok();
    }
}
