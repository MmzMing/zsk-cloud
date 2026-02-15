package com.zsk.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 最近管理日志响应 视图对象
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@Schema(description = "最近管理日志响应")
public class SysRecentLogResponseVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 日志列表 */
    @Schema(description = "日志列表")
    private List<SysRecentLogVo> list;

    /** 总条数 */
    @Schema(description = "总条数")
    private Long total;
}
