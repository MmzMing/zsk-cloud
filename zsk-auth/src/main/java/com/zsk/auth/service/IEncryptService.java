package com.zsk.auth.service;

import com.zsk.auth.domain.PublicKeyResponse;

/**
 * 加密服务接口
 * 
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
public interface IEncryptService {

    /**
     * 获取公钥信息
     *
     * @return 公钥响应对象（包含公钥、有效期、版本号）
     */
    PublicKeyResponse getPublicKey();

    /**
     * 使用私钥解密数据
     *
     * @param encryptedData 加密后的数据（Base64编码）
     * @return 解密后的原始数据
     */
    String decrypt(String encryptedData);
}
