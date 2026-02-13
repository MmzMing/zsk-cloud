package com.zsk.gateway.filter;

import com.zsk.common.core.config.properties.IgnoreWhiteProperties;
import com.zsk.common.core.constant.CacheConstants;
import com.zsk.common.core.constant.SecurityConstants;
import com.zsk.common.core.domain.R;
import com.zsk.common.core.utils.JsonUtil;
import com.zsk.common.core.utils.StringUtils;
import com.zsk.common.redis.service.RedisService;
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
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthFilter implements GlobalFilter, Ordered {

    private final IgnoreWhiteProperties ignoreWhiteProperties;
    private final RedisService redisService;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String url = request.getURI().getPath();

        // 跳过不需要验证的路径
        if (matches(url, ignoreWhiteProperties.getWhites())) {
            return chain.filter(exchange);
        }

        String token = getToken(request);
        if (StringUtils.isEmpty(token)) {
            return unauthorizedResponse(exchange, "令牌不能为空");
        }

        String tokenKey = CacheConstants.LOGIN_TOKEN_KEY + token;
        boolean hasKey = redisService.hasKey(tokenKey);
        if (!hasKey) {
            return unauthorizedResponse(exchange, "令牌已过期或验证不正确");
        }

        // 刷新 token 过期时间
        redisService.expire(tokenKey, SecurityConstants.TOKEN_EXPIRE, TimeUnit.MINUTES);

        // 设置用户信息到请求头
        String tokenInfoJson = redisService.getCacheObject(tokenKey);
        if (StringUtils.isNotEmpty(tokenInfoJson)) {
            Map<String, Object> tokenInfo = JsonUtil.parseMap(tokenInfoJson);
            String userId = tokenInfo.get("userId").toString();
            String username = tokenInfo.get("username").toString();
            String roles = tokenInfo.get("roles") != null ? StringUtils.join((Collection<?>) tokenInfo.get("roles"), ",") : "";
            String permissions = tokenInfo.get("permissions") != null ? StringUtils.join((Collection<?>) tokenInfo.get("permissions"), ",") : "";

            ServerHttpRequest mutableReq = request.mutate()
                    .header(SecurityConstants.USER_ID_HEADER, userId)
                    .header(SecurityConstants.USER_NAME_HEADER, username)
                    .header(SecurityConstants.USER_KEY_HEADER, tokenKey)
                    .header(SecurityConstants.ROLES, roles)
                    .header(SecurityConstants.PERMISSIONS, permissions)
                    .build();
            ServerWebExchange mutableExchange = exchange.mutate().request(mutableReq).build();
            return chain.filter(mutableExchange);
        }

        return chain.filter(exchange);
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
