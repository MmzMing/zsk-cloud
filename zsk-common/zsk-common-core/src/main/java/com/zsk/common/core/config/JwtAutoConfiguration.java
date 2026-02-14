package com.zsk.common.core.config;

import com.zsk.common.core.utils.JwtUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * JWT 自动配置
 *
 * @author wuhuaming
 * @date 2026-02-14
 */
@AutoConfiguration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtAutoConfiguration {

    public JwtAutoConfiguration(JwtProperties jwtProperties) {
        JwtUtils.init(jwtProperties.getSecret(), jwtProperties.getPublicKey(), jwtProperties.getPrivateKey());
    }
}
