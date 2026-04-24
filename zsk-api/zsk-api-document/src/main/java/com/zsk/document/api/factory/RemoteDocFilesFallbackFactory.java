package com.zsk.document.api.factory;

import com.zsk.common.core.domain.R;
import com.zsk.document.api.RemoteDocFilesService;
import com.zsk.document.api.domain.DocFilesApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务降级处理
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-25
 */
@Component
public class RemoteDocFilesFallbackFactory implements FallbackFactory<RemoteDocFilesService> {

    private static final Logger log = LoggerFactory.getLogger(RemoteDocFilesFallbackFactory.class);

    @Override
    public RemoteDocFilesService create(Throwable throwable) {
        log.error("文件服务调用失败:{}", throwable.getMessage());
        return new RemoteDocFilesService() {
            @Override
            public R<DocFilesApi> upload(MultipartFile file, String source) {
                return R.fail("文件上传失败:" + throwable.getMessage());
            }

            @Override
            public R<Boolean> remove(String ids, String source) {
                return R.fail("文件删除失败:" + throwable.getMessage());
            }
        };
    }
}
