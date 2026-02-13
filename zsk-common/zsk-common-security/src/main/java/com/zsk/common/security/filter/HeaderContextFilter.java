package com.zsk.common.security.filter;

import cn.hutool.core.util.URLUtil;
import com.zsk.common.core.constant.SecurityConstants;
import com.zsk.common.core.context.SecurityContext;
import com.zsk.common.core.utils.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 请求头解析过滤器
 * 将请求头中的用户信息设置到当前线程上下文中
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
        String deptId = request.getHeader(SecurityConstants.DEPT_ID);
        String token = request.getHeader(SecurityConstants.USER_KEY_HEADER);
        String rolesStr = request.getHeader(SecurityConstants.ROLES);
        String permissionsStr = request.getHeader(SecurityConstants.PERMISSIONS);

        if (StringUtils.isNotEmpty(userId)) {
            SecurityContext.setUserId(Long.valueOf(userId));
        }
        if (StringUtils.isNotEmpty(userName)) {
            SecurityContext.setUserName(URLUtil.decode(userName));
        }
        if (StringUtils.isNotEmpty(deptId)) {
            SecurityContext.setDeptId(Long.valueOf(deptId));
        }
        if (StringUtils.isNotEmpty(token)) {
            SecurityContext.setToken(token);
        }
        if (StringUtils.isNotEmpty(rolesStr)) {
            Set<String> roles = Stream.of(rolesStr.split(","))
                    .filter(StringUtils::isNotEmpty)
                    .collect(Collectors.toSet());
            SecurityContext.setRoles(roles);
        }
        if (StringUtils.isNotEmpty(permissionsStr)) {
            Set<String> permissions = Stream.of(permissionsStr.split(","))
                    .filter(StringUtils::isNotEmpty)
                    .collect(Collectors.toSet());
            SecurityContext.setPermissions(permissions);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            SecurityContext.clear();
        }
    }
}
