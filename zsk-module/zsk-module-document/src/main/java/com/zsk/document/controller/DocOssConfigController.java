package com.zsk.document.controller;

import com.zsk.common.core.domain.R;
import com.zsk.common.oss.core.DynamicOssTemplate;
import com.zsk.common.oss.properties.OssProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * OSS配置管理
 *
 * @author wuhuaming
 * @date 2026-02-14
 * @version 1.0
 */
@Tag(name = "OSS配置管理")
@RestController
@RequestMapping("/oss/config")
@RequiredArgsConstructor
public class DocOssConfigController {

    private final DynamicOssTemplate dynamicOssTemplate;

    /**
     * 获取当前OSS配置
     */
    @Operation(summary = "获取当前OSS配置")
    @GetMapping
    public R<OssProperties> getConfig() {
        return R.ok(dynamicOssTemplate.getProperties());
    }

    /**
     * 更新OSS配置
     */
    @Operation(summary = "更新OSS配置")
    @PostMapping
    public R<Void> updateConfig(@RequestBody OssProperties properties) {
        dynamicOssTemplate.refresh(properties);
        return R.ok();
    }
}
