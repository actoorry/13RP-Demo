# ============================================================
# 13RP-Demo 后端 Dockerfile
# Spring Boot 3.3.5 + Java 21（无 MySQL/Redis 依赖，数据来自预计算 JSON）
# 多阶段构建：build 阶段打包，runtime 阶段精简运行
# 构建产物：target/rd13-demo-0.3.0.jar（finalName 见 pom.xml）
# ============================================================

# ---------- 阶段 1：构建 ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# 配置阿里云 Maven 镜像加速（国内网络），写 /root/.m2/settings.xml
RUN mkdir -p /root/.m2 \
    && printf '%s\n' \
      '<?xml version="1.0" encoding="UTF-8"?>' \
      '<settings xmlns="http://maven.apache.org/SETTINGS/1.1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.1.0 https://maven.apache.org/xsd/settings-1.1.0.xsd">' \
      '  <mirrors>' \
      '    <mirror>' \
      '      <id>aliyun</id>' \
      '      <name>Aliyun Public Mirror</name>' \
      '      <url>https://maven.aliyun.com/repository/public</url>' \
      '      <mirrorOf>*</mirrorOf>' \
      '    </mirror>' \
      '  </mirrors>' \
      '</settings>' \
      > /root/.m2/settings.xml

# 先只复制 pom.xml，预拉取依赖（Docker layer 缓存：pom 不变则依赖层不重建）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 再复制全部源码，跳过测试打包
COPY src ./src
RUN mvn package -DskipTests -B

# ---------- 阶段 2：运行 ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# 安装 curl 用于 HEALTHCHECK（apt 需 root 权限，必须在切换到非 root 用户前）
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# 创建非 root 运行用户（安全加固）
RUN addgroup --system --gid 1001 appuser \
    && adduser --system --uid 1001 --ingroup appuser appuser

# 复制构建产物 jar（demo-data JSON 已打进 jar 内，无需额外 COPY）
# 通配符匹配 target 下唯一 jar，固定重命名为 app.jar（配合 ENTRYPOINT 使用）
COPY --from=build /app/target/*.jar ./app.jar

EXPOSE 8080

# 健康检查：每 10s 探测健康接口，3s 超时，连续 5 次失败判为不健康
HEALTHCHECK --interval=10s --timeout=3s --retries=5 CMD curl -f http://localhost:8080/api/health || exit 1

USER appuser
ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]
