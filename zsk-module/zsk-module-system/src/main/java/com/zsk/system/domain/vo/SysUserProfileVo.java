package com.zsk.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 用户资料 视图对象
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@Schema(description = "用户资料")
public class SysUserProfileVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    @Schema(description = "用户ID")
    private String id;

    /** 用户名 */
    @Schema(description = "用户名")
    private String username;

    /** 昵称 */
    @Schema(description = "昵称")
    private String name;

    /** 头像URL */
    @Schema(description = "头像URL")
    private String avatar;

    /** 背景图URL */
    @Schema(description = "背景图URL")
    private String banner;

    /** 等级 */
    @Schema(description = "等级")
    private Integer level;

    /** 标签 */
    @Schema(description = "标签")
    private List<String> tags;

    /** 个人简介 */
    @Schema(description = "个人简介")
    private String bio;

    /** 所在地 */
    @Schema(description = "所在地")
    private String location;

    /** 个人网站 */
    @Schema(description = "个人网站")
    private String website;

    /** 统计数据 */
    @Schema(description = "统计数据")
    private StatsInfo stats;

    /** 是否已关注 */
    @Schema(description = "是否已关注")
    private Boolean isFollowing;

    /**
     * 统计数据
     */
    @Data
    @Schema(description = "统计数据")
    public static class StatsInfo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /** 粉丝数 */
        @Schema(description = "粉丝数")
        private Integer followers;

        /** 关注数 */
        @Schema(description = "关注数")
        private Integer following;

        /** 作品数 */
        @Schema(description = "作品数")
        private Integer works;

        /** 点赞数 */
        @Schema(description = "点赞数")
        private Integer likes;
    }
}
