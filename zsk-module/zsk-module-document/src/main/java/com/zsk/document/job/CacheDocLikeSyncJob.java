package com.zsk.document.job;

import com.xxl.job.core.handler.annotation.XxlJob;
import com.zsk.common.xxljob.annotation.XxlJobAutoRegister;
import com.zsk.document.service.ICacheDocLikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 缓存文档点赞数据同步定时任务
 * <p>
 * 每5分钟将Redis中的点赞数据同步到数据库
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheDocLikeSyncJob {

    /**
     * 点赞服务
     */
    private final ICacheDocLikeService cacheDocLikeService;

    /**
     * 同步点赞数据到数据库
     * 每5分钟执行一次
     */
    @XxlJob("likeSyncJob")
    @XxlJobAutoRegister(
            name = "点赞数据同步任务",
            description = "每5分钟同步Redis点赞数据到数据库",
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
}
