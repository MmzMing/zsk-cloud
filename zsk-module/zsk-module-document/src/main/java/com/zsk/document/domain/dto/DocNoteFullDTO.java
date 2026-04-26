package com.zsk.document.domain.dto;

import com.zsk.document.domain.DocNote;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 笔记全量数据传输对象
 * <p>
 * 组合完整的 {@link DocNote} 元信息对象与 Markdown 正文内容，
 * 用于创建和更新笔记时的一次性提交，由聚合 Service 在事务中同时写入两张表。
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-26
 */
@Data
@Schema(description = "笔记全量DTO（元信息 + 正文）")
public class DocNoteFullDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 笔记元信息对象
     * <p>
     * 完整的笔记基础信息，包含标题、分类、标签、封面、状态等字段。
     * 创建时 docNote.id 为空（由雪花算法自动生成），更新时必须传入 id。
     * </p>
     */
    @NotNull(message = "笔记信息不能为空")
    @Valid
    @Schema(description = "笔记元信息", requiredMode = Schema.RequiredMode.REQUIRED)
    private DocNote docNote;

    /**
     * 笔记正文内容（Markdown 格式）
     * <p>
     * 创建时必填，更新时可选（仅更新元信息时可不传）。
     * </p>
     */
    @NotBlank(message = "笔记内容不能为空")
    @Schema(description = "笔记正文（Markdown格式）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;
}
