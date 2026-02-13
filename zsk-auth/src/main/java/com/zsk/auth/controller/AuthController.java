package com.zsk.auth.controller;

import com.zsk.auth.domain.CaptchaResponse;
import com.zsk.auth.domain.LoginRequest;
import com.zsk.auth.domain.LoginResponse;
import com.zsk.auth.domain.RegisterBody;
import com.zsk.auth.service.IAuthService;
import com.zsk.auth.service.ICaptchaService;
import com.zsk.auth.service.IEmailService;
import com.zsk.auth.service.IThirdPartyAuthService;
import com.zsk.common.core.domain.R;
import com.zsk.common.sentinel.annotation.RateLimit;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证管理 控制器
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2024-01-15
 */
@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;
    private final ICaptchaService captchaService;
    private final IEmailService emailService;
    private final IThirdPartyAuthService thirdPartyAuthService;

    /**
     * 用户注册
     *
     * @param registerBody 注册信息
     * @return 响应结果
     */
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
    @PostMapping("/refresh")
    public R<LoginResponse> refreshToken(@RequestParam String refreshToken) {
        LoginResponse response = authService.refreshToken(refreshToken);
        return R.ok(response);
    }

    /**
     * 退出登录
     *
     * @param token 访问令牌
     * @return 响应结果
     */
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
    @GetMapping("/captcha")
    public R<CaptchaResponse> generateCaptcha() {
        return R.ok(captchaService.generateSlideCaptcha());
    }

    /**
     * 发送邮箱验证码
     *
     * @param email 邮箱地址
     * @return 响应结果
     */
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
}
