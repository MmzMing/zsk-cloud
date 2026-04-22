package com.zsk.system.controller;

import com.zsk.common.core.domain.R;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.system.domain.dto.SysBehaviorQuery;
import com.zsk.system.domain.vo.SysBehaviorDetailVO;
import com.zsk.system.domain.vo.SysBehaviorEventVO;
import com.zsk.system.domain.vo.SysBehaviorUserVO;
import com.zsk.system.service.ISysBehaviorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 行为审计 控制器 (v2)
 * <p>
 * 数据源：MongoDB sys_oper_log。提供：
 * <ul>
 *   <li>GET  /monitor/behavior/users   - 用户列表（聚合）</li>
 *   <li>GET  /monitor/behavior/events  - 行为分页列表（多条件）</li>
 *   <li>GET  /monitor/behavior/{id}    - 行为详情（完整请求/响应）</li>
 * </ul>
 *
 * @author wuhuaming
 * @date 2026-04-22
 * @version 2.0
 */
@Tag(name = "行为审计")
@RestController
@RequestMapping("/monitor/behavior")
@RequiredArgsConstructor
public class SysBehaviorController {

    private final ISysBehaviorService behaviorService;

    /**
     * 获取行为审计用户列表
     */
    @Operation(summary = "获取行为审计用户列表")
    @GetMapping("/users")
    public R<List<SysBehaviorUserVO>> listUsers() {
        return R.ok(behaviorService.listBehaviorUsers());
    }

    /**
     * 分页查询用户行为列表
     */
    @Operation(summary = "分页查询用户行为列表")
    @GetMapping("/events")
    public R<PageResult<SysBehaviorEventVO>> pageEvents(@Valid SysBehaviorQuery query) {
        return R.ok(behaviorService.pageEvents(query));
    }

    /**
     * 获取行为详情（完整请求/响应）
     */
    @Operation(summary = "获取行为详情")
    @GetMapping("/{id}")
    public R<SysBehaviorDetailVO> getDetail(
            @Parameter(description = "行为记录ID", required = true) @PathVariable String id) {
        return R.ok(behaviorService.getDetail(id));
    }
}
