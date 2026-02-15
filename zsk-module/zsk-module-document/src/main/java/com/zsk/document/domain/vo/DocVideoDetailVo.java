package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 视频详情 视图对象
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@Schema(description = "视频详情")
public class DocVideoDetailVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 视频ID */
    @Schema(description = "视频ID")
    private String id;

    /** 视频标题 */
    @Schema(description = "视频标题")
    private String title;

    /** 视频描述 */
    @Schema(description = "视频描述")
    private String description;

    /** 视频播放地址 */
    @Schema(description = "视频播放地址")
    private String videoUrl;

    /** 封面图URL */
    @Schema(description = "封面图URL")
    private String coverUrl;

    /** 作者信息 */
    @Schema(description = "作者信息")
    private AuthorInfo author;

    /** 统计数据 */
    @Schema(description = "统计数据")
    private StatsInfo stats;

    /** 标签列表 */
    @Schema(description = "标签列表")
    private List<String> tags;

    /** 推荐视频列表 */
    @Schema(description = "推荐视频列表")
    private List<RecommendVideo> recommendations;

    /** 分集信息 */
    @Schema(description = "分集信息")
    private List<EpisodeInfo> episodes;

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

        /** 作者头像URL */
        @Schema(description = "作者头像URL")
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

        /** 播放量 */
        @Schema(description = "播放量")
        private String views;

        /** 点赞数 */
        @Schema(description = "点赞数")
        private Integer likes;

        /** 收藏数 */
        @Schema(description = "收藏数")
        private Integer favorites;

        /** 发布日期 */
        @Schema(description = "发布日期")
        private String date;

        /** 是否已点赞 */
        @Schema(description = "是否已点赞")
        private Boolean isLiked;

        /** 是否已收藏 */
        @Schema(description = "是否已收藏")
        private Boolean isFavorited;
    }

    /**
     * 推荐视频
     */
    @Data
    @Schema(description = "推荐视频")
    public static class RecommendVideo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /** 视频ID */
        @Schema(description = "视频ID")
        private String id;

        /** 视频标题 */
        @Schema(description = "视频标题")
        private String title;

        /** 封面图URL */
        @Schema(description = "封面图URL")
        private String coverUrl;

        /** 视频时长 */
        @Schema(description = "视频时长")
        private String duration;

        /** 播放量 */
        @Schema(description = "播放量")
        private String views;

        /** 描述 */
        @Schema(description = "描述")
        private String description;

        /** 作者名称 */
        @Schema(description = "作者名称")
        private String authorName;

        /** 发布日期 */
        @Schema(description = "发布日期")
        private String date;
    }

    /**
     * 分集信息
     */
    @Data
    @Schema(description = "分集信息")
    public static class EpisodeInfo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /** 分集ID */
        @Schema(description = "分集ID")
        private String id;

        /** 分集标题 */
        @Schema(description = "分集标题")
        private String title;

        /** 分集视频地址 */
        @Schema(description = "分集视频地址")
        private String videoUrl;

        /** 分集时长 */
        @Schema(description = "分集时长")
        private String duration;
    }
}
