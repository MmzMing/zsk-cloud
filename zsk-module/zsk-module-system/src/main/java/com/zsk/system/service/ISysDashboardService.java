package com.zsk.system.service;

import com.zsk.system.domain.vo.SysAnalysisMetricVo;
import com.zsk.system.domain.vo.SysDashboardOverviewVo;
import com.zsk.system.domain.vo.SysDashboardTrafficVo;
import com.zsk.system.domain.vo.SysDashboardTrendVo;
import com.zsk.system.domain.vo.SysTimeDistributionVo;

import java.util.List;

/**
 * 仪表盘 服务接口
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
public interface ISysDashboardService {

    /**
     * 获取概览数据
     *
     * @return 概览数据列表
     */
    List<SysDashboardOverviewVo> getOverview();

    /**
     * 获取流量统计数据
     *
     * @param range 时间维度（day/week/month）
     * @return 流量统计列表
     */
    List<SysDashboardTrafficVo> getTraffic(String range);

    /**
     * 获取访问量趋势数据
     *
     * @param range 时间维度（day/week）
     * @return 趋势数据列表
     */
    List<SysDashboardTrendVo> getTrend(String range);

    /**
     * 获取分析指标数据
     *
     * @return 分析指标列表
     */
    List<SysAnalysisMetricVo> getAnalysisMetrics();

    /**
     * 获取时间分布数据
     *
     * @param date 日期
     * @param step 步长（hour/half-hour）
     * @return 时间分布列表
     */
    List<SysTimeDistributionVo> getTimeDistribution(String date, String step);
}
