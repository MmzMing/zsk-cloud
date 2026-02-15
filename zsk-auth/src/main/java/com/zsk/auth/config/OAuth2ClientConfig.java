package com.zsk.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * OAuth2 客户端配置类
 * <p>
 * 该类负责初始化系统支持的所有第三方 OAuth2 客户端注册信息。
 * 包含 GitHub、QQ 和微信的认证端点、令牌端点及用户信息端点配置。
 * 配置信息主要从 application 配置文件中读取。
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Configuration
public class OAuth2ClientConfig {

    /**
     * QQ 应用标识 (App ID)
     * 从配置文件 auth.qq.app-id 获取
     */
    @Value("${auth.qq.app-id}")
    private String qqAppId;

    /**
     * QQ 应用密钥 (App Secret)
     * 从配置文件 auth.qq.app-secret 获取
     */
    @Value("${auth.qq.app-secret}")
    private String qqAppSecret;

    /**
     * QQ 认证回调地址
     * 用户在 QQ 授权后将跳转回此地址
     */
    @Value("${auth.qq.redirect-uri}")
    private String qqRedirectUri;

    /**
     * 微信应用标识 (App ID)
     * 从配置文件 auth.wechat.app-id 获取
     */
    @Value("${auth.wechat.app-id}")
    private String wechatAppId;

    /**
     * 微信应用密钥 (App Secret)
     * 从配置文件 auth.wechat.app-secret 获取
     */
    @Value("${auth.wechat.app-secret}")
    private String wechatAppSecret;

    /**
     * 微信认证回调地址
     * 用户在微信扫码授权后将跳转回此地址
     */
    @Value("${auth.wechat.redirect-uri}")
    private String wechatRedirectUri;

    /**
     * GitHub 客户端标识 (Client ID)
     * 从配置文件 auth.github.client-id 获取
     */
    @Value("${auth.github.client-id}")
    private String githubClientId;

    /**
     * GitHub 客户端密钥 (Client Secret)
     * 从配置文件 auth.github.client-secret 获取
     */
    @Value("${auth.github.client-secret}")
    private String githubClientSecret;

    /**
     * GitHub 认证回调地址
     * 用户在 GitHub 授权后将跳转回此地址
     */
    @Value("${auth.github.redirect-uri}")
    private String githubRedirectUri;

    /**
     * 配置 OAuth2 客户端注册存储库
     * <p>
     * 创建一个基于内存的 {@link ClientRegistrationRepository}，其中包含了：
     * <ul>
     *   <li><b>GitHub:</b> 标准 OAuth2 实现，使用 basic 认证方式获取令牌。</li>
     *   <li><b>QQ:</b> 腾讯互联实现，令牌获取使用 POST 方式。</li>
     *   <li><b>WeChat:</b> 微信开放平台实现，使用 snsapi_login 作用域。</li>
     * </ul>
     * </p>
     *
     * @return 包含所有已配置客户端的内存存储库实例
     */
    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        List<ClientRegistration> registrations = new ArrayList<>();

        // GitHub 客户端配置
        registrations.add(ClientRegistration.withRegistrationId("github")
                .clientId(githubClientId)
                .clientSecret(githubClientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(githubRedirectUri)
                .scope("read:user")
                .authorizationUri("https://github.com/login/oauth/authorize")
                .tokenUri("https://github.com/login/oauth/access_token")
                .userInfoUri("https://api.github.com/user")
                .userNameAttributeName("id")
                .clientName("GitHub")
                .build());

        // QQ 客户端配置
        registrations.add(ClientRegistration.withRegistrationId("qq")
                .clientId(qqAppId)
                .clientSecret(qqAppSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(qqRedirectUri)
                .authorizationUri("https://graph.qq.com/oauth2.0/authorize")
                .tokenUri("https://graph.qq.com/oauth2.0/token?fmt=json")
                .userInfoUri("https://graph.qq.com/user/get_user_info")
                .userNameAttributeName("openid")
                .clientName("QQ")
                .build());

        // WeChat 客户端配置
        registrations.add(ClientRegistration.withRegistrationId("wechat")
                .clientId(wechatAppId)
                .clientSecret(wechatAppSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(wechatRedirectUri)
                .scope("snsapi_login")
                .authorizationUri("https://open.weixin.qq.com/connect/qrconnect")
                .tokenUri("https://api.weixin.qq.com/sns/oauth2/access_token")
                .userInfoUri("https://api.weixin.qq.com/sns/userinfo")
                .userNameAttributeName("openid")
                .clientName("WeChat")
                .build());

        return new InMemoryClientRegistrationRepository(registrations);
    }
}
