package com.zsk.system.controller;

import com.zsk.common.core.domain.R;
import com.zsk.common.log.domain.OperLog;
import com.zsk.system.service.IOperLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 操作日志 控制器
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Tag(name = "操作日志")
@RestController
@RequestMapping("/operLog")
@RequiredArgsConstructor
public class SysOperLogController {

    private final IOperLogService operLogService;

    /**
     * 查询操作日志列表
     *
     * @param title 模块标题
     * @param operName 操作人员
     * @param status 操作状态
     * @param page 页码
     * @param pageSize 每页大小
     * @return 日志列表及总数
     */
    @Operation(summary = "查询操作日志列表")
    @GetMapping("/list")
    public R<Map<String, Object>> list(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String operName,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        List<OperLog> list = operLogService.selectOperLogList(title, operName, status, page, pageSize);
        long total = operLogService.countOperLog(title, operName, status);

        Map<String, Object> result = new HashMap<>();
        result.put("rows", list);
        result.put("total", total);
        return R.ok(result);
    }

    /**
     * 获取操作日志详细信息
     *
     * @param id 日志ID
     * @return 日志详情
     */
    @Operation(summary = "获取操作日志详细信息")
    @GetMapping("/{id}")
    public R<OperLog> getInfo(@PathVariable String id) {
        return R.ok(operLogService.selectOperLogList(null, null, null, 1, 1)
                .stream()
                .filter(log -> id.equals(log.getId()))
                .findFirst()
                .orElse(null));
    }

    /**
     * 批量删除操作日志
     *
     * @param ids 日志ID列表
     * @return 是否成功
     */
    @Operation(summary = "批量删除操作日志")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<String> ids) {
        return operLogService.deleteOperLogByIds(ids) ? R.ok() : R.fail();
    }

    /**
     * 清空操作日志
     *
     * @return 是否成功
     */
    @Operation(summary = "清空操作日志")
    @DeleteMapping("/clear")
    public R<Void> clear() {
        return operLogService.clearOperLog() ? R.ok() : R.fail();
    }
}
