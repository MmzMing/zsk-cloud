package com.zsk.system.controller;

import com.zsk.common.core.domain.R;
import com.zsk.system.domain.vo.SysRecentLogResponseVo;
import com.zsk.system.service.ISysLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理日志 控制器
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Tag(name = "管理日志")
@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
public class SysLogController {

    private final ISysLogService logService;

    /**
     * 获取最近管理日志
     *
     * @param category 分类（content/user/system）
     * @param page 页码
     * @param pageSize 每页数量
     * @return 日志列表
     */
    @Operation(summary = "获取最近管理日志")
    @GetMapping("/recent")
    public R<SysRecentLogResponseVo> getRecentLogs(
        @Parameter(description = "分类（content/user/system）")
        @RequestParam(value = "category", required = false) String category,
        @Parameter(description = "页码")
        @RequestParam(value = "page", defaultValue = "1") Integer page,
        @Parameter(description = "每页数量")
        @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        return R.ok(logService.getRecentLogs(category, page, pageSize));
    }
}
