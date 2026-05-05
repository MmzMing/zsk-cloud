package com.zsk.document.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.zsk.common.core.constant.CacheConstants;
import com.zsk.document.domain.DocUserInteraction;
import com.zsk.document.domain.context.DocUserInteractionContext;
import com.zsk.document.enums.CacheDocLikeTypeEnum;
import com.zsk.document.mapper.DocUserInteractionMapper;
import com.zsk.document.service.ICacheDocLikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 缓存文档点赞服务实现
 *
 * <p>Redis 结构：
 * <ul>
 *   <li>用户维度 Set — {@code zsk:like:user:{userId}:{typeCode}} → Set&lt;targetId&gt;，TTL=7d</li>
 *   <li>计数 Hash  — {@code zsk:stat:{targetType}:{targetId}} → Hash {like:{typeCode}: count}</li>
 *   <li>待同步队列 — {@code zsk:like:pending} → Hash {userId:typeCode:targetId: 1/0}</li>
 * </ul>
 *
 * <p>写操作原子性：SISMEMBER+SADD 非原子，但并发重复点赞最多导致计数多加 1，
 * XXL-Job 同步后以 DB 记录为准，可接受；若需严格幂等可加 Redisson 锁。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheDocLikeServiceImpl implements ICacheDocLikeService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final DocUserInteractionMapper docUserInteractionMapper;

    /**
     * 点赞目标
     *
     * @param type     点赞类型编码
     * @param targetId 目标ID
     * @param userId   用户ID
     * @return true-点赞成功，false-参数无效或已点赞
     */
    @Override
    public boolean like(Integer type, Long targetId, Long userId) {
        CacheDocLikeTypeEnum likeType = CacheDocLikeTypeEnum.getByCode(type);
        if (likeType == null || targetId == null || userId == null) {
            return false;
        }
        String userKey = buildUserKey(likeType, userId);
        String targetStr = String.valueOf(targetId);

        // 幂等校验：已点赞则直接返回
        Boolean added = redisTemplate.opsForSet().isMember(userKey, targetStr);
        if (Boolean.TRUE.equals(added)) {
            return false;
        }

        // 写入用户维度 Set 并刷新 TTL
        redisTemplate.opsForSet().add(userKey, targetStr);
        redisTemplate.expire(userKey, CacheConstants.CACHE_INTERACTION_TTL_SECONDS, TimeUnit.SECONDS);

        // 目标维度点赞计数 +1
        String statKey = buildStatKey(likeType, targetId);
        redisTemplate.opsForHash().increment(statKey, buildCountField(likeType), 1);

        // 写入待同步队列，值为 "1" 表示点赞状态
        redisTemplate.opsForHash().put(
                CacheConstants.CACHE_LIKE_PENDING,
                buildPendingField(userId, likeType, targetId),
                "1");

        log.debug("点赞: userId={}, type={}, targetId={}", userId, type, targetId);
        return true;
    }

    /**
     * 取消点赞
     *
     * @param type     点赞类型编码
     * @param targetId 目标ID
     * @param userId   用户ID
     * @return true-取消成功，false-参数无效或未点赞
     */
    @Override
    public boolean unlike(Integer type, Long targetId, Long userId) {
        CacheDocLikeTypeEnum likeType = CacheDocLikeTypeEnum.getByCode(type);
        if (likeType == null || targetId == null || userId == null) {
            return false;
        }
        String userKey = buildUserKey(likeType, userId);
        String targetStr = String.valueOf(targetId);

        // 校验：未点赞则无需取消
        Boolean isMember = redisTemplate.opsForSet().isMember(userKey, targetStr);
        if (!Boolean.TRUE.equals(isMember)) {
            return false;
        }

        // 从用户维度 Set 移除
        redisTemplate.opsForSet().remove(userKey, targetStr);

        // 目标维度点赞计数 -1
        String statKey = buildStatKey(likeType, targetId);
        redisTemplate.opsForHash().increment(statKey, buildCountField(likeType), -1);

        // 写入待同步队列，值为 "0" 表示取消点赞
        redisTemplate.opsForHash().put(
                CacheConstants.CACHE_LIKE_PENDING,
                buildPendingField(userId, likeType, targetId),
                "0");

        log.debug("取消点赞: userId={}, type={}, targetId={}", userId, type, targetId);
        return true;
    }

    /**
     * 获取用户点赞总数（查库）
     *
     * @param userId 用户ID
     * @return 点赞总数
     */
    @Override
    public Long getUserLikeCount(Long userId) {
        if (userId == null) {
            return 0L;
        }
        Long count = docUserInteractionMapper.countByUser(userId, DocUserInteractionContext.INTERACTION_TYPE_LIKE);
        return count != null ? count : 0L;
    }

    /**
     * 获取目标点赞数（先查缓存，未命中回源数据库并回填）
     *
     * @param type     点赞类型编码
     * @param targetId 目标ID
     * @return 点赞数
     */
    @Override
    public Long getLikeCount(Integer type, Long targetId) {
        CacheDocLikeTypeEnum likeType = CacheDocLikeTypeEnum.getByCode(type);
        if (likeType == null || targetId == null) {
            return 0L;
        }
        // 优先从缓存读取计数
        Object count = redisTemplate.opsForHash().get(buildStatKey(likeType, targetId), buildCountField(likeType));
        if (count != null) {
            return Long.parseLong(count.toString());
        }
        // 缓存未命中，回源数据库
        Integer targetType = getTargetType(likeType);
        if (targetType == null) {
            return 0L;
        }
        Long dbCount = docUserInteractionMapper.countByTarget(targetType, targetId, DocUserInteractionContext.INTERACTION_TYPE_LIKE);
        // 回填缓存，仅当计数 > 0 时写入
        if (dbCount != null && dbCount > 0) {
            redisTemplate.opsForHash().put(buildStatKey(likeType, targetId), buildCountField(likeType), dbCount.toString());
        }
        return dbCount != null ? dbCount : 0L;
    }

    /**
     * 判断用户是否已点赞该目标（先查缓存，未命中查库）
     *
     * @param type     点赞类型编码
     * @param targetId 目标ID
     * @param userId   用户ID
     * @return true-已点赞
     */
    @Override
    public boolean hasLiked(Integer type, Long targetId, Long userId) {
        CacheDocLikeTypeEnum likeType = CacheDocLikeTypeEnum.getByCode(type);
        if (likeType == null || targetId == null || userId == null) {
            return false;
        }
        // 优先从缓存判断
        String userKey = buildUserKey(likeType, userId);
        Boolean hit = redisTemplate.opsForSet().isMember(userKey, String.valueOf(targetId));
        if (Boolean.TRUE.equals(hit)) {
            return true;
        }
        // 缓存未命中，查库确认（status=1 表示有效点赞）
        Integer targetType = getTargetType(likeType);
        DocUserInteraction record = docUserInteractionMapper.selectByUserAndTarget(
                userId, targetType, targetId, DocUserInteractionContext.INTERACTION_TYPE_LIKE);
        return record != null && record.getStatus() == 1;
    }

    /**
     * 批量获取目标点赞数
     *
     * @param type      点赞类型编码
     * @param targetIds 目标ID集合
     * @return targetId → 点赞数
     */
    @Override
    public Map<Long, Long> getLikeCountBatch(Integer type, Iterable<Long> targetIds) {
        Map<Long, Long> result = new HashMap<>();
        if (targetIds == null) {
            return result;
        }
        for (Long targetId : targetIds) {
            result.put(targetId, getLikeCount(type, targetId));
        }
        return result;
    }

    /**
     * 将 Redis 待同步队列中的点赞数据批量写入数据库（rename → 读取 → 删除 → upsert）
     */
    @Override
    public void syncLikeDataToDb() {
        log.info("开始同步点赞数据...");
        // 检查待同步队列是否存在
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(CacheConstants.CACHE_LIKE_PENDING))) {
            log.info("点赞 pending 队列不存在，跳过");
            return;
        }
        // 原子切换：rename 确保后续新写入到新队列，当前队列独占处理
        String processingKey = CacheConstants.CACHE_LIKE_PENDING + ":processing:" + System.currentTimeMillis();
        redisTemplate.rename(CacheConstants.CACHE_LIKE_PENDING, processingKey);

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
                // Hash value：1=点赞，0=取消点赞
                Integer status = Integer.parseInt(entry.getValue().toString());

                CacheDocLikeTypeEnum likeType = CacheDocLikeTypeEnum.getByCode(typeCode);
                if (likeType == null) {
                    continue;
                }
                Integer targetType = getTargetType(likeType);
                if (targetType == null) {
                    continue;
                }

                DocUserInteraction record = new DocUserInteraction();
                record.setId(IdWorker.getId());
                record.setUserId(userId);
                record.setTargetType(targetType);
                record.setTargetId(targetId);
                record.setInteractionType(DocUserInteractionContext.INTERACTION_TYPE_LIKE);
                record.setStatus(status);
                batch.add(record);
            } catch (Exception e) {
                log.warn("解析 pending 记录失败: {}", entry.getKey(), e);
            }
        }

        // 批量写入数据库（INSERT ON DUPLICATE KEY UPDATE）
        if (!batch.isEmpty()) {
            docUserInteractionMapper.batchUpsert(batch);
            log.info("点赞同步完成，共 {} 条", batch.size());
        }
    }

    /**
     * 从数据库预热点赞缓存（重建用户维度 Set + 计数 Hash）
     *
     * @param type     点赞类型编码
     * @param targetId 目标ID
     */
    @Override
    public void warmLikeCacheFromDb(Integer type, Long targetId) {
        CacheDocLikeTypeEnum likeType = CacheDocLikeTypeEnum.getByCode(type);
        if (likeType == null || targetId == null) {
            return;
        }
        Integer targetType = getTargetType(likeType);
        if (targetType == null) {
            return;
        }

        // 查询所有点赞了该目标的用户，重建各自的用户维度 Set
        List<DocUserInteraction> records = docUserInteractionMapper.selectByTarget(
                targetType, targetId, DocUserInteractionContext.INTERACTION_TYPE_LIKE);

        if (records != null && !records.isEmpty()) {
            for (DocUserInteraction record : records) {
                if (record.getUserId() == null || record.getUserId() <= 0) {
                    continue;
                }
                String userKey = buildUserKey(likeType, record.getUserId());
                redisTemplate.opsForSet().add(userKey, String.valueOf(targetId));
                redisTemplate.expire(userKey, CacheConstants.CACHE_INTERACTION_TTL_SECONDS, TimeUnit.SECONDS);
            }
        }

        // 重建目标维度点赞计数 Hash
        Long dbCount = docUserInteractionMapper.countByTarget(targetType, targetId, DocUserInteractionContext.INTERACTION_TYPE_LIKE);
        if (dbCount != null && dbCount > 0) {
            redisTemplate.opsForHash().put(buildStatKey(likeType, targetId), buildCountField(likeType), dbCount.toString());
        }
        log.info("点赞缓存预热完成: type={}, targetId={}", type, targetId);
    }

    /**
     * 构建用户维度缓存 Key：zsk:like:user:{userId}:{typeCode}
     */
    private String buildUserKey(CacheDocLikeTypeEnum type, Long userId) {
        return CacheConstants.CACHE_LIKE_USER + userId + ":" + type.getCode();
    }

    /**
     * 构建统计维度缓存 Key：zsk:stat:{targetType}:{targetId}
     */
    private String buildStatKey(CacheDocLikeTypeEnum type, Long targetId) {
        return CacheConstants.CACHE_STAT + getTargetType(type) + ":" + targetId;
    }

    /**
     * 构建计数 Hash 字段：like:{typeCode}
     */
    private String buildCountField(CacheDocLikeTypeEnum type) {
        return "like:" + type.getCode();
    }

    /**
     * 构建待同步队列 Hash 字段：{userId}:{typeCode}:{targetId}
     */
    private String buildPendingField(Long userId, CacheDocLikeTypeEnum type, Long targetId) {
        return userId + ":" + type.getCode() + ":" + targetId;
    }

    /**
     * 根据点赞类型枚举映射数据库目标类型
     *
     * @param likeType 点赞类型枚举
     * @return 目标类型，未知类型返回 null
     */
    private Integer getTargetType(CacheDocLikeTypeEnum likeType) {
        switch (likeType) {
            case NOTE:
                return DocUserInteractionContext.TARGET_TYPE_NOTE;
            case NOTE_COMMENT:
            case VIDEO_COMMENT:
                return DocUserInteractionContext.TARGET_TYPE_COMMENT;
            case VIDEO:
                return DocUserInteractionContext.TARGET_TYPE_VIDEO;
            default:
                return null;
        }
    }
}
