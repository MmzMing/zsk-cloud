package com.zsk.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 公钥响应对象
 * 
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicKeyResponse {

    /** RSA公钥（Base64编码） */
    private String publicKey;

    /** 密钥有效期（秒），0表示永不过期 */
    private Long keyExpire;

    /** 密钥版本号（用于前端标识当前使用的密钥） */
    private String keyVersion;
}
