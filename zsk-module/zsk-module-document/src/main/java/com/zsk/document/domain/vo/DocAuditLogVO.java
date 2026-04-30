package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一审核日志VO
 *
 * <p>用于展示审核操作日志，统一支持文档、视频、评论等多种内容类型。</p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-30
 */
@Data
@Schema(description = "统一审核日志VO")
public class DocAuditLogVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    @Schema(description = "日志ID")
    private Long id;

    /**
     * 审核目标类型（1-文档 2-视频 3-文档评论 4-视频评论）
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
     * 审核结果（1-通过 2-驳回 3-已撤回）
     */
    @Schema(description = "审核结果（1-通过 2-驳回 3-已撤回）")
    private Integer result;

    /**
     * 审核轮次
     */
    @Schema(description = "审核轮次")
    private Integer auditRound;

    /**
     * 审核意见
     */
    @Schema(description = "审核意见")
    private String auditMind;

    /**
     * 风险等级（low-低 medium-中 high-高）
     */
    @Schema(description = "风险等级")
    private String riskLevel;
}
