package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 搜索结果项 视图对象
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@Schema(description = "搜索结果项")
public class SearchResultVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 资源ID */
    @Schema(description = "资源ID")
    private String id;

    /** 排名 */
    @Schema(description = "排名")
    private Integer rank;

    /** 资源类型（video/document/tool/user） */
    @Schema(description = "资源类型（video/document/tool/user）")
    private String type;

    /** 标题 */
    @Schema(description = "标题")
    private String title;

    /** 作者ID */
    @Schema(description = "作者ID")
    private String authorId;

    /** 作者 */
    @Schema(description = "作者")
    private String author;

    /** 作者头像 */
    @Schema(description = "作者头像")
    private String authorAvatar;

    /** 描述 */
    @Schema(description = "描述")
    private String description;

    /** 分类 */
    @Schema(description = "分类")
    private String category;

    /** 标签列表 */
    @Schema(description = "标签列表")
    private List<String> tags;

    /** 统计信息文本 */
    @Schema(description = "统计信息文本")
    private String stats;

    /** 缩略图URL */
    @Schema(description = "缩略图URL")
    private String thumbnail;

    /** 头像URL（用户类型） */
    @Schema(description = "头像URL（用户类型）")
    private String avatar;

    /** 视频时长 */
    @Schema(description = "视频时长")
    private String duration;

    /** 播放量 */
    @Schema(description = "播放量")
    private Long playCount;

    /** 评论数 */
    @Schema(description = "评论数")
    private Long commentCount;

    /** 阅读量 */
    @Schema(description = "阅读量")
    private Long readCount;

    /** 收藏数 */
    @Schema(description = "收藏数")
    private Long favoriteCount;

    /** 使用次数 */
    @Schema(description = "使用次数")
    private Long usageCount;

    /** 粉丝数 */
    @Schema(description = "粉丝数")
    private Long followers;

    /** 作品数 */
    @Schema(description = "作品数")
    private Long works;
}
