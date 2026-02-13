package com.zsk.gateway.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 安全过滤白名单配置
 *
 * @author zsk
 * @version 1.0
 * @date 2024-02-13
 */
@Data
@Component
@ConfigurationProperties(prefix = "security.ignore")
public class IgnoreWhiteProperties {
    /**
     * 放行白名单配置
     */
    private List<String> whites;
}
