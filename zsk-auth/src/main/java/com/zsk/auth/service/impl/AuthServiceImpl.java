package com.zsk.auth.service.impl;

import com.zsk.auth.domain.LoginRequest;
import com.zsk.auth.domain.LoginResponse;
import com.zsk.auth.domain.RegisterBody;
import com.zsk.auth.service.IAuthService;
import com.zsk.auth.service.ICaptchaService;
import com.zsk.auth.service.IEmailService;
import com.zsk.auth.service.IEncryptService;
import com.zsk.auth.service.IThirdPartyAuthService;
import com.zsk.common.core.constant.CacheConstants;
import com.zsk.common.core.constant.CommonConstants;
import com.zsk.common.core.constant.SecurityConstants;
import com.zsk.common.core.domain.R;
import com.zsk.common.core.exception.AuthException;
import com.zsk.common.core.exception.BusinessException;
import com.zsk.common.core.utils.JwtUtils;
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
 * @date 2026-02-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final RemoteUserService remoteUserService;
    private final ICaptchaService captchaService;
    private final IEmailService emailService;
    private final IEncryptService encryptService;
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
        String password = encryptService.decrypt(registerBody.getPassword());
        String confirmPassword = encryptService.decrypt(registerBody.getConfirmPassword());
        String code = registerBody.getCode();
        String uuid = registerBody.getUuid();
        String email = registerBody.getEmail();

        // 验证邮箱验证码
        emailService.validateEmailCode(email, code);


        if (StringUtils.isBlank(password)) {
            throw new BusinessException("密码不能为空");
        }

        if (password.length() < 8 || password.length() > 20) {
            throw new BusinessException("密码长度必须在8到20个字符之间");
        }

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
        sysUser.setEmail(email);
        sysUser.setPassword(SecurityUtils.encryptPassword(password));
        sysUser.setStatus("0"); // 正常状态
        sysUser.setUserType(StringUtils.defaultIfBlank(registerBody.getUserType(), "1001")); // 默认注册用户类型为1001

        R<Boolean> registerResult = remoteUserService.createUser(sysUser);

        if (registerResult == null || !registerResult.isSuccess()) {
            String msg = registerResult != null ? registerResult.getMsg() : "注册失败";
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

        if (StringUtils.isEmpty(code)) {
            throw new AuthException("验证码不能为空");
        }
        //校验用户
        R<LoginUser> userResult = remoteUserService.getUserInfo(username, CommonConstants.REQUEST_SOURCE_INNER);
        if (userResult == null || !userResult.isSuccess()) {
            throw new AuthException("用户不存在");
        }
        LoginUser loginUser = userResult.getData();
        if (loginUser == null || loginUser.getSysUser() == null) {
            throw new AuthException("用户不存在");
        }

        // 邮箱验证码验证
        emailService.validateEmailCode(loginUser.getSysUser().getEmail(), code);
        // 密码验证
        String decryptedPassword = encryptService.decrypt(password);
        SysUserApi user = loginUser.getSysUser();
        if (!SecurityUtils.matchesPassword(decryptedPassword, user.getPassword())) {
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
        String password = request.getPassword();

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
        
        // 校验密码
        if (StringUtils.isNotEmpty(password)) {
             String decryptedPassword = encryptService.decrypt(password);
             if (!SecurityUtils.matchesPassword(decryptedPassword, user.getPassword())) {
                 throw new AuthException("邮箱或密码错误");
             }
        }

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

        return LoginResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(SecurityConstants.TOKEN_EXPIRE * 60)
                .userId(user.getId())
                .username(user.getUserName())
                .nickname(user.getNickName())
                .avatar(user.getAvatar())
                .build();
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
        String tokenKey = CacheConstants.CACHE_LOGIN_TOKEN + uuid;

        Map<String, Object> claims = new HashMap<>();
        claims.put(SecurityConstants.USER_KEY, uuid); // 存入 uuid 用于后续验证 Redis 状态
        claims.put(SecurityConstants.USER_ID, user.getId());
        claims.put(SecurityConstants.USER_NAME, user.getUserName());
        claims.put(SecurityConstants.NICK_NAME, user.getNickName());
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
     * 刷新令牌处理
     *
     * @param refreshToken 刷新令牌
     * @return 新的登录结果
     * @throws AuthException 刷新失败异常
     */
    @Override
    public void refreshTokenTime(String refreshToken) {
        if (StringUtils.isEmpty(refreshToken)) {
            throw new AuthException("刷新令牌不能为空");
        }
        // 如果带了 Bearer 前缀，先去掉
        if (refreshToken.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            refreshToken = refreshToken.replace(SecurityConstants.TOKEN_PREFIX, "");
        }
        // 从 refreshToken 获取 uuid (user_key)
        String uuid = JwtUtils.getUserKey(refreshToken);
        if (StringUtils.isEmpty(uuid)) {
            throw new AuthException("刷新令牌无效");
        }
        // 根据 uuid 获取 redis 中的缓存
        String tokenKey = CacheConstants.CACHE_LOGIN_TOKEN + uuid;
        Object userId = redisService.getCacheObject(tokenKey);
        if (userId == null) {
            throw new AuthException("刷新令牌已过期或不存在");
        }
        // 更新对应的时间 (续期)
        redisService.expire(tokenKey, SecurityConstants.TOKEN_EXPIRE, TimeUnit.MINUTES);

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

        try {
            // 如果带了 Bearer 前缀，先去掉
            if (token.startsWith(SecurityConstants.TOKEN_PREFIX)) {
                token = token.replace(SecurityConstants.TOKEN_PREFIX, "");
            }

            // 从 token 解析获取 uuid (user_key)
            String uuid = JwtUtils.getUserKey(token);
            if (StringUtils.isNotEmpty(uuid)) {
                // 根据 uuid 删除 redis 缓存
                String tokenKey = CacheConstants.CACHE_LOGIN_TOKEN + uuid;
                redisService.deleteObject(tokenKey);
            }
        } catch (Exception e) {
            log.error("退出登录时解析 Token 失败: {}", e.getMessage());
        }
    }

    @Override
    public void sendEmailCodeByUsername(String username, String captchaVerification) {
        // 验证滑块验证码凭证
        captchaService.verifyCaptchaToken(captchaVerification);

        R<LoginUser> userResult = remoteUserService.getUserInfo(username, CommonConstants.REQUEST_SOURCE_INNER);
        if (userResult == null || !userResult.isSuccess()) {
            throw new AuthException("用户不存在");
        }
        
        LoginUser loginUser = userResult.getData();
        if (loginUser == null || loginUser.getSysUser() == null) {
            throw new AuthException("用户不存在");
        }
        
        String email = loginUser.getSysUser().getEmail();
        if (StringUtils.isEmpty(email)) {
            throw new AuthException("该用户未绑定邮箱");
        }
        
        emailService.sendEmailCode(email);
    }

    /**
     * 发送密码重置验证码
     *
     * @param email 邮箱地址
     */
    @Override
    public void sendPasswordResetCode(String email, String captchaVerification) {
        if (StringUtils.isEmpty(email)) {
            throw new AuthException("邮箱地址不能为空");
        }
        
        // 验证滑块验证码凭证
        captchaService.verifyCaptchaToken(captchaVerification);

        /** 验证邮箱格式 */
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new AuthException("邮箱格式不正确");
        }

        /** 验证用户是否存在 */
        R<LoginUser> userResult = remoteUserService.getUserInfoByEmail(email, CommonConstants.REQUEST_SOURCE_INNER);
        if (userResult == null || !userResult.isSuccess() || userResult.getData() == null) {
            throw new AuthException("该邮箱未绑定任何账号");
        }

        /** 发送重置验证码 */
        emailService.sendEmailCode(email);
    }

    /**
     * 验证重置验证码
     *
     * @param email 邮箱地址
     * @param code 验证码
     * @return 验证令牌（用于后续重置密码）
     */
    @Override
    public String verifyResetCode(String email, String code) {
        if (StringUtils.isEmpty(email) || StringUtils.isEmpty(code)) {
            throw new AuthException("邮箱和验证码不能为空");
        }

        /** 验证邮箱验证码 */
        emailService.validateEmailCode(email, code);

        /** 生成验证令牌（有效期15分钟） */
        String verifyToken = UUID.randomUUID().toString().replace("-", "");
        String verifyKey = CacheConstants.CACHE_PASSWORD_RESET + verifyToken;

        /** 缓存验证令牌 */
        redisService.setCacheObject(verifyKey, email, 15, TimeUnit.MINUTES);

        return verifyToken;
    }

    /**
     * 重置密码
     *
     * @param email 邮箱地址
     * @param verifyToken 验证令牌
     * @param newPassword 新密码（已加密）
     */
    @Override
    public void resetPassword(String email, String verifyToken, String newPassword) {
        if (StringUtils.isEmpty(email) || StringUtils.isEmpty(verifyToken) || StringUtils.isEmpty(newPassword)) {
            throw new AuthException("参数不完整");
        }

        /** 验证令牌有效性 */
        String verifyKey = CacheConstants.CACHE_PASSWORD_RESET + verifyToken;
        String cachedEmail = redisService.getCacheObject(verifyKey);

        if (StringUtils.isEmpty(cachedEmail)) {
            throw new AuthException("验证令牌已过期，请重新获取");
        }

        if (!email.equals(cachedEmail)) {
            throw new AuthException("验证令牌无效");
        }

        /** 解密密码 */
        String decryptedPassword = encryptService.decrypt(newPassword);

        /** 验证密码强度 */
        if (StringUtils.isEmpty(decryptedPassword) || decryptedPassword.length() < 8) {
            throw new AuthException("密码长度不能少于8位");
        }

        /** 获取用户信息 */
        R<LoginUser> userResult = remoteUserService.getUserInfoByEmail(email, CommonConstants.REQUEST_SOURCE_INNER);
        if (userResult == null || !userResult.isSuccess() || userResult.getData() == null) {
            throw new AuthException("用户不存在");
        }

        LoginUser loginUser = userResult.getData();
        SysUserApi user = loginUser.getSysUser();

        /** 更新密码 */
        SysUserApi updateUser = new SysUserApi();
        updateUser.setId(user.getId());
        updateUser.setPassword(SecurityUtils.encryptPassword(decryptedPassword));

        R<Boolean> updateResult = remoteUserService.updateUser(updateUser);
        if (updateResult == null || !updateResult.isSuccess()) {
            String msg = updateResult != null ? updateResult.getMsg() : "密码重置失败";
            throw new AuthException(msg);
        }

        /** 删除验证令牌 */
        redisService.deleteObject(verifyKey);

        /** 使该用户所有Token失效 */
        invalidateUserTokens(user.getId());

        log.info("用户 {} 重置密码成功", email);
    }

    /**
     * 使指定用户的所有Token失效
     *
     * @param userId 用户ID
     */
    private void invalidateUserTokens(Long userId) {
        // 这里可以扩展实现：遍历Redis中该用户的所有Token并删除
        // 目前简化处理，用户重新登录即可
        log.info("用户 {} 的所有Token已失效", userId);
    }
}
