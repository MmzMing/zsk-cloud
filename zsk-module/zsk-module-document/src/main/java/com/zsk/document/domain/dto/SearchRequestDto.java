package com.zsk.document.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 搜索请求参数
 *
 * @author wuhuaming
 */
@Data
@Schema(description = "搜索请求参数")
public class SearchRequestDto {

    @Schema(description = "关键字")
    private String keyword;

    @Schema(description = "类型（all/video/document/tool/user）")
    private String type = "all";

    @Schema(description = "排序（hot/latest/like/usage/relevance/fans/active）")
    private String sort;

    @Schema(description = "时长筛选")
    private String duration;

    @Schema(description = "时间范围")
    private String timeRange;

    @Schema(description = "分类筛选")
    private String category;
}
