# ZSK-Cloud 企业级微服务脚手架

## 🚀 项目概述
ZSK-Cloud 是一款基于最新技术栈构建的企业级微服务开发脚手架，旨在为开发者提供高性能、高可用、可快速扩展的底座。

- **核心架构**: 基于 Java 21、Spring Boot 3.5.0 及 Spring Cloud 2025.0.0。
- **治理中心**: 集成 Nacos 3.1.1、Sentinel 流量控制、Spring Cloud Gateway。
- **核心功能**: 包含认证授权、分布式事务、动态数据源、多租户支持及统一对象存储（Minio/OSS）。

---

## 🛠️ 技术栈
| 类别 | 关键技术 | 版本 | 说明 |
| :--- | :--- | :--- | :--- |
| **开发环境** | JDK / Maven | 21 / 3.9+ | 享受最新 Java 特性 |
| **核心框架** | Spring Boot | 3.5.0 | 官方推荐稳定版本 |
| | Spring Cloud | 2025.0.0 | 微服务架构 |
| | Spring Cloud Alibaba | 2025.0.0.0 | 阿里微服务生态 |
| **服务治理** | Nacos | 3.1.1 | 服务注册与配置中心 |
| | Sentinel | 3.1.1 (集成) | 流量控制与熔断降级 |
| | Spring Cloud Gateway | 4.x | API 网关 |
| **持久层** | MyBatis-Plus | 3.5.7 | 灵活高效的 ORM 框架 |
| | MySQL | 8.3.0 | 关系型数据库 |
| | MongoDB | 6.x | NoSQL 数据库 |
| | Druid | 1.2.23 | 数据库连接池 |
| | Dynamic Datasource | 4.3.1 | 动态数据源 |
| **缓存/队列** | Redis | 3.4.1 | 缓存数据库 |
| | Redisson | 3.27.0 | 分布式锁与工具 |
| | RocketMQ | 5.2.0 | 消息队列 (待集成) |
| | Caffeine | 3.1.8 | 本地缓存 |
| **安全认证** | Spring Security | 6.x | 权限控制 |
| | JWT | 0.12.5 | 令牌认证 |
| | Bouncy Castle | 1.78 | 加密算法 |
| | OAuth2 | - | 第三方登录 |
| **文件存储** | MinIO | 8.5.8 | 对象存储 |
| | 阿里云 OSS | 3.18.1 | 云存储 |
| **工具类** | Hutool | 5.8.40 | Java 工具集 |
| | Guava | 33.0.0-jre | Google 工具库 |
| | MapStruct | 1.5.5.Final | 对象映射 |
| | Lombok | 1.18.36 | 代码简化 |
| | EasyExcel | 4.0.3 | Excel 处理 |
| **文档/监控** | Knife4j | 4.5.0 | OpenAPI 3 文档 |
| | SpringDoc | 2.8.4 | API 文档 |
| | SkyWalking | 9.1.0 | 链路追踪 (待集成) |
| | Micrometer | 1.13.0 | 监控指标 |
| **任务调度** | XXL-JOB | 2.4.1 | 分布式调度 (待集成) |

---

## 📂 项目结构
```text
zsk-cloud
├── zsk-api               // 接口模块 (Feign 客户端)
│   ├── zsk-api-system    // 系统模块远程接口
│   └── zsk-api-document  // 文档模块远程接口
├── zsk-auth              // 认证中心 (登录、鉴权、第三方登录)
├── zsk-common            // 通用组件
│   ├── zsk-common-core      // 核心工具类、异常处理、统一响应
│   ├── zsk-common-security  // 权限控制与权限注解
│   ├── zsk-common-redis     // Redis / Redisson 分布式缓存
│   ├── zsk-common-oss       // 统一对象存储 (MinIO / OSS)
│   ├── zsk-common-datasource// 动态数据源支持
│   ├── zsk-common-log       // 日志采集
│   ├── zsk-common-swagger   // API 文档 (Knife4j)
│   ├── zsk-common-sentinel  // Sentinel 限流熔断
│   ├── zsk-common-xxljob    // XXL-JOB 分布式调度 (待实现)
├── zsk-gateway           // 网关中心 (动态路由、限流、黑名单)
├── zsk-module            // 业务模块
│   ├── zsk-module-system   // 系统管理 (用户、角色、权限、字典、监控)
│   └── zsk-module-document // 文档管理 (笔记、文件、视频、流程)
├── zsk-visual            // 图形化监控
│   └── zsk-visual-monitor  // 监控中心 (已集成SkyWalking)
├── init                  // 环境初始化配置
│   ├── nacos             // Nacos 配置文件
│   └── sql               // 数据库脚本
└── docs                  // 项目文档
```

---

## ✨ 已实现功能

### 基础架构
- **网关服务 (`zsk-gateway`)**: 集成 Spring Cloud Gateway，实现鉴权过滤、黑名单校验、日志记录、XSS 防护及 Sentinel 分布式限流。
- **认证服务 (`zsk-auth`)**: 基于 JWT 的令牌机制，支持图形验证码、邮箱验证及第三方登录（QQ、微信、GitHub）。采用"私钥签名、公钥验证"模式，支持 `keyLocator` 自动兼容对称与非对称算法。
- **通用组件 (`zsk-common`)**:
  - `core`: 核心工具类、全局异常拦截、统一响应格式
  - `security`: 基于 Spring Security 的权限控制与权限注解
  - `redis`: 集成 Redisson 实现分布式缓存与分布式锁
  - `oss`: 统一对象存储接口，支持 MinIO 与阿里云 OSS
  - `datasource`: 动态数据源切换支持
  - `swagger`: 集成 Knife4j 实现 OpenAPI 3 文档
  - `sentinel`: Sentinel 限流熔断集成
  - `log`: 操作日志采集

### 业务模块
- **系统管理 (`zsk-module-system`)**: 完整的 RBAC 模型（用户、角色、菜单）、字典管理、参数配置、通知公告、系统监控、操作日志等。
- **文档管理 (`zsk-module-document`)**: 笔记管理（笔记、评论、图片）、文件管理、视频管理、视频评论、流程管理及历史记录、用户互动（点赞、收藏、关注）等。
---

## 🚧 待开发与规划

### 核心组件完善
- **分布式消息队列**: 集成 RocketMQ，完成消息生产者与消费者示例。
- **搜索引擎**: 集成 Elasticsearch，实现全文搜索功能。

### 业务模块扩展
- **工作流引擎**: 集成 Flowable 或 Activiti 实现 BPMN 2.0 工作流，增强流程管理能力。
- **跨模块调用**: 完善 `zsk-api` 模块，实现各业务模块间的 Feign 远程调用。

### 工程化与运维
- **自动化测试**: 补充单元测试（JUnit/Mockito）与集成测试用例。
- **容器化部署**: 提供 Dockerfile、Docker-compose 及 Jenkinsfile 部署脚本。
- **日志采集**: 集成 ELK (Elasticsearch, Logstash, Kibana) 实现统一日志采集。

### AI 与智能应用
- **AI-QQ-BOT**: 使用 Spring AI 集成大模型，对接 QQ 机器人，在 QQ 群中提供智能客服与文档助手功能，配合 Nacos 的 MCP 和 Skill 实现动态配置。

---

## 🛡️ 安全机制说明
### **JWT 密钥管理**
项目支持 **对称加密 (HS256)** 与 **非对称加密 (RS256)** 的动态兼容：
- **签名原则**：遵循“私钥签名、公钥验证”。私钥仅保存在认证中心（zsk-auth），用于签发令牌；公钥分发给各微服务，用于校验令牌合法性。
- **智能解析**：通过 `keyLocator` 机制，系统能根据令牌头部的算法 (`alg`) 自动匹配密钥。若 `alg` 为 `RS256` 则使用公钥验证，若为 `HS256` 则回退至 `secret` 验证，确保了配置迁移过程中的平滑过渡。

---

## 🔐 第三方登录流程
项目遵循标准的 OAuth2 授权码模式，实现“即登即用”：

### **详细调用链路**

![oauth2](./docs/oauth2.png)

### **核心逻辑步骤**
1. **授权引导**: 前端通过 `/third-party/url` 接口获取预构建的第三方授权地址，并在 Redis 中存储 `state` 防伪码以应对 CSRF 风险。
2. **授权回调**: 用户在第三方平台确认授权后，浏览器重定向至前端回调页，前端提取 `code` 与 `state` 并透传至后端回调接口。
3. **双重交换**:
   - **令牌交换**：后端策略类（Strategy）通过授权码向第三方服务器换取 `access_token`。
   - **信息获取**：使用令牌调用第三方用户信息 API，解析并封装为统一的 `SysUserApi` 实体。
4. **账户联动**:
   - **映射检测**：根据 `loginType` 与第三方唯一标识（如 OpenID）查询本地账户关联表。
   - **静默注册**：若为新用户，系统将基于第三方基础资料（昵称、头像）自动完成账号初始化。
5. **颁发凭证**: 认证通过后，系统签发高安全性的 JWT 访问令牌，并将登录状态同步至 Redis 缓存。

---

## 📖 快速上手
1. **克隆项目**: `git clone https://github.com/zsk-cloud/zsk-cloud.git`
2. **初始化环境**: 执行 `sql/` 下的脚本，并在 Nacos 中导入 `init/nacos/` 下的配置文件。
3. **本地启动**:
    - 启动 `ZskGatewayApplication` (网关)
    - 启动 `ZskAuthApplication` (认证)
    - 启动各业务模块应用

---

## 📄 相关文档
- [详细项目总结](./PROJECT_SUMMARY.md)
- [开发规范指南](./docs/开发文档.md)
- [数据库规范](.trae/rules/prorules.md)
