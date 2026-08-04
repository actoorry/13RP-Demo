# 13RP-Demo 项目环境说明（Claude Code 必读）

本文件给 Claude Code 提供本机环境路径与构建约定，避免环境探测失败浪费时间。

## 本机环境路径（Windows）

| 组件 | 路径 / 版本 | 说明 |
|------|------------|------|
| **JDK** | `C:\Users\Administrator\.jdks\ms-21.0.12`（OpenJDK 21.0.12 LTS） | JAVA_HOME 已配置为用户级环境变量 |
| **Maven** | `D:\IDEA\IntelliJ IDEA 2025.1\plugins\maven\lib\maven3`（3.9.9） | MAVEN_HOME 已配置为用户级环境变量，`mvn` 可直接使用；本地仓库 `C:\Users\Administrator\.m2\repository` |
| **Node.js** | `C:\Program Files\nodejs`（v24.18.0） | 已全局可用 |
| **npm** | 11.16.0 | ⚠️ npm 11 有 allow-scripts 策略：esbuild/vue-demi 的 postinstall 默认被拦截，若 `npm run build` 报 esbuild 二进制错误，执行 `npm approve-scripts` 或 `npm rebuild esbuild` |
| **Docker** | 29.6.2 | ⚠️ **仅允许用于 MySQL 和 Redis 容器**，其他一律本机工具链 |
| **MySQL** | 现有容器 `mysql8`（mysql:8.0） | 账号 `root` / 密码 `123456`；13RP 建库名 `boyu_demo` |
| **Redis** | 现有容器 `redis`（redis:7-alpine） | 默认端口 6379，无密码，直接连接 |
| **Nginx** | ❌ 不安装 | 前端运行用 Vite（`npm run dev` / `npm run preview`） |
| **Git** | 2.55.0 | Git Bash 位于 `C:\Program Files\Git\usr\bin\bash.exe` |

## 构建约定（必须遵守）

```powershell
# 项目根
cd E:\13RP\13RP-Demo

# 后端：本机 Maven 构建 + 运行
cd backend
mvn package -DskipTests          # 构建
java -jar target/rd13-demo-0.3.0.jar   # 运行（端口 8080）

# 前端：本机 npm 构建 + Vite 运行
cd frontend
npm install
npm run dev                       # 开发（端口 5173，/api /ws 代理到 8080）
npm run build                     # 生产构建（dist/）

# 验证
curl http://localhost:8080/api/health    # 后端
curl http://localhost:5173/              # 前端（Vite dev）
```

- **禁止使用 Docker 容器**（MySQL/Redis 除外）：不写 docker-compose 构建、不写 Dockerfile、不用 maven/node/nginx 镜像
- 后端：Spring Boot 3.3.x + Java 21，Maven 本机构建
- 前端：Vue3 + Vite 6 + TypeScript，npm 本机构建
- MySQL/Redis：连接现有容器（mysql8: root/123456，redis: 6379），通过 JDBC/Lettuce 直连，**不通过 Docker 管理**
- 端口约定：backend `8080`、前端 Vite dev `5173`

## 项目背景摘要

- **13RP**：博宇宙十三维决策操作系统（P0 = 博宇四方管理端功能复现 + 国内场景决策演示）
- **演示场景**：台风"海燕"封港 5 天（宁波舟山港/上海港）→ 342 条路径推演 → 3 方案（P1 改港青岛 / P2 空运 / P3 备选供应商）→ 4 博弈因素 → 5 条指令（合同要素字段）
- **权威文档**（在 E:\13RP 仓库）：
  - `03_P0阶段_MVP/Demo实施计划书.md`（分镜/排期）
  - `03_P0阶段_MVP/Demo技术方案与开发手册.md`（目录结构/API/数据流）
  - `03_P0阶段_MVP/Demo验收场景.md`（验收标准）
  - `03_P0阶段_MVP/P0开发委派计划.md`（任务包拆分 + 已知坑清单）
  - `04_参考资料/功能清单.md`（博宇四方管理端功能清单）

## 已知坑清单（组长实测经验，写代码前必读）

1. **Controller 重载**：REST 方法收 `@RequestBody Map`，WebSocket 直调需同名便捷重载（String/int 参数）
2. **final 字段注入**：`private final Xxx ws` 必须在构造函数注入
3. **WebSocket handler 动作分支**：前端所有 send action 必须有对应 case（trigger-event/start-simulation/start-optimization/start-gaming/confirm-plan/fast-forward/skip-simulation/reset）
4. **el-radio-group 用 `v-model`**，不要 `:model-value`（单向绑定点击后选中态不更新）
5. **Map.of 混合类型**推断失败：用 `Map.<String,Object>of(...)` 或 HashMap
6. **PowerShell curl 传 JSON**：用单引号 `-d '{"a":1}'`（双引号会转义错误 → 400）
7. **进度动画**：progress 状态单一来源（TimelineController），经 StateMachine 广播
8. **前端刷新恢复**：onMounted 拉 `/api/demo/state` 后按 phase 恢复数据（solutions/instructions）
