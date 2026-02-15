package com.zsk.document.api.factory;

import com.zsk.common.core.domain.R;
import com.zsk.document.api.RemoteDocumentService;
import com.zsk.document.api.domain.DocAnalysisMetricApi;
import com.zsk.document.api.domain.DocStatisticsApi;
import com.zsk.document.api.domain.DocTimeDistributionApi;
import com.zsk.document.api.domain.DocTrafficItemApi;
import com.zsk.document.api.domain.DocTrendItemApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 文档服务降级处理
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Component
public class RemoteDocumentFallbackFactory implements FallbackFactory<RemoteDocumentService> {

    private static final Logger log = LoggerFactory.getLogger(RemoteDocumentFallbackFactory.class);

    @Override
    public RemoteDocumentService create(Throwable throwable) {
        log.error("文档服务调用失败:{}", throwable.getMessage());
        return new RemoteDocumentService() {
            @Override
            public R<DocStatisticsApi> getStatisticsOverview(String source) {
                return R.fail("获取文档统计信息失败:" + throwable.getMessage());
            }

            @Override
            public R<List<DocTrafficItemApi>> getTrafficStatistics(String range, String source) {
                return R.fail("获取流量统计数据失败:" + throwable.getMessage());
            }

            @Override
            public R<List<DocTrendItemApi>> getTrendStatistics(String range, String source) {
                return R.fail("获取趋势数据失败:" + throwable.getMessage());
            }

            @Override
            public R<List<DocAnalysisMetricApi>> getAnalysisMetrics(String source) {
                return R.fail("获取分析指标数据失败:" + throwable.getMessage());
            }

            @Override
            public R<List<DocTimeDistributionApi>> getTimeDistribution(String date, String step, String source) {
                return R.fail("获取时间分布数据失败:" + throwable.getMessage());
            }
        };
    }
}
