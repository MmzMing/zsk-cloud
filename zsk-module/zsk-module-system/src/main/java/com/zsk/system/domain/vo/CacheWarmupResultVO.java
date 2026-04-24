package com.zsk.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 缓存预热结果 视图对象
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Data
@Schema(description = "缓存预热结果")
public class CacheWarmupResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 预热的缓存名称
     */
    @Schema(description = "预热的缓存名称")
    private String cacheName;

    /**
     * 预热是否成功
     */
    @Schema(description = "预热是否成功")
    private Boolean success;

    /**
     * 预热数量
     */
    @Schema(description = "预热数量")
    private Integer count;

    /**
     * 耗时（毫秒）
     */
    @Schema(description = "耗时（毫秒）")
    private Long duration;

    /**
     * 错误信息
     */
    @Schema(description = "错误信息")
    private String errorMessage;
}