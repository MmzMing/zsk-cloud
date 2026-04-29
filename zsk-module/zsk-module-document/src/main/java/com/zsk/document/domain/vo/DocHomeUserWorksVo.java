package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "前台用户作品项")
public class DocHomeUserWorksVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "作品ID")
    private Long id;

    @Schema(description = "作品标题")
    private String title;

    @Schema(description = "作品类型（note-笔记 video-视频）")
    private String type;

    @Schema(description = "作品描述")
    private String description;

    @Schema(description = "封面图地址")
    private String coverUrl;

    @Schema(description = "大类分类")
    private String category;

    @Schema(description = "标签")
    private String tags;

    @Schema(description = "浏览量")
    private Long viewCount;

    @Schema(description = "点赞数")
    private Long likeCount;

    @Schema(description = "收藏数")
    private Long favoriteCount;

    @Schema(description = "创建时间")
    private String createTime;
}
