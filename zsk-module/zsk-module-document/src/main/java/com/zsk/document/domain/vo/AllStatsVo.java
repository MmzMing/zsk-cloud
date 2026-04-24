package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 内容统计信息
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Data
@Builder
@Schema(description = "内容统计信息")
public class AllStatsVo {

    @Schema(description = "文档总数")
    private Long docCount;

    @Schema(description = "视频总数")
    private Long videoCount;

    @Schema(description = "评论总数")
    private Long commentCount;

    @Schema(description = "上周新增文档数")
    private Long lastWeekDocCount;

    @Schema(description = "上周新增视频数")
    private Long lastWeekVideoCount;

    @Schema(description = "上周新增评论数")
    private Long lastWeekCommentCount;
}
