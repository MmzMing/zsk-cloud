package com.zsk.document.job;

import com.xxl.job.core.handler.annotation.XxlJob;
import com.zsk.common.xxljob.annotation.XxlJobAutoRegister;
import com.zsk.document.service.ICacheDocCollectService;
import com.zsk.document.service.ICacheDocFollowService;
import com.zsk.document.service.ICacheDocLikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 缓存文档社交数据同步定时任务
 * <p>
 * 每5分钟将Redis中的点赞、收藏和关注数据同步到数据库
 * 同时写入DocUserInteraction表和更新目标表的计数
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheDocSocialSyncJob {

    private final ICacheDocLikeService cacheDocLikeService;
    private final ICacheDocCollectService cacheDocCollectService;
    private final ICacheDocFollowService cacheDocFollowService;

    /**
     * 同步点赞数据到数据库
     * 每5分钟执行一次
     */
    @XxlJob("likeSyncJob")
    @XxlJobAutoRegister(
            name = "点赞数据同步任务",
            description = "每5分钟同步Redis点赞数据到数据库，同时写入DocUserInteraction表",
            cron = "0 0/5 * * * ?",
            author = "wuhuaming"
    )
    public void syncLikeData() {
        log.info("开始执行点赞数据同步定时任务...");
        try {
            cacheDocLikeService.syncLikeDataToDb();
            log.info("点赞数据同步定时任务执行完成");
        } catch (Exception e) {
            log.error("点赞数据同步定时任务执行失败", e);
        }
    }

    /**
     * 同步收藏数据到数据库
     * 每5分钟执行一次
     */
    @XxlJob("collectSyncJob")
    @XxlJobAutoRegister(
            name = "收藏数据同步任务",
            description = "每5分钟同步Redis收藏数据到数据库，同时写入DocUserInteraction表",
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
            description = "每5分钟同步Redis关注数据到数据库，同时写入DocUserInteraction表",
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
