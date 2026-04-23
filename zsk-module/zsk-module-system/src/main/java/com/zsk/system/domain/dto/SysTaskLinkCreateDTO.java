package com.zsk.system.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 创建任务依赖 请求数据传输对象
 *
 * @author wuhuaming
 */
@Data
@Schema(description = "创建任务依赖请求")
public class SysTaskLinkCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 源任务ID（前驱任务） */
    @NotNull(message = "源任务ID不能为空")
    @Schema(description = "源任务ID（前驱任务）")
    private Long source;

    /** 目标任务ID（后继任务） */
    @NotNull(message = "目标任务ID不能为空")
    @Schema(description = "目标任务ID（后继任务）")
    private Long target;

    /** 依赖类型：0-完成开始 1-开始开始 2-完成完成 3-开始完成（dhtmlx原生格式） */
    @NotBlank(message = "依赖类型不能为空")
    @Schema(description = "依赖类型：0-完成开始(e2s) / 1-开始开始(s2s) / 2-完成完成(e2e) / 3-开始完成(s2e)")
    private String type;
}