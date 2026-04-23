package com.zsk.system.controller;

import com.zsk.common.core.domain.R;
import com.zsk.system.domain.dto.SysTaskLinkCreateDTO;
import com.zsk.system.domain.vo.SysTaskLinkVO;
import com.zsk.system.service.ISysTaskLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 任务依赖关系控制器
 * <p>
 * 提供任务依赖关系的增删查接口，支持循环依赖检测
 *
 * @author wuhuaming
 */
@Tag(name = "任务依赖管理")
@Slf4j
@RestController
@RequestMapping("/task/link")
@RequiredArgsConstructor
public class SysTaskLinkController {

    private final ISysTaskLinkService taskLinkService;

    /**
     * 获取全部任务依赖关系
     *
     * @return 任务依赖关系列表
     */
    @Operation(summary = "获取任务依赖关系列表")
    @GetMapping("/list")
    public R<List<SysTaskLinkVO>> list() {
        log.info("获取任务依赖关系列表");
        return R.ok(taskLinkService.listLinks());
    }

    /**
     * 创建任务依赖
     *
     * @param dto 依赖关系创建参数
     * @return 创建后的依赖关系
     */
    @Operation(summary = "创建任务依赖")
    @PostMapping
    public R<SysTaskLinkVO> add(@Valid @RequestBody SysTaskLinkCreateDTO dto) {
        log.info("创建任务依赖, source={}, target={}", dto.getSource(), dto.getTarget());
        return R.ok(taskLinkService.createLink(dto));
    }

    /**
     * 删除任务依赖
     *
     * @param ids 依赖关系ID列表
     * @return 无返回值
     */
    @Operation(summary = "删除任务依赖")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        log.info("删除任务依赖, ids={}", ids);
        taskLinkService.deleteLinkByIds(ids);
        return R.ok();
    }
}
