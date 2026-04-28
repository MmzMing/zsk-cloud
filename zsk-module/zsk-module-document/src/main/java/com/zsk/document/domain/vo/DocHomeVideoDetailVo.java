package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 前台首页视频详情视图对象
 * <p>
 * 用于前台首页视频详情页展示视频元信息和内容。
 * 仅包含公开展示的字段，不包含审核状态、版本号等后台管理字段。
 * 获取元信息时优先增加浏览量（Redis）。
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-29
 */
@Data
@Schema(description = "前台首页视频详情")
public class DocHomeVideoDetailVo implements Serializable {

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
     * 视频描述
     */
    @Schema(description = "视频描述")
    private String description;

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
     * 大类分类
     */
    @Schema(description = "大类分类")
    private String category;

    /**
     * 标签列表
     */
    @Schema(description = "标签列表")
    private List<String> tags;
}
