package com.zsk.auth.service;

import com.zsk.auth.domain.LoginRequest;
import com.zsk.auth.domain.LoginResponse;
import com.zsk.auth.domain.RegisterBody;

/**
 * 认证服务 接口
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-14
 */
public interface IAuthService {
    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录结果
     */
    LoginResponse login(LoginRequest request);

    /**
     * 用户注册
     *
     * @param registerBody 注册信息
     */
    void register(RegisterBody registerBody);

    /**
     * 刷新令牌
     *
     * @param refreshToken 刷新令牌
     * @return 登录结果
     */
    void refreshTokenTime(String refreshToken);

    /**
     * 退出登录
     *
     * @param token 访问令牌
     */
    void logout(String token);

    /**
     * 发送密码重置验证码
     *
     * @param email 邮箱地址
     */
    void sendPasswordResetCode(String email);

    /**
     * 验证重置验证码
     *
     * @param email 邮箱地址
     * @param code 验证码
     * @return 验证令牌（用于后续重置密码）
     */
    String verifyResetCode(String email, String code);

    /**
     * 重置密码
     *
     * @param email 邮箱地址
     * @param verifyToken 验证令牌
     * @param newPassword 新密码（已加密）
     */
    void resetPassword(String email, String verifyToken, String newPassword);
}
