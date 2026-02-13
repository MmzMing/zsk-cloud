package com.zsk.auth.domain;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求对象
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2024-01-15
 */
@Data
public class LoginRequest {
    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 图形验证码内容
     */
    private String code;

    /**
     * 图形验证码唯一标识
     */
    private String uuid;

    /**
     * 登录类型（password-密码登录，email-邮箱登录，qq-QQ登录，wechat-微信登录，github-GitHub登录）
     */
    @NotBlank(message = "登录类型不能为空")
    private String loginType;

    /**
     * 邮箱地址（邮箱登录时必填）
     */
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 邮箱验证码（邮箱登录时必填）
     */
    private String emailCode;

    /**
     * 第三方授权码（第三方登录时必填）
     */
    private String authCode;

    /**
     * 第三方状态值（第三方登录时必填）
     */
    private String state;
}
