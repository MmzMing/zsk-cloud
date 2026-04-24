package com.zsk.document.service.impl;

import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsk.common.core.exception.BusinessException;
import com.zsk.common.core.utils.StringUtils;
import com.zsk.common.oss.core.DynamicOssTemplate;
import com.zsk.common.oss.core.OssTemplate;
import com.zsk.common.oss.utils.OssUtils;
import com.zsk.document.domain.DocFiles;
import com.zsk.document.domain.vo.MultipartCompleteRequest;
import com.zsk.document.domain.vo.MultipartInitRequest;
import com.zsk.document.mapper.DocFilesMapper;
import com.zsk.document.service.IDocFilesService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件Service业务层处理
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-14
 */
@Service
@RequiredArgsConstructor
@Slf4j
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
        log.info("开始上传文件, fileName={}", originalFilename);
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
        DocFiles result = saveFileRecord(file, IdUtil.simpleUUID(), objectName, originalFilename, url);
        log.info("文件上传完成, fileName={}, fileId={}", originalFilename, result.getFileId());
        return result;
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
            log.warn("初始化分片上传失败, 参数为空");
            throw new BusinessException("参数不能为空");
        }
        String fileName = request.getFileName();
        String contentType = request.getContentType();
        String md5 = request.getMd5();
        log.info("初始化分片上传, fileName={}, md5={}", fileName, md5);

        if (StringUtils.isEmpty(fileName)) {
            log.warn("初始化分片上传失败, 文件名为空");
            throw new BusinessException("文件名不能为空");
        }
        if (StringUtils.isEmpty(md5)) {
            log.warn("初始化分片上传失败, MD5为空");
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

        log.info("初始化分片上传完成, uploadId={}", uploadId);
        return uploadId;
    }

    /**
     * 上传分片
     *
     * @param uploadId   上传ID
     * @param partNumber 分片号
     * @param stream     输入流
     * @param size       分片大小
     * @return ETag
     */
    @Override
    @SneakyThrows
    public String uploadPart(String uploadId, Integer partNumber, InputStream stream, long size) {
        log.info("上传分片, uploadId={}, partNumber={}", uploadId, partNumber);
        // 查询文件记录
        DocFiles docFile = getByFileId(uploadId);
        if (docFile == null) {
            log.warn("上传分片失败, 文件记录不存在, uploadId={}", uploadId);
            throw new BusinessException("文件记录不存在");
        }

        // 上传分片 (使用记录中的FilePath)
        try (InputStream is = stream) {
            String etag = ossTemplate.uploadPart(docFile.getBucket(), docFile.getFilePath(), uploadId, partNumber, is, size);
            log.info("上传分片完成, uploadId={}, partNumber={}", uploadId, partNumber);
            return etag;
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
        log.info("完成分片上传, uploadId={}", uploadId);

        // 查询文件记录
        DocFiles docFile = getByFileId(uploadId);
        if (docFile == null) {
            log.warn("完成分片上传失败, 文件记录不存在, uploadId={}", uploadId);
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
            log.error("文件校验失败, uploadId={}, expectedMd5={}, actualMd5={}", uploadId, expectedMd5, actualMd5);
            ossTemplate.removeObject(docFile.getBucket(), docFile.getFilePath());
            removeById(docFile.getId());
            throw new BusinessException("文件校验失败：MD5值不一致，文件可能已损坏或被篡改");
        }

        // 更新记录URL
        docFile.setUrl(ossTemplate.getObjectUrl(docFile.getBucket(), docFile.getFilePath()));
        docFile.setStatus(2); // 已上传
        updateById(docFile);
        log.info("完成分片上传成功, uploadId={}", uploadId);
    }

    /**
     * 根据文件ID查询记录
     *
     * @param fileId 文件ID
     * @return 文件记录
     */
    @Override
    public DocFiles getByFileId(String fileId) {
        log.info("根据文件ID查询记录, fileId={}", fileId);
        DocFiles docFile = getOne(new LambdaQueryWrapper<DocFiles>().eq(DocFiles::getFileId, fileId));
        if (docFile == null) {
            log.warn("文件记录不存在, fileId={}", fileId);
        }
        return docFile;
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

    /**
     * 批量删除文件（同时删除OSS文件和数据库记录）
     * <p>
     * 处理流程：
     * 1. 参数校验：检查ID列表是否为空
     * 2. 查询文件记录：根据ID列表查询数据库中的文件记录
     * 3. 删除OSS文件：遍历文件记录，调用OSS模板删除存储桶中的文件
     * 4. 删除数据库记录：最后删除数据库中的文件记录
     * <p>
     * 注意事项：
     * - OSS文件删除失败时仅记录警告日志，不影响数据库删除（保证数据一致性优先）
     * - 支持单个或多个文件ID批量删除
     *
     * @param ids 文件ID列表（数据库主键ID）
     * @return 删除结果：成功返回true，失败返回false
     */
    @Override
    public boolean removeFiles(List<String> ids) {
        log.info("批量删除文件, ids={}", ids);
        // 参数校验：ID列表为空直接返回成功
        if (CollectionUtils.isEmpty(ids)) {
            log.warn("批量删除文件失败, ID列表为空");
            return true;
        }

        // 根据ID列表查询文件记录
        List<DocFiles> fileList = listByIds(ids);
        if (CollectionUtils.isEmpty(fileList)) {
            log.warn("批量删除文件失败, 未找到文件记录, ids={}", ids);
            return true;
        }

        // 遍历文件记录，逐个删除OSS文件
        for (DocFiles docFile : fileList) {
            try {
                // 获取存储桶名称（优先使用记录中的bucket，否则使用默认bucket）
                String bucket = StringUtils.isNotEmpty(docFile.getBucket()) ? docFile.getBucket() : getBucketName();
                String filePath = docFile.getFilePath();
                // 只有当文件路径不为空时才执行OSS删除
                if (StringUtils.isNotEmpty(filePath)) {
                    ossTemplate.removeObject(bucket, filePath);
                    log.info("删除OSS文件成功, bucket={}, path={}", bucket, filePath);
                }
            } catch (Exception e) {
                // OSS删除失败不影响数据库删除，仅记录警告日志
                log.warn("删除OSS文件失败: bucket={}, path={}, error={}", docFile.getBucket(), docFile.getFilePath(), e.getMessage());
            }
        }

        // 删除数据库记录
        boolean result = removeByIds(ids);
        log.info("批量删除文件完成, 删除{}条记录", ids.size());
        return result;
    }
}
