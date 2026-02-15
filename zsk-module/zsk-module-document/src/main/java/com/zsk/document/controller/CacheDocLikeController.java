package com.zsk.document.controller;

import com.zsk.common.core.domain.R;
import com.zsk.common.log.annotation.Log;
import com.zsk.common.log.enums.BusinessType;
import com.zsk.document.enums.CacheDocLikeTypeEnum;
import com.zsk.document.service.ICacheDocLikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 缓存文档点赞Controller
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Tag(name = "点赞管理")
@RestController
@RequestMapping("/like")
@RequiredArgsConstructor
public class CacheDocLikeController {

    private final ICacheDocLikeService cacheDocLikeService;

    /**
     * 点赞
     *
     * @param type     点赞类型（1-笔记 2-笔记评论 3-视频 4-视频评论）
     * @param targetId 目标ID
     * @param userId   用户ID
     * @return 操作结果
     */
    @Log(title = "点赞管理", businessType = BusinessType.INSERT)
    @Operation(summary = "点赞")
    @PostMapping
    public R<Boolean> like(
            @Parameter(description = "点赞类型（1-笔记 2-笔记评论 3-视频 4-视频评论）") @RequestParam Integer type,
            @Parameter(description = "目标ID") @RequestParam Long targetId,
            @Parameter(description = "用户ID") @RequestParam Long userId) {
        boolean result = cacheDocLikeService.like(type, targetId, userId);
        return R.ok(result ? "点赞成功" : "已点赞", result);
    }

    /**
     * 取消点赞
     *
     * @param type     点赞类型
     * @param targetId 目标ID
     * @param userId   用户ID
     * @return 操作结果
     */
    @Log(title = "点赞管理", businessType = BusinessType.DELETE)
    @Operation(summary = "取消点赞")
    @DeleteMapping
    public R<Boolean> unlike(
            @Parameter(description = "点赞类型") @RequestParam Integer type,
            @Parameter(description = "目标ID") @RequestParam Long targetId,
            @Parameter(description = "用户ID") @RequestParam Long userId) {
        boolean result = cacheDocLikeService.unlike(type, targetId, userId);
        return R.ok(result ? "取消点赞成功" : "未点赞", result);
    }

    /**
     * 获取点赞数量
     *
     * @param type     点赞类型
     * @param targetId 目标ID
     * @return 点赞数量
     */
    @Operation(summary = "获取点赞数量")
    @GetMapping("/count")
    public R<Long> getLikeCount(
            @Parameter(description = "点赞类型") @RequestParam Integer type,
            @Parameter(description = "目标ID") @RequestParam Long targetId) {
        return R.ok(cacheDocLikeService.getLikeCount(type, targetId));
    }

    /**
     * 判断用户是否已点赞
     *
     * @param type     点赞类型
     * @param targetId 目标ID
     * @param userId   用户ID
     * @return 是否已点赞
     */
    @Operation(summary = "判断用户是否已点赞")
    @GetMapping("/hasLiked")
    public R<Boolean> hasLiked(
            @Parameter(description = "点赞类型") @RequestParam Integer type,
            @Parameter(description = "目标ID") @RequestParam Long targetId,
            @Parameter(description = "用户ID") @RequestParam Long userId) {
        return R.ok(cacheDocLikeService.hasLiked(type, targetId, userId));
    }

    /**
     * 批量获取点赞数量
     *
     * @param type      点赞类型
     * @param targetIds 目标ID列表
     * @return 目标ID与点赞数量的映射
     */
    @Operation(summary = "批量获取点赞数量")
    @GetMapping("/count/batch")
    public R<Map<Long, Long>> getLikeCountBatch(
            @Parameter(description = "点赞类型") @RequestParam Integer type,
            @Parameter(description = "目标ID列表") @RequestParam List<Long> targetIds) {
        return R.ok(cacheDocLikeService.getLikeCountBatch(type, targetIds));
    }

    /**
     * 获取点赞类型列表
     *
     * @return 点赞类型列表
     */
    @Operation(summary = "获取点赞类型列表")
    @GetMapping("/types")
    public R<CacheDocLikeTypeEnum[]> getLikeTypes() {
        return R.ok(CacheDocLikeTypeEnum.values());
    }
}
