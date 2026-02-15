package com.zsk.common.oss.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.zsk.common.core.enums.FileType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.InputStream;
import java.util.Date;

/**
 * OSS工具类
 *
 * @author wuhuaming
 * @date 2026-02-14
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OssUtils {

    /**
     * 获取文件MD5
     *
     * @param inputStream 文件输入流
     * @return MD5值
     */
    public static String getMd5(InputStream inputStream) {
        return DigestUtil.md5Hex(inputStream);
    }

    /**
     * 获取文件存储路径
     * 规则：类型名称/MD5首字符/MD5第二字符/年/月/日/MD5完整值.扩展名
     *
     * @param fileName 文件名
     * @param md5      文件MD5值
     * @return 文件路径
     */
    public static String getPath(String fileName, String md5) {
        // 1. 获取扩展名
        String extension = FileNameUtil.extName(fileName);
        // 2. 获取类型名称
        FileType fileType = FileType.getByExtension(extension);
        String typeName = fileType.getName();

        // 3. 校验MD5，为空则生成随机UUID
        if (StrUtil.isBlank(md5)) {
            md5 = IdUtil.simpleUUID();
        }
        // 确保MD5长度至少为2
        if (md5.length() < 2) {
            md5 = StrUtil.padPre(md5, 2, "0");
        }

        // 4. 构建路径
        // 子目录：MD5首字符/MD5第二字符
        String subDir = md5.substring(0, 1) + "/" + md5.substring(1, 2);
        // 时间：年/月/日
        String datePath = DateUtil.format(new Date(), "yyyy/MM/dd");

        // 完整路径：类型名称/MD5首字符/MD5第二字符/年/月/日/MD5.扩展名
        return StrUtil.format("{}/{}/{}/{}.{}", typeName, subDir, datePath, md5, extension);
    }

    /**
     * 获取分块文件存储路径
     * 规则：在文件路径基础上增加 /chunk/
     *
     * @param filePath 原文件路径
     * @return 分块路径
     */
    public static String getChunkPath(String filePath) {
        return filePath + "/chunk/";
    }

    /**
     * 获取分块文件存储路径 (根据文件名和MD5)
     *
     * @param fileName 文件名
     * @param md5      文件MD5值
     * @return 分块路径
     */
    public static String getChunkPath(String fileName, String md5) {
        return getPath(fileName, md5) + "/chunk/";
    }

    /**
     * 获取文件后缀
     *
     * @param fileName 文件名
     * @return 后缀名
     */
    public static String getExtension(String fileName) {
        return FileNameUtil.extName(fileName);
    }

    /**
     * 获取文件类型名称
     *
     * @param fileName 文件名
     * @return 类型名称
     */
    public static String getTypeName(String fileName) {
        return FileType.getByExtension(FileNameUtil.extName(fileName)).getName();
    }

    /**
     * 获取文件MIME类型
     *
     * @param fileName 文件名
     * @return MIME类型
     */
    public static String getContentType(String fileName) {
        return FileUtil.getMimeType(fileName);
    }
}
