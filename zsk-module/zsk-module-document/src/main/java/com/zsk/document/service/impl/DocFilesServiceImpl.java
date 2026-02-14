package com.zsk.document.service.impl;

import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsk.common.core.exception.BusinessException;
import com.zsk.common.oss.core.DynamicOssTemplate;
import com.zsk.common.oss.core.OssTemplate;
import com.zsk.common.oss.utils.OssUtils;
import com.zsk.common.core.utils.StringUtils;
import com.zsk.document.domain.DocFiles;
import com.zsk.document.domain.vo.MultipartCompleteRequest;
import com.zsk.document.domain.vo.MultipartInitRequest;
import com.zsk.document.mapper.DocFilesMapper;
import com.zsk.document.service.IDocFilesService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;

/**
 * 文件Service业务层处理
 * 
 * @author wuhuaming
 * @date 2026-02-14
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class DocFilesServiceImpl extends ServiceImpl<DocFilesMapper, DocFiles> implements IDocFilesService {

    private final OssTemplate ossTemplate;

    /**
     * 上传文件
     *
     * @param file 文件
     * @return 结果
     */
    @Override
    @SneakyThrows
    public DocFiles uploadFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        // 计算MD5
        String md5;
        try (InputStream is = file.getInputStream()) {
            md5 = OssUtils.getMd5(is);
        }
        
        // 生成存储路径
        String objectName = OssUtils.getPath(originalFilename, md5);
        
        // 上传到OSS
        try (InputStream is = file.getInputStream()) {
            ossTemplate.putObject(objectName, is, file.getContentType());
        }
        
        // 获取URL
        String url = ossTemplate.getObjectUrl(objectName);
        
        // 保存记录
        return saveFileRecord(file, IdUtil.simpleUUID(), objectName, originalFilename, url);
    }

    /**
     * 初始化分片上传
     *
     * @param request 请求参数
     * @return uploadId
     */
    @Override
    @SneakyThrows
    public String initiateMultipartUpload(MultipartInitRequest request) {
        if (request == null) {
            throw new BusinessException("参数不能为空");
        }
        String fileName = request.getFileName();
        String contentType = request.getContentType();
        String md5 = request.getMd5();

        if (StringUtils.isEmpty(fileName)) {
            throw new BusinessException("文件名不能为空");
        }
        if (StringUtils.isEmpty(md5)) {
            throw new BusinessException("文件MD5不能为空");
        }
        // 生成存储路径
        String objectName = OssUtils.getPath(fileName, md5);
        
        // 获取BucketName
        String bucketName = getBucketName();
        
        // 初始化分片上传
        String uploadId = ossTemplate.initiateMultipartUpload(bucketName, objectName, contentType);
        
        // 保存初始化记录 (状态为0-上传中)
        DocFiles docFile = new DocFiles();
        docFile.setFileId(uploadId); // 使用uploadId作为fileId
        docFile.setFileName(fileName);
        docFile.setFilePath(objectName);
        docFile.setFileType(OssUtils.getExtension(fileName));
        docFile.setCreateTime(LocalDateTime.now());
        docFile.setBucket(bucketName);
        docFile.setStatus(1); // 上传中
        save(docFile);
        
        return uploadId;
    }

    /**
     * 上传分片
     *
     * @param uploadId 上传ID
     * @param partNumber 分片号
     * @param stream   输入流
     * @param size     分片大小
     * @return ETag
     */
    @Override
    @SneakyThrows
    public String uploadPart(String uploadId, Integer partNumber, InputStream stream, long size) {
        // 查询文件记录
        DocFiles docFile = getByFileId(uploadId);
        if (docFile == null) {
            throw new BusinessException("文件记录不存在");
        }
        
        // 上传分片 (使用记录中的FilePath)
        try (InputStream is = stream) {
            return ossTemplate.uploadPart(docFile.getBucket(), docFile.getFilePath(), uploadId, partNumber, is, size);
        }
    }

    /**
     * 完成分片上传
     *
     * @param request 请求参数
     */
    @Override
    @SneakyThrows
    public void completeMultipartUpload(MultipartCompleteRequest request) {
        String uploadId = request.getUploadId();
        
        // 查询文件记录
        DocFiles docFile = getByFileId(uploadId);
        if (docFile == null) {
            throw new BusinessException("文件记录不存在");
        }
        
        // 完成分片上传
        ossTemplate.completeMultipartUpload(docFile.getBucket(), docFile.getFilePath(), uploadId, request.getParts());
        
        // 校验文件MD5
        // 1. 获取路径中的MD5 (文件名就是MD5)
        String expectedMd5 = FileNameUtil.mainName(docFile.getFilePath());
        
        // 2. 计算实际文件的MD5
        String actualMd5;
        try (InputStream is = ossTemplate.getObject(docFile.getBucket(), docFile.getFilePath())) {
            actualMd5 = OssUtils.getMd5(is);
        }
        
        // 3. 比较MD5
        if (!expectedMd5.equalsIgnoreCase(actualMd5)) {
            // MD5不一致，删除文件和记录
            ossTemplate.removeObject(docFile.getBucket(), docFile.getFilePath());
            removeById(docFile.getId());
            throw new BusinessException("文件校验失败：MD5值不一致，文件可能已损坏或被篡改");
        }
        
        // 更新记录URL
        docFile.setUrl(ossTemplate.getObjectUrl(docFile.getBucket(), docFile.getFilePath()));
        docFile.setStatus(2); // 已上传
        updateById(docFile);
    }

    /**
     * 根据文件ID查询记录
     *
     * @param fileId 文件ID
     * @return 文件记录
     */
    private DocFiles getByFileId(String fileId) {
        return getOne(new LambdaQueryWrapper<DocFiles>().eq(DocFiles::getFileId, fileId));
    }

    /**
     * 保存文件记录
     *
     * @param file             文件对象
     * @param fileId           文件ID
     * @param objectName       存储对象名称
     * @param originalFilename 原始文件名
     * @param url              访问地址
     * @return 文件记录
     */
    private DocFiles saveFileRecord(MultipartFile file, String fileId, String objectName, String originalFilename, String url) {
        DocFiles docFile = new DocFiles();
        docFile.setFileId(fileId);
        docFile.setFileName(originalFilename);
        docFile.setFilePath(objectName);
        docFile.setFileType(OssUtils.getExtension(originalFilename));
        docFile.setFileSize(file.getSize());
        docFile.setCreateTime(LocalDateTime.now());
        docFile.setUrl(url);
        docFile.setBucket(getBucketName());
        docFile.setStatus(2); // 已上传
        save(docFile);
        return docFile;
    }

    /**
     * 获取存储桶名称
     *
     * @return 存储桶名称
     */
    private String getBucketName() {
        if (ossTemplate instanceof DynamicOssTemplate) {
            return ((DynamicOssTemplate) ossTemplate).getProperties().getBucketName();
        }
        // 默认处理
        return "default"; 
    }
}
