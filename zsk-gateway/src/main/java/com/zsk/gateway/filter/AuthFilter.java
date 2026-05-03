package com.zsk.gateway.filter;

import com.zsk.common.core.constant.CacheConstants;
import com.zsk.common.core.constant.SecurityConstants;
import com.zsk.common.core.domain.R;
import com.zsk.common.core.utils.JsonUtil;
import com.zsk.common.core.utils.JwtUtils;
import com.zsk.common.core.utils.StringUtils;
import com.zsk.common.redis.service.RedisService;
import com.zsk.gateway.config.properties.IgnoreWhiteProperties;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 认证过滤器
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-14
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
     * <p>
     * 该方法是网关认证的核心过滤器，负责对所有请求进行Token验证和用户信息注入。
     * 采用"网关管令牌，Security管权限"的设计理念，网关仅负责Token有效性验证，
     * 具体的权限控制由下游微服务的Spring Security处理。
     * <p>
     * 处理流程：
     * 1. 白名单校验 - 跳过不需要认证的路径
     * 2. Token提取 - 从请求头中解析Bearer Token
     * 3. Token验证 - 解析JWT并验证Redis中的Token状态
     * 4. 用户信息注入 - 将用户信息添加到请求头传递给下游服务
     * <p>
     * Token存储结构（变更后）：
     * - Key: zsk:login:token:{userId}
     * - Value: Set<token> (支持多设备登录，最多5个Token)
     *
     * @param exchange 服务网络交换器，包含请求和响应信息
     * @param chain    过滤器链，用于继续执行后续过滤器
     * @return Mono<Void> 异步处理结果
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String url = request.getURI().getPath();

        // ==================== 步骤1：白名单校验 ====================
        // 检查请求路径是否在白名单中（登录、验证码、静态资源等）
        // 白名单配置在 ignoreWhiteProperties 中，支持Ant路径匹配模式
        if (matches(url, ignoreWhiteProperties.getWhites())) {
            return chain.filter(exchange);
        }

        // ==================== 步骤2：Token提取 ====================
        // 从请求头 Authorization 中解析 Token
        // 格式：Authorization: Bearer {token}
        String token = getToken(request);

        // ==================== 步骤3：匿名访问处理 ====================
        // 网关强制要求Token验证，不走网关的请求由下游微服务处理
        // 设计原则：网关管令牌，Security管权限
        if (StringUtils.isEmpty(token)) {
            return unauthorizedResponse(exchange, "令牌不能为空");
        }

        // ==================== 步骤4：Token有效性校验 ====================
        // 4.1 解析JWT获取Claims（用户信息载体）
        // 4.2 从Redis验证Token状态（是否过期/被踢出）
        // 4.3 刷新Token过期时间（滑动过期机制）
        try {
            // 解析JWT Token，获取Claims对象
            // Claims包含：user_id, user_name, nick_name等信息
            Claims claims = JwtUtils.parseToken(token);
            if (claims == null) {
                return unauthorizedResponse(exchange, "令牌已过期或验证不正确");
            }

            // 从Claims中提取用户ID
            String userId = claims.get(SecurityConstants.USER_ID).toString();

            // 构建Redis Key：zsk:login:token:{userId}
            // 该Key存储该用户的所有有效Token集合（Set结构）
            String tokenKey = CacheConstants.CACHE_LOGIN_TOKEN + userId;

            // 验证当前Token是否在用户的Token集合中
            // 支持多设备登录：一个用户可以有多个有效Token
            Boolean isMember = redisService.isMemberOfSet(tokenKey, token);
            if (Boolean.FALSE.equals(isMember)) {
                // Token不在集合中，可能已被踢出或过期
                return unauthorizedResponse(exchange, "令牌已过期或验证不正确");
            }

            // 刷新Token集合的过期时间（滑动过期）
            // 每次请求都会重置过期时间，保持活跃用户的登录状态
            redisService.expire(tokenKey, SecurityConstants.TOKEN_EXPIRE, TimeUnit.MINUTES);

            // ==================== 步骤5：获取用户详细信息 ====================
            // 从Claims中提取用户基本信息
            String username = claims.get(SecurityConstants.USER_NAME).toString();
            String nickname = claims.get(SecurityConstants.NICK_NAME) != null ? claims.get(SecurityConstants.NICK_NAME).toString() : "";

            // 从Redis获取用户的角色和权限信息
            // Key格式：
            // - zsk:login:roles:{userId} -> Set<String> 角色集合
            // - zsk:login:permissions:{userId} -> Set<String> 权限集合
            String rolesKey = CacheConstants.CACHE_LOGIN_ROLES + userId;
            String permsKey = CacheConstants.CACHE_LOGIN_PERMISSIONS + userId;
            Set<String> rolesSet = redisService.getCacheObject(rolesKey);
            Set<String> permsSet = redisService.getCacheObject(permsKey);

            // 将Set集合转换为逗号分隔的字符串
            // 例如：admin,user,guest 或 system:user:list,system:role:add
            String roles = StringUtils.isNotEmpty(rolesSet) ? StringUtils.join(rolesSet, ",") : "";
            String permissions = StringUtils.isNotEmpty(permsSet) ? StringUtils.join(permsSet, ",") : "";

            // 刷新角色和权限缓存的过期时间
            // 与Token保持一致的过期时间
            redisService.expire(rolesKey, SecurityConstants.TOKEN_EXPIRE, TimeUnit.MINUTES);
            redisService.expire(permsKey, SecurityConstants.TOKEN_EXPIRE, TimeUnit.MINUTES);

            // ==================== 步骤6：注入用户信息到请求头 ====================
            // 将用户信息添加到请求头，传递给下游微服务
            // 下游服务通过解析请求头获取用户信息，无需再次查询数据库
            ServerHttpRequest mutableReq = request.mutate()
                    .header(SecurityConstants.USER_ID_HEADER, userId)           // 用户ID
                    .header(SecurityConstants.USER_NAME_HEADER, username)       // 用户名
                    .header(SecurityConstants.NICK_NAME_HEADER, nickname)       // 昵称
                    .header(SecurityConstants.USER_KEY_HEADER, userId)          // 用户标识（原uuid，现改为userId）
                    .header(SecurityConstants.ROLES, roles)                     // 角色列表
                    .header(SecurityConstants.PERMISSIONS, permissions)         // 权限列表
                    .build();
            ServerWebExchange mutableExchange = exchange.mutate().request(mutableReq).build();

            // 继续执行后续过滤器链，将请求转发给下游服务
            return chain.filter(mutableExchange);
        } catch (Exception e) {
            // JWT解析失败，记录日志并返回401错误
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
            return token;
        }

        HttpCookie cookie = request.getCookies().getFirst("access_token");
        if (cookie != null && StringUtils.isNotEmpty(cookie.getValue())) {
            return cookie.getValue();
        }

        return null;
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
