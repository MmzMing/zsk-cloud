package com.zsk.system.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * About页面配置属性
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "about")
public class AboutProperties {

    /**
     * 技术栈列表
     */
    private List<TechStackItem> techStack = new ArrayList<>();

    /**
     * FAQ分类列表
     */
    private List<FaqCategory> faq = new ArrayList<>();

    /**
     * 技术栈项
     */
    @Data
    public static class TechStackItem {
        /** 唯一标识 */
        private String id;
        /** 名称 */
        private String name;
        /** 描述 */
        private String description;
    }

    /**
     * FAQ分类
     */
    @Data
    public static class FaqCategory {
        /** 分类名称 */
        private String title;
        /** 该分类下的FAQ列表 */
        private List<FaqItem> items = new ArrayList<>();
    }

    /**
     * FAQ项
     */
    @Data
    public static class FaqItem {
        /** 唯一标识 */
        private String id;
        /** 问题 */
        private String question;
        /** 回答 */
        private String answer;
    }
}
