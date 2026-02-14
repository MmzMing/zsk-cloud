package com.zsk.common.log.aspect;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.zsk.common.core.utils.IpUtils;
import com.zsk.common.log.annotation.Log;
import com.zsk.common.log.domain.OperLog;
import com.zsk.common.security.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * 操作日志记录处理
 *
 * @author zsk
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final MongoTemplate mongoTemplate;

    // 配置织入点 - 拦截 com.zsk 包下所有 controller
    @Pointcut("execution(public * com.zsk..controller..*.*(..))")
    public void logPointCut() {}

    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long beginTime = System.currentTimeMillis();
        OperLog operLog = new OperLog();
        Object result = null;
        try {
            result = point.proceed();
        } catch (Throwable e) {
            operLog.setStatus(1);
            operLog.setErrorMsg(StrUtil.sub(e.getMessage(), 0, 2000));
            throw e;
        } finally {
            long costTime = System.currentTimeMillis() - beginTime;
            operLog.setCostTime(costTime);
            operLog.setOperTime(LocalDateTime.now());
            if (operLog.getStatus() == null) {
                operLog.setStatus(0);
            }
            // 获取注解
            Log controllerLog = getAnnotationLog(point);
            saveLog(point, operLog, result, controllerLog);
        }
        return result;
    }

    private void saveLog(ProceedingJoinPoint joinPoint, OperLog operLog, Object result, Log controllerLog) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                operLog.setOperUrl(request.getRequestURI());
                operLog.setOperIp(IpUtils.getIpAddr(request));
                operLog.setRequestMethod(request.getMethod());
                operLog.setOperName(SecurityUtils.getUserName());
                
                if (controllerLog == null || controllerLog.isSaveRequestData()) {
                    if (joinPoint.getArgs().length > 0) {
                         try {
                            String params = argsArrayToString(joinPoint.getArgs());
                            operLog.setOperParam(StrUtil.sub(params, 0, 2000));
                         } catch (Exception e) {
                             // ignore
                         }
                    }
                }
            }
            
            operLog.setMethod(joinPoint.getSignature().getName());
            
            if (controllerLog != null) {
                operLog.setBusinessType(controllerLog.businessType().ordinal());
                operLog.setTitle(controllerLog.title());
            }

            if (result != null && !isFilterObject(result)) {
                if (controllerLog == null || controllerLog.isSaveResponseData()) {
                     try {
                        operLog.setJsonResult(StrUtil.sub(JSONUtil.toJsonStr(result), 0, 2000));
                     } catch (Exception e) {
                         // ignore
                     }
                }
            }

            mongoTemplate.save(operLog);
            
        } catch (Exception e) {
            log.error("保存操作日志异常:{}", e.getMessage());
        }
    }

    /**
     * 是否存在注解，如果存在就获取
     */
    private Log getAnnotationLog(ProceedingJoinPoint joinPoint) {
        try {
            java.lang.reflect.Method method = ((org.aspectj.lang.reflect.MethodSignature) joinPoint.getSignature()).getMethod();
            if (method != null) {
                return method.getAnnotation(Log.class);
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }
    
    /**
     * 参数拼装
     */
    private String argsArrayToString(Object[] paramsArray) {
        StringBuilder params = new StringBuilder();
        if (paramsArray != null && paramsArray.length > 0) {
            for (Object o : paramsArray) {
                if (o != null && !isFilterObject(o)) {
                    try {
                        String jsonObj = JSONUtil.toJsonStr(o);
                        params.append(jsonObj).append(" ");
                    } catch (Exception e) {
                    }
                }
            }
        }
        return params.toString().trim();
    }

    /**
     * 判断是否需要过滤的对象
     * 
     * @param o 对象信息。
     * @return 如果是需要过滤的对象，则返回true；否则返回false。
     */
    public boolean isFilterObject(final Object o) {
        Class<?> clazz = o.getClass();
        if (clazz.isArray()) {
            return clazz.getComponentType().isAssignableFrom(MultipartFile.class);
        } else if (Collection.class.isAssignableFrom(clazz)) {
            Collection collection = (Collection) o;
            for (Object value : collection) {
                return value instanceof MultipartFile;
            }
        } else if (Map.class.isAssignableFrom(clazz)) {
            Map map = (Map) o;
            for (Object value : map.entrySet()) {
                Map.Entry entry = (Map.Entry) value;
                return entry.getValue() instanceof MultipartFile;
            }
        }
        return o instanceof MultipartFile || o instanceof HttpServletRequest || o instanceof HttpServletResponse
                || o instanceof BindingResult;
    }
}
