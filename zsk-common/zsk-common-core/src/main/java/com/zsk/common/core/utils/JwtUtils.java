package com.zsk.common.core.utils;

import com.zsk.common.core.constant.SecurityConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Jwt工具类
 *
 * @author wuhuaming
 */
public class JwtUtils {
    /**
     * 默认密钥（生产环境应从配置文件读取）
     */
    private static final String DEFAULT_SECRET = "zsk-cloud-secret-key-20260213-abcdefg";

    /**
     * 获取密钥对象
     *
     * @param secret 字符串密钥
     * @return 密钥对象
     */
    private static SecretKey getSecretKey(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 从数据声明生成令牌
     *
     * @param claims 数据声明
     * @return 令牌
     */
    public static String createToken(Map<String, Object> claims) {
        return createToken(claims, DEFAULT_SECRET);
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
        return parseToken(token, DEFAULT_SECRET);
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
