package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 文档审核日志VO
 *
 * <p>用于展示文档审核操作日志，包含审核人、审核时间、审核结果等信息。</p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Data
@Schema(description = "文档审核日志VO")
public class NoteAuditLogVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    @Schema(description = "日志ID")
    private Long id;

    /**
     * 文档ID
     */
    @Schema(description = "文档ID")
    private Long noteId;

    /**
     * 文档标题
     */
    @Schema(description = "文档标题")
    private String noteName;

    /**
     * 审核人
     */
    @Schema(description = "审核人")
    private String auditorName;

    /**
     * 审核时间
     */
    @Schema(description = "审核时间")
    private String auditTime;

    /**
     * 审核结果（approved-通过 rejected-驳回）
     */
    @Schema(description = "审核结果")
    private String result;

    /**
     * 审核意见
     */
    @Schema(description = "审核意见")
    private String auditMind;
}
