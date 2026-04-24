package com.zsk.system.controller;

import com.zsk.common.core.domain.R;
import com.zsk.system.domain.vo.SysDashboardOverviewVo;
import com.zsk.system.service.ISysDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 仪表盘 控制器
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Tag(name = "仪表盘")
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class SysDashboardController {

    private final ISysDashboardService dashboardService;

    /**
     * 获取概览数据
     *
     * @return 概览数据列表
     */
    @Operation(summary = "获取概览数据")
    @GetMapping("/overview")
    public R<List<SysDashboardOverviewVo>> getOverview() {
        return R.ok(dashboardService.getOverview());
    }
}
