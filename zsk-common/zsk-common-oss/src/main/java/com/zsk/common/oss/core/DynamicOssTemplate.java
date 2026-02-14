package com.zsk.common.oss.core;

import com.aliyun.oss.OSSClientBuilder;
import com.zsk.common.oss.model.OssPart;
import com.zsk.common.oss.properties.OssProperties;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.List;

/**
 * 动态OSS操作模板，支持运行时切换
 *
 * @author wuhuaming
 * @date 2026-02-14
 * @version 1.0
 */
@Slf4j
public class DynamicOssTemplate implements OssTemplate {

    /** 当前使用的模板实现 */
    private volatile OssTemplate currentTemplate;
    
    /** 当前OSS配置属性 */
    private volatile OssProperties currentProperties;

    /**
     * 构造方法
     *
     * @param properties OSS配置属性
     */
    public DynamicOssTemplate(OssProperties properties) {
        this.currentProperties = properties;
        refresh(properties);
    }

    /**
     * 刷新配置
     *
     * @param properties 新配置
     */
    public synchronized void refresh(OssProperties properties) {
        log.info("Refreshing OSS Template with type: {}", properties.getType());
        this.currentProperties = properties;
        if ("minio".equals(properties.getType())) {
            MinioClient minioClient = MinioClient.builder()
                    .endpoint(properties.getEndpoint())
                    .credentials(properties.getAccessKey(), properties.getSecretKey())
                    .build();
            this.currentTemplate = new MinioTemplate(minioClient, properties);
        } else if ("aliyun".equals(properties.getType())) {
            com.aliyun.oss.OSS ossClient = new OSSClientBuilder().build(
                    properties.getEndpoint(),
                    properties.getAccessKey(),
                    properties.getSecretKey());
            this.currentTemplate = new AliyunTemplate(ossClient, properties);
        } else {
            throw new IllegalArgumentException("Unsupported OSS type: " + properties.getType());
        }
    }

    /**
     * 获取当前配置属性
     *
     * @return OssProperties
     */
    public OssProperties getProperties() {
        return currentProperties;
    }

    /**
     * 创建存储桶
     *
     * @param bucketName 桶名称
     */
    @Override
    public void makeBucket(String bucketName) {
        currentTemplate.makeBucket(bucketName);
    }

    /**
     * 删除存储桶
     *
     * @param bucketName 桶名称
     */
    @Override
    public void removeBucket(String bucketName) {
        currentTemplate.removeBucket(bucketName);
    }

    /**
     * 存储桶是否存在
     *
     * @param bucketName 桶名称
     * @return boolean 是否存在
     */
    @Override
    public boolean bucketExists(String bucketName) {
        return currentTemplate.bucketExists(bucketName);
    }

    /**
     * 文件上传
     *
     * @param bucketName 桶名称
     * @param objectName 文件名
     * @param stream     输入流
     * @param contentType 文件类型
     */
    @Override
    public void putObject(String bucketName, String objectName, InputStream stream, String contentType) {
        currentTemplate.putObject(bucketName, objectName, stream, contentType);
    }

    /**
     * 文件上传（使用默认桶）
     *
     * @param objectName 文件名
     * @param stream     输入流
     * @param contentType 文件类型
     */
    @Override
    public void putObject(String objectName, InputStream stream, String contentType) {
        currentTemplate.putObject(objectName, stream, contentType);
    }

    /**
     * 获取文件
     *
     * @param bucketName 桶名称
     * @param objectName 文件名
     * @return InputStream 文件流
     */
    @Override
    public InputStream getObject(String bucketName, String objectName) {
        return currentTemplate.getObject(bucketName, objectName);
    }

    /**
     * 获取文件URL
     *
     * @param bucketName 桶名称
     * @param objectName 文件名
     * @return String 文件URL
     */
    @Override
    public String getObjectUrl(String bucketName, String objectName) {
        return currentTemplate.getObjectUrl(bucketName, objectName);
    }

    /**
     * 获取文件URL（使用默认桶）
     *
     * @param objectName 文件名
     * @return String 文件URL
     */
    @Override
    public String getObjectUrl(String objectName) {
        return currentTemplate.getObjectUrl(objectName);
    }

    /**
     * 删除文件
     *
     * @param bucketName 桶名称
     * @param objectName 文件名
     */
    @Override
    public void removeObject(String bucketName, String objectName) {
        currentTemplate.removeObject(bucketName, objectName);
    }

    /**
     * 初始化分片上传
     *
     * @param bucketName 桶名称
     * @param objectName 文件名
     * @param contentType 文件类型
     * @return String uploadId
     */
    @Override
    public String initiateMultipartUpload(String bucketName, String objectName, String contentType) {
        return currentTemplate.initiateMultipartUpload(bucketName, objectName, contentType);
    }

    /**
     * 上传分片
     *
     * @param bucketName 桶名称
     * @param objectName 文件名
     * @param uploadId 上传ID
     * @param partNumber 分片号
     * @param stream 输入流
     * @param size 分片大小
     * @return String ETag
     */
    @Override
    public String uploadPart(String bucketName, String objectName, String uploadId, int partNumber, InputStream stream, long size) {
        return currentTemplate.uploadPart(bucketName, objectName, uploadId, partNumber, stream, size);
    }

    /**
     * 完成分片上传
     *
     * @param bucketName 桶名称
     * @param objectName 文件名
     * @param uploadId 上传ID
     * @param parts 分片列表
     */
    @Override
    public void completeMultipartUpload(String bucketName, String objectName, String uploadId, List<OssPart> parts) {
        currentTemplate.completeMultipartUpload(bucketName, objectName, uploadId, parts);
    }

    /**
     * 取消分片上传
     *
     * @param bucketName 桶名称
     * @param objectName 文件名
     * @param uploadId 上传ID
     */
    @Override
    public void abortMultipartUpload(String bucketName, String objectName, String uploadId) {
        currentTemplate.abortMultipartUpload(bucketName, objectName, uploadId);
    }
}
