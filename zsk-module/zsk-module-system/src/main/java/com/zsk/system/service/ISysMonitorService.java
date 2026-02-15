package com.zsk.system.service;

import com.zsk.system.domain.SysMonitorData;

import java.util.List;

/**
 * 系统监控 服务接口
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
public interface ISysMonitorService {

    /**
     * 获取服务器实时监控数据
     *
     * @return 监控数据
     */
    SysMonitorData getRealTimeData();

    /**
     * 获取监控概览数据
     *
     * @return 概览数据
     */
    SysMonitorData getOverview();

    /**
     * 获取监控趋势数据
     *
     * @param metric 指标类型（cpu/memory/disk/network/jvmHeap/jvmThread）
     * @param range 时间范围（1h/24h/7d）
     * @return 趋势数据列表
     */
    List<SysMonitorData> getTrendData(String metric, String range);

    /**
     * 采集并保存监控数据
     */
    void collectAndSave();

    /**
     * 清理过期监控数据
     *
     * @param days 保留天数
     */
    void cleanExpiredData(int days);
}
