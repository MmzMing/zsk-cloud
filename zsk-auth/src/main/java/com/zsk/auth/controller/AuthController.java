package com.zsk.auth.controller;

import com.zsk.auth.domain.CaptchaCheckRequest;
import com.zsk.auth.domain.CaptchaResponse;
import com.zsk.auth.domain.LoginRequest;
import com.zsk.auth.domain.LoginResponse;
import com.zsk.auth.domain.PublicKeyResponse;
import com.zsk.auth.domain.RegisterBody;
import com.zsk.auth.service.IAuthService;
import com.zsk.auth.service.ICaptchaService;
import com.zsk.auth.service.IEmailService;
import com.zsk.auth.service.IEncryptService;
import com.zsk.auth.service.IThirdPartyAuthService;
import com.zsk.common.core.domain.R;
import com.zsk.common.sentinel.annotation.RateLimit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 用户注册
     *
     * @param registerBody 注册信息
     * @return 响应结果
     */
    @Operation(summary = "用户注册")
    @PostMapping("/register")
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
    @RateLimit(resource = "auth:login", count = 10, timeUnit = java.util.concurrent.TimeUnit.MINUTES)
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
    public R<Void> checkCaptcha(@RequestBody CaptchaCheckRequest request) {
        captchaService.validateCaptcha(request.getUuid(), request.getCode());
        return R.ok();
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
    @RateLimit(resource = "auth:email:code", count = 5, timeUnit = java.util.concurrent.TimeUnit.MINUTES)
    public R<Void> sendEmailCode(@RequestParam String email) {
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
     * 发送密码重置验证码
     *
     * @param email 邮箱地址
     * @return 响应结果
     */
    @Operation(summary = "发送密码重置验证码")
    @PostMapping("/password/reset/code")
    @RateLimit(resource = "auth:password:reset", count = 3, timeUnit = java.util.concurrent.TimeUnit.MINUTES)
    public R<Void> sendPasswordResetCode(@RequestParam String email) {
        authService.sendPasswordResetCode(email);
        return R.ok();
    }

    /**
     * 验证重置验证码
     *
     * @param email 邮箱地址
     * @param code 验证码
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
     * @param email 邮箱地址
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
}
