package com.zsk.auth.strategy.impl;

import com.zsk.auth.strategy.OAuth2UserInfoStrategy;
import com.zsk.common.core.utils.JsonUtil;
import com.zsk.system.api.domain.SysUserApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * 微信用户信息策略实现
 * <p>
 * 实现了基于微信开放平台（OAuth2）协议的用户认证流程。
 * 针对微信特殊的令牌刷新和用户信息接口地址进行了定制化实现。
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Slf4j
@Component
public class WeChatUserInfoStrategy implements OAuth2UserInfoStrategy {

    /**
     * 通用的 RestClient 实例
     * 用于调用微信用户信息接口 sns/userinfo
     */
    private final RestClient restClient = RestClient.create();

    /**
     * 基于 RestClient 的令牌响应客户端
     * 微信的 Access Token 响应格式虽然符合 JSON，但通常建议通过定制化客户端来处理可能的非标字段
     */
    private final RestClientAuthorizationCodeTokenResponseClient tokenResponseClient = new RestClientAuthorizationCodeTokenResponseClient();

    /**
     * 获取微信注册 ID
     *
     * @return 始终返回 "wechat"
     */
    @Override
    public String getRegistrationId() {
        return "wechat";
    }

    /**
     * 向微信换取 Access Token
     *
     * @param grantRequest 包含授权码的请求
     * @return 微信返回的令牌响应对象
     */
    @Override
    public OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest grantRequest) {
        return tokenResponseClient.getTokenResponse(grantRequest);
    }

    /**
     * 加载微信用户信息并映射为系统用户
     * <p>
     * 流程：使用获取到的 access_token 和 openid 调用微信 sns/userinfo 接口。
     * 映射规则：
     * <ul>
     *   <li>用户名：wechat_{openid}</li>
     *   <li>昵称：取微信的 nickname</li>
     *   <li>头像：取微信的 headimgurl</li>
     * </ul>
     * </p>
     *
     * @param userRequest 包含 Access Token 和客户端信息的请求对象
     * @return 映射后的系统用户实体 SysUserApi
     * @throws RuntimeException 接口调用失败或微信返回错误码时抛出
     */
    @Override
    public SysUserApi getUserInfo(OAuth2UserRequest userRequest) {
        String accessToken = userRequest.getAccessToken().getTokenValue();
        // 微信在授权成功后，额外的参数中会包含 openid
        String openId = (String) userRequest.getAdditionalParameters().get("openid");

        if (openId == null) {
            throw new RuntimeException("获取微信 OpenID 失败");
        }

        // 调用微信用户信息接口
        String userInfoUrl = "https://api.weixin.qq.com/sns/userinfo?access_token=" + accessToken +
                "&openid=" + openId + "&lang=zh_CN";

        String userInfoResponse = restClient.get()
                .uri(userInfoUrl)
                .retrieve()
                .body(String.class);
        Map<String, Object> attributes = JsonUtil.parseMap(userInfoResponse);

        if (attributes == null || attributes.isEmpty()) {
            throw new RuntimeException("获取微信用户信息失败");
        }

        // errcode 为 0 或不存在表示成功
        if (attributes.containsKey("errcode") && (Integer) attributes.get("errcode") != 0) {
            throw new RuntimeException("获取微信用户信息失败: " + attributes.get("errmsg"));
        }

        SysUserApi user = new SysUserApi();
        user.setUserName(getRegistrationId() + "_" + openId);
        user.setNickName((String) attributes.get("nickname"));
        user.setAvatar((String) attributes.get("headimgurl"));
        user.setStatus("0");
        return user;
    }
}
