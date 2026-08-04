import { defineStore } from 'pinia'
import { PORT_DEFS, PORT_NAMES } from '../constants/demo'

export type DemoPhase =
  | 'INIT'
  | 'EVENT_INJECTED'
  | 'SIMULATING'
  | 'SIMULATION_DONE'
  | 'OPTIMIZING'
  | 'GAMING'
  | 'PLAN_SELECTED'
  | 'DONE'

export interface PortInfo {
  code: string
  name: string
  status: string
  lng: number
  lat: number
  kind: 'port' | 'warehouse' | 'base'
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
  checked?: boolean
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
  ports?: unknown
  inventory?: unknown
  planId?: unknown
  planName?: unknown
  type?: string
  portId?: unknown
  port?: unknown
}

function defaultPorts(): PortInfo[] {
  return PORT_DEFS.map((p) => ({ ...p, status: 'NORMAL' }))
}

export const useDemoStore = defineStore('demo', {
  state: () => ({
    phase: 'INIT' as DemoPhase,
    progress: 0,
    progressMsg: '',
    deliveryRate: 96.5,
    openOrders: 1200,
    ports: defaultPorts() as PortInfo[],
    portNames: { ...PORT_NAMES } as Record<string, string>,
    inventory: {} as Record<string, number>,
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
    },

    /** dashboard：订单/港口/库存实时看板 */
    applyDashboard(msg: WsMessage) {
      const rate = msg.orderDeliveryRate ?? msg.deliveryRate
      if (typeof rate === 'number' && Number.isFinite(rate)) {
        this.deliveryRate = Math.max(0, Math.min(100, rate))
      }
      if (typeof msg.openOrders === 'number' && Number.isFinite(msg.openOrders)) {
        this.openOrders = msg.openOrders
      }
      if (msg.ports) this.applyPorts(msg.ports)
      if (msg.inventory) this.applyInventory(msg.inventory)
    },

    /** 归一化港口状态：兼容 数组 或  code->status 映射 两种后端形状 */
    applyPorts(raw: unknown) {
      const byCode = new Map(this.ports.map((p) => [p.code, p]))
      if (Array.isArray(raw)) {
        for (const item of raw) {
          if (!item || typeof item !== 'object') continue
          const obj = item as Record<string, unknown>
          const code = String(obj.code ?? obj.id ?? '').toUpperCase()
          const found = byCode.get(code)
          if (found) found.status = String(obj.status ?? 'NORMAL').toUpperCase()
        }
      } else if (raw && typeof raw === 'object') {
        const map = raw as Record<string, unknown>
        for (const [code, status] of Object.entries(map)) {
          const found = byCode.get(code.toUpperCase())
          if (found) found.status = String(status).toUpperCase()
        }
      }
    },

    applyInventory(raw: unknown) {
      if (!raw || typeof raw !== 'object') return
      this.inventory = { ...this.inventory, ...(raw as Record<string, number>) }
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

    /** twin-event：PORT_ALERT 直接置红；ROUTE_HIGHLIGHT 由地图依据 ports 自行高亮 */
    applyTwinEvent(msg: WsMessage) {
      if (msg.type === 'PORT_ALERT') {
        const code = String(msg.portId ?? msg.port ?? '').toUpperCase()
        const found = this.ports.find((p) => p.code === code)
        if (found) found.status = 'CLOSED'
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
