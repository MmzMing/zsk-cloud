package com.zsk.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsk.system.domain.SysTask;
import com.zsk.system.domain.dto.SysTaskCreateDTO;
import com.zsk.system.domain.dto.SysTaskUpdateDTO;
import com.zsk.system.domain.vo.SysTaskListVO;
import com.zsk.system.domain.vo.SysTaskVO;

import java.util.List;

/**
 * 任务管理 服务层
 *
 * @author wuhuaming
 */
public interface ISysTaskService extends IService<SysTask> {

    /**
     * 获取任务列表（含依赖关系），用于 Gantt 图渲染
     *
     * @return Gantt 数据（tasks + links）
     */
    SysTaskListVO listTasksWithLinks();

    /**
     * 获取单个任务详情
     *
     * @param id 任务ID
     * @return 任务视图对象
     */
    SysTaskVO getTaskDetail(Long id);

    /**
     * 创建任务
     *
     * @param dto 创建请求
     * @return 创建后的任务视图对象（含生成的 id）
     */
    SysTaskVO createTask(SysTaskCreateDTO dto);

    /**
     * 更新任务
     *
     * @param dto 更新请求
     */
    void updateTask(SysTaskUpdateDTO dto);

    /**
     * 删除任务（含子任务及相关依赖关系）
     *
     * @param ids 任务ID列表
     */
    void deleteTaskByIds(List<Long> ids);
}
