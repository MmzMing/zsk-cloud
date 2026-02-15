package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 视频分类VO
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@Schema(description = "视频分类VO")
public class VideoCategoryVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 分类ID */
    @Schema(description = "分类ID")
    private String id;

    /** 分类名称 */
    @Schema(description = "分类名称")
    private String name;

    /** 子分类列表 */
    @Schema(description = "子分类列表")
    private List<VideoCategoryVO> children;
}
