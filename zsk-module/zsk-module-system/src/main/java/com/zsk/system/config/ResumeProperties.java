package com.zsk.system.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 简历配置属性
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "resume")
public class ResumeProperties {

    /**
     * 简历模块列表
     */
    private List<ResumeModule> modules = new ArrayList<>();

    /**
     * 简历模块
     */
    @Data
    public static class ResumeModule {
        /** 模块ID */
        private String id;
        /** 模块类型（basic/content） */
        private String type;
        /** 模块标题 */
        private String title;
        /** 图标 */
        private String icon;
        /** 是否可删除 */
        private Boolean isDeletable;
        /** 是否可见 */
        private Boolean isVisible;
        /** 基础信息数据 */
        private BasicInfo data;
        /** 富文本内容 */
        private String content;
    }

    /**
     * 基础信息
     */
    @Data
    public static class BasicInfo {
        /** 姓名 */
        private String name;
        /** 求职意向 */
        private String jobIntention;
        /** 年龄 */
        private String age;
        /** 性别 */
        private String gender;
        /** 城市 */
        private String city;
        /** 电话 */
        private String phone;
        /** 邮箱 */
        private String email;
        /** GitHub */
        private String github;
        /** 个人简介 */
        private String summary;
        /** 头像 */
        private String avatar;
        /** 工作经验 */
        private String experience;
        /** 期望薪资 */
        private String salary;
        /** 政治面貌 */
        private String politics;
        /** 求职状态 */
        private String status;
    }
}
