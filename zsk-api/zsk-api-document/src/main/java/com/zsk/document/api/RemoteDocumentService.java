package com.zsk.document.api;

import com.zsk.common.core.constant.CommonConstants;
import com.zsk.common.core.domain.R;
import com.zsk.document.api.domain.DocAnalysisMetricApi;
import com.zsk.document.api.domain.DocFilesApi;
import com.zsk.document.api.domain.DocStatisticsApi;
import com.zsk.document.api.domain.DocTimeDistributionApi;
import com.zsk.document.api.domain.DocTrafficItemApi;
import com.zsk.document.api.domain.DocTrendItemApi;
import com.zsk.document.api.factory.RemoteDocumentFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    /**
     * 上传文件
     *
     * @param file 文件
     * @param source 请求来源
     * @return 文件信息
     */
    @PostMapping("/document/files/upload")
    R<DocFilesApi> upload(@RequestPart("file") MultipartFile file, @RequestHeader(CommonConstants.REQUEST_SOURCE_HEADER) String source);

    /**
     * 初始化分片上传
     *
     * @param request 分片上传初始化请求
     * @param source 请求来源
     * @return 上传ID
     */
    @PostMapping("/document/files/multipart/init")
    R<String> initiateMultipartUpload(@RequestBody Object request, @RequestHeader(CommonConstants.REQUEST_SOURCE_HEADER) String source);

    /**
     * 上传分片
     *
     * @param uploadId 上传ID
     * @param partNumber 分片编号
     * @param file 文件
     * @param source 请求来源
     * @return 分片ETag
     * @throws IOException 上传异常
     */
    @PostMapping("/document/files/multipart/upload")
    R<String> uploadPart(
        @RequestParam("uploadId") String uploadId,
        @RequestParam("partNumber") Integer partNumber,
        @RequestPart("file") MultipartFile file,
        @RequestHeader(CommonConstants.REQUEST_SOURCE_HEADER) String source) throws IOException;

    /**
     * 完成分片上传
     *
     * @param request 完成分片上传请求
     * @param source 请求来源
     * @return 响应结果
     */
    @PostMapping("/document/files/multipart/complete")
    R<Void> completeMultipartUpload(@RequestBody Object request, @RequestHeader(CommonConstants.REQUEST_SOURCE_HEADER) String source);

    /**
     * 删除文件
     *
     * @param ids 文件ID（支持逗号分隔的多个ID）
     * @param source 请求来源
     * @return 是否成功
     */
    @DeleteMapping("/document/files/{ids}")
    R<Boolean> remove(@PathVariable String ids, @RequestHeader(CommonConstants.REQUEST_SOURCE_HEADER) String source);
}
