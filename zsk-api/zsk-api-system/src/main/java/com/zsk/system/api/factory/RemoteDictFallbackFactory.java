package com.zsk.system.api.factory;

import com.zsk.common.core.domain.R;
import com.zsk.system.api.RemoteDictService;
import com.zsk.system.api.domain.SysDictDataApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 字典服务降级处理
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Component
public class RemoteDictFallbackFactory implements FallbackFactory<RemoteDictService> {
    private static final Logger log = LoggerFactory.getLogger(RemoteDictFallbackFactory.class);

    @Override
    public RemoteDictService create(Throwable throwable) {
        log.error("字典服务调用失败:{}", throwable.getMessage());
        return dictType -> R.fail("获取字典数据失败:" + throwable.getMessage());
    }
}
