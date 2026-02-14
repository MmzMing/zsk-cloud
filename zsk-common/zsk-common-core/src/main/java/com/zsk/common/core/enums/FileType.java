package com.zsk.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文件类型枚举
 *
 * @author wuhuaming
 */
@Getter
@AllArgsConstructor
public enum FileType {
    /**
     * 图片
     */
    IMAGE("image", new String[]{"bmp", "dib", "gif", "jfif", "jpe", "jpeg", "jpg", "png", "tif", "tiff", "ico", "psd", "svg", "webp"}),

    /**
     * 视频
     */
    VIDEO("video", new String[]{"wmv", "asf", "asx", "rm", "rmvb", "mpg", "mpeg", "mpe", "3gp", "mov", "mp4", "m4v", "avi", "dat", "mkv", "flv", "vob", "swf", "webm", "ts"}),

    /**
     * 音频
     */
    AUDIO("audio", new String[]{"mp3", "ogg", "wav", "ape", "cda", "au", "midi", "mac", "aac", "flac", "wma"}),

    /**
     * 文档
     */
    DOC("doc", new String[]{"pptx", "docx", "xlsx", "doc", "wps", "xls", "ppt", "txt", "sql", "htm", "html", "pdf", "dwg", "md", "json", "xml", "csv"}),

    /**
     * 压缩包
     */
    ZIP("zip", new String[]{"rar", "zip", "cab", "arj", "7z", "tar", "gz", "gzip", "jar", "iso", "z", "uue", "ace", "lzh", "bz2", "bz", "xz"}),

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
