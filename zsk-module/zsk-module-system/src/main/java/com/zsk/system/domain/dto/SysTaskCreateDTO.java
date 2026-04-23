package com.zsk.system.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 创建任务 请求数据传输对象
 *
 * @author wuhuaming
 */
@Data
@Schema(description = "创建任务请求")
public class SysTaskCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 任务名称 */
    @NotBlank(message = "任务名称不能为空")
    @Schema(description = "任务名称")
    private String text;

    /** 开始时间，格式：yyyy-MM-dd */
    @NotBlank(message = "开始时间不能为空")
    @Schema(description = "开始时间，格式：yyyy-MM-dd（如：2026-04-30）")
    private String startDate;

    /** 持续天数 */
    @NotNull(message = "持续天数不能为空")
    @Min(value = 0, message = "持续天数不能为负数")
    @Schema(description = "持续天数")
    private Integer duration;

    /** 完成进度 0-100 */
    @Min(value = 0, message = "进度不能小于0")
    @Max(value = 100, message = "进度不能大于100")
    @Schema(description = "完成进度 0-100，默认 0")
    private Integer progress;

    /** 任务类型：task / project / milestone */
    @NotBlank(message = "任务类型不能为空")
    @Schema(description = "任务类型：task-普通任务 / project-项目 / milestone-里程碑")
    private String type;

    /** 父任务ID，可选 */
    @Schema(description = "父任务ID，可选（顶级任务不传或传0）")
    private Long parent;

    /** 任务描述 */
    @Schema(description = "任务描述/备注")
    private String details;

    /** 任务颜色，可选 */
    @Schema(description = "任务颜色，可选")
    private String color;
}