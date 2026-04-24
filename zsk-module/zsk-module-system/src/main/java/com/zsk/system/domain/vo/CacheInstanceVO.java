package com.zsk.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 缓存实例信息 视图对象
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Data
@Schema(description = "缓存实例信息")
public class CacheInstanceVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 实例ID
     */
    @Schema(description = "实例ID")
    private String instanceId;

    /**
     * 实例名称
     */
    @Schema(description = "实例名称")
    private String instanceName;

    /**
     * 缓存类型
     */
    @Schema(description = "缓存类型")
    private String cacheType;

    /**
     * 状态
     */
    @Schema(description = "状态")
    private String status;

    /**
     * 缓存数量
     */
    @Schema(description = "缓存数量")
    private Long cacheCount;

    /**
     * 内存使用（字节）
     */
    @Schema(description = "内存使用（字节）")
    private Long memoryUsed;

    /**
     * 命中率
     */
    @Schema(description = "命中率")
    private Double hitRate;

    /**
     * QPS
     */
    @Schema(description = "QPS")
    private Long qps;
}