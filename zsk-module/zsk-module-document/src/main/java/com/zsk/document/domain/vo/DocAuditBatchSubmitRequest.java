package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 统一批量审核提交请求
 *
 * <p>支持对同一类型的多个内容批量提交审核结果。</p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-30
 */
@Data
@Schema(description = "统一批量审核提交请求")
public class DocAuditBatchSubmitRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 审核目标类型（1-文档 2-视频 3-文档评论 4-视频评论）
     */
    @NotNull(message = "目标类型不能为空")
    @Schema(description = "审核目标类型（1-文档 2-视频 3-文档评论 4-视频评论）")
    private Integer targetType;

    /**
     * 审核目标ID列表
     */
    @NotEmpty(message = "目标ID列表不能为空")
    @Schema(description = "审核目标ID列表")
    private List<Long> targetIds;

    /**
     * 审核状态（1-审核通过 2-审核驳回）
     */
    @NotNull(message = "审核状态不能为空")
    @Schema(description = "审核状态（1-审核通过 2-审核驳回）")
    private Integer auditStatus;

    /**
     * 审核意见
     */
    @Schema(description = "审核意见")
    private String auditMind;

    /**
     * 违规项ID列表
     */
    @Schema(description = "违规项ID列表")
    private List<String> violationIds;
}
