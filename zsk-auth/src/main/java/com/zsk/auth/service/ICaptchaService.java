package com.zsk.auth.service;

import com.zsk.auth.domain.CaptchaResponse;

/**
 * 验证码服务 接口
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-14
 */
public interface ICaptchaService {
    /**
     * 生成滑块验证码
     *
     * @return 验证码响应对象
     */
    CaptchaResponse generateSlideCaptcha();

    /**
     * 校验滑块验证码
     *
     * @param uuid 验证码标识
     * @param code 验证码内容（X坐标移动距离）
     * @return 验证通过的凭证Token
     */
    String validateCaptcha(String uuid, String code);

    /**
     * 校验验证码凭证
     *
     * @param token 验证凭证
     */
    void verifyCaptchaToken(String token);
}
