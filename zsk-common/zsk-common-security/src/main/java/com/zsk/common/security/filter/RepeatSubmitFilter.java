package com.zsk.common.security.filter;

import com.zsk.common.core.domain.R;
import com.zsk.common.core.utils.JsonUtil;
import com.zsk.common.core.utils.ServletUtils;
import com.zsk.common.core.utils.SpringUtil;
import com.zsk.common.security.annotation.RepeatSubmit;
import com.zsk.common.security.interceptor.impl.RedisRepeatSubmitProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;

/**
 * 防止重复提交过滤器
 * 
 * @author zsk
 * @date 2024-02-13
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
public class RepeatSubmitFilter extends OncePerRequestFilter {
    /** 重复提交处理提供者 */
    private final RedisRepeatSubmitProvider repeatSubmitProvider;

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
        try {
            if (isRepeatSubmit(request, response)) {
                return;
            }
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
        filterChain.doFilter(request, response);
    }

    /**
     * 判断是否重复提交
     *
     * @param request 请求对象
     * @param response 响应对象
     * @return true:重复提交, false:非重复提交
     * @throws Exception 异常
     */
    private boolean isRepeatSubmit(HttpServletRequest request, HttpServletResponse response) throws Exception {
        RequestMappingHandlerMapping requestMappingHandlerMapping = SpringUtil.getBean(RequestMappingHandlerMapping.class);
        HandlerExecutionChain chain = requestMappingHandlerMapping.getHandler(request);
        if (chain == null || !(chain.getHandler() instanceof HandlerMethod handlerMethod)) {
            return false;
        }
        RepeatSubmit annotation = handlerMethod.getMethodAnnotation(RepeatSubmit.class);
        if (annotation == null) {
            annotation = handlerMethod.getBeanType().getAnnotation(RepeatSubmit.class);
        }
        if (annotation == null) {
            return false;
        }
        if (repeatSubmitProvider.isRepeatSubmit(request, annotation)) {
            ServletUtils.renderString(response, JsonUtil.toJson(R.fail(annotation.message())));
            return true;
        }
        return false;
    }
}
