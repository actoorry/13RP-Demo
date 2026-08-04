// 13RP 决策演示共享常量：港口/节点坐标、物流路线、阶段标签、偏好选项
export type PortStatus = 'NORMAL' | 'CLOSED' | 'CONGESTED'

export type PortKind = 'port' | 'warehouse' | 'base'

export interface PortDef {
  code: string
  name: string
  lng: number
  lat: number
  kind: PortKind
}

export const PORT_DEFS: PortDef[] = [
  { code: 'NINGBO', name: '宁波舟山港', lng: 121.87, lat: 29.97, kind: 'port' },
  { code: 'SHANGHAI', name: '上海港', lng: 121.48, lat: 31.23, kind: 'port' },
  { code: 'QINGDAO', name: '青岛港', lng: 120.32, lat: 36.07, kind: 'port' },
  { code: 'TIANJIN', name: '天津港', lng: 117.72, lat: 38.98, kind: 'port' },
  { code: 'GUANGZHOU', name: '广州港', lng: 113.47, lat: 23.1, kind: 'port' },
  { code: 'SUZHOU', name: '苏州仓', lng: 120.58, lat: 31.3, kind: 'warehouse' },
  { code: 'GANZHOU', name: '赣州基地', lng: 114.93, lat: 25.83, kind: 'base' },
]

export const PORT_NAMES: Record<string, string> = {
  NINGBO: '宁波舟山港',
  SHANGHAI: '上海港',
  QINGDAO: '青岛港',
  TIANJIN: '天津港',
  GUANGZHOU: '广州港',
  SUZHOU: '苏州仓',
  GANZHOU: '赣州基地',
}

export const STATUS_COLOR: Record<PortStatus, string> = {
  NORMAL: '#00d4aa',
  CLOSED: '#ff4757',
  CONGESTED: '#ffa940',
}

export const STATUS_LABEL: Record<PortStatus, string> = {
  NORMAL: '正常',
  CLOSED: '封港',
  CONGESTED: '拥堵',
}

export interface RouteDef {
  id: string
  from: string
  to: string
  label: string
  alternative?: boolean
}

export const ROUTES: RouteDef[] = [
  { id: 'nb-qd', from: 'NINGBO', to: 'QINGDAO', label: '宁波→青岛', alternative: true },
  { id: 'nb-tj', from: 'NINGBO', to: 'TIANJIN', label: '宁波→天津', alternative: true },
  { id: 'sh-sz', from: 'SHANGHAI', to: 'SUZHOU', label: '上海→苏州' },
  { id: 'nb-sh', from: 'NINGBO', to: 'SHANGHAI', label: '宁波→上海' },
  { id: 'qd-tj', from: 'QINGDAO', to: 'TIANJIN', label: '青岛→天津' },
  { id: 'gz-nb', from: 'GUANGZHOU', to: 'NINGBO', label: '广州→宁波' },
  { id: 'gz-gz', from: 'GANZHOU', to: 'GUANGZHOU', label: '赣州→广州', alternative: true },
]

export const PHASE_LABELS: Record<string, string> = {
  INIT: '初始就绪',
  EVENT_INJECTED: '事件已注入',
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
