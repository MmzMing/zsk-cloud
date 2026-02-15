package com.zsk.common.xxljob.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * XXL-Job配置属性
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Data
@ConfigurationProperties(prefix = "xxl.job")
public class XxlJobProperties {

    /**
     * 是否启用自动注册
     */
    private boolean enabled = true;

    /**
     * 执行器配置
     */
    private Executor executor = new Executor();

    /**
     * Admin配置
     */
    private Admin admin = new Admin();

    /**
     * 执行器配置
     */
    @Data
    public static class Executor {

        /**
         * 执行器AppName（唯一标识）
         */
        private String appname;

        /**
         * 执行器名称
         */
        private String title;

        /**
         * 执行器注册地址（为空则自动获取）
         */
        private String address;

        /**
         * 执行器IP（为空则自动获取）
         */
        private String ip;

        /**
         * 执行器端口号
         */
        private int port = 9999;

        /**
         * 执行器通讯TOKEN
         */
        private String accessToken;

        /**
         * 执行器运行日志文件存储路径
         */
        private String logPath = "logs/xxl-job/jobhandler";

        /**
         * 执行器日志文件保存天数
         */
        private int logRetentionDays = 30;
    }

    /**
     * Admin配置
     */
    @Data
    public static class Admin {

        /**
         * 调度中心部署地址（集群部署存在多个地址则用逗号分隔）
         */
        private String addresses;

        /**
         * 调度中心登录用户名
         */
        private String username = "admin";

        /**
         * 调度中心登录密码
         */
        private String password = "123456";
    }
}
