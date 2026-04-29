# ZSK-Cloud Docker 部署流程文档

## 流程总览

```
Step 1  确认服务器环境
  ↓
Step 2  确认 1Panel 中间件就绪
  ↓
Step 3  初始化数据库
  ↓
Step 4  配置 Nacos
  ↓
Step 5  上传项目到服务器
  ↓
Step 6  配置 .env 环境变量
  ↓
Step 7  Docker 构建与启动
  ↓
Step 8  验证部署
```

---

## Step 1：确认服务器环境

登录部署服务器，确认以下环境已安装：

```bash
# 检查 Docker 版本（需要 20.10+）
docker --version

# 检查 Docker Compose 版本（需要 2.0+）
docker compose version
```

| 项目 | 最低要求 |
|:---|:---|
| 操作系统 | Linux (CentOS 7+ / Ubuntu 20.04+) |
| Docker | 20.10+ |
| Docker Compose | 2.0+ |
| 内存 | ≥ 4GB (4 个 Java 服务) |
| 磁盘 | ≥ 20GB (镜像 + 构建) |
| JDK | 不需要（容器内构建） |
| Maven | 不需要（容器内构建） |

> 如果 Docker 未安装，参考官方文档安装：https://docs.docker.com/engine/install/

---

## Step 2：确认 1Panel 中间件就绪

登录 1Panel 管理界面，确认以下中间件正常运行：

| 中间件 | 地址 | 用途 | 确认方式 |
|:---|:---|:---|:---|
| Nacos | `192.168.101.129:8848` | 注册中心 + 配置中心 | 浏览器访问 `http://192.168.101.129:8848/nacos` |
| MySQL | `192.168.101.129:3306` | 业务数据库 | 1Panel 数据库列表查看状态 |
| Redis | `192.168.101.129:6379` | 缓存 | 1Panel Redis 状态页 |
| MongoDB | `192.168.101.129:27017` | 日志存储 | 1Panel MongoDB 状态页 |

> ⚠️ 所有中间件必须先正常运行，否则后续服务启动会失败。

---

## Step 3：初始化数据库

在 MySQL 中创建所需的数据库和表。

### 3.1 登录 MySQL

```bash
mysql -h 192.168.101.129 -u root -p
```

### 3.2 按顺序执行 SQL 脚本

> SQL 脚本在项目的 `init/sql/` 目录下，需要先完成 Step 5 上传项目后再执行。
> 如果服务器上已有项目文件，直接执行：

```bash
cd /opt/zsk-cloud

# 1. 创建 Nacos 配置数据库
mysql -h 192.168.101.129 -u root -p < init/sql/nacos.sql

# 2. 创建系统模块数据库
mysql -h 192.168.101.129 -u root -p < init/sql/zsk_system.sql

# 3. 创建文档模块数据库
mysql -h 192.168.101.129 -u root -p < init/sql/zsk_document.sql
```

> 如果是首次部署，三个脚本都必须执行。如果是更新部署，跳过此步骤。

---

## Step 4：配置 Nacos

### 4.1 登录 Nacos 控制台

浏览器访问：`http://192.168.101.129:8848/nacos`

- 用户名：`nacos`
- 密码：`nacos`（或 1Panel 中设置的密码）

### 4.2 导入配置

1. 进入 **配置管理 → 配置列表**
2. 切换命名空间到 `prod`（namespace ID：`d4cbb030-726e-4d17-92d3-1042063c3bd7`）
3. 点击 **导入配置**，选择 `init/nacos/prod/` 目录下的配置文件

> 如果 `prod` 命名空间下缺少配置，从 `dev` 命名空间克隆一份，然后修改中间件地址。

### 4.3 修改生产环境配置（关键！）

**必须确保 `prod` 分组的配置指向 1Panel 上的真实中间件地址，而非 `localhost`。**

需要检查/修改的配置项：

| Nacos 配置文件 | 需修改项 | 修改为 |
|:---|:---|:---|
| `application.yml` (prod) | `spring.data.redis.host` | `192.168.101.129` |
| `application.yml` (prod) | `spring.data.redis.password` | 1Panel 中 Redis 的密码 |
| `application.yml` (prod) | `spring.data.mongodb.host` | `192.168.101.129` |
| `application.yml` (prod) | `spring.data.mongodb.password` | 1Panel 中 MongoDB 的密码 |
| `zsk-module-system-prod.yml` | `spring.datasource.url` | `jdbc:mysql://192.168.101.129:3306/zsk_system?...` |
| `zsk-module-system-prod.yml` | `spring.datasource.password` | 1Panel 中 MySQL 的密码 |
| `zsk-module-system-prod.yml` | `spring.data.redis.host` | `192.168.101.129` |
| `zsk-module-system-prod.yml` | `spring.data.redis.password` | 1Panel 中 Redis 的密码 |

> ⚠️ 如果 `prod` 分组下缺少 `application.yml`、`zsk-gateway-prod.yml`、`zsk-auth-prod.yml`、`zsk-module-document-prod.yml`，需要从 `dev` 分组复制并修改中间件地址。

---

## Step 5：上传项目到服务器

### 需要上传的文件清单

上传到服务器目录：`/opt/zsk-cloud/`

```
/opt/zsk-cloud/
├── docker-compose.yml                          # Docker Compose 编排文件
├── .env.example                                # 环境变量模板
├── .dockerignore                               # Docker 构建忽略规则
├── pom.xml                                     # Maven 父 POM
├── zsk-gateway/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
├── zsk-auth/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
├── zsk-module/
│   ├── pom.xml
│   ├── zsk-module-system/
│   │   ├── Dockerfile
│   │   ├── pom.xml
│   │   └── src/
│   └── zsk-module-document/
│       ├── Dockerfile
│       ├── pom.xml
│       └── src/
├── zsk-common/                                 # 公共模块（构建依赖）
│   ├── pom.xml
│   ├── zsk-common-core/
│   ├── zsk-common-security/
│   ├── zsk-common-redis/
│   ├── zsk-common-datasource/
│   ├── zsk-common-log/
│   ├── zsk-common-swagger/
│   ├── zsk-common-sentinel/
│   ├── zsk-common-oss/
│   └── zsk-common-xxljob/
├── zsk-api/                                    # Feign 接口（构建依赖）
│   ├── pom.xml
│   ├── zsk-api-system/
│   └── zsk-api-document/
└── init/                                       # 初始化脚本
    ├── sql/
    │   ├── nacos.sql
    │   ├── zsk_system.sql
    │   └── zsk_document.sql
    └── nacos/
        ├── dev/
        └── prod/
```

### 不需要上传的文件

| 路径 | 原因 |
|:---|:---|
| `target/` | 构建产物，Docker 内重新编译 |
| `.git/` | Git 历史，部署不需要 |
| `.idea/`、`.vscode/` | IDE 配置 |
| `*.iml` | IntelliJ 模块文件 |
| `docs/` | 文档目录，部署不需要 |
| `zsk-visual/` | 监控模块（未实现），暂不部署 |
| `.env` | 含敏感信息，服务器上手动创建 |

### 上传方式：Git 克隆（推荐）

**前提**：项目代码已推送到 Git 仓库（GitHub / Gitee / 自建 GitLab）。

```bash
# 1. SSH 登录部署服务器
ssh root@<server-ip>

# 2. 安装 Git（如果未安装）
yum install -y git        # CentOS
apt install -y git        # Ubuntu

# 3. 克隆项目到 /opt 目录
cd /opt
git clone <your-repo-url> zsk-cloud

# 4. 进入项目目录
cd zsk-cloud

# 5. 切换到部署分支（如 main 或 release）
git checkout main
```

> 后续更新部署只需 `git pull` 即可拉取最新代码。

### 备选方式：SCP 上传（无 Git 仓库时使用）

在本地开发机执行：

```bash
# 1. 打包项目（排除不需要的文件）
cd e:\code\zsk\zsk-cloud
tar --exclude='target' \
    --exclude='.git' \
    --exclude='.idea' \
    --exclude='.vscode' \
    --exclude='*.iml' \
    --exclude='docs' \
    --exclude='zsk-visual' \
    -czf zsk-cloud.tar.gz .

# 2. 上传到服务器
scp zsk-cloud.tar.gz root@<server-ip>:/opt/zsk-cloud/

# 3. SSH 登录服务器解压
ssh root@<server-ip>
mkdir -p /opt/zsk-cloud
cd /opt/zsk-cloud
tar -xzf zsk-cloud.tar.gz
```

---

## Step 6：配置 .env 环境变量

```bash
cd /opt/zsk-cloud

# 1. 从模板创建 .env 文件
cp .env.example .env

# 2. 编辑 .env，修改为实际值
vi .env
```

`.env` 文件内容（根据实际情况修改）：

```properties
# Nacos 地址（1Panel 上的 Nacos）
NACOS_ADDR=192.168.101.129:8848

# Nacos 认证
NACOS_USERNAME=nacos
NACOS_PASSWORD=123456

# Nacos 命名空间 ID
NACOS_NAMESPACE=d4cbb030-726e-4d17-92d3-1042063c3bd7

# Sentinel 控制台地址
SENTINEL_DASHBOARD=192.168.101.129:8858
```

> ⚠️ `.env` 文件包含敏感信息，不会提交到 Git。每台服务器需要单独创建。

---

## Step 7：Docker 构建与启动

```bash
cd /opt/zsk-cloud

# 构建镜像并启动所有服务
docker compose up -d --build
```

> 首次构建需要下载 Maven 依赖，耗时约 5-10 分钟。后续构建会利用 Docker 缓存加速。

### 查看构建进度

```bash
# 实时查看构建日志
docker compose build --progress=plain 2>&1 | tee build.log
```

### 其他启动命令

```bash
# 仅构建镜像（不启动）
docker compose build

# 仅启动（镜像已存在时）
docker compose up -d

# 重新构建单个服务
docker compose up -d --build zsk-gateway
```

---

## Step 8：验证部署

### 8.1 检查容器状态

```bash
docker compose ps
```

预期输出（所有服务 STATUS 为 Up 或 healthy）：

```
NAME                  STATUS
zsk-gateway           Up
zsk-auth              Up
zsk-module-system     Up
zsk-module-document   Up
```

> 如果某个服务状态为 `Exited`，查看日志排查：`docker compose logs --tail 100 <service-name>`

### 8.2 检查服务日志

```bash
# 查看所有服务日志
docker compose logs -f

# 查看单个服务日志
docker compose logs -f zsk-gateway
docker compose logs -f zsk-auth
docker compose logs -f zsk-module-system
docker compose logs -f zsk-module-document
```

### 8.3 健康检查

```bash
# Gateway（网关）
curl http://localhost:8080

# Auth（认证服务，context-path: /auth）
curl http://localhost:10010/auth

# System（系统模块，context-path: /system）
curl http://localhost:20010/system

# Document（文档模块，context-path: /document）
curl http://localhost:20020/document
```

### 8.4 检查 Nacos 注册

1. 浏览器访问 Nacos 控制台：`http://192.168.101.129:8848/nacos`
2. 进入 **服务管理 → 服务列表**
3. 确认以下 4 个服务已注册：

- `zsk-gateway`
- `zsk-auth`
- `zsk-module-system`
- `zsk-module-document`

---

## 常用运维命令

### 服务管理

```bash
# 启动所有服务
docker compose up -d

# 停止所有服务
docker compose stop

# 重启所有服务
docker compose restart

# 停止并删除容器（不删镜像）
docker compose down

# 停止并删除容器 + 镜像
docker compose down --rmi all

# 重启单个服务
docker compose restart zsk-gateway
```

### 日志查看

```bash
# 实时跟踪日志
docker compose logs -f zsk-gateway

# 查看最近 30 分钟日志
docker compose logs --since 30m zsk-gateway

# 导出日志到文件
docker compose logs zsk-gateway > gateway.log 2>&1
```

### 更新部署

```bash
# 拉取最新代码
git pull

# 重新构建并启动
docker compose up -d --build

# 或者仅重建变更的服务
docker compose up -d --build zsk-module-system
```

### 进入容器调试

```bash
docker compose exec zsk-gateway sh
docker compose exec zsk-module-system sh
```

---

## 端口映射

| 服务 | 容器端口 | 宿主机端口 | 访问路径 |
|:---|:---|:---|:---|
| zsk-gateway | 8080 | 8080 | `http://<ip>:8080` |
| zsk-auth | 10010 | 10010 | `http://<ip>:10010/auth` |
| zsk-module-system | 20010 | 20010 | `http://<ip>:20010/system` |
| zsk-module-document | 20020 | 20020 | `http://<ip>:20020/document` |

---

## 故障排查

### 服务启动失败

```bash
# 查看退出码
docker compose ps -a

# 查看详细日志
docker compose logs --tail 200 <service-name>

# 常见退出码：
# 137 - OOM (内存不足)，需增加服务器内存或调低 JVM 比例
# 1   - 应用异常，检查配置和日志
```

### 无法连接 Nacos

```bash
# 从容器内测试 Nacos 连通性
docker compose exec zsk-gateway sh -c "wget -qO- http://192.168.101.129:8848/nacos/ || echo 'UNREACHABLE'"

# 如果不通，检查：
# 1. Nacos 是否正常运行
# 2. 防火墙是否放通 8848 端口
# 3. .env 中 NACOS_ADDR 是否正确
```

### 无法连接 MySQL/Redis

```bash
# 检查 Nacos 中 prod 配置的中间件地址是否正确
# 确保地址是 1Panel 的实际 IP，而非 localhost 或容器名

# 从容器内测试连通性
docker compose exec zsk-module-system sh -c "wget -qO- http://192.168.101.129:3306/ 2>&1 || true"
```

### 内存不足

修改 `docker-compose.yml` 中 `JAVA_OPTS` 的内存比例：

```yaml
environment:
  JAVA_OPTS: "-XX:+UseContainerSupport -XX:InitialRAMPercentage=30.0 -XX:MaxRAMPercentage=50.0 -XX:+UseG1GC"
```

### 构建失败

```bash
# 清理 Docker 缓存后重新构建
docker compose build --no-cache

# 查看 Maven 构建详细日志
docker compose build --progress=plain zsk-gateway 2>&1 | tee build.log
```

---

## 架构图

```
                ┌─────────────────────────────────────────────┐
                │           1Panel 服务器 (192.168.101.129)    │
                │                                             │
                │  ┌───────┐  ┌───────┐  ┌──────┐  ┌───────┐ │
                │  │ Nacos │  │ MySQL │  │ Redis│  │MongoDB│ │
                │  │ :8848 │  │ :3306 │  │:6379 │  │:27017 │ │
                │  └───┬───┘  └───┬───┘  └──┬───┘  └───┬───┘ │
                └──────┼──────────┼─────────┼──────────┼─────┘
                       │          │         │          │
                ┌──────┼──────────┼─────────┼──────────┼─────┐
                │      │  Docker 部署服务器                    │
                │      │          │         │          │      │
                │  ┌───┴──┐  ┌────┴──┐      │          │      │
                │  │      │  │       │      │          │      │
                │  │Gateway│  │ Auth  │      │          │      │
                │  │ :8080 │  │:10010 │      │          │      │
                │  └──────┘  └───────┘      │          │      │
                │                           │          │      │
                │  ┌────────┐  ┌──────────┐ │          │      │
                │  │ System │  │ Document │ │          │      │
                │  │ :20010 │  │ :20020   │ │          │      │
                │  └────────┘  └──────────┘ │          │      │
                │                           │          │      │
                └───────────────────────────┴──────────┴──────┘
```
