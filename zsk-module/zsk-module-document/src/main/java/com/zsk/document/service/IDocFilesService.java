package com.zsk.document.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsk.document.domain.DocFiles;
import com.zsk.document.domain.vo.MultipartCompleteRequest;
import com.zsk.document.domain.vo.MultipartInitRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * 文件Service接口
 * 
 * @author wuhuaming
 * @date 2026-02-14
 * @version 1.0
 */
public interface IDocFilesService extends IService<DocFiles> {
    /**
     * 上传文件
     *
     * @param file 文件
     * @return 结果
     */
    DocFiles uploadFile(MultipartFile file);

    /**
     * 初始化分片上传
     *
     * @param request 请求参数
     * @return uploadId
     */
    String initiateMultipartUpload(MultipartInitRequest request);

    /**
     * 上传分片
     *
     * @param uploadId 上传ID
     * @param partNumber 分片号
     * @param stream   输入流
     * @param size     分片大小
     * @return ETag
     */
    String uploadPart(String uploadId, Integer partNumber, InputStream stream, long size);

    /**
     * 完成分片上传
     *
     * @param request 请求参数
     */
    void completeMultipartUpload(MultipartCompleteRequest request);
}
