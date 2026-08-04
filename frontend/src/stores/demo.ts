import { defineStore } from 'pinia'
import { ElMessage } from 'element-plus'
import { SUPPLY_NODES, NODE_NAMES } from '../constants/demo'

export type DemoPhase =
  | 'INIT'
  | 'EVENT_INJECTED'
  | 'SIMULATING'
  | 'SIMULATION_DONE'
  | 'OPTIMIZING'
  | 'GAMING'
  | 'PLAN_SELECTED'
  | 'DONE'

export interface SupplierInfo {
  code: string
  name: string
  status: string
  lng: number
  lat: number
  kind: 'supplier' | 'factory' | 'base'
}

export interface SolutionPlan {
  id: string
  name: string
  metrics: { cost: number; carbon: number; delivery: number; risk: number }
  execSteps?: string[]
  preferenceRank?: Record<string, string[]>
}

export interface GameFactor {
  id: string
  name: string
  /** 后端 game-results.json 字段名是 defaultChecked，控制默认勾选 */
  defaultChecked?: boolean
}

export interface GameResultRow {
  planId: string
  winRateBefore: number
  winRateAfter: number
}

export interface GameResults {
  factors: GameFactor[]
  results: GameResultRow[]
}

export interface Instruction {
  seq: number
  action: string
  contractFields: Record<string, string>
  owner: string
  deadline: string
  docNo: string
  falsifiableCondition?: string
}

/** 品类库存条目：数量 + 单位（对齐后端 [{category, qty, unit}]） */
export interface InventoryEntry {
  qty: number
  unit: string
}

export interface WsMessage {
  channel?: string
  phase?: string
  progress?: number
  message?: string
  msg?: string
  progressMsg?: string
  orderDeliveryRate?: number
  deliveryRate?: number
  openOrders?: number
  /** 后端看板推送字段名为 ports（节点含 id/name/status/kind）；状态变量仍命名为 suppliers */
  ports?: unknown
  inventory?: unknown
  planId?: unknown
  planName?: unknown
  type?: string
  supplierId?: unknown
  supplier?: unknown
  portId?: unknown
  port?: unknown
  action?: string
  ok?: boolean
  error?: string
  payload?: unknown
}

function defaultSuppliers(): SupplierInfo[] {
  return SUPPLY_NODES.map((p) => ({ ...p, status: 'NORMAL' }))
}

export const useDemoStore = defineStore('demo', {
  state: () => ({
    phase: 'INIT' as DemoPhase,
    progress: 0,
    progressMsg: '',
    deliveryRate: 96.5,
    openOrders: 1200,
    suppliers: defaultSuppliers() as SupplierInfo[],
    supplierNames: { ...NODE_NAMES } as Record<string, string>,
    inventory: {} as Record<string, InventoryEntry>,
    solutions: [] as SolutionPlan[],
    gameResults: null as GameResults | null,
    instructions: [] as Instruction[],
    planId: '',
    planName: '',
  }),
  actions: {
    /** 按 channel 分发 WebSocket 消息 */
    applyWsMessage(msg: WsMessage) {
      if (!msg) return
      const channel = msg.channel ?? (typeof msg.phase === 'string' ? 'demo-state' : '')
      if (channel === 'dashboard') this.applyDashboard(msg)
      else if (channel === 'demo-state') this.applyDemoState(msg)
      else if (channel === 'twin-event') this.applyTwinEvent(msg)
      else if (channel === 'demo-action') this.applyDemoAction(msg)
    },

    /** demo-action：动作回执确认。ok=false 时警告并提示用户 */
    applyDemoAction(msg: WsMessage) {
      // 兼容平铺 {ok,error} 与嵌套 {payload:{ok,error}} 两种后端形状
      const nested =
        msg.payload && typeof msg.payload === 'object'
          ? (msg.payload as Record<string, unknown>)
          : null
      const ok = msg.ok ?? (nested ? nested.ok : undefined)
      const error = msg.error ?? (nested ? nested.error : undefined)
      if (ok === false) {
        const detail = typeof error === 'string' && error ? error : '未知错误'
        const label = typeof msg.action === 'string' && msg.action ? `[${msg.action}] ` : ''
        console.warn(`[demo-action] 操作失败${label}`, detail)
        ElMessage.warning(`操作失败：${detail}`)
      }
    },

    /** dashboard：订单/供应商/库存实时看板 */
    applyDashboard(msg: WsMessage) {
      const rate = msg.orderDeliveryRate ?? msg.deliveryRate
      if (typeof rate === 'number' && Number.isFinite(rate)) {
        this.deliveryRate = Math.max(0, Math.min(100, rate))
      }
      if (typeof msg.openOrders === 'number' && Number.isFinite(msg.openOrders)) {
        this.openOrders = msg.openOrders
      }
      if (msg.ports) this.applySuppliers(msg.ports)
      if (msg.inventory) this.applyInventory(msg.inventory)
    },

    /** 归一化供应商状态：兼容 数组 或  code->status 映射 两种后端形状；数组节点结构为 {id/name/status/kind} */
    applySuppliers(raw: unknown) {
      const byCode = new Map(this.suppliers.map((p) => [p.code, p]))
      if (Array.isArray(raw)) {
        for (const item of raw) {
          if (!item || typeof item !== 'object') continue
          const obj = item as Record<string, unknown>
          const code = String(obj.code ?? obj.id ?? '').toUpperCase()
          const found = byCode.get(code)
          if (!found) continue
          found.status = String(obj.status ?? 'NORMAL').toUpperCase()
          if (typeof obj.name === 'string' && obj.name) found.name = obj.name
          if (obj.kind === 'supplier' || obj.kind === 'factory' || obj.kind === 'base') {
            found.kind = obj.kind
          }
        }
      } else if (raw && typeof raw === 'object') {
        const map = raw as Record<string, unknown>
        for (const [code, status] of Object.entries(map)) {
          const found = byCode.get(code.toUpperCase())
          if (found) found.status = String(status).toUpperCase()
        }
      }
    },

    /** 归一化库存：兼容后端 数组 [{category,qty,unit}] 与 映射 {category:qty} 两种形状 */
    applyInventory(raw: unknown) {
      if (!raw || typeof raw !== 'object') return
      if (Array.isArray(raw)) {
        for (const item of raw) {
          if (!item || typeof item !== 'object') continue
          const obj = item as Record<string, unknown>
          const name = obj.category ?? obj.name
          const qty = obj.qty ?? obj.value
          if (name == null || typeof qty !== 'number' || !Number.isFinite(qty)) continue
          const unit = typeof obj.unit === 'string' ? obj.unit : ''
          this.inventory[String(name)] = { qty, unit }
        }
      } else {
        const map = raw as Record<string, unknown>
        for (const [name, qty] of Object.entries(map)) {
          if (typeof qty === 'number' && Number.isFinite(qty)) {
            this.inventory[name] = { qty, unit: '' }
          }
        }
      }
    },

    /** demo-state：状态机 phase / 进度 / 进度消息 */
    applyDemoState(msg: WsMessage) {
      if (typeof msg.phase === 'string') this.phase = msg.phase as DemoPhase
      if (typeof msg.progress === 'number' && Number.isFinite(msg.progress)) {
        this.progress = Math.max(0, Math.min(100, msg.progress))
      }
      const m = msg.message ?? msg.msg ?? msg.progressMsg
      if (typeof m === 'string') this.progressMsg = m
      if (msg.planId) this.planId = String(msg.planId)
      if (msg.planName) this.planName = String(msg.planName)
      if (this.phase === 'INIT') this.resetData()
    },

    /** twin-event：SUPPLIER_ALERT 直接置缺货；ROUTE_HIGHLIGHT 由地图依据 suppliers 自行高亮 */
    applyTwinEvent(msg: WsMessage) {
      // 兼容新旧事件类型：后端若仍沿用 PORT_ALERT 也能命中
      if (msg.type === 'SUPPLIER_ALERT' || msg.type === 'PORT_ALERT') {
        const code = String(
          msg.supplierId ?? msg.portId ?? msg.supplier ?? msg.port ?? '',
        ).toUpperCase()
        const found = this.suppliers.find((p) => p.code === code)
        if (found) found.status = 'SHORTAGE'
      }
    },

    resetData() {
      this.progress = 0
      this.progressMsg = ''
      this.solutions = []
      this.gameResults = null
      this.instructions = []
      this.planId = ''
      this.planName = ''
    },

    async fetchState() {
      try {
        const res = await fetch('/api/demo/state')
        if (!res.ok) return
        const data: WsMessage = await res.json()
        this.applyWsMessage({ channel: 'demo-state', ...data })
      } catch (err) {
        console.error('[demo] 拉取 state 失败', err)
      }
    },

    async fetchSolutions(preference = 'balanced') {
      try {
        const res = await fetch(`/api/demo/solutions?preference=${encodeURIComponent(preference)}`)
        if (!res.ok) return
        const data = await res.json()
        const plans = Array.isArray(data) ? data : (data.plans ?? [])
        this.solutions = plans as SolutionPlan[]
      } catch (err) {
        console.error('[demo] 拉取 solutions 失败', err)
      }
    },

    async fetchGameResults(factors?: string[]) {
      try {
        const qs = factors && factors.length ? `?factors=${factors.slice().sort().join('+')}` : ''
        const res = await fetch(`/api/demo/game-results${qs}`)
        if (!res.ok) return
        const data = await res.json()
        this.gameResults = data as GameResults
      } catch (err) {
        console.error('[demo] 拉取 game-results 失败', err)
      }
    },

    async fetchInstructions(planId: string) {
      if (!planId) return
      try {
        this.planId = planId
        const res = await fetch(`/api/demo/instructions?planId=${encodeURIComponent(planId)}`)
        if (!res.ok) return
        const data = await res.json()
        const list = Array.isArray(data) ? data : (data.instructions ?? [])
        this.instructions = list as Instruction[]
        const plan = this.solutions.find((s) => s.id === planId)
        if (plan) this.planName = plan.name
      } catch (err) {
        console.error('[demo] 拉取 instructions 失败', err)
      }
    },
  },
})
