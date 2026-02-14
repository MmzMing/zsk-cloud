package com.zsk.common.oss.core;

import com.google.common.collect.Multimap;
import com.zsk.common.oss.model.OssPart;
import com.zsk.common.oss.properties.OssProperties;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * MinIO实现类
 *
 * @author wuhuaming
 * @date 2026-02-14
 * @version 1.0
 */
@RequiredArgsConstructor
public class MinioTemplate implements OssTemplate {

    /** Minio客户端 */
    private final MinioClient minioClient;
    
    /** OSS配置属性 */
    private final OssProperties ossProperties;


    /**
     * 创建存储桶
     *
     * @param bucketName 桶名称
     */
    @Override
    @SneakyThrows
    public void makeBucket(String bucketName) {
        if (!bucketExists(bucketName)) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }

    /**
     * 删除存储桶
     *
     * @param bucketName 桶名称
     */
    @Override
    @SneakyThrows
    public void removeBucket(String bucketName) {
        minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
    }

    /**
     * 存储桶是否存在
     *
     * @param bucketName 桶名称
     * @return boolean 是否存在
     */
    @Override
    @SneakyThrows
    public boolean bucketExists(String bucketName) {
        return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
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
    @SneakyThrows
    public void putObject(String bucketName, String objectName, InputStream stream, String contentType) {
        if (!bucketExists(bucketName)) {
            makeBucket(bucketName);
        }
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(stream, stream.available(), -1)
                    .contentType(contentType)
                    .build());
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
    @SneakyThrows
    public InputStream getObject(String bucketName, String objectName) {
        return minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build());
    }

    /**
     * 获取文件URL
     *
     * @param bucketName 桶名称
     * @param objectName 文件名
     * @return String 文件URL
     */
    @Override
    @SneakyThrows
    public String getObjectUrl(String bucketName, String objectName) {
        // 如果配置了自定义域名，直接拼接返回
        if (ossProperties.getDomain() != null && !ossProperties.getDomain().isEmpty()) {
            return ossProperties.getDomain() + "/" + bucketName + "/" + objectName;
        }
        // 否则生成预签名URL，默认有效期7天
        return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(bucketName)
                .object(objectName)
                .expiry(7, TimeUnit.DAYS)
                .build());
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
    @SneakyThrows
    public void removeObject(String bucketName, String objectName) {
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build());
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
    @SneakyThrows
    public String initiateMultipartUpload(String bucketName, String objectName, String contentType) {
        if (!bucketExists(bucketName)) {
            makeBucket(bucketName);
        }
        return UUID.randomUUID().toString();
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
    @SneakyThrows
    public String uploadPart(String bucketName, String objectName, String uploadId, int partNumber, InputStream stream, long size) {
        String chunkObjectName = getChunkObjectName(objectName, uploadId, partNumber);
            ObjectWriteResponse response = minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(chunkObjectName)
                    .stream(stream, size, -1)
                    .build());
            return response.etag();
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
    @SneakyThrows
    public void completeMultipartUpload(String bucketName, String objectName, String uploadId, List<OssPart> parts) {
        // 1. 将分片按编号排序并构建合并源
        List<ComposeSource> sources = parts.stream()
                .sorted(Comparator.comparing(OssPart::getPartNumber))
                .map(part -> ComposeSource.builder()
                        .bucket(bucketName)
                        .object(getChunkObjectName(objectName, uploadId, part.getPartNumber()))
                        .build())
                .collect(Collectors.toList());

        // 2. 执行合并对象操作
        minioClient.composeObject(ComposeObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .sources(sources)
                .build());

        // 3. 删除所有临时分片文件
        for (OssPart part : parts) {
            removeObject(bucketName, getChunkObjectName(objectName, uploadId, part.getPartNumber()));
        }
    }

    /**
     * 取消分片上传
     *
     * @param bucketName 桶名称
     * @param objectName 文件名
     * @param uploadId 上传ID
     */
    @Override
    @SneakyThrows
    public void abortMultipartUpload(String bucketName, String objectName, String uploadId) {
        String prefix = ".chunks/" + uploadId + "/";
        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .recursive(true)
                .build());
        for (Result<Item> result : results) {
            removeObject(bucketName, result.get().objectName());
        }
    }

    /**
     * 获取分片对象名称
     *
     * @param objectName 文件名
     * @param uploadId 上传ID
     * @param partNumber 分片号
     * @return String 分片对象名称
     */
    private String getChunkObjectName(String objectName, String uploadId, int partNumber) {
        if (objectName != null && !objectName.isEmpty()) {
            return objectName + "/chunk/" + partNumber;
        }
        return ".chunks/" + uploadId + "/" + partNumber;
    }

}
