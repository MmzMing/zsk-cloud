package com.zsk.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审核目标类型枚举
 *
 * <p>定义统一审核系统中所有支持的内容类型，用于 document_audit 表的 target_type 字段。
 * 新增内容类型时只需在此枚举中添加值，并实现对应的 AuditTargetStrategy 即可。</p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-30
 */
@Getter
@AllArgsConstructor
public enum AuditTargetType {

    /**
     * 文档
     */
    NOTE(1, "document_note", "文档"),

    /**
     * 视频
     */
    VIDEO(2, "document_video", "视频"),

    /**
     * 文档评论
     */
    NOTE_COMMENT(3, "document_note_comment", "文档评论"),

    /**
     * 视频评论
     */
    VIDEO_COMMENT(4, "document_video_comment", "视频评论");

    /**
     * 类型编码（存储到数据库的值）
     */
    private final Integer code;

    /**
     * 对应的数据库表名（用于日志和动态SQL）
     */
    private final String tableName;

    /**
     * 中文描述
     */
    private final String desc;

    /**
     * 根据code获取枚举
     *
     * @param code 类型编码
     * @return 枚举值，未匹配则返回null
     */
    public static AuditTargetType getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (AuditTargetType type : AuditTargetType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
