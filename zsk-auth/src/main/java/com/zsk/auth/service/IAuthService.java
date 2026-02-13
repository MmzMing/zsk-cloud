package com.zsk.auth.service;

import com.zsk.auth.domain.LoginRequest;
import com.zsk.auth.domain.LoginResponse;
import com.zsk.auth.domain.RegisterBody;

/**
 * 认证服务 接口
 * 
 * @author wuhuaming
 * @date 2024-01-15
 * @version 1.0
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
}
