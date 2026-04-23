package com.zsk.system.controller;

import com.zsk.common.core.domain.R;
import com.zsk.system.domain.dto.SysTaskCreateDTO;
import com.zsk.system.domain.dto.SysTaskUpdateDTO;
import com.zsk.system.domain.vo.SysTaskListVO;
import com.zsk.system.domain.vo.SysTaskVO;
import com.zsk.system.service.ISysTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 任务管理控制器
 * <p>
 * 提供任务的增删改查接口，支持 Gantt 图数据渲染
 *
 * @author wuhuaming
 */
@Tag(name = "任务管理")
@Slf4j
@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
public class SysTaskController {

    private final ISysTaskService taskService;

    /**
     * 获取任务列表（含依赖关系）
     * <p>
     * 返回所有任务及其依赖关系，用于 Gantt 图渲染
     *
     * @return 任务列表及依赖关系
     */
    @Operation(summary = "获取任务列表（含依赖关系）")
    @GetMapping("/list")
    public R<SysTaskListVO> list() {
        log.info("获取任务列表（含依赖关系）");
        return R.ok(taskService.listTasksWithLinks());
    }

    /**
     * 获取单个任务详情
     *
     * @param id 任务ID
     * @return 任务详情
     */
    @Operation(summary = "获取单个任务详情")
    @GetMapping("/{id}")
    public R<SysTaskVO> getInfo(@PathVariable Long id) {
        log.info("获取任务详情, id={}", id);
        return R.ok(taskService.getTaskDetail(id));
    }

    /**
     * 创建任务
     *
     * @param dto 任务创建参数
     * @return 创建后的任务信息
     */
    @Operation(summary = "创建任务")
    @PostMapping
    public R<SysTaskVO> add(@Valid @RequestBody SysTaskCreateDTO dto) {
        log.info("创建任务, text={}", dto.getText());
        return R.ok(taskService.createTask(dto));
    }

    /**
     * 更新任务
     *
     * @param dto 任务更新参数
     * @return 无返回值
     */
    @Operation(summary = "更新任务")
    @PutMapping
    public R<Void> edit(@Valid @RequestBody SysTaskUpdateDTO dto) {
        log.info("更新任务, id={}", dto.getId());
        taskService.updateTask(dto);
        return R.ok();
    }

    /**
     * 删除任务（含子任务及相关依赖关系）
     *
     * @param ids 任务ID列表
     * @return 无返回值
     */
    @Operation(summary = "删除任务")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        log.info("删除任务, ids={}", ids);
        taskService.deleteTaskByIds(ids);
        return R.ok();
    }
}
