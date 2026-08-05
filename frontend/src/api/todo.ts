import request from './request'
import type { PageResult } from './request'

/** 四板块订阅（CRM/采购/销售/财务，configJson 存阀值） */
export interface TodoSubscription {
  id?: number
  /** 板块：CRM/采购/销售/财务 */
  boardType?: string
  /** 订阅类型：如进项欠票/应收应付/凭证/库龄等 */
  subType?: string
  /** 配置阀值（JSON：{ amount, days } 等） */
  configJson?: string | Record<string, unknown>
  /** 订阅人 id */
  ownerId?: number
  /** 启用 0/1 */
  enabled?: number
}

/** 个人待办（公共/指派，PENDING→DONE） */
export interface TodoPersonal {
  id?: number
  /** 用户 id */
  userId?: number
  /** 待办类型：公共/指派 */
  todoType?: string
  /** 模板：出库/入库模板 */
  templateType?: string
  /** 提醒时间 */
  remindTime?: string
  /** 指派人员 */
  assignee?: string
  /** 状态：PENDING 待办 / DONE 已办 */
  status?: string
}

export interface PageQuery {
  page?: number
  size?: number
  keyword?: string
  [key: string]: unknown
}

export const subscriptionApi = {
  list: (params?: PageQuery) =>
    request.get<PageResult<TodoSubscription>, PageResult<TodoSubscription>>(
      '/api/todo/subscription',
      { params },
    ),
  create: (data: TodoSubscription) =>
    request.post<TodoSubscription, TodoSubscription>('/api/todo/subscription', data),
  update: (id: number, data: TodoSubscription) =>
    request.put<TodoSubscription, TodoSubscription>(`/api/todo/subscription/${id}`, data),
  remove: (id: number) =>
    request.delete<never, void>(`/api/todo/subscription/${id}`),
}

export const personalApi = {
  list: (params?: PageQuery) =>
    request.get<PageResult<TodoPersonal>, PageResult<TodoPersonal>>(
      '/api/todo/personal',
      { params },
    ),
  create: (data: TodoPersonal) =>
    request.post<TodoPersonal, TodoPersonal>('/api/todo/personal', data),
  update: (id: number, data: TodoPersonal) =>
    request.put<TodoPersonal, TodoPersonal>(`/api/todo/personal/${id}`, data),
  remove: (id: number) =>
    request.delete<never, void>(`/api/todo/personal/${id}`),
  /** 完成（PENDING→DONE） */
  complete: (id: number) =>
    request.put<TodoPersonal, TodoPersonal>(`/api/todo/personal/${id}`, { status: 'DONE' }),
}
