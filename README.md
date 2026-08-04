# 13RP-Demo

**13RP（博宇宙十三维决策操作系统）** P0 阶段决策演示项目。

P0 = 博宇四方管理端功能复现 + 国内场景决策演示。本项目为**可构建的项目骨架**（占位级），不含业务逻辑，后续任务将逐步实现 4RP 看板 / 6RP 孪生 / 决策交互面板。

## 技术栈

| 端 | 技术 |
|----|------|
| 后端 | Java 21 · Spring Boot 3.3.x · Maven · WebSocket |
| 前端 | Vue3 · Vite6 · TypeScript · Element Plus · Pinia · ECharts |
| 数据 | MySQL 8（现有容器 `mysql8`）· Redis 7（现有容器 `redis`） |

> 遵循《13RP-Demo/CLAUDE.md》约定：**禁止 Docker 容器**（MySQL/Redis 除外），全部使用本机工具链。

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
