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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
 * @version 2.0
 * @date 2026-04-22
 */
@Tag(name = "行为审计")
@RestController
@RequestMapping("/monitor/behavior")
@RequiredArgsConstructor
public class SysBehaviorController {

    /** 行为审计服务 */
    private final ISysBehaviorService behaviorService;

    /**
     * 获取行为审计用户列表
     * <p>
     * 聚合 operName 维度，统计每个用户的行为次数、最近操作时间、IP及风险等级
     *
     * @return 用户行为聚合列表
     */
    @Operation(summary = "获取行为审计用户列表")
    @GetMapping("/users")
    public R<List<SysBehaviorUserVO>> listUsers() {
        return R.ok(behaviorService.listBehaviorUsers());
    }

    /**
     * 分页查询用户行为列表
     * <p>
     * 支持按用户/业务类型/标题/IP/状态/时间范围等多条件分页查询
     *
     * @param query 查询条件
     * @return 分页行为事件列表
     */
    @Operation(summary = "分页查询用户行为列表")
    @GetMapping("/events")
    public R<PageResult<SysBehaviorEventVO>> pageEvents(@Valid SysBehaviorQuery query) {
        return R.ok(behaviorService.pageEvents(query));
    }

    /**
     * 获取行为详情（完整请求/响应）
     * <p>
     * 根据行为记录ID返回完整的操作日志详情，包含请求参数和响应结果
     *
     * @param id 行为记录ID
     * @return 行为详情
     */
    @Operation(summary = "获取行为详情")
    @GetMapping("/{id}")
    public R<SysBehaviorDetailVO> getDetail(
            @Parameter(description = "行为记录ID", required = true) @PathVariable String id) {
        return R.ok(behaviorService.getDetail(id));
    }
}
