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

    private Boolean enabled = true;

    private List<String> excludeUrls = new ArrayList<>();

    private Boolean bodyEnabled = true;

    private List<String> bodyContentTypes = new ArrayList<>() {{
        add("application/json");
        add("application/xml");
        add("text/xml");
    }};

    private Long bodyMaxBytes = 1024 * 1024L;
}
