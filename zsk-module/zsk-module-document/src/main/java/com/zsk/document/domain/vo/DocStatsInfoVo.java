package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "统计数据")
public class DocStatsInfoVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "阅读数")
    private Integer views;

    @Schema(description = "点赞数")
    private Integer likes;

    @Schema(description = "收藏数")
    private Integer favorites;

    @Schema(description = "是否已点赞")
    private Boolean isLiked;

    @Schema(description = "是否已收藏")
    private Boolean isFavorited;
}