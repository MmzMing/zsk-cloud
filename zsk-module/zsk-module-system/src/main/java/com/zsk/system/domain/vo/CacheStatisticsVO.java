package com.zsk.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 缓存统计信息 视图对象
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@Schema(description = "缓存统计信息")
public class CacheStatisticsVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 缓存键总数 */
    @Schema(description = "缓存键总数")
    private Long totalKeys;

    /** 内存使用（字节） */
    @Schema(description = "内存使用（字节）")
    private Long memoryUsed;

    /** 命中率 */
    @Schema(description = "命中率")
    private Double hitRate;

    /** QPS */
    @Schema(description = "QPS")
    private Long qps;

    /** 缓存命中率趋势 */
    @Schema(description = "缓存命中率趋势")
    private Double hitRateTrend;

    /** QPS趋势 */
    @Schema(description = "QPS趋势")
    private Double qpsTrend;

    /** 今日写入次数 */
    @Schema(description = "今日写入次数")
    private Long todayWrites;

    /** 今日读取次数 */
    @Schema(description = "今日读取次数")
    private Long todayReads;
}