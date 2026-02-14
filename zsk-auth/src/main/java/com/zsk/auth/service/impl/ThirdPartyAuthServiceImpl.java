package com.zsk.auth.service.impl;

import com.zsk.auth.service.IThirdPartyAuthService;
import com.zsk.auth.strategy.OAuth2UserInfoStrategy;
import com.zsk.common.core.constant.CacheConstants;
import com.zsk.common.core.constant.CommonConstants;
import com.zsk.common.core.domain.R;
import com.zsk.common.core.exception.AuthException;
import com.zsk.common.core.utils.StringUtils;
import com.zsk.common.redis.service.RedisService;
import com.zsk.system.api.RemoteUserService;
import com.zsk.system.api.domain.SysUserApi;
import com.zsk.system.api.model.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 第三方认证服务实现
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ThirdPartyAuthServiceImpl implements IThirdPartyAuthService {

    /** 远程用户服务 */
    private final RemoteUserService remoteUserService;

    /** Redis服务 */
    private final RedisService redisService;

    /** 客户端注册存储库 */
    private final ClientRegistrationRepository clientRegistrationRepository;

    /** OAuth2用户信息策略列表 */
    private final List<OAuth2UserInfoStrategy> strategies;
    
    /** 策略缓存映射 */
    private final Map<String, OAuth2UserInfoStrategy> strategyMap = new ConcurrentHashMap<>();

    /**
     * 根据登录类型获取对应的用户信息策略
     *
     * @param loginType 登录类型（github, qq, wechat等）
     * @return OAuth2用户信息策略
     */
    private OAuth2UserInfoStrategy getStrategy(String loginType) {
        if (strategyMap.isEmpty()) {
            strategies.forEach(s -> strategyMap.put(s.getRegistrationId(), s));
        }
        return strategyMap.get(loginType);
    }

    /**
     * 根据授权码获取第三方用户信息并登录/注册
     *
     * @param loginType 登录类型
     * @param authCode 授权码
     * @param state 状态码
     * @return 系统用户信息
     * @throws AuthException 认证异常
     */
    @Override
    public SysUserApi getUserByAuthCode(String loginType, String authCode, String state) {
        // 校验 state，防止 CSRF 攻击
        validateState(loginType, state);

        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId(loginType);
        if (clientRegistration == null) {
            throw new AuthException("不支持的登录类型: " + loginType);
        }

        OAuth2UserInfoStrategy strategy = getStrategy(loginType);
        if (strategy == null) {
            throw new AuthException("未找到对应的策略实现: " + loginType);
        }

        // 1. 获取 Access Token
        OAuth2AccessTokenResponse tokenResponse;
        try {
            tokenResponse = getTokenResponse(clientRegistration, authCode, state, strategy);
        } catch (Exception e) {
            log.error("获取第三方访问令牌失败", e);
            throw new AuthException("获取第三方访问令牌失败: " + e.getMessage());
        }

        // 2. 获取用户信息
        SysUserApi thirdPartyUser;
        try {
            OAuth2UserRequest userRequest = new OAuth2UserRequest(clientRegistration, tokenResponse.getAccessToken(), tokenResponse.getAdditionalParameters());
            thirdPartyUser = strategy.getUserInfo(userRequest);
        } catch (Exception e) {
            log.error("获取第三方用户信息失败", e);
            throw new AuthException("获取第三方用户信息失败: " + e.getMessage());
        }

        // 3. 登录/注册逻辑
        return processLoginOrRegister(loginType, thirdPartyUser);
    }

    /**
     * 获取第三方访问令牌响应
     *
     * @param clientRegistration 客户端注册信息
     * @param authCode 授权码
     * @param state 状态码
     * @param strategy 用户信息策略
     * @return 访问令牌响应
     */
    private OAuth2AccessTokenResponse getTokenResponse(ClientRegistration clientRegistration, String authCode, String state, OAuth2UserInfoStrategy strategy) {
        // 构建 OAuth2AuthorizationRequest 和 OAuth2AuthorizationResponse 以满足 OAuth2AuthorizationExchange
        // 注意：redirectUri 必须与发起请求时的一致
        OAuth2AuthorizationRequest authorizationRequest = OAuth2AuthorizationRequest.authorizationCode()
                .clientId(clientRegistration.getClientId())
                .authorizationUri(clientRegistration.getProviderDetails().getAuthorizationUri())
                .redirectUri(clientRegistration.getRedirectUri())
                .scopes(clientRegistration.getScopes())
                .state(state)
                .attributes(Map.of(OAuth2AuthorizationRequest.class.getName(), "")) // 避免 build 校验报错
                .build();

        OAuth2AuthorizationResponse authorizationResponse = OAuth2AuthorizationResponse.success(authCode)
                .redirectUri(clientRegistration.getRedirectUri())
                .state(state)
                .build();

        OAuth2AuthorizationExchange authorizationExchange = new OAuth2AuthorizationExchange(authorizationRequest, authorizationResponse);
        OAuth2AuthorizationCodeGrantRequest grantRequest = new OAuth2AuthorizationCodeGrantRequest(clientRegistration, authorizationExchange);
        
        return strategy.getTokenResponse(grantRequest);
    }

    /**
     * 处理登录或注册逻辑
     *
     * @param loginType 登录类型
     * @param thirdPartyUser 第三方用户信息
     * @return 系统用户信息
     */
    private SysUserApi processLoginOrRegister(String loginType, SysUserApi thirdPartyUser) {
        String thirdPartyId = thirdPartyUser.getUserName().substring(loginType.length() + 1); // remove prefix
        
        // 查询是否绑定
        R<LoginUser> userResult = remoteUserService.getUserByThirdPartyId(loginType, thirdPartyId, CommonConstants.REQUEST_SOURCE_INNER);
        if (userResult != null && userResult.isSuccess() && userResult.getData() != null) {
            return userResult.getData().getSysUser();
        }

        // 首次登录，自动注册
        R<Boolean> createResult = remoteUserService.createUser(thirdPartyUser);
        if (createResult != null && createResult.isSuccess() && createResult.getData()) {
            return thirdPartyUser;
        }

        throw new AuthException("第三方登录自动注册失败");
    }

    /**
     * 校验 state，防止 CSRF 攻击
     *
     * @param loginType 登录类型
     * @param state 状态码
     */
    private void validateState(String loginType, String state) {
        if (StringUtils.isEmpty(state)) {
            throw new AuthException("state不能为空");
        }
        String stateKey = CacheConstants.THIRD_PARTY_STATE_KEY + state;
        String savedLoginType = redisService.getCacheObject(stateKey);
        
        if (StringUtils.isEmpty(savedLoginType) || !savedLoginType.equals(loginType)) {
            throw new AuthException("非法请求或state已过期");
        }
        
        // 验证通过后删除 state，防止重放
        redisService.deleteObject(stateKey);
    }

    /**
     * 获取第三方授权登录URL
     *
     * @param loginType 登录类型
     * @return 授权URL
     */
    @Override
    public String getAuthUrl(String loginType) {
        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId(loginType);
        if (clientRegistration == null) {
            throw new AuthException("不支持的登录类型: " + loginType);
        }

        String state = UUID.randomUUID().toString().replace("-", "");
        String stateKey = CacheConstants.THIRD_PARTY_STATE_KEY + state;
        redisService.setCacheObject(stateKey, loginType, 10, java.util.concurrent.TimeUnit.MINUTES);

        // 构建授权URL
        StringBuilder url = new StringBuilder(clientRegistration.getProviderDetails().getAuthorizationUri());
        url.append("?response_type=code");
        url.append("&client_id=").append(clientRegistration.getClientId());
        url.append("&redirect_uri=").append(clientRegistration.getRedirectUri());
        url.append("&state=").append(state);
        
        if (clientRegistration.getScopes() != null && !clientRegistration.getScopes().isEmpty()) {
            url.append("&scope=").append(String.join(" ", clientRegistration.getScopes()));
        }
        
        return url.toString();
    }
}
