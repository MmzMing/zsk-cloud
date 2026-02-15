package com.zsk.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsk.system.domain.SysToken;
import com.zsk.system.mapper.SysTokenMapper;
import com.zsk.system.service.ISysTokenService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 系统令牌 服务层实现
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Service
public class SysTokenServiceImpl extends ServiceImpl<SysTokenMapper, SysToken> implements ISysTokenService {

    /**
     * 创建令牌
     *
     * @param token 令牌信息
     * @return 生成的令牌值
     */
    @Override
    public String createToken(SysToken token) {
        /** 生成令牌值 */
        String tokenValue = UUID.randomUUID().toString().replace("-", "");
        token.setTokenValue(tokenValue);
        token.setStatus("active");
        this.save(token);
        return tokenValue;
    }

    /**
     * 吊销令牌
     *
     * @param id 令牌ID
     * @return 是否成功
     */
    @Override
    public boolean revokeToken(Long id) {
        SysToken token = new SysToken();
        token.setId(id);
        token.setStatus("revoked");
        return this.updateById(token);
    }

    /**
     * 批量吊销令牌
     *
     * @param ids 令牌ID列表
     * @return 是否成功
     */
    @Override
    public boolean batchRevokeToken(List<Long> ids) {
        for (Long id : ids) {
            SysToken token = new SysToken();
            token.setId(id);
            token.setStatus("revoked");
            if (!this.updateById(token)) {
                return false;
            }
        }
        return true;
    }
}
