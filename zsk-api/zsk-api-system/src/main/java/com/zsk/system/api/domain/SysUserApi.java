package com.zsk.system.api.domain;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/**
 * 用户对象 sys_user
 * 
 * @author zsk
 */
@Data
public class SysUserApi implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long id;

    /** 租户ID */
    private Long tenantId;

    /** 用户账号 */
    private String userName;

    /** 用户昵称 */
    private String nickName;

    /** 用户类型（00系统用户） */
    private String userType;

    /** 用户邮箱 */
    private String email;

    /** 手机号码 */
    private String phonenumber;

    /** 用户性别（0男 1女 2未知） */
    private String sex;

    /** 用户头像 */
    private String avatar;

    /** 密码 */
    private String password;

    /** 帐号状态（0正常 1停用） */
    private String status;

    /** 删除标志（0代表存在 2代表删除） */
    private Integer delFlag;

    /** 最后登录IP */
    private String loginIp;

    /** 最后登录时间 */
    private java.time.LocalDateTime loginDate;

    /** 备注 */
    private String remark;
}
