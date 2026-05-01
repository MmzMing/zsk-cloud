package com.zsk.system.domain.vo;

import com.zsk.system.domain.SysDictData;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 带版本号的字典数据响应对象
 * <p>
 * 前端通过此对象同时获取字典数据和版本号，版本号可用于后续缓存比对。
 * <p>
 * 使用示例：
 * <pre>
 * // 前端首次加载
 * GET /dict/type/cache/tag/{dictType}
 * 返回 { "version": 1714521600000, "data": [...] }
 * 前端缓存: localStorage.setItem("dict_version_{dictType}", res.version)
 * localStorage.setItem("dict_data_{dictType}", JSON.stringify(res.data))
 *
 * // 前端后续检查更新
 * GET /dict/type/version/{dictType}
 * 返回版本号，与本地缓存的版本号对比，不一致则重新拉取
 * </pre>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-05-01
 */
@Data
public class DictCacheVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 缓存版本号（时间戳）
     * <p>
     * 每次数据变更时自动生成新时间戳，前端通过比较版本号判断是否需要刷新本地缓存。
     */
    private long version;

    /**
     * 字典数据列表
     * <p>
     * 按 dictSort 升序排列，仅包含状态为"0"（正常）的字典数据。
     */
    private List<SysDictData> data;
}
