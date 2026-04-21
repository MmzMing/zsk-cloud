package com.zsk.system.controller;

import com.zsk.common.core.domain.R;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.system.domain.dto.SysLogQueryDTO;
import com.zsk.system.domain.vo.SysRecentLogVo;
import com.zsk.system.service.ISysLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     * 分页查询管理日志
     *
     * @param pageQuery 分页参数
     * @param queryDTO 查询条件
     * @return 分页日志列表
     */
    @Operation(summary = "分页查询管理日志")
    @GetMapping("/page")
    public R<PageResult<SysRecentLogVo>> page(PageQuery pageQuery, SysLogQueryDTO queryDTO) {
        return R.ok(logService.pageLogs(pageQuery, queryDTO));
    }

    /**
     * 批量删除管理日志
     *
     * @param ids 日志ID列表
     * @return 操作结果
     */
    @Operation(summary = "批量删除管理日志")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<String> ids) {
        return logService.deleteLogByIds(ids) ? R.ok() : R.fail();
    }
}
