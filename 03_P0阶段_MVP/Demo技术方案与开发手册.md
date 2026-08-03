# Demo 技术方案与开发手册 — "13RP一号"

> **日期**：2026 年 7 月 31 日
> **版本**：V0.3（Java + Vue3 + Three.js）
> **前置阅读**：`Demo验收场景.md`、`Demo实施计划书.md`、`00_项目总览/13RP总纲.md`

---

# 第一部分：技术方案

## 一、项目总体架构

```
┌─────────────────────────────────────────────────────────┐
│                    浏览器（观众可见）                     │
│                                                         │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────────┐  │
│  │ 4RP 监控看板  │  │ 6RP 数字孪生  │  │ 决策交互面板   │  │
│  │ Vue3+ECharts │  │  Three.js    │  │ Vue3+Element  │  │
│  └──────┬───────┘  └──────┬───────┘  └───────┬───────┘  │
│         │                 │                   │          │
│         └─────────────────┼───────────────────┘          │
│                           │ WebSocket + REST             │
└───────────────────────────┼──────────────────────────────┘
                            │
┌───────────────────────────┼──────────────────────────────┐
│                           ▼                              │
│                  Spring Boot 3.x（后台）                   │
│                                                         │
│  ┌──────────────────────────────────────────────────┐  │
│  │           Demo Orchestrator（状态机核心）           │  │
│  └──────────────────────────────────────────────────┘  │
│         │              │              │                  │
│  ┌──────▼──────┐ ┌─────▼──────┐ ┌────▼─────────────┐   │
│  │ MockDataGen │ │Precomputed │ │ WebSocketHandler │   │
│  │ (时序数据)   │ │ (JSON方案) │ │ (实时推送)        │   │
│  └─────────────┘ └────────────┘ └──────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

## 二、项目目录结构

```
13RP-Demo/
├── docker-compose.yml                 # 一键启动
├── README.md
│
├── frontend/                          # Vue3 前端项目
│   ├── package.json
│   ├── pnpm-lock.yaml
│   ├── vite.config.ts
│   ├── tailwind.config.ts
│   ├── tsconfig.json
│   ├── index.html
│   ├── public/
│   │   └── models/                    # Three.js 静态模型（GLB/GLTF）
│   └── src/
│       ├── main.ts                    # 入口
│       ├── App.vue                    # 根组件（三屏布局壳）
│       ├── router/
│       │   └── index.ts              # 路由（含 #/{state} hash 路由）
│       ├── stores/
│       │   ├── dashboard.ts          # 4RP 看板数据 store
│       │   ├── twin.ts               # 6RP 孪生状态 store
│       │   └── demoState.ts          # Demo 状态机 store
│       ├── composables/
│       │   ├── useWebSocket.ts       # WebSocket 连接与重连
│       │   └── useCountUp.ts         # 数字滚动动画
│       ├── components/
│       │   ├── layout/
│       │   │   ├── ThreePanelLayout.vue  # 三屏布局容器
│       │   │   └── StatusBar.vue         # 底部状态栏
│       │   ├── dashboard/
│       │   │   ├── DashboardPanel.vue    # 4RP 看板主面板
│       │   │   ├── GaugeCard.vue         # 指标卡片（ECharts 仪表盘）
│       │   │   └── AlertBanner.vue       # 红色报警横幅
│       │   ├── twin/
│       │   │   ├── TwinScene.vue         # Three.js 场景容器
│       │   │   ├── FactoryNode.vue       # 工厂节点（桥接 Three.js）
│       │   │   └── LogisticsRoute.vue    # 物流路线（桥接 Three.js）
│       │   ├── decision/
│       │   │   ├── DecisionPanel.vue     # 决策交互主面板
│       │   │   ├── SimulationProgress.vue # 推演进度条
│       │   │   ├── RadarChart.vue        # 方案雷达图
│       │   │   ├── PlanCard.vue          # 方案卡片
│       │   │   ├── GamePanel.vue         # 博弈对抗面板
│       │   │   └── InstructionList.vue   # 指令列表
│       │   └── common/
│       │       ├── DemoButton.vue        # 统一按钮样式
│       │       └── ConfirmDialog.vue     # 确认弹窗
│       ├── utils/
│       │   ├── constants.ts             # 颜色/尺寸/字体常量
│       │   └── formatters.ts            # 数值格式化
│       └── assets/
│           └── styles/
│               └── global.css           # 全局样式 + TailwindCSS
│
├── backend/                           # Spring Boot 后端项目
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│       ├── main/
│       │   ├── java/com/rd13/demo/
│       │   │   ├── Rd13DemoApplication.java      # 启动类
│       │   │   ├── config/
│       │   │   │   ├── WebSocketConfig.java      # WebSocket 配置
│       │   │   │   └── CorsConfig.java           # CORS 配置
│       │   │   ├── orchestrator/
│       │   │   │   ├── DemoStateMachine.java     # 状态机核心
│       │   │   │   ├── DemoPhase.java            # 阶段枚举
│       │   │   │   └── TimelineController.java   # 时间轴控制器
│       │   │   ├── websocket/
│       │   │   │   ├── DemoWebSocketHandler.java # WebSocket 主处理器
│       │   │   │   └── WebSocketSessionManager.java # 会话管理
│       │   │   ├── controller/
│       │   │   │   └── DemoController.java       # REST API
│       │   │   ├── service/
│       │   │   │   └── MockDataService.java      # 模拟数据生成
│       │   │   └── model/
│       │   │       ├── FactoryData.java          # 工厂数据模型
│       │   │       ├── SimulationPath.java       # 推演路径模型
│       │   │       ├── SolutionPlan.java         # 方案模型
│       │   │       ├── GameResult.java           # 博弈结果模型
│       │   │       └── Instruction.java           # 指令模型
│       │   └── resources/
│       │       ├── application.yml
│       │       └── demo-data/
│       │           ├── paths.json                # 342条路径（核心3条+占位）
│       │           ├── solutions.json            # 3组帕累托方案
│       │           ├── game-results.json         # 博弈胜率矩阵
│       │           └── instructions.json         # 5条指令模板
│       └── test/java/com/rd13/demo/
│           ├── orchestrator/
│           │   └── DemoStateMachineTest.java
│           └── service/
│               └── MockDataServiceTest.java
│
└── docs/
    └── demo-script.md                  # 演示者提词卡
```

## 三、核心模块设计

### 3.1 Demo 状态机（DemoStateMachine.java）

状态机是 Demo 中台的核心，控制整个 15 分钟演示的节奏。

```
┌──────┐  注入事件   ┌───────────────┐  启动推演   ┌────────────┐
│ INIT │ ─────────→ │ EVENT_INJECTED │ ─────────→ │ SIMULATING │
└──────┘             └───────────────┘             └─────┬──────┘
                                                        │ 推演完成
                       ┌──────────────┐                  │
                       │ PLAN_SELECTED│ ←──┐      ┌──────▼───────┐
                       └──────┬───────┘    │      │SIMULATION_DONE│
                              │ 确认方案    │      └──────┬───────┘
                              │            │             │ 启动寻优
                       ┌──────▼───┐   ┌────┴─────┐ ┌────▼──────┐
                       │   DONE   │   │  GAMING  │ │ OPTIMIZING │
                       └──────────┘   └──────────┘ └───────────┘
```

每个状态变化通过 WebSocket 广播到所有前端客户端，前端根据状态渲染对应面板。

### 3.2 模拟数据生成器（MockDataService.java）

三工厂（中国苏州、越南海防、墨西哥蒙特雷）的时序数据生成逻辑：

```java
// 核心生成逻辑（伪代码，与实施计划书 4.1 保持一致）
@Service
public class MockDataService {
    private final Map<String, FactoryParams> factories = new ConcurrentHashMap<>();

    // 三工厂基础参数（容量均值, 噪声, 在制品, 成品, 交付率, OEE, 电力状态）
    public MockDataService() {
        factories.put("CN", new FactoryParams(87, 3, 1250, 340, 94, 82, PowerStatus.NORMAL));
        factories.put("VN", new FactoryParams(78, 2, 890, 210, 91, 76, PowerStatus.NORMAL));
        factories.put("MX", new FactoryParams(92, 1, 1420, 180, 96, 88, PowerStatus.NORMAL));
    }

    // 每秒生成一次带高斯噪声的时序数据
    @Scheduled(fixedRateString = "${demo.data-push-interval-ms:1000}")
    public void generateAndPush() {
        Map<String, FactoryData> tick = new HashMap<>();
        factories.forEach((code, params) -> {
            tick.put(code, FactoryData.builder()
                .capacityUtilization(addGaussianNoise(params.capacity, params.noise))
                .wipInventory((int) addGaussianNoise(params.wip, params.wip * 0.02))
                .finishedInventory((int) addGaussianNoise(params.finished, params.finished * 0.02))
                .deliveryRate(addGaussianNoise(params.delivery, 1.0))
                .oee(addGaussianNoise(params.oee, 2.0))
                .powerStatus(params.powerStatus) // 正常/停电
                .build());
        });
        webSocketHandler.broadcastDashboard(tick);
    }

    // 注入停电事件：指定工厂产能直接置零
    public void injectPowerOutage(String factoryCode) {
        FactoryParams params = factories.get(factoryCode);
        params.setCapacity(0);
        params.setOee(0);
        params.setPowerStatus(PowerStatus.OUTAGE);
    }

    private double addGaussianNoise(double mean, double stdDev) {
        return Math.max(0, mean + ThreadLocalRandom.current().nextGaussian() * stdDev);
    }
}
```

### 3.3 WebSocket 通信设计

| channel 字段 | 方向 | 数据内容 | 频率 |
|------|:---:|---------|:---:|
| `dashboard` | 后端→前端 | 三工厂实时指标 JSON | 每秒 1 次 |
| `twin-event` | 后端→前端 | 孪生场景事件（模型变色、路线闪烁等） | 按需触发 |
| `demo-state` | 后端→前端 | 状态机状态变更 + 进度百分比 | 状态切换时 |
| `demo-action` | 前端→后端 | 用户操作（注入事件、启动推演等，经消息体 `action` 字段区分） | 用户操作时 |

> **说明**：P0 采用**原生 WebSocket**（`/ws/demo` 端点，见 3.1 代码），不引入 STOMP 协议。上表为消息体 JSON 中的 `channel` 字段，前端 `useWebSocket.ts` 据此分发到对应 Pinia store。

消息格式（JSON）：

```json
// dashboard 消息体
{
  "timestamp": "2026-07-31T14:30:00",
  "factories": {
    "CN": {"capacityUtilization": 87.3, "wipInventory": 1248, "finishedInventory": 342, "deliveryRate": 94.1, "oee": 82.5, "powerStatus": "NORMAL"},
    "VN": {"capacityUtilization": 78.1, "wipInventory": 892,  "finishedInventory": 208, "deliveryRate": 91.3, "oee": 76.2, "powerStatus": "NORMAL"},
    "MX": {"capacityUtilization": 92.0, "wipInventory": 1418, "finishedInventory": 182, "deliveryRate": 96.0, "oee": 88.1, "powerStatus": "NORMAL"}
  }
}

// demo-state 消息体
{
  "state": "SIMULATING",
  "progress": 67,
  "message": "已跑完 228/342 条路径...",
  "activePaths": ["越南增产+海运", "中国空运", "墨西哥减产"]
}
```

### 3.4 REST API 设计

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/api/demo/trigger-event` | 注入突发事件（body: `{"eventType":"power_outage","duration":5}`） |
| `POST` | `/api/demo/start-simulation` | 启动 7RP 推演 |
| `POST` | `/api/demo/start-optimization` | 启动 8RP 寻优（body 可选偏好参数） |
| `POST` | `/api/demo/start-gaming` | 启动 9RP 博弈对抗（body: 勾选的对抗因素列表） |
| `POST` | `/api/demo/confirm-plan` | 确认最终方案（body: `{"planId":"P1"}`） |
| `GET`  | `/api/demo/state` | 获取当前 Demo 状态 |
| `GET`  | `/api/demo/solutions?preference={type}` | 获取预计算方案（按偏好排序） |
| `GET`  | `/api/demo/game-results?factors={list}` | 获取博弈结果 |
| `GET`  | `/api/demo/instructions?planId={id}` | 获取最终指令列表 |

### 3.5 Three.js 孪生场景设计

**场景结构**：

```
Scene
├── 地球底图（平面或简化球体，贴世界地图纹理）
├── 工厂节点组
│   ├── 中国·苏州   [位置: (120.6E, 31.3N)]  默认 🟢 绿色立方体
│   ├── 越南·海防   [位置: (106.7E, 20.9N)]  默认 🟢 绿色立方体
│   └── 墨西哥·蒙特雷 [位置: (100.3W, 25.7N)] 默认 🟢→🔴 事件触发后变红
├── 物流路线组
│   ├── 中国 → 墨西哥 海运路线（28天）
│   ├── 越南 → 墨西哥 海运路线（21天）
│   └── 中国 → 墨西哥 空运路线（72h）
├── 粒子特效
│   ├── 红色波纹扩散（停电工厂周围）
│   └── 路线流动粒子（船舶/飞机图标沿路径移动）
└── OrbitControls（支持旋转/缩放/平移）
```

**Vue3 集成方式**：

```typescript
// components/twin/TwinScene.vue 核心逻辑（伪代码）
import * as THREE from 'three';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js';

// 初始化场景
const scene = new THREE.Scene();
const camera = new THREE.PerspectiveCamera(45, width/height, 0.1, 1000);
const renderer = new THREE.WebGLRenderer({ antialias: true });

// 创建工厂节点（带 CSS 标签的立方体）
function createFactoryNode(lat: number, lng: number, label: string, color: THREE.Color) {
  const geometry = new THREE.BoxGeometry(0.5, 0.5, 0.5);
  const material = new THREE.MeshBasicMaterial({ color });
  const cube = new THREE.Mesh(geometry, material);
  // 经纬度转 3D 坐标
  const pos = latLngToVec3(lat, lng, 5);
  cube.position.copy(pos);
  scene.add(cube);
  return cube;
}

// 物流路线动画（LineRenderer + 粒子沿路径）
function animateRoute(start: THREE.Vector3, end: THREE.Vector3, color: THREE.Color) {
  // 使用 CubicBezierCurve3 弯曲地球表面
  const curve = new THREE.CubicBezierCurve3(start, mid, mid, end);
  // 粒子沿曲线移动
}

// 响应 WebSocket 事件
function handleTwinEvent(event: TwinEvent) {
  switch (event.type) {
    case 'FACTORY_ALERT':
      factories.get(event.factoryId).material.color.set('#FF4757');
      playRippleEffect(event.position);
      break;
    case 'ROUTE_HIGHLIGHT':
      routes.get(event.routeId).highlight();
      break;
  }
}
```

## 四、数据流设计

### 4.1 整体数据流

```
MockDataService(Java)          DemoStateMachine(Java)
       │                              │
       │ @Scheduled(1s)               │ 状态变更时
       ▼                              ▼
WebSocketHandler ──── 广播 ──── WebSocketHandler
       │                              │
       │ dashboard                  │ demo-state
       ▼                              ▼
  Pinia: dashboardStore         Pinia: demoStateStore
       │                              │
       ├──────────────────────────────┤
       ▼                              ▼
  ECharts 仪表盘更新           Vue 条件渲染面板切换
       │                              │
       │                              ├── 进度条动画触发
       │                              ├── 方案卡片排序动画
       │                              └── 指令逐条弹出
```

### 4.2 预计算数据流（5 个阶段的衔接）

```
阶段 1: 事件注入
  用户点击"停电" → POST /api/demo/trigger-event
  → StateMachine: INIT → EVENT_INJECTED
  → MockDataService: factories.MX.powerStatus = OUTAGE, capacity = 0
  → WebSocket 持续推送归零数据（50帧动画，5秒完成）

阶段 2: 推演（预计算回放）
  用户点击"启动推演" → POST /api/demo/start-simulation
  → StateMachine: EVENT_INJECTED → SIMULATING
  → Orchestrator 播放 120 秒进度条动画（每0.5秒更新progress%）
  → 路径文本从 paths.json 的6个路径名中随机轮换显示
  → 进度到 100% 后，从 paths.json 取出核心3条路径推送给前端

阶段 3: 多目标寻优（预计算排序）
  用户点击"启动寻优" → POST /api/demo/start-optimization
  → StateMachine: SIMULATION_DONE → OPTIMIZING
  → 前端取 solutions.json，按用户偏好字段重新排序
  → 用户切换偏好时纯前端排序动画，不请求后端
  → ECharts 雷达图同步更新

阶段 4: 博弈对抗（预计算矩阵）
  用户点击"加入博弈层" → POST /api/demo/start-gaming
  → StateMachine: OPTIMIZING → GAMING
  → 前端取 game-results.json，按勾选的对抗因素匹配胜率
  → 胜率数字 countUp.js 动画 + 卡片震荡动画

阶段 5: 降维输出（模板化填充）
  用户点击"确认方案 P1" → POST /api/demo/confirm-plan
  → StateMachine: GAMING → PLAN_SELECTED
  → 前端取 instructions.json 中 P1 的5条指令模板
  → 当前时间向上取整 → 填充截止时间字段
  → 指令逐条 typewriter 动画弹出
```

## 五、关键代码骨架

### 5.1 后端入口配置

```java
// application.yml
server:
  port: 8080

spring:
  jackson:
    property-naming-strategy: SNAKE_CASE

demo:
  simulation-duration-seconds: 120   # 推演动画时长
  data-push-interval-ms: 1000        # 数据推送间隔

// WebSocketConfig.java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(demoWebSocketHandler(), "/ws/demo")
                .setAllowedOrigins("*");
    }
}

// DemoStateMachine.java
@Component
public class DemoStateMachine {
    private DemoPhase currentPhase = DemoPhase.INIT;

    public synchronized DemoPhase transition(DemoPhase target) {
        if (!currentPhase.canTransitionTo(target)) {
            throw new IllegalStateException(
                "Cannot transition from " + currentPhase + " to " + target);
        }
        DemoPhase previous = currentPhase;
        currentPhase = target;
        // 广播状态变更
        webSocketHandler.broadcastState(target, previous);
        return currentPhase;
    }
}

// DemoPhase.java
public enum DemoPhase {
    INIT,
    EVENT_INJECTED,
    SIMULATING,
    SIMULATION_DONE,
    OPTIMIZING,
    GAMING,
    PLAN_SELECTED,
    DONE;

    // 合法的状态转移规则
    private static final Map<DemoPhase, Set<DemoPhase>> ALLOWED = Map.of(
        INIT,             Set.of(EVENT_INJECTED),
        EVENT_INJECTED,   Set.of(SIMULATING),
        SIMULATING,       Set.of(SIMULATION_DONE),
        SIMULATION_DONE,  Set.of(OPTIMIZING),
        OPTIMIZING,       Set.of(GAMING),
        GAMING,           Set.of(PLAN_SELECTED),
        PLAN_SELECTED,    Set.of(DONE)
    );

    public boolean canTransitionTo(DemoPhase target) {
        return ALLOWED.getOrDefault(this, Set.of()).contains(target);
    }
}
```

### 5.2 前端核心组件骨架

```typescript
// composables/useWebSocket.ts
export function useWebSocket() {
  const ws = ref<WebSocket | null>(null);
  const connected = ref(false);
  const reconnectAttempt = ref(0);

  function connect() {
    // 生产环境通过 VITE_WS_URL 注入（如 wss://demo.13rp.local/ws/demo）
    const wsUrl = import.meta.env.VITE_WS_URL ?? 'ws://localhost:8080/ws/demo';
    ws.value = new WebSocket(wsUrl);

    ws.value.onopen = () => {
      connected.value = true;
      reconnectAttempt.value = 0;
    };

    ws.value.onmessage = (event) => {
      const msg = JSON.parse(event.data);
      // 按 channel 分发给对应的 Pinia store
      if (msg.channel === 'dashboard') {
        dashboardStore.update(msg.data);
      } else if (msg.channel === 'demo-state') {
        demoStateStore.transition(msg.data.state, msg.data.progress);
      } else if (msg.channel === 'twin-event') {
        twinStore.handleEvent(msg.data);
      }
    };

    ws.value.onclose = () => {
      connected.value = false;
      // 指数退避重连（max 5s）
      const delay = Math.min(1000 * Math.pow(2, reconnectAttempt.value), 5000);
      reconnectAttempt.value++;
      setTimeout(connect, delay);
    };
  }

  onMounted(connect);
  onUnmounted(() => ws.value?.close());

  return { connected };
}

// stores/demoState.ts
export const useDemoStateStore = defineStore('demoState', () => {
  const currentPhase = ref<DemoPhase>('INIT');
  const progress = ref(0);        // 当前阶段进度 0-100
  const phaseMessage = ref('');   // 阶段提示文字

  function transition(phase: DemoPhase, prog?: number) {
    currentPhase.value = phase;
    progress.value = prog ?? 0;
    // 同步到 URL hash（支持刷新恢复）
    window.location.hash = `#/${phase}`;
  }

  // 各阶段对应展示的面板
  const activePanel = computed(() => {
    const map: Record<DemoPhase, string> = {
      INIT: 'dashboard',
      EVENT_INJECTED: 'dashboard',
      SIMULATING: 'simulation',
      SIMULATION_DONE: 'simulation',
      OPTIMIZING: 'optimization',
      GAMING: 'gaming',
      PLAN_SELECTED: 'instructions',
      DONE: 'instructions',
    };
    return map[currentPhase.value];
  });

  return { currentPhase, progress, phaseMessage, activePanel, transition };
});

// stores/dashboard.ts
export const useDashboardStore = defineStore('dashboard', () => {
  const factories = ref<Record<string, FactoryData>>({});

  function update(data: DashboardMessage) {
    factories.value = data.factories;
  }

  // 计算各指标面板需要的 ECharts option
  const capacityChartOption = computed(() => ({
    // ECharts gauge 配置
    series: [{
      type: 'gauge',
      data: Object.entries(factories.value).map(([code, f]) => ({
        value: f.capacityUtilization,
        name: code === 'CN' ? '中国' : code === 'VN' ? '越南' : '墨西哥',
        itemStyle: { color: f.powerStatus === 'OUTAGE' ? '#FF4757' : '#00D4AA' }
      }))
    }]
  }));

  return { factories, capacityChartOption, update };
});
```

---

# 第二部分：开发手册

## 六、环境搭建

### 6.1 必需软件

| 软件 | 最低版本 | 说明 |
|------|:---:|------|
| JDK | 21 (LTS) | 后端编译与运行 |
| Node.js | 20 LTS | 前端构建 |
| pnpm | 9.x | 前端包管理 |
| Docker | 24+ | 容器化部署 |
| Maven | 3.9+ | 后端构建（或用 `./mvnw` 自带 wrapper） |

### 6.2 首次搭建步骤

```bash
# 1. 克隆仓库
git clone <repo-url> 13RP-Demo
cd 13RP-Demo

# 2. 后端
cd backend
./mvnw clean compile              # 编译（首次会下载依赖，约 2-5 分钟）
./mvnw spring-boot:run            # 启动（默认 :8080）

# 3. 前端（另开终端）
cd frontend
pnpm install                      # 安装依赖（淘宝镜像）
pnpm dev                          # Vite 开发服务器（默认 :5173）

# 4. 浏览器访问（macOS 用 open，Windows 用 start）
open http://localhost:5173        # 前端页面（macOS）
start http://localhost:5173       # 前端页面（Windows）
open http://localhost:8080/api/demo/state  # 后端 API 健康检查（macOS）
start http://localhost:8080/api/demo/state # 后端 API 健康检查（Windows）
```

### 6.3 Docker Compose 一键启动（演示环境）

```bash
# 项目根目录
docker compose up -d

# 确认三个容器全部 healthy
docker compose ps
# 输出示例：
# NAME                STATUS
# demo-frontend       Up (healthy)
# demo-backend        Up (healthy)
# demo-static         Up (healthy)
```

### 6.4 环境变量

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `SERVER_PORT` | `8080` | 后端端口 |
| `WS_MAX_SESSIONS` | `10` | WebSocket 最大连接数 |
| `MOCK_DATA_INTERVAL_MS` | `1000` | 模拟数据推送间隔 |
| `SIMULATION_DURATION_SEC` | `120` | 推演动画时长 |
| `DEMO_PROFILE` | `dev` | `dev`（允许跳过阶段）/ `prod`（严格流程） |
| `VITE_WS_URL` | `ws://localhost:8080/ws/demo` | 前端 WebSocket 地址（生产环境为 `wss://demo.13rp.local/ws/demo`） |

## 七、编码规范

### 7.1 Java 后端规范

- 包结构：`com.rd13.demo.{module}`，module 按功能划分
- 依赖注入：构造器注入优先，避免字段 `@Autowired`
- 异常处理：Controller 层统一用 `@RestControllerAdvice` 捕获
- WebSocket 会话管理：`ConcurrentHashMap<String, WebSocketSession>` 存储，注意并发安全
- 日志：SLF4J + Logback，关键状态变更必须 log（INFO 级别）
- 虚拟线程：WebSocket 的 onMessage 处理适合用虚拟线程（`Executors.newVirtualThreadPerTaskExecutor()`），避免阻塞平台线程

### 7.2 Vue3 前端规范

- 组件风格：`<script setup lang="ts">` + Composition API
- Props/Emits 必须显式声明 TypeScript 类型
- Pinia store 使用 Setup Store 风格（`defineStore('name', () => {...})`）
- CSS：优先 TailwindCSS 原子类，复杂样式用 `<style scoped>`
- 目录约定：组件文件用 PascalCase（`DashboardPanel.vue`），composable 用 camelCase（`useWebSocket.ts`）
- 颜色/尺寸：统一从 `utils/constants.ts` 引用，不要硬编码色值

### 7.3 Three.js 编码规范

- THREE.js 代码只允许出现在 `components/twin/` 目录下
- 场景初始化、材质、几何体等 Three.js 对象封装为独立的 composable
- 所有 Three.js 对象必须在 `onUnmounted` 中调用 `.dispose()` 释放 WebGL 资源
- 动画循环统一由 `TwinScene.vue` 管理，子组件通过 emit 注册/注销动画回调

## 八、Git 工作流

```
master（主分支，保护分支）
  └── develop（开发分支）
        ├── feature/4rp-dashboard     → W3 看板开发
        ├── feature/6rp-threejs       → W4 Three.js 场景
        ├── feature/demo-orchestrator → W5 状态机
        ├── feature/simulation-ui     → W5-W6 推演+寻优面板
        ├── feature/game-panel        → W7 博弈面板
        ├── feature/instructions      → W8 指令面板
        └── feature/integration       → W9 全流程联调
```

**分支策略**：
- `master`：只接受 `develop` 的 merge，每次 merge 对应一个里程碑
- `develop`：日常集成分支
- `feature/*`：每人一个 feature 分支，功能完成后提 PR 到 `develop`
- PR 必须至少 1 人 review 后才能 merge

**提交规范**：
```
<type>(<scope>): <subject>

类型：feat / fix / style / refactor / test / docs / chore
范围：backend / frontend / threejs / dashboard / decision / docker
示例：feat(backend): 实现 DemoStateMachine 状态转移
      fix(frontend): 修复 WebSocket 断连后雷达图不更新
```

## 九、每日开发流程

### W1-W4（基础搭建阶段）

```
上午 (9:00-12:00)     下午 (14:00-18:00)      晚上
─────────────────    ──────────────────     ─────────
写代码                Code Review            自由学习
                      30min 站会（16:00）
```

### W5-W8（核心开发阶段）

```
上午 (9:00-12:00)     下午 (14:00-18:00)      晚上
─────────────────    ──────────────────     ─────────
写代码                分模块联调              修 Bug
                      30min 站会（16:00）
```

### W9-W12（联调打磨阶段）

```
上午 (9:00-12:00)     下午 (14:00-18:00)      晚上
─────────────────    ──────────────────     ─────────
全流程联调            彩排 + Bug 修复         修 Bug
                      每日站会（10:00）
```

### 站会议题（15-30 分钟）

1. 昨天完成了什么？
2. 今天计划做什么？
3. 遇到什么阻塞？需要谁协助？
4. 当前 Demo 流程从头到尾能跑通吗？（W5 之后每天必问）

## 十、常见问题与踩坑预案

### 10.1 WebSocket 相关

| 问题 | 原因 | 解决 |
|------|------|------|
| 看板数据突然不刷新 | WebSocket 断连 | 检查浏览器控制台 → 确认重连机制触发 → 检查后端 `WebSocketSessionManager` 是否清理了旧 session |
| 首次连接失败 | CORS 或防火墙 | 确认 `CorsConfig.setAllowedOrigins("*")` 已配置；开发环境用 `localhost:5173` |
| 多个标签页同时打开导致数据重复推送 | 多个 WebSocket 连接 | 后端 `WebSocketSessionManager` 已用 `ConcurrentHashMap` 去重，前端用 `onWindowBlur` 暂停非活跃标签页 |

### 10.2 Three.js 相关

| 问题 | 原因 | 解决 |
|------|------|------|
| 浏览器加载 Three.js 白屏 > 5 秒 | WebGL 初始化慢 | 首帧显示 Loading 动画；`renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))` 限制像素比 |
| 低配设备帧率 < 20fps | 模型面数过高或粒子过多 | 工厂用 BoxGeometry（12 三角面/个）；粒子控制在 200 以内 |
| 内存泄漏（长时间运行后帧率下降） | Three.js 对象未释放 | 每次 `onUnmounted` 调用 `geometry.dispose()` + `material.dispose()` + `texture.dispose()` |

### 10.3 Docker 相关

| 问题 | 原因 | 解决 |
|------|------|------|
| `docker compose up` 启动失败 | 镜像拉取慢 | 配置 Docker 国内镜像加速器（阿里云容器镜像服务） |
| 容器启动后前端访问不到后端 | 网络未互通 | 确认 `docker-compose.yml` 中容器在同一 `network` 下，前端 nginx `proxy_pass` 指向 `http://demo-backend:8080` |

### 10.4 阿里云部署

- 安全组开放端口：`80`（前端）/ `8080`（后端 API）/ `443`（HTTPS）
- 域名 + SSL 证书提前 3 天配置
- Demo 前 3 天完成压力测试（模拟 5 个并发 WebSocket 连接 + Unity/Three.js 持续渲染 30 分钟）

---

## 十一、附录

### A. 开发工具推荐

| 用途 | 工具 |
|------|------|
| Java IDE | IntelliJ IDEA Community 2024+ |
| 前端 IDE | VS Code + Vue Official 插件 + Tailwind CSS IntelliSense |
| API 测试 | Postman 或 IDEA HTTP Client |
| WebSocket 调试 | Chrome DevTools → Network → WS 标签 |
| Three.js 调试 | Chrome DevTools → Three.js Inspector 扩展 |
| Git 图形化 | Sourcetree 或 GitLens（VS Code 插件） |

### B. Demo 状态快速检查

```bash
# 后端健康
curl http://localhost:8080/api/demo/state

# WebSocket 连通性
wscat -c ws://localhost:8080/ws/demo

# Docker 状态
docker compose ps
docker compose logs -f --tail=50

# 前端构建
cd frontend && pnpm build && pnpm preview
```

### C. 关键文档索引

| 问题 | 查阅文档 |
|------|---------|
| 为什么要这样做？ | `00_项目总览/项目立意.md` |
| 这个层的职责是什么？ | `01_技术白皮书/三层架构详解.md` |
| Demo 15 分钟具体怎么演？ | `Demo验收场景.md` |
| 我的 W 周交付物是什么？ | `Demo实施计划书.md` 第五章 |
| 按钮颜色用哪个？ | 本文档 UI 规范（`utils/constants.ts`） |
| 竞品是怎么做的？ | `05_可行性分析/竞品与参考产品调研.md` |

---

> **下一篇**：环境搭建完成后，按 `Demo实施计划书.md` 第五章的 12 周排期进入 W1 执行喵。
