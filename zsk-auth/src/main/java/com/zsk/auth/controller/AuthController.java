package com.zsk.auth.controller;

import com.zsk.auth.domain.*;
import com.zsk.auth.service.*;
import com.zsk.common.core.domain.R;
import com.zsk.common.sentinel.annotation.RateLimit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

/**
 * 认证管理 控制器
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-14
 */
@Slf4j
@Tag(name = "认证管理")
@RestController
@RequestMapping
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;
    private final ICaptchaService captchaService;
    private final IEmailService emailService;
    private final IThirdPartyAuthService thirdPartyAuthService;
    private final IEncryptService encryptService;

    @Value("${magic-link.redirect-url}")
    private String magicLinkRedirectUrl;

    @Value("${third-party.redirect-url:http://localhost:3000}")
    private String thirdPartyRedirectUrl;

    /**
     * 用户注册
     *
     * @param registerBody 注册信息
     * @return 响应结果
     */
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    @RateLimit(resource = "auth:register", key = "#registerBody.email", count = 10, timeUnit = TimeUnit.MINUTES)
    public R<Void> register(@RequestBody @Valid RegisterBody registerBody) {
        authService.register(registerBody);
        return R.ok();
    }

    /**
     * 用户登录
     *
     * @param request 登录参数
     * @return 登录结果
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    @RateLimit(resource = "auth:login", key = "#request.username", count = 10, timeUnit = TimeUnit.MINUTES)
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return R.ok(response);
    }

    /**
     * 刷新令牌
     *
     * @param refreshToken 刷新令牌
     * @return 登录结果
     */
    @Operation(summary = "刷新令牌")
    @PostMapping("/refresh")
    public R<Void> refreshTokenTime(@RequestHeader("Authorization") String refreshToken) {
        authService.refreshTokenTime(refreshToken);
        return R.ok();
    }

    /**
     * 退出登录
     *
     * @param token 访问令牌
     * @return 响应结果
     */
    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public R<Void> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token);
        return R.ok();
    }

    /**
     * 获取滑块拼图验证码
     *
     * @return 验证码响应对象（包含背景图、拼图、UUID）
     */
    @Operation(summary = "获取滑块拼图验证码")
    @GetMapping("/captcha")
    public R<CaptchaResponse> generateCaptcha() {
        return R.ok(captchaService.generateSlideCaptcha());
    }

    /**
     * 校验滑块验证码
     *
     * @param request 校验参数
     * @return 校验结果
     */
    @Operation(summary = "校验滑块验证码")
    @PostMapping("/captcha/check")
    public R<String> checkCaptcha(@RequestBody CaptchaCheckRequest request) {
        return R.ok(captchaService.validateCaptcha(request.getUuid(), request.getCode()));
    }

    /**
     * 获取RSA公钥
     *
     * @return 公钥响应对象（包含公钥、有效期、版本号）
     */
    @Operation(summary = "获取RSA公钥")
    @GetMapping("/public-key")
    public R<PublicKeyResponse> getPublicKey() {
        return R.ok(encryptService.getPublicKey());
    }

    /**
     * 发送邮箱验证码
     *
     * @param email 邮箱地址
     * @return 响应结果
     */
    @Operation(summary = "发送邮箱验证码")
    @PostMapping("/email/code")
    @RateLimit(resource = "auth:email:code", key = "#email", count = 5, timeUnit = TimeUnit.MINUTES)
    public R<Void> sendEmailCode(@RequestParam String email, @RequestParam String captchaVerification) {
        // 验证滑块验证码凭证
        captchaService.verifyCaptchaToken(captchaVerification);
        emailService.sendEmailCode(email);
        return R.ok();
    }

    /**
     * 获取第三方登录授权URL
     *
     * @param loginType 登录类型
     * @return 授权URL
     */
    @Operation(summary = "获取第三方登录授权URL")
    @GetMapping("/third-party/url")
    public R<String> getAuthUrl(@RequestParam String loginType) {
        String authUrl = thirdPartyAuthService.getAuthUrl(loginType);
        return R.ok(authUrl);
    }

    /**
     * 第三方登录回调
     *
     * @param loginType 登录类型
     * @param code      授权码
     * @param state     状态码
     * @return 登录结果
     */
    @Operation(summary = "第三方登录回调")
    @PostMapping("/third-party/callback")
    public R<LoginResponse> thirdPartyCallback(@RequestParam String loginType,
                                               @RequestParam String code,
                                               @RequestParam String state) {
        LoginRequest request = new LoginRequest();
        request.setLoginType(loginType);
        request.setAuthCode(code);
        request.setState(state);

        LoginResponse response = authService.login(request);
        return R.ok(response);
    }

    /**
     * GitHub 登录回调
     *
     * @param code  授权码
     * @param state 状态码
     * @return 重定向到前端
     */
    @Operation(summary = "GitHub 登录回调")
    @GetMapping("/github/callback")
    public ResponseEntity<Void> githubCallback(@RequestParam String code,
                                               @RequestParam String state,
                                               HttpServletResponse response) {
        return handleThirdPartyCallback(code, state, "github", response);
    }

    /**
     * 微信登录回调
     *
     * @param code  授权码
     * @param state 状态码
     * @return 重定向到前端
     */
    @Operation(summary = "微信登录回调")
    @GetMapping("/wechat/callback")
    public ResponseEntity<Void> wechatCallback(@RequestParam String code,
                                               @RequestParam String state,
                                               HttpServletResponse response) {
        return handleThirdPartyCallback(code, state, "wechat", response);
    }

    /**
     * QQ 登录回调
     *
     * @param code  授权码
     * @param state 状态码
     * @return 重定向到前端
     */
    @Operation(summary = "QQ 登录回调")
    @GetMapping("/qq/callback")
    public ResponseEntity<Void> qqCallback(@RequestParam String code,
                                           @RequestParam String state,
                                           HttpServletResponse response) {
        return handleThirdPartyCallback(code, state, "qq", response);
    }

    /**
     * 处理第三方登录回调通用逻辑
     *
     * @param code      授权码
     * @param state     状态码
     * @param loginType 登录类型
     * @param response  HTTP响应对象
     * @return 重定向响应
     */
    private ResponseEntity<Void> handleThirdPartyCallback(String code, String state, String loginType, HttpServletResponse response) {
        try {
            LoginRequest request = new LoginRequest();
            request.setLoginType(loginType);
            request.setAuthCode(code);
            request.setState(state);

            LoginResponse loginResponse = authService.login(request);

            Cookie cookie = new Cookie("access_token", loginResponse.getAccessToken());
            cookie.setHttpOnly(false);
            cookie.setSecure(true);
            cookie.setPath("/");
            cookie.setMaxAge(loginResponse.getExpiresIn().intValue());
            response.addCookie(cookie);

            return ResponseEntity.status(HttpStatus.FOUND).header("Location", thirdPartyRedirectUrl).build();
        } catch (Exception e) {
            log.warn("第三方登录回调失败: loginType={}, error={}", loginType, e.getMessage());
            String errorUrl = thirdPartyRedirectUrl + "/login?error=third_party_auth_failed";
            return ResponseEntity.status(HttpStatus.FOUND).header("Location", errorUrl).build();
        }
    }

    /**
     * 根据用户名发送邮箱验证码
     *
     * @param username 用户名
     * @return 响应结果
     */
    @Operation(summary = "根据用户名发送邮箱验证码")
    @PostMapping("/email/code/username")
    @RateLimit(resource = "auth:email:code:username", key = "#username", count = 5, timeUnit = TimeUnit.MINUTES)
    public R<Void> sendEmailCodeByUsername(@RequestParam String username, @RequestParam String captchaVerification) {
        authService.sendEmailCodeByUsername(username, captchaVerification);
        return R.ok();
    }

    /**
     * 发送密码重置验证码
     *
     * @param email               邮箱地址
     * @param captchaVerification 验证码验证凭证
     * @return 响应结果
     */
    @Operation(summary = "发送密码重置验证码")
    @PostMapping("/password/reset/code")
    @RateLimit(resource = "auth:password:reset", key = "#email", count = 3, timeUnit = java.util.concurrent.TimeUnit.MINUTES)
    public R<Void> sendPasswordResetCode(@RequestParam String email, @RequestParam String captchaVerification) {
        authService.sendPasswordResetCode(email, captchaVerification);
        return R.ok();
    }

    /**
     * 验证重置验证码
     *
     * @param email 邮箱地址
     * @param code  验证码
     * @return 验证令牌（用于后续重置密码）
     */
    @Operation(summary = "验证重置验证码")
    @PostMapping("/password/reset/verify")
    public R<String> verifyResetCode(@RequestParam String email, @RequestParam String code) {
        String verifyToken = authService.verifyResetCode(email, code);
        return R.ok(verifyToken);
    }

    /**
     * 重置密码
     *
     * @param email       邮箱地址
     * @param verifyToken 验证令牌
     * @param newPassword 新密码（RSA加密后）
     * @return 响应结果
     */
    @Operation(summary = "重置密码")
    @PostMapping("/password/reset")
    public R<Void> resetPassword(
            @RequestParam String email,
            @RequestParam String verifyToken,
            @RequestParam String newPassword) {
        authService.resetPassword(email, verifyToken, newPassword);
        return R.ok();
    }


//    /**
//     * 获取Turnstile站点密钥
//     * <p>前端调用此接口获取Cloudflare Turnstile的站点密钥，用于初始化人机校验组件
//     *
//     * @param siteKey Turnstile站点密钥，从配置文件注入
//     * @return 站点密钥
//     */
//     @Operation(summary = "获取Turnstile站点密钥")
//     @GetMapping("/captcha/turnstile/site-key")
//     public R<String> getTurnstileSiteKey(@Value("${turnstile.site-key:}") String siteKey) {
//         return R.ok(siteKey);
//     }

    /**
     * 发送魔法链接
     * <p>用户输入邮箱并完成人机校验后调用此接口发送魔法链接邮件
     * 接口有频率限制：同一邮箱3分钟内最多调用3次
     *
     * @param request 魔法链接请求，包含邮箱地址和人机校验凭证
     * @return 响应结果，提示魔法链接已发送
     */
    @Operation(summary = "发送魔法链接")
    @PostMapping("/magic-link/send")
    @RateLimit(resource = "auth:magic-link:send", key = "#request.email", count = 3, timeUnit = TimeUnit.MINUTES)
    public R<String> sendMagicLink(@Valid @RequestBody MagicLinkRequest request) {
        authService.sendMagicLink(request.getEmail(), request.getTurnstileToken());
        return R.ok("魔法链接已发送至您的邮箱，15分钟内有效");
    }

    /**
     * 魔法链接回调
     * <p>用户点击邮件中的魔法链接后，浏览器会跳转到此接口
     * 验证Token有效性后，将登录令牌写入HttpOnly Cookie并重定向到首页
     *
     * @param token    魔法链接中的Token
     * @param response HTTP响应对象，用于设置Cookie
     * @return 重定向响应，成功跳转到首页，失败跳转到登录页
     */
    @Operation(summary = "魔法链接回调")
    @GetMapping("/magic-link/callback")
    public ResponseEntity<Void> magicLinkCallback(@RequestParam String token, HttpServletResponse response) {
        try {
            LoginResponse loginResponse = authService.verifyMagicLink(token);

            Cookie cookie = new Cookie("access_token", loginResponse.getAccessToken());
            cookie.setHttpOnly(false);
            cookie.setSecure(true);
            cookie.setPath("/");
            cookie.setMaxAge(7200);
            response.addCookie(cookie);

            return ResponseEntity.status(HttpStatus.FOUND).header("Location", magicLinkRedirectUrl).build();
        } catch (Exception e) {
            log.warn("魔法链接验证失败: {}", e.getMessage());
            String redirectUrl = magicLinkRedirectUrl + "/login?error=invalid_token";
            return ResponseEntity.status(HttpStatus.FOUND).header("Location", redirectUrl).build();
        }
    }
}
