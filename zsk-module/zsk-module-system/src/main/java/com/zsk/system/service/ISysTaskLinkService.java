package com.zsk.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsk.system.domain.SysTaskLink;
import com.zsk.system.domain.dto.SysTaskLinkCreateDTO;
import com.zsk.system.domain.vo.SysTaskLinkVO;

import java.util.List;

/**
 * 任务依赖关系 服务层
 *
 * @author wuhuaming
 */
public interface ISysTaskLinkService extends IService<SysTaskLink> {

    /**
     * 获取全部任务依赖关系
     *
     * @return 依赖关系视图列表
     */
    List<SysTaskLinkVO> listLinks();

    /**
     * 创建任务依赖（含校验：不能自引用、不能重复、不能循环依赖）
     *
     * @param dto 创建请求
     * @return 创建后的依赖视图对象（含生成的 id）
     */
    SysTaskLinkVO createLink(SysTaskLinkCreateDTO dto);

    /**
     * 删除任务依赖
     *
     * @param ids 依赖ID列表
     */
    void deleteLinkByIds(List<Long> ids);

    /**
     * 根据任务ID删除相关的依赖关系
     *
     * @param taskIds 任务ID列表
     */
    void deleteLinksByTaskIds(List<Long> taskIds);
}
