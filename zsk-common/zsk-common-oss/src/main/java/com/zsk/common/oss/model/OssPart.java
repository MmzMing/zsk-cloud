package com.zsk.common.oss.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OSS分片信息
 *
 * @author wuhuaming
 * @date 2026-02-14
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OssPart {
    /** 分片号 */
    private Integer partNumber;

    /** ETag */
    private String etag;
}
