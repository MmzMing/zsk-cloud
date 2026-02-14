package com.zsk.auth.service;

import com.zsk.system.api.domain.SysUserApi;

/**
 * 第三方认证服务 接口
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-14
 */
public interface IThirdPartyAuthService {
    /**
     * 根据授权码获取用户信息
     *
     * @param loginType 登录类型
     * @param authCode  授权码
     * @param state     状态码
     * @return 用户信息
     */
    SysUserApi getUserByAuthCode(String loginType, String authCode, String state);

    /**
     * 获取第三方登录授权URL
     *
     * @param loginType 登录类型
     * @return 授权URL
     */
    String getAuthUrl(String loginType);
}
