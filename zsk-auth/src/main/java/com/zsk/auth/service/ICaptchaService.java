package com.zsk.auth.service;

import com.zsk.auth.domain.CaptchaResponse;

/**
 * 验证码服务 接口
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2024-01-15
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
     */
    void validateCaptcha(String uuid, String code);
}
