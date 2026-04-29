package com.zsk.document.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zsk.common.datasource.domain.entity.TenantEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 统一审核记录表对象 document_audit
 *
 * <p>统一存储文档、视频、评论等多种内容类型的审核记录，
 * 通过 target_type + target_id 多态关联具体内容。</p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("document_audit")
@Schema(description = "统一审核记录对象")
public class DocAudit extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 审核目标类型（1-文档 2-视频 3-文档评论 4-视频评论）
     */
    @Schema(description = "审核目标类型（1-文档 2-视频 3-文档评论 4-视频评论）")
    private Integer targetType;

    /**
     * 审核目标ID
     */
    @Schema(description = "审核目标ID")
    private Long targetId;

    /**
     * 审核类型（ai-AI审核 manual-人工审核）
     */
    @Schema(description = "审核类型（ai-AI审核 manual-人工审核）")
    private String auditType;

    /**
     * 审核状态（0-待审核 1-审核通过 2-审核驳回 3-已撤回）
     */
    @Schema(description = "审核状态（0-待审核 1-审核通过 2-审核驳回 3-已撤回）")
    private Integer auditStatus;

    /**
     * 审核结果详情（JSON格式）
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
     * 违规原因ID列表（逗号分隔）
     */
    @Schema(description = "违规原因ID列表（逗号分隔）")
    private String violationIds;

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
