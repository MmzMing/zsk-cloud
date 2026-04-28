package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 前台首页视频合集项视图对象
 * <p>
 * 用于前台首页视频合集区域展示合集内的视频条目。
 * 仅包含公开展示的字段，不包含审核状态等后台管理字段。
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-29
 */
@Data
@Schema(description = "前台首页视频合集项")
public class DocHomeVideoCollectionItemVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 视频ID
     */
    @Schema(description = "视频ID")
    private Long id;

    /**
     * 视频标题
     */
    @Schema(description = "视频标题")
    private String title;

    /**
     * 视频播放地址
     */
    @Schema(description = "视频播放地址")
    private String videoUrl;

    /**
     * 封面图URL
     */
    @Schema(description = "封面图URL")
    private String coverUrl;

    /**
     * 视频描述
     */
    @Schema(description = "视频描述")
    private String description;

    /**
     * 浏览量（从Redis缓存获取）
     */
    @Schema(description = "浏览量")
    private Long viewCount;
}
