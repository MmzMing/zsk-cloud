package com.zsk.auth.strategy.impl;

import com.zsk.auth.strategy.OAuth2UserInfoStrategy;
import com.zsk.common.core.utils.JsonUtil;
import com.zsk.system.api.domain.SysUserApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.Map;

/**
 * QQ 用户信息策略实现
 * <p>
 * 实现了基于 QQ 互联（OAuth2）协议的用户认证流程。
 * 由于 QQ 响应的 Media Type 较为多样（如 text/plain），
 * 本实现通过自定义 RestClient 增强了对非标准响应类型的处理能力。
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Slf4j
@Component
public class QQUserInfoStrategy implements OAuth2UserInfoStrategy {

    /**
     * 通用的 RestClient 实例
     * 用于调用 QQ 的 OpenID 接口及获取详细用户信息接口
     */
    private final RestClient restClient = RestClient.create();

    /**
     * OAuth2 令牌响应客户端
     * 封装了向 QQ 换取 Access Token 的逻辑，并定制了消息转换器以支持多种媒体类型
     */
    private final OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> tokenResponseClient = createTokenResponseClient();

    /**
     * 初始化定制化的令牌响应客户端
     * <p>
     * QQ 的令牌响应有时会返回 {@code text/plain} 或 {@code text/html}，
     * 必须手动添加这些 Media Type 支持，否则 {@link OAuth2AccessTokenResponseHttpMessageConverter} 会报错。
     * </p>
     *
     * @return 配置好的 RestClientAuthorizationCodeTokenResponseClient
     */
    private static OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> createTokenResponseClient() {
        OAuth2AccessTokenResponseHttpMessageConverter tokenResponseMessageConverter = new OAuth2AccessTokenResponseHttpMessageConverter();
        // 增加对 QQ 特有的媒体类型支持
        tokenResponseMessageConverter.setSupportedMediaTypes(Arrays.asList(
                MediaType.APPLICATION_JSON,
                MediaType.TEXT_PLAIN,
                MediaType.TEXT_HTML,
                new MediaType("application", "x-javascript")
        ));

        RestClient restClient = RestClient.builder()
                .messageConverters(messageConverters -> {
                    messageConverters.add(0, new FormHttpMessageConverter());
                    messageConverters.add(1, tokenResponseMessageConverter);
                })
                .defaultStatusHandler(new OAuth2ErrorResponseErrorHandler())
                .build();

        RestClientAuthorizationCodeTokenResponseClient client = new RestClientAuthorizationCodeTokenResponseClient();
        client.setRestClient(restClient);
        return client;
    }

    /**
     * 获取 QQ 注册 ID
     *
     * @return 始终返回 "qq"
     */
    @Override
    public String getRegistrationId() {
        return "qq";
    }

    /**
     * 向 QQ 换取 Access Token
     *
     * @param grantRequest 包含授权码的请求
     * @return QQ 返回的令牌响应对象
     */
    @Override
    public OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest grantRequest) {
        return tokenResponseClient.getTokenResponse(grantRequest);
    }

    /**
     * 分两步获取 QQ 用户信息
     * <p>
     * 1. 调用 me 接口获取用户唯一标识 OpenID。<br>
     * 2. 调用 get_user_info 接口获取昵称、头像等详细资料。
     * </p>
     *
     * @param userRequest 包含 Access Token 的用户请求
     * @return 映射后的系统用户实体 SysUserApi
     * @throws RuntimeException 接口调用失败或返回码非 0 时抛出
     */
    @Override
    public SysUserApi getUserInfo(OAuth2UserRequest userRequest) {
        String accessToken = userRequest.getAccessToken().getTokenValue();
        String clientId = userRequest.getClientRegistration().getClientId();

        // 1. 获取 OpenID
        // QQ的 openid 需要单独调用 me 接口获取，响应格式通常为 callback( {"client_id":"...","openid":"..."} );
        String openIdUrl = "https://graph.qq.com/oauth2.0/me?access_token=" + accessToken;
        String openIdResponse = restClient.get()
                .uri(openIdUrl)
                .retrieve()
                .body(String.class);
        String openId = parseOpenId(openIdResponse);

        if (openId == null) {
            throw new RuntimeException("获取QQ OpenID失败");
        }

        // 2. 获取用户信息
        String userInfoUrl = "https://graph.qq.com/user/get_user_info?access_token=" + accessToken +
                "&oauth_consumer_key=" + clientId +
                "&openid=" + openId + "&fmt=json";

        String userInfoResponse = restClient.get()
                .uri(userInfoUrl)
                .retrieve()
                .body(String.class);
        Map<String, Object> attributes = JsonUtil.parseMap(userInfoResponse);

        if (attributes == null || attributes.isEmpty()) {
            throw new RuntimeException("获取QQ用户信息失败");
        }

        // ret 为 0 表示成功
        if (attributes.containsKey("ret") && (Integer) attributes.get("ret") != 0) {
            throw new RuntimeException("获取QQ用户信息失败: " + attributes.get("msg"));
        }

        SysUserApi user = new SysUserApi();
        user.setUserName(getRegistrationId() + "_" + openId);
        user.setNickName((String) attributes.get("nickname"));
        user.setAvatar((String) attributes.get("figureurl_qq_2")); // 优先取 100x100 高清头像
        if (user.getAvatar() == null) {
            user.setAvatar((String) attributes.get("figureurl_qq_1")); // 兜底取 40x40 头像
        }
        user.setStatus("0");
        return user;
    }

    /**
     * 从 QQ 的 JSONP 格式响应中提取 OpenID
     *
     * @param response 接口返回的字符串数据
     * @return 提取出的 OpenID 字符串，找不到则返回 null
     */
    private String parseOpenId(String response) {
        if (response != null && response.contains("openid")) {
            int start = response.indexOf("\"openid\":\"");
            if (start != -1) {
                start += 10;
                int end = response.indexOf("\"", start);
                if (end != -1) {
                    return response.substring(start, end);
                }
            }
        }
        return null;
    }
}
