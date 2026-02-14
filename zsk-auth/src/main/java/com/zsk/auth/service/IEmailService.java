package com.zsk.auth.service;

/**
 * 邮箱服务 接口
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-14
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
     * @param code  验证码内容
     */
    void validateEmailCode(String email, String code);
}
