package com.zsk.system.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 工具箱配置属性
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "toolbox")
public class ToolboxProperties {

    /**
     * 工具列表
     */
    private List<ToolItem> tools = new ArrayList<>();

    /**
     * 工具项
     */
    @Data
    public static class ToolItem {
        /** 工具ID */
        private String id;
        /** 标题 */
        private String title;
        /** 描述 */
        private String description;
        /** Logo图标URL */
        private String logo;
        /** 标签列表 */
        private List<String> tags;
        /** 访问链接 */
        private String url;
        /** 预览图列表 */
        private List<String> images;
        /** 特性功能点列表 */
        private List<String> features;
        /** 统计数据 */
        private StatsInfo stats;
        /** 作者信息 */
        private AuthorInfo author;
        /** 创建日期 */
        private String createAt;
    }

    /**
     * 统计数据
     */
    @Data
    public static class StatsInfo {
        /** 浏览量 */
        private Long views;
        /** 点赞数 */
        private Long likes;
        /** 使用量 */
        private Long usage;
    }

    /**
     * 作者信息
     */
    @Data
    public static class AuthorInfo {
        /** 作者姓名 */
        private String name;
        /** 头像URL */
        private String avatar;
    }
}
