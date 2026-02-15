package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 视频标签VO
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@Schema(description = "视频标签VO")
public class VideoTagVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 标签ID */
    @Schema(description = "标签ID")
    private String id;

    /** 标签名称 */
    @Schema(description = "标签名称")
    private String name;
}
