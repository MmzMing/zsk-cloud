package com.zsk.common.security.filter;

import cn.hutool.core.util.URLUtil;
import com.zsk.common.core.constant.SecurityConstants;
import com.zsk.common.core.context.SecurityContext;
import com.zsk.common.core.utils.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 请求头解析过滤器
 * 将请求头中的用户信息设置到当前线程上下文中，并同步到 Spring Security 上下文
 * 
 * @author zsk
 * @date 2024-02-13
 * @version 1.0
 */
@Component
public class HeaderContextFilter extends OncePerRequestFilter {
    /**
     * 执行过滤逻辑
     *
     * @param request 请求对象
     * @param response 响应对象
     * @param filterChain 过滤器链
     * @throws ServletException 异常
     * @throws IOException IO异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String userId = request.getHeader(SecurityConstants.USER_ID_HEADER);
        String userName = request.getHeader(SecurityConstants.USER_NAME_HEADER);
        String nickName = request.getHeader(SecurityConstants.NICK_NAME_HEADER);
        String deptId = request.getHeader(SecurityConstants.DEPT_ID);
        String rolesStr = request.getHeader(SecurityConstants.ROLES);
        String permissionsStr = request.getHeader(SecurityConstants.PERMISSIONS);

        if (StringUtils.isNotEmpty(userId)) {
            SecurityContext.setUserId(Long.valueOf(userId));
        }
        
        String decodedUserName = null;
        if (StringUtils.isNotEmpty(userName)) {
            decodedUserName = URLUtil.decode(userName);
            SecurityContext.setUserName(decodedUserName);
        }
        
        if (StringUtils.isNotEmpty(nickName)) {
            SecurityContext.setNickName(URLUtil.decode(nickName));
        }
        
        if (StringUtils.isNotEmpty(deptId)) {
            SecurityContext.setDeptId(Long.valueOf(deptId));
        }
        
        // 构建 Spring Security 权限集合
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        
        // 解析并设置角色
        Set<String> roles = strToSet(rolesStr);
        if (StringUtils.isNotEmpty(roles)) {
            SecurityContext.setRoles(roles);
            roles.stream()
                .map(role -> new SimpleGrantedAuthority(SecurityConstants.ROLE_PREFIX + role))
                .forEach(authorities::add);
        }
        
        // 解析并设置权限
        Set<String> permissions = strToSet(permissionsStr);
        if (StringUtils.isNotEmpty(permissions)) {
            SecurityContext.setPermissions(permissions);
            permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .forEach(authorities::add);
        }

        // 如果获取到了用户信息，将其填充到 Spring Security 上下文中
        if (StringUtils.isNotEmpty(userId) && StringUtils.isNotEmpty(decodedUserName)) {
            // 使用用户名和权限构建 Authentication 对象
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    decodedUserName, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            SecurityContext.clear();
            SecurityContextHolder.clearContext();
        }
    }

    /**
     * 解析逗号分隔的字符串为 Set
     */
    private Set<String> strToSet(String str) {
        return Stream.of(StringUtils.split(StringUtils.defaultString(str), ","))
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toSet());
    }
}
