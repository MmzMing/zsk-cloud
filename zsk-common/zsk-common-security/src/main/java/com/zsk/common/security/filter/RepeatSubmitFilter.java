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
 * @version 1.0
 * @date 2024-02-13
 */
@Component
@RequiredArgsConstructor
public class RepeatSubmitFilter extends OncePerRequestFilter {
    /**
     * 重复提交处理提供者
     */
    private final RedisRepeatSubmitProvider repeatSubmitProvider;

    /**
     * 执行过滤逻辑
     *
     * @param request     请求对象
     * @param response    响应对象
     * @param filterChain 过滤器链
     * @throws ServletException 异常
     * @throws IOException      IO异常
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
     * 逻辑说明：
     * 1. 获取 Spring MVC 的 RequestMappingHandlerMapping，用于查找当前请求对应的 HandlerMethod。
     * 2. 如果请求没有对应的 HandlerMethod（如 404 或静态资源），则不进行防重校验。
     * 3. 检查方法上或类上是否标注了 @RepeatSubmit 注解。
     * 4. 如果存在注解，调用 repeatSubmitProvider 进行具体的防重逻辑判断：
     * - 根据 URL + Token + UserId 生成唯一的 Redis Key。
     * - 对比当前请求参数与 Redis 中记录的上一次请求参数。
     * - 校验两次请求的时间间隔是否小于注解设定的 interval 值。
     * 5. 若判定为重复提交，通过 ServletUtils 向客户端返回统一的 JSON 错误响应。
     *
     * @param request  请求对象
     * @param response 响应对象
     * @return true:重复提交, false:非重复提交
     * @throws Exception 异常
     */
    private boolean isRepeatSubmit(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 从 Spring 上下文中获取处理器映射器
        RequestMappingHandlerMapping requestMappingHandlerMapping = SpringUtil.getBean("requestMappingHandlerMapping");

        // 查找当前请求对应的处理器执行链
        HandlerExecutionChain chain = requestMappingHandlerMapping.getHandler(request);

        // 如果找不到处理器，或者处理器不是 HandlerMethod（Spring MVC 处理方法的封装），直接放行
        if (chain == null || !(chain.getHandler() instanceof HandlerMethod handlerMethod)) {
            return false;
        }

        // 优先从方法上获取 @RepeatSubmit 注解
        RepeatSubmit annotation = handlerMethod.getMethodAnnotation(RepeatSubmit.class);
        if (annotation == null) {
            // 如果方法上没有，再尝试从 Controller 类上获取
            annotation = handlerMethod.getBeanType().getAnnotation(RepeatSubmit.class);
        }

        // 如果最终没找到注解，说明该接口不需要防重校验
        if (annotation == null) {
            return false;
        }

        // 调用 Redis 提供者进行核心逻辑判断（参数比对 + 时间间隔校验）
        if (repeatSubmitProvider.isRepeatSubmit(request, annotation)) {
            // 如果是重复提交，直接向 Response 写入错误信息并终止请求
            ServletUtils.renderString(response, JsonUtil.toJson(R.fail(annotation.message())));
            return true;
        }

        return false;
    }
}
