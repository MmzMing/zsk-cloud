package com.zsk.system.api;

import com.zsk.common.core.constant.ServiceNameConstants;
import com.zsk.common.core.domain.R;
import com.zsk.system.api.domain.SysDictDataApi;
import com.zsk.system.api.factory.RemoteDictFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * 字典服务
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@FeignClient(contextId = "remoteDictService", value = ServiceNameConstants.SYSTEM_SERVICE, fallbackFactory = RemoteDictFallbackFactory.class, url = "http://127.0.0.1:20010")
public interface RemoteDictService {

    /**
     * 根据字典类型查询字典数据
     *
     * @param dictType 字典类型
     * @return 字典数据列表
     */
    @GetMapping("/dict/data/type/{dictType}")
    R<List<SysDictDataApi>> getDictDataByType(@PathVariable("dictType") String dictType);
}
