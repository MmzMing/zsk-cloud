package com.zsk.system.controller;

import com.zsk.common.core.domain.R;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.system.domain.CacheSysInfo;
import com.zsk.system.domain.SysCacheLog;
import com.zsk.system.domain.dto.*;
import com.zsk.system.domain.vo.*;
import com.zsk.system.service.ICacheSysService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 缓存管理 控制器
 * <p>
 * 提供缓存实例管理、缓存键操作、缓存统计等功能接口
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Tag(name = "缓存管理", description = "缓存管理相关接口")
@RestController
@RequestMapping("/cache")
@RequiredArgsConstructor
public class CacheSysController {

    private final ICacheSysService cacheSysService;

    /**
     * 获取缓存实例列表
     *
     * @return 实例列表
     */
    @Operation(summary = "获取缓存实例列表", description = "获取所有缓存实例的基本信息")
    @GetMapping("/instances")
    public R<List<CacheInstanceVO>> getInstances() {
        return R.ok(cacheSysService.getInstances());
    }

    /**
     * 获取缓存日志
     *
     * @param instanceId 实例ID（可选）
     * @return 日志列表
     */
    @Operation(summary = "获取缓存日志", description = "获取缓存操作日志，支持按实例ID筛选")
    @GetMapping("/logs")
    public R<List<SysCacheLog>> getLogs(
            @Parameter(description = "实例ID") @RequestParam(required = false) String instanceId) {
        return R.ok(cacheSysService.getLogs(instanceId));
    }

    /**
     * 获取缓存分布饼图数据
     *
     * @return 缓存名称汇总数据
     */
    @Operation(summary = "获取缓存分布饼图数据", description = "根据缓存名称汇总统计，用于饼图展示")
    @GetMapping("/distribution")
    public R<List<CachePieVO>> getCacheDistribution() {
        return R.ok(cacheSysService.getCacheDistribution());
    }

    /**
     * 获取内存使用仪表盘数据
     *
     * @return 内存使用仪表盘数据
     */
    @Operation(summary = "获取内存使用仪表盘数据", description = "获取系统总内存和缓存占用内存量，用于仪表盘展示")
    @GetMapping("/memory/usage")
    public R<GaugeDataPoint> getMemoryUsage() {
        return R.ok(cacheSysService.getMemoryUsage());
    }

    /**
     * 获取Redis信息
     *
     * @return Redis信息
     */
    @Operation(summary = "获取Redis信息", description = "获取Redis服务器的详细信息，包括版本、内存使用、连接数等")
    @GetMapping("/info")
    public R<CacheRedisInfoVO> getRedisInfo() {
        return R.ok(cacheSysService.getRedisInfo());
    }

    /**
     * 获取缓存键名列表
     *
     * @param pageQuery 分页参数
     * @param queryDTO  查询参数
     * @return 缓存键名列表
     */
    @Operation(summary = "获取缓存键名列表", description = "获取缓存键名列表，支持按缓存名称筛选和关键字模糊搜索")
    @GetMapping("/keys")
    public R<PageResult<String>> getCacheKeys(PageQuery pageQuery, CacheKeyQueryDTO queryDTO) {
        return R.ok(cacheSysService.getKeys(pageQuery, queryDTO));
    }

    /**
     * 获取缓存信息列表
     *
     * @param pageQuery 分页参数
     * @param queryDTO  查询参数
     * @return 缓存信息列表
     */
    @Operation(summary = "获取缓存信息列表", description = "获取缓存详细信息列表，支持按缓存名称筛选和分页")
    @GetMapping("/list")
    public R<PageResult<CacheSysInfo>> getCacheList(PageQuery pageQuery, CacheKeyQueryDTO queryDTO) {
        return R.ok(cacheSysService.getCacheInfoList(pageQuery, queryDTO));
    }

    /**
     * 获取缓存详细信息
     *
     * @param cacheKey 缓存键名
     * @return 缓存信息
     */
    @Operation(summary = "获取缓存详细信息", description = "根据缓存键名获取缓存的详细信息，包括值、过期时间、数据大小等")
    @GetMapping("/info/{cacheKey}")
    public R<CacheSysInfo> getCacheInfo(
            @Parameter(description = "缓存键名", required = true) @PathVariable String cacheKey) {
        return R.ok(cacheSysService.getCacheInfo(cacheKey));
    }

    /**
     * 获取缓存值
     *
     * @param cacheKey 缓存键名
     * @return 缓存值
     */
    @Operation(summary = "获取缓存值", description = "根据缓存键名获取缓存的实际值")
    @GetMapping("/value/{cacheKey}")
    public R<Object> getCacheValue(
            @Parameter(description = "缓存键名", required = true) @PathVariable String cacheKey) {
        return R.ok(cacheSysService.getCacheValue(cacheKey));
    }

    /**
     * 刷新缓存键
     *
     * @param refreshDTO 刷新参数
     * @return 是否成功
     */
    @Operation(summary = "刷新缓存键", description = "刷新指定缓存键，重新加载数据")
    @PostMapping("/keys/refresh")
    public R<Void> refreshKey(@Valid @RequestBody CacheKeyRefreshDTO refreshDTO) {
        return cacheSysService.refreshKey(refreshDTO) ? R.ok() : R.fail();
    }

    /**
     * 删除缓存键
     *
     * @param key 键名
     * @return 是否成功
     */
    @Operation(summary = "删除缓存键", description = "删除指定的缓存键")
    @DeleteMapping("/keys/{key}")
    public R<Void> deleteKey(
            @Parameter(description = "缓存键名", required = true) @PathVariable String key) {
        return cacheSysService.deleteKey(key) ? R.ok() : R.fail();
    }

    /**
     * 批量刷新缓存键
     *
     * @param keys 键名列表
     * @return 是否成功
     */
    @Operation(summary = "批量刷新缓存键", description = "批量刷新多个缓存键")
    @PostMapping("/keys/batchRefresh")
    public R<Void> batchRefreshKeys(@Valid @RequestBody CacheKeyBatchDTO batchDTO) {
        return cacheSysService.batchRefreshKeys(batchDTO.getKeys()) ? R.ok() : R.fail();
    }

    /**
     * 批量删除缓存键
     *
     * @param keys 键名列表
     * @return 是否成功
     */
    @Operation(summary = "批量删除缓存键", description = "批量删除多个缓存键")
    @PostMapping("/keys/batchDelete")
    public R<Void> batchDeleteKeys(@Valid @RequestBody CacheKeyBatchDTO batchDTO) {
        return cacheSysService.batchDeleteKeys(batchDTO.getKeys()) ? R.ok() : R.fail();
    }

    /**
     * 删除缓存
     *
     * @param cacheKeys 缓存键名列表
     * @return 删除数量
     */
    @Operation(summary = "删除缓存", description = "删除指定的缓存键列表")
    @DeleteMapping("/delete")
    public R<Long> deleteCache(@RequestBody Collection<String> cacheKeys) {
        return R.ok(cacheSysService.deleteCache(cacheKeys));
    }

    /**
     * 清空指定名称的缓存
     *
     * @param cacheName 缓存名称
     * @return 删除数量
     */
    @Operation(summary = "清空指定名称的缓存", description = "清空指定缓存名称下的所有缓存键")
    @DeleteMapping("/clear/{cacheName}")
    public R<Long> clearCacheByName(
            @Parameter(description = "缓存名称", required = true) @PathVariable String cacheName) {
        return R.ok(cacheSysService.clearCacheByName(cacheName));
    }

    /**
     * 清空缓存实例
     *
     * @return 是否成功
     */
    @Operation(summary = "清空缓存实例", description = "清空整个缓存实例的所有缓存")
    @PostMapping("/instances/clear")
    public R<Void> clearInstance() {
        return cacheSysService.clearInstance() ? R.ok() : R.fail();
    }

    /**
     * 清空所有缓存
     *
     * @return 删除数量
     */
    @Operation(summary = "清空所有缓存", description = "清空所有缓存实例的所有缓存")
    @DeleteMapping("/clearAll")
    public R<Long> clearAllCache() {
        return R.ok(cacheSysService.clearAllCache());
    }

    /**
     * 刷新缓存过期时间
     *
     * @param ttlDTO 过期时间参数
     * @return 是否成功
     */
    @Operation(summary = "刷新缓存过期时间", description = "更新指定缓存键的过期时间")
    @PutMapping("/refreshTtl")
    public R<Boolean> refreshTtl(@Valid @RequestBody CacheTtlRefreshDTO ttlDTO) {
        return R.ok(cacheSysService.refreshTtl(ttlDTO));
    }

    /**
     * 批量刷新缓存过期时间
     *
     * @param cacheKeyTtlMap 缓存键名与过期时间的映射
     * @return 成功刷新的数量
     */
    @Operation(summary = "批量刷新缓存过期时间", description = "批量更新多个缓存键的过期时间")
    @PutMapping("/refreshTtlBatch")
    public R<Integer> refreshTtlBatch(@Valid @RequestBody CacheTtlBatchDTO batchDTO) {
        return R.ok(cacheSysService.refreshTtlBatch(batchDTO.getCacheKeyTtlMap()));
    }

    /**
     * 缓存预热
     *
     * @param warmupDTO 预热参数
     * @return 预热结果
     */
    @Operation(summary = "缓存预热", description = "预热指定缓存名称的数据，若不指定则预热所有缓存")
    @PostMapping("/warmup")
    public R<List<CacheWarmupResultVO>> warmupCache(
            @RequestBody(required = false) CacheWarmupDTO warmupDTO) {
        return R.ok(cacheSysService.warmupCache(warmupDTO));
    }

    /**
     * 获取缓存统计信息
     *
     * @return 缓存统计信息
     */
    @Operation(summary = "获取缓存统计信息", description = "获取缓存系统的综合统计信息")
    @GetMapping("/statistics")
    public R<CacheStatisticsVO> getCacheStatistics() {
        return R.ok(cacheSysService.getCacheStatistics());
    }

    /**
     * 判断缓存是否存在
     *
     * @param cacheKey 缓存键名
     * @return 是否存在
     */
    @Operation(summary = "判断缓存是否存在", description = "检查指定缓存键是否存在")
    @GetMapping("/exists/{cacheKey}")
    public R<Boolean> hasCacheKey(
            @Parameter(description = "缓存键名", required = true) @PathVariable String cacheKey) {
        return R.ok(cacheSysService.hasCacheKey(cacheKey));
    }
}