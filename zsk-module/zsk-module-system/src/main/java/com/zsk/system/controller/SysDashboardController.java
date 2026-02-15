package com.zsk.system.controller;

import com.zsk.common.core.domain.R;
import com.zsk.system.domain.vo.SysAnalysisMetricVo;
import com.zsk.system.domain.vo.SysDashboardOverviewVo;
import com.zsk.system.domain.vo.SysDashboardTrafficVo;
import com.zsk.system.domain.vo.SysDashboardTrendVo;
import com.zsk.system.domain.vo.SysTimeDistributionVo;
import com.zsk.system.service.ISysDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 仪表盘 控制器
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
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

    /**
     * 获取流量统计数据
     *
     * @param range 时间维度（day/week/month）
     * @return 流量统计列表
     */
    @Operation(summary = "获取流量统计数据")
    @GetMapping("/traffic")
    public R<List<SysDashboardTrafficVo>> getTraffic(
        @Parameter(description = "时间维度（day/week/month）")
        @RequestParam(value = "range", required = false) String range) {
        return R.ok(dashboardService.getTraffic(range));
    }

    /**
     * 获取访问量趋势数据
     *
     * @param range 时间维度（day/week）
     * @return 趋势数据列表
     */
    @Operation(summary = "获取访问量趋势数据")
    @GetMapping("/trend")
    public R<List<SysDashboardTrendVo>> getTrend(
        @Parameter(description = "时间维度（day/week）")
        @RequestParam(value = "range", required = false) String range) {
        return R.ok(dashboardService.getTrend(range));
    }

    /**
     * 获取分析指标数据
     *
     * @return 分析指标列表
     */
    @Operation(summary = "获取分析指标数据")
    @GetMapping("/analysis/metrics")
    public R<List<SysAnalysisMetricVo>> getAnalysisMetrics() {
        return R.ok(dashboardService.getAnalysisMetrics());
    }

    /**
     * 获取时间分布数据
     *
     * @param date 日期
     * @param step 步长（hour/half-hour）
     * @return 时间分布列表
     */
    @Operation(summary = "获取时间分布数据")
    @GetMapping("/analysis/time-distribution")
    public R<List<SysTimeDistributionVo>> getTimeDistribution(
        @Parameter(description = "日期（yyyy-MM-dd）")
        @RequestParam(value = "date", required = false) String date,
        @Parameter(description = "步长（hour/half-hour）")
        @RequestParam(value = "step", required = false) String step) {
        return R.ok(dashboardService.getTimeDistribution(date, step));
    }
}
