package com.zsk.common.security.interceptor.impl;

import com.zsk.common.core.constant.CommonConstants;
import com.zsk.common.core.utils.JsonUtil;
import com.zsk.common.core.utils.StringUtils;
import com.zsk.common.redis.service.RedisService;
import com.zsk.common.security.annotation.RepeatSubmit;
import com.zsk.common.security.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Redis 防重复提交提供者
 * 核心逻辑：通过记录请求的 [URL + 用户标识 + 请求参数] 以及 [请求时间] 到 Redis 中，
 * 在设定的间隔时间内，如果再次收到相同的请求，则判定为重复提交。
 *
 * @author wuhuaming
 */
@Component
public class RedisRepeatSubmitProvider {
    /** Redis 缓存中存储请求参数的 Key */
    public final String REPEAT_PARAMS = "repeatParams";

    /** Redis 缓存中存储请求时间的 Key */
    public final String REPEAT_TIME = "repeatTime";

    /** 防重提交 Redis Key 前缀 */
    public final String REPEAT_SUBMIT_KEY = "repeat_submit:";

    @Autowired
    private RedisService redisService;

    /**
     * 判断是否为重复提交
     * 
     * @param request 请求对象
     * @param annotation 防重注解配置
     * @return true: 是重复提交, false: 不是重复提交
     */
    public boolean isRepeatSubmit(HttpServletRequest request, RepeatSubmit annotation) {
        // 1. 获取当前请求参数并转为 JSON 字符串
        String nowParams = JsonUtil.toJson(request.getParameterMap());

        // 2. 构建唯一的缓存 Key：前缀 + URL + Token + 用户ID
        // 这样可以精确锁定到：哪个用户在哪个接口上发了什么请求
        String url = request.getRequestURI();
        String header = request.getHeader(CommonConstants.TOKEN_HEADER);
        Long userId = SecurityUtils.getUserId();

        String cacheRepeatKey = REPEAT_SUBMIT_KEY + url + ":" + StringUtils.nvl(header, "") + ":" + userId;

        // 3. 从 Redis 中获取上一次该请求的缓存数据
        Map<String, Object> sessionMap = redisService.getCacheObject(cacheRepeatKey);
        if (sessionMap != null) {
            // 如果缓存存在，提取上次请求的参数和时间
            if (sessionMap.containsKey(REPEAT_PARAMS)) {
                String preParams = (String) sessionMap.get(REPEAT_PARAMS);
                long preTime = (long) sessionMap.get(REPEAT_TIME);
                
                // 4. 核心比对：
                // a. 参数是否一致 (compareParams)
                // b. 当前时间与上次时间差是否小于注解设定的间隔 (interval)
                if (compareParams(nowParams, preParams) && (System.currentTimeMillis() - preTime) < annotation.interval()) {
                    return true; // 判定为重复提交
                }
            }
        }
        
        // 5. 如果不是重复提交，则将本次请求的数据存入 Redis
        // 设置过期时间为注解指定的间隔时间，确保缓存能自动清理
        Map<String, Object> cacheMap = new HashMap<>();
        cacheMap.put(REPEAT_PARAMS, nowParams);
        cacheMap.put(REPEAT_TIME, System.currentTimeMillis());
        redisService.setCacheObject(cacheRepeatKey, cacheMap, annotation.interval(), TimeUnit.MILLISECONDS);
        
        return false;
    }

    /**
     * 判断两次请求的参数是否相同
     * 目前采用简单的字符串相等比对
     */
    private boolean compareParams(String nowParams, String preParams) {
        return StringUtils.equals(nowParams, preParams);
    }
}
