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
 * 判断请求url和数据是否和上一次相同，
 * 如果对应的数据和上一次相同，且时间间隔小于设定的间隔时间，则视为重复提交
 *
 * @author wuhuaming
 */
@Component
public class RedisRepeatSubmitProvider {
    public final String REPEAT_PARAMS = "repeatParams";

    public final String REPEAT_TIME = "repeatTime";

    /**
     * 防重提交 redis key
     */
    public final String REPEAT_SUBMIT_KEY = "repeat_submit:";

    @Autowired
    private RedisService redisService;

    public boolean isRepeatSubmit(HttpServletRequest request, RepeatSubmit annotation) {
        String nowParams = JsonUtil.toJson(request.getParameterMap());

        // url + token + userId
        String url = request.getRequestURI();
        String header = request.getHeader(CommonConstants.TOKEN_HEADER);
        Long userId = SecurityUtils.getUserId();

        String cacheRepeatKey = REPEAT_SUBMIT_KEY + url + ":" + StringUtils.nvl(header, "") + ":" + userId;

        Map<String, Object> sessionMap = redisService.getCacheObject(cacheRepeatKey);
        if (sessionMap != null) {
            if (sessionMap.containsKey(REPEAT_PARAMS)) {
                String preParams = (String) sessionMap.get(REPEAT_PARAMS);
                long preTime = (long) sessionMap.get(REPEAT_TIME);
                if (compareParams(nowParams, preParams) && (System.currentTimeMillis() - preTime) < annotation.interval()) {
                    return true;
                }
            }
        }
        Map<String, Object> cacheMap = new HashMap<>();
        cacheMap.put(REPEAT_PARAMS, nowParams);
        cacheMap.put(REPEAT_TIME, System.currentTimeMillis());
        redisService.setCacheObject(cacheRepeatKey, cacheMap, annotation.interval(), TimeUnit.MILLISECONDS);
        return false;
    }

    /**
     * 判断参数是否相同
     */
    private boolean compareParams(String nowParams, String preParams) {
        return StringUtils.equals(nowParams, preParams);
    }
}
