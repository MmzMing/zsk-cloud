package com.zsk.auth.service.impl;

import com.zsk.auth.service.IEmailService;
import com.zsk.common.core.constant.CacheConstants;
import com.zsk.common.core.exception.AuthException;
import com.zsk.common.core.utils.StringUtils;
import com.zsk.common.redis.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 邮箱服务实现
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements IEmailService {

    private final RedisService redisService;

    /**
     * 邮件服务器主机名
     */
    @Value("${email.host:smtp.qq.com}")
    private String emailHost;

    /**
     * 邮件服务器端口
     */
    @Value("${email.port:465}")
    private Integer emailPort;

    /**
     * 邮件服务器认证账号
     */
    @Value("${email.username}")
    private String emailUsername;

    /**
     * 邮件服务器认证密码
     */
    @Value("${email.password}")
    private String emailPassword;

    /**
     * 发件人地址
     */
    @Value("${email.from}")
    private String emailFrom;

    /**
     * 邮件主题
     */
    @Value("${email.subject:验证码}")
    private String emailSubject;

    /**
     * 验证码过期时间（秒）
     */
    @Value("${email.code.expire:300}")
    private Long emailCodeExpire;

    /**
     * 魔法链接主题
     */
    @Value("${email.magic-link.subject:登录链接}")
    private String magicLinkSubject;

    /**
     * 魔法链接地址模板
     */
    @Value("${email.magic-link.url:http://localhost:8080/api/auth/magic-link/callback?token=}")
    private String magicLinkUrl;

    /**
     * 发送邮箱验证码
     *
     * @param email 目标邮箱地址
     * @throws AuthException 发送失败或参数无效时抛出
     */
    @Override
    public void sendEmailCode(String email) {
        if (StringUtils.isEmpty(email)) {
            throw new AuthException("邮箱地址不能为空");
        }

        String code = generateEmailCode();
        String emailKey = CacheConstants.CACHE_EMAIL_CODE + email;

        redisService.setCacheObject(emailKey, code, emailCodeExpire, TimeUnit.SECONDS);

        try {
            HtmlEmail htmlEmail = new HtmlEmail();
            htmlEmail.setHostName(emailHost);
            htmlEmail.setSmtpPort(emailPort);
            htmlEmail.setSSLOnConnect(true);
            htmlEmail.setAuthentication(emailUsername, emailPassword);
            htmlEmail.setFrom(emailFrom);
            htmlEmail.setSubject(emailSubject);
            htmlEmail.setCharset("UTF-8");
            htmlEmail.setHtmlMsg(getHtmlTemplate(code));
            htmlEmail.addTo(email);
            htmlEmail.send();

            log.info("邮箱验证码发送成功: {}", email);
        } catch (EmailException e) {
            log.error("邮箱验证码发送失败: {}", email, e);
            throw new AuthException("邮箱验证码发送失败");
        }
    }

    /**
     * 校验邮箱验证码
     *
     * @param email 邮箱地址
     * @param code  用户输入的验证码内容
     * @throws AuthException 验证码无效或错误时抛出
     */
    @Override
    public void validateEmailCode(String email, String code) {
        if (StringUtils.isEmpty(email) || StringUtils.isEmpty(code)) {
            throw new AuthException("邮箱和验证码不能为空");
        }

        String emailKey = CacheConstants.CACHE_EMAIL_CODE + email;
        String cachedCode = redisService.getCacheObject(emailKey);

        if (StringUtils.isEmpty(cachedCode)) {
            throw new AuthException("验证码已过期");
        }

        if (!code.equals(cachedCode)) {
            throw new AuthException("验证码错误");
        }

        redisService.deleteObject(emailKey);
    }

    /**
     * 生成6位数字验证码
     *
     * @return 6位数字字符串
     */
    private String generateEmailCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    /**
     * 获取HTML邮件模板
     *
     * @param code 验证码
     * @return HTML内容
     */
    private String getHtmlTemplate(String code) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>验证码</title>
                <style>
                    body { font-family: "Helvetica Neue", Helvetica, Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }
                    .container { max-width: 600px; margin: 40px auto; background-color: #ffffff; padding: 40px; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.05); }
                    .header { text-align: center; padding-bottom: 30px; border-bottom: 1px solid #eeeeee; margin-bottom: 30px; }
                    .header h2 { color: #333333; margin: 0; font-size: 24px; font-weight: 500; }
                    .content { text-align: center; color: #555555; }
                    .message { font-size: 16px; line-height: 1.6; margin-bottom: 25px; }
                    .code-box { background-color: #f8f9fa; border-radius: 6px; padding: 20px; margin: 0 auto 25px; display: inline-block; min-width: 200px; border: 1px solid #e9ecef; }
                    .code { font-size: 36px; font-weight: bold; color: #0056b3; letter-spacing: 8px; font-family: monospace; margin: 0; }
                    .footer { text-align: center; padding-top: 30px; border-top: 1px solid #eeeeee; color: #999999; font-size: 13px; margin-top: 30px; }
                    .footer p { margin: 5px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h2>安全验证</h2>
                    </div>
                    <div class="content">
                        <p class="message">您好！您正在进行身份验证，您的验证码为：</p>
                        <div class="code-box">
                            <p class="code">%s</p>
                        </div>
                        <p class="message">该验证码5分钟内有效，为了您的账号安全，请勿泄露给他人。</p>
                    </div>
                    <div class="footer">
                        <p>此邮件由系统自动发送，请勿回复。</p>
                        <p>&copy; 2026 ZSK Cloud. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """, code);
    }

    /**
     * 发送魔法链接邮件
     * <p>使用 Apache Commons Email 发送包含魔法登录链接的HTML邮件
     * 邮件包含可点击的登录按钮和备用链接地址
     *
     * @param email 目标邮箱地址
     * @param token 魔法链接Token，用于后续验证
     * @throws AuthException 邮箱地址为空或发送失败时抛出
     */
    @Override
    public void sendMagicLinkEmail(String email, String token) {
        if (StringUtils.isEmpty(email)) {
            throw new AuthException("邮箱地址不能为空");
        }

        String magicLink = magicLinkUrl + token;

        try {
            HtmlEmail htmlEmail = new HtmlEmail();
            htmlEmail.setHostName(emailHost);
            htmlEmail.setSmtpPort(emailPort);
            htmlEmail.setSSLOnConnect(true);
            htmlEmail.setAuthentication(emailUsername, emailPassword);
            htmlEmail.setFrom(emailFrom);
            htmlEmail.setSubject(magicLinkSubject);
            htmlEmail.setCharset("UTF-8");
            htmlEmail.setHtmlMsg(getMagicLinkTemplate(magicLink));
            htmlEmail.addTo(email);
            htmlEmail.send();

            log.info("魔法链接发送成功: {}", email);
        } catch (EmailException e) {
            log.error("魔法链接发送失败: {}", email, e);
            throw new AuthException("魔法链接发送失败");
        }
    }

    /**
     * 获取魔法链接邮件HTML模板
     * <p>生成包含登录按钮和备用链接的精美HTML邮件内容
     *
     * @param magicLink 完整的魔法登录链接
     * @return HTML格式的邮件内容
     */
    private String getMagicLinkTemplate(String magicLink) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>登录链接</title>
                <style>
                    body { font-family: "Helvetica Neue", Helvetica, Arial, sans-serif; background-color: #ffffff; margin: 0; padding: 0; }
                    .container { max-width: 600px; margin: 40px auto; background-color: #ffffff; padding: 40px; border-radius: 0; }
                    .header { text-align: center; padding-bottom: 24px; border-bottom: 2px solid #000000; margin-bottom: 30px; }
                    .header h2 { color: #000000; margin: 0; font-size: 24px; font-weight: 700; letter-spacing: 2px; }
                    .content { text-align: center; color: #333333; }
                    .message { font-size: 15px; line-height: 1.8; margin-bottom: 24px; }
                    .btn-box { margin: 30px 0; }
                    .btn { display: inline-block; padding: 14px 48px; background-color: #000000; color: #ffffff; text-decoration: none; border-radius: 0; font-size: 15px; font-weight: 600; letter-spacing: 1px; }
                    .btn:hover { background-color: #333333; }
                    .link-section { margin: 24px 0; }
                    .link-label { font-size: 14px; color: #666666; margin-bottom: 10px; }
                    .link { color: #000000; word-break: break-all; font-size: 13px; font-family: monospace; background-color: #f5f5f5; padding: 10px 14px; border: 1px solid #e0e0e0; text-align: left; }
                    .footer { text-align: center; padding-top: 24px; border-top: 2px solid #000000; color: #666666; font-size: 12px; margin-top: 30px; }
                    .footer p { margin: 4px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h2>安全登录</h2>
                    </div>
                    <div class="content">
                        <p class="message">您好！您正在尝试登录 ZSK Cloud，点击下方按钮即可完成登录：</p>
                        <div class="btn-box">
                            <a href="%s" class="btn">立即登录</a>
                        </div>
                        <div class="link-section">
                            <p class="link-label">如果按钮无法点击，请手动复制以下链接到浏览器地址栏：</p>
                            <span class="link">%s</span>
                        </div>
                        <p class="message">该链接15分钟内有效，为了您的账号安全，请勿泄露给他人。</p>
                    </div>
                    <div class="footer">
                        <p>此邮件由系统自动发送，请勿回复。</p>
                        <p>&copy; 2026 ZSK Cloud. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """, magicLink, magicLink);
    }
}
