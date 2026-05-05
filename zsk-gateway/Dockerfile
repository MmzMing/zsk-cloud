# ==================== 第一阶段：构建 ====================
# 基于 JDK 21 Alpine 镜像编译项目
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

# 复制整个项目（利用 .dockerignore 排除不需要的文件）
COPY . .

# 安装 Maven 并编译 zsk-gateway 模块及其所有依赖模块
# -pl zsk-gateway  : 仅构建 gateway 模块
# -am              : 同时构建 gateway 依赖的模块（zsk-common-*、zsk-api-*）
# -T 1C            : 每个 CPU 核心一个线程并行构建
# -DskipTests      : 跳过测试
RUN apk add --no-cache maven && \
    mvn -T 1C clean package -pl zsk-gateway -am -DskipTests -Dmaven.test.skip=true

# ==================== 第二阶段：运行 ====================
# 仅使用 JRE 21 Alpine 镜像，体积更小，不含编译工具
FROM eclipse-temurin:21-jre-alpine
LABEL maintainer="ZSK Team <dev@zsk.cloud>"

# 设置时区为上海，安装后删除 tzdata 减小镜像体积
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    apk del tzdata

# 创建非 root 用户 spring:spring 运行应用（安全最佳实践）
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# 从构建阶段复制编译好的 jar 包
COPY --from=builder /build/zsk-gateway/target/*.jar app.jar

# 将应用目录所有权交给 spring 用户
RUN chown -R spring:spring /app

# 切换到非 root 用户
USER spring:spring

# JVM 参数（可通过 docker-compose environment 覆盖）：
# -XX:+UseContainerSupport        : 自动识别容器内存限制
# -XX:InitialRAMPercentage=50.0   : 初始堆内存为容器内存的 50%
# -XX:MaxRAMPercentage=75.0       : 最大堆内存为容器内存的 75%
# -XX:+UseG1GC                    : 使用 G1 垃圾收集器
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:InitialRAMPercentage=50.0 -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

# 网关服务端口
EXPOSE 8080

# 启动命令：使用 JAVA_OPTS 环境变量动态配置 JVM 参数
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app/app.jar"]
