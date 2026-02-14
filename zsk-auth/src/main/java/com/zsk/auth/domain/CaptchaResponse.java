package com.zsk.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 验证码响应对象
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaptchaResponse {
    /**
     * 验证码唯一标识
     */
    private String uuid;

    /**
     * 背景图片（Base64）
     */
    private String bgUrl;

    /**
     * 拼图图片（Base64）
     */
    private String puzzleUrl;
}
