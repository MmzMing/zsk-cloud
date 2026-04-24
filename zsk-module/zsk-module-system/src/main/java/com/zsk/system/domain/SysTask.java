package com.zsk.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zsk.common.datasource.domain.entity.TenantEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 任务管理对象 sys_task
 *
 * @author wuhuaming
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_task")
@Schema(description = "任务管理对象")
public class SysTask extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 任务名称
     */
    @Schema(description = "任务名称")
    private String text;

    /**
     * 开始时间
     */
    @Schema(description = "开始时间")
    private LocalDateTime startDate;

    /**
     * 持续天数
     */
    @Schema(description = "持续天数")
    private Integer duration;

    /**
     * 进度 0-100
     */
    @Schema(description = "进度 0-100")
    private Integer progress;

    /**
     * 任务类型：task-普通任务 project-项目 milestone-里程碑
     */
    @Schema(description = "任务类型：task-普通任务 project-项目 milestone-里程碑")
    private String type;

    /**
     * 父任务ID，顶级为 0，可选
     */
    @Schema(description = "父任务ID，顶级为 0，可选")
    private Long parentId;

    /**
     * 是否展开（0否 1是）
     */
    @Schema(description = "是否展开（0否 1是）")
    private Integer openFlag;

    /**
     * 任务描述
     */
    @Schema(description = "任务描述")
    private String details;

    /**
     * 任务颜色
     */
    @Schema(description = "任务颜色")
    private String color;
}