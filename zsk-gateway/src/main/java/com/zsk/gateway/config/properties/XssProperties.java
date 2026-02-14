package com.zsk.gateway.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * XSS配置属性
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-14
 */
@Data
@Configuration
@RefreshScope
@ConfigurationProperties(prefix = "security.xss")
public class XssProperties {
    /**
     * XSS开关
     */
    private Boolean enabled = true;

    /**
     * 排除路径
     */
    private List<String> excludeUrls = new ArrayList<>();
}
