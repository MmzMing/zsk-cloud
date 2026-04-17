package com.zsk.auth.domain;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 魔法链接请求对象
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-17
 */
@Data
public class MagicLinkRequest {

    /**
     * 邮箱地址
     */
    @NotBlank(message = "邮箱地址不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * Cloudflare Turnstile人机校验Token（前端从Turnstile组件获取）
     */
    @NotBlank(message = "校验凭证不能为空")
    private String turnstileToken;
}
