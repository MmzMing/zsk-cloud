package com.zsk.system.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 强制下线 数据传输对象
 * <p>
 * v2: 改为按 userId 维度强制下线（一次踢掉该用户所有设备）。
 *
 * @author wuhuaming
 * @version 2.0
 * @date 2026-04-22
 */
@Data
@Schema(description = "强制下线请求")
public class SysForceLogoutDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID列表
     */
    @Schema(description = "用户ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "用户ID不能为空")
    private List<Long> userIds;
}
