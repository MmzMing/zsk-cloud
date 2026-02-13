package com.zsk.common.datasource.handler;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.zsk.common.core.context.SecurityContext;
import com.zsk.common.core.context.TenantContext;
import com.zsk.common.core.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis Plus 自动填充处理器
 *
 * @author zsk
 */
@Slf4j
@Component
public class MybatisPlusMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("开始插入填充...");
        LocalDateTime now = LocalDateTime.now();
        String userName = SecurityContext.getUserName();
        if (StringUtils.isEmpty(userName)) {
            userName = "admin";
        }
        Long tenantId = TenantContext.getTenantId();
        if (ObjectUtil.isEmpty(tenantId)) {
            tenantId = 1001L;
        }


        if (metaObject.hasSetter("createTime")) {
            this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        }
        if (metaObject.hasSetter("updateTime")) {
            this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
        }
        if (metaObject.hasSetter("createName")) {
            this.strictInsertFill(metaObject, "createName", String.class, userName);
        }
        if (metaObject.hasSetter("updateName")) {
            this.strictInsertFill(metaObject, "updateName", String.class, userName);
        }
        if (metaObject.hasSetter("tenantId")) {
            this.strictInsertFill(metaObject, "tenantId", Long.class, tenantId);
        }
        if (metaObject.hasSetter("deleted")) {
            this.strictInsertFill(metaObject, "deleted", Integer.class, 0);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("开始更新填充...");
        String userName = SecurityContext.getUserName();
        if (StringUtils.isEmpty(userName)) {
            userName = "admin";
        }

        if (metaObject.hasSetter("updateTime")) {
            this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        }
        if (metaObject.hasSetter("updateName")) {
            this.strictUpdateFill(metaObject, "updateName", String.class, userName);
        }
    }
}
