package com.zsk.document.service.impl;

import com.zsk.common.core.constant.CacheConstants;
import com.zsk.common.redis.service.RedisService;
import com.zsk.document.domain.DocUserInteraction;
import com.zsk.document.domain.context.DocUserInteractionContext;
import com.zsk.document.enums.CacheDocViewTypeEnum;
import com.zsk.document.mapper.DocUserInteractionMapper;
import com.zsk.document.service.ICacheDocViewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 缓存文档浏览服务实现类
 * <p>
 * 使用Redis键实现浏览量统计功能：
 * 1. view:count:{target_id}:{type} String 存储内容的实时浏览数
 * 2. view:lock:{user_id}:{target_id} String 分布式锁（防止重复浏览，过期时间5分钟）
 * <p>
 * 浏览量数据先写入Redis，后由定时任务 {@link com.zsk.document.job.CacheDocSocialSyncJob} 同步到数据库。
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheDocViewServiceImpl implements ICacheDocViewService {

    /**
     * Redis服务
     */
    private final RedisService redisService;

    /**
     * Redis模板
     */
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 用户交互Mapper
     */
    private final DocUserInteractionMapper docUserInteractionMapper;

    /**
     * 分布式锁过期时间（秒），5分钟内同一用户重复浏览只计一次
     */
    private static final long LOCK_EXPIRE_SECONDS = 300;

    /**
     * 增加浏览量
     * <p>
     * 用户浏览内容时调用，增加对应目标的浏览计数。
     * 如果用户已登录，会检查5分钟内的重复浏览；未登录用户直接增加计数。
     * </p>
     *
     * @param type     浏览类型（1-笔记 2-视频）
     * @param targetId 目标ID（笔记ID或视频ID）
     * @param userId   用户ID（可选，为空时表示匿名浏览）
     * @return 是否成功（重复浏览返回false）
     */
    @Override
    public boolean view(Integer type, Long targetId, Long userId) {
        // 1. 验证参数并获取浏览类型枚举
        CacheDocViewTypeEnum viewType = CacheDocViewTypeEnum.getByCode(type);
        if (viewType == null || targetId == null) {
            log.warn("浏览参数无效: type={}, targetId={}", type, targetId);
            return false;
        }

        // 2. 构建浏览计数Redis键
        String countKey = buildCountKey(targetId, viewType);

        // 3. 如果用户已登录，检查是否短时间内重复浏览
        if (userId != null) {
            String lockKey = buildLockKey(userId, targetId);
            Boolean locked = redisService.getCacheObject(lockKey);
            if (Boolean.TRUE.equals(locked)) {
                log.debug("用户 {} 短时间内重复浏览目标 {}", userId, targetId);
                return false;
            }
            // 设置浏览锁，防止短时间内重复计数
            redisService.setCacheObject(lockKey, true, LOCK_EXPIRE_SECONDS, TimeUnit.SECONDS);
        }

        // 4. 增加浏览计数
        redisTemplate.opsForValue().increment(countKey, 1);
        log.debug("浏览 {} 类型目标 {}, 用户={}", viewType.getDesc(), targetId, userId);
        return true;
    }

    /**
     * 获取浏览数量
     * <p>
     * 从Redis获取实时浏览量，如缓存未命中则从数据库加载并写入缓存。
     * </p>
     *
     * @param type     浏览类型（1-笔记 2-视频）
     * @param targetId 目标ID
     * @return 浏览数量
     */
    @Override
    public Long getViewCount(Integer type, Long targetId) {
        // 1. 验证参数并获取浏览类型枚举
        CacheDocViewTypeEnum viewType = CacheDocViewTypeEnum.getByCode(type);
        if (viewType == null || targetId == null) {
            return 0L;
        }

        // 2. 构建浏览计数Redis键
        String countKey = buildCountKey(targetId, viewType);

        // 3. 尝试从Redis获取浏览量
        Object count = redisTemplate.opsForValue().get(countKey);
        if (count != null) {
            return Long.parseLong(count.toString());
        }

        // 4. 缓存未命中，从数据库加载
        Long dbCount = getViewCountFromDb(viewType, targetId);
        if (dbCount != null && dbCount > 0) {
            // 写入缓存，后续请求可直接从缓存读取
            redisService.setCacheObject(countKey, dbCount);
        }
        return dbCount != null ? dbCount : 0L;
    }

    /**
     * 批量获取浏览数量
     * <p>
     * 批量查询多个目标的浏览量。
     * </p>
     *
     * @param type      浏览类型
     * @param targetIds 目标ID列表
     * @return 目标ID与浏览数量的映射
     */
    @Override
    public Map<Long, Long> getViewCountBatch(Integer type, Iterable<Long> targetIds) {
        Map<Long, Long> result = new HashMap<>();
        // 1. 验证参数
        CacheDocViewTypeEnum viewType = CacheDocViewTypeEnum.getByCode(type);
        if (viewType == null || targetIds == null) {
            return result;
        }

        // 2. 逐个查询浏览量
        for (Long targetId : targetIds) {
            result.put(targetId, getViewCount(type, targetId));
        }
        return result;
    }

    /**
     * 同步浏览数据到数据库
     * <p>
     * 由定时任务调用，将Redis中的浏览量数据同步到数据库持久化。
     * 同步完成后，Redis中的计数会保留，继续累加新的浏览量。
     * </p>
     */
    @Override
    public void syncViewDataToDb() {
        log.info("开始同步浏览数据到数据库...");
        int syncCount = 0;

        // 1. 扫描所有浏览计数Redis键
        String pattern = CacheConstants.CACHE_VIEW_COUNT + "*";
        Collection<String> keys = redisService.keys(pattern);

        if (keys == null || keys.isEmpty()) {
            log.info("没有需要同步的浏览数据");
            return;
        }

        // 2. 遍历所有浏览计数键，同步到数据库
        for (String countKey : keys) {
            try {
                // 解析Redis键获取targetId和type
                String[] parts = countKey.split(":");
                if (parts.length >= 5) {
                    Long targetId = Long.parseLong(parts[3]);
                    Integer type = Integer.parseInt(parts[4]);
                    CacheDocViewTypeEnum viewType = CacheDocViewTypeEnum.getByCode(type);

                    if (viewType != null) {
                        // 获取当前浏览量并同步到数据库
                        Object count = redisTemplate.opsForValue().get(countKey);
                        if (count != null) {
                            Long viewCount = Long.parseLong(count.toString());
                            saveViewCountToDb(viewType, targetId, viewCount);
                            syncCount++;
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("同步浏览数据失败: key={}", countKey, e);
            }
        }

        log.info("浏览数据同步完成，共处理 {} 条记录", syncCount);
    }

    /**
     * 构建浏览计数键
     * <p>
     * Redis键格式: zsk:view:count:{targetId}:{typeCode}
     * </p>
     *
     * @param targetId 目标ID
     * @param type     浏览类型枚举
     * @return Redis键
     */
    private String buildCountKey(Long targetId, CacheDocViewTypeEnum type) {
        return CacheConstants.CACHE_VIEW_COUNT + targetId + ":" + type.getCode();
    }

    /**
     * 构建分布式锁键
     * <p>
     * Redis键格式: zsk:view:count:lock:{userId}:{targetId}
     * </p>
     *
     * @param userId   用户ID
     * @param targetId 目标ID
     * @return Redis键
     */
    private String buildLockKey(Long userId, Long targetId) {
        return CacheConstants.CACHE_VIEW_COUNT + "lock:" + userId + ":" + targetId;
    }

    /**
     * 从数据库获取浏览数量
     * <p>
     * 通过统计 doc_user_interaction 表中交互类型为浏览的记录数。
     * </p>
     *
     * @param type     浏览类型枚举
     * @param targetId 目标ID
     * @return 浏览数量
     */
    private Long getViewCountFromDb(CacheDocViewTypeEnum type, Long targetId) {
        Integer targetType = getTargetType(type);
        if (targetType == null) {
            return 0L;
        }
        return docUserInteractionMapper.countByTarget(
            targetType, targetId, DocUserInteractionContext.INTERACTION_TYPE_VIEW);
    }

    /**
     * 保存浏览数量到数据库
     * <p>
     * 将Redis中的浏览量同步到 doc_user_interaction 表。
     * 使用userId=0作为系统汇总记录。
     * </p>
     *
     * @param type      浏览类型枚举
     * @param targetId  目标ID
     * @param viewCount 浏览数量
     */
    private void saveViewCountToDb(CacheDocViewTypeEnum type, Long targetId, Long viewCount) {
        try {
            Integer targetType = getTargetType(type);
            if (targetType == null) {
                return;
            }

            // 查询是否已存在浏览汇总记录（userId=0表示系统汇总）
            DocUserInteraction existing = docUserInteractionMapper.selectByUserAndTarget(
                0L, targetType, targetId, DocUserInteractionContext.INTERACTION_TYPE_VIEW);

            if (existing != null) {
                // 更新已有记录的浏览量
                existing.setStatus(viewCount.intValue());
                docUserInteractionMapper.updateById(existing);
            } else {
                // 创建新的浏览汇总记录
                DocUserInteraction interaction = new DocUserInteraction();
                interaction.setUserId(0L);
                interaction.setTargetType(targetType);
                interaction.setTargetId(targetId);
                interaction.setInteractionType(DocUserInteractionContext.INTERACTION_TYPE_VIEW);
                interaction.setStatus(viewCount.intValue());
                docUserInteractionMapper.insert(interaction);
            }
            log.debug("同步浏览数据到数据库: type={}, targetId={}, count={}", type.getDesc(), targetId, viewCount);
        } catch (Exception e) {
            log.error("保存浏览数据失败: type={}, targetId={}", type, targetId, e);
        }
    }

    /**
     * 获取目标类型
     * <p>
     * 将浏览类型枚举转换为交互表中的目标类型编码。
     * </p>
     *
     * @param viewType 浏览类型枚举
     * @return 目标类型编码（1-笔记 2-视频）
     */
    private Integer getTargetType(CacheDocViewTypeEnum viewType) {
        switch (viewType) {
            case NOTE:
                return DocUserInteractionContext.TARGET_TYPE_NOTE;
            case VIDEO:
                return DocUserInteractionContext.TARGET_TYPE_VIDEO;
            default:
                return null;
        }
    }
}
