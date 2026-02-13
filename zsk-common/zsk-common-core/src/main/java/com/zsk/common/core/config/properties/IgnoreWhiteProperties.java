package com.zsk.common.core.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 安全过滤白名单配置
 * 
 * @author zsk
 * @date 2024-02-13
 * @version 1.0
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "security.ignore")
public class IgnoreWhiteProperties {
    /** 放行白名单配置 */
    private List<String> whites;
}
