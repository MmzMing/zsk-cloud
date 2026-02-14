package com.zsk.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zsk.common.datasource.domain.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 通知公告对象 sys_notice
 *
 * @author wuhuaming
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_notice")
@Schema(description = "通知公告对象")
public class SysNotice extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 公告标题
     */
    @Schema(description = "公告标题")
    private String noticeTitle;

    /**
     * 公告类型（1通知 2公告）
     */
    @Schema(description = "公告类型（1通知 2公告）")
    private String noticeType;

    /**
     * 公告内容
     */
    @Schema(description = "公告内容")
    private byte[] noticeContent;

    /**
     * 公告状态（0正常 1关闭）
     */
    @Schema(description = "公告状态（0正常 1关闭）")
    private String status;
}
