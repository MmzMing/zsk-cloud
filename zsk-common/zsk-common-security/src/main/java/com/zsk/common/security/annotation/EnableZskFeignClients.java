package com.zsk.common.security.annotation;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 自定义 Feign 扫描注解
 * 默认扫描 com.zsk 包下的所有 Feign 客户端
 *
 * @author zsk
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EnableFeignClients
public @interface EnableZskFeignClients {
    @AliasFor(annotation = EnableFeignClients.class)
    String[] value() default {};

    @AliasFor(annotation = EnableFeignClients.class)
    String[] basePackages() default {"com.zsk"};

    @AliasFor(annotation = EnableFeignClients.class)
    Class<?>[] basePackageClasses() default {};

    @AliasFor(annotation = EnableFeignClients.class)
    Class<?>[] defaultConfiguration() default {};

    @AliasFor(annotation = EnableFeignClients.class)
    Class<?>[] clients() default {};
}
