package com.zsk.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 任务信息 视图对象（匹配 Gantt 组件 ITask 接口）
 *
 * @author wuhuaming
 */
@Data
@Schema(description = "任务信息视图对象")
public class SysTaskVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 任务唯一ID */
    @Schema(description = "任务唯一ID")
    private Long id;

    /** 任务名称 */
    @Schema(description = "任务名称")
    private String text;

    /** 开始时间，格式：yyyy-MM-dd */
    @Schema(description = "开始时间，格式：yyyy-MM-dd")
    private String startDate;

    /** 持续天数 */
    @Schema(description = "持续天数")
    private Integer duration;

    /** 完成进度 0-100 */
    @Schema(description = "完成进度 0-100")
    private Integer progress;

    /** 任务类型：task / project / milestone */
    @Schema(description = "任务类型：task / project / milestone")
    private String type;

    /** 父任务ID，顶级为 0，可选 */
    @Schema(description = "父任务ID，顶级为 0，可选")
    private Long parent;

    /** 是否默认展开 */
    @Schema(description = "是否默认展开")
    private Boolean open;

    /** 任务描述/备注 */
    @Schema(description = "任务描述/备注")
    private String details;

    /** 任务颜色 */
    @Schema(description = "任务颜色")
    private String color;
}