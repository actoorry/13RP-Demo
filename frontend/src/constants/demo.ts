// 13RP 决策演示共享常量：供应网络节点、供应路线、阶段标签、偏好选项
export type SupplierStatus = 'NORMAL' | 'SHORTAGE' | 'TIGHT'

export type SupplyNodeKind = 'supplier' | 'factory' | 'base'

export interface SupplyNode {
  code: string
  name: string
  lng: number
  lat: number
  kind: SupplyNodeKind
}

export const SUPPLY_NODES: SupplyNode[] = [
  { code: 'BAOTOU', name: '北方铜业', lng: 109.84, lat: 40.66, kind: 'supplier' },
  { code: 'GANZHOU', name: '中原铜业', lng: 114.93, lat: 25.83, kind: 'supplier' },
  { code: 'NINGBO', name: '南方铜业', lng: 121.55, lat: 29.88, kind: 'supplier' },
  { code: 'SUZHOU', name: '苏州应用工厂', lng: 120.58, lat: 31.3, kind: 'factory' },
  { code: 'GUANGZHOU', name: '广州深加工基地', lng: 113.47, lat: 23.1, kind: 'base' },
]

export const NODE_NAMES: Record<string, string> = {
  BAOTOU: '北方铜业',
  GANZHOU: '中原铜业',
  NINGBO: '南方铜业',
  SUZHOU: '苏州应用工厂',
  GUANGZHOU: '广州深加工基地',
}

export const STATUS_COLOR: Record<SupplierStatus, string> = {
  NORMAL: '#00d4aa',
  SHORTAGE: '#ff4757',
  TIGHT: '#ffa940',
}

export const STATUS_LABEL: Record<SupplierStatus, string> = {
  NORMAL: '正常',
  SHORTAGE: '缺货',
  TIGHT: '紧张',
}

export interface RouteDef {
  id: string
  from: string
  to: string
  label: string
  alternative?: boolean
}

export const ROUTES: RouteDef[] = [
  { id: 'bt-sz', from: 'BAOTOU', to: 'SUZHOU', label: '北方铜业→苏州' },
  { id: 'gz-sz', from: 'GANZHOU', to: 'SUZHOU', label: '中原铜业→苏州', alternative: true },
  { id: 'nb-sz', from: 'NINGBO', to: 'SUZHOU', label: '南方铜业→苏州', alternative: true },
  { id: 'sz-gz', from: 'SUZHOU', to: 'GUANGZHOU', label: '苏州→广州' },
  { id: 'gz-gz', from: 'GANZHOU', to: 'GUANGZHOU', label: '中原铜业→广州', alternative: true },
]

export const PHASE_LABELS: Record<string, string> = {
  INIT: '初始就绪',
  EVENT_INJECTED: '模拟宇宙已创建',
  SIMULATING: '推演中',
  SIMULATION_DONE: '推演完成',
  OPTIMIZING: '多目标寻优',
  GAMING: '博弈对抗',
  PLAN_SELECTED: '方案已确认',
  DONE: '演示完成',
}

export const PREFERENCE_OPTIONS = [
  { value: 'delivery_first', label: '优先保交付' },
  { value: 'balanced', label: '平衡成本与交付' },
  { value: 'cost_first', label: '成本优先' },
]

export const PLAN_COLORS = ['#00d4aa', '#4a90d9', '#ffa940']
