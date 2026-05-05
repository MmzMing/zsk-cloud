package com.zsk.document.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.zsk.common.core.constant.CacheConstants;
import com.zsk.document.domain.DocUserInteraction;
import com.zsk.document.domain.context.DocUserInteractionContext;
import com.zsk.document.enums.CacheDocCollectTypeEnum;
import com.zsk.document.mapper.DocUserInteractionMapper;
import com.zsk.document.service.ICacheDocCollectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 缓存文档收藏服务实现
 *
 * <p>Redis 结构：
 * <ul>
 *   <li>用户维度 Set — {@code zsk:collect:user:{userId}:{typeCode}} → Set&lt;targetId&gt;，TTL=7d</li>
 *   <li>计数 Hash  — {@code zsk:stat:{targetType}:{targetId}} → Hash {collect:{typeCode}: count}</li>
 *   <li>待同步队列 — {@code zsk:collect:pending} → Hash {userId:typeCode:targetId: 1/0}</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheDocCollectServiceImpl implements ICacheDocCollectService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final DocUserInteractionMapper docUserInteractionMapper;

    /**
     * 收藏目标
     *
     * @param type     收藏类型编码
     * @param targetId 目标ID
     * @param userId   用户ID
     * @return true-收藏成功，false-参数无效或已收藏
     */
    @Override
    public boolean collect(Integer type, Long targetId, Long userId) {
        CacheDocCollectTypeEnum collectType = CacheDocCollectTypeEnum.getByCode(type);
        if (collectType == null || targetId == null || userId == null) {
            return false;
        }
        String userKey = buildUserKey(collectType, userId);
        String targetStr = String.valueOf(targetId);

        // 幂等校验：已收藏则直接返回
        Boolean isMember = redisTemplate.opsForSet().isMember(userKey, targetStr);
        if (Boolean.TRUE.equals(isMember)) {
            return false;
        }

        // 写入用户维度 Set 并刷新 TTL
        redisTemplate.opsForSet().add(userKey, targetStr);
        redisTemplate.expire(userKey, CacheConstants.CACHE_INTERACTION_TTL_SECONDS, TimeUnit.SECONDS);

        // 目标维度计数 +1
        redisTemplate.opsForHash().increment(buildStatKey(collectType, targetId), buildCountField(collectType), 1);

        // 写入待同步队列，值为 "1" 表示收藏状态
        redisTemplate.opsForHash().put(
                CacheConstants.CACHE_COLLECT_PENDING,
                buildPendingField(userId, collectType, targetId),
                "1");

        log.debug("收藏: userId={}, type={}, targetId={}", userId, type, targetId);
        return true;
    }

    /**
     * 取消收藏
     *
     * @param type     收藏类型编码
     * @param targetId 目标ID
     * @param userId   用户ID
     * @return true-取消成功，false-参数无效或未收藏
     */
    @Override
    public boolean uncollect(Integer type, Long targetId, Long userId) {
        CacheDocCollectTypeEnum collectType = CacheDocCollectTypeEnum.getByCode(type);
        if (collectType == null || targetId == null || userId == null) {
            return false;
        }
        String userKey = buildUserKey(collectType, userId);
        String targetStr = String.valueOf(targetId);

        // 校验：未收藏则无需取消
        Boolean isMember = redisTemplate.opsForSet().isMember(userKey, targetStr);
        if (!Boolean.TRUE.equals(isMember)) {
            return false;
        }

        // 从用户维度 Set 移除
        redisTemplate.opsForSet().remove(userKey, targetStr);

        // 目标维度计数 -1
        redisTemplate.opsForHash().increment(buildStatKey(collectType, targetId), buildCountField(collectType), -1);

        // 写入待同步队列，值为 "0" 表示取消收藏
        redisTemplate.opsForHash().put(
                CacheConstants.CACHE_COLLECT_PENDING,
                buildPendingField(userId, collectType, targetId),
                "0");

        log.debug("取消收藏: userId={}, type={}, targetId={}", userId, type, targetId);
        return true;
    }

    /**
     * 获取用户收藏总数（查库）
     *
     * @param userId 用户ID
     * @return 收藏总数
     */
    @Override
    public Long getUserCollectCount(Long userId) {
        if (userId == null) {
            return 0L;
        }
        Long count = docUserInteractionMapper.countByUser(userId, DocUserInteractionContext.INTERACTION_TYPE_FAVORITE);
        return count != null ? count : 0L;
    }

    /**
     * 获取目标收藏数（先查缓存，未命中回源数据库并回填）
     *
     * @param type     收藏类型编码
     * @param targetId 目标ID
     * @return 收藏数
     */
    @Override
    public Long getCollectCount(Integer type, Long targetId) {
        CacheDocCollectTypeEnum collectType = CacheDocCollectTypeEnum.getByCode(type);
        if (collectType == null || targetId == null) {
            return 0L;
        }
        // 优先从缓存读取计数
        Object count = redisTemplate.opsForHash().get(buildStatKey(collectType, targetId), buildCountField(collectType));
        if (count != null) {
            return Long.parseLong(count.toString());
        }
        // 缓存未命中，回源数据库
        Integer targetType = getTargetType(collectType);
        if (targetType == null) {
            return 0L;
        }
        Long dbCount = docUserInteractionMapper.countByTarget(targetType, targetId, DocUserInteractionContext.INTERACTION_TYPE_FAVORITE);
        // 回填缓存，仅当计数 > 0 时写入
        if (dbCount != null && dbCount > 0) {
            redisTemplate.opsForHash().put(buildStatKey(collectType, targetId), buildCountField(collectType), dbCount.toString());
        }
        return dbCount != null ? dbCount : 0L;
    }

    /**
     * 判断用户是否已收藏该目标（先查缓存，未命中查库）
     *
     * @param type     收藏类型编码
     * @param targetId 目标ID
     * @param userId   用户ID
     * @return true-已收藏
     */
    @Override
    public boolean hasCollected(Integer type, Long targetId, Long userId) {
        CacheDocCollectTypeEnum collectType = CacheDocCollectTypeEnum.getByCode(type);
        if (collectType == null || targetId == null || userId == null) {
            return false;
        }
        // 优先从缓存判断
        Boolean hit = redisTemplate.opsForSet().isMember(buildUserKey(collectType, userId), String.valueOf(targetId));
        if (Boolean.TRUE.equals(hit)) {
            return true;
        }
        // 缓存未命中，查库确认（status=1 表示有效收藏）
        Integer targetType = getTargetType(collectType);
        DocUserInteraction record = docUserInteractionMapper.selectByUserAndTarget(
                userId, targetType, targetId, DocUserInteractionContext.INTERACTION_TYPE_FAVORITE);
        return record != null && record.getStatus() == 1;
    }

    /**
     * 批量获取目标收藏数
     *
     * @param type      收藏类型编码
     * @param targetIds 目标ID集合
     * @return targetId → 收藏数
     */
    @Override
    public Map<Long, Long> getCollectCountBatch(Integer type, Iterable<Long> targetIds) {
        Map<Long, Long> result = new HashMap<>();
        if (targetIds == null) {
            return result;
        }
        for (Long targetId : targetIds) {
            result.put(targetId, getCollectCount(type, targetId));
        }
        return result;
    }

    /**
     * 将 Redis 待同步队列中的收藏数据批量写入数据库（rename → 读取 → 删除 → upsert）
     */
    @Override
    public void syncCollectDataToDb() {
        log.info("开始同步收藏数据...");
        // 检查待同步队列是否存在
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(CacheConstants.CACHE_COLLECT_PENDING))) {
            log.info("收藏 pending 队列不存在，跳过");
            return;
        }
        // 原子切换：rename 确保后续新写入到新队列，当前队列独占处理
        String processingKey = CacheConstants.CACHE_COLLECT_PENDING + ":processing:" + System.currentTimeMillis();
        redisTemplate.rename(CacheConstants.CACHE_COLLECT_PENDING, processingKey);

        // 读取并立即删除，避免重复处理
        Map<Object, Object> pending = redisTemplate.opsForHash().entries(processingKey);
        redisTemplate.delete(processingKey);

        if (pending.isEmpty()) {
            return;
        }

        // 解析 pending 记录，构建批量 upsert 列表
        List<DocUserInteraction> batch = new ArrayList<>(pending.size());
        for (Map.Entry<Object, Object> entry : pending.entrySet()) {
            // Hash field 格式：{userId}:{typeCode}:{targetId}
            String[] parts = entry.getKey().toString().split(":");
            if (parts.length != 3) {
                continue;
            }
            try {
                Long userId = Long.parseLong(parts[0]);
                Integer typeCode = Integer.parseInt(parts[1]);
                Long targetId = Long.parseLong(parts[2]);
                // Hash value：1=收藏，0=取消收藏
                Integer status = Integer.parseInt(entry.getValue().toString());

                CacheDocCollectTypeEnum collectType = CacheDocCollectTypeEnum.getByCode(typeCode);
                if (collectType == null) {
                    continue;
                }
                Integer targetType = getTargetType(collectType);
                if (targetType == null) {
                    continue;
                }

                DocUserInteraction record = new DocUserInteraction();
                record.setId(IdWorker.getId());
                record.setUserId(userId);
                record.setTargetType(targetType);
                record.setTargetId(targetId);
                record.setInteractionType(DocUserInteractionContext.INTERACTION_TYPE_FAVORITE);
                record.setStatus(status);
                batch.add(record);
            } catch (Exception e) {
                log.warn("解析 pending 记录失败: {}", entry.getKey(), e);
            }
        }

        // 批量写入数据库（INSERT ON DUPLICATE KEY UPDATE）
        if (!batch.isEmpty()) {
            docUserInteractionMapper.batchUpsert(batch);
            log.info("收藏同步完成，共 {} 条", batch.size());
        }
    }

    /**
     * 从数据库预热收藏缓存（重建用户维度 Set + 计数 Hash）
     *
     * @param type     收藏类型编码
     * @param targetId 目标ID
     */
    @Override
    public void warmCollectCacheFromDb(Integer type, Long targetId) {
        CacheDocCollectTypeEnum collectType = CacheDocCollectTypeEnum.getByCode(type);
        if (collectType == null || targetId == null) {
            return;
        }
        Integer targetType = getTargetType(collectType);
        if (targetType == null) {
            return;
        }

        // 查询所有收藏了该目标的用户，重建各自的用户维度 Set
        List<DocUserInteraction> records = docUserInteractionMapper.selectByTarget(
                targetType, targetId, DocUserInteractionContext.INTERACTION_TYPE_FAVORITE);

        if (records != null && !records.isEmpty()) {
            for (DocUserInteraction record : records) {
                if (record.getUserId() == null || record.getUserId() <= 0) {
                    continue;
                }
                String userKey = buildUserKey(collectType, record.getUserId());
                redisTemplate.opsForSet().add(userKey, String.valueOf(targetId));
                redisTemplate.expire(userKey, CacheConstants.CACHE_INTERACTION_TTL_SECONDS, TimeUnit.SECONDS);
            }
        }

        // 重建目标维度计数 Hash
        Long dbCount = docUserInteractionMapper.countByTarget(targetType, targetId, DocUserInteractionContext.INTERACTION_TYPE_FAVORITE);
        if (dbCount != null && dbCount > 0) {
            redisTemplate.opsForHash().put(buildStatKey(collectType, targetId), buildCountField(collectType), dbCount.toString());
        }
        log.info("收藏缓存预热完成: type={}, targetId={}", type, targetId);
    }

    /**
     * 构建用户维度缓存 Key：zsk:collect:user:{userId}:{typeCode}
     */
    private String buildUserKey(CacheDocCollectTypeEnum type, Long userId) {
        return CacheConstants.CACHE_COLLECT_USER + userId + ":" + type.getCode();
    }

    /**
     * 构建统计维度缓存 Key：zsk:stat:{targetType}:{targetId}
     */
    private String buildStatKey(CacheDocCollectTypeEnum type, Long targetId) {
        return CacheConstants.CACHE_STAT + getTargetType(type) + ":" + targetId;
    }

    /**
     * 构建计数 Hash 字段：collect:{typeCode}
     */
    private String buildCountField(CacheDocCollectTypeEnum type) {
        return "collect:" + type.getCode();
    }

    /**
     * 构建待同步队列 Hash 字段：{userId}:{typeCode}:{targetId}
     */
    private String buildPendingField(Long userId, CacheDocCollectTypeEnum type, Long targetId) {
        return userId + ":" + type.getCode() + ":" + targetId;
    }

    /**
     * 根据收藏类型枚举映射数据库目标类型
     *
     * @param collectType 收藏类型枚举
     * @return 目标类型，未知类型返回 null
     */
    private Integer getTargetType(CacheDocCollectTypeEnum collectType) {
        switch (collectType) {
            case NOTE:
                return DocUserInteractionContext.TARGET_TYPE_NOTE;
            case VIDEO:
                return DocUserInteractionContext.TARGET_TYPE_VIDEO;
            default:
                return null;
        }
    }
}
