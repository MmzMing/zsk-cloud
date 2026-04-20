# 权限管理 API接口文档

## 1. 接口列表

| API路径 | HTTP方法 | 所属文件 | 功能描述 |
|---------|----------|----------|----------|
| /api/system/permission/list | GET | SysPermissionController.java | 获取权限列表（按模块分组） |

## 2. 接口详情

### 2.1 获取权限列表

**路径**: `GET /api/system/permission/list`

**功能描述**: 获取权限列表，按模块分组返回

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": [
    {
      "id": "1",
      "label": "系统管理",
      "items": [
        {
          "id": "2",
          "key": "system:user:list",
          "name": "用户列表",
          "module": "系统管理",
          "description": "查看用户列表",
          "type": "menu",
          "createdAt": "2026-02-15 10:00:00"
        },
        {
          "id": "3",
          "key": "system:user:add",
          "name": "新增用户",
          "module": "系统管理",
          "description": "新增用户",
          "type": "action",
          "createdAt": "2026-02-15 10:00:00"
        }
      ]
    }
  ]
}
```