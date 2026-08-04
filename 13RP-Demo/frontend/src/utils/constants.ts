// 港口/仓库节点（经纬度 + 中文名）
export const NODES = [
  { id: 'NINGBO', name: '宁波舟山港', type: 'port', lng: 121.87, lat: 29.97 },
  { id: 'SHANGHAI', name: '上海港', type: 'port', lng: 121.48, lat: 31.23 },
  { id: 'QINGDAO', name: '青岛港', type: 'port', lng: 120.32, lat: 36.07 },
  { id: 'TIANJIN', name: '天津港', type: 'port', lng: 117.72, lat: 38.98 },
  { id: 'GUANGZHOU', name: '广州港', type: 'port', lng: 113.47, lat: 23.10 },
  { id: 'SUZHOU_WAREHOUSE', name: '苏州仓', type: 'warehouse', lng: 120.58, lat: 31.30 },
  { id: 'GANZHOU_BASE', name: '赣州基地', type: 'supplier', lng: 114.93, lat: 25.83 }
];

// 物流路线（start, end, name, days, mode）
export const ROUTES = [
  { start: 'NINGBO', end: 'QINGDAO', name: '海运 · 宁波→青岛', days: 2, mode: 'sea' },
  { start: 'NINGBO', end: 'TIANJIN', name: '海运 · 宁波→天津', days: 3, mode: 'sea' },
  { start: 'SHANGHAI', end: 'SUZHOU_WAREHOUSE', name: '公路 · 上海→苏州', days: 1, mode: 'road' },
  { start: 'QINGDAO', end: 'SUZHOU_WAREHOUSE', name: '铁路 · 青岛→苏州', days: 2, mode: 'rail' },
  { start: 'GANZHOU_BASE', end: 'SUZHOU_WAREHOUSE', name: '公路 · 赣州→苏州', days: 3, mode: 'road' },
  { start: 'SHANGHAI', end: 'QINGDAO', name: '空运 · 上海→青岛', days: 1, mode: 'air' }
];

export const PORT_STATUS_COLOR: Record<string, string> = {
  NORMAL: '#00d4aa',
  CLOSED: '#ff4757',
  CONGESTED: '#ffa940'
};

export const PHASE_LABEL: Record<string, string> = {
  INIT: '系统待命',
  EVENT_INJECTED: '事件已注入 · 报警中',
  SIMULATING: '推演中',
  SIMULATION_DONE: '推演完成',
  OPTIMIZING: '多目标寻优',
  GAMING: '博弈对抗',
  PLAN_SELECTED: '方案确认 · 指令输出',
  DONE: '演示结束'
};
