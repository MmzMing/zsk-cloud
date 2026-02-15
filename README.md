# ZSK-Cloud 企业级微服务脚手架

## 🚀 项目概述
ZSK-Cloud 是一款基于最新技术栈构建的企业级微服务开发脚手架，旨在为开发者提供高性能、高可用、可快速扩展的底座。

- **核心架构**: 基于 Java 21、Spring Boot 3.5.0 及 Spring Cloud 2025.0.0。
- **治理中心**: 集成 Nacos 3.1.1、Sentinel 流量控制、Spring Cloud Gateway。
- **核心功能**: 包含认证授权、分布式事务、动态数据源、多租户支持及统一对象存储（Minio/OSS）。

---

## 🛠️ 技术栈
| 类别 | 关键技术 | 说明 |
| :--- | :--- | :--- |
| **开发环境** | JDK 21 / Maven 3.9+ | 享受最新 Java 特性 |
| **核心框架** | Spring Boot 3.5 / Spring Cloud 2025 / Spring AI | 官方推荐稳定版本 |
| **服务治理** | Nacos / Sentinel / Gateway | 全套微服务治理方案 |
| **持久层** | MyBatis-Plus / MongoDB / MySQL 8 / | 灵活高效的 ORM 框架 |
| **缓存/队列** | Redis (Redisson) / RocketMQ  | 高性能数据处理 |
| **安全认证** | Spring Security / JWT / BCrypt / OAuth2 | 严密的权限控制体系 |
| **文档/监控** | Knife4j / Prometheus / SkyWalking | 完善的 API 与运维监控 |

---

## 📂 项目结构
```text
zsk-cloud
├── zsk-api               // 接口模块 (Feign 客户端)
├── zsk-auth              // 认证中心 (登录、鉴权、第三方登录)
├── zsk-common            // 通用组件 (Core, Security, Redis, OSS, Log等)
├── zsk-gateway           // 网关中心 (动态路由、限流、黑名单)
├── zsk-module            // 业务模块
│   ├── zsk-module-system   // 系统管理 (用户、角色、权限、字典)
│   └── zsk-module-document // 文档管理 (笔记、文件、视频评论、流程)
├── zsk-visual            // 图形化监控 (Monitor, Sentinel DashBoard等)
├── sql                   // 数据库脚本
└── init                  // 环境初始化配置 (Nacos 配置文件等)
```

---

## ✨ 已实现功能
- **统一鉴权**: 基于 JWT 的令牌机制，支持单点登录、滑块验证码及邮箱验证。采用“私钥签名、公钥验证”模式，并支持 `keyLocator` 自动兼容对称与非对称算法。
- **权限管理**: 完善的 RBAC 模型，细粒度的权限注解控制。
- **第三方登录**: 集成 QQ、微信、GitHub 授权登录，支持 OAuth2 流程及账号自动映射。
- **系统工具**: 动态数据源切换、全局异常拦截、统一响应格式、操作日志采集。
- **业务场景**: 实现了完整的文档笔记管理流程、文件上传预览、视频互动评论等。
---

## 🚧 待开发与规划
- **分布式调度**: 深度集成 XXL-JOB 任务调度。
- **监控大屏**: 完善可视化监控面板与链路追踪（SkyWalking）。
- **工作流引擎**: 集成 Flowable 实现更复杂的 BPMN 2.0 业务流。
- **AI-QQ-BOT**: 使用Spring AI集成大模型，对接QQ机器人，在QQ群中提供智能客服与文档助手功能，配合nacos的MCP和Skill实现动态配置。
- **容器化部署**: Docker-compose 和 Jenkinsfile 。

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
