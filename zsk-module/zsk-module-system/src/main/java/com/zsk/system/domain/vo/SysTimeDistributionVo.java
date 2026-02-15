package com.zsk.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 时间分布项 视图对象
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "时间分布项")
public class SysTimeDistributionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 类型（文档/视频） */
    @Schema(description = "类型（文档/视频）")
    private String type;

    /** 时间点 */
    @Schema(description = "时间点")
    private String time;

    /** 数值 */
    @Schema(description = "数值")
    private Long value;
}
