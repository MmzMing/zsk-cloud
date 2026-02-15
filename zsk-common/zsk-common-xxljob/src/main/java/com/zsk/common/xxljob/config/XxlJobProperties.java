package com.zsk.common.xxljob.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * XXL-Job配置属性
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    /**
     * 执行器配置
     */
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

        public String getAppname() {
            return appname;
        }

        public void setAppname(String appname) {
            this.appname = appname;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getLogPath() {
            return logPath;
        }

        public void setLogPath(String logPath) {
            this.logPath = logPath;
        }

        public int getLogRetentionDays() {
            return logRetentionDays;
        }

        public void setLogRetentionDays(int logRetentionDays) {
            this.logRetentionDays = logRetentionDays;
        }
    }

    /**
     * Admin配置
     */
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

        public String getAddresses() {
            return addresses;
        }

        public void setAddresses(String addresses) {
            this.addresses = addresses;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
