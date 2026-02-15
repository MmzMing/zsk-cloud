package com.zsk.document.job;

import com.xxl.job.core.handler.annotation.XxlJob;
import com.zsk.common.xxljob.annotation.XxlJobAutoRegister;
import com.zsk.document.service.ICacheDocCollectService;
import com.zsk.document.service.ICacheDocFollowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 缓存文档社交数据同步定时任务
 * <p>
 * 每5分钟将Redis中的收藏和关注数据同步到数据库
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheDocSocialSyncJob {

    /**
     * 收藏服务
     */
    private final ICacheDocCollectService cacheDocCollectService;

    /**
     * 关注服务
     */
    private final ICacheDocFollowService cacheDocFollowService;

    /**
     * 同步收藏数据到数据库
     * 每5分钟执行一次
     */
    @XxlJob("collectSyncJob")
    @XxlJobAutoRegister(
            name = "收藏数据同步任务",
            description = "每5分钟同步Redis收藏数据到数据库",
            cron = "0 0/5 * * * ?",
            author = "wuhuaming"
    )
    public void syncCollectData() {
        log.info("开始执行收藏数据同步定时任务...");
        try {
            cacheDocCollectService.syncCollectDataToDb();
            log.info("收藏数据同步定时任务执行完成");
        } catch (Exception e) {
            log.error("收藏数据同步定时任务执行失败", e);
        }
    }

    /**
     * 同步关注数据到数据库
     * 每5分钟执行一次
     */
    @XxlJob("followSyncJob")
    @XxlJobAutoRegister(
            name = "关注数据同步任务",
            description = "每5分钟同步Redis关注数据到数据库",
            cron = "0 0/5 * * * ?",
            author = "wuhuaming"
    )
    public void syncFollowData() {
        log.info("开始执行关注数据同步定时任务...");
        try {
            cacheDocFollowService.syncFollowDataToDb();
            log.info("关注数据同步定时任务执行完成");
        } catch (Exception e) {
            log.error("关注数据同步定时任务执行失败", e);
        }
    }
}
