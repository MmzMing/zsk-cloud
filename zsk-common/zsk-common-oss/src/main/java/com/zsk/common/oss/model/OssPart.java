package com.zsk.common.oss.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OSS分片信息
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OssPart {
    /**
     * 分片号
     */
    private Integer partNumber;

    /**
     * ETag
     */
    private String etag;
}
