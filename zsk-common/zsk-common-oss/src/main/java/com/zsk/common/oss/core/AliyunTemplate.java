package com.zsk.common.oss.core;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.*;
import com.zsk.common.oss.model.OssPart;
import com.zsk.common.oss.properties.OssProperties;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 阿里云OSS实现类
 *
 * @author wuhuaming
 * @date 2026-02-14
 * @version 1.0
 */
@RequiredArgsConstructor
public class AliyunTemplate implements OssTemplate {

    /** 阿里云OSS客户端 */
    private final OSS ossClient;
    
    /** OSS配置属性 */
    private final OssProperties ossProperties;

    /**
     * 创建存储桶
     *
     * @param bucketName 桶名称
     */
    @Override
    public void makeBucket(String bucketName) {
        if (!ossClient.doesBucketExist(bucketName)) {
            ossClient.createBucket(bucketName);
        }
    }

    /**
     * 删除存储桶
     *
     * @param bucketName 桶名称
     */
    @Override
    public void removeBucket(String bucketName) {
        ossClient.deleteBucket(bucketName);
    }

    /**
     * 存储桶是否存在
     *
     * @param bucketName 桶名称
     * @return boolean 是否存在
     */
    @Override
    public boolean bucketExists(String bucketName) {
        return ossClient.doesBucketExist(bucketName);
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
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        ossClient.putObject(bucketName, objectName, stream, metadata);
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
        putObject(ossProperties.getBucketName(), objectName, stream, contentType);
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
        return ossClient.getObject(bucketName, objectName).getObjectContent();
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
        // 如果配置了自定义域名
        if (ossProperties.getDomain() != null && !ossProperties.getDomain().isEmpty()) {
            return "https://" + ossProperties.getDomain() + "/" + objectName;
        }
        // 默认URL生成 (有效期10年)
        Date expiration = new Date(System.currentTimeMillis() + 3600L * 1000 * 24 * 365 * 10);
        URL url = ossClient.generatePresignedUrl(bucketName, objectName, expiration);
        return url.toString();
    }

    /**
     * 获取文件URL（使用默认桶）
     *
     * @param objectName 文件名
     * @return String 文件URL
     */
    @Override
    public String getObjectUrl(String objectName) {
        return getObjectUrl(ossProperties.getBucketName(), objectName);
    }

    /**
     * 删除文件
     *
     * @param bucketName 桶名称
     * @param objectName 文件名
     */
    @Override
    public void removeObject(String bucketName, String objectName) {
        ossClient.deleteObject(bucketName, objectName);
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
        InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketName, objectName);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        request.setObjectMetadata(metadata);
        InitiateMultipartUploadResult result = ossClient.initiateMultipartUpload(request);
        return result.getUploadId();
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
        UploadPartRequest request = new UploadPartRequest();
        request.setBucketName(bucketName);
        request.setKey(objectName);
        request.setUploadId(uploadId);
        request.setPartNumber(partNumber);
        request.setInputStream(stream);
        request.setPartSize(size);
        UploadPartResult result = ossClient.uploadPart(request);
        return result.getPartETag().getETag();
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
        List<PartETag> partETags = parts.stream()
                .map(p -> new PartETag(p.getPartNumber(), p.getEtag()))
                .collect(Collectors.toList());
        
        CompleteMultipartUploadRequest request = new CompleteMultipartUploadRequest(bucketName, objectName, uploadId, partETags);
        ossClient.completeMultipartUpload(request);
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
        AbortMultipartUploadRequest request = new AbortMultipartUploadRequest(bucketName, objectName, uploadId);
        ossClient.abortMultipartUpload(request);
    }
}
