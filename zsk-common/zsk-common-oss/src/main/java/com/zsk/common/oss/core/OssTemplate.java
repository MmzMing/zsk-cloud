package com.zsk.common.oss.core;

import com.zsk.common.oss.model.OssPart;

import java.io.InputStream;
import java.util.List;

/**
 * OSS操作模板接口
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-14
 */
public interface OssTemplate {

    /**
     * 创建存储桶
     *
     * @param bucketName 桶名称
     */
    void makeBucket(String bucketName);

    /**
     * 删除存储桶
     *
     * @param bucketName 桶名称
     */
    void removeBucket(String bucketName);

    /**
     * 存储桶是否存在
     *
     * @param bucketName 桶名称
     * @return boolean 是否存在
     */
    boolean bucketExists(String bucketName);

    /**
     * 文件上传
     *
     * @param bucketName  桶名称
     * @param objectName  文件名
     * @param stream      输入流
     * @param contentType 文件类型
     */
    void putObject(String bucketName, String objectName, InputStream stream, String contentType);

    /**
     * 文件上传（使用默认桶）
     *
     * @param objectName  文件名
     * @param stream      输入流
     * @param contentType 文件类型
     */
    void putObject(String objectName, InputStream stream, String contentType);

    /**
     * 获取文件
     *
     * @param bucketName 桶名称
     * @param objectName 文件名
     * @return InputStream 文件流
     */
    InputStream getObject(String bucketName, String objectName);

    /**
     * 获取文件URL
     *
     * @param bucketName 桶名称
     * @param objectName 文件名
     * @return String 文件URL
     */
    String getObjectUrl(String bucketName, String objectName);

    /**
     * 获取文件URL（使用默认桶）
     *
     * @param objectName 文件名
     * @return String 文件URL
     */
    String getObjectUrl(String objectName);

    /**
     * 删除文件
     *
     * @param bucketName 桶名称
     * @param objectName 文件名
     */
    void removeObject(String bucketName, String objectName);

    /**
     * 初始化分片上传
     *
     * @param bucketName  桶名称
     * @param objectName  文件名
     * @param contentType 文件类型
     * @return String uploadId
     */
    String initiateMultipartUpload(String bucketName, String objectName, String contentType);

    /**
     * 上传分片
     *
     * @param bucketName 桶名称
     * @param objectName 文件名
     * @param uploadId   上传ID
     * @param partNumber 分片号
     * @param stream     输入流
     * @param size       分片大小
     * @return String ETag
     */
    String uploadPart(String bucketName, String objectName, String uploadId, int partNumber, InputStream stream, long size);

    /**
     * 完成分片上传
     *
     * @param bucketName 桶名称
     * @param objectName 文件名
     * @param uploadId   上传ID
     * @param parts      分片列表
     */
    void completeMultipartUpload(String bucketName, String objectName, String uploadId, List<OssPart> parts);

    /**
     * 取消分片上传
     *
     * @param bucketName 桶名称
     * @param objectName 文件名
     * @param uploadId   上传ID
     */
    void abortMultipartUpload(String bucketName, String objectName, String uploadId);
}
