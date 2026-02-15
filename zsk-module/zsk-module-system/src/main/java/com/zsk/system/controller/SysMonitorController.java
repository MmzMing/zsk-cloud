package com.zsk.system.controller;

import com.zsk.common.core.domain.R;
import com.zsk.system.domain.SysMonitorData;
import com.zsk.system.service.ISysMonitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统监控 控制器
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Tag(name = "系统监控")
@RestController
@RequestMapping("/monitor")
@RequiredArgsConstructor
public class SysMonitorController {

    private final ISysMonitorService monitorService;

    /**
     * 获取服务器实时监控数据
     *
     * @return 监控数据点列表
     */
    @Operation(summary = "获取服务器实时监控数据")
    @GetMapping("/data")
    public R<List<Map<String, Object>>> getMonitorData() {
        SysMonitorData data = monitorService.getRealTimeData();
        List<Map<String, Object>> points = buildMonitorPoints(data);
        return R.ok(points);
    }

    /**
     * 获取服务器监控概览
     *
     * @return 监控概览数据
     */
    @Operation(summary = "获取服务器监控概览")
    @GetMapping("/overview")
    public R<Map<String, Object>> getOverview() {
        SysMonitorData data = monitorService.getOverview();
        Map<String, Object> overview = new HashMap<>();
        overview.put("cpu", data.getCpuUsage());
        overview.put("memory", data.getMemUsage());
        overview.put("disk", data.getDiskUsage());
        overview.put("network", data.getNetUsage());
        overview.put("jvmHeap", data.getJvmHeapUsage());
        overview.put("jvmThread", data.getJvmThreadCount());
        overview.put("hostName", data.getHostName());
        overview.put("hostIp", data.getHostIp());
        overview.put("osName", data.getOsName());
        return R.ok(overview);
    }

    /**
     * 获取服务器监控趋势
     *
     * @param metric 指标类型
     * @param range 时间范围
     * @return 监控趋势数据
     */
    @Operation(summary = "获取服务器监控趋势")
    @GetMapping("/trend")
    public R<List<Map<String, Object>>> getTrend(
            @RequestParam(defaultValue = "cpu") String metric,
            @RequestParam(defaultValue = "1h") String range) {
        List<SysMonitorData> dataList = monitorService.getTrendData(metric, range);
        List<Map<String, Object>> trend = dataList.stream()
                .map(data -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("time", data.getCollectTime().toString());
                    item.put("value", getMetricValue(data, metric));
                    item.put("metric", metric);
                    return item;
                })
                .toList();
        return R.ok(trend);
    }

    /**
     * 手动触发监控数据采集
     *
     * @return 是否成功
     */
    @Operation(summary = "手动触发监控数据采集")
    @PostMapping("/collect")
    public R<Void> collect() {
        monitorService.collectAndSave();
        return R.ok();
    }

    /**
     * 清理过期监控数据
     *
     * @param days 保留天数
     * @return 是否成功
     */
    @Operation(summary = "清理过期监控数据")
    @DeleteMapping("/clean")
    public R<Void> clean(@RequestParam(defaultValue = "7") Integer days) {
        monitorService.cleanExpiredData(days);
        return R.ok();
    }

    /**
     * 构建监控数据点列表
     *
     * @param data 监控数据
     * @return 数据点列表
     */
    private List<Map<String, Object>> buildMonitorPoints(SysMonitorData data) {
        List<Map<String, Object>> points = new ArrayList<>();
        String time = data.getCollectTime().toString();

        addPoint(points, time, data.getCpuUsage(), "cpu");
        addPoint(points, time, data.getMemUsage(), "memory");
        addPoint(points, time, data.getDiskUsage(), "disk");
        addPoint(points, time, data.getNetUsage(), "network");
        addPoint(points, time, data.getJvmHeapUsage(), "jvmHeap");

        return points;
    }

    /**
     * 添加数据点
     */
    private void addPoint(List<Map<String, Object>> points, String time, Double value, String metric) {
        Map<String, Object> point = new HashMap<>();
        point.put("time", time);
        point.put("value", value != null ? Math.round(value * 100.0) / 100.0 : 0);
        point.put("metric", metric);
        points.add(point);
    }

    /**
     * 获取指定指标的值
     */
    private Double getMetricValue(SysMonitorData data, String metric) {
        return switch (metric) {
            case "cpu" -> data.getCpuUsage();
            case "memory" -> data.getMemUsage();
            case "disk" -> data.getDiskUsage();
            case "network" -> data.getNetUsage();
            case "jvmHeap" -> data.getJvmHeapUsage();
            case "jvmThread" -> data.getJvmThreadCount() != null ? data.getJvmThreadCount().doubleValue() : 0.0;
            default -> 0.0;
        };
    }
}
