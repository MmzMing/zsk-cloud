package com.zsk.common.security.config;

import com.zsk.common.core.domain.R;
import com.zsk.common.core.utils.JsonUtil;
import com.zsk.common.core.utils.ServletUtils;
import com.zsk.common.security.filter.HeaderContextFilter;
import com.zsk.common.security.filter.RepeatSubmitFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;

/**
 * 权限配置
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-14
 */
@AutoConfiguration
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SecurityConfiguration {
    /**
     * 请求头解析过滤器
     */
    private final HeaderContextFilter headerContextFilter;
    /**
     * 防止重复提交过滤器
     */
    private final RepeatSubmitFilter repeatSubmitFilter;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    /**
     * 配置安全过滤链
     * <p>
     * 核心逻辑说明：
     * 1. 无状态处理：通过 SessionCreationPolicy.STATELESS 禁用 Session，完全依赖 Token 认证。
     * 2. 身份恢复：HeaderContextFilter 负责从网关透传的 Header 中解析用户信息并存入 SecurityContext。
     * 3. 权限判定：通过 isAuthenticated 方法检查 SecurityContext 或内部调用标识。
     * 4. 异常处理：自定义 AuthenticationEntryPoint，在认证失败时返回 401 JSON，而非重定向。
     * <p>
     * 为什么不跳转到 /login：
     * 1. 前后端分离架构：后端仅提供 RESTful API，跳转会导致 AJAX 请求收到 HTML 源码而非 JSON。
     * 2. 跨域限制：CORS 策略下，浏览器会拦截跨域的 302 重定向。
     * 3. 职责解耦：后端只负责返回 401 状态码，由前端 Axios 拦截器捕获并执行 router.push('/login')。
     *
     * @param http HttpSecurity
     * @return SecurityFilterChain
     * @throws Exception 异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 禁用 CSRF，因为在微服务架构下，通常使用 Token 验证，不需要 CSRF 防护
        http.csrf(AbstractHttpConfigurer::disable)
                // 开启跨域配置
                .cors(Customizer.withDefaults())
                // 禁用 Session，使用无状态（STATELESS）策略，完全依赖 Token 认证
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 配置请求权限控制，允许所有请求，通过注解控制权限
                .authorizeHttpRequests(auth -> {
                    auth.anyRequest().permitAll();
                })
                // 配置异常处理逻辑
                .exceptionHandling(ex -> ex
                        // 未认证（未登录）处理器
                        .authenticationEntryPoint((request, response, authException) ->
                                writeUnauthorized(response))
                        // 权限不足处理器
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                writeForbidden(response)))
                // 在授权过滤器之前添加自定义请求头解析过滤器，用于解析网关透传的用户信息
                .addFilterBefore(headerContextFilter, AuthorizationFilter.class)
                // 在请求头解析过滤器之前添加防重提交过滤器
                .addFilterBefore(repeatSubmitFilter, HeaderContextFilter.class)
                // 禁用 HTTP Basic 认证
                .httpBasic(AbstractHttpConfigurer::disable)
                // 禁用表单登录
                .formLogin(AbstractHttpConfigurer::disable)
                // 禁用默认注销功能
                .logout(AbstractHttpConfigurer::disable);

        return http.build();
    }

    /**
     * 跨域资源配置
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许所有请求域名
        config.setAllowedOriginPatterns(Collections.singletonList("*"));
        // 允许所有请求方法
        config.addAllowedMethod("*");
        // 允许所有请求头
        config.addAllowedHeader("*");
        // 允许凭证
        config.setAllowCredentials(true);
        // 暴露哪些头部信息
        config.addExposedHeader("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对所有路径生效
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * 写入未认证响应
     *
     * @param response 响应对象
     */
    private void writeUnauthorized(HttpServletResponse response) {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        ServletUtils.renderString(response, JsonUtil.toJson(R.unauthorized("未登录或登录已过期")));
    }

    /**
     * 写入未授权响应
     *
     * @param response 响应对象
     */
    private void writeForbidden(HttpServletResponse response) {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        ServletUtils.renderString(response, JsonUtil.toJson(R.forbidden("没有权限访问资源")));
    }
}
