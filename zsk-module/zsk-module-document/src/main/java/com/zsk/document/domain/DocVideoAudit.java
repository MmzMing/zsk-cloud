package com.zsk.document.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zsk.common.datasource.domain.entity.TenantEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 视频审核详情表对象 document_video_audit
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("document_video_audit")
@Schema(description = "视频审核详情对象")
public class DocVideoAudit extends TenantEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 视频ID */
    @Schema(description = "视频ID")
    private Long videoId;

    /** 审核类型（ai-AI审核 manual-人工审核） */
    @Schema(description = "审核类型（ai-AI审核 manual-人工审核）")
    private String auditType;

    /** 审核状态（0-待审核 1-审核通过 2-审核驳回） */
    @Schema(description = "审核状态（0-待审核 1-审核通过 2-审核驳回）")
    private Integer auditStatus;

    /** 审核结果详情（JSON格式） */
    @Schema(description = "审核结果详情（JSON格式）")
    private String auditResult;

    /** 风险等级（low-低 medium-中 high-高） */
    @Schema(description = "风险等级（low-低 medium-中 high-高）")
    private String riskLevel;

    /** 审核意见 */
    @Schema(description = "审核意见")
    private String auditMind;

    /** 审核人ID */
    @Schema(description = "审核人ID")
    private Long auditorId;

    /** 审核人姓名 */
    @Schema(description = "审核人姓名")
    private String auditorName;

    /** 审核时间 */
    @Schema(description = "审核时间")
    private LocalDateTime auditTime;
}
