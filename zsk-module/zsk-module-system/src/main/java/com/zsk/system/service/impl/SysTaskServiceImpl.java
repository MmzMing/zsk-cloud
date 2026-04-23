package com.zsk.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsk.common.core.exception.BusinessException;
import com.zsk.system.domain.SysTask;
import com.zsk.system.domain.dto.SysTaskCreateDTO;
import com.zsk.system.domain.dto.SysTaskUpdateDTO;
import com.zsk.system.domain.vo.SysTaskLinkVO;
import com.zsk.system.domain.vo.SysTaskListVO;
import com.zsk.system.domain.vo.SysTaskVO;
import com.zsk.system.mapper.SysTaskMapper;
import com.zsk.system.service.ISysTaskLinkService;
import com.zsk.system.service.ISysTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 任务管理服务层实现
 * <p>
 * 负责任务的增删改查，支持树形结构和依赖关系管理
 *
 * @author wuhuaming
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysTaskServiceImpl extends ServiceImpl<SysTaskMapper, SysTask> implements ISysTaskService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    

    private final ISysTaskLinkService taskLinkService;

    /**
     * 获取任务列表及依赖关系
     * <p>
     * 用于 Gantt 图渲染，返回所有任务及其依赖关系
     *
     * @return 任务列表及依赖关系封装对象
     */
    @Override
    public SysTaskListVO listTasksWithLinks() {
        log.info("获取任务列表及依赖关系");
        
        // 查询所有任务记录
        List<SysTask> tasks = this.list();
        
        // 将任务实体列表转换为 VO 列表
        List<SysTaskVO> taskVoList = tasks.stream().map(this::toTaskVO).toList();
        
        // 获取所有任务依赖关系
        List<SysTaskLinkVO> linkVoList = taskLinkService.listLinks();

        // 封装返回结果
        SysTaskListVO result = new SysTaskListVO();
        result.setTasks(taskVoList);
        result.setLinks(linkVoList);
        
        return result;
    }

    /**
     * 获取任务详情
     *
     * @param id 任务ID
     * @return 任务详情视图对象
     * @throws BusinessException 任务不存在时抛出
     */
    @Override
    public SysTaskVO getTaskDetail(Long id) {
        log.info("获取任务详情, id={}", id);
        
        // 根据ID查询任务实体
        SysTask task = this.getById(id);
        
        // 校验任务是否存在
        if (task == null) {
            log.warn("任务不存在, id={}", id);
            throw new BusinessException("任务不存在");
        }
        
        // 转换为VO并返回
        return toTaskVO(task);
    }

    /**
     * 创建任务
     * <p>
     * 创建新任务，支持设置父子关系
     *
     * @param dto 任务创建参数
     * @return 创建后的任务视图对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysTaskVO createTask(SysTaskCreateDTO dto) {
        log.info("创建任务, text={}", dto.getText());
        
        // 初始化任务实体
        SysTask task = new SysTask();
        
        // 设置任务名称
        task.setText(dto.getText());
        
        // 解析开始时间（yyyy-MM-dd 格式字符串转 LocalDateTime）
        if (StrUtil.isNotBlank(dto.getStartDate())) {
            LocalDate date = LocalDate.parse(dto.getStartDate(), DATE_FORMATTER);
            task.setStartDate(LocalDateTime.of(date, LocalTime.MIN));
        }
        
        // 设置持续时间
        task.setDuration(dto.getDuration());
        
        // 设置进度（默认0）
        task.setProgress(dto.getProgress() != null ? dto.getProgress() : 0);
        
        // 设置任务类型
        task.setType(dto.getType());
        
        // 设置父任务ID（默认0表示无父任务）
        task.setParentId(dto.getParent() != null ? dto.getParent() : 0L);
        
        // 设置展开标志（默认展开）
        task.setOpenFlag(1);
        
        // 设置任务详情描述
        task.setDetails(dto.getDetails());

        // 设置任务颜色（前端控制，后端不限制）
        task.setColor(dto.getColor());

        // 保存任务到数据库
        this.save(task);
        
        log.info("任务创建成功, id={}", task.getId());
        
        // 转换为VO并返回
        return toTaskVO(task);
    }

    /**
     * 更新任务
     * <p>
     * 支持部分字段更新，仅更新DTO中非空字段
     *
     * @param dto 任务更新参数
     * @throws BusinessException 任务不存在时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTask(SysTaskUpdateDTO dto) {
        log.info("更新任务, id={}", dto.getId());
        
        // 根据ID查询任务实体
        SysTask task = this.getById(dto.getId());
        
        // 校验任务是否存在
        if (task == null) {
            log.warn("任务不存在, id={}", dto.getId());
            throw new BusinessException("任务不存在");
        }

        // 条件更新：仅更新非空字段
        if (StrUtil.isNotBlank(dto.getText())) {
            task.setText(dto.getText());
        }
        if (StrUtil.isNotBlank(dto.getStartDate())) {
            LocalDate date = LocalDate.parse(dto.getStartDate(), DATE_FORMATTER);
            task.setStartDate(LocalDateTime.of(date, LocalTime.MIN));
        }
        if (dto.getDuration() != null) {
            task.setDuration(dto.getDuration());
        }
        if (dto.getProgress() != null) {
            task.setProgress(dto.getProgress());
        }
        if (StrUtil.isNotBlank(dto.getType())) {
            task.setType(dto.getType());
        }
        if (dto.getParent() != null) {
            task.setParentId(dto.getParent());
        }
        if (dto.getDetails() != null) {
            task.setDetails(dto.getDetails());
        }
        if (dto.getColor() != null) {
            task.setColor(dto.getColor());
        }

        // 更新任务到数据库
        this.updateById(task);
        
        log.info("任务更新成功, id={}", dto.getId());
    }

    /**
     * 批量删除任务
     * <p>
     * 级联删除子任务及相关依赖关系
     *
     * @param ids 任务ID列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTaskByIds(List<Long> ids) {
        log.info("删除任务, ids={}", ids);
        
        // 空列表直接返回
        if (CollUtil.isEmpty(ids)) {
            return;
        }

        // 收集所有要删除的任务ID（包括子任务）
        List<Long> allIds = new ArrayList<>(ids);
        collectChildTaskIds(ids, allIds);

        // 先删除相关依赖关系
        taskLinkService.deleteLinksByTaskIds(allIds);
        
        // 再删除任务
        this.removeByIds(allIds);

        log.info("任务删除成功, 删除数量={}", allIds.size());
    }

    /**
     * 递归收集子任务ID
     * <p>
     * 使用深度优先递归，收集指定父任务下的所有层级子任务ID
     *
     * @param parentIds 父任务ID列表
     * @param result    收集结果（会被更新）
     */
    private void collectChildTaskIds(List<Long> parentIds, List<Long> result) {
        // 构建查询条件：查询指定父任务ID下的子任务，仅选择ID字段
        LambdaQueryWrapper<SysTask> wrapper = Wrappers.<SysTask>lambdaQuery()
                .in(SysTask::getParentId, parentIds)
                .select(SysTask::getId);
        
        // 执行查询并提取子任务ID列表
        List<Long> childIds = this.list(wrapper).stream().map(SysTask::getId).toList();

        // 如果存在子任务，继续递归收集
        if (CollUtil.isNotEmpty(childIds)) {
            result.addAll(childIds);
            collectChildTaskIds(childIds, result);
        }
    }

    /**
     * SysTask 实体转 SysTaskVO
     *
     * @param task 任务实体
     * @return 任务视图对象
     */
    private SysTaskVO toTaskVO(SysTask task) {
        SysTaskVO vo = new SysTaskVO();
        vo.setId(task.getId());
        vo.setText(task.getText());
        vo.setStartDate(task.getStartDate() != null ? task.getStartDate().toLocalDate().format(DATE_FORMATTER) : null);
        vo.setDuration(task.getDuration());
        vo.setProgress(task.getProgress());
        vo.setType(task.getType());
        vo.setParent(task.getParentId());
        vo.setOpen(task.getOpenFlag() != null && task.getOpenFlag() == 1);
        vo.setDetails(task.getDetails());
        vo.setColor(task.getColor());
        return vo;
    }
}