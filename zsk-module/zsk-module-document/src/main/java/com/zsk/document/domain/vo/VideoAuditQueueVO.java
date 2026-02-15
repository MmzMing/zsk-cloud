package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 视频审核队列VO
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@Schema(description = "视频审核队列VO")
public class VideoAuditQueueVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 视频ID */
    @Schema(description = "视频ID")
    private Long id;

    /** 视频标题 */
    @Schema(description = "视频标题")
    private String videoTitle;

    /** 上传者 */
    @Schema(description = "上传者")
    private String uploader;

    /** 分类 */
    @Schema(description = "分类")
    private String broadCode;

    /** 审核状态（pending-待审核 approved-审核通过 rejected-审核驳回） */
    @Schema(description = "审核状态")
    private String status;

    /** 风险等级（low-低 medium-中 high-高） */
    @Schema(description = "风险等级")
    private String riskLevel;

    /** 是否已AI审核 */
    @Schema(description = "是否已AI审核")
    private Boolean isAiChecked;

    /** 创建时间 */
    @Schema(description = "创建时间")
    private String createTime;
}
