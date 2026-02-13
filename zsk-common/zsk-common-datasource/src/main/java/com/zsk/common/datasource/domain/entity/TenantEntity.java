package com.zsk.common.datasource.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 多租户基础实体类
 *
 * @author zsk
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TenantEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 租户ID
     */
    private Long tenantId;
}
