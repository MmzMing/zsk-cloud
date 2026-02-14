package com.zsk.auth.strategy;

import com.zsk.system.api.domain.SysUserApi;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;

/**
 * OAuth2 用户信息策略接口
 * <p>
 * 定义了第三方登录（OAuth2）的核心操作流程，包括获取注册标识、换取访问令牌以及解析用户信息。
 * 不同的第三方平台（如 GitHub、QQ、微信）需实现此接口以处理其特有的协议逻辑。
 * </p>
 * 
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
public interface OAuth2UserInfoStrategy {

    /**
     * 获取客户端注册 ID (RegistrationId)
     * <p>
     * 该 ID 对应 {@code ClientRegistration} 中的 registrationId，
     * 用于在策略工厂中唯一标识和检索对应的策略实现。
     * </p>
     *
     * @return 注册标识字符串（如 "github", "qq", "wechat"）
     */
    String getRegistrationId();

    /**
     * 获取 Access Token 响应
     * <p>
     * 使用授权码（Authorization Code）向第三方认证服务器发起请求，换取访问令牌。
     * 不同平台可能对参数编码、响应格式有特殊要求，实现类需在此处处理。
     * </p>
     *
     * @param grantRequest 授权码授权请求对象，包含客户端注册信息及授权交换码
     * @return OAuth2AccessTokenResponse 包含 access_token、refresh_token 及过期时间等信息
     */
    OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest grantRequest);

    /**
     * 获取并解析第三方用户信息
     * <p>
     * 使用已获取的 Access Token 调用第三方平台的用户信息接口，并将返回的原始属性
     * 映射为系统统一的 {@link SysUserApi} 实体对象。
     * </p>
     *
     * @param userRequest 包含 Access Token 及客户端注册信息的用户请求对象
     * @return 系统统一的用户信息实体，包含用户名、昵称、头像等基本信息
     */
    SysUserApi getUserInfo(OAuth2UserRequest userRequest);
}
