package com.zsk.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsk.common.core.exception.BusinessException;
import com.zsk.system.domain.SysTaskLink;
import com.zsk.system.domain.dto.SysTaskLinkCreateDTO;
import com.zsk.system.domain.vo.SysTaskLinkVO;
import com.zsk.system.mapper.SysTaskLinkMapper;
import com.zsk.system.service.ISysTaskLinkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 任务依赖关系服务层实现
 * <p>
 * 负责任务依赖关系的增删查，支持循环依赖检测
 *
 * @author wuhuaming
 */
@Slf4j
@Service
public class SysTaskLinkServiceImpl extends ServiceImpl<SysTaskLinkMapper, SysTaskLink> implements ISysTaskLinkService {

    /**
     * 获取所有任务依赖关系
     *
     * @return 任务依赖关系VO列表
     */
    @Override
    public List<SysTaskLinkVO> listLinks() {
        log.info("获取任务依赖关系列表");
        
        // 查询所有依赖关系并转换为VO
        return this.list().stream().map(this::toLinkVO).toList();
    }

    /**
     * 创建任务依赖关系
     * <p>
     * 执行三项校验：
     * 1. 源任务和目标任务不能相同
     * 2. 不能重复创建相同的依赖关系
     * 3. 不能创建循环依赖
     *
     * @param dto 依赖关系创建参数
     * @return 创建后的依赖关系视图对象
     * @throws BusinessException 校验失败时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysTaskLinkVO createLink(SysTaskLinkCreateDTO dto) {
        log.info("创建任务依赖关系, source={}, target={}", dto.getSource(), dto.getTarget());

        // 校验1：源任务和目标任务不能相同
        if (dto.getSource().equals(dto.getTarget())) {
            log.warn("源任务和目标任务不能相同, source={}", dto.getSource());
            throw new BusinessException("源任务和目标任务不能相同");
        }

        // 校验2：检查是否已存在相同的依赖关系
        LambdaQueryWrapper<SysTaskLink> duplicateCheck = Wrappers.<SysTaskLink>lambdaQuery()
                .eq(SysTaskLink::getSourceId, dto.getSource())
                .eq(SysTaskLink::getTargetId, dto.getTarget());
        if (this.count(duplicateCheck) > 0) {
            log.warn("依赖关系已存在, source={}, target={}", dto.getSource(), dto.getTarget());
            throw new BusinessException("该依赖关系已存在");
        }

        // 校验3：检测循环依赖
        if (hasCircularDependency(dto.getSource(), dto.getTarget())) {
            log.warn("检测到循环依赖, source={}, target={}", dto.getSource(), dto.getTarget());
            throw new BusinessException("不能创建循环依赖");
        }

        // 构建依赖关系实体
        SysTaskLink link = new SysTaskLink();
        link.setSourceId(dto.getSource());
        link.setTargetId(dto.getTarget());
        link.setType(dto.getType());

        // 保存到数据库
        this.save(link);
        
        log.info("任务依赖关系创建成功, id={}", link.getId());
        
        // 转换为VO并返回
        return toLinkVO(link);
    }

    /**
     * 批量删除任务依赖关系
     *
     * @param ids 依赖关系ID列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLinkByIds(List<Long> ids) {
        log.info("删除任务依赖关系, ids={}", ids);
        
        // 空列表直接返回
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        
        // 按ID批量删除
        this.removeByIds(ids);
        
        log.info("任务依赖关系删除成功, 删除数量={}", ids.size());
    }

    /**
     * 按任务ID批量删除相关依赖关系
     * <p>
     * 删除所有与指定任务相关的依赖关系（作为源任务或目标任务）
     *
     * @param taskIds 任务ID列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLinksByTaskIds(List<Long> taskIds) {
        log.info("按任务ID删除依赖关系, taskIds={}", taskIds);
        
        // 空列表直接返回
        if (CollUtil.isEmpty(taskIds)) {
            return;
        }
        
        // 构建查询条件：删除源任务或目标任务在指定列表中的依赖关系
        LambdaQueryWrapper<SysTaskLink> wrapper = Wrappers.<SysTaskLink>lambdaQuery()
                .in(SysTaskLink::getSourceId, taskIds)
                .or()
                .in(SysTaskLink::getTargetId, taskIds);
        
        // 执行删除
        this.remove(wrapper);
        
        log.info("按任务ID删除依赖关系成功");
    }

    /**
     * 检测是否存在循环依赖
     * <p>
     * 使用 BFS（广度优先搜索）算法检测：
     * 从目标任务(target)出发，沿着已有依赖链路遍历，判断能否到达源任务(source)
     * 如果能够到达，则说明添加新依赖后会形成循环
     *
     * @param source 源任务ID（新依赖的起点）
     * @param target 目标任务ID（新依赖的终点）
     * @return 是否存在循环依赖
     */
    private boolean hasCircularDependency(Long source, Long target) {
        // 获取所有已存在的依赖关系
        List<SysTaskLink> allLinks = this.list();

        // 构建邻接表：key为源任务ID，value为目标任务ID列表
        // 用于快速查找某个任务的下游依赖任务
        Map<Long, List<Long>> adjacency = new HashMap<>();
        for (SysTaskLink link : allLinks) {
            adjacency.computeIfAbsent(link.getSourceId(), k -> new ArrayList<>()).add(link.getTargetId());
        }

        // BFS遍历所需的数据结构
        Set<Long> visited = new HashSet<>();  // 记录已访问的任务，避免重复遍历
        Deque<Long> queue = new ArrayDeque<>();  // BFS队列
        
        // 从目标任务开始遍历
        queue.add(target);
        visited.add(target);

        // BFS主循环
        while (!queue.isEmpty()) {
            // 取出队首任务
            Long current = queue.poll();
            
            // 获取当前任务的所有下游依赖任务
            List<Long> neighbors = adjacency.getOrDefault(current, Collections.emptyList());
            
            // 遍历下游任务
            for (Long next : neighbors) {
                // 如果下游任务是源任务，说明存在循环
                if (next.equals(source)) {
                    return true;
                }
                // 如果该任务未被访问过，加入队列继续遍历
                if (visited.add(next)) {
                    queue.add(next);
                }
            }
        }
        
        // 遍历完成未找到源任务，说明不存在循环
        return false;
    }

    /**
     * SysTaskLink 实体转 SysTaskLinkVO
     *
     * @param link 依赖关系实体
     * @return 依赖关系视图对象
     */
    private SysTaskLinkVO toLinkVO(SysTaskLink link) {
        SysTaskLinkVO vo = new SysTaskLinkVO();
        vo.setId(link.getId());
        vo.setSource(link.getSourceId());
        vo.setTarget(link.getTargetId());
        vo.setType(link.getType());
        return vo;
    }
}
