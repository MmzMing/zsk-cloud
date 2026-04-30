package com.zsk.system.domain.dto;

import com.zsk.system.domain.SysDictData;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 字典缓存包装对象
 * <p>
 * 将版本号与字典数据打包存储在同一个 Redis Value 中，
 * 实现版本与数据的原子读取，保证一致性。
 * <p>
 * Redis 存储结构：
 * <pre>
 * Key:   zsk:dict:data:{dictType}
 * Value: DictCacheItem { version: 5, data: [{dictLabel:"男", ...}, ...] }
 * </pre>
 * <p>
 * 前端使用流程：
 * 1. 调用 GET /dict/type/version 获取全局版本号
 * 2. 若全局版本号变化，调用 GET /dict/type/cache/tag/{dictType} 获取数据
 * 3. 响应中的 version 字段即为该类型的版本号，前端可缓存用于增量比对
 *
 * @author wuhuaming
 */
@Data
public class DictCacheItem implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 缓存版本号
     * <p>
     * 每次数据变更时递增，前端通过比较版本号判断是否需要刷新本地缓存。
     * 版本号取自全局版本号（zsk:dict:version）在写入时的值，
     * 保证全局递增、不重复。
     */
    private long version;

    /**
     * 字典数据列表
     * <p>
     * 按 dictSort 升序排列，仅包含状态为"0"（正常）的字典数据。
     */
    private List<SysDictData> data;
}
