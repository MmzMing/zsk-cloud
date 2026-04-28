package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 前台搜索结果视图对象
 * <p>
 * 用于首页/前台搜索功能的结果展示，仅包含前端展示所需的公开字段，
 * 不包含敏感信息（如审核状态、审核意见、内部状态等）。
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-28
 */
@Data
@Schema(description = "前台搜索结果项")
public class DocHomeSearchResultVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 资源ID
     */
    @Schema(description = "资源ID")
    private String id;

    /**
     * 资源类型（video/document）
     */
    @Schema(description = "资源类型（video/document）")
    private String type;

    /**
     * 标题
     */
    @Schema(description = "标题")
    private String title;

    /**
     * 作者ID
     */
    @Schema(description = "作者ID")
    private String authorId;

    /**
     * 作者名称
     */
    @Schema(description = "作者名称")
    private String author;

    /**
     * 描述/简介
     */
    @Schema(description = "描述/简介")
    private String description;

    /**
     * 分类编码
     */
    @Schema(description = "分类编码")
    private String category;

    /**
     * 标签列表
     */
    @Schema(description = "标签列表")
    private List<String> tags;

    /**
     * 点赞数
     */
    @Schema(description = "点赞数")
    private Long likeCount;

    /**
     * 缩略图URL
     */
    @Schema(description = "缩略图URL")
    private String thumbnail;

    /**
     * 视频时长（仅视频类型有值）
     */
    @Schema(description = "视频时长（仅视频类型有值）")
    private String duration;

    /**
     * 播放量（仅视频类型有值）
     */
    @Schema(description = "播放量（仅视频类型有值）")
    private Long playCount;

    /**
     * 阅读量（仅笔记类型有值）
     */
    @Schema(description = "阅读量（仅笔记类型有值）")
    private Long readCount;

}
