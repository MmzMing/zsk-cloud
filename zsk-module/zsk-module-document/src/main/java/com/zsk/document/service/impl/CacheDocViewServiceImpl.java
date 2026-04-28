package com.zsk.document.service.impl;

import com.zsk.common.core.constant.CacheConstants;
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
 * 基于 Redis Hash 实现高性能浏览量统计：
 * <ul>
 *     <li><b>Hash:</b> stat:{targetType}:{targetId} — 使用哈希存储浏览量计数，HINCRBY 原子自增</li>
 *     <li><b>Lock:</b> view:lock:{userId}:{targetId}:{typeCode} — SETNX 实现防短时间重复浏览</li>
 * </ul>
 * <p>
 * <b>核心设计要点：</b>
 * <ul>
 *     <li>浏览量无需记录用户级别状态，直接 HINCRBY 自增，性能最优</li>
 *     <li>登录用户使用 SETNX 锁防重复（默认 5 分钟内同一内容只计一次）</li>
 *     <li>匿名用户不做去重限制</li>
 *     <li>先写 Redis，定时任务异步同步到数据库</li>
 *     <li>查询时先查 Redis，Redis 未命中则查 DB 并回写缓存</li>
 * </ul>
 *
 * @author wuhuaming
 * @version 3.0
 * @date 2026-04-25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheDocViewServiceImpl implements ICacheDocViewService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final DocUserInteractionMapper docUserInteractionMapper;

    private static final long LOCK_EXPIRE_SECONDS = 300;

    /**
     * 记录内容浏览
     * <p>
     * 使用 Redis Hash 实现原子浏览量统计：
     * 1. 登录用户使用 SETNX 锁防短时间重复浏览（默认 5 分钟）
     * 2. 匿名用户直接计数
     * 3. HINCRBY 原子自增浏览量
     *
     * @param type     浏览类型（见 {@link CacheDocViewTypeEnum}）
     * @param targetId 目标内容ID（笔记ID/视频ID）
     * @param userId   用户ID（可为 null，表示匿名用户）
     * @return true-计数成功，false-重复浏览或参数无效
     */
    @Override
    public boolean view(Integer type, Long targetId, Long userId) {
        // 参数校验
        CacheDocViewTypeEnum viewType = CacheDocViewTypeEnum.getByCode(type);
        if (viewType == null || targetId == null) {
            log.warn("浏览参数无效: type={}, targetId={}", type, targetId);
            return false;
        }

        // 登录用户使用 SETNX 防重复浏览（5分钟内同一内容只计一次）
        if (userId != null) {
            String lockKey = buildLockKey(userId, targetId, viewType);
            // SETNX：只有当 key 不存在时才设置成功，返回 true
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, true, LOCK_EXPIRE_SECONDS, TimeUnit.SECONDS);
            if (Boolean.FALSE.equals(locked)) {
                // 已存在锁，说明短时间内已浏览过
                return false;
            }
        }

        // 更新浏览量计数：stat:{targetType}:{targetId} 的 view:{typeCode} 字段
        String statKey = buildStatKey(viewType, targetId);
        String countField = buildCountField(viewType);
        redisTemplate.opsForHash().increment(statKey, countField, 1);

        log.debug("浏览 {} targetId={}, userId={}", viewType.getDesc(), targetId, userId);
        return true;
    }

    /**
     * 获取目标内容的浏览量
     * <p>
     * 缓存策略：先查 Redis，未命中则查数据库并回写缓存
     *
     * @param type     浏览类型
     * @param targetId 目标内容ID
     * @return 浏览量
     */
    @Override
    public Long getViewCount(Integer type, Long targetId) {
        CacheDocViewTypeEnum viewType = CacheDocViewTypeEnum.getByCode(type);
        if (viewType == null || targetId == null) {
            return 0L;
        }

        // 先查 Redis 缓存
        String statKey = buildStatKey(viewType, targetId);
        String countField = buildCountField(viewType);
        Object count = redisTemplate.opsForHash().get(statKey, countField);
        if (count != null) {
            return Long.parseLong(count.toString());
        }

        // Redis 未命中，查数据库
        Integer targetType = getTargetType(viewType);
        if (targetType == null) {
            return 0L;
        }
        Long dbCount = docUserInteractionMapper.countByTarget(targetType, targetId, DocUserInteractionContext.INTERACTION_TYPE_VIEW);

        // 回写缓存（仅当有数据时）
        if (dbCount != null && dbCount > 0) {
            redisTemplate.opsForHash().put(statKey, countField, dbCount.toString());
        }
        return dbCount != null ? dbCount : 0L;
    }

    /**
     * 批量获取目标内容的浏览量
     *
     * @param type      浏览类型
     * @param targetIds 目标内容ID列表
     * @return 目标ID到浏览量的映射
     */
    @Override
    public Map<Long, Long> getViewCountBatch(Integer type, Iterable<Long> targetIds) {
        Map<Long, Long> result = new HashMap<>();
        if (targetIds == null) {
            return result;
        }
        for (Long targetId : targetIds) {
            result.put(targetId, getViewCount(type, targetId));
        }
        return result;
    }

    /**
     * 同步浏览数据从 Redis 到数据库
     * <p>
     * 执行流程：
     * 1. 扫描所有 stat:* 键
     * 2. 读取 view:* 字段的浏览量计数
     * 3. 将计数写入数据库
     */
    @Override
    public void syncViewDataToDb() {
        log.info("开始同步浏览数据到数据库...");
        int syncCount = 0;

        String pattern = CacheConstants.CACHE_STAT + "*";
        Collection<String> keys = redisTemplate.keys(pattern);
        if (keys == null || keys.isEmpty()) {
            log.info("没有需要同步的浏览数据");
            return;
        }

        for (String statKey : keys) {
            try {
                // 解析 Stat Key 获取 targetType 和 targetId
                Long[] parsed = extractStatKey(statKey);
                if (parsed == null) {
                    continue;
                }
                int targetType = parsed[0].intValue();
                long targetId = parsed[1];

                // 遍历 Hash 的所有字段，只处理浏览量字段
                Map<Object, Object> entries = redisTemplate.opsForHash().entries(statKey);
                for (Map.Entry<Object, Object> entry : entries.entrySet()) {
                    String field = entry.getKey().toString();
                    // 只处理浏览量相关字段
                    if (!field.startsWith("view:")) {
                        continue;
                    }
                    long viewCount = Long.parseLong(entry.getValue().toString());
                    saveViewCountToDb(targetType, targetId, viewCount);
                    syncCount++;
                }
            } catch (Exception e) {
                log.warn("同步浏览数据失败: key={}", statKey, e);
            }
        }

        log.info("浏览数据同步完成，共处理 {} 条记录", syncCount);
    }

    /**
     * 构建统计 Hash Key
     *
     * @param type     浏览类型
     * @param targetId 目标内容ID
     * @return Key 格式: stat:{targetType}:{targetId}
     */
    private String buildStatKey(CacheDocViewTypeEnum type, Long targetId) {
        return CacheConstants.CACHE_STAT + getTargetType(type) + ":" + targetId;
    }

    /**
     * 构建计数字段名
     *
     * @param type 浏览类型
     * @return 字段名格式: view:{typeCode}
     */
    private String buildCountField(CacheDocViewTypeEnum type) {
        return "view:" + type.getCode();
    }

    /**
     * 构建防重复浏览锁 Key
     *
     * @param userId   用户ID
     * @param targetId 目标内容ID
     * @param type     浏览类型
     * @return Key 格式: view:lock:{userId}:{targetId}:{typeCode}
     */
    private String buildLockKey(Long userId, Long targetId, CacheDocViewTypeEnum type) {
        return CacheConstants.CACHE_VIEW_LOCK + userId + ":" + targetId + ":" + type.getCode();
    }

    /**
     * 从 Stat Key 中解析 targetType 和 targetId
     *
     * @param key Redis Key
     * @return [targetType, targetId]
     */
    private Long[] extractStatKey(String key) {
        try {
            String[] parts = key.split(":");
            if (parts.length >= 4) {
                return new Long[]{Long.parseLong(parts[2]), Long.parseLong(parts[3])};
            }
        } catch (Exception e) {
            log.warn("解析 stat 键失败: {}", key, e);
        }
        return null;
    }

    /**
     * 将浏览类型转换为目标类型
     *
     * @param viewType 浏览类型
     * @return 目标类型
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

    /**
     * 保存浏览量计数到数据库
     *
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @param viewCount  浏览量
     */
    private void saveViewCountToDb(Integer targetType, Long targetId, Long viewCount) {
        // userId=0 表示这是一条统计记录，而非用户级记录
        DocUserInteraction existing = docUserInteractionMapper.selectByUserAndTarget(
                0L, targetType, targetId, DocUserInteractionContext.INTERACTION_TYPE_VIEW);
        if (existing != null) {
            existing.setStatus(viewCount.intValue());
            docUserInteractionMapper.updateById(existing);
        } else {
            DocUserInteraction interaction = new DocUserInteraction();
            interaction.setUserId(0L);
            interaction.setTargetType(targetType);
            interaction.setTargetId(targetId);
            interaction.setInteractionType(DocUserInteractionContext.INTERACTION_TYPE_VIEW);
            interaction.setStatus(viewCount.intValue());
            docUserInteractionMapper.insert(interaction);
        }
    }

    /**
     * 从数据库预热浏览量缓存
     * <p>
     * <b>预热目的：</b>服务重启后，将数据库中的浏览量数据加载到Redis缓存，恢复缓存状态。
     * <p>
     * <b>浏览量设计特殊说明：</b>
     * <ul>
     *     <li>浏览量无需记录用户级别状态，直接使用Hash存储计数，性能最优</li>
     *     <li>登录用户使用SETNX锁防短时间重复浏览（默认5分钟内同一内容只计一次）</li>
     *     <li>匿名用户不做去重限制</li>
     * </ul>
     * <p>
     * <b>数据模型说明：</b>
     * <ul>
     *     <li>数据库中使用userId=0的记录存储浏览量统计值（status字段）</li>
     *     <li>预热时通过countByTarget重新统计，而非直接读取userId=0的记录</li>
     * </ul>
     * <p>
     * <b>预热流程：</b>
     * <ol>
     *     <li>预热浏览量计数到Hash：通过countByTarget重新统计，保证数据准确性</li>
     * </ol>
     *
     * @param type     浏览类型
     * @param targetId 目标ID
     */
    @Override
    public void warmViewCacheFromDb(Integer type, Long targetId) {
        // 参数校验
        CacheDocViewTypeEnum viewType = CacheDocViewTypeEnum.getByCode(type);
        if (viewType == null || targetId == null) {
            log.warn("预热缓存参数无效: type={}, targetId={}", type, targetId);
            return;
        }

        // 获取目标类型映射
        Integer targetType = getTargetType(viewType);
        if (targetType == null) {
            return;
        }

        // ========== 预热浏览量计数到Hash ==========
        // Hash Key: stat:{targetType}:{targetId}
        // Field: view:{typeCode} -> 浏览量计数值
        // 
        // 浏览量无需用户级状态记录，只需存储统计值，因此只有Hash预热，没有Bitmap预热
        String statKey = buildStatKey(viewType, targetId);
        String countField = buildCountField(viewType);

        // 通过countByTarget重新统计，而不是读取userId=0的统计记录
        // 这样可以保证数据一致性，避免因异常导致统计记录与实际不一致
        Long dbCount = docUserInteractionMapper.countByTarget(targetType, targetId, DocUserInteractionContext.INTERACTION_TYPE_VIEW);
        if (dbCount != null && dbCount > 0) {
            redisTemplate.opsForHash().put(statKey, countField, dbCount.toString());
            log.debug("浏览量计数预热完成: targetId={}, count={}", targetId, dbCount);
        }

        log.info("浏览量缓存预热完成: type={}, targetId={}", type, targetId);
    }
}
