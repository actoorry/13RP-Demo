# 13RP-Demo

**13RP（博宇宙十三维决策操作系统）** P0 阶段决策演示项目。

P0 = 博宇企业管理平台九大业务域管理端复现（V0.4 五模块：系统基础 / 采购 / 销售 / 库存 / 财务 / 人力）+ 国内场景决策演示（电解铜断供推演）。

本项目为**可运行的完整 Demo**：后端 Spring Boot 提供管理端九域 REST API（JWT 认证 + 权限码）+ 决策演示 WebSocket；前端 Vue3 提供管理端 25+ 页面 + 决策演示三屏驾驶舱。本机启动后，同一局域网内的其他电脑可通过浏览器访问。

## 技术栈

| 端 | 技术 |
|----|------|
| 后端 | Java 21 · Spring Boot 3.3.x · MyBatis-Plus · Spring Security JWT · WebSocket · MySQL 8 · Redis（可降级） |
| 前端 | Vue3 · Vite6 · TypeScript · Element Plus · Pinia · ECharts |
| 数据 | MySQL 8（现有容器 `mysql8`，库 `boyu_demo`）· Redis 7（现有容器 `redis`，非硬依赖，连不上自动降级查库） |

## 目录结构

```
13RP-Demo/
├── backend/        # Spring Boot 后端（端口 8080）
│   └── src/main/resources/
│       ├── schema.sql   # 建表（幂等，启动自动执行）
│       ├── data.sql     # 种子数据（幂等，启动自动执行）
│       └── demo-data/   # 决策演示预计算 JSON（342 路径 / 3 方案 / 4 因素 / 5 指令）
├── frontend/       # Vue3 前端（Vite dev 端口 5173，/api、/ws 代理到 8080）
├── scripts/        # 本机构建辅助脚本（build.cmd）
├── CLAUDE.md       # 环境路径与构建约定
└── README.md
```

## 本机启动说明

### 0. 环境要求

- JDK 21：`C:\Users\Administrator\.jdks\ms-21.0.12`
- Maven 3.9.9：IDEA bundled（`D:\IDEA\IntelliJ IDEA 2025.1\plugins\maven\lib\maven3`）
- Node.js ≥ 20（本机 24.18.0）、npm 11
- MySQL 8 容器 `mysql8`（root/123456，库 `boyu_demo` 自动初始化）；Redis 容器 `redis`（可选）

### 1. 后端

```bash
cd backend
mvn package -DskipTests                 # 构建 → target/rd13-demo-0.4.0.jar
java -jar target/rd13-demo-0.4.0.jar    # 运行（端口 8080）
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
npm run dev         # 开发（端口 5173，已配置 host 0.0.0.0 监听所有网卡）
npm run build       # 生产构建（dist/）
```

浏览器打开 `http://localhost:5173/`。

## 局域网访问

服务在本机启动后，同一局域网内的其他电脑可通过浏览器直接访问：

```
http://<本机IP>:5173
```

登录账号：`admin` / `123456`

### 准备工作（一次即可）

1. **放行防火墙端口**（Windows 需管理员执行一次）：

```powershell
netsh advfirewall firewall add rule name="13RP-Demo Frontend 5173" dir=in action=allow protocol=TCP localport=5173
netsh advfirewall firewall add rule name="13RP-Demo Backend 8080" dir=in action=allow protocol=TCP localport=8080
```

2. **确认本机 IP**：

```powershell
ipconfig        # 查看 IPv4 地址（如 192.168.2.52）
```

3. **确认访问**：在本机浏览器打开 `http://<本机IP>:5173` 验证一次；其他电脑需与演示机在同一网段（同一 WiFi / 网线）。

## 版本

- 前端 / 后端：V0.4
