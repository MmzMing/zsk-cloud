package com.zsk.auth.service;

/**
 * 邮箱服务 接口
 * 
 * @author wuhuaming
 * @date 2024-01-15
 * @version 1.0
 */
public interface IEmailService {
    /**
     * 发送邮箱验证码
     * 
     * @param email 邮箱地址
     */
    void sendEmailCode(String email);

    /**
     * 校验邮箱验证码
     * 
     * @param email 邮箱地址
     * @param code 验证码内容
     */
    void validateEmailCode(String email, String code);
}
