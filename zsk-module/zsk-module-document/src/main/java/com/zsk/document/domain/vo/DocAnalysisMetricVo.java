package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 分析指标项 视图对象
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分析指标项")
public class DocAnalysisMetricVo implements Serializable {

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

    /** 变化量 */
    @Schema(description = "变化量")
    private String delta;

    /** 描述说明 */
    @Schema(description = "描述说明")
    private String description;

    /** 趋势（up/down/stable） */
    @Schema(description = "趋势（up/down/stable）")
    private String tone;
}
