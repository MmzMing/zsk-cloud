package com.zsk.document.service.impl;

import com.zsk.common.core.constant.CacheConstants;
import com.zsk.document.domain.DocUserInteraction;
import com.zsk.document.domain.context.DocUserInteractionContext;
import com.zsk.document.enums.CacheDocCollectTypeEnum;
import com.zsk.document.mapper.DocUserInteractionMapper;
import com.zsk.document.service.ICacheDocCollectService;
import com.zsk.document.util.BitmapOffsetUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 缓存文档收藏服务实现类
 * <p>
 * 基于 Redis 双 Key 方案（Bitmap + Hash）实现高性能收藏功能：
 * <ul>
 *     <li><b>Bitmap:</b> collect:bit:{typeCode}:{targetId} — 使用位图存储用户收藏状态，SETBIT 返回旧值实现原子 toggle</li>
 *     <li><b>Hash:</b> stat:{targetType}:{targetId} — 使用哈希存储统计计数，HINCRBY 原子增减</li>
 * </ul>
 * <p>
 * <b>核心设计要点：</b>
 * <ul>
 *     <li>SETBIT 返回旧值，天然支持原子 toggle，无需分布式锁</li>
 *     <li>Bitmap 极省内存：1亿用户仅需约 12MB</li>
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
public class CacheDocCollectServiceImpl implements ICacheDocCollectService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final DocUserInteractionMapper docUserInteractionMapper;

    /**
     * 用户收藏内容
     * <p>
     * 使用 Redis Bitmap 实现原子收藏操作：
     * 1. SETBIT 设置用户位为 1，返回旧状态
     * 2. 若旧状态为 false（未收藏），则增加计数
     * 3. 若旧状态为 true（已收藏），说明重复操作，直接返回 false
     *
     * @param type     收藏类型（见 {@link CacheDocCollectTypeEnum}）
     * @param targetId 目标内容ID（笔记ID/视频ID）
     * @param userId   用户ID
     * @return true-收藏成功，false-已收藏或参数无效
     */
    @Override
    public boolean collect(Integer type, Long targetId, Long userId) {
        // 参数校验
        CacheDocCollectTypeEnum collectType = CacheDocCollectTypeEnum.getByCode(type);
        if (collectType == null || targetId == null || userId == null) {
            log.warn("收藏参数无效: type={}, targetId={}, userId={}", type, targetId, userId);
            return false;
        }

        // 构建 Bitmap Key：collect:bit:{typeCode}:{targetId}
        String bitmapKey = buildBitmapKey(collectType, targetId);
        
        // SETBIT 设置为 true，返回旧状态（原子操作）
        // 旧状态=false → 未收藏，需要增加计数
        // 旧状态=true → 已收藏，无需操作
        Boolean wasCollected = redisTemplate.opsForValue().setBit(bitmapKey, BitmapOffsetUtil.toOffset(userId), true);
        if (Boolean.TRUE.equals(wasCollected)) {
            return false; // 已收藏，无需重复操作
        }

        // 更新统计计数：stat:{targetType}:{targetId} 的 collect:{typeCode} 字段
        String statKey = buildStatKey(collectType, targetId);
        String countField = buildCountField(collectType);
        redisTemplate.opsForHash().increment(statKey, countField, 1);
        
        log.debug("用户 {} 收藏 {} targetId={}", userId, collectType.getDesc(), targetId);
        return true;
    }

    /**
     * 用户取消收藏
     * <p>
     * 使用 Redis Bitmap 实现原子取消收藏操作：
     * 1. SETBIT 设置用户位为 0，返回旧状态
     * 2. 若旧状态为 true（已收藏），则减少计数
     * 3. 若旧状态为 false（未收藏），说明重复操作，直接返回 false
     *
     * @param type     收藏类型（见 {@link CacheDocCollectTypeEnum}）
     * @param targetId 目标内容ID（笔记ID/视频ID）
     * @param userId   用户ID
     * @return true-取消收藏成功，false-未收藏或参数无效
     */
    @Override
    public boolean uncollect(Integer type, Long targetId, Long userId) {
        // 参数校验
        CacheDocCollectTypeEnum collectType = CacheDocCollectTypeEnum.getByCode(type);
        if (collectType == null || targetId == null || userId == null) {
            log.warn("取消收藏参数无效: type={}, targetId={}, userId={}", type, targetId, userId);
            return false;
        }

        // 构建 Bitmap Key
        String bitmapKey = buildBitmapKey(collectType, targetId);
        
        // SETBIT 设置为 false，返回旧状态（原子操作）
        // 旧状态=true → 已收藏，需要减少计数
        // 旧状态=false → 未收藏，无需操作
        Boolean wasCollected = redisTemplate.opsForValue().setBit(bitmapKey, BitmapOffsetUtil.toOffset(userId), false);
        if (Boolean.FALSE.equals(wasCollected)) {
            return false; // 未收藏，无需操作
        }

        // 更新统计计数，减少 1
        String statKey = buildStatKey(collectType, targetId);
        String countField = buildCountField(collectType);
        redisTemplate.opsForHash().increment(statKey, countField, -1);
        
        log.debug("用户 {} 取消收藏 {} targetId={}", userId, collectType.getDesc(), targetId);
        return true;
    }

    /**
     * 获取用户收藏总数
     * <p>
     * 直接从数据库查询用户的收藏记录数
     *
     * @param userId 用户ID
     * @return 收藏总数
     */
    @Override
    public Long getUserCollectCount(Long userId) {
        if (userId == null) {
            return 0L;
        }
        Long dbCount = docUserInteractionMapper.countByUser(userId, DocUserInteractionContext.INTERACTION_TYPE_FAVORITE);
        return dbCount != null ? dbCount : 0L;
    }

    /**
     * 获取目标内容的收藏数
     * <p>
     * 缓存策略：先查 Redis，未命中则查数据库并回写缓存
     *
     * @param type     收藏类型
     * @param targetId 目标内容ID
     * @return 收藏数
     */
    @Override
    public Long getCollectCount(Integer type, Long targetId) {
        CacheDocCollectTypeEnum collectType = CacheDocCollectTypeEnum.getByCode(type);
        if (collectType == null || targetId == null) {
            return 0L;
        }

        // 先查 Redis 缓存
        String statKey = buildStatKey(collectType, targetId);
        String countField = buildCountField(collectType);
        Object count = redisTemplate.opsForHash().get(statKey, countField);
        if (count != null) {
            return Long.parseLong(count.toString());
        }

        // Redis 未命中，查数据库
        Integer targetType = getTargetType(collectType);
        if (targetType == null) {
            return 0L;
        }
        Long dbCount = docUserInteractionMapper.countByTarget(targetType, targetId, DocUserInteractionContext.INTERACTION_TYPE_FAVORITE);
        
        // 回写缓存（仅当有数据时）
        if (dbCount != null && dbCount > 0) {
            redisTemplate.opsForHash().put(statKey, countField, dbCount.toString());
        }
        return dbCount != null ? dbCount : 0L;
    }

    /**
     * 判断用户是否已收藏目标内容
     * <p>
     * 缓存策略：先查 Redis Bitmap，未命中则查数据库并回写缓存
     *
     * @param type     收藏类型
     * @param targetId 目标内容ID
     * @param userId   用户ID
     * @return true-已收藏，false-未收藏
     */
    @Override
    public boolean hasCollected(Integer type, Long targetId, Long userId) {
        CacheDocCollectTypeEnum collectType = CacheDocCollectTypeEnum.getByCode(type);
        if (collectType == null || targetId == null || userId == null) {
            return false;
        }

        // 先查 Redis Bitmap
        String bitmapKey = buildBitmapKey(collectType, targetId);
        Boolean bit = redisTemplate.opsForValue().getBit(bitmapKey, BitmapOffsetUtil.toOffset(userId));
        if (Boolean.TRUE.equals(bit)) {
            return true;
        }

        // Redis 未命中，查数据库
        DocUserInteraction interaction = docUserInteractionMapper.selectByUserAndTarget(
                userId, getTargetType(collectType), targetId, DocUserInteractionContext.INTERACTION_TYPE_FAVORITE);
        return interaction != null && interaction.getStatus() == 1;
    }

    /**
     * 批量获取目标内容的收藏数
     *
     * @param type      收藏类型
     * @param targetIds 目标内容ID列表
     * @return 目标ID到收藏数的映射
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
     * 同步收藏数据从 Redis 到数据库
     * <p>
     * 执行流程：
     * 1. 扫描所有 collect:bit:* 键，读取 Bitmap 获取所有收藏用户ID，写入数据库
     * 2. 扫描所有 stat:* 键，读取 collect:* 字段的计数，写入数据库
     * 3. 同步完成后删除 Bitmap 键（防止重复同步）
     */
    @Override
    public void syncCollectDataToDb() {
        log.info("开始同步收藏数据到数据库...");
        int userSyncCount = 0;
        int countSyncCount = 0;

        // 1. 同步用户收藏状态（从 Bitmap）
        String pattern = CacheConstants.CACHE_COLLECT_BIT + "*";
        Collection<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            for (String bitmapKey : keys) {
                // 解析 Key 中的 typeCode 和 targetId
                Integer typeCode = extractTypeCode(bitmapKey);
                Long targetId = extractTargetId(bitmapKey);
                if (typeCode == null || targetId == null) {
                    continue;
                }
                CacheDocCollectTypeEnum collectType = CacheDocCollectTypeEnum.getByCode(typeCode);
                if (collectType == null) {
                    continue;
                }
                Integer targetType = getTargetType(collectType);
                if (targetType == null) {
                    continue;
                }

                // 获取所有已收藏的用户ID（Bitmap中值为1的位）
                Set<Long> offsets = getSetBits(bitmapKey);
                for (Long offset : offsets) {
                    saveInteractionToDb(offset, targetType, targetId, DocUserInteractionContext.INTERACTION_TYPE_FAVORITE);
                    userSyncCount++;
                }
                // 删除已同步的 Bitmap 键
                redisTemplate.delete(bitmapKey);
            }
        }

        // 2. 同步收藏计数（从 Hash）
        String statPattern = CacheConstants.CACHE_STAT + "*";
        Collection<String> statKeys = redisTemplate.keys(statPattern);
        if (statKeys != null && !statKeys.isEmpty()) {
            for (String statKey : statKeys) {
                Long[] parsed = extractStatKey(statKey);
                if (parsed == null) {
                    continue;
                }
                int targetType = parsed[0].intValue();
                long targetId = parsed[1];

                Map<Object, Object> entries = redisTemplate.opsForHash().entries(statKey);
                for (Map.Entry<Object, Object> entry : entries.entrySet()) {
                    String field = entry.getKey().toString();
                    // 只处理收藏相关字段
                    if (!field.startsWith("collect:")) {
                        continue;
                    }
                    try {
                        long count = Long.parseLong(entry.getValue().toString());
                        saveCountToDb(targetType, targetId, DocUserInteractionContext.INTERACTION_TYPE_FAVORITE, count);
                        countSyncCount++;
                    } catch (Exception e) {
                        log.warn("解析计数字段失败: key={}, field={}", statKey, field, e);
                    }
                }
            }
        }

        log.info("收藏数据同步完成，用户记录 {} 条，计数 {} 条", userSyncCount, countSyncCount);
    }

    /**
     * 构建收藏状态 Bitmap Key
     *
     * @param type     收藏类型
     * @param targetId 目标内容ID
     * @return Key 格式: collect:bit:{typeCode}:{targetId}
     */
    private String buildBitmapKey(CacheDocCollectTypeEnum type, Long targetId) {
        return CacheConstants.CACHE_COLLECT_BIT + type.getCode() + ":" + targetId;
    }

    /**
     * 构建统计 Hash Key
     *
     * @param type     收藏类型
     * @param targetId 目标内容ID
     * @return Key 格式: stat:{targetType}:{targetId}
     */
    private String buildStatKey(CacheDocCollectTypeEnum type, Long targetId) {
        return CacheConstants.CACHE_STAT + getTargetType(type) + ":" + targetId;
    }

    /**
     * 构建计数字段名
     *
     * @param type 收藏类型
     * @return 字段名格式: collect:{typeCode}
     */
    private String buildCountField(CacheDocCollectTypeEnum type) {
        return "collect:" + type.getCode();
    }

    /**
     * 从 Bitmap Key 中解析 typeCode
     *
     * @param key Redis Key
     * @return typeCode
     */
    private Integer extractTypeCode(String key) {
        try {
            String[] parts = key.split(":");
            if (parts.length >= 4) {
                return Integer.parseInt(parts[3]);
            }
        } catch (Exception e) {
            log.warn("解析 typeCode 失败: {}", key, e);
        }
        return null;
    }

    /**
     * 从 Bitmap Key 中解析 targetId
     *
     * @param key Redis Key
     * @return targetId
     */
    private Long extractTargetId(String key) {
        try {
            String[] parts = key.split(":");
            if (parts.length >= 5) {
                return Long.parseLong(parts[4]);
            }
        } catch (Exception e) {
            log.warn("解析 targetId 失败: {}", key, e);
        }
        return null;
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
     * 获取 Bitmap 中所有值为 1 的位（即所有已收藏的用户ID）
     *
     * @param bitmapKey Bitmap Key
     * @return 用户ID集合
     */
    private Set<Long> getSetBits(String bitmapKey) {
        Set<Long> result = new HashSet<>();
        try {
            redisTemplate.execute((RedisCallback<Void>) connection -> {
                long pos = 0;
                byte[] keyBytes = bitmapKey.getBytes();
                while (true) {
                    // BITPOS 查找下一个值为 1 的位，从 pos 位置开始查找
                    Range<Long> range = Range.from(Range.Bound.inclusive(pos)).to(Range.Bound.unbounded());
                    Long idx = connection.bitPos(keyBytes, true, range);
                    if (idx == null || idx < 0) {
                        break;
                    }
                    result.add(idx);
                    pos = idx + 1;
                }
                return null;
            });
        } catch (Exception e) {
            log.warn("读取 Bitmap 位失败: {}", bitmapKey, e);
        }
        return result;
    }

    /**
     * 将收藏类型转换为目标类型
     *
     * @param collectType 收藏类型
     * @return 目标类型
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

    /**
     * 保存用户交互记录到数据库
     *
     * @param userId          用户ID
     * @param targetType      目标类型
     * @param targetId        目标ID
     * @param interactionType 交互类型
     */
    private void saveInteractionToDb(Long userId, Integer targetType, Long targetId, Integer interactionType) {
        DocUserInteraction existing = docUserInteractionMapper.selectByUserAndTarget(
                userId, targetType, targetId, interactionType);
        if (existing != null) {
            // 更新现有记录状态为已收藏
            existing.setStatus(1);
            docUserInteractionMapper.updateById(existing);
        } else {
            // 插入新记录
            DocUserInteraction interaction = new DocUserInteraction();
            interaction.setUserId(userId);
            interaction.setTargetType(targetType);
            interaction.setTargetId(targetId);
            interaction.setInteractionType(interactionType);
            interaction.setStatus(1);
            docUserInteractionMapper.insert(interaction);
        }
    }

    /**
     * 保存统计计数到数据库
     *
     * @param targetType      目标类型
     * @param targetId        目标ID
     * @param interactionType 交互类型
     * @param count           计数值
     */
    private void saveCountToDb(Integer targetType, Long targetId, Integer interactionType, Long count) {
        // userId=0 表示这是一条统计记录，而非用户级记录
        DocUserInteraction existing = docUserInteractionMapper.selectByUserAndTarget(
                0L, targetType, targetId, interactionType);
        if (existing != null) {
            existing.setStatus(count.intValue());
            docUserInteractionMapper.updateById(existing);
        } else {
            DocUserInteraction interaction = new DocUserInteraction();
            interaction.setUserId(0L);
            interaction.setTargetType(targetType);
            interaction.setTargetId(targetId);
            interaction.setInteractionType(interactionType);
            interaction.setStatus(count.intValue());
            docUserInteractionMapper.insert(interaction);
        }
    }

    /**
     * 从数据库预热收藏缓存
     * <p>
     * <b>预热目的：</b>服务重启后，将数据库中的收藏数据加载到Redis缓存，恢复缓存状态。
     * <p>
     * <b>数据模型说明：</b>
     * <ul>
     *     <li>用户级记录（userId > 0）：存储单个用户的收藏状态，用于构建Bitmap</li>
     *     <li>统计记录（userId = 0）：status字段存储计数，用于快速查询总数</li>
     * </ul>
     * <p>
     * <b>预热流程：</b>
     * <ol>
     *     <li>预热用户收藏状态到Bitmap：必须查询用户级记录，因为需要知道具体哪些用户收藏了该内容</li>
     *     <li>预热收藏计数到Hash：通过countByTarget重新统计，保证数据准确性（避免统计记录与实际不一致）</li>
     * </ol>
     * <p>
     * <b>为什么不直接读取userId=0的统计记录？</b>
     * <ul>
     *     <li>Bitmap预热必须用用户级记录（需要具体用户ID来设置bit位）</li>
     *     <li>Hash计数重新统计可保证数据一致性（异常场景下统计记录可能不准确）</li>
     * </ul>
     *
     * @param type     收藏类型
     * @param targetId 目标ID
     */
    @Override
    public void warmCollectCacheFromDb(Integer type, Long targetId) {
        // 参数校验
        CacheDocCollectTypeEnum collectType = CacheDocCollectTypeEnum.getByCode(type);
        if (collectType == null || targetId == null) {
            log.warn("预热缓存参数无效: type={}, targetId={}", type, targetId);
            return;
        }

        // 获取目标类型映射
        Integer targetType = getTargetType(collectType);
        if (targetType == null) {
            return;
        }

        // ========== 步骤1：预热用户收藏状态到Bitmap ==========
        // Bitmap Key: collect:bit:{typeCode}:{targetId}
        // 每个bit位代表一个用户ID，值为1表示该用户已收藏
        String bitmapKey = buildBitmapKey(collectType, targetId);
        // 先清除旧的Bitmap缓存，避免脏数据（服务重启后可能有残留的过期数据）
        redisTemplate.delete(bitmapKey);

        // 查询数据库中该目标的所有有效收藏记录（status=1）
        // 必须查询用户级记录（userId > 0），因为需要具体的用户ID来设置Bitmap的bit位
        List<DocUserInteraction> interactions = docUserInteractionMapper.selectByTarget(
                targetType, targetId, DocUserInteractionContext.INTERACTION_TYPE_FAVORITE);

        if (interactions != null && !interactions.isEmpty()) {
            for (DocUserInteraction interaction : interactions) {
                // 过滤无效用户ID（userId > 0）
                if (interaction.getUserId() != null && interaction.getUserId() > 0) {
                    // 将用户ID对应的bit位设置为1，表示已收藏
                    redisTemplate.opsForValue().setBit(bitmapKey, BitmapOffsetUtil.toOffset(interaction.getUserId()), true);
                }
            }
            log.debug("收藏Bitmap预热完成: targetId={}, 记录数={}", targetId, interactions.size());
        }

        // ========== 步骤2：预热收藏计数到Hash ==========
        // Hash Key: stat:{targetType}:{targetId}
        // Field: collect:{typeCode} -> 收藏计数值
        String statKey = buildStatKey(collectType, targetId);
        String countField = buildCountField(collectType);
        
        // 通过countByTarget重新统计用户级记录，而不是读取userId=0的统计记录
        // 这样可以保证数据一致性，避免因异常导致统计记录与实际用户记录不一致
        Long dbCount = docUserInteractionMapper.countByTarget(targetType, targetId, DocUserInteractionContext.INTERACTION_TYPE_FAVORITE);
        if (dbCount != null && dbCount > 0) {
            redisTemplate.opsForHash().put(statKey, countField, dbCount.toString());
            log.debug("收藏计数预热完成: targetId={}, count={}", targetId, dbCount);
        }

        log.info("收藏缓存预热完成: type={}, targetId={}", type, targetId);
    }
}
