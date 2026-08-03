# Demo 技术方案与开发手册 — "博宇四方复现版 + 13RP 决策演示"

> **日期**：2026 年 8 月 3 日
> **版本**：V0.3（功能复现 + 国内场景决策演示）
> **前置阅读**：`Demo验收场景.md`、`Demo实施计划书.md`、`00_项目总览/13RP总纲.md`、`04_参考资料/功能清单.md`

---

# 第一部分：技术方案

## 一、项目总体架构

```
┌─────────────────────────────────────────────────────────────┐
│                    浏览器（观众可见）                          │
│                                                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │ 管理端页面     │  │ 4RP 看板      │  │ 决策交互面板      │  │
│  │ Vue3+Element  │  │ ECharts 实时  │  │ Vue3+Element    │  │
│  │ (五大模块)     │  │ + 物流网络图   │  │ (推演/寻优/博弈)  │  │
│  └──────┬───────┘  └──────┬───────┘  └───────┬──────────┘  │
│         │                 │                   │             │
│         └─────────────────┼───────────────────┘             │
│                           │ REST + WebSocket                │
└───────────────────────────┼─────────────────────────────────┘
                            │
┌───────────────────────────┼─────────────────────────────────┐
│                           ▼                                 │
│                  Spring Boot 3.x（后台）                      │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐  │
│  │              管理端服务（真实 CRUD + RBAC）              │  │
│  │  system(架构) / commodity(商品) / crm / statistics     │  │
│  │  / operation(运营)                                    │  │
│  └──────────────────────────────────────────────────────┘  │
│         │                            │                      │
│  ┌──────▼──────┐        ┌────────────▼──────────┐          │
│  │ MySQL 8     │        │ Demo Orchestrator       │          │
│  │ + Redis     │        │ 决策演示状态机 + 模拟数据  │          │
│  └─────────────┘        └───────┬────────┬───────┘          │
│                                 │        │                  │
│                     ┌───────────▼──┐  ┌──▼──────────────┐   │
│                     │ MockDataGen  │  │ Precomputed     │   │
│                     │ (时序数据)    │  │ (JSON 方案)     │   │
│                     └──────────────┘  └─────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

## 二、项目目录结构

```
13RP-Demo/
├── docker-compose.yml                 # 一键启动（backend + mysql + redis）
├── README.md
│
├── frontend/                          # Vue3 前端项目
│   ├── package.json
│   ├── vite.config.ts
│   ├── index.html
│   └── src/
│       ├── main.ts                    # 入口
│       ├── App.vue                    # 根组件（登录后进入管理端）
│       ├── router/
│       │   └── index.ts              # 路由（管理端路由 + 决策演示 #/{state} hash 路由）
│       ├── stores/
│       │   ├── auth.ts               # 登录态/用户/权限 store
│       │   ├── dashboard.ts          # 4RP 看板数据 store
│       │   ├── logistics.ts          # 物流网络图 store
│       │   └── demoState.ts          # 决策演示状态机 store
│       ├── composables/
│       │   ├── useWebSocket.ts       # WebSocket 连接与重连
│       │   └── useCountUp.ts         # 数字滚动动画
│       ├── components/
│       │   ├── layout/
│       │   │   ├── AdminLayout.vue       # 管理端布局（顶部公司/平台 + 侧边导航 + 内容区）
│       │   │   ├── DemoLayout.vue        # 决策演示三屏布局
│       │   │   └── StatusBar.vue         # 底部状态栏
│       │   ├── common/
│       │   │   ├── SearchBar.vue         # 通用搜索栏（查询/重置/导出）
│       │   │   ├── DataTable.vue         # 通用列表（分页/行操作）
│       │   │   └── ConfirmDialog.vue     # 确认弹窗
│       │   ├── system/                   # 公司架构
│       │   │   ├── InstitutionTree.vue   # 机构管理（树）
│       │   │   ├── PersonList.vue        # 人员管理
│       │   │   └── RoleList.vue          # 角色管理 + 授权
│       │   ├── commodity/                # 商品管理
│       │   │   ├── ChannelList.vue       # 频道管理
│       │   │   ├── CategoryTree.vue      # 品类管理（三级树）
│       │   │   ├── AttributeList.vue     # 属性管理
│       │   │   ├── RuleList.vue          # 规则管理
│       │   │   ├── UnitList.vue          # 单位管理
│       │   │   ├── UnitConversionList.vue # 单位换算
│       │   │   ├── ContractElementList.vue # 合同要素管理
│       │   │   └── ContractElementItemList.vue # 合同要素项管理
│       │   ├── statistics/               # 数据统计
│       │   │   ├── OverviewPage.vue      # 数据总览
│       │   │   ├── UserStatsPage.vue     # 用户统计/明细
│       │   │   ├── DetailStatsPage.vue   # 数据统计明细（多维度）
│       │   │   └── EnterpriseList.vue    # 企业信息录入（生态企业库）
│       │   ├── crm/                      # CRM
│       │   │   ├── LeadList.vue          # 线索管理
│       │   │   ├── CustomerList.vue      # 客户管理
│       │   │   ├── LeadPool.vue          # 线索池
│       │   │   ├── PublicSea.vue         # 客户公海
│       │   │   └── CustomFieldSetting.vue # 自定义字段设置
│       │   ├── operation/                # 运营管理
│       │   │   ├── UserManage.vue        # 客户运营（用户管理）
│       │   │   ├── CertAudit.vue         # 认证管理
│       │   │   ├── SharedData.vue        # 共享数据
│       │   │   └── MultiHead.vue         # 多名头关联
│       │   └── demo/                     # 决策演示
│       │       ├── DashboardPanel.vue    # 4RP 看板
│       │       ├── LogisticsMap.vue      # 物流网络图（ECharts 地图）
│       │       ├── SimulationProgress.vue # 推演进度条
│       │       ├── RadarChart.vue        # 方案雷达图
│       │       ├── PlanCard.vue          # 方案卡片
│       │       ├── GamePanel.vue         # 博弈对抗面板
│       │       └── InstructionList.vue   # 指令列表（合同要素字段）
│       ├── utils/
│       │   ├── constants.ts             # 颜色/尺寸常量
│       │   └── formatters.ts            # 数值格式化
│       └── assets/styles/global.css     # 全局样式
│
├── backend/                           # Spring Boot 后端项目
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│       ├── main/
│       │   ├── java/com/boyu/demo/
│       │   │   ├── BoyuDemoApplication.java      # 启动类
│       │   │   ├── config/
│       │   │   │   ├── SecurityConfig.java       # Spring Security + JWT 过滤器
│       │   │   │   ├── WebSocketConfig.java      # WebSocket 配置
│       │   │   │   └── CorsConfig.java           # CORS 配置
│       │   │   ├── common/                      # 通用
│       │   │   │   ├── Result.java              # 统一返回
│       │   │   │   └── PageQuery.java           # 分页查询基类
│       │   │   ├── module/system/               # 公司架构（机构/人员/角色/菜单）
│       │   │   │   ├── controller/
│       │   │   │   ├── service/
│       │   │   │   ├── entity/
│       │   │   │   └── mapper/
│       │   │   ├── module/commodity/            # 商品管理（频道/品类/属性/规则/单位/换算/合同要素）
│       │   │   │   ├── controller/
│       │   │   │   ├── service/
│       │   │   │   ├── entity/
│       │   │   │   └── mapper/
│       │   │   ├── module/crm/                  # CRM（线索/客户/池/公海/自定义字段）
│       │   │   ├── module/statistics/           # 数据统计（总览/用户/明细/企业库）
│       │   │   ├── module/operation/            # 运营管理（用户/认证/共享/多名头）
│       │   │   ├── orchestrator/
│       │   │   │   ├── DemoStateMachine.java    # 决策演示状态机
│       │   │   │   ├── DemoPhase.java           # 阶段枚举
│       │   │   │   └── TimelineController.java  # 时间轴控制器
│       │   │   ├── websocket/
│       │   │   │   ├── DemoWebSocketHandler.java # WebSocket 主处理器
│       │   │   │   └── WebSocketSessionManager.java
│       │   │   ├── controller/
│       │   │   │   └── DemoController.java      # 决策演示 REST API
│       │   │   ├── service/
│       │   │   │   └── MockDataService.java     # 模拟数据生成（订单/物流/港口/库存）
│       │   │   └── model/
│       │   │       ├── SimulationPath.java      # 推演路径
│       │   │       ├── SolutionPlan.java        # 方案
│       │   │       ├── GameResult.java          # 博弈结果
│       │   │       └── Instruction.java         # 指令（含合同要素字段）
│       │   └── resources/
│       │       ├── application.yml
│       │       └── demo-data/
│       │           ├── paths.json               # 342 条路径（核心 3 条 + 占位）
│       │           ├── solutions.json           # 3 组帕累托方案（含偏好排序）
│       │           ├── game-results.json        # 博弈胜率矩阵（4 因素 × 3 方案）
│       │           └── instructions/
│       │               ├── plan_p1.json         # P1 改港青岛方案 5 条指令（合同要素字段）
│       │               ├── plan_p2.json         # P2 空运方案
│       │               └── plan_p3.json         # P3 备选供应商方案
│       └── test/java/com/boyu/demo/
│           ├── orchestrator/DemoStateMachineTest.java
│           └── service/MockDataServiceTest.java
│
└── docs/
    └── demo-script.md                  # 演示者提词卡
```

## 三、核心模块设计

### 3.1 认证与权限（RBAC，对齐"公司架构"）

**模型**：机构（树）→ 人员（归属机构）→ 角色（绑定人员）→ 菜单/按钮权限（角色授权）

```java
// 核心实体（伪代码）
// sys_institution: id, parent_id, name, sort, deleted
// sys_person:      id, institution_id, avatar, account, name, phone, dept, position, status
// sys_role:        id, name, sort, deleted
// sys_menu:        id, parent_id, name, type(MENU/BUTTON), path, perms
// sys_role_menu:   role_id, menu_id
// sys_person_role: person_id, role_id
```

- 登录：`POST /api/auth/login` → JWT（含 personId + roleIds），Redis 缓存 token 与权限
- 鉴权：Spring Security Filter 解析 JWT → 校验接口权限码（`@PreAuthorize("hasAuthority('system:person:add')")`）
- 前端：登录后拉取菜单树 + 按钮权限集 → 路由守卫 + `v-permission` 指令控制按钮显隐

### 3.2 商品管理（对齐功能清单"商品管理"10 子模块）

**核心关系链**：频道 → 品类（三级树）→ 属性（关联品类）→ 规则（关联品类+属性）→ 单位/单位换算 → 合同要素/要素项

```java
// channel(频道):  id, name, status(待使用/使用中), creator, create_time
// category(品类): id, parent_id, channel_id, name, sort, is_show  // 支持三级
// attribute(属性): id, category_id, name, param_values, attr_values, enabled, sort
//                 // 示例：品牌/产地、规格、材质、牌号（稀土：镝/铒铝/铕）
// rule(规则):     id, category_id, attr_name, attr_value, publish_time
// unit(单位):     id, category_id, name, enabled      // 公斤、吨
// unit_conversion(单位换算): id, unit_id, target_unit_id, ratio, enabled  // 1000 公斤→吨
// contract_element(合同要素): id, name, channel_id, settle_method, freight_bearer,
//                             supplier_note, pay_method, available_qty, creator, status
// contract_element_item(合同要素项): id, element_id, name, editable
//     // 示例项：装货地址/卸货地址/品名/装货时间/卸货时间/结算方式/预算价格/重量/供方数量/报价
```

> **关键点**：合同要素/要素项是 5RP 指令输出的字段来源。指令 JSON 的每个字段直接引用 `contract_element_item` 的名称与顺序。

### 3.3 数据统计（对齐功能清单"数据统计"）

| 页面 | 统计口径 | 实现 |
|------|---------|------|
| 数据总览 | 用户总计/今日新增/新增企业用户/新增个人用户；用户总数趋势图（按月） | 聚合查询 `sys_user`（模拟表）+ ECharts 折线 |
| 用户统计/明细 | ERP 同步三维口径：ERP 未查到总数 / 已找到未主动填写企业信息 / 企业用户与 ERP 信息不同；明细含 ERP 公司名称/所属组织/负责人 | 模拟字段 `erp_sync_status` |
| 数据统计明细 | 品类数（总/一/二/三级）、属性数、规则发布次数、规则完整度（如 85.5%）、企业完整度、供应/需求信息发布数、内容统计（简介/领域/新闻…），累计 vs 昨日 | 跨表聚合 + 定时快照表 `stat_snapshot` |
| 企业信息录入 | 生态企业库（名称/负责人/三级分类/属性/属性值/联系人），搜索/分页 | 模拟表 `enterprise`（≥1000 条 seed 数据） |

### 3.4 CRM + 运营管理（对齐功能清单）

- **线索/客户**：共享一套字段模型（名称/来源/企业类型/手机/电话/邮箱/地址/行业/级别/下次联系/备注/最后跟进），视图按 `owner_id`/`follow_flag`/`converted_flag` 过滤
- **线索池/公海**：规则表（如"N 天未跟进自动回收"），定时任务 `@Scheduled` 扫描回收
- **自定义字段**：`custom_field_def`（module, field_key, field_label, type, options）→ 业务表扩展 JSON 列 `extra_fields`
- **客户运营**：用户表（注册渠道/实名/认证/状态）+ 认证审核（状态机：待认证→通过/拒绝）
- **多名头关联**：集团表 + 关联表（集团名称/关联名头/进度）

### 3.5 决策演示状态机（DemoStateMachine.java）

```java
public enum DemoPhase {
    INIT, EVENT_INJECTED, SIMULATING, SIMULATION_DONE,
    OPTIMIZING, GAMING, PLAN_SELECTED, DONE
}
```

状态流转（国内场景版）：

```
INIT ──台风封港──→ EVENT_INJECTED ──启动推演──→ SIMULATING
                                                    │ 推演完成
PLAN_SELECTED ←──确认方案── GAMING ←──加入博弈── OPTIMIZING ←──启动寻优── SIMULATION_DONE
     │
     └──→ DONE
```

每个状态变化通过 WebSocket 广播到前端，前端根据状态渲染对应面板。

### 3.6 模拟数据生成器（MockDataService.java）

从"三工厂 × 6 指标"改为"订单 / 物流 / 库存 / 港口 四域"：

```java
@Service
public class MockDataService {
    // 订单域：每日订单量、交付率
    private double deliveryRate = 96.0;   // ±2% 高斯噪声
    // 物流域：港口间航线时效（天）
    //   NINGBO->QINGDAO: 2, NINGBO->TIANJIN: 3, SHANGHAI->SUZHOU: 1, ...
    private final Map<String, Integer> routeDays = new HashMap<>();
    // 港口域：5 个港口状态（NORMAL / CLOSED / CONGESTED）
    private final Map<String, PortStatus> ports = new ConcurrentHashMap<>();
    // 库存域：品类库存水位（稀土·镝 等，按属性维度）
    private final Map<String, Integer> inventory = new ConcurrentHashMap<>();

    @Scheduled(fixedRateString = "${demo.data-push-interval-ms:1000}")
    public void generateAndPush() {
        Map<String, Object> tick = new HashMap<>();
        tick.put("orderDeliveryRate", clamp(deliveryRate + noise(2)));
        tick.put("openOrders", (int) (1200 + noise(50)));
        tick.put("ports", ports);          // 港口状态
        tick.put("inventory", inventory);  // 品类库存水位
        webSocketHandler.broadcastDashboard(tick);
    }

    // 注入台风封港：宁波/上海 港口状态置 CLOSED，交付率逐帧下降
    public void injectTyphoon() {
        ports.put("NINGBO", PortStatus.CLOSED);
        ports.put("SHANGHAI", PortStatus.CLOSED);
    }
}
```

### 3.7 WebSocket 通信设计

| channel 字段 | 方向 | 数据内容 | 频率 |
|------|:---:|---------|:---:|
| `dashboard` | 后端→前端 | 订单交付率/港口状态/库存水位 JSON | 每秒 1 次 |
| `twin-event` | 后端→前端 | 物流网络事件（节点变色、路线闪烁） | 按需触发 |
| `demo-state` | 后端→前端 | 状态机状态变更 + 进度百分比 | 状态切换时 |
| `demo-action` | 前端→后端 | 用户操作（注入事件、启动推演等） | 用户操作时 |

> P0 采用原生 WebSocket（`/ws/demo`），消息体 JSON 中 `channel` 字段分发到对应 Pinia store。

### 3.8 REST API 清单

**管理端（通用模式，列出代表接口）**：

| 模块 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 认证 | POST | `/api/auth/login` | 登录（返回 JWT + 菜单 + 权限） |
| 机构 | GET/POST/PUT/DELETE | `/api/system/institution` | 机构树 CRUD（批量删除 `DELETE /batch`） |
| 人员 | GET/POST/PUT/DELETE | `/api/system/person` | 人员 CRUD + 导出 `GET /export` |
| 角色 | GET/POST/PUT/DELETE | `/api/system/role` | 角色 CRUD + `PUT /{id}/menus` 授权 |
| 频道 | GET/POST/PUT/DELETE | `/api/commodity/channel` | 频道 CRUD |
| 品类 | GET/POST/PUT/DELETE | `/api/commodity/category` | 品类 CRUD（树） |
| 属性 | GET/POST/PUT/DELETE | `/api/commodity/attribute` | 属性 CRUD |
| 规则 | GET/POST/PUT/DELETE | `/api/commodity/rule` | 规则 CRUD |
| 单位 | GET/POST/PUT/DELETE | `/api/commodity/unit` | 单位 CRUD |
| 换算 | GET/POST/PUT/DELETE | `/api/commodity/unit-conversion` | 单位换算 CRUD |
| 合同要素 | GET/POST/PUT/DELETE | `/api/commodity/contract-element` | 合同要素 CRUD（含要素项子接口 `/items`） |
| 线索/客户 | GET/POST/PUT/DELETE | `/api/crm/lead` `/api/crm/customer` | CRUD + 视图过滤参数 |
| 线索池/公海 | GET/POST | `/api/crm/lead-pool` `/api/crm/public-sea` | 规则配置 |
| 自定义字段 | GET/POST/PUT | `/api/crm/custom-field` | 按模块维护 |
| 用户管理 | GET/POST/PUT | `/api/operation/user` | 搜索/导出/冻结 |
| 认证审核 | GET/PUT | `/api/operation/cert` | 通过/拒绝 |
| 共享数据 | GET/PUT | `/api/operation/shared-data` | 开关 |
| 多名头 | GET/POST/PUT | `/api/operation/multi-head` | 关联管理 |
| 统计 | GET | `/api/statistics/overview` `/user` `/detail` | 三大统计口径 |
| 企业库 | GET/POST/PUT | `/api/statistics/enterprise` | 企业信息录入 |

**决策演示**：

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/demo/trigger-event` | 注入台风封港（body: `{"eventType":"typhoon_port_closure","duration":5}`） |
| POST | `/api/demo/start-simulation` | 启动 7RP 推演 |
| POST | `/api/demo/start-optimization` | 启动 8RP 寻优（body 可选偏好参数） |
| POST | `/api/demo/start-gaming` | 启动 9RP 博弈（body: 勾选对抗因素列表） |
| POST | `/api/demo/confirm-plan` | 确认最终方案（body: `{"planId":"P1"}`） |
| GET | `/api/demo/state` | 获取当前 Demo 状态 |
| GET | `/api/demo/solutions?preference={type}` | 预计算方案（按偏好排序） |
| GET | `/api/demo/game-results?factors={list}` | 博弈结果 |
| GET | `/api/demo/instructions?planId={id}` | 指令列表（合同要素字段） |

### 3.9 物流网络图（ECharts 地图）

**场景结构**（替代 Three.js 3D 场景）：

```
中国地图（geo，注册 china.json）
├── 港口节点（effectScatter）：宁波舟山港/上海港/青岛港/天津港/广州港
│   └── 状态颜色：正常 #00D4AA / 封港 #FF4757 / 拥堵 #FFA940
├── 仓库节点（scatter）：苏州仓/上海仓/青岛仓
├── 物流路线（lines）：宁波→青岛、宁波→天津、上海→苏州 等
│   └── 动画：lineStyle 渐变动画 + 粒子沿路径移动（effect）
└── 事件响应：twin-event 消息 → 节点颜色切换 + 波纹特效（effectScatter rippleEffect）
```

```typescript
// components/demo/LogisticsMap.vue 核心逻辑（伪代码）
import * as echarts from 'echarts';
import chinaJson from 'china.json';
echarts.registerMap('china', chinaJson);

const option = {
  geo: { map: 'china', roam: true },
  series: [
    {
      type: 'effectScatter',       // 港口节点（带波纹）
      coordinateSystem: 'geo',
      data: ports.map(p => ({ name: p.name, value: [p.lng, p.lat, 1], status: p.status }))
    },
    {
      type: 'lines',               // 物流路线（粒子流动）
      coordinateSystem: 'geo',
      data: routes,
      effect: { show: true, period: 4, trailLength: 0.4, symbol: 'arrow' }
    }
  ]
};

// 响应 WebSocket twin-event
function handleTwinEvent(event: TwinEvent) {
  if (event.type === 'PORT_ALERT') {
    updatePortColor(event.portId, '#FF4757');   // 封港变红
    enableRipple(event.portId);                  // 波纹特效
  }
  if (event.type === 'ROUTE_HIGHLIGHT') {
    highlightRoute(event.routeId);               // 方案路线高亮
  }
}
```

### 3.10 指令输出（对齐合同要素字段）

```json
// instructions/plan_p1.json（改港青岛方案的 5 条指令）
{
  "planId": "P1",
  "instructions": [
    {
      "seq": 1,
      "action": "预订宁波→青岛转港舱位",
      "contractFields": {
        "品名": "稀土·镝", "重量": "1200 吨", "装货地址": "宁波北仑港",
        "卸货地址": "青岛前湾港", "装货时间": "本周内", "结算方式": "预付"
      },
      "owner": "张物流", "deadline": "今天 15:00",
      "docNo": "SO-20260803-001",
      "falsifiableCondition": "若青岛港也封港 → 自动切换天津港"
    }
  ]
}
```

前端渲染：`el-timeline` 逐条弹出，合同要素字段以"要素名：值"标签形式展示，与商品管理"合同要素项"配置一致（字段从 `contract_element_item` 定义读取）。

## 四、数据流设计

### 4.1 决策演示数据流（5 阶段衔接）

```
阶段 1: 事件注入
  点击"台风封港" → POST /api/demo/trigger-event
  → StateMachine: INIT → EVENT_INJECTED
  → MockDataService: ports.NINGBO/SHANGHAI = CLOSED, deliveryRate 逐帧下降
  → WebSocket 持续推送（50 帧动画，5 秒完成）+ 物流图节点变红

阶段 2: 推演（预计算回放）
  点击"启动推演" → POST /api/demo/start-simulation
  → StateMachine: EVENT_INJECTED → SIMULATING
  → Orchestrator 播放 120 秒进度条动画（每 0.5 秒更新 progress%）
  → 路径文本从 paths.json 6 个路径名随机轮换
  → 进度 100% 后，从 paths.json 取核心 3 条路径推送给前端

阶段 3: 多目标寻优（预计算排序）
  点击"启动寻优" → POST /api/demo/start-optimization
  → StateMachine: SIMULATION_DONE → OPTIMIZING
  → 前端取 solutions.json，按用户偏好字段重新排序（纯前端，不请求后端）
  → ECharts 雷达图同步更新

阶段 4: 博弈对抗（预计算矩阵）
  点击"加入博弈层" → POST /api/demo/start-gaming
  → StateMachine: OPTIMIZING → GAMING
  → 前端取 game-results.json，按勾选对抗因素匹配胜率
  → 胜率 countUp 动画 + 卡片震荡动画

阶段 5: 降维输出（模板化填充）
  点击"确认方案 P1" → POST /api/demo/confirm-plan
  → StateMachine: GAMING → PLAN_SELECTED
  → 前端取 instructions.json 中 P1 的 5 条指令模板
  → 合同要素字段渲染 + 当前时间向上取整填充截止时间
  → 指令逐条 typewriter 动画弹出
```

### 4.2 管理端数据流（以"企业信息录入"为例）

```
前端列表页(搜索/分页) → GET /api/statistics/enterprise?keyword=&category=&page=
→ StatisticsService → EnterpriseMapper（MySQL，seed 1000+ 条）
→ Result 分页返回 → DataTable 渲染
→ 导出：GET /api/statistics/enterprise/export → Excel 下载（EasyExcel）
```

---

# 第二部分：开发手册

## 五、环境搭建（W1）

### 5.1 前置依赖

| 依赖 | 版本 | 说明 |
|------|------|------|
| JDK | 21 | 虚拟线程支持 |
| Node.js | ≥ 20 | Vite 6 要求 |
| pnpm | ≥ 9 | 包管理 |
| Docker | ≥ 24 | docker-compose 一键启动 |
| MySQL | 8.0 | Docker 内置 |
| Redis | 7.x | Docker 内置 |

### 5.2 Docker Compose 一键启动

```yaml
# docker-compose.yml
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: rd13demo
      MYSQL_DATABASE: boyu_demo
    ports: ["3306:3306"]
    volumes: [mysql-data:/var/lib/mysql]

  redis:
    image: redis:7-alpine
    ports: ["6379:6379"]

  backend:
    build: ./backend
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/boyu_demo
      SPRING_DATA_REDIS_HOST: redis
    depends_on: [mysql, redis]
    ports: ["8080:8080"]

  frontend:
    build: ./frontend
    ports: ["80:80"]
    depends_on: [backend]

volumes:
  mysql-data:
```

### 5.3 数据库初始化（核心表清单）

```sql
-- 公司架构
CREATE TABLE sys_institution (id BIGINT PRIMARY KEY, parent_id BIGINT, name VARCHAR(100), sort INT, deleted TINYINT DEFAULT 0);
CREATE TABLE sys_person (id BIGINT PRIMARY KEY, institution_id BIGINT, avatar VARCHAR(255), account VARCHAR(50), name VARCHAR(50), phone VARCHAR(20), dept VARCHAR(50), position VARCHAR(50), status TINYINT);
CREATE TABLE sys_role (id BIGINT PRIMARY KEY, name VARCHAR(50), sort INT, deleted TINYINT);
CREATE TABLE sys_menu (id BIGINT PRIMARY KEY, parent_id BIGINT, name VARCHAR(50), type VARCHAR(10), path VARCHAR(100), perms VARCHAR(100));
CREATE TABLE sys_role_menu (role_id BIGINT, menu_id BIGINT);
CREATE TABLE sys_person_role (person_id BIGINT, role_id BIGINT);

-- 商品管理
CREATE TABLE commodity_channel (id BIGINT PRIMARY KEY, name VARCHAR(100), status VARCHAR(20), creator VARCHAR(50), create_time DATETIME);
CREATE TABLE commodity_category (id BIGINT PRIMARY KEY, parent_id BIGINT, channel_id BIGINT, name VARCHAR(100), sort INT, is_show TINYINT);
CREATE TABLE commodity_attribute (id BIGINT PRIMARY KEY, category_id BIGINT, name VARCHAR(50), param_values TEXT, attr_values TEXT, enabled TINYINT, sort INT);
CREATE TABLE commodity_rule (id BIGINT PRIMARY KEY, category_id BIGINT, attr_name VARCHAR(50), attr_value VARCHAR(100), publish_time DATETIME);
CREATE TABLE commodity_unit (id BIGINT PRIMARY KEY, category_id BIGINT, name VARCHAR(20), enabled TINYINT);
CREATE TABLE commodity_unit_conversion (id BIGINT PRIMARY KEY, unit_id BIGINT, target_unit_id BIGINT, ratio DECIMAL(12,4), enabled TINYINT);
CREATE TABLE commodity_contract_element (id BIGINT PRIMARY KEY, name VARCHAR(100), channel_id BIGINT, settle_method VARCHAR(50), freight_bearer VARCHAR(50), supplier_note VARCHAR(255), pay_method VARCHAR(50), available_qty DECIMAL(12,2), creator VARCHAR(50), status VARCHAR(20));
CREATE TABLE commodity_contract_element_item (id BIGINT PRIMARY KEY, element_id BIGINT, name VARCHAR(50), editable TINYINT);

-- CRM
CREATE TABLE crm_lead (id BIGINT PRIMARY KEY, name VARCHAR(100), source VARCHAR(50), company_type VARCHAR(50), phone VARCHAR(20), tel VARCHAR(20), email VARCHAR(100), address VARCHAR(255), industry VARCHAR(50), level VARCHAR(20), next_contact_time DATETIME, remark VARCHAR(500), owner_id BIGINT, follow_flag TINYINT, converted_flag TINYINT, last_follow_time DATETIME, extra_fields JSON);
CREATE TABLE crm_customer (id BIGINT PRIMARY KEY, /* 同 lead 结构 */);
CREATE TABLE crm_pool_rule (id BIGINT PRIMARY KEY, type VARCHAR(20), rule_name VARCHAR(100), recycle_days INT, enabled TINYINT);
CREATE TABLE crm_custom_field_def (id BIGINT PRIMARY KEY, module VARCHAR(20), field_key VARCHAR(50), field_label VARCHAR(50), field_type VARCHAR(20), options VARCHAR(500));

-- 运营管理
CREATE TABLE op_user (id BIGINT PRIMARY KEY, name VARCHAR(50), phone VARCHAR(20), real_name_flag TINYINT, company_name VARCHAR(100), company_cert_flag TINYINT, status VARCHAR(10), register_time DATETIME, user_type VARCHAR(20), channel VARCHAR(20), erp_sync_status VARCHAR(20), erp_company_name VARCHAR(100), org VARCHAR(100), owner VARCHAR(50));
CREATE TABLE op_cert (id BIGINT PRIMARY KEY, user_id BIGINT, company_name VARCHAR(100), status VARCHAR(20), cert_time DATETIME);
CREATE TABLE op_shared_data (id BIGINT PRIMARY KEY, enabled TINYINT);
CREATE TABLE op_multi_head (id BIGINT PRIMARY KEY, group_name VARCHAR(100), company_name VARCHAR(100), creator VARCHAR(50), create_time DATETIME, progress VARCHAR(20));

-- 数据统计
CREATE TABLE stat_enterprise (id BIGINT PRIMARY KEY, name VARCHAR(100), owner VARCHAR(50), category1 VARCHAR(50), category2 VARCHAR(50), category3 VARCHAR(50), attr_name VARCHAR(50), attr_value VARCHAR(100), contact VARCHAR(50), phone VARCHAR(20), create_time DATETIME);
CREATE TABLE stat_snapshot (id BIGINT PRIMARY KEY, snapshot_date DATE, metric_key VARCHAR(50), metric_value DECIMAL(12,2), yesterday_value DECIMAL(12,2));

-- 决策演示
CREATE TABLE demo_event_log (id BIGINT PRIMARY KEY, event_type VARCHAR(50), duration INT, trigger_time DATETIME);  -- 演示审计（可选）
```

> **说明**：以上为 P0 核心表；`crm_customer` 与 `crm_lead` 结构一致可复用；演示用预计算 JSON 存 `resources/demo-data/`，不落库（P1 起落库）。

## 六、关键代码骨架

### 6.1 JWT 认证（Spring Security）

```java
// config/SecurityConfig.java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/login", "/ws/**").permitAll()
                .requestMatchers("/api/demo/**").authenticated()
                .anyRequest().authenticated())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}

// JwtFilter: 解析 Header "Authorization: Bearer xxx" → 校验签名 → 载入权限列表到 SecurityContext
// 权限码示例：system:person:add / commodity:category:edit / crm:lead:export / statistics:overview:view
```

### 6.2 品类树接口

```java
@RestController
@RequestMapping("/api/commodity/category")
public class CategoryController {
    // 返回整棵树（懒加载可优化为按 parentId 查询）
    @GetMapping("/tree")
    public Result<List<CategoryNode>> tree(@RequestParam Long channelId) {
        return Result.ok(categoryService.buildTree(channelId));
    }
}
```

### 6.3 决策演示状态机

```java
// orchestrator/DemoStateMachine.java
@Component
public class DemoStateMachine {
    private volatile DemoPhase phase = DemoPhase.INIT;

    public synchronized void transition(DemoPhase target) {
        if (!allowedTransitions(phase).contains(target)) {
            throw new IllegalStateException("非法状态迁移: " + phase + " → " + target);
        }
        this.phase = target;
        webSocketHandler.broadcastState(phase, progress);
    }

    private List<DemoPhase> allowedTransitions(DemoPhase from) {
        return switch (from) {
            case INIT -> List.of(EVENT_INJECTED);
            case EVENT_INJECTED -> List.of(SIMULATING);
            case SIMULATING -> List.of(SIMULATION_DONE);
            case SIMULATION_DONE -> List.of(OPTIMIZING);
            case OPTIMIZING -> List.of(GAMING);
            case GAMING -> List.of(PLAN_SELECTED);
            case PLAN_SELECTED -> List.of(DONE);
            case DONE -> List.of();
        };
    }
}
```

### 6.4 推演进度动画

```java
// orchestrator/TimelineController.java
public void playSimulation() {
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    scheduler.scheduleAtFixedRate(() -> {
        progress += 0.5;   // 每 0.5 秒 +0.5%，120 秒到 100%
        String msg = pathPool[(int)(Math.random() * pathPool.length)];
        webSocketHandler.broadcastState(DemoPhase.SIMULATING, progress, "已跑完 " + (int)(progress * 3.42) + "/342 条路径 " + msg);
        if (progress >= 100) { scheduler.shutdown(); stateMachine.transition(DemoPhase.SIMULATION_DONE); }
    }, 0, 500, TimeUnit.MILLISECONDS);
}
```

## 七、测试计划

### 7.1 后端单元测试

| 测试类 | 覆盖 |
|--------|------|
| `DemoStateMachineTest` | 状态迁移合法性（合法/非法迁移路径） |
| `MockDataServiceTest` | 台风注入后端口状态、交付率渐变 |
| `CategoryTreeServiceTest` | 三级品类树构建、级联删除校验 |
| `CrmRecycleTaskTest` | 线索池/公海 N 天未跟进回收规则 |
| `PermissionTest` | 角色授权 → 接口权限码校验生效 |

### 7.2 前端验收走查

- 功能复现走查：对照 36 张截图逐屏核对（字段/布局/状态），输出《功能复现走查表》
- 决策演示走查：5 阶段流程 + 快进模式 + F5 恢复

### 7.3 性能验收

| 指标 | 目标 |
|------|:---:|
| 看板刷新延迟 | < 1 秒 |
| 列表页加载（千条级） | < 3 秒 |
| WebSocket 消息延迟 | < 200ms |
| 内存占用 | < 1.5GB（浏览器进程） |
| Docker 总内存 | < 6GB |

---

## 八、部署

### 8.1 阿里云 ECS

```
ECS 4C/16G（按量付费）
├── Docker + docker-compose 部署三容器（mysql/redis/backend/frontend）
├── Nginx（frontend 容器内）反向代理 /api → backend:8080，/ws → backend:8080
├── 域名 demo.13rp.local + HTTPS（LetsEncrypt）
└── 数据卷：mysql-data（演示数据可持久化）
```

### 8.2 本地备份

同 Docker Compose 全套，域名指向 `localhost`；断网/云端故障时一键切换。

### 8.3 演示审计与回滚

- `demo_event_log` 记录每次演示事件注入（时间/类型）—— 便于复盘
- 数据库每日快照：`mysqldump boyu_demo > backup_$(date).sql`
- 预计算 JSON 版本化：demo-data 目录随 git 版本管理，可随时回滚到某个分镜版本

---

## 附录：决策演示预计算 JSON 字段规范

| 文件 | 关键字段 |
|------|---------|
| `paths.json` | total_paths: 342, simulated: 342, perturbations: 1000, top_paths[]: {id, name, cost, carbon, delivery, risk_score} |
| `solutions.json` | plans[]: {id, name, metrics{cost,carbon,delivery,risk}, preferenceRank{delivery_first, balanced, cost_first}, execSteps[]} |
| `game-results.json` | factors[]: {id, name, checked}; results: {planId, winRateBefore, winRateAfter} |
| `instructions/plan_pX.json` | instructions[]: {seq, action, contractFields{品名,重量,装货地址,卸货地址,装货时间,卸货时间,结算方式}, owner, deadline, docNo, falsifiableCondition} |
