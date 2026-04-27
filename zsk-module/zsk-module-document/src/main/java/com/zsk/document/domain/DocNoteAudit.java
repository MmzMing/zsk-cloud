package com.zsk.document.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zsk.common.datasource.domain.entity.TenantEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 文档审核详情表对象 document_note_audit
 *
 * <p>存储文档（笔记）的人工审核和AI审核记录，与 document_note 表是一对多关系。
 * 每次审核操作产生一条记录，便于追溯审核历史。</p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("document_note_audit")
@Schema(description = "文档审核详情对象")
public class DocNoteAudit extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文档ID（关联 document_note.id）
     */
    @Schema(description = "文档ID")
    private Long noteId;

    /**
     * 审核类型（ai-AI审核 manual-人工审核）
     */
    @Schema(description = "审核类型（ai-AI审核 manual-人工审核）")
    private String auditType;

    /**
     * 审核状态（0-待审核 1-审核通过 2-审核驳回）
     */
    @Schema(description = "审核状态（0-待审核 1-审核通过 2-审核驳回）")
    private Integer auditStatus;

    /**
     * 审核结果详情（JSON格式，如AI检测结果、违规项列表等）
     */
    @Schema(description = "审核结果详情（JSON格式）")
    private String auditResult;

    /**
     * 风险等级（low-低 medium-中 high-高）
     */
    @Schema(description = "风险等级（low-低 medium-中 high-高）")
    private String riskLevel;

    /**
     * 审核意见
     */
    @Schema(description = "审核意见")
    private String auditMind;

    /**
     * 审核人ID
     */
    @Schema(description = "审核人ID")
    private Long auditorId;

    /**
     * 审核人姓名
     */
    @Schema(description = "审核人姓名")
    private String auditorName;

    /**
     * 审核时间
     */
    @Schema(description = "审核时间")
    private LocalDateTime auditTime;
}
