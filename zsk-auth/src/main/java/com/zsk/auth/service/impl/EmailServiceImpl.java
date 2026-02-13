package com.zsk.auth.service.impl;

import com.zsk.auth.service.IEmailService;
import com.zsk.common.core.constant.CacheConstants;
import com.zsk.common.core.exception.AuthException;
import com.zsk.common.core.utils.StringUtils;
import com.zsk.common.redis.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 邮箱服务实现
 * 
 * @author wuhuaming
 * @date 2024-01-15
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements IEmailService {

    private final RedisService redisService;

    /** 邮件服务器主机名 */
    @Value("${email.host:smtp.qq.com}")
    private String emailHost;

    /** 邮件服务器端口 */
    @Value("${email.port:465}")
    private Integer emailPort;

    /** 邮件服务器认证账号 */
    @Value("${email.username}")
    private String emailUsername;

    /** 邮件服务器认证密码 */
    @Value("${email.password}")
    private String emailPassword;

    /** 发件人地址 */
    @Value("${email.from}")
    private String emailFrom;

    /** 邮件主题 */
    @Value("${email.subject:验证码}")
    private String emailSubject;

    /** 验证码过期时间（秒） */
    @Value("${email.code.expire:300}")
    private Long emailCodeExpire;

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
        String emailKey = CacheConstants.EMAIL_CODE_KEY + email;

        redisService.setCacheObject(emailKey, code, emailCodeExpire, TimeUnit.SECONDS);

        try {
            SimpleEmail simpleEmail = new SimpleEmail();
            simpleEmail.setHostName(emailHost);
            simpleEmail.setSmtpPort(emailPort);
            simpleEmail.setSSLOnConnect(true);
            simpleEmail.setAuthentication(emailUsername, emailPassword);
            simpleEmail.setFrom(emailFrom);
            simpleEmail.setSubject(emailSubject);
            simpleEmail.setMsg("您的验证码是：" + code + "，5分钟内有效。");
            simpleEmail.addTo(email);
            simpleEmail.send();

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
     * @param code 用户输入的验证码内容
     * @throws AuthException 验证码无效或错误时抛出
     */
    @Override
    public void validateEmailCode(String email, String code) {
        if (StringUtils.isEmpty(email) || StringUtils.isEmpty(code)) {
            throw new AuthException("邮箱和验证码不能为空");
        }

        String emailKey = CacheConstants.EMAIL_CODE_KEY + email;
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
}
