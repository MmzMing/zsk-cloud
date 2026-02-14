package com.zsk.common.security.aspect;

import com.zsk.common.core.constant.CommonConstants;
import com.zsk.common.core.exception.PermissionException;
import com.zsk.common.core.utils.ServletUtils;
import com.zsk.common.core.utils.StringUtils;
import com.zsk.common.security.annotation.InnerAuth;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * 内部服务调用验证切面
 *
 * @author wuhuaming
 */
@Aspect
@Component
public class InnerAuthAspect implements Ordered {

    @Around("@annotation(innerAuth)")
    public Object around(ProceedingJoinPoint point, InnerAuth innerAuth) throws Throwable {
        String source = ServletUtils.getRequest().getHeader(CommonConstants.REQUEST_SOURCE_HEADER);

        // 内部调用标签校验
        if (!StringUtils.equals(CommonConstants.REQUEST_SOURCE_INNER, source)) {
            throw new PermissionException("没有内部访问权限，不允许直接访问");
        }

        String userId = ServletUtils.getRequest().getHeader(CommonConstants.USER_ID_HEADER);
        // 用户信息校验
        if (innerAuth.isUser() && StringUtils.isEmpty(userId)) {
            throw new PermissionException("没有内部访问权限，无法获取用户信息");
        }

        return point.proceed();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
