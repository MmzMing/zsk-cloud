package com.zsk.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Cloudflare Turnstile 配置属性类
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-17
 */
@Data
@Component
@ConfigurationProperties(prefix = "turnstile")
public class TurnstileProperties {

    /**
     * Cloudflare Turnstile 密钥
     */
    private String secretKey;

    /**
     * Cloudflare Turnstile 站点密钥
     */
    private String siteKey;

    /**
     * Turnstile验证API地址
     */
    private String verifyUrl = "https://challenges.cloudflare.com/turnstile/v0/siteverify";
}
