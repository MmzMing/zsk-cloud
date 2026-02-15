package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 违规原因VO
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@Schema(description = "违规原因VO")
public class ViolationReasonVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 违规项ID */
    @Schema(description = "违规项ID")
    private String id;

    /** 违规原因标签 */
    @Schema(description = "违规原因标签")
    private String label;
}
