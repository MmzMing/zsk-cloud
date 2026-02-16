package com.zsk.auth.domain;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户注册请求对象
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-14
 */
@Data
public class RegisterBody implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Length(min = 2, max = 20, message = "用户名长度必须在2到20个字符之间")
    private String username;

    /**
     * 邮箱
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 用户密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 确认密码
     */
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    /**
     * 验证码内容
     */
    @NotBlank(message = "验证码不能为空")
    private String code;

    /**
     * 验证码唯一标识
     */
    @NotBlank(message = "验证码标识不能为空")
    private String uuid;

    /**
     * 用户类型（00系统用户）
     */
    private String userType;
}
