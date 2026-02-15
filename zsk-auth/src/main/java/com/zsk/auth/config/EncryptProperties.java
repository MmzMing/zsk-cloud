package com.zsk.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * 加密配置属性
 * 
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "security.encrypt")
public class EncryptProperties {

    /** RSA公钥（Base64编码，用于前端加密密码） */
    private String publicKey;

    /** RSA私钥（Base64编码，用于后端解密密码） */
    private String privateKey;

    /** 密钥有效期（秒），0表示永不过期 */
    private Long keyExpire = 0L;
}
