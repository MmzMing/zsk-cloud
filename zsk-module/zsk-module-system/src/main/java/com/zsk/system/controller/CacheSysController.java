package com.zsk.system.controller;

import com.zsk.common.core.domain.R;
import com.zsk.system.domain.CacheSysInfo;
import com.zsk.system.service.ICacheSysService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 缓存系统管理 控制器
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Tag(name = "缓存管理")
@RestController
@RequestMapping("/cache")
@RequiredArgsConstructor
public class CacheSysController {

    private final ICacheSysService cacheSysService;

    /**
     * 获取缓存键名列表
     *
     * @param cacheName 缓存名称（可选）
     * @return 缓存键名列表
     */
    @Operation(summary = "获取缓存键名列表")
    @GetMapping("/keys")
    public R<Collection<String>> getCacheKeys(
            @Parameter(description = "缓存名称") @RequestParam(required = false) String cacheName) {
        return R.ok(cacheSysService.getCacheKeys(cacheName));
    }

    /**
     * 获取缓存信息列表
     *
     * @param cacheName 缓存名称（可选）
     * @return 缓存信息列表
     */
    @Operation(summary = "获取缓存信息列表")
    @GetMapping("/list")
    public R<List<CacheSysInfo>> getCacheList(
            @Parameter(description = "缓存名称") @RequestParam(required = false) String cacheName) {
        return R.ok(cacheSysService.getCacheInfoList(cacheName));
    }

    /**
     * 获取缓存详细信息
     *
     * @param cacheKey 缓存键名
     * @return 缓存信息
     */
    @Operation(summary = "获取缓存详细信息")
    @GetMapping("/info/{cacheKey}")
    public R<CacheSysInfo> getCacheInfo(
            @Parameter(description = "缓存键名") @PathVariable String cacheKey) {
        return R.ok(cacheSysService.getCacheInfo(cacheKey));
    }

    /**
     * 获取缓存值
     *
     * @param cacheKey 缓存键名
     * @return 缓存值
     */
    @Operation(summary = "获取缓存值")
    @GetMapping("/value/{cacheKey}")
    public R<Object> getCacheValue(
            @Parameter(description = "缓存键名") @PathVariable String cacheKey) {
        return R.ok(cacheSysService.getCacheValue(cacheKey));
    }

    /**
     * 删除缓存
     *
     * @param cacheKeys 缓存键名列表
     * @return 删除数量
     */
    @Operation(summary = "删除缓存")
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
    @Operation(summary = "清空指定名称的缓存")
    @DeleteMapping("/clear/{cacheName}")
    public R<Long> clearCacheByName(
            @Parameter(description = "缓存名称") @PathVariable String cacheName) {
        return R.ok(cacheSysService.clearCacheByName(cacheName));
    }

    /**
     * 清空所有缓存
     *
     * @return 删除数量
     */
    @Operation(summary = "清空所有缓存")
    @DeleteMapping("/clearAll")
    public R<Long> clearAllCache() {
        return R.ok(cacheSysService.clearAllCache());
    }

    /**
     * 刷新缓存过期时间
     *
     * @param cacheKey 缓存键名
     * @param ttl      过期时间（秒）
     * @return 是否成功
     */
    @Operation(summary = "刷新缓存过期时间")
    @PutMapping("/refreshTtl")
    public R<Boolean> refreshTtl(
            @Parameter(description = "缓存键名") @RequestParam String cacheKey,
            @Parameter(description = "过期时间（秒）") @RequestParam Long ttl) {
        return R.ok(cacheSysService.refreshTtl(cacheKey, ttl));
    }

    /**
     * 批量刷新缓存过期时间
     *
     * @param cacheKeyTtlMap 缓存键名与过期时间的映射
     * @return 成功刷新的数量
     */
    @Operation(summary = "批量刷新缓存过期时间")
    @PutMapping("/refreshTtlBatch")
    public R<Integer> refreshTtlBatch(@RequestBody Map<String, Long> cacheKeyTtlMap) {
        return R.ok(cacheSysService.refreshTtlBatch(cacheKeyTtlMap));
    }

    /**
     * 缓存预热
     *
     * @param cacheNames 需要预热的缓存名称列表
     * @return 预热结果
     */
    @Operation(summary = "缓存预热")
    @PostMapping("/warmup")
    public R<Map<String, Object>> warmupCache(
            @Parameter(description = "缓存名称列表") @RequestBody(required = false) List<String> cacheNames) {
        return R.ok(cacheSysService.warmupCache(cacheNames));
    }

    /**
     * 获取缓存统计信息
     *
     * @return 缓存统计信息
     */
    @Operation(summary = "获取缓存统计信息")
    @GetMapping("/statistics")
    public R<Map<String, Object>> getCacheStatistics() {
        return R.ok(cacheSysService.getCacheStatistics());
    }

    /**
     * 判断缓存是否存在
     *
     * @param cacheKey 缓存键名
     * @return 是否存在
     */
    @Operation(summary = "判断缓存是否存在")
    @GetMapping("/exists/{cacheKey}")
    public R<Boolean> hasCacheKey(
            @Parameter(description = "缓存键名") @PathVariable String cacheKey) {
        return R.ok(cacheSysService.hasCacheKey(cacheKey));
    }
}
