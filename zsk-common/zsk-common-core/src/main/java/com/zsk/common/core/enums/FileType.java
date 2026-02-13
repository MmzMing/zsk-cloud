package com.zsk.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文件类型枚举
 *
 * @author zsk
 */
@Getter
@AllArgsConstructor
public enum FileType {
    /**
     * 图片
     */
    IMAGE("image", new String[]{"png", "jpg", "jpeg", "gif", "bmp", "webp"}),

    /**
     * 视频
     */
    VIDEO("video", new String[]{"mp4", "avi", "rmvb", "wmv", "mkv", "flv"}),

    /**
     * 音频
     */
    AUDIO("audio", new String[]{"mp3", "wav", "wma", "ogg", "flac"}),

    /**
     * 文档
     */
    DOC("doc", new String[]{"doc", "docx", "xls", "xlsx", "ppt", "pptx", "pdf", "txt"}),

    /**
     * 压缩包
     */
    ZIP("zip", new String[]{"zip", "rar", "7z", "tar", "gz"}),

    /**
     * 其他
     */
    OTHER("other", new String[]{});

    /**
     * 类型名称
     */
    private final String name;

    /**
     * 扩展名数组
     */
    private final String[] extensions;

    /**
     * 根据扩展名获取文件类型
     *
     * @param extension 扩展名
     * @return 文件类型
     */
    public static FileType getByExtension(String extension) {
        if (extension == null || extension.isEmpty()) {
            return OTHER;
        }
        String ext = extension.toLowerCase();
        for (FileType type : values()) {
            for (String e : type.extensions) {
                if (e.equalsIgnoreCase(ext)) {
                    return type;
                }
            }
        }
        return OTHER;
    }
}
