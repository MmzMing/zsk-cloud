package com.zsk.gateway.filter;

import com.zsk.common.core.config.properties.IgnoreWhiteProperties;
import com.zsk.common.core.constant.CacheConstants;
import com.zsk.common.core.constant.SecurityConstants;
import com.zsk.common.core.domain.R;
import com.zsk.common.core.utils.JwtUtils;
import com.zsk.common.core.utils.JsonUtil;
import com.zsk.common.core.utils.StringUtils;
import com.zsk.common.redis.service.RedisService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 认证过滤器
 *
 * @author wuhuaming
 * @date 2024-02-13
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthFilter implements GlobalFilter, Ordered {

    private final IgnoreWhiteProperties ignoreWhiteProperties;
    private final RedisService redisService;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    /**
     * 过滤逻辑实现
     *
     * @param exchange 服务网络交换器
     * @param chain 过滤器链
     * @return Mono<Void>
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String url = request.getURI().getPath();

        // 1. 白名单校验：跳过不需要验证的路径（如登录、验证码、静态资源等）
        if (matches(url, ignoreWhiteProperties.getWhites())) {
            return chain.filter(exchange);
        }

        // 2. 令牌提取：从请求头 Authorization 中解析 Token
        String token = getToken(request);
        
        // 3. 匿名访问处理：
        // 如果令牌为空，则直接放行。下游微服务将根据方法上的安全注解（如 @PreAuthorize）决定是否拦截。
        // 这种设计实现了“缩小 Security 粒度”，即：网关不强制鉴权，由业务层按需鉴权。
        if (StringUtils.isEmpty(token)) {
            return chain.filter(exchange);
        }

        // 4. 令牌有效性校验 & 5. 用户信息获取
        // 使用 JWT 解析获取用户信息，Redis 仅用于校验 Token 状态（是否过期/黑名单）
        try {
            Claims claims = JwtUtils.parseToken(token);
            if (claims == null) {
                return unauthorizedResponse(exchange, "令牌已过期或验证不正确");
            }
            
            // 获取 Token 唯一标识 (jti/uuid)
            String uuid = (String) claims.get(SecurityConstants.USER_KEY);
            String tokenKey = CacheConstants.LOGIN_TOKEN_KEY + uuid;
            
            // 检查 Redis 中是否存在该 Key (状态管控)
            boolean hasKey = redisService.hasKey(tokenKey);
            if (!hasKey) {
                return unauthorizedResponse(exchange, "令牌已过期或验证不正确");
            }

            // 刷新 Token 在 Redis 中的过期时间 (滑动过期)
            redisService.expire(tokenKey, SecurityConstants.TOKEN_EXPIRE, TimeUnit.MINUTES);

            // 从 JWT Claims 中直接提取用户信息，无需查 Redis
            String userId = claims.get(SecurityConstants.USER_ID).toString();
            String username = claims.get(SecurityConstants.USER_NAME).toString();
            String nickname = claims.get(SecurityConstants.NICK_NAME) != null ? claims.get(SecurityConstants.NICK_NAME).toString() : "";
            
            // 处理集合类型的 Claims (roles, permissions)
            Object rolesObj = claims.get(SecurityConstants.ROLES);
            Object permsObj = claims.get(SecurityConstants.PERMISSIONS);

            String roles = rolesObj != null ? StringUtils.join((Collection<?>) rolesObj, ",") : "";
            String permissions = permsObj != null ? StringUtils.join((Collection<?>) permsObj, ",") : "";

            // 将用户信息注入请求头
            ServerHttpRequest mutableReq = request.mutate()
                    .header(SecurityConstants.USER_ID_HEADER, userId)
                    .header(SecurityConstants.USER_NAME_HEADER, username)
                    .header(SecurityConstants.NICK_NAME_HEADER, nickname)
                    .header(SecurityConstants.USER_KEY_HEADER, uuid) // 注意：这里传递的是 uuid 而不是完整的 JWT
                    .header(SecurityConstants.ROLES, roles)
                    .header(SecurityConstants.PERMISSIONS, permissions)
                    .build();
            ServerWebExchange mutableExchange = exchange.mutate().request(mutableReq).build();
            
            // 继续执行后续过滤器链
            return chain.filter(mutableExchange);
        } catch (Exception e) {
            log.error("JWT解析失败: {}", e.getMessage());
            return unauthorizedResponse(exchange, "令牌已过期或验证不正确");
        }
    }

    /**
     * 查找字符串是否匹配白名单
     */
    private boolean matches(String url, List<String> patternList) {
        if (StringUtils.isEmpty(url) || patternList == null || patternList.isEmpty()) {
            return false;
        }
        for (String pattern : patternList) {
            if (antPathMatcher.match(pattern, url)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取请求token
     */
    private String getToken(ServerHttpRequest request) {
        String token = request.getHeaders().getFirst(SecurityConstants.AUTHORIZATION_HEADER);
        if (StringUtils.isNotEmpty(token) && token.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            token = token.replace(SecurityConstants.TOKEN_PREFIX, "");
        }
        return token;
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String msg) {
        log.error("[鉴权异常处理]请求路径:{}", exchange.getRequest().getPath());
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        R<?> result = R.fail(401, msg);
        DataBuffer buffer = response.bufferFactory().wrap(JsonUtil.toJsonString(result).getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -200;
    }
}
