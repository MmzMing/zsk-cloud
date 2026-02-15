package com.zsk.system.controller;

import com.zsk.common.core.domain.R;
import com.zsk.system.service.ISysBehaviorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 行为审计 控制器
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Tag(name = "行为审计")
@RestController
@RequestMapping("/monitor/behavior")
@RequiredArgsConstructor
public class SysBehaviorController {

    private final ISysBehaviorService behaviorService;

    /**
     * 获取行为审计用户列表
     *
     * @return 用户列表
     */
    @Operation(summary = "获取行为审计用户列表")
    @GetMapping("/users")
    public R<List<Map<String, Object>>> getUsers() {
        return R.ok(behaviorService.getUsers());
    }

    /**
     * 获取用户行为时间轴
     *
     * @param userId 用户ID
     * @param range 时间范围
     * @return 行为数据点列表
     */
    @Operation(summary = "获取用户行为时间轴")
    @GetMapping("/timeline")
    public R<List<Map<String, Object>>> getTimeline(
            @RequestParam String userId,
            @RequestParam(defaultValue = "today") String range) {
        return R.ok(behaviorService.getTimeline(userId, range));
    }

    /**
     * 获取行为审计事件列表
     *
     * @param userId 用户ID
     * @param keyword 关键字
     * @return 事件列表
     */
    @Operation(summary = "获取行为审计事件列表")
    @GetMapping("/events")
    public R<List<Map<String, Object>>> getEvents(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String keyword) {
        return R.ok(behaviorService.getEvents(userId, keyword));
    }
}
