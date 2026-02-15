package com.zsk.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 流量统计项 视图对象
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "流量统计项")
public class SysDashboardTrafficVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 类型（文档/视频） */
    @Schema(description = "类型（文档/视频）")
    private String type;

    /** 日期/周期标签 */
    @Schema(description = "日期/周期标签")
    private String date;

    /** 数量 */
    @Schema(description = "数量")
    private Long value;
}
