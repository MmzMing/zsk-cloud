package com.zsk.document.controller;

import com.zsk.common.core.domain.R;
import com.zsk.common.security.utils.SecurityUtils;
import com.zsk.document.domain.vo.AllStatsVo;
import com.zsk.document.domain.vo.UserStatsVo;
import com.zsk.document.service.IDocStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户统计信息 控制器
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Tag(name = "用户统计信息")
@RestController
@RequestMapping("/docAllContent")
@RequiredArgsConstructor
public class DocAllContentController {

    private final IDocStatsService docStatsService;

    /**
     * 获取用户统计信息（点赞、关注、收藏总数）
     *
     * @return 用户统计信息
     */
    @Operation(summary = "获取用户统计信息")
    @GetMapping("/user/stats")
    public R<UserStatsVo> getUserStats() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }
        UserStatsVo statsVo = docStatsService.getUserStats(userId);
        return R.ok(statsVo);
    }

    /**
     * 获取内容统计信息（文章总数、视频总数、评论总数）
     *
     * @return 内容统计信息
     */
    @Operation(summary = "获取内容统计信息")
    @GetMapping("/content/stats")
    public R<AllStatsVo> getContentStats() {
        AllStatsVo statsVo = docStatsService.getContentStats();
        return R.ok(statsVo);
    }

    /**
     * 获取当前用户ID
     * 尝试从安全工具类获取当前登录用户ID，如果获取失败则返回null
     *
     * @return 当前用户ID，未登录或获取失败返回null
     */
    private Long getCurrentUserId() {
        try {
            return SecurityUtils.getUserId();
        } catch (Exception e) {
            return null;
        }
    }
}