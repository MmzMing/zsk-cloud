package com.zsk.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsk.system.domain.SysToken;

import java.util.List;

/**
 * 系统令牌 服务层
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
public interface ISysTokenService extends IService<SysToken> {

    /**
     * 创建令牌
     *
     * @param token 令牌信息
     * @return 生成的令牌值
     */
    String createToken(SysToken token);

    /**
     * 吊销令牌
     *
     * @param id 令牌ID
     * @return 是否成功
     */
    boolean revokeToken(Long id);

    /**
     * 批量吊销令牌
     *
     * @param ids 令牌ID列表
     * @return 是否成功
     */
    boolean batchRevokeToken(List<Long> ids);
}
