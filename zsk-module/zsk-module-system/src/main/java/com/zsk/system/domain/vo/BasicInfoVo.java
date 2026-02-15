package com.zsk.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 简历基础信息 视图对象
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "简历基础信息")
public class BasicInfoVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 姓名 */
    @Schema(description = "姓名")
    private String name;

    /** 求职意向 */
    @Schema(description = "求职意向")
    private String jobIntention;

    /** 年龄 */
    @Schema(description = "年龄")
    private String age;

    /** 性别 */
    @Schema(description = "性别")
    private String gender;

    /** 城市 */
    @Schema(description = "城市")
    private String city;

    /** 电话 */
    @Schema(description = "电话")
    private String phone;

    /** 邮箱 */
    @Schema(description = "邮箱")
    private String email;

    /** GitHub */
    @Schema(description = "GitHub")
    private String github;

    /** 个人简介 */
    @Schema(description = "个人简介")
    private String summary;

    /** 头像 */
    @Schema(description = "头像")
    private String avatar;

    /** 工作经验 */
    @Schema(description = "工作经验")
    private String experience;

    /** 期望薪资 */
    @Schema(description = "期望薪资")
    private String salary;

    /** 政治面貌 */
    @Schema(description = "政治面貌")
    private String politics;

    /** 求职状态 */
    @Schema(description = "求职状态")
    private String status;
}
