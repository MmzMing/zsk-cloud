package com.zsk.common.security.utils;

import com.zsk.common.core.constant.CacheConstants;
import com.zsk.common.core.utils.SpringUtil;
import com.zsk.common.redis.service.RedisService;

import java.util.Collection;

/**
 * 字典工具类
 *
 * @author wuhuaming
 */
public class DictUtils {

    /**
     * 设置字典缓存
     *
     * @param key        参数键
     * @param dictValues 字典值
     */
    public static void setDictCache(String key, Collection<?> dictValues) {
        getRedisService().setCacheObject(getCacheKey(key), dictValues);
    }

    /**
     * 获取字典缓存
     *
     * @param key 参数键
     * @return dictValues 字典值
     */
    public static <T> Collection<T> getDictCache(String key) {
        return getRedisService().getCacheObject(getCacheKey(key));
    }

    /**
     * 删除指定字典缓存
     *
     * @param key 字典键
     */
    public static void removeDictCache(String key) {
        getRedisService().deleteObject(getCacheKey(key));
    }

    /**
     * 清空字典缓存
     */
    public static void clearDictCache() {
        Collection<String> keys = getRedisService().keys(CacheConstants.DICT_KEY + "*");
        getRedisService().deleteObject(keys);
    }

    /**
     * 设置cache key
     *
     * @param configKey 参数键
     * @return 缓存键key
     */
    public static String getCacheKey(String configKey) {
        return CacheConstants.DICT_KEY + configKey;
    }

    private static RedisService getRedisService() {
        return SpringUtil.getBean(RedisService.class);
    }
}
