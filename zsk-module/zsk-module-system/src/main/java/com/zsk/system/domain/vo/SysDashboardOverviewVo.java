package com.zsk.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 仪表盘概览数据 视图对象
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@Schema(description = "仪表盘概览数据")
public class SysDashboardOverviewVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 唯一标识 */
    @Schema(description = "唯一标识")
    private String key;

    /** 显示标签 */
    @Schema(description = "显示标签")
    private String label;

    /** 当前数值 */
    @Schema(description = "当前数值")
    private String value;

    /** 变化量（如：+12.5%） */
    @Schema(description = "变化量")
    private String delta;

    /** 描述说明 */
    @Schema(description = "描述说明")
    private String description;
}
