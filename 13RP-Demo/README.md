# 13RP Demo — 博宇四方复现版 + 决策演示

> 13RP 十三维决策操作系统 · P0 演示工程（V0.3 决策演示闭环，第 1 步）
> 文档见 `E:\13RP\03_P0阶段_MVP\`

## 快速启动

```bash
docker compose up --build
# 前端: http://localhost
# 后端: http://localhost:8080
```

## 演示流程（15 分钟）

1. 打开 `http://localhost`，左侧 4RP 看板正常滚动，中央物流网络图正常
2. 点击 **"模拟：台风封港"** → 选"台风'海燕'登陆华东，宁波舟山港/上海港封港"，时长 5 天，确认注入
3. 看板订单交付率下降 + 宁波/上海节点变红 → 点击 **"启动推演"**
4. 推演进度 0%→100%（120 秒动画，可点快进）→ 3 条推荐路径弹出 → **"启动寻优"**
5. 勾选偏好（保交付/平衡/降碳）→ 方案排序变化 → **"加入博弈层"**
6. 勾选对抗因素 → 胜率变化 → **"确认方案"** → 5 条指令逐条输出（含失效条件）

## 目录结构

```
13RP-Demo/
├── docker-compose.yml
├── backend/          # Spring Boot 3 (Java 21)
│   └── src/main/resources/demo-data/   # 预计算 JSON
└── frontend/         # Vue3 + Vite + Element Plus + ECharts
```

## 验收对照

- `Demo验收场景.md` B 部分：事件注入/推演/寻优/博弈/降维输出
- 第 2 步（管理端五模块 CRUD + RBAC + MySQL/Redis）按 `Demo实施计划书.md` W1-W9 排期推进
