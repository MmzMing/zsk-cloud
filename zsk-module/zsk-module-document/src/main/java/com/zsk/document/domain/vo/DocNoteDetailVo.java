package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文档详情 视图对象
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@Schema(description = "文档详情")
public class DocNoteDetailVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 文档ID */
    @Schema(description = "文档ID")
    private String id;

    /** 标题 */
    @Schema(description = "标题")
    private String title;

    /** 内容 */
    @Schema(description = "内容")
    private String content;

    /** 分类 */
    @Schema(description = "分类")
    private String category;

    /** 日期 */
    @Schema(description = "日期")
    private String date;

    /** 封面图地址 */
    @Schema(description = "封面图地址")
    private String coverUrl;

    /** 作者信息 */
    @Schema(description = "作者信息")
    private AuthorInfo author;

    /** 统计数据 */
    @Schema(description = "统计数据")
    private StatsInfo stats;

    /** 推荐文档列表 */
    @Schema(description = "推荐文档列表")
    private java.util.List<RecommendDoc> recommendations;

    /**
     * 作者信息
     */
    @Data
    @Schema(description = "作者信息")
    public static class AuthorInfo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /** 作者ID */
        @Schema(description = "作者ID")
        private String id;

        /** 作者名称 */
        @Schema(description = "作者名称")
        private String name;

        /** 头像 */
        @Schema(description = "头像")
        private String avatar;

        /** 粉丝数 */
        @Schema(description = "粉丝数")
        private String fans;

        /** 是否已关注 */
        @Schema(description = "是否已关注")
        private Boolean isFollowing;
    }

    /**
     * 统计数据
     */
    @Data
    @Schema(description = "统计数据")
    public static class StatsInfo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /** 阅读数 */
        @Schema(description = "阅读数")
        private String views;

        /** 点赞数 */
        @Schema(description = "点赞数")
        private Integer likes;

        /** 收藏数 */
        @Schema(description = "收藏数")
        private Integer favorites;

        /** 是否已点赞 */
        @Schema(description = "是否已点赞")
        private Boolean isLiked;

        /** 是否已收藏 */
        @Schema(description = "是否已收藏")
        private Boolean isFavorited;
    }

    /**
     * 推荐文档
     */
    @Data
    @Schema(description = "推荐文档")
    public static class RecommendDoc implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /** 文档ID */
        @Schema(description = "文档ID")
        private String id;

        /** 标题 */
        @Schema(description = "标题")
        private String title;

        /** 阅读数 */
        @Schema(description = "阅读数")
        private String views;
    }
}
