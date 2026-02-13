package com.zsk.common.security.aspect;

import com.zsk.common.security.annotation.RequiresLogin;
import com.zsk.common.security.annotation.RequiresPermissions;
import com.zsk.common.security.annotation.RequiresRoles;
import com.zsk.common.security.enums.Logical;
import com.zsk.common.security.utils.AuthUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 权限认证切面
 *
 * @author zsk
 */
@Aspect
@Component
public class PreAuthorizeAspect {

    /**
     * 定义 AOP 签名
     */
    public static final String POINTCUT_SIGN = "@annotation(com.zsk.common.security.annotation.RequiresLogin) || "
            + "@annotation(com.zsk.common.security.annotation.RequiresPermissions) || "
            + "@annotation(com.zsk.common.security.annotation.RequiresRoles)";

    @Pointcut(POINTCUT_SIGN)
    public void pointcut() {
    }

    @Before("pointcut()")
    public void before(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Class<?> targetClass = joinPoint.getTarget().getClass();

        // 校验登录
        RequiresLogin requiresLogin = method.getAnnotation(RequiresLogin.class);
        if (requiresLogin == null) {
            requiresLogin = targetClass.getAnnotation(RequiresLogin.class);
        }
        if (requiresLogin != null) {
            AuthUtils.checkLogin();
        }

        // 校验角色
        RequiresRoles requiresRoles = method.getAnnotation(RequiresRoles.class);
        if (requiresRoles == null) {
            requiresRoles = targetClass.getAnnotation(RequiresRoles.class);
        }
        if (requiresRoles != null) {
            if (requiresRoles.logical() == Logical.AND) {
                AuthUtils.checkRoleAnd(requiresRoles.value());
            } else {
                AuthUtils.checkRoleAny(requiresRoles.value());
            }
        }

        // 校验权限
        RequiresPermissions requiresPermissions = method.getAnnotation(RequiresPermissions.class);
        if (requiresPermissions == null) {
            requiresPermissions = targetClass.getAnnotation(RequiresPermissions.class);
        }
        if (requiresPermissions != null) {
            if (requiresPermissions.logical() == Logical.AND) {
                AuthUtils.checkPermissionAnd(requiresPermissions.value());
            } else {
                AuthUtils.checkPermissionAny(requiresPermissions.value());
            }
        }
    }
}
