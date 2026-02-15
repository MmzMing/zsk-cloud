package com.zsk.auth.service.impl;

import com.zsk.auth.config.EncryptProperties;
import com.zsk.auth.domain.PublicKeyResponse;
import com.zsk.auth.service.IEncryptService;
import com.zsk.common.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * 加密服务实现
 * 
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EncryptServiceImpl implements IEncryptService {

    /** 加密配置属性 */
    private final EncryptProperties encryptProperties;

    /** RSA算法名称 */
    private static final String RSA_ALGORITHM = "RSA";

    /** 密钥版本前缀 */
    private static final String KEY_VERSION_PREFIX = "v1";

    /**
     * 获取公钥信息
     *
     * @return 公钥响应对象（包含公钥、有效期、版本号）
     */
    @Override
    public PublicKeyResponse getPublicKey() {
        return PublicKeyResponse.builder()
                .publicKey(encryptProperties.getPublicKey())
                .keyExpire(encryptProperties.getKeyExpire())
                .keyVersion(generateKeyVersion())
                .build();
    }

    /**
     * 使用私钥解密数据
     *
     * @param encryptedData 加密后的数据（Base64编码）
     * @return 解密后的原始数据
     * @throws BusinessException 解密失败时抛出
     */
    @Override
    public String decrypt(String encryptedData) {
        try {
            PrivateKey privateKey = getPrivateKey();
            // 1. 指定 RSA-OAEP 算法，匹配前端的 SHA-256 哈希
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");

            // 2. 配置 OAEP 参数（和前端 SHA-256 对应）
            OAEPParameterSpec oaepParams = new OAEPParameterSpec(
                    "SHA-256",  // 哈希算法，匹配前端的 hash: "SHA-256"
                    "MGF1",     // 掩码生成函数
                    MGF1ParameterSpec.SHA256,  // MGF1 的哈希算法
                    PSource.PSpecified.DEFAULT // 明文参数
            );

            // 3. 初始化解密模式，传入 OAEP 参数
            cipher.init(Cipher.DECRYPT_MODE, privateKey, oaepParams);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decryptedBytes);
        } catch (Exception e) {
            log.error("RSA解密失败: {}", e.getMessage());
            throw new BusinessException("数据解密失败");
        }
    }

    /**
     * 获取RSA私钥对象
     *
     * @return RSA私钥
     * @throws Exception 密钥解析异常
     */
    private PrivateKey getPrivateKey() throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(encryptProperties.getPrivateKey());
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * 生成密钥版本号
     *
     * @return 密钥版本号（格式：v1_yyyyMMdd）
     */
    private String generateKeyVersion() {
        return KEY_VERSION_PREFIX + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }
}
