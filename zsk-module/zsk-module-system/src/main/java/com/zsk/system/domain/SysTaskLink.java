package com.zsk.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zsk.common.datasource.domain.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 任务依赖关系对象 sys_task_link
 *
 * @author wuhuaming
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_task_link")
@Schema(description = "任务依赖关系对象")
public class SysTaskLink extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 源任务ID（前驱任务）
     */
    @Schema(description = "源任务ID（前驱任务）")
    private Long sourceId;

    /**
     * 目标任务ID（后继任务）
     */
    @Schema(description = "目标任务ID（后继任务）")
    private Long targetId;

    /**
     * 依赖类型：0-完成开始 1-开始开始 2-完成完成 3-开始完成（dhtmlx原生格式）
     */
    @Schema(description = "依赖类型：0-完成开始 1-开始开始 2-完成完成 3-开始完成")
    private String type;
}