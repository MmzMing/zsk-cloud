# 开发规范

## 包命名规范
| 层级   | 命名规则                   | 示例                        |
| ------ | -------------------------- | --------------------------- |
| 基础包 | `com.zsk.{模块名}`         | `com.zsk.system`            |
| 控制器 | `controller`               | `com.zsk.system.controller` |
| 服务层 | `service` / `service.impl` | `com.zsk.system.service`    |
| 数据层 | `mapper`                   | `com.zsk.system.mapper`     |
| 实体类 | `domain`                   | `com.zsk.system.domain`     |
| 配置类 | `config`                   | `com.zsk.system.config`     |
| 工具类 | `utils`                    | `com.zsk.common.core.utils` |

## 类命名规范
| 类型     | 后缀                 | 示例                       |
| -------- | -------------------- | -------------------------- |
| 实体类   | 无/Entity            | `SysUser`、`SysUserEntity` |
| 数据传输 | DTO/VO/BO            | `UserDTO`、`UserVO`        |
| 控制器   | Controller           | `SysUserController`        |
| 服务接口 | Service              | `ISysUserService`          |
| 服务实现 | ServiceImpl          | `SysUserServiceImpl`       |
| 数据映射 | Mapper               | `SysUserMapper`            |
| 配置类   | Config/Configuration | `SecurityConfig`           |
| 工具类   | Utils/Util           | `StringUtils`              |
| 异常类   | Exception            | `ServiceException`         |

## 注释规范（中文）
**类注释：**
```java
/**
 * 用户管理 服务层实现
 * 
 * @author wuhuaming
 * @date 2024-01-15
 * @version 1.0
 */
@Service
public class SysUserServiceImpl implements ISysUserService {
```

**方法注释：**
```java
    /**
     * 根据条件分页查询用户列表
     *
     * @param user 用户信息（支持用户名、手机号模糊查询）
     * @param page 分页参数（页码、每页大小）
     * @return 分页结果（包含用户列表及总数）
     * @throws ServiceException 查询异常时抛出
     */
    @Override
    public PageResult<SysUser> selectUserList(SysUser user, PageDomain page) {
```

**字段注释：**
```java
    /** 用户ID（主键，自增） */
    private Long userId;

    /** 用户名（唯一，用于登录） */
    private String userName;

    /** 用户状态（0-正常 1-停用） */
    private String status;
```

## 接口规范（R）
| 操作     | HTTP方法 | URL示例                   | 说明              |
| -------- | -------- | ------------------------- | ----------------- |
| 查询列表 | GET      | `/system/user/list`       | 支持分页、排序    |
| 查询详情 | GET      | `/system/user/{userId}`   | 路径参数          |
| 新增     | POST     | `/system/user`            | 请求体JSON        |
| 修改     | PUT      | `/system/user`            | 请求体JSON        |
| 删除     | DELETE   | `/system/user/{userIds}`  | 支持批量          |
| 导出     | POST     | `/system/user/export`     | 返回Excel流       |
| 导入     | POST     | `/system/user/importData` | 接收MultipartFile |

**统一响应格式：**
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "total": 100,
        "rows": [...]
    },
    "timestamp": 1705315200000
}
```

## 数据库规范

**表命名：**
- 模块前缀：`sys_`（系统）、`gen_`（代码生成）、`bpm_`（工作流）
- 主键：`id`
- 创建人姓名：`create_name`
- 创建时间：`create_time`
- 更新人姓名：`update_name`
- 更新时间：`update_time`
- 是否已删除(0否1是)：`deleted`

**字段类型：**
| Java类型      | MySQL类型       | 说明               |
| ------------- | --------------- | ------------------ |
| Long          | `BIGINT(20)`    | 主键、外键         |
| String        | `VARCHAR(500)`  | 字符串（长度按需） |
| Integer       | `INT(4)`        | 状态、类型         |
| BigDecimal    | `DECIMAL(10,2)` | 金额               |
| LocalDateTime | `DATETIME`      | 时间戳（JDK8+）    |
| Boolean       | `TINYINT(1)`    | 布尔值             |

---
