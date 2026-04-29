package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 违规原因VO
 *
 * <p>用于展示违规原因字典数据。</p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-30
 */
@Data
@Schema(description = "违规原因VO")
public class DocViolationReasonVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 违规项ID
     */
    @Schema(description = "违规项ID")
    private String id;

    /**
     * 违规原因标签
     */
    @Schema(description = "违规原因标签")
    private String label;
}
