package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 审核提交请求
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@Schema(description = "审核提交请求")
public class AuditSubmitRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 视频ID */
    @NotNull(message = "视频ID不能为空")
    @Schema(description = "视频ID")
    private Long videoId;

    /** 审核状态（1-审核通过 2-审核驳回） */
    @NotNull(message = "审核状态不能为空")
    @Schema(description = "审核状态（1-审核通过 2-审核驳回）")
    private Integer auditStatus;

    /** 审核意见 */
    @Schema(description = "审核意见")
    private String auditMind;

    /** 违规项ID列表 */
    @Schema(description = "违规项ID列表")
    private List<String> violationIds;
}
