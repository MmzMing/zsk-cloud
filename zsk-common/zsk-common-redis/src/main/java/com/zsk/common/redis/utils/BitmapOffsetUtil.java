package com.zsk.common.redis.utils;

/**
 * Redis Bitmap Offset 转换工具类
 * <p>
 * Redis SETBIT/GETBIT 的 offset 参数必须是 0 到 2^32-1（4294967295）之间的整数。
 * 但项目使用 Snowflake ID（如 2045019264374075394），远超此范围。
 * <p>
 * 本工具类通过哈希取模将任意 Long ID 映射到合法的 Bitmap offset 范围。
 * 同一 ID 始终映射到同一 offset，保证一致性。
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-27
 */
public class BitmapOffsetUtil {

    /**
     * Redis Bitmap offset 最大值（2^32 - 1）
     */
    public static final long MAX_BITMAP_OFFSET = (1L << 32) - 1; // 4294967295

    private BitmapOffsetUtil() {
    }

    /**
     * 将用户ID转换为合法的 Redis Bitmap offset
     * <p>
     * 使用哈希取模算法，保证同一 userId 始终得到同一 offset。
     * 结果范围：0 ~ {@link #MAX_BITMAP_OFFSET}
     *
     * @param userId 用户ID（Snowflake ID）
     * @return 合法的 Bitmap offset
     */
    public static long toOffset(Long userId) {
        if (userId == null) {
            return 0L;
        }
        long hash = hash64(userId);
        return Math.abs(hash) % MAX_BITMAP_OFFSET;
    }

    /**
     * 将目标ID转换为合法的 Redis Bitmap offset
     * <p>
     * 与 {@link #toOffset(Long)} 使用相同的算法，用于关注场景（targetId 作为 bit 位）。
     *
     * @param targetId 目标ID（Snowflake ID）
     * @return 合法的 Bitmap offset
     */
    public static long targetToOffset(Long targetId) {
        return toOffset(targetId);
    }

    /**
     * 64位哈希函数（基于 MurmurHash3 思想）
     * <p>
     * 将 Long 值均匀散列，减少取模后的冲突。
     *
     * @param value 原始值
     * @return 64位哈希值
     */
    private static long hash64(long value) {
        long k = value;
        k ^= k >>> 33;
        k *= 0xff51afd7ed558ccdL;
        k ^= k >>> 33;
        k *= 0xc4ceb9fe1a85ec53L;
        k ^= k >>> 33;
        return k;
    }
}
