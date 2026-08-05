import request from './request'
import type { PageResult } from './request'

/** 库存统计（库龄 ≥ ageWarnDays 红色预警） */
export interface InventoryStock {
  id?: number
  productName?: string
  grade?: string
  spec?: string
  orgId?: number
  orgName?: string
  actualQty?: number
  transitQty?: number
  stockAge?: number
  /** 库龄预警阈值（天） */
  ageWarnDays?: number
}

/** 安全库存设计（有货率/Z值/补货周期/经济量/订货点/最大/安全） */
export interface InventorySafeStock {
  id?: number
  productName?: string
  material?: string
  orgId?: number
  orgName?: string
  /** 有货率（%） */
  serviceLevel?: number
  /** Z 值 */
  zValue?: number
  /** 补货周期（天） */
  replenishCycle?: number
  /** 经济补货量 */
  economicQty?: number
  /** 订货点量 */
  orderPointQty?: number
  /** 最大库存 */
  maxQty?: number
  /** 安全库存 */
  safeStock?: number
}

/** 入库管理（CREATED 制单完成 → APPROVED 批准 → CHECKED 保管员审核） */
export interface InventoryInbound {
  id?: number
  inboundNo: string
  /** 估价/代销/内部 */
  inboundType?: string
  sourceOrderNo?: string
  productName?: string
  qty?: number
  /** 账面结算数量 */
  settleQty?: number
  status: string
  checker?: string
  /** 分级审核：≤合理称差直接审核 / >合理称差总监/经理审核 */
  auditLevel?: string
}

/** 出库/发货（CREATED → APPROVED；运费承担方分流） */
export interface InventoryOutbound {
  id?: number
  outboundNo: string
  saleOrderNo?: string
  productName?: string
  qty?: number
  /** 运费承担方：博宇承担/对方承担 */
  freightBearer?: string
  carrier?: string
  plateNo?: string
  driver?: string
  driverPhone?: string
  status: string
}

/** 调拨（库位转移） */
export interface InventoryTransfer {
  id?: number
  transferNo: string
  batchNo?: string
  /** 实提数量 */
  qty?: number
  targetLocation?: string
  status: string
}

/** 盘点（实盘） */
export interface InventoryCheck {
  id?: number
  checkNo: string
  batchNo?: string
  actualQty?: number
  status: string
}

/** 批号管理 */
export interface InventoryBatch {
  id?: number
  batchNo: string
  productName?: string
  createDate?: string
  creator?: string
  remark?: string
}

export interface PageQuery {
  page?: number
  size?: number
  keyword?: string
  [key: string]: unknown
}

export const inventoryStockApi = {
  list: (params?: PageQuery) =>
    request.get<PageResult<InventoryStock>, PageResult<InventoryStock>>(
      '/api/inventory/stock',
      { params },
    ),
  create: (data: InventoryStock) =>
    request.post<InventoryStock, InventoryStock>('/api/inventory/stock', data),
  update: (id: number, data: Partial<InventoryStock>) =>
    request.put<InventoryStock, InventoryStock>(`/api/inventory/stock/${id}`, data),
  remove: (id: number) =>
    request.delete<never, void>(`/api/inventory/stock/${id}`),
}

export const inventorySafeStockApi = {
  list: (params?: PageQuery) =>
    request.get<PageResult<InventorySafeStock>, PageResult<InventorySafeStock>>(
      '/api/inventory/safe-stock',
      { params },
    ),
  create: (data: InventorySafeStock) =>
    request.post<InventorySafeStock, InventorySafeStock>('/api/inventory/safe-stock', data),
  update: (id: number, data: Partial<InventorySafeStock>) =>
    request.put<InventorySafeStock, InventorySafeStock>(`/api/inventory/safe-stock/${id}`, data),
  remove: (id: number) =>
    request.delete<never, void>(`/api/inventory/safe-stock/${id}`),
}

export const inventoryInboundApi = {
  list: (params?: PageQuery) =>
    request.get<PageResult<InventoryInbound>, PageResult<InventoryInbound>>(
      '/api/inventory/inbound',
      { params },
    ),
  create: (data: InventoryInbound) =>
    request.post<InventoryInbound, InventoryInbound>('/api/inventory/inbound', data),
  update: (id: number, data: Partial<InventoryInbound>) =>
    request.put<InventoryInbound, InventoryInbound>(`/api/inventory/inbound/${id}`, data),
  remove: (id: number) =>
    request.delete<never, void>(`/api/inventory/inbound/${id}`),
}

export const inventoryOutboundApi = {
  list: (params?: PageQuery) =>
    request.get<PageResult<InventoryOutbound>, PageResult<InventoryOutbound>>(
      '/api/inventory/outbound',
      { params },
    ),
  create: (data: InventoryOutbound) =>
    request.post<InventoryOutbound, InventoryOutbound>('/api/inventory/outbound', data),
  update: (id: number, data: Partial<InventoryOutbound>) =>
    request.put<InventoryOutbound, InventoryOutbound>(`/api/inventory/outbound/${id}`, data),
  remove: (id: number) =>
    request.delete<never, void>(`/api/inventory/outbound/${id}`),
}

export const inventoryTransferApi = {
  list: (params?: PageQuery) =>
    request.get<PageResult<InventoryTransfer>, PageResult<InventoryTransfer>>(
      '/api/inventory/transfer',
      { params },
    ),
  create: (data: InventoryTransfer) =>
    request.post<InventoryTransfer, InventoryTransfer>('/api/inventory/transfer', data),
  update: (id: number, data: Partial<InventoryTransfer>) =>
    request.put<InventoryTransfer, InventoryTransfer>(`/api/inventory/transfer/${id}`, data),
  remove: (id: number) =>
    request.delete<never, void>(`/api/inventory/transfer/${id}`),
}

export const inventoryCheckApi = {
  list: (params?: PageQuery) =>
    request.get<PageResult<InventoryCheck>, PageResult<InventoryCheck>>(
      '/api/inventory/check',
      { params },
    ),
  create: (data: InventoryCheck) =>
    request.post<InventoryCheck, InventoryCheck>('/api/inventory/check', data),
  update: (id: number, data: Partial<InventoryCheck>) =>
    request.put<InventoryCheck, InventoryCheck>(`/api/inventory/check/${id}`, data),
  remove: (id: number) =>
    request.delete<never, void>(`/api/inventory/check/${id}`),
}

export const inventoryBatchApi = {
  list: (params?: PageQuery) =>
    request.get<PageResult<InventoryBatch>, PageResult<InventoryBatch>>(
      '/api/inventory/batch',
      { params },
    ),
  create: (data: InventoryBatch) =>
    request.post<InventoryBatch, InventoryBatch>('/api/inventory/batch', data),
  update: (id: number, data: Partial<InventoryBatch>) =>
    request.put<InventoryBatch, InventoryBatch>(`/api/inventory/batch/${id}`, data),
  remove: (id: number) =>
    request.delete<never, void>(`/api/inventory/batch/${id}`),
}
