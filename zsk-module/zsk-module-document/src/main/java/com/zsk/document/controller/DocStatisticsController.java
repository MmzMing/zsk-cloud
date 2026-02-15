package com.zsk.document.controller;

import com.zsk.common.core.domain.R;
import com.zsk.document.domain.vo.DocAnalysisMetricVo;
import com.zsk.document.domain.vo.DocStatisticsVo;
import com.zsk.document.domain.vo.DocTimeDistributionVo;
import com.zsk.document.domain.vo.DocTrafficItemVo;
import com.zsk.document.domain.vo.DocTrendItemVo;
import com.zsk.document.service.IDocStatisticsService;
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
 * 文档统计 控制器
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Tag(name = "文档统计")
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class DocStatisticsController {

    private final IDocStatisticsService statisticsService;

    /**
     * 获取文档统计概览数据
     *
     * @return 统计数据
     */
    @Operation(summary = "获取文档统计概览数据")
    @GetMapping("/overview")
    public R<DocStatisticsVo> getOverview() {
        return R.ok(statisticsService.getStatisticsOverview());
    }

    /**
     * 获取流量统计数据
     *
     * @param range 时间维度（day/week/month）
     * @return 流量统计列表
     */
    @Operation(summary = "获取流量统计数据")
    @GetMapping("/traffic")
    public R<List<DocTrafficItemVo>> getTraffic(
        @Parameter(description = "时间维度（day/week/month）")
        @RequestParam(value = "range", required = false) String range) {
        return R.ok(statisticsService.getTrafficStatistics(range));
    }

    /**
     * 获取访问量趋势数据
     *
     * @param range 时间维度（day/week）
     * @return 趋势数据列表
     */
    @Operation(summary = "获取访问量趋势数据")
    @GetMapping("/trend")
    public R<List<DocTrendItemVo>> getTrend(
        @Parameter(description = "时间维度（day/week）")
        @RequestParam(value = "range", required = false) String range) {
        return R.ok(statisticsService.getTrendStatistics(range));
    }

    /**
     * 获取分析指标数据
     *
     * @return 分析指标列表
     */
    @Operation(summary = "获取分析指标数据")
    @GetMapping("/analysis/metrics")
    public R<List<DocAnalysisMetricVo>> getAnalysisMetrics() {
        return R.ok(statisticsService.getAnalysisMetrics());
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
    public R<List<DocTimeDistributionVo>> getTimeDistribution(
        @Parameter(description = "日期（yyyy-MM-dd）")
        @RequestParam(value = "date", required = false) String date,
        @Parameter(description = "步长（hour/half-hour）")
        @RequestParam(value = "step", required = false) String step) {
        return R.ok(statisticsService.getTimeDistribution(date, step));
    }
}
