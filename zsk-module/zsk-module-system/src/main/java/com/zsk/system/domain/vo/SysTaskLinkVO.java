package com.zsk.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 任务依赖关系 视图对象（匹配 Gantt 组件 ILink 接口）
 *
 * @author wuhuaming
 */
@Data
@Schema(description = "任务依赖关系视图对象")
public class SysTaskLinkVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 依赖关系唯一ID
     */
    @Schema(description = "依赖关系唯一ID")
    private Long id;

    /**
     * 源任务ID（前驱任务）
     */
    @Schema(description = "源任务ID（前驱任务）")
    private Long source;

    /**
     * 目标任务ID（后继任务）
     */
    @Schema(description = "目标任务ID（后继任务）")
    private Long target;

    /**
     * 依赖类型：0-完成开始 1-开始开始 2-完成完成 3-开始完成（dhtmlx原生格式）
     */
    @Schema(description = "依赖类型：0-完成开始 / 1-开始开始 / 2-完成完成 / 3-开始完成")
    private String type;
}