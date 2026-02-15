package com.zsk.document.controller;

import com.zsk.common.core.domain.R;
import com.zsk.common.log.annotation.Log;
import com.zsk.common.log.enums.BusinessType;
import com.zsk.document.enums.CacheDocFollowTypeEnum;
import com.zsk.document.service.ICacheDocFollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 缓存文档关注Controller
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Tag(name = "关注管理")
@RestController
@RequestMapping("/follow")
@RequiredArgsConstructor
public class CacheDocFollowController {

    private final ICacheDocFollowService cacheDocFollowService;

    /**
     * 关注
     *
     * @param type     关注类型（1-用户 2-笔记作者 3-视频作者）
     * @param targetId 目标ID（被关注者ID）
     * @param userId   用户ID（关注者ID）
     * @return 操作结果
     */
    @Log(title = "关注管理", businessType = BusinessType.INSERT)
    @Operation(summary = "关注")
    @PostMapping
    public R<Boolean> follow(
            @Parameter(description = "关注类型（1-用户 2-笔记作者 3-视频作者）") @RequestParam Integer type,
            @Parameter(description = "目标ID") @RequestParam Long targetId,
            @Parameter(description = "用户ID") @RequestParam Long userId) {
        boolean result = cacheDocFollowService.follow(type, targetId, userId);
        return R.ok(result ? "关注成功" : "已关注", result);
    }

    /**
     * 取消关注
     *
     * @param type     关注类型
     * @param targetId 目标ID
     * @param userId   用户ID
     * @return 操作结果
     */
    @Log(title = "关注管理", businessType = BusinessType.DELETE)
    @Operation(summary = "取消关注")
    @DeleteMapping
    public R<Boolean> unfollow(
            @Parameter(description = "关注类型") @RequestParam Integer type,
            @Parameter(description = "目标ID") @RequestParam Long targetId,
            @Parameter(description = "用户ID") @RequestParam Long userId) {
        boolean result = cacheDocFollowService.unfollow(type, targetId, userId);
        return R.ok(result ? "取消关注成功" : "未关注", result);
    }

    /**
     * 获取关注数量（粉丝数）
     *
     * @param type     关注类型
     * @param targetId 目标ID
     * @return 关注数量
     */
    @Operation(summary = "获取关注数量")
    @GetMapping("/count")
    public R<Long> getFollowCount(
            @Parameter(description = "关注类型") @RequestParam Integer type,
            @Parameter(description = "目标ID") @RequestParam Long targetId) {
        return R.ok(cacheDocFollowService.getFollowCount(type, targetId));
    }

    /**
     * 判断用户是否已关注
     *
     * @param type     关注类型
     * @param targetId 目标ID
     * @param userId   用户ID
     * @return 是否已关注
     */
    @Operation(summary = "判断用户是否已关注")
    @GetMapping("/hasFollowed")
    public R<Boolean> hasFollowed(
            @Parameter(description = "关注类型") @RequestParam Integer type,
            @Parameter(description = "目标ID") @RequestParam Long targetId,
            @Parameter(description = "用户ID") @RequestParam Long userId) {
        return R.ok(cacheDocFollowService.hasFollowed(type, targetId, userId));
    }

    /**
     * 批量获取关注数量
     *
     * @param type      关注类型
     * @param targetIds 目标ID列表
     * @return 目标ID与关注数量的映射
     */
    @Operation(summary = "批量获取关注数量")
    @GetMapping("/count/batch")
    public R<Map<Long, Long>> getFollowCountBatch(
            @Parameter(description = "关注类型") @RequestParam Integer type,
            @Parameter(description = "目标ID列表") @RequestParam List<Long> targetIds) {
        return R.ok(cacheDocFollowService.getFollowCountBatch(type, targetIds));
    }

    /**
     * 获取关注类型列表
     *
     * @return 关注类型列表
     */
    @Operation(summary = "获取关注类型列表")
    @GetMapping("/types")
    public R<CacheDocFollowTypeEnum[]> getFollowTypes() {
        return R.ok(CacheDocFollowTypeEnum.values());
    }
}
