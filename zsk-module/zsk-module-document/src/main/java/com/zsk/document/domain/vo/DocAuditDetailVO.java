package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 统一审核详情VO
 *
 * <p>用于展示审核记录详情，统一支持文档、视频、评论等多种内容类型。</p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-30
 */
@Data
@Schema(description = "统一审核详情VO")
public class DocAuditDetailVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 审核记录ID
     */
    @Schema(description = "审核记录ID")
    private Long id;

    /**
     * 审核目标类型
     */
    @Schema(description = "审核目标类型")
    private Integer targetType;

    /**
     * 审核目标ID
     */
    @Schema(description = "审核目标ID")
    private Long targetId;

    /**
     * 内容标题
     */
    @Schema(description = "内容标题")
    private String targetTitle;

    /**
     * 审核类型（ai/manual）
     */
    @Schema(description = "审核类型")
    private String auditType;

    /**
     * 审核状态（0-待审核 1-审核通过 2-审核驳回 3-已撤回）
     */
    @Schema(description = "审核状态")
    private Integer auditStatus;

    /**
     * 审核结果详情（JSON格式）
     */
    @Schema(description = "审核结果详情")
    private String auditResult;

    /**
     * 风险等级
     */
    @Schema(description = "风险等级")
    private String riskLevel;

    /**
     * 审核意见
     */
    @Schema(description = "审核意见")
    private String auditMind;

    /**
     * 违规原因ID列表（逗号分隔）
     */
    @Schema(description = "违规原因ID列表")
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

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
