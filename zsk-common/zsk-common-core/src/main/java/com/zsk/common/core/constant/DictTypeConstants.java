package com.zsk.common.core.constant;

/**
 * 字典类型常量
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
public class DictTypeConstants {

    /** 视频分类字典类型 */
    public static final String VIDEO_CATEGORY = "video_category";

    /** 视频标签字典类型 */
    public static final String VIDEO_TAG = "video_tag";

    /** 视频违规原因字典类型 */
    public static final String VIDEO_VIOLATION_REASON = "video_violation_reason";

    /** 文档分类字典类型 */
    public static final String DOCUMENT_CATEGORY = "document_category";

    /** 文档标签字典类型 */
    public static final String DOCUMENT_TAG = "document_tag";

    private DictTypeConstants() {
        throw new AssertionError("常量类禁止实例化");
    }
}
