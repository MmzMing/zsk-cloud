package com.zsk.document.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 笔记详情数据传输对象
 * <p>
 * 用于接收前端传入的笔记内容数据，包含参数校验注解。
 * DTO 与 DO 分离，避免将前端参数直接映射到数据库实体，增强安全性。
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-25
 */
@Data
@Schema(description = "笔记详情DTO")
public class DocNoteDtlDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 关联笔记ID
     * <p>
     * 必填字段，用于关联 document_note 表。
     * 新增时必须传入，修改时可通过路径参数传入。
     * </p>
     */
    @NotNull(message = "笔记ID不能为空")
    @Schema(description = "关联笔记ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long noteId;

    /**
     * 笔记内容（Markdown 格式）
     * <p>
     * 必填字段，存储用户编辑的 Markdown 文本。
     * 前端需保证传入有效的 Markdown 格式内容。
     * </p>
     */
    @NotBlank(message = "笔记内容不能为空")
    @Schema(description = "笔记内容（Markdown格式）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;
}
