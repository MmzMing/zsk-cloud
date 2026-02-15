package com.zsk.document.service;

import com.zsk.document.domain.vo.DocAnalysisMetricVo;
import com.zsk.document.domain.vo.DocStatisticsVo;
import com.zsk.document.domain.vo.DocTimeDistributionVo;
import com.zsk.document.domain.vo.DocTrafficItemVo;
import com.zsk.document.domain.vo.DocTrendItemVo;

import java.util.List;

/**
 * 文档统计 服务接口
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
public interface IDocStatisticsService {

    /**
     * 获取文档统计概览数据
     *
     * @return 统计数据
     */
    DocStatisticsVo getStatisticsOverview();

    /**
     * 获取流量统计数据
     *
     * @param range 时间维度（day/week/month）
     * @return 流量统计列表
     */
    List<DocTrafficItemVo> getTrafficStatistics(String range);

    /**
     * 获取访问量趋势数据
     *
     * @param range 时间维度（day/week）
     * @return 趋势数据列表
     */
    List<DocTrendItemVo> getTrendStatistics(String range);

    /**
     * 获取分析指标数据
     *
     * @return 分析指标列表
     */
    List<DocAnalysisMetricVo> getAnalysisMetrics();

    /**
     * 获取时间分布数据
     *
     * @param date 日期
     * @param step 步长（hour/half-hour）
     * @return 时间分布列表
     */
    List<DocTimeDistributionVo> getTimeDistribution(String date, String step);
}
