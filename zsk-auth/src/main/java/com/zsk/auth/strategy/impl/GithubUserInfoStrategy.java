package com.zsk.auth.strategy.impl;

import com.zsk.auth.strategy.OAuth2UserInfoStrategy;
import com.zsk.system.api.domain.SysUserApi;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * GitHub 用户信息策略实现
 * <p>
 * 实现了基于 GitHub OAuth2 协议的用户认证流程。
 * 使用 RestClient 驱动的令牌响应客户端以符合 Spring Security 最新推荐实践。
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Component
public class GithubUserInfoStrategy implements OAuth2UserInfoStrategy {

    /**
     * 默认OAuth2用户服务，用于加载并解析 GitHub 返回的用户原始属性
     */
    private final DefaultOAuth2UserService userService = new DefaultOAuth2UserService();

    /**
     * 基于 RestClient 的授权码令牌响应客户端
     * 用于通过 Authorization Code 换取 GitHub 的 Access Token
     */
    private final RestClientAuthorizationCodeTokenResponseClient tokenResponseClient = new RestClientAuthorizationCodeTokenResponseClient();

    /**
     * 获取 GitHub 注册 ID
     *
     * @return 始终返回 "github"
     */
    @Override
    public String getRegistrationId() {
        return "github";
    }

    /**
     * 向 GitHub 换取 Access Token
     *
     * @param grantRequest 包含授权码和客户端信息的请求
     * @return GitHub 返回的令牌响应
     */
    @Override
    public OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest grantRequest) {
        return tokenResponseClient.getTokenResponse(grantRequest);
    }

    /**
     * 加载 GitHub 用户信息并映射为系统用户
     * <p>
     * 映射规则：
     * <ul>
     *   <li>用户名：github_{id}</li>
     *   <li>昵称：取 GitHub 的 login 字段</li>
     *   <li>头像：取 GitHub 的 avatar_url 字段</li>
     * </ul>
     * </p>
     *
     * @param userRequest 包含 Access Token 的用户请求
     * @return 系统用户信息对象 SysUserApi
     */
    @Override
    public SysUserApi getUserInfo(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = userService.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        SysUserApi user = new SysUserApi();
        user.setUserName(getRegistrationId() + "_" + attributes.get("id"));
        user.setNickName((String) attributes.get("login"));
        user.setAvatar((String) attributes.get("avatar_url"));
        user.setStatus("0");
        return user;
    }
}
