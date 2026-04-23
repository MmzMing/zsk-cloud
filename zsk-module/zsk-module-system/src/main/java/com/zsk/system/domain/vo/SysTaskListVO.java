package com.zsk.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Gantt 任务列表 视图对象（包含 tasks + links）
 *
 * @author wuhuaming
 */
@Data
@Schema(description = "Gantt 任务列表视图对象")
public class SysTaskListVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 任务列表（扁平结构，通过 parent 表示层级） */
    @Schema(description = "任务列表（扁平结构，通过 parent 表示层级）")
    private List<SysTaskVO> tasks;

    /** 任务依赖关系列表 */
    @Schema(description = "任务依赖关系列表")
    private List<SysTaskLinkVO> links;
}
