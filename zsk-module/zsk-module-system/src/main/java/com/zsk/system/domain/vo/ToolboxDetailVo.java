package com.zsk.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 工具箱详情 视图对象
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@Schema(description = "工具箱详情")
public class ToolboxDetailVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 工具ID */
    @Schema(description = "工具ID")
    private String id;

    /** 标题 */
    @Schema(description = "标题")
    private String title;

    /** 描述 */
    @Schema(description = "描述")
    private String description;

    /** Logo图标URL */
    @Schema(description = "Logo图标URL")
    private String logo;

    /** 标签列表 */
    @Schema(description = "标签列表")
    private List<String> tags;

    /** 访问链接 */
    @Schema(description = "访问链接")
    private String url;

    /** 预览图列表 */
    @Schema(description = "预览图列表")
    private List<String> images;

    /** 特性功能点列表 */
    @Schema(description = "特性功能点列表")
    private List<String> features;

    /** 相关工具列表 */
    @Schema(description = "相关工具列表")
    private List<Object> relatedTools;

    /** 统计数据 */
    @Schema(description = "统计数据")
    private StatsInfoVo stats;

    /** 作者信息 */
    @Schema(description = "作者信息")
    private AuthorInfoVo author;

    /** 创建日期 */
    @Schema(description = "创建日期")
    private String createAt;

    /**
     * 统计数据
     */
    @Data
    @Schema(description = "统计数据")
    public static class StatsInfoVo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /** 浏览量 */
        @Schema(description = "浏览量")
        private Long views;

        /** 点赞数 */
        @Schema(description = "点赞数")
        private Long likes;

        /** 使用量 */
        @Schema(description = "使用量")
        private Long usage;
    }

    /**
     * 作者信息
     */
    @Data
    @Schema(description = "作者信息")
    public static class AuthorInfoVo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /** 作者姓名 */
        @Schema(description = "作者姓名")
        private String name;

        /** 头像URL */
        @Schema(description = "头像URL")
        private String avatar;
    }
}
