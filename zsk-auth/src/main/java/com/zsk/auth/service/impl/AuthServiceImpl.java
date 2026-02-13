package com.zsk.auth.service.impl;

import com.zsk.auth.domain.LoginRequest;
import com.zsk.auth.domain.LoginResponse;
import com.zsk.auth.domain.RegisterBody;
import com.zsk.auth.service.IAuthService;
import com.zsk.auth.service.ICaptchaService;
import com.zsk.auth.service.IEmailService;
import com.zsk.auth.service.IThirdPartyAuthService;
import com.zsk.common.core.constant.CacheConstants;
import com.zsk.common.core.constant.CommonConstants;
import com.zsk.common.core.constant.SecurityConstants;
import com.zsk.common.core.domain.R;
import com.zsk.common.core.exception.AuthException;
import com.zsk.common.core.exception.BusinessException;
import com.zsk.common.core.utils.JwtUtils;
import com.zsk.common.core.utils.JsonUtil;
import com.zsk.common.core.utils.StringUtils;
import com.zsk.common.redis.service.RedisService;
import com.zsk.common.security.utils.SecurityUtils;
import com.zsk.system.api.RemoteUserService;
import com.zsk.system.api.domain.SysUserApi;
import com.zsk.system.api.model.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2024-01-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final RemoteUserService remoteUserService;
    private final ICaptchaService captchaService;
    private final IEmailService emailService;
    private final IThirdPartyAuthService thirdPartyAuthService;
    private final RedisService redisService;

    /**
     * 用户登录处理
     *
     * @param request 登录请求参数（包含登录类型、用户名、密码等）
     * @return 登录结果（包含访问令牌、刷新令牌、用户信息）
     * @throws AuthException 认证异常
     */
    @Override
    public LoginResponse login(LoginRequest request) {
        String loginType = request.getLoginType();

        return switch (loginType) {
            case "password" -> passwordLogin(request);
            case "email" -> emailLogin(request);
            case "qq", "wechat", "github" -> thirdPartyLogin(request);
            default -> throw new AuthException("不支持的登录类型: " + loginType);
        };
    }

    /**
     * 用户注册处理
     *
     * @param registerBody 注册请求信息
     * @throws BusinessException 注册业务异常
     */
    @Override
    public void register(RegisterBody registerBody) {
        String username = registerBody.getUsername();
        String password = registerBody.getPassword();
        String confirmPassword = registerBody.getConfirmPassword();
        String code = registerBody.getCode();
        String uuid = registerBody.getUuid();

        // 验证验证码
        captchaService.validateCaptcha(uuid, code);

        if (!password.equals(confirmPassword)) {
            throw new BusinessException("密码和确认密码不一致");
        }

        // 检查用户是否已存在
        R<LoginUser> result = remoteUserService.getUserInfo(username, CommonConstants.REQUEST_SOURCE_INNER);
        if (result != null && result.isSuccess() && result.getData() != null) {
            throw new BusinessException("保存用户'" + username + "'失败，注册账号已存在");
        }

        SysUserApi sysUser = new SysUserApi();
        sysUser.setUserName(username);
        sysUser.setNickName(username);
        sysUser.setPassword(SecurityUtils.encryptPassword(password));
        sysUser.setStatus("0"); // 正常状态
        sysUser.setUserType("00"); // 系统用户

        R<Boolean> registerResult = remoteUserService.createUser(sysUser);

        if (registerResult == null || !registerResult.isSuccess()) {
            String msg = registerResult != null ? registerResult.getMessage() : "注册失败";
            throw new BusinessException(msg);
        }
    }

    /**
     * 账号密码登录实现
     *
     * @param request 登录请求参数
     * @return 登录结果
     */
    private LoginResponse passwordLogin(LoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();
        String code = request.getCode();
        String uuid = request.getUuid();

        if (StringUtils.isEmpty(code) || StringUtils.isEmpty(uuid)) {
            throw new AuthException("验证码不能为空");
        }

        captchaService.validateCaptcha(uuid, code);

        R<LoginUser> userResult = remoteUserService.getUserInfo(username, CommonConstants.REQUEST_SOURCE_INNER);
        if (userResult == null || !userResult.isSuccess()) {
            throw new AuthException("用户不存在");
        }

        LoginUser loginUser = userResult.getData();
        if (loginUser == null || loginUser.getSysUser() == null) {
            throw new AuthException("用户不存在");
        }

        SysUserApi user = loginUser.getSysUser();
        if (!SecurityUtils.matchesPassword(password, user.getPassword())) {
            throw new AuthException("用户名或密码错误");
        }

        if ("1".equals(user.getStatus())) {
            throw new AuthException("账号已被停用");
        }

        return generateToken(loginUser);
    }

    /**
     * 邮箱验证码登录实现
     *
     * @param request 登录请求参数
     * @return 登录结果
     */
    private LoginResponse emailLogin(LoginRequest request) {
        String email = request.getEmail();
        String emailCode = request.getEmailCode();

        if (StringUtils.isEmpty(email) || StringUtils.isEmpty(emailCode)) {
            throw new AuthException("邮箱和验证码不能为空");
        }

        emailService.validateEmailCode(email, emailCode);

        R<LoginUser> userResult = remoteUserService.getUserInfoByEmail(email, CommonConstants.REQUEST_SOURCE_INNER);
        if (userResult == null || !userResult.isSuccess()) {
            throw new AuthException("用户不存在");
        }

        LoginUser loginUser = userResult.getData();
        if (loginUser == null || loginUser.getSysUser() == null) {
            throw new AuthException("用户不存在");
        }

        SysUserApi user = loginUser.getSysUser();
        if ("1".equals(user.getStatus())) {
            throw new AuthException("账号已被停用");
        }

        return generateToken(loginUser);
    }

    /**
     * 第三方授权登录实现
     *
     * @param request 登录请求参数
     * @return 登录结果
     */
    private LoginResponse thirdPartyLogin(LoginRequest request) {
        String loginType = request.getLoginType();
        String authCode = request.getAuthCode();
        String state = request.getState();

        if (StringUtils.isEmpty(authCode)) {
            throw new AuthException("授权码不能为空");
        }

        SysUserApi user = thirdPartyAuthService.getUserByAuthCode(loginType, authCode, state);
        if (user == null) {
            throw new AuthException("第三方登录失败");
        }

        if ("1".equals(user.getStatus())) {
            throw new AuthException("账号已被停用");
        }

        // 第三方登录也需要获取权限信息
        R<LoginUser> loginUserResult = remoteUserService.getUserByThirdPartyId(loginType, user.getUserName().substring(loginType.length() + 1), CommonConstants.REQUEST_SOURCE_INNER);
        if (loginUserResult == null || !loginUserResult.isSuccess()) {
            throw new AuthException("获取用户信息失败");
        }

        return generateToken(loginUserResult.getData());
    }

    /**
     * 生成完整的登录令牌响应
     *
     * @param loginUser 登录用户信息
     * @return 登录响应结果
     */
    private LoginResponse generateToken(LoginUser loginUser) {
        SysUserApi user = loginUser.getSysUser();
        String accessToken = generateAccessToken(loginUser);
        String refreshToken = generateRefreshToken(loginUser);

        LoginResponse response = LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(SecurityConstants.TOKEN_EXPIRE * 60)
                .userId(user.getId())
                .username(user.getUserName())
                .nickname(user.getNickName())
                .avatar(user.getAvatar())
                .build();

        return response;
    }

    /**
     * 生成访问令牌（Access Token）并缓存
     *
     * @param loginUser 登录用户信息
     * @return 访问令牌
     */
    private String generateAccessToken(LoginUser loginUser) {
        SysUserApi user = loginUser.getSysUser();
        // 生成唯一标识 jti (JWT ID)，作为 Redis Key
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String tokenKey = CacheConstants.LOGIN_TOKEN_KEY + uuid;

        Map<String, Object> claims = new HashMap<>();
        claims.put(SecurityConstants.USER_KEY, uuid); // 存入 uuid 用于后续验证 Redis 状态
        claims.put(SecurityConstants.USER_ID, user.getId());
        claims.put(SecurityConstants.USER_NAME, user.getUserName());
        claims.put(SecurityConstants.NICK_NAME, user.getNickName());
        // claims.put("avatar", user.getAvatar()); // 头像信息较大且非必要，可不存
        claims.put(SecurityConstants.ROLES, loginUser.getRoles());
        claims.put(SecurityConstants.PERMISSIONS, loginUser.getPermissions());
        // claims.put("loginTime", System.currentTimeMillis());

        // Redis 只存储简单的状态信息（如 userId），不再存储全量用户信息
        // 这样既利用了 JWT 的无状态特性传递数据，又利用 Redis 实现了 Token 的可控性（过期、黑名单）
        redisService.setCacheObject(tokenKey, user.getId(), SecurityConstants.TOKEN_EXPIRE, TimeUnit.MINUTES);
        
        // 生成 JWT
        return JwtUtils.createToken(claims);
    }

    /**
     * 生成刷新令牌（Refresh Token）并缓存
     *
     * @param loginUser 登录用户信息
     * @return 刷新令牌
     */
    private String generateRefreshToken(LoginUser loginUser) {
        SysUserApi user = loginUser.getSysUser();
        String token = UUID.randomUUID().toString().replace("-", "");
        String tokenKey = CacheConstants.LOGIN_REFRESH_TOKEN_KEY + token;

        Map<String, Object> tokenInfo = new HashMap<>();
        tokenInfo.put("userId", user.getId());
        tokenInfo.put("username", user.getUserName());

        redisService.setCacheObject(tokenKey, JsonUtil.toJsonString(tokenInfo), SecurityConstants.REFRESH_TOKEN_EXPIRE, TimeUnit.DAYS);
        return token;
    }

    /**
     * 刷新令牌处理
     *
     * @param refreshToken 刷新令牌
     * @return 新的登录结果
     * @throws AuthException 刷新失败异常
     */
    @Override
    public LoginResponse refreshToken(String refreshToken) {
        if (StringUtils.isEmpty(refreshToken)) {
            throw new AuthException("刷新令牌不能为空");
        }

        String tokenKey = CacheConstants.LOGIN_REFRESH_TOKEN_KEY + refreshToken;
        String tokenInfoJson = redisService.getCacheObject(tokenKey);

        if (StringUtils.isEmpty(tokenInfoJson)) {
            throw new AuthException("刷新令牌已过期或不存在");
        }

        Map<String, Object> tokenInfo = JsonUtil.parseMap(tokenInfoJson);
        String username = tokenInfo.get("username").toString();

        R<LoginUser> userResult = remoteUserService.getUserInfo(username, CommonConstants.REQUEST_SOURCE_INNER);
        if (userResult == null || !userResult.isSuccess()) {
            throw new AuthException("用户不存在");
        }

        LoginUser loginUser = userResult.getData();
        if (loginUser == null || loginUser.getSysUser() == null) {
            throw new AuthException("用户不存在");
        }

        redisService.deleteObject(tokenKey);
        return generateToken(loginUser);
    }

    /**
     * 退出登录处理
     *
     * @param token 访问令牌
     */
    @Override
    public void logout(String token) {
        if (StringUtils.isEmpty(token)) {
            return;
        }

        String tokenKey = CacheConstants.LOGIN_TOKEN_KEY + token;
        redisService.deleteObject(tokenKey);
    }
}
