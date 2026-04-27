package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 文档审核队列VO
 *
 * <p>用于展示文档审核队列中的待审核项，包含文档基本信息和审核状态。</p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Data
@Schema(description = "文档审核队列VO")
public class NoteAuditQueueVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文档ID
     */
    @Schema(description = "文档ID")
    private Long id;

    /**
     * 文档标题
     */
    @Schema(description = "文档标题")
    private String noteName;

    /**
     * 上传者
     */
    @Schema(description = "上传者")
    private String uploader;

    /**
     * 分类
     */
    @Schema(description = "分类")
    private String broadCode;

    /**
     * 审核状态（pending-待审核 approved-审核通过 rejected-审核驳回）
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
