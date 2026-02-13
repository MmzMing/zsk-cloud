package com.zsk.auth.service.impl;

import com.zsk.system.api.RemoteUserService;
import com.zsk.system.api.domain.SysUserApi;
import com.zsk.auth.service.IThirdPartyAuthService;
import com.zsk.common.core.constant.CacheConstants;
import com.zsk.common.core.constant.CommonConstants;
import com.zsk.system.api.model.LoginUser;
import com.zsk.common.core.domain.R;
import com.zsk.common.core.exception.AuthException;
import com.zsk.common.core.utils.JsonUtil;
import com.zsk.common.core.utils.StringUtils;
import com.zsk.common.redis.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 第三方认证服务实现
 * 
 * @author wuhuaming
 * @date 2024-01-15
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ThirdPartyAuthServiceImpl implements IThirdPartyAuthService {

    private final RemoteUserService remoteUserService;
    private final RedisService redisService;

    /** QQ AppId */
    @Value("${auth.qq.app-id}")
    private String qqAppId;

    /** QQ AppSecret */
    @Value("${auth.qq.app-secret}")
    private String qqAppSecret;

    /** QQ 重定向地址 */
    @Value("${auth.qq.redirect-uri}")
    private String qqRedirectUri;

    /** 微信 AppId */
    @Value("${auth.wechat.app-id}")
    private String wechatAppId;

    /** 微信 AppSecret */
    @Value("${auth.wechat.app-secret}")
    private String wechatAppSecret;

    /** 微信 重定向地址 */
    @Value("${auth.wechat.redirect-uri}")
    private String wechatRedirectUri;

    /** GitHub ClientId */
    @Value("${auth.github.client-id}")
    private String githubClientId;

    /** GitHub ClientSecret */
    @Value("${auth.github.client-secret}")
    private String githubClientSecret;

    /** GitHub 重定向地址 */
    @Value("${auth.github.redirect-uri}")
    private String githubRedirectUri;

    /**
     * 根据授权码获取第三方用户信息并登录/注册
     * 
     * @param loginType 登录类型（qq, wechat, github）
     * @param authCode 授权码
     * @param state 状态码（用于防止CSRF攻击）
     * @return 系统用户信息
     * @throws AuthException 认证失败时抛出
     */
    @Override
    public SysUserApi getUserByAuthCode(String loginType, String authCode, String state) {
        String accessToken = getAccessToken(loginType, authCode);
        if (StringUtils.isEmpty(accessToken)) {
            throw new AuthException("获取第三方访问令牌失败");
        }

        Map<String, Object> userInfo = getUserInfo(loginType, accessToken);
        if (userInfo == null || userInfo.isEmpty()) {
            throw new AuthException("获取第三方用户信息失败");
        }

        String thirdPartyId = getThirdPartyId(loginType, userInfo);
        String thirdPartyUsername = getThirdPartyUsername(loginType, userInfo);
        String thirdPartyAvatar = getThirdPartyAvatar(loginType, userInfo);

        R<LoginUser> userResult = remoteUserService.getUserByThirdPartyId(loginType, thirdPartyId, CommonConstants.REQUEST_SOURCE_INNER);
        if (userResult != null && userResult.isSuccess() && userResult.getData() != null) {
            return userResult.getData().getSysUser();
        }

        // 首次登录，自动注册
        SysUserApi sysUser = new SysUserApi();
        sysUser.setUserName(loginType + "_" + thirdPartyId);
        sysUser.setNickName(thirdPartyUsername);
        sysUser.setAvatar(thirdPartyAvatar);
        sysUser.setStatus("0");

        R<Boolean> createResult = remoteUserService.createUser(sysUser);
        if (createResult != null && createResult.isSuccess() && createResult.getData()) {
            return sysUser;
        }

        throw new AuthException("第三方登录自动注册失败");
    }

    /**
     * 获取第三方授权登录URL
     * 
     * @param loginType 登录类型
     * @return 授权跳转URL
     */
    @Override
    public String getAuthUrl(String loginType) {
        String state = UUID.randomUUID().toString().replace("-", "");
        String stateKey = CacheConstants.THIRD_PARTY_STATE_KEY + state;
        redisService.setCacheObject(stateKey, loginType, 10, java.util.concurrent.TimeUnit.MINUTES);

        return switch (loginType) {
            case "qq" -> "https://graph.qq.com/oauth2.0/authorize?response_type=code&client_id=" + qqAppId
                    + "&redirect_uri=" + qqRedirectUri + "&state=" + state;
            case "wechat" -> "https://open.weixin.qq.com/connect/qrconnect?appid=" + wechatAppId
                    + "&redirect_uri=" + wechatRedirectUri + "&response_type=code&scope=snsapi_login&state=" + state;
            case "github" -> "https://github.com/login/oauth/authorize?client_id=" + githubClientId
                    + "&redirect_uri=" + githubRedirectUri + "&state=" + state;
            default -> throw new AuthException("不支持的登录类型: " + loginType);
        };
    }

    /**
     * 获取访问令牌
     * 
     * @param loginType 登录类型
     * @param authCode 授权码
     * @return 访问令牌
     */
    private String getAccessToken(String loginType, String authCode) {
        return switch (loginType) {
            case "qq" -> getQQAccessToken(authCode);
            case "wechat" -> getWechatAccessToken(authCode);
            case "github" -> getGithubAccessToken(authCode);
            default -> null;
        };
    }

    /**
     * 获取QQ访问令牌
     * 
     * @param authCode 授权码
     * @return QQ访问令牌
     */
    private String getQQAccessToken(String authCode) {
        String url = "https://graph.qq.com/oauth2.0/token?grant_type=authorization_code&client_id=" + qqAppId
                + "&client_secret=" + qqAppSecret + "&code=" + authCode + "&redirect_uri=" + qqRedirectUri;

        try {
            String response = sendGetRequest(url);
            Map<String, Object> result = parseResponse(response);
            return result != null ? result.get("access_token").toString() : null;
        } catch (Exception e) {
            log.error("获取QQ访问令牌失败", e);
            return null;
        }
    }

    /**
     * 获取微信访问令牌
     * 
     * @param authCode 授权码
     * @return 微信访问令牌
     */
    private String getWechatAccessToken(String authCode) {
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + wechatAppId
                + "&secret=" + wechatAppSecret + "&code=" + authCode + "&grant_type=authorization_code";

        try {
            String response = sendGetRequest(url);
            Map<String, Object> result = JsonUtil.parseMap(response);
            return result != null ? result.get("access_token").toString() : null;
        } catch (Exception e) {
            log.error("获取微信访问令牌失败", e);
            return null;
        }
    }

    /**
     * 获取GitHub访问令牌
     * 
     * @param authCode 授权码
     * @return GitHub访问令牌
     */
    private String getGithubAccessToken(String authCode) {
        String url = "https://github.com/login/oauth/access_token";

        try {
            Map<String, String> params = new HashMap<>();
            params.put("client_id", githubClientId);
            params.put("client_secret", githubClientSecret);
            params.put("code", authCode);

            String response = sendPostRequest(url, params);
            Map<String, Object> result = parseResponse(response);
            return result != null ? result.get("access_token").toString() : null;
        } catch (Exception e) {
            log.error("获取GitHub访问令牌失败", e);
            return null;
        }
    }

    /**
     * 获取用户信息
     * 
     * @param loginType 登录类型
     * @param accessToken 访问令牌
     * @return 用户信息Map
     */
    private Map<String, Object> getUserInfo(String loginType, String accessToken) {
        return switch (loginType) {
            case "qq" -> getQQUserInfo(accessToken);
            case "wechat" -> getWechatUserInfo(accessToken);
            case "github" -> getGithubUserInfo(accessToken);
            default -> null;
        };
    }

    /**
     * 获取QQ用户信息
     * 
     * @param accessToken 访问令牌
     * @return QQ用户信息Map
     */
    private Map<String, Object> getQQUserInfo(String accessToken) {
        String url = "https://graph.qq.com/user/get_user_info?access_token=" + accessToken + "&oauth_consumer_key=" + qqAppId + "&fmt=json";

        try {
            String response = sendGetRequest(url);
            return JsonUtil.parseMap(response);
        } catch (Exception e) {
            log.error("获取QQ用户信息失败", e);
            return null;
        }
    }

    /**
     * 获取微信用户信息
     * 
     * @param accessToken 访问令牌
     * @return 微信用户信息Map
     */
    private Map<String, Object> getWechatUserInfo(String accessToken) {
        String url = "https://api.weixin.qq.com/sns/userinfo?access_token=" + accessToken + "&lang=zh_CN";

        try {
            String response = sendGetRequest(url);
            return JsonUtil.parseMap(response);
        } catch (Exception e) {
            log.error("获取微信用户信息失败", e);
            return null;
        }
    }

    /**
     * 获取GitHub用户信息
     * 
     * @param accessToken 访问令牌
     * @return GitHub用户信息Map
     */
    private Map<String, Object> getGithubUserInfo(String accessToken) {
        String url = "https://api.github.com/user";

        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "token " + accessToken);

            String response = sendGetRequest(url, headers);
            return JsonUtil.parseMap(response);
        } catch (Exception e) {
            log.error("获取GitHub用户信息失败", e);
            return null;
        }
    }

    /**
     * 提取第三方唯一标识ID
     * 
     * @param loginType 登录类型
     * @param userInfo 用户信息Map
     * @return 第三方唯一标识
     */
    private String getThirdPartyId(String loginType, Map<String, Object> userInfo) {
        return switch (loginType) {
            case "qq" -> userInfo.get("openid") != null ? userInfo.get("openid").toString() : "";
            case "wechat" -> userInfo.get("openid") != null ? userInfo.get("openid").toString() : "";
            case "github" -> userInfo.get("id") != null ? userInfo.get("id").toString() : "";
            default -> "";
        };
    }

    /**
     * 提取第三方用户名
     * 
     * @param loginType 登录类型
     * @param userInfo 用户信息Map
     * @return 第三方用户名
     */
    private String getThirdPartyUsername(String loginType, Map<String, Object> userInfo) {
        return switch (loginType) {
            case "qq" -> userInfo.get("nickname") != null ? userInfo.get("nickname").toString() : "";
            case "wechat" -> userInfo.get("nickname") != null ? userInfo.get("nickname").toString() : "";
            case "github" -> userInfo.get("login") != null ? userInfo.get("login").toString() : "";
            default -> "";
        };
    }

    /**
     * 提取第三方头像URL
     * 
     * @param loginType 登录类型
     * @param userInfo 用户信息Map
     * @return 第三方头像URL
     */
    private String getThirdPartyAvatar(String loginType, Map<String, Object> userInfo) {
        return switch (loginType) {
            case "qq" -> userInfo.get("figureurl_qq_2") != null ? userInfo.get("figureurl_qq_2").toString() : "";
            case "wechat" -> userInfo.get("headimgurl") != null ? userInfo.get("headimgurl").toString() : "";
            case "github" -> userInfo.get("avatar_url") != null ? userInfo.get("avatar_url").toString() : "";
            default -> "";
        };
    }

    /**
     * 发送GET请求
     * 
     * @param url 请求URL
     * @return 响应内容
     */
    private String sendGetRequest(String url) {
        return sendGetRequest(url, null);
    }

    /**
     * 发送GET请求（带请求头）
     * 
     * @param url 请求URL
     * @param headers 请求头Map
     * @return 响应内容
     */
    private String sendGetRequest(String url, Map<String, String> headers) {
        try {
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
        } catch (Exception e) {
            log.error("发送GET请求失败: {}", url, e);
            return null;
        }
    }

    /**
     * 发送POST请求
     * 
     * @param url 请求URL
     * @param params POST参数Map
     * @return 响应内容
     */
    private String sendPostRequest(String url, Map<String, String> params) {
        try {
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setDoOutput(true);

            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (postData.length() > 0) {
                    postData.append("&");
                }
                postData.append(java.net.URLEncoder.encode(entry.getKey(), "UTF-8"));
                postData.append("=");
                postData.append(java.net.URLEncoder.encode(entry.getValue(), "UTF-8"));
            }

            try (java.io.OutputStream os = connection.getOutputStream()) {
                byte[] input = postData.toString().getBytes("UTF-8");
                os.write(input, 0, input.length);
            }

            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
        } catch (Exception e) {
            log.error("发送POST请求失败: {}", url, e);
            return null;
        }
    }

    /**
     * 解析HTTP响应内容（支持JSON和URL查询字符串格式）
     * 
     * @param response 原始响应字符串
     * @return 解析后的Map
     */
    private Map<String, Object> parseResponse(String response) {
        if (StringUtils.isEmpty(response)) {
            return null;
        }

        if (response.startsWith("{")) {
            return JsonUtil.parseMap(response);
        }

        Map<String, Object> result = new HashMap<>();
        String[] pairs = response.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                result.put(keyValue[0], keyValue[1]);
            }
        }
        return result;
    }
}
