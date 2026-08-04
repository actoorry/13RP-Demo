# 13RP-Demo

**13RP（博宇宙十三维决策操作系统）** P0 阶段决策演示项目。

P0 = 博宇四方管理端功能复现 + 国内场景决策演示。本项目为**可构建的项目骨架**（占位级），不含业务逻辑，后续任务将逐步实现 4RP 看板 / 6RP 孪生 / 决策交互面板。

## 技术栈

| 端 | 技术 |
|----|------|
| 后端 | Java 21 · Spring Boot 3.3.x · Maven · WebSocket |
| 前端 | Vue3 · Vite6 · TypeScript · Element Plus · Pinia · ECharts |
| 数据 | MySQL 8（现有容器 `mysql8`）· Redis 7（现有容器 `redis`） |

> 本机开发遵循《13RP-Demo/CLAUDE.md》约定，使用本机工具链（JDK / Maven / Node）；如需在服务器或其他电脑一键复现，见下方「[Docker 一键部署](#docker-一键部署)」章节。

## 目录结构

```
13RP-Demo/
├── backend/        # Spring Boot 后端（端口 8080）
├── frontend/       # Vue3 前端（Vite dev 端口 5173）
├── scripts/        # 本机构建辅助脚本
├── CLAUDE.md       # 环境路径与构建约定（必读）
└── README.md
```

## 本机启动说明

### 0. 环境要求（详见 CLAUDE.md）

- JDK 21：`C:\Users\Administrator\.jdks\ms-21.0.12`
- Maven 3.9.9：IDEA bundled（`D:\IDEA\IntelliJ IDEA 2025.1\plugins\maven\lib\maven3`）
- Node.js ≥ 20（本机 24.18.0）、npm 11（有 allow-scripts 策略，见下方"已知坑"）

### 1. 后端

```bash
cd backend
mvn package -DskipTests                 # 构建 → target/rd13-demo-0.3.0.jar
java -jar target/rd13-demo-0.3.0.jar    # 运行（端口 8080）
```

健康检查：

```bash
curl http://localhost:8080/api/health   # 期望 {"ok":true}
```

> 若 `mvn` / `java` 不在 PATH 中，可执行 `scripts\build.cmd backend`（脚本内置本机 JDK/Maven 路径）完成构建。

### 2. 前端

```bash
cd frontend
npm install         # 依赖（已配置淘宝镜像 registry.npmmirror.com）
npm run dev         # 开发（端口 5173，/api、/ws 代理到 localhost:8080）
npm run build       # 生产构建（dist/）
```

浏览器打开 `http://localhost:5173/`。

## Docker 一键部署

> 本机开发仍用上述本机工具链方式（见上）；**Docker 用于服务器 / 其他电脑一键复现**。仓库推送到 GitHub 后，目标机器克隆下来执行下方命令即可。

### 0. 前置要求

- Docker Engine ≥ 20.10（含 Docker Compose v2，新版 Docker 内置 `docker compose` 子命令）
- 目标机器**无需**安装 JDK / Maven / Node / Nginx —— 全部在容器内完成构建与运行

### 1. 启动

```bash
docker compose up -d --build
```

首次启动会自动构建两个镜像（后端 Spring Boot + 前端 Nginx）并先后台运行：

| 服务 | 容器名 | 说明 |
|------|--------|------|
| 后端 | `rd13-backend` | 端口 `8080`，提供 `/api` REST 与 `/ws` WebSocket |
| 前端 | `rd13-frontend` | 端口 `8088`，Nginx 托管静态资源并反代后端 |

浏览器打开 **http://localhost:8088**。

### 2. 停止 / 清理

```bash
docker compose down            # 停止并删除容器（镜像保留）
docker compose down --rmi all  # 连同构建的镜像一并删除
```

### 3. 更新到最新版本

```bash
git pull
docker compose up -d --build
```

### 4. 部署说明

- 无 MySQL / Redis 依赖：后端数据来自预计算 JSON + MockDataService，已内置进镜像
- 前端 Nginx 将 `/api`、`/ws` 反代到后端容器（服务名 `backend:8080`），WebSocket 已配置 Upgrade 升级头
- 若宿主机 `8080` / `8088` 端口被占用，可修改 `docker-compose.yml` 中的端口映射

## 已知坑（npm 11 allow-scripts）

npm 11 默认拦截 esbuild / vue-demi 的 postinstall 脚本。本项目通过淘宝镜像安装时 esbuild 平台二进制可正常使用，构建不受影响；若后续 `npm run build` 报 esbuild 二进制错误，执行：

```bash
npm rebuild esbuild
# 或
npm approve-scripts esbuild
```

## 版本

- 前端 / 后端：V0.3

## 相关文档

权威定义与详细设计见仓库根目录（E:\13RP）：

- `03_P0阶段_MVP/Demo技术方案与开发手册.md`（目录结构 / 核心模块设计 / 数据流）
- `03_P0阶段_MVP/Demo实施计划书.md`（分镜 / 排期）
- `03_P0阶段_MVP/P0开发委派计划.md`（任务包拆分 / 已知坑清单）
