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
 * @version 1.0
 * @date 2026-02-15
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

    /**
     * 根据字典类型和字典值查询字典标签
     * <p>
     * 用于业务服务将字典值转换为中文标签返回给前端，
     * 前端无需再进行字典转换。
     * 例如：查询 sys_user_sex 的 "1" 对应 "男"。
     *
     * @param dictType  字典类型（如 sys_user_sex）
     * @param dictValue 字典值（如 1）
     * @return 字典标签（如 男），未找到时返回 null
     */
    @GetMapping("/dict/data/label/{dictType}/{dictValue}")
    R<String> getDictLabel(@PathVariable("dictType") String dictType, @PathVariable("dictValue") String dictValue);
}
