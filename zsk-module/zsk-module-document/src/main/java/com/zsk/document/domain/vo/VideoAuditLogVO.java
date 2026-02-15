package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 视频审核日志VO
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@Schema(description = "视频审核日志VO")
public class VideoAuditLogVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 日志ID */
    @Schema(description = "日志ID")
    private Long id;

    /** 视频ID */
    @Schema(description = "视频ID")
    private Long videoId;

    /** 视频标题 */
    @Schema(description = "视频标题")
    private String videoTitle;

    /** 审核人 */
    @Schema(description = "审核人")
    private String auditorName;

    /** 审核时间 */
    @Schema(description = "审核时间")
    private String auditTime;

    /** 审核结果（approved-通过 rejected-驳回） */
    @Schema(description = "审核结果")
    private String result;

    /** 审核意见 */
    @Schema(description = "审核意见")
    private String auditMind;
}
