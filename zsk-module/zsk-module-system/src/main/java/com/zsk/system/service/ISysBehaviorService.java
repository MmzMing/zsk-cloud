package com.zsk.system.service;

import com.zsk.common.datasource.domain.PageResult;
import com.zsk.system.domain.dto.SysBehaviorQuery;
import com.zsk.system.domain.vo.SysBehaviorDetailVO;
import com.zsk.system.domain.vo.SysBehaviorEventVO;
import com.zsk.system.domain.vo.SysBehaviorUserVO;

import java.util.List;

/**
 * 行为审计 服务接口 (v2)
 * <p>
 * 数据源：MongoDB collection {@code sys_oper_log} ({@link com.zsk.common.log.domain.OperLog})。
 * 关联：通过 {@code operName} 与 {@code sys_user.user_name} 关联。
 *
 * @author wuhuaming
 * @date 2026-04-22
 * @version 2.0
 */
public interface ISysBehaviorService {

    /**
     * 获取有行为记录的用户列表（聚合，用于前端筛选）
     *
     * @return 用户列表
     */
    List<SysBehaviorUserVO> listBehaviorUsers();

    /**
     * 分页查询用户行为列表（多条件）
     *
     * @param query 查询条件
     * @return 行为分页结果
     */
    PageResult<SysBehaviorEventVO> pageEvents(SysBehaviorQuery query);

    /**
     * 获取行为详情（完整请求/响应）
     *
     * @param id 行为记录ID（MongoDB _id）
     * @return 行为详情
     */
    SysBehaviorDetailVO getDetail(String id);
}
