import request from './request'
import type { PageResult } from './request'

/** X5 流程实例（报销/借款/付款/退款；500 元分级审批；RUNNING→DONE/REJECTED） */
export interface FlowX5 {
  id?: number
  /** 流程单号 */
  flowNo?: string
  /** 类型：报销/借款/付款/退款 */
  flowType?: string
  /** 标题 */
  title?: string
  /** 金额 */
  amount?: number
  /** 申请人 */
  applicant?: string
  /** 当前步骤 */
  currentStep?: string
  /** 当前审批人 */
  approver?: string
  /** 状态：RUNNING 运行中 / DONE 完成 / REJECTED 驳回 */
  status?: string
  /** 创建时间 */
  createTime?: string
}

/** 安码流程实例（合同/财务审批；RUNNING→DONE/REJECTED） */
export interface FlowAnma {
  id?: number
  /** 流程单号 */
  flowNo?: string
  /** 类型：合同/财务审批 */
  flowType?: string
  /** 标题 */
  title?: string
  /** 合同金额 */
  contractAmount?: number
  supplierId?: number
  /** 供应商名称（后端联表返回） */
  supplierName?: string
  customerId?: number
  /** 客户名称（后端联表返回） */
  customerName?: string
  /** 当前步骤 */
  currentStep?: string
  /** 当前审批人 */
  approver?: string
  /** 状态：RUNNING 运行中 / DONE 完成 / REJECTED 驳回 */
  status?: string
}

/** 流程待办/已办（PENDING→DONE，多办理人任一通过） */
export interface FlowTask {
  id?: number
  /** 流程实例 id */
  instanceId?: number
  /** 步骤名称 */
  stepName?: string
  /** 办理人 */
  assignee?: string
  /** 状态：PENDING 待办 / DONE 已办 */
  status?: string
  /** 备注 */
  remark?: string
}

export interface PageQuery {
  page?: number
  size?: number
  keyword?: string
  [key: string]: unknown
}

export const flowX5Api = {
  list: (params?: PageQuery) =>
    request.get<PageResult<FlowX5>, PageResult<FlowX5>>(
      '/api/flow/x5',
      { params },
    ),
  create: (data: FlowX5) =>
    request.post<FlowX5, FlowX5>('/api/flow/x5', data),
  update: (id: number, data: FlowX5) =>
    request.put<FlowX5, FlowX5>(`/api/flow/x5/${id}`, data),
  remove: (id: number) =>
    request.delete<never, void>(`/api/flow/x5/${id}`),
  /** 审批通过（RUNNING→DONE） */
  approve: (id: number) =>
    request.put<FlowX5, FlowX5>(`/api/flow/x5/${id}`, { status: 'DONE' }),
  /** 驳回（RUNNING→REJECTED） */
  reject: (id: number) =>
    request.put<FlowX5, FlowX5>(`/api/flow/x5/${id}`, { status: 'REJECTED' }),
}

export const flowAnmaApi = {
  list: (params?: PageQuery) =>
    request.get<PageResult<FlowAnma>, PageResult<FlowAnma>>(
      '/api/flow/anma',
      { params },
    ),
  create: (data: FlowAnma) =>
    request.post<FlowAnma, FlowAnma>('/api/flow/anma', data),
  update: (id: number, data: FlowAnma) =>
    request.put<FlowAnma, FlowAnma>(`/api/flow/anma/${id}`, data),
  remove: (id: number) =>
    request.delete<never, void>(`/api/flow/anma/${id}`),
  /** 审批通过（RUNNING→DONE） */
  approve: (id: number) =>
    request.put<FlowAnma, FlowAnma>(`/api/flow/anma/${id}`, { status: 'DONE' }),
  /** 驳回（RUNNING→REJECTED） */
  reject: (id: number) =>
    request.put<FlowAnma, FlowAnma>(`/api/flow/anma/${id}`, { status: 'REJECTED' }),
}

export const flowTaskApi = {
  list: (params?: PageQuery) =>
    request.get<PageResult<FlowTask>, PageResult<FlowTask>>(
      '/api/flow/task',
      { params },
    ),
  create: (data: FlowTask) =>
    request.post<FlowTask, FlowTask>('/api/flow/task', data),
  update: (id: number, data: FlowTask) =>
    request.put<FlowTask, FlowTask>(`/api/flow/task/${id}`, data),
  remove: (id: number) =>
    request.delete<never, void>(`/api/flow/task/${id}`),
  /** 办理（PENDING→DONE，多办理人任一通过） */
  complete: (id: number) =>
    request.put<FlowTask, FlowTask>(`/api/flow/task/${id}`, { status: 'DONE' }),
}
