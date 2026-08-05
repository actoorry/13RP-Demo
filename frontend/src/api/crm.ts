import request from './request'
import type { PageResult } from './request'

/** 活动管理（使用/生产/经营，主客/次客关系） */
export interface CrmActivity {
  id?: number
  customerId?: number
  /** 客户名称（后端联表返回） */
  customerName?: string
  contactId?: number
  /** 联系人名称（后端联表返回） */
  contactName?: string
  /** 活动类型：使用/生产/经营 */
  activityType?: string
  /** 关系：主客/次客/主潜/次潜/大中/主供/次供 */
  relation?: string
  /** 品名 */
  productName?: string
  /** 价格 */
  price?: number
  /** 预需时间 */
  preNeedTime?: string
  /** 内容 */
  content?: string
  /** 创建人 */
  creator?: string
}

/** 品种资料（月用量/下月计划/竞争对手/SWOT） */
export interface CrmVariety {
  id?: number
  customerId?: number
  customerName?: string
  /** 品种类型：使用/生产/经营 */
  varietyType?: string
  /** 品名 */
  productName?: string
  /** 牌号 */
  grade?: string
  /** 材质 */
  material?: string
  /** 规格 */
  spec?: string
  /** 品牌/产地 */
  brandOrigin?: string
  /** 竞争对手 */
  competitor?: string
  /** SWOT */
  swot?: string
  /** 月用量 */
  monthlyQty?: number
  /** 下月计划 */
  nextMonthPlan?: number
}

/** 证照风控（资料已核实 + 是否允许交易，未核实禁做业务单据） */
export interface CrmCert {
  id?: number
  customerId?: number
  customerName?: string
  /** 证照类型：营业执照/组织机构代码证/税务登记证/法人身份证等 */
  certType?: string
  /** 到期日期 */
  expireDate?: string
  /** 注册资本 */
  registeredCapital?: number
  /** 税号 */
  taxNo?: string
  /** 资料已核实 0/1 */
  verifiedFlag?: number
  /** 是否允许交易 0/1 */
  tradeAllowedFlag?: number
}

/** 客户基本资料 */
export interface CrmCustomer {
  id?: number
  /** 客户名称 */
  name?: string
  /** 来源 */
  source?: string
  /** 公司类型 */
  companyType?: string
  phone?: string
  tel?: string
  email?: string
  address?: string
  /** 行业（组织归属） */
  industry?: string
  /** 等级 */
  level?: string
  /** 下次联系时间 */
  nextContactTime?: string
  remark?: string
  /** 负责人 id */
  ownerId?: number
  /** 跟进标记 0/1 */
  followFlag?: number
  /** 已转化 0/1 */
  convertedFlag?: number
  /** 最后跟进时间 */
  lastFollowTime?: string
}

/** 销售线索 */
export interface CrmLead {
  id?: number
  /** 线索名称 */
  name?: string
  /** 来源 */
  source?: string
  /** 创建时间（后端补充返回；表无该列时前端回退到跟进时间） */
  createTime?: string
  /** 公司类型 */
  companyType?: string
  phone?: string
  tel?: string
  email?: string
  address?: string
  industry?: string
  level?: string
  /** 下次联系时间 */
  nextContactTime?: string
  remark?: string
  ownerId?: number
  /** 跟进标记 0/1 */
  followFlag?: number
  /** 已转化 0/1 */
  convertedFlag?: number
  /** 最后跟进时间 */
  lastFollowTime?: string
}

export interface PageQuery {
  page?: number
  size?: number
  keyword?: string
  [key: string]: unknown
}

export const activityApi = {
  list: (params?: PageQuery) =>
    request.get<PageResult<CrmActivity>, PageResult<CrmActivity>>(
      '/api/crm/activity',
      { params },
    ),
  create: (data: CrmActivity) =>
    request.post<CrmActivity, CrmActivity>('/api/crm/activity', data),
  update: (id: number, data: CrmActivity) =>
    request.put<CrmActivity, CrmActivity>(`/api/crm/activity/${id}`, data),
  remove: (id: number) =>
    request.delete<never, void>(`/api/crm/activity/${id}`),
}

export const varietyApi = {
  list: (params?: PageQuery) =>
    request.get<PageResult<CrmVariety>, PageResult<CrmVariety>>(
      '/api/crm/variety',
      { params },
    ),
  create: (data: CrmVariety) =>
    request.post<CrmVariety, CrmVariety>('/api/crm/variety', data),
  update: (id: number, data: CrmVariety) =>
    request.put<CrmVariety, CrmVariety>(`/api/crm/variety/${id}`, data),
  remove: (id: number) =>
    request.delete<never, void>(`/api/crm/variety/${id}`),
}

export const certApi = {
  list: (params?: PageQuery) =>
    request.get<PageResult<CrmCert>, PageResult<CrmCert>>(
      '/api/crm/cert',
      { params },
    ),
  create: (data: CrmCert) =>
    request.post<CrmCert, CrmCert>('/api/crm/cert', data),
  update: (id: number, data: CrmCert) =>
    request.put<CrmCert, CrmCert>(`/api/crm/cert/${id}`, data),
  remove: (id: number) =>
    request.delete<never, void>(`/api/crm/cert/${id}`),
}

export const customerApi = {
  list: (params?: PageQuery) =>
    request.get<PageResult<CrmCustomer>, PageResult<CrmCustomer>>(
      '/api/crm/customer',
      { params },
    ),
  create: (data: CrmCustomer) =>
    request.post<CrmCustomer, CrmCustomer>('/api/crm/customer', data),
  update: (id: number, data: CrmCustomer) =>
    request.put<CrmCustomer, CrmCustomer>(`/api/crm/customer/${id}`, data),
  remove: (id: number) =>
    request.delete<never, void>(`/api/crm/customer/${id}`),
}

export const leadApi = {
  list: (params?: PageQuery) =>
    request.get<PageResult<CrmLead>, PageResult<CrmLead>>(
      '/api/crm/lead',
      { params },
    ),
  create: (data: CrmLead) =>
    request.post<CrmLead, CrmLead>('/api/crm/lead', data),
  update: (id: number, data: CrmLead) =>
    request.put<CrmLead, CrmLead>(`/api/crm/lead/${id}`, data),
  remove: (id: number) =>
    request.delete<never, void>(`/api/crm/lead/${id}`),
}
