package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一审核队列VO
 *
 * <p>用于展示审核队列中的待审核项，统一支持文档、视频、评论等多种内容类型。</p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-30
 */
@Data
@Schema(description = "统一审核队列VO")
public class DocAuditQueueVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 审核记录ID
     */
    @Schema(description = "审核记录ID")
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
    private String title;

    /**
     * 分类编码
     */
    @Schema(description = "分类编码")
    private String broadCode;

    /**
     * 上传者ID
     */
    @Schema(description = "上传者ID")
    private Long uploaderId;

    /**
     * 上传者名称
     */
    @Schema(description = "上传者名称")
    private String uploaderName;

    /**
     * 审核状态（pending-待审核 approved-审核通过 rejected-审核驳回 withdrawn-已撤回）
     */
    @Schema(description = "审核状态")
    private String status;

    /**
     * 风险等级（low-低 medium-中 high-高）
     */
    @Schema(description = "风险等级")
    private String riskLevel;

    /**
     * 是否已AI审核
     */
    @Schema(description = "是否已AI审核")
    private Boolean isAiChecked;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private String createTime;
}
