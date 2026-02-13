package com.zsk.common.security.annotation;

import java.lang.annotation.*;

/**
 * 内部认证：仅限内部服务调用
 *
 * @author zsk
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InnerAuth {
    /**
     * 是否校验用户信息
     */
    boolean isUser() default false;
}
