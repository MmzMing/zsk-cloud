package com.zsk.system.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 字典数据API实体
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
public class SysDictDataApi implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 字典ID */
    private Long id;

    /** 字典排序 */
    private Integer dictSort;

    /** 字典标签 */
    private String dictLabel;

    /** 字典键值 */
    private String dictValue;

    /** 字典类型 */
    private String dictType;

    /** 父级字典值（用于构建树形结构） */
    private String parentValue;

    /** 状态（0正常 1停用） */
    private String status;
}
