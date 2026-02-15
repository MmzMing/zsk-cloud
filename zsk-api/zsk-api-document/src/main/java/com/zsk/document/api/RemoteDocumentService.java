package com.zsk.document.api;

import com.zsk.common.core.constant.CommonConstants;
import com.zsk.common.core.domain.R;
import com.zsk.document.api.domain.DocAnalysisMetricApi;
import com.zsk.document.api.domain.DocStatisticsApi;
import com.zsk.document.api.domain.DocTimeDistributionApi;
import com.zsk.document.api.domain.DocTrafficItemApi;
import com.zsk.document.api.domain.DocTrendItemApi;
import com.zsk.document.api.factory.RemoteDocumentFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 文档服务远程调用接口
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@FeignClient(contextId = "remoteDocumentService", value = "zsk-module-document", fallbackFactory = RemoteDocumentFallbackFactory.class)
public interface RemoteDocumentService {

    /**
     * 获取文档统计信息
     *
     * @param source 请求来源
     * @return 统计信息
     */
    @GetMapping("/document/statistics/overview")
    R<DocStatisticsApi> getStatisticsOverview(@RequestHeader(CommonConstants.REQUEST_SOURCE_HEADER) String source);

    /**
     * 获取流量统计数据
     *
     * @param range 时间维度（day/week/month）
     * @param source 请求来源
     * @return 流量统计列表
     */
    @GetMapping("/document/statistics/traffic")
    R<List<DocTrafficItemApi>> getTrafficStatistics(
        @RequestParam(value = "range", required = false) String range,
        @RequestHeader(CommonConstants.REQUEST_SOURCE_HEADER) String source);

    /**
     * 获取访问量趋势数据
     *
     * @param range 时间维度（day/week）
     * @param source 请求来源
     * @return 趋势数据列表
     */
    @GetMapping("/document/statistics/trend")
    R<List<DocTrendItemApi>> getTrendStatistics(
        @RequestParam(value = "range", required = false) String range,
        @RequestHeader(CommonConstants.REQUEST_SOURCE_HEADER) String source);

    /**
     * 获取分析指标数据
     *
     * @param source 请求来源
     * @return 分析指标列表
     */
    @GetMapping("/document/statistics/analysis/metrics")
    R<List<DocAnalysisMetricApi>> getAnalysisMetrics(@RequestHeader(CommonConstants.REQUEST_SOURCE_HEADER) String source);

    /**
     * 获取时间分布数据
     *
     * @param date 日期
     * @param step 步长（hour/half-hour）
     * @param source 请求来源
     * @return 时间分布列表
     */
    @GetMapping("/document/statistics/analysis/time-distribution")
    R<List<DocTimeDistributionApi>> getTimeDistribution(
        @RequestParam(value = "date", required = false) String date,
        @RequestParam(value = "step", required = false) String step,
        @RequestHeader(CommonConstants.REQUEST_SOURCE_HEADER) String source);
}
