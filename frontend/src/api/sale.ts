import request from './request'
import type { PageResult } from './request'

/** 销售明细（数量/金额/利润/成本/费用） */
export interface SaleOrder {
  id?: number
  orderNo: string
  customerId?: number
  /** 客户名称（后端联表返回时使用，缺省回退 customerId） */
  customerName?: string
  productName?: string
  qty?: number
  amount?: number
  profit?: number
  cost?: number
  fee?: number
  orgId?: number
  orgName?: string
  createTime?: string
}

/** 业务日报漏斗（联系/线索/成交） */
export interface SaleDailyReport {
  id?: number
  reportDate?: string
  contactCnt?: number
  leadCnt?: number
  dealCnt?: number
  orgId?: number
  orgName?: string
}

/** 开票申请（APPLIED 申请 → PENDING 待开 → ISSUED 已开） */
export interface SaleInvoiceApply {
  id?: number
  applyNo: string
  customerId?: number
  customerName?: string
  invoiceNo?: string
  status: string
  creator?: string
}

export interface PageQuery {
  page?: number
  size?: number
  keyword?: string
  [key: string]: unknown
}

export const saleOrderApi = {
  list: (params?: PageQuery) =>
    request.get<PageResult<SaleOrder>, PageResult<SaleOrder>>('/api/sale/order', {
      params,
    }),
  create: (data: SaleOrder) =>
    request.post<SaleOrder, SaleOrder>('/api/sale/order', data),
  update: (id: number, data: Partial<SaleOrder>) =>
    request.put<SaleOrder, SaleOrder>(`/api/sale/order/${id}`, data),
  remove: (id: number) =>
    request.delete<never, void>(`/api/sale/order/${id}`),
}

export const saleDailyReportApi = {
  list: (params?: PageQuery) =>
    request.get<PageResult<SaleDailyReport>, PageResult<SaleDailyReport>>(
      '/api/sale/daily-report',
      { params },
    ),
  create: (data: SaleDailyReport) =>
    request.post<SaleDailyReport, SaleDailyReport>('/api/sale/daily-report', data),
  update: (id: number, data: Partial<SaleDailyReport>) =>
    request.put<SaleDailyReport, SaleDailyReport>(`/api/sale/daily-report/${id}`, data),
  remove: (id: number) =>
    request.delete<never, void>(`/api/sale/daily-report/${id}`),
}

export const saleInvoiceApplyApi = {
  list: (params?: PageQuery) =>
    request.get<PageResult<SaleInvoiceApply>, PageResult<SaleInvoiceApply>>(
      '/api/sale/invoice-apply',
      { params },
    ),
  create: (data: SaleInvoiceApply) =>
    request.post<SaleInvoiceApply, SaleInvoiceApply>('/api/sale/invoice-apply', data),
  update: (id: number, data: Partial<SaleInvoiceApply>) =>
    request.put<SaleInvoiceApply, SaleInvoiceApply>(`/api/sale/invoice-apply/${id}`, data),
  remove: (id: number) =>
    request.delete<never, void>(`/api/sale/invoice-apply/${id}`),
}
