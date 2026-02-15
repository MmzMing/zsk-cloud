package com.zsk.common.core.context;

/**
 * 缓存上下文 - 缓存相关配置
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
public class CacheContext {

    /** 默认缓存过期时间（小时） */
    private static final long DEFAULT_CACHE_EXPIRE_HOURS = 24;

    /** 视频分类缓存过期时间（小时） */
    private static long videoCategoryCacheExpireHours = DEFAULT_CACHE_EXPIRE_HOURS;

    /** 视频标签缓存过期时间（小时） */
    private static long videoTagCacheExpireHours = DEFAULT_CACHE_EXPIRE_HOURS;

    private CacheContext() {
        throw new AssertionError("工具类禁止实例化");
    }

    /**
     * 获取视频分类缓存过期时间（小时）
     *
     * @return 过期时间
     */
    public static long getVideoCategoryCacheExpireHours() {
        return videoCategoryCacheExpireHours;
    }

    /**
     * 设置视频分类缓存过期时间（小时）
     *
     * @param hours 过期时间
     */
    public static void setVideoCategoryCacheExpireHours(long hours) {
        videoCategoryCacheExpireHours = hours;
    }

    /**
     * 获取视频标签缓存过期时间（小时）
     *
     * @return 过期时间
     */
    public static long getVideoTagCacheExpireHours() {
        return videoTagCacheExpireHours;
    }

    /**
     * 设置视频标签缓存过期时间（小时）
     *
     * @param hours 过期时间
     */
    public static void setVideoTagCacheExpireHours(long hours) {
        videoTagCacheExpireHours = hours;
    }
}
