package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 首页视频项 视图对象
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@Schema(description = "首页视频项")
public class HomeVideoVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 视频ID */
    @Schema(description = "视频ID")
    private String id;

    /** 视频分类 */
    @Schema(description = "视频分类")
    private String category;

    /** 视频时长 */
    @Schema(description = "视频时长")
    private String duration;

    /** 视频标题 */
    @Schema(description = "视频标题")
    private String title;

    /** 视频描述 */
    @Schema(description = "视频描述")
    private String description;

    /** 播放量 */
    @Schema(description = "播放量")
    private String views;

    /** 点赞数 */
    @Schema(description = "点赞数")
    private Integer likes;

    /** 评论数 */
    @Schema(description = "评论数")
    private Integer comments;

    /** 发布日期 */
    @Schema(description = "发布日期")
    private String date;

    /** 封面图URL */
    @Schema(description = "封面图URL")
    private String cover;

    /** 视频源地址 */
    @Schema(description = "视频源地址")
    private String sources;
}
