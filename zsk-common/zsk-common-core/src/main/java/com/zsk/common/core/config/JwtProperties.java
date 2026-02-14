package com.zsk.common.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 配置属性
 *
 * @author wuhuaming
 * @date 2026-02-14
 */
@Data
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {
    /**
     * 令牌密钥（对称加密使用）
     */
    private String secret;

    /**
     * 公钥（非对称加密使用）
     */
    private String publicKey;

    /**
     * 私钥（非对称加密使用）
     */
    private String privateKey;
}
