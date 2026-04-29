package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "前台用户作品统计")
public class DocHomeUserStatsVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "总获赞数（笔记+视频）")
    private Long totalLikeCount;

    @Schema(description = "总浏览数（笔记+视频）")
    private Long totalViewCount;

    @Schema(description = "总收藏数（笔记+视频）")
    private Long totalFavoriteCount;

    @Schema(description = "笔记数量")
    private Long noteCount;

    @Schema(description = "视频数量")
    private Long videoCount;
}
