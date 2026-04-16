package com.zsk.system.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 强制下线 数据传输对象
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@Schema(description = "强制下线请求")
public class SysForceLogoutDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 会话编号列表（Redis中的uuid） */
    @Schema(description = "会话编号列表", required = true)
    @NotEmpty(message = "会话编号不能为空")
    private List<String> sessionIds;
}
