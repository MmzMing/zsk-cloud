package com.zsk.document.api;

import com.zsk.common.core.constant.CommonConstants;
import com.zsk.common.core.domain.R;
import com.zsk.document.api.domain.DocFilesApi;
import com.zsk.document.api.factory.RemoteDocFilesFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务远程调用接口
 *
 * @author wuhuaming
 * @date 2026-04-25
 * @version 1.0
 */
@FeignClient(contextId = "remoteDocFilesService", value = "zsk-module-document", fallbackFactory = RemoteDocFilesFallbackFactory.class)
public interface RemoteDocFilesService {

    /**
     * 上传文件
     *
     * @param file   文件
     * @param source 请求来源
     * @return 文件信息
     */
    @PostMapping("/docFiles/upload")
    R<DocFilesApi> upload(@RequestPart("file") MultipartFile file,
                          @RequestHeader(CommonConstants.REQUEST_SOURCE_HEADER) String source);

    /**
     * 删除文件
     *
     * @param ids    文件ID列表
     * @param source 请求来源
     * @return 是否成功
     */
    @DeleteMapping("/docFiles/{ids}")
    R<Boolean> remove(@PathVariable("ids") String ids,
                      @RequestHeader(CommonConstants.REQUEST_SOURCE_HEADER) String source);
}
