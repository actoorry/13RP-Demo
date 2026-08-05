import request from './request'
import type { PageResult } from './request'

/** 到账公告 */
export interface FinanceArrival {
  id?: number
  accountId?: number
  /** 账套名称（后端联表返回时使用，缺省回退 accountId） */
  accountName?: string
  orgId?: number
  orgName?: string
  amount?: number
  arrivalTime?: string
  operator?: string
}

/** 费用管理（分摊 UNALLOCATED → ALLOCATED；marked=1 红色标记） */
export interface FinanceExpense {
  id?: number
  expenseNo: string
  customerId?: number
  customerName?: string
  productName?: string
  amount?: number
  taxAmount?: number
  /** 分摊类型 */
  allocateType?: string
  allocateStatus: string
  /** 标记（1 变色红色） */
  marked?: number
}

/** 发票管理（CREATED 已新增 → APPROVED 已审核 → VOID 已作废） */
export interface FinanceInvoice {
  id?: number
  invoiceNo: string
  /** 进项/销项 */
  invoiceType?: string
  customerId?: number
  customerName?: string
  productCode?: string
  productName?: string
  amount?: number
  status: string
  auditor?: string
}

/** 化验费（报告 PENDING → PASS/FAIL；付款 UNPAID → PAID → REIMBURSED） */
export interface FinanceLabFee {
  id?: number
  inboundId?: number
  /** 化验机构 */
  labName?: string
  sampleNo?: string
  element?: string
  labFee?: number
  reportStatus: string
  payStatus: string
  voucherNo?: string
}

/** 应收应付 */
export interface FinanceArAp {
  id?: number
  /** 往来类型：客户 CUSTOMER / 供应商 SUPPLIER */
  partyType?: string
  partyId?: number
  /** 往来方名称（后端联表返回时使用，缺省回退 partyId） */
  partyName?: string
  accountId?: number
  accountName?: string
  orgId?: number
  orgName?: string
  receivable?: number
  payable?: number
  balance?: number
}

export interface PageQuery {
  page?: number
  size?: number
  keyword?: string
  [key: string]: unknown
}

export const financeArrivalApi = {
  list: (params?: PageQuery) =>
    request.get<PageResult<FinanceArrival>, PageResult<FinanceArrival>>(
      '/api/finance/arrival',
      { params },
    ),
  create: (data: FinanceArrival) =>
    request.post<FinanceArrival, FinanceArrival>('/api/finance/arrival', data),
  update: (id: number, data: Partial<FinanceArrival>) =>
    request.put<FinanceArrival, FinanceArrival>(`/api/finance/arrival/${id}`, data),
  remove: (id: number) =>
    request.delete<never, void>(`/api/finance/arrival/${id}`),
}

export const financeExpenseApi = {
  list: (params?: PageQuery) =>
    request.get<PageResult<FinanceExpense>, PageResult<FinanceExpense>>(
      '/api/finance/expense',
      { params },
    ),
  create: (data: FinanceExpense) =>
    request.post<FinanceExpense, FinanceExpense>('/api/finance/expense', data),
  update: (id: number, data: Partial<FinanceExpense>) =>
    request.put<FinanceExpense, FinanceExpense>(`/api/finance/expense/${id}`, data),
  remove: (id: number) =>
    request.delete<never, void>(`/api/finance/expense/${id}`),
}

export const financeInvoiceApi = {
  list: (params?: PageQuery) =>
    request.get<PageResult<FinanceInvoice>, PageResult<FinanceInvoice>>(
      '/api/finance/invoice',
      { params },
    ),
  create: (data: FinanceInvoice) =>
    request.post<FinanceInvoice, FinanceInvoice>('/api/finance/invoice', data),
  update: (id: number, data: Partial<FinanceInvoice>) =>
    request.put<FinanceInvoice, FinanceInvoice>(`/api/finance/invoice/${id}`, data),
  remove: (id: number) =>
    request.delete<never, void>(`/api/finance/invoice/${id}`),
}

export const financeLabFeeApi = {
  list: (params?: PageQuery) =>
    request.get<PageResult<FinanceLabFee>, PageResult<FinanceLabFee>>(
      '/api/finance/lab-fee',
      { params },
    ),
  create: (data: FinanceLabFee) =>
    request.post<FinanceLabFee, FinanceLabFee>('/api/finance/lab-fee', data),
  update: (id: number, data: Partial<FinanceLabFee>) =>
    request.put<FinanceLabFee, FinanceLabFee>(`/api/finance/lab-fee/${id}`, data),
  remove: (id: number) =>
    request.delete<never, void>(`/api/finance/lab-fee/${id}`),
}

export const financeArApApi = {
  list: (params?: PageQuery) =>
    request.get<PageResult<FinanceArAp>, PageResult<FinanceArAp>>(
      '/api/finance/ar-ap',
      { params },
    ),
  create: (data: FinanceArAp) =>
    request.post<FinanceArAp, FinanceArAp>('/api/finance/ar-ap', data),
  update: (id: number, data: Partial<FinanceArAp>) =>
    request.put<FinanceArAp, FinanceArAp>(`/api/finance/ar-ap/${id}`, data),
  remove: (id: number) =>
    request.delete<never, void>(`/api/finance/ar-ap/${id}`),
}
