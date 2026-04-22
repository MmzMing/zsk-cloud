# 仪表盘 API接口文档

## 1. 接口列表

| API路径 | HTTP方法 | 所属文件 | 功能描述 |
|---------|----------|----------|----------|
| /dashboard/overview | GET | SysDashboardController.java | 获取仪表盘概览数据 |

## 2. 接口详情

### 2.1 获取仪表盘概览数据

**路径**: `GET /dashboard/overview`

**功能描述**: 获取仪表盘概览数据，包含用户总数、文档总数、视频总数和评论数

**请求头**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | string | 是 | Bearer token |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": [
    {
      "key": "users",
      "label": "用户总数",
      "value": "1234",
      "delta": "+12.5%",
      "description": "系统注册用户数量"
    },
    {
      "key": "docs",
      "label": "文档总数",
      "value": "5678",
      "delta": "+5.2%",
      "description": "已发布文档数量"
    },
    {
      "key": "videos",
      "label": "视频总数",
      "value": "901",
      "delta": "+8.3%",
      "description": "已发布视频数量"
    },
    {
      "key": "comments",
      "label": "评论数",
      "value": "2345",
      "delta": "+15.7%",
      "description": "文档和视频评论总数"
    }
  ]
}
```

| 参数名 | 类型 | 说明 |
|--------|------|------|
| data[].key | string | 唯一标识 |
| data[].label | string | 显示标签 |
| data[].value | string | 当前数值 |
| data[].delta | string | 变化量（如：+12.5%） |
| data[].description | string | 描述说明 |

**注意**: 该接口仅返回用户总数、文档总数、视频总数和评论数四项数据，不包含其他统计信息。
