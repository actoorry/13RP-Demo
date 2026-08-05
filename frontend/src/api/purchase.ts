import request from './request'
import type { PageResult } from './request'

/** 供应商分级（战略/优选/考察/一般） */
export interface SupplierGrade {
  id?: number
  supplierId?: number
  supplierName?: string
  grade: string
  contact?: string
  phone?: string
}

/** 预测预案（年规划/月计划/周优化/日执行） */
export interface ForecastPlan {
  id?: number
  planType: string
  planName: string
  periodStart?: string
  periodEnd?: string
  creator?: string
}

/** 询价管理（急询价/指定询价，可标"急"） */
export interface Inquiry {
  id?: number
  inquiryNo: string
  inquiryType: string
  productName: string
  supplierId?: number
  supplierName?: string
  status: string
  urgentFlag?: number
  replyTime?: string
  creator?: string
}

/** 采购申请审批链（批准→客服部复核） */
export interface PurchaseApply {
  id?: number
  applyNo: string
  inquiryId?: number
  inquiryNo?: string
  status: string
  approver?: string
  approveTime?: string
}

/** 待审批订单 + 结算分流（现款后货→待付款 / 先货后款→待入库）；CLOSED 已关闭为终态 */
export interface PurchaseOrder {
  id?: number
  orderNo: string
  source?: string
  settleMethod: string
  supplierId?: number
  supplierName?: string
  payAmount?: number
  /** 待审批 PENDING_APPROVE / 已审批 APPROVED / 已关闭 CLOSED */
  status: string
  /** 结算分流：WAIT_PAY 待付款 / WAIT_INBOUND 待入库 */
  settlementStatus?: string
  creator?: string
}

/** 进项欠票（一入库单一欠票） */
export interface PurchaseDebt {
  id?: number
  inboundId?: number
  inboundNo?: string
  invoiceId?: number
  invoiceNo?: string
  amount?: number
  status: string
}

/** 应付列表 */
export interface PurchasePayable {
  id?: number
  supplierId?: number
  supplierName?: string
  balance?: number
  dueDate?: string
  status: string
}

export interface PageQuery {
  page?: number
  size?: number
  keyword?: string
  [key: string]: unknown
}

export const supplierGradeApi = {
  list: (params?: PageQuery) =>
    request.get<PageResult<SupplierGrade>, PageResult<SupplierGrade>>(
      '/api/purchase/supplier-grade',
      { params },
    ),
  create: (data: SupplierGrade) =>
    request.post<SupplierGrade, SupplierGrade>('/api/purchase/supplier-grade', data),
  update: (id: number, data: SupplierGrade) =>
    request.put<SupplierGrade, SupplierGrade>(`/api/purchase/supplier-grade/${id}`, data),
  remove: (id: number) =>
    request.delete<never, void>(`/api/purchase/supplier-grade/${id}`),
}

export const forecastApi = {
  list: (params?: PageQuery) =>
    request.get<PageResult<ForecastPlan>, PageResult<ForecastPlan>>(
      '/api/purchase/forecast',
      { params },
    ),
  create: (data: ForecastPlan) =>
    request.post<ForecastPlan, ForecastPlan>('/api/purchase/forecast', data),
  update: (id: number, data: ForecastPlan) =>
    request.put<ForecastPlan, ForecastPlan>(`/api/purchase/forecast/${id}`, data),
  remove: (id: number) =>
    request.delete<never, void>(`/api/purchase/forecast/${id}`),
}

export const inquiryApi = {
  list: (params?: PageQuery) =>
    request.get<PageResult<Inquiry>, PageResult<Inquiry>>('/api/purchase/inquiry', {
      params,
    }),
  create: (data: Inquiry) =>
    request.post<Inquiry, Inquiry>('/api/purchase/inquiry', data),
  update: (id: number, data: Inquiry) =>
    request.put<Inquiry, Inquiry>(`/api/purchase/inquiry/${id}`, data),
}

export const applyApi = {
  list: (params?: PageQuery) =>
    request.get<PageResult<PurchaseApply>, PageResult<PurchaseApply>>(
      '/api/purchase/apply',
      { params },
    ),
  create: (data: PurchaseApply) =>
    request.post<PurchaseApply, PurchaseApply>('/api/purchase/apply', data),
  update: (id: number, data: PurchaseApply) =>
    request.put<PurchaseApply, PurchaseApply>(`/api/purchase/apply/${id}`, data),
}

export const orderApi = {
  list: (params?: PageQuery) =>
    request.get<PageResult<PurchaseOrder>, PageResult<PurchaseOrder>>(
      '/api/purchase/order',
      { params },
    ),
  create: (data: PurchaseOrder) =>
    request.post<PurchaseOrder, PurchaseOrder>('/api/purchase/order', data),
  update: (id: number, data: PurchaseOrder) =>
    request.put<PurchaseOrder, PurchaseOrder>(`/api/purchase/order/${id}`, data),
  /** 付款（后端结算分流：WAIT_PAY → 付款完成） */
  pay: (id: number) =>
    request.put<PurchaseOrder, PurchaseOrder>(`/api/purchase/order/${id}`, { action: 'PAY' }),
}

export const debtApi = {
  list: (params?: PageQuery) =>
    request.get<PageResult<PurchaseDebt>, PageResult<PurchaseDebt>>(
      '/api/purchase/debt',
      { params },
    ),
  create: (data: PurchaseDebt) =>
    request.post<PurchaseDebt, PurchaseDebt>('/api/purchase/debt', data),
  update: (id: number, data: PurchaseDebt) =>
    request.put<PurchaseDebt, PurchaseDebt>(`/api/purchase/debt/${id}`, data),
}

export const payableApi = {
  list: (params?: PageQuery) =>
    request.get<PageResult<PurchasePayable>, PageResult<PurchasePayable>>(
      '/api/purchase/payable',
      { params },
    ),
  create: (data: PurchasePayable) =>
    request.post<PurchasePayable, PurchasePayable>('/api/purchase/payable', data),
  update: (id: number, data: PurchasePayable) =>
    request.put<PurchasePayable, PurchasePayable>(`/api/purchase/payable/${id}`, data),
}
