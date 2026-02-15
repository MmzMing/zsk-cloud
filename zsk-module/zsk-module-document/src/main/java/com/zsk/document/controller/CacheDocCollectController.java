package com.zsk.document.controller;

import com.zsk.common.core.domain.R;
import com.zsk.common.log.annotation.Log;
import com.zsk.common.log.enums.BusinessType;
import com.zsk.document.enums.CacheDocCollectTypeEnum;
import com.zsk.document.service.ICacheDocCollectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 缓存文档收藏Controller
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Tag(name = "收藏管理")
@RestController
@RequestMapping("/collect")
@RequiredArgsConstructor
public class CacheDocCollectController {

    private final ICacheDocCollectService cacheDocCollectService;

    /**
     * 收藏
     *
     * @param type     收藏类型（1-笔记 2-视频）
     * @param targetId 目标ID
     * @param userId   用户ID
     * @return 操作结果
     */
    @Log(title = "收藏管理", businessType = BusinessType.INSERT)
    @Operation(summary = "收藏")
    @PostMapping
    public R<Boolean> collect(
            @Parameter(description = "收藏类型（1-笔记 2-视频）") @RequestParam Integer type,
            @Parameter(description = "目标ID") @RequestParam Long targetId,
            @Parameter(description = "用户ID") @RequestParam Long userId) {
        boolean result = cacheDocCollectService.collect(type, targetId, userId);
        return R.ok(result ? "收藏成功" : "已收藏", result);
    }

    /**
     * 取消收藏
     *
     * @param type     收藏类型
     * @param targetId 目标ID
     * @param userId   用户ID
     * @return 操作结果
     */
    @Log(title = "收藏管理", businessType = BusinessType.DELETE)
    @Operation(summary = "取消收藏")
    @DeleteMapping
    public R<Boolean> uncollect(
            @Parameter(description = "收藏类型") @RequestParam Integer type,
            @Parameter(description = "目标ID") @RequestParam Long targetId,
            @Parameter(description = "用户ID") @RequestParam Long userId) {
        boolean result = cacheDocCollectService.uncollect(type, targetId, userId);
        return R.ok(result ? "取消收藏成功" : "未收藏", result);
    }

    /**
     * 获取收藏数量
     *
     * @param type     收藏类型
     * @param targetId 目标ID
     * @return 收藏数量
     */
    @Operation(summary = "获取收藏数量")
    @GetMapping("/count")
    public R<Long> getCollectCount(
            @Parameter(description = "收藏类型") @RequestParam Integer type,
            @Parameter(description = "目标ID") @RequestParam Long targetId) {
        return R.ok(cacheDocCollectService.getCollectCount(type, targetId));
    }

    /**
     * 判断用户是否已收藏
     *
     * @param type     收藏类型
     * @param targetId 目标ID
     * @param userId   用户ID
     * @return 是否已收藏
     */
    @Operation(summary = "判断用户是否已收藏")
    @GetMapping("/hasCollected")
    public R<Boolean> hasCollected(
            @Parameter(description = "收藏类型") @RequestParam Integer type,
            @Parameter(description = "目标ID") @RequestParam Long targetId,
            @Parameter(description = "用户ID") @RequestParam Long userId) {
        return R.ok(cacheDocCollectService.hasCollected(type, targetId, userId));
    }

    /**
     * 批量获取收藏数量
     *
     * @param type      收藏类型
     * @param targetIds 目标ID列表
     * @return 目标ID与收藏数量的映射
     */
    @Operation(summary = "批量获取收藏数量")
    @GetMapping("/count/batch")
    public R<Map<Long, Long>> getCollectCountBatch(
            @Parameter(description = "收藏类型") @RequestParam Integer type,
            @Parameter(description = "目标ID列表") @RequestParam List<Long> targetIds) {
        return R.ok(cacheDocCollectService.getCollectCountBatch(type, targetIds));
    }

    /**
     * 获取收藏类型列表
     *
     * @return 收藏类型列表
     */
    @Operation(summary = "获取收藏类型列表")
    @GetMapping("/types")
    public R<CacheDocCollectTypeEnum[]> getCollectTypes() {
        return R.ok(CacheDocCollectTypeEnum.values());
    }
}
