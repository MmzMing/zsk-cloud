package com.zsk.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 仪表盘数据点 视图对象
 * <p>
 * 用于展示仪表盘数据，如内存使用率、CPU使用率等
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-22
 */
@Data
@Schema(description = "仪表盘数据点")
public class GaugeDataPoint implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 当前数值
     */
    @Schema(description = "当前数值")
    private Double value;

    /**
     * 最小值（默认 0）
     */
    @Schema(description = "最小值")
    private Double min = 0D;

    /**
     * 最大值
     */
    @Schema(description = "最大值")
    private Double max;

    /**
     * 显示名称（可选）
     */
    @Schema(description = "显示名称")
    private String name;
}