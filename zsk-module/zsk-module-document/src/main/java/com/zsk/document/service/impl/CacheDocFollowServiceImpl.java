package com.zsk.document.service.impl;

import com.zsk.common.core.constant.CacheConstants;
import com.zsk.common.redis.utils.BitmapOffsetUtil;
import com.zsk.document.domain.DocUserInteraction;
import com.zsk.document.domain.context.DocUserInteractionContext;
import com.zsk.document.enums.CacheDocFollowTypeEnum;
import com.zsk.document.mapper.DocUserInteractionMapper;
import com.zsk.document.service.ICacheDocFollowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 缓存文档关注服务实现类
 * <p>
 * 基于 Redis 双 Key 方案（Bitmap + Hash）实现高性能关注功能：
 * <ul>
 *     <li><b>Bitmap:</b> follow:bit:{typeCode}:{userId} — 使用位图存储用户关注状态，bit位为目标ID，记录该用户关注了谁</li>
 *     <li><b>Hash:</b> stat:{targetType}:{targetId} — 使用哈希存储统计计数（粉丝数），HINCRBY 原子增减</li>
 * </ul>
 * <p>
 * <b>核心设计要点：</b>
 * <ul>
 *     <li>关注关系是双向的：用户关注状态存储在 userId 的 Bitmap 中，粉丝计数存储在 targetId 的 Hash 中</li>
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
public class CacheDocFollowServiceImpl implements ICacheDocFollowService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final DocUserInteractionMapper docUserInteractionMapper;

    /**
     * 用户关注目标（用户/作者）
     * <p>
     * 使用 Redis Bitmap 实现原子关注操作：
     * 1. SETBIT 设置目标位为 1，返回旧状态
     * 2. 若旧状态为 false（未关注），则增加目标的粉丝计数
     * 3. 若旧状态为 true（已关注），说明重复操作，直接返回 false
     *
     * @param type     关注类型（见 {@link CacheDocFollowTypeEnum}）
     * @param targetId 目标ID（用户ID/作者ID）
     * @param userId   用户ID
     * @return true-关注成功，false-已关注或参数无效
     */
    @Override
    public boolean follow(Integer type, Long targetId, Long userId) {
        // 参数校验
        CacheDocFollowTypeEnum followType = CacheDocFollowTypeEnum.getByCode(type);
        if (followType == null || targetId == null || userId == null) {
            log.warn("关注参数无效: type={}, targetId={}, userId={}", type, targetId, userId);
            return false;
        }

        // 禁止关注自己
        if (targetId.equals(userId)) {
            log.warn("用户不能关注自己: userId={}", userId);
            return false;
        }

        // 构建 Bitmap Key：follow:bit:{typeCode}:{userId}
        // Bitmap 的位为目标ID，用于记录该用户关注了哪些目标
        String bitmapKey = buildBitmapKey(followType, userId);

        // SETBIT 设置为 true，返回旧状态（原子操作）
        // 旧状态=false → 未关注，需要增加粉丝计数
        // 旧状态=true → 已关注，无需操作
        Boolean wasFollowed = redisTemplate.opsForValue().setBit(bitmapKey, BitmapOffsetUtil.targetToOffset(targetId), true);
        if (Boolean.TRUE.equals(wasFollowed)) {
            return false; // 已关注，无需重复操作
        }

        // 更新粉丝计数：stat:{targetType}:{targetId} 的 follow:{typeCode} 字段
        String statKey = buildStatKey(followType, targetId);
        String countField = buildCountField(followType);
        redisTemplate.opsForHash().increment(statKey, countField, 1);

        log.debug("用户 {} 关注 {} targetId={}", userId, followType.getDesc(), targetId);
        return true;
    }

    /**
     * 用户取消关注
     * <p>
     * 使用 Redis Bitmap 实现原子取消关注操作：
     * 1. SETBIT 设置目标位为 0，返回旧状态
     * 2. 若旧状态为 true（已关注），则减少目标的粉丝计数
     * 3. 若旧状态为 false（未关注），说明重复操作，直接返回 false
     *
     * @param type     关注类型（见 {@link CacheDocFollowTypeEnum}）
     * @param targetId 目标ID（用户ID/作者ID）
     * @param userId   用户ID
     * @return true-取消关注成功，false-未关注或参数无效
     */
    @Override
    public boolean unfollow(Integer type, Long targetId, Long userId) {
        // 参数校验
        CacheDocFollowTypeEnum followType = CacheDocFollowTypeEnum.getByCode(type);
        if (followType == null || targetId == null || userId == null) {
            log.warn("取消关注参数无效: type={}, targetId={}, userId={}", type, targetId, userId);
            return false;
        }

        // 构建 Bitmap Key
        String bitmapKey = buildBitmapKey(followType, userId);

        // SETBIT 设置为 false，返回旧状态（原子操作）
        // 旧状态=true → 已关注，需要减少粉丝计数
        // 旧状态=false → 未关注，无需操作
        Boolean wasFollowed = redisTemplate.opsForValue().setBit(bitmapKey, BitmapOffsetUtil.targetToOffset(targetId), false);
        if (Boolean.FALSE.equals(wasFollowed)) {
            return false; // 未关注，无需操作
        }

        // 更新粉丝计数，减少 1
        String statKey = buildStatKey(followType, targetId);
        String countField = buildCountField(followType);
        redisTemplate.opsForHash().increment(statKey, countField, -1);

        log.debug("用户 {} 取消关注 {} targetId={}", userId, followType.getDesc(), targetId);
        return true;
    }

    /**
     * 获取用户关注总数（用户关注了多少人）
     * <p>
     * 直接从数据库查询用户的关注记录数
     *
     * @param userId 用户ID
     * @return 关注总数
     */
    @Override
    public Long getUserFollowCount(Long userId) {
        if (userId == null) {
            return 0L;
        }
        Long dbCount = docUserInteractionMapper.countByUser(userId, DocUserInteractionContext.INTERACTION_TYPE_FOLLOW);
        return dbCount != null ? dbCount : 0L;
    }

    /**
     * 获取目标的粉丝数（有多少人关注了该目标）
     * <p>
     * 缓存策略：先查 Redis，未命中则查数据库并回写缓存
     *
     * @param type     关注类型
     * @param targetId 目标ID
     * @return 粉丝数
     */
    @Override
    public Long getFollowCount(Integer type, Long targetId) {
        CacheDocFollowTypeEnum followType = CacheDocFollowTypeEnum.getByCode(type);
        if (followType == null || targetId == null) {
            return 0L;
        }

        // 先查 Redis 缓存
        String statKey = buildStatKey(followType, targetId);
        String countField = buildCountField(followType);
        Object count = redisTemplate.opsForHash().get(statKey, countField);
        if (count != null) {
            return Long.parseLong(count.toString());
        }

        // Redis 未命中，查数据库
        Integer targetType = getTargetType(followType);
        if (targetType == null) {
            return 0L;
        }
        Long dbCount = docUserInteractionMapper.countByTarget(targetType, targetId, DocUserInteractionContext.INTERACTION_TYPE_FOLLOW);

        // 回写缓存（仅当有数据时）
        if (dbCount != null && dbCount > 0) {
            redisTemplate.opsForHash().put(statKey, countField, dbCount.toString());
        }
        return dbCount != null ? dbCount : 0L;
    }

    /**
     * 判断用户是否已关注目标
     * <p>
     * 缓存策略：先查 Redis Bitmap，未命中则查数据库并回写缓存
     *
     * @param type     关注类型
     * @param targetId 目标ID
     * @param userId   用户ID
     * @return true-已关注，false-未关注
     */
    @Override
    public boolean hasFollowed(Integer type, Long targetId, Long userId) {
        CacheDocFollowTypeEnum followType = CacheDocFollowTypeEnum.getByCode(type);
        if (followType == null || targetId == null || userId == null) {
            return false;
        }

        // 先查 Redis Bitmap
        String bitmapKey = buildBitmapKey(followType, userId);
        Boolean bit = redisTemplate.opsForValue().getBit(bitmapKey, BitmapOffsetUtil.targetToOffset(targetId));
        if (Boolean.TRUE.equals(bit)) {
            return true;
        }

        // Redis 未命中，查数据库
        DocUserInteraction interaction = docUserInteractionMapper.selectByUserAndTarget(
                userId, getTargetType(followType), targetId, DocUserInteractionContext.INTERACTION_TYPE_FOLLOW);
        return interaction != null && interaction.getStatus() == 1;
    }

    /**
     * 批量获取目标的粉丝数
     *
     * @param type      关注类型
     * @param targetIds 目标ID列表
     * @return 目标ID到粉丝数的映射
     */
    @Override
    public Map<Long, Long> getFollowCountBatch(Integer type, Iterable<Long> targetIds) {
        Map<Long, Long> result = new HashMap<>();
        if (targetIds == null) {
            return result;
        }
        for (Long targetId : targetIds) {
            result.put(targetId, getFollowCount(type, targetId));
        }
        return result;
    }

    /**
     * 同步关注数据从 Redis 到数据库
     * <p>
     * 执行流程：
     * 1. 扫描所有 follow:bit:* 键，读取 Bitmap 获取所有关注关系（用户关注了哪些目标），写入数据库
     * 2. 扫描所有 stat:* 键，读取 follow:* 字段的计数（粉丝数），写入数据库
     * 3. 同步完成后删除 Bitmap 键（防止重复同步）
     */
    @Override
    public void syncFollowDataToDb() {
        log.info("开始同步关注数据到数据库...");
        int userSyncCount = 0;
        int countSyncCount = 0;

        // 1. 同步用户关注状态（从 Bitmap）
        String pattern = CacheConstants.CACHE_FOLLOW_BIT + "*";
        Collection<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            for (String bitmapKey : keys) {
                // 解析 Key 中的 typeCode 和 userId
                Integer typeCode = extractTypeCode(bitmapKey);
                Long userId = extractUserId(bitmapKey);
                if (typeCode == null || userId == null) {
                    continue;
                }
                CacheDocFollowTypeEnum followType = CacheDocFollowTypeEnum.getByCode(typeCode);
                if (followType == null) {
                    continue;
                }
                Integer targetType = getTargetType(followType);
                if (targetType == null) {
                    continue;
                }

                // 获取用户关注的所有目标ID（Bitmap中值为1的位）
                Set<Long> offsets = getSetBits(bitmapKey);
                for (Long offset : offsets) {
                    saveInteractionToDb(userId, targetType, offset, DocUserInteractionContext.INTERACTION_TYPE_FOLLOW);
                    userSyncCount++;
                }
                // 删除已同步的 Bitmap 键
                redisTemplate.delete(bitmapKey);
            }
        }

        // 2. 同步粉丝计数（从 Hash）
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
                    // 只处理关注相关字段
                    if (!field.startsWith("follow:")) {
                        continue;
                    }
                    try {
                        long count = Long.parseLong(entry.getValue().toString());
                        saveCountToDb(targetType, targetId, DocUserInteractionContext.INTERACTION_TYPE_FOLLOW, count);
                        countSyncCount++;
                    } catch (Exception e) {
                        log.warn("解析计数字段失败: key={}, field={}", statKey, field, e);
                    }
                }
            }
        }

        log.info("关注数据同步完成，用户记录 {} 条，计数 {} 条", userSyncCount, countSyncCount);
    }

    /**
     * 构建关注状态 Bitmap Key
     *
     * @param type   关注类型
     * @param userId 用户ID
     * @return Key 格式: follow:bit:{typeCode}:{userId}
     */
    private String buildBitmapKey(CacheDocFollowTypeEnum type, Long userId) {
        return CacheConstants.CACHE_FOLLOW_BIT + type.getCode() + ":" + userId;
    }

    /**
     * 构建统计 Hash Key
     *
     * @param type     关注类型
     * @param targetId 目标ID
     * @return Key 格式: stat:{targetType}:{targetId}
     */
    private String buildStatKey(CacheDocFollowTypeEnum type, Long targetId) {
        return CacheConstants.CACHE_STAT + getTargetType(type) + ":" + targetId;
    }

    /**
     * 构建计数字段名
     *
     * @param type 关注类型
     * @return 字段名格式: follow:{typeCode}
     */
    private String buildCountField(CacheDocFollowTypeEnum type) {
        return "follow:" + type.getCode();
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
     * 从 Bitmap Key 中解析 userId
     *
     * @param key Redis Key
     * @return userId
     */
    private Long extractUserId(String key) {
        try {
            String[] parts = key.split(":");
            if (parts.length >= 5) {
                return Long.parseLong(parts[4]);
            }
        } catch (Exception e) {
            log.warn("解析 userId 失败: {}", key, e);
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
     * 获取 Bitmap 中所有值为 1 的位（即用户关注的所有目标ID）
     *
     * @param bitmapKey Bitmap Key
     * @return 目标ID集合
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
     * 将关注类型转换为目标类型
     *
     * @param followType 关注类型
     * @return 目标类型
     */
    private Integer getTargetType(CacheDocFollowTypeEnum followType) {
        switch (followType) {
            case USER:
                return DocUserInteractionContext.TARGET_TYPE_USER;
            case NOTE_AUTHOR:
                return DocUserInteractionContext.TARGET_TYPE_NOTE;
            case VIDEO_AUTHOR:
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
            // 更新现有记录状态为已关注
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
     * 从数据库预热关注缓存
     * <p>
     * <b>预热目的：</b>服务重启后，将数据库中的关注数据加载到Redis缓存，恢复缓存状态。
     * <p>
     * <b>数据模型说明：</b>
     * <ul>
     *     <li>用户级记录（userId > 0）：存储单个用户的关注关系，用于构建Bitmap</li>
     *     <li>统计记录（userId = 0）：status字段存储粉丝计数，用于快速查询总数</li>
     * </ul>
     * <p>
     * <b>关注Bitmap设计特殊说明：</b>
     * <p>
     * 与点赞/收藏不同，关注的Bitmap设计采用反向映射：
     * <ul>
     *     <li>Key: follow:bit:{typeCode}:{userId} — 以关注者ID作为Key</li>
     *     <li>Bit位: targetId — 以被关注者ID作为bit位位置</li>
     * </ul>
     * 这样设计便于快速查询"某用户关注了哪些人"（BITOP操作）
     * <p>
     * <b>预热流程：</b>
     * <ol>
     *     <li>预热用户关注状态到Bitmap：查询所有关注该目标的用户，为每个用户构建Bitmap</li>
     *     <li>预热粉丝计数到Hash：通过countByTarget重新统计，保证数据准确性</li>
     * </ol>
     *
     * @param type     关注类型
     * @param targetId 目标ID（被关注者ID）
     */
    @Override
    public void warmFollowCacheFromDb(Integer type, Long targetId) {
        // 参数校验
        CacheDocFollowTypeEnum followType = CacheDocFollowTypeEnum.getByCode(type);
        if (followType == null || targetId == null) {
            log.warn("预热缓存参数无效: type={}, targetId={}", type, targetId);
            return;
        }

        // 获取目标类型映射
        Integer targetType = getTargetType(followType);
        if (targetType == null) {
            return;
        }

        // ========== 步骤1：预热用户关注状态到Bitmap ==========
        // 关注的Bitmap设计与点赞/收藏不同：
        // - Key: follow:bit:{typeCode}:{userId} — 以关注者ID作为Key
        // - Bit位: targetId — 以被关注者ID作为bit位位置
        // 这样设计便于快速查询"某用户关注了哪些人"（BITOP操作）

        // 查询数据库中关注该目标的所有用户记录（status=1）
        // 必须查询用户级记录（userId > 0），因为需要知道具体哪些用户关注了该目标
        List<DocUserInteraction> interactions = docUserInteractionMapper.selectByTarget(
                targetType, targetId, DocUserInteractionContext.INTERACTION_TYPE_FOLLOW);

        if (interactions != null && !interactions.isEmpty()) {
            for (DocUserInteraction interaction : interactions) {
                // 过滤无效用户ID（userId > 0）
                if (interaction.getUserId() != null && interaction.getUserId() > 0) {
                    // 为每个关注者构建Bitmap：将targetId对应的bit位设置为1
                    String bitmapKey = buildBitmapKey(followType, interaction.getUserId());
                    redisTemplate.opsForValue().setBit(bitmapKey, BitmapOffsetUtil.targetToOffset(targetId), true);
                }
            }
            log.debug("关注Bitmap预热完成: targetId={}, 记录数={}", targetId, interactions.size());
        }

        // ========== 步骤2：预热粉丝计数到Hash ==========
        // Hash Key: stat:{targetType}:{targetId}
        // Field: follow:{typeCode} -> 粉丝计数值
        String statKey = buildStatKey(followType, targetId);
        String countField = buildCountField(followType);

        // 通过countByTarget重新统计用户级记录，而不是读取userId=0的统计记录
        // 这样可以保证数据一致性，避免因异常导致统计记录与实际用户记录不一致
        Long dbCount = docUserInteractionMapper.countByTarget(targetType, targetId, DocUserInteractionContext.INTERACTION_TYPE_FOLLOW);
        if (dbCount != null && dbCount > 0) {
            redisTemplate.opsForHash().put(statKey, countField, dbCount.toString());
            log.debug("粉丝计数预热完成: targetId={}, count={}", targetId, dbCount);
        }

        log.info("关注缓存预热完成: type={}, targetId={}", type, targetId);
    }
}
