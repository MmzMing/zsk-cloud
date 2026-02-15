package com.zsk.common.core.utils;

import com.zsk.common.core.constant.SecurityConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

/**
 * Jwt工具类
 * 经在 application.yml 中配置了 public-key 和 private-key ，系统将自动检测并优先使用 非对称加密（RSA） 。
 * 移除了公私钥配置，系统会回退尝试使用 secret 。
 * 如果 secret 也未配置，系统将报错，从而避免了使用不安全的默认硬编码密钥。
 *
 * @author wuhuaming
 */
@Slf4j
public class JwtUtils {
    /**
     * 默认密钥
     */
    private static String secret;

    /**
     * 公钥
     */
    private static PublicKey publicKey;

    /**
     * 私钥
     */
    private static PrivateKey privateKey;

    /**
     * 初始化密钥信息
     *
     * @param secret        对称加密密钥
     * @param publicKeyStr  非对称加密公钥（Base64）
     * @param privateKeyStr 非对称加密私钥（Base64）
     */
    public static void init(String secret, String publicKeyStr, String privateKeyStr) {
        JwtUtils.secret = secret;
        try {
            if (StringUtils.hasText(publicKeyStr)) {
                byte[] keyBytes = Base64.getDecoder().decode(publicKeyStr);
                X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
                KeyFactory kf = KeyFactory.getInstance("RSA");
                JwtUtils.publicKey = kf.generatePublic(spec);
            }
            if (StringUtils.hasText(privateKeyStr)) {
                byte[] keyBytes = Base64.getDecoder().decode(privateKeyStr);
                PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
                KeyFactory kf = KeyFactory.getInstance("RSA");
                JwtUtils.privateKey = kf.generatePrivate(spec);
            }
        } catch (Exception e) {
            log.error("初始化 JWT 密钥失败: {}", e.getMessage());
        }
    }

    /**
     * 获取密钥对象
     *
     * @param secret 字符串密钥
     * @return 密钥对象
     */
    private static SecretKey getSecretKey(String secret) {
        if (!StringUtils.hasText(secret)) {
            throw new IllegalArgumentException("JWT secret key is not configured");
        }
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 从数据声明生成令牌
     *
     * @param claims 数据声明
     * @return 令牌
     */
    public static String createToken(Map<String, Object> claims) {
        if (privateKey != null) {
            return Jwts.builder()
                    .claims(claims)
                    .signWith(privateKey)
                    .compact();
        }
        return createToken(claims, secret);
    }

    /**
     * 从数据声明生成令牌
     *
     * @param claims 数据声明
     * @param secret 密钥
     * @return 令牌
     */
    public static String createToken(Map<String, Object> claims, String secret) {
        return Jwts.builder()
                .claims(claims)
                .signWith(getSecretKey(secret))
                .compact();
    }

    /**
     * 从令牌中获取数据声明
     *
     * @param token 令牌
     * @return 数据声明
     */
    public static Claims parseToken(String token) {
        return Jwts.parser()
                .keyLocator(header -> {
                    String alg = header.getAlgorithm();
                    if (alg != null && (alg.startsWith("RS") || alg.startsWith("PS"))) {
                        return publicKey;
                    }
                    return getSecretKey(secret);
                })
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从令牌中获取数据声明
     *
     * @param token  令牌
     * @param secret 密钥
     * @return 数据声明
     */
    public static Claims parseToken(String token, String secret) {
        return Jwts.parser()
                .verifyWith(getSecretKey(secret))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 根据令牌获取用户ID
     *
     * @param token 令牌
     * @return 用户ID
     */
    public static String getUserId(String token) {
        Claims claims = parseToken(token);
        return getValue(claims, SecurityConstants.USER_ID);
    }

    /**
     * 根据令牌获取用户名
     *
     * @param token 令牌
     * @return 用户名
     */
    public static String getUserName(String token) {
        Claims claims = parseToken(token);
        return getValue(claims, SecurityConstants.USER_NAME);
    }

    /**
     * 根据令牌获取用户Key
     *
     * @param token 令牌
     * @return 用户Key
     */
    public static String getUserKey(String token) {
        Claims claims = parseToken(token);
        return getValue(claims, SecurityConstants.USER_KEY);
    }

    /**
     * 根据令牌获取租户ID
     *
     * @param token 令牌
     * @return 租户ID
     */
    public static String getTenantId(String token) {
        Claims claims = parseToken(token);
        return getValue(claims, SecurityConstants.DEPT_ID);
    }

    /**
     * 根据身份标识获取值
     *
     * @param claims 身份标识
     * @param key    键
     * @return 值
     */
    private static String getValue(Claims claims, String key) {
        Object value = claims.get(key);
        return value == null ? "" : String.valueOf(value);
    }
}
