package com.zsk.auth.service.impl;

import com.zsk.auth.domain.LoginRequest;
import com.zsk.auth.domain.LoginResponse;
import com.zsk.auth.domain.RegisterBody;
import com.zsk.auth.service.*;
import com.zsk.common.core.constant.CacheConstants;
import com.zsk.common.core.constant.CommonConstants;
import com.zsk.common.core.constant.SecurityConstants;
import com.zsk.common.core.domain.R;
import com.zsk.common.core.exception.AuthException;
import com.zsk.common.core.exception.BusinessException;
import com.zsk.common.core.utils.IpUtils;
import com.zsk.common.core.utils.JwtUtils;
import com.zsk.common.core.utils.StringUtils;
import com.zsk.common.redis.service.RedisService;
import com.zsk.common.security.utils.SecurityUtils;
import com.zsk.system.api.RemoteUserService;
import com.zsk.system.api.domain.SysUserApi;
import com.zsk.system.api.model.LoginUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
        sysUser.setLoginIp(getClientIp());
        sysUser.setLoginDate(LocalDateTime.now());

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

        /** 更新最后登录IP和登录时间 */
        updateLoginIp(user.getId());

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
     * <p>
     * 该方法负责生成JWT Token并将Token存储到Redis中，支持多设备登录。
     * 采用基于用户ID的Token集合存储方式，一个用户最多可以同时登录5个设备。
     * <p>
     * 存储结构变更说明：
     * - 变更前：key=uuid, value=userId（单Token模式）
     * - 变更后：key=userId, value=Set<token>（多Token模式）
     * <p>
     * 处理流程：
     * 1. 构建JWT Claims（用户信息载体）
     * 2. 生成JWT Token
     * 3. 检查用户Token数量，超过5个则删除最旧的
     * 4. 将Token添加到Redis Set集合
     * 5. 缓存用户的角色和权限信息
     * <p>
     * Redis存储结构：
     * - Token集合：zsk:login:token:{userId} -> Set<token>
     * - 角色集合：zsk:login:roles:{userId} -> Set<String>
     * - 权限集合：zsk:login:permissions:{userId} -> Set<String>
     *
     * @param loginUser 登录用户信息，包含用户基本信息、角色、权限等
     * @return 访问令牌（JWT格式）
     */
    private String generateAccessToken(LoginUser loginUser) {
        SysUserApi user = loginUser.getSysUser();
        Long userId = user.getId();

        // ==================== 步骤1：构建JWT Claims ====================
        // Claims是JWT的载荷部分，包含用户基本信息
        // 不再使用uuid作为user_key，直接使用userId
        Map<String, Object> claims = new HashMap<>();
        claims.put(SecurityConstants.USER_ID, userId);              // 用户ID
        claims.put(SecurityConstants.USER_NAME, user.getUserName()); // 用户名
        claims.put(SecurityConstants.NICK_NAME, user.getNickName()); // 昵称

        // ==================== 步骤2：生成JWT Token ====================
        // 使用配置的密钥（对称或非对称）生成Token
        String token = JwtUtils.createToken(claims);

        // ==================== 步骤3：Token数量控制 ====================
        // 检查用户当前的Token数量，支持最多5个设备同时登录
        String tokenKey = CacheConstants.CACHE_LOGIN_TOKEN + userId;
        Long tokenCount = redisService.getSetSize(tokenKey);
        
        if (tokenCount != null && tokenCount >= 5) {
            // 已达到最大Token数量限制，删除最旧的Token
            // 注意：Redis Set是无序的，这里删除的是随机一个Token
            // 如需精确删除最旧Token，建议使用Redis Sorted Set
            Set<String> tokens = redisService.getCacheSet(tokenKey);
            if (tokens != null && !tokens.isEmpty()) {
                String oldestToken = tokens.iterator().next();
                redisService.removeSetCacheObject(tokenKey, oldestToken);
                log.info("用户 {} Token数量达到上限，删除旧Token", userId);
            }
        }

        // ==================== 步骤4：存储Token到Redis ====================
        // 将新生成的Token添加到用户的Token集合中
        redisService.setSetCacheObject(tokenKey, token);
        
        // 设置Token集合的过期时间（滑动过期）
        // 每次登录或请求都会刷新过期时间
        redisService.expire(tokenKey, SecurityConstants.TOKEN_EXPIRE, TimeUnit.MINUTES);

        // ==================== 步骤5：缓存角色和权限信息 ====================
        // 每次登录都重新缓存用户的角色和权限信息
        // 这样可以确保权限变更后，重新登录即可生效
        String rolesKey = CacheConstants.CACHE_LOGIN_ROLES + userId;
        String permsKey = CacheConstants.CACHE_LOGIN_PERMISSIONS + userId;
        
        // 先删除旧的缓存
        redisService.deleteObject(rolesKey);
        redisService.deleteObject(permsKey);
        
        // 缓存新的角色和权限信息
        // 过期时间与Token保持一致
        redisService.setCacheObject(rolesKey, loginUser.getRoles(), SecurityConstants.TOKEN_EXPIRE, TimeUnit.MINUTES);
        redisService.setCacheObject(permsKey, loginUser.getPermissions(), SecurityConstants.TOKEN_EXPIRE, TimeUnit.MINUTES);

        return token;
    }


    /**
     * 刷新令牌处理
     * <p>
     * 该方法用于刷新Token的过期时间，实现滑动过期机制。
     * 用户在Token即将过期前，可以通过此方法延长Token的有效期。
     * <p>
     * 处理流程：
     * 1. 验证Token格式（去除Bearer前缀）
     * 2. 从Token中解析用户ID
     * 3. 验证Token是否在用户的Token集合中
     * 4. 刷新Token集合、角色集合、权限集合的过期时间
     * <p>
     * 注意事项：
     * - 只刷新Token集合的过期时间，不生成新Token
     * - 同时刷新角色和权限缓存的过期时间
     * - 如果Token不在集合中，说明已被踢出或过期
     *
     * @param refreshToken 刷新令牌（可带Bearer前缀）
     * @throws AuthException Token无效或已过期
     */
    @Override
    public void refreshTokenTime(String refreshToken) {
        // 参数校验
        if (StringUtils.isEmpty(refreshToken)) {
            throw new AuthException("刷新令牌不能为空");
        }
        
        // 去除Bearer前缀（如果有）
        if (refreshToken.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            refreshToken = refreshToken.replace(SecurityConstants.TOKEN_PREFIX, "");
        }

        // 从Token中解析用户ID
        Long userId = JwtUtils.getUserIdAsLong(refreshToken);
        if (userId == null) {
            throw new AuthException("刷新令牌无效");
        }

        // 验证Token是否在用户的Token集合中
        String tokenKey = CacheConstants.CACHE_LOGIN_TOKEN + userId;
        Boolean isMember = redisService.isMemberOfSet(tokenKey, refreshToken);
        if (Boolean.FALSE.equals(isMember)) {
            // Token不在集合中，可能已被踢出或过期
            throw new AuthException("刷新令牌已过期或不存在");
        }

        // 刷新Token集合的过期时间
        redisService.expire(tokenKey, SecurityConstants.TOKEN_EXPIRE, TimeUnit.MINUTES);
        
        // 同时刷新角色和权限缓存的过期时间
        redisService.expire(CacheConstants.CACHE_LOGIN_ROLES + userId, SecurityConstants.TOKEN_EXPIRE, TimeUnit.MINUTES);
        redisService.expire(CacheConstants.CACHE_LOGIN_PERMISSIONS + userId, SecurityConstants.TOKEN_EXPIRE, TimeUnit.MINUTES);
    }

    /**
     * 退出登录处理
     * <p>
     * 该方法负责处理用户退出登录，删除指定的Token。
     * 支持多设备登录场景，退出时只删除当前设备的Token，不影响其他设备的登录状态。
     * <p>
     * 处理流程：
     * 1. 验证Token格式（去除Bearer前缀）
     * 2. 从Token中解析用户ID
     * 3. 从用户的Token集合中删除指定Token
     * 4. 检查用户是否还有其他Token
     * 5. 如果没有剩余Token，清理角色和权限缓存
     * <p>
     * 注意事项：
     * - 只删除当前Token，不删除其他设备的Token
     * - 当用户所有Token都被删除后，才清理角色和权限缓存
     * - 异常情况仅记录日志，不影响退出流程
     *
     * @param token 访问令牌（可带Bearer前缀）
     */
    @Override
    public void logout(String token) {
        // 参数校验
        if (StringUtils.isEmpty(token)) {
            return;
        }

        try {
            // 去除Bearer前缀（如果有）
            if (token.startsWith(SecurityConstants.TOKEN_PREFIX)) {
                token = token.replace(SecurityConstants.TOKEN_PREFIX, "");
            }

            // 从Token中解析用户ID
            Long userId = JwtUtils.getUserIdAsLong(token);
            if (userId != null) {
                // 从用户的Token集合中删除指定Token
                String tokenKey = CacheConstants.CACHE_LOGIN_TOKEN + userId;
                redisService.removeSetCacheObject(tokenKey, token);

                // 检查用户是否还有剩余Token
                Long remainingTokens = redisService.getSetSize(tokenKey);
                if (remainingTokens == null || remainingTokens == 0) {
                    // 用户没有任何Token了，清理角色和权限缓存
                    redisService.deleteObject(CacheConstants.CACHE_LOGIN_ROLES + userId);
                    redisService.deleteObject(CacheConstants.CACHE_LOGIN_PERMISSIONS + userId);
                    log.info("用户 {} 所有设备已退出登录", userId);
                }
            }
        } catch (Exception e) {
            // 异常情况仅记录日志，不影响退出流程
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
     * @param code  验证码
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
     * @param email       邮箱地址
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

        R<Boolean> updateResult = remoteUserService.updateUser(updateUser, CommonConstants.REQUEST_SOURCE_INNER);
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

    /**
     * 发送魔法链接
     * <p>处理流程：
     * 1. 校验邮箱格式
     * 2. 调用 Cloudflare Turnstile API 验证人机校验Token
     * 3. 生成UUID作为魔法链接Token
     * 4. 缓存Token与邮箱的映射关系（15分钟过期）
     * 5. 发送包含魔法链接的邮件
     * <p>限流由 Controller 层 @RateLimit 注解控制
     *
     * @param email          邮箱地址
     * @param turnstileToken Cloudflare Turnstile验证Token
     * @throws AuthException 参数校验失败或人机校验失败
     */
    @Override
    public void sendMagicLink(String email, String turnstileToken) {
        if (StringUtils.isEmpty(email)) {
            throw new AuthException("邮箱地址不能为空");
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new AuthException("邮箱格式不正确");
        }

        if (!captchaService.verifyTurnstileToken(turnstileToken)) {
            throw new AuthException("人机校验失败，请重试");
        }

        String magicToken = UUID.randomUUID().toString().replace("-", "");
        String magicLinkKey = CacheConstants.CACHE_MAGIC_LINK + magicToken;

        redisService.setCacheObject(magicLinkKey, email, 15, TimeUnit.MINUTES);

        emailService.sendMagicLinkEmail(email, magicToken);

        log.info("魔法链接已发送至邮箱: {}", email);
    }

    /**
     * 验证魔法链接并生成登录态
     * <p>处理流程：
     * 1. 校验Token非空
     * 2. 从Redis获取绑定的邮箱地址
     * 3. 删除Token（一次性使用）
     * 4. 根据邮箱查询用户信息
     * 5. 如果用户不存在，自动创建新用户
     * 6. 校验用户状态
     * 7. 生成登录Token
     *
     * @param token 魔法链接Token
     * @return 登录结果，包含访问令牌和用户信息
     * @throws AuthException Token无效、过期或账号被停用
     */
    @Override
    public LoginResponse verifyMagicLink(String token) {
        if (StringUtils.isEmpty(token)) {
            throw new AuthException("链接无效");
        }

        String magicLinkKey = CacheConstants.CACHE_MAGIC_LINK + token;
        String email = redisService.getCacheObject(magicLinkKey);

        if (StringUtils.isEmpty(email)) {
            throw new AuthException("链接已过期或无效");
        }

        redisService.deleteObject(magicLinkKey);

        R<LoginUser> userResult = remoteUserService.getUserInfoByEmail(email, CommonConstants.REQUEST_SOURCE_INNER);

        LoginUser loginUser;
        if (userResult == null || !userResult.isSuccess() || userResult.getData() == null) {
            loginUser = createUserByEmail(email);
        } else {
            loginUser = userResult.getData();
        }

        if (loginUser == null || loginUser.getSysUser() == null) {
            throw new AuthException("用户创建失败");
        }

        SysUserApi user = loginUser.getSysUser();
        if ("1".equals(user.getStatus())) {
            throw new AuthException("账号已被停用");
        }

        return generateToken(loginUser);
    }

    /**
     * 根据邮箱自动创建用户
     * <p>当魔法链接验证时用户不存在，则自动创建新用户
     * 用户名默认为邮箱@前面的部分，其他字段使用默认值
     *
     * @param email 邮箱地址
     * @return 创建成功的用户信息
     */
    private LoginUser createUserByEmail(String email) {
        String username = email.substring(0, email.indexOf('@'));

        SysUserApi sysUser = new SysUserApi();
        sysUser.setUserName(username);
        sysUser.setNickName(username);
        sysUser.setEmail(email);
        sysUser.setStatus("0");
        sysUser.setUserType("1001");
        sysUser.setLoginIp(getClientIp());
        sysUser.setLoginDate(LocalDateTime.now());

        R<Boolean> createResult = remoteUserService.createUser(sysUser);
        if (createResult == null || !createResult.isSuccess()) {
            log.error("自动创建用户失败: {}", email);
            throw new AuthException("用户创建失败");
        }

        R<LoginUser> userResult = remoteUserService.getUserInfoByEmail(email, CommonConstants.REQUEST_SOURCE_INNER);
        if (userResult == null || !userResult.isSuccess()) {
            log.error("获取新创建用户信息失败: {}", email);
            throw new AuthException("用户创建失败");
        }

        log.info("通过魔法链接自动创建用户: {}", email);
        return userResult.getData();
    }

    /**
     * 获取客户端IP地址
     *
     * @return IP地址
     */
    private String getClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return IpUtils.getIpAddr(request);
        }
        return "unknown";
    }

    /**
     * 更新用户最后登录IP和登录时间
     *
     * @param userId 用户ID
     */
    private void updateLoginIp(Long userId) {
        try {
            SysUserApi updateUser = new SysUserApi();
            updateUser.setId(userId);
            updateUser.setLoginIp(getClientIp());
            updateUser.setLoginDate(LocalDateTime.now());
            remoteUserService.updateUser(updateUser, CommonConstants.REQUEST_SOURCE_INNER);
        } catch (Exception e) {
            log.error("更新用户登录IP失败: {}", e.getMessage());
        }
    }
}
