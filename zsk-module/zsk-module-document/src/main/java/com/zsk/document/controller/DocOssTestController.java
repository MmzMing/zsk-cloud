package com.zsk.document.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.zsk.common.core.domain.R;
import com.zsk.common.oss.core.OssTemplate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * OSS上传测试接口
 *
 * @author wuhuaming
 * @date 2026-02-14
 * @version 1.0
 */
@Tag(name = "OSS上传测试")
@Slf4j
@RestController
@RequestMapping("/oss/test")
@RequiredArgsConstructor
public class DocOssTestController {

    private final OssTemplate ossTemplate;

    /**
     * 测试文件上传
     *
     * @param file 待上传文件
     * @return 文件访问URL
     */
    @Operation(summary = "测试文件上传")
    @PostMapping("/upload")
    public R<String> testUpload(@RequestPart("file") MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String suffix = FileUtil.getSuffix(originalFilename);
            String fileName = IdUtil.simpleUUID() + "." + suffix;
            String contentType = file.getContentType();
            
            log.info("开始测试OSS上传: originalFilename={}, fileName={}, contentType={}", 
                    originalFilename, fileName, contentType);

            // 执行上传
            try (InputStream is = file.getInputStream()) {
                ossTemplate.putObject(fileName, is, contentType);
            }

            // 获取访问地址
            String url = ossTemplate.getObjectUrl(fileName);
            
            log.info("OSS上传测试成功: url={}", url);
            return R.ok("上传成功", url);
        } catch (Exception e) {
            log.error("OSS上传测试失败", e);
            return R.fail("上传失败：" + e.getMessage());
        }
    }
}
