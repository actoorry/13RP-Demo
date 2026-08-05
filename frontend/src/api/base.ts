import request from './request'
import type { PageResult } from './request'

/** 基础数据域 —— 账套管理（多账套切换，数据隔离边界） */
export interface BaseAccount {
  id?: number
  name: string
  code: string
  status?: number
  createTime?: string
}

/** 产品主数据（六项维护：品名/牌号/材质/规格/品牌产地/其他 + 作废级联） */
export interface BaseProduct {
  id?: number
  accountId?: number
  name: string
  grade?: string
  material?: string
  spec?: string
  brandOrigin?: string
  other?: string
  parentId?: number
  sort?: number
  status?: number
}

/** 产品主数据树节点（GET /api/base/product/tree → 品名→牌号→材质树） */
export interface ProductNode {
  id: number
  name: string
  /** 节点类型：product 品名 / grade 牌号 / material 材质 */
  type: string
  children?: ProductNode[]
}

/** 材质元素维护（元素符号/常用值/含量区间/牌号独立标记） */
export interface MaterialElement {
  id?: number
  symbol: string
  sort?: number
  commonValue?: string
  rangeMin?: number
  rangeMax?: number
  gradeIndependent?: number
}

/** 合同包装验收标准 */
export interface PackageStandard {
  id?: number
  packageName: string
  damageCompensation?: string
  status?: number
}

/** 移动端主营品种 */
export interface MobileConfig {
  id?: number
  productName: string
  sort?: number
  status?: number
}

/** 通用查询参数 */
export interface PageQuery {
  page?: number
  size?: number
  keyword?: string
  accountId?: number
  [key: string]: unknown
}

export const accountApi = {
  list: (params?: PageQuery) =>
    request.get<PageResult<BaseAccount>, PageResult<BaseAccount>>('/api/base/account', {
      params,
    }),
  create: (data: BaseAccount) =>
    request.post<BaseAccount, BaseAccount>('/api/base/account', data),
  update: (id: number, data: BaseAccount) =>
    request.put<BaseAccount, BaseAccount>(`/api/base/account/${id}`, data),
  remove: (id: number) =>
    request.delete<never, void>(`/api/base/account/${id}`),
}

export const productApi = {
  list: (params?: PageQuery) =>
    request.get<PageResult<BaseProduct>, PageResult<BaseProduct>>('/api/base/product', {
      params,
    }),
  tree: (accountId: number) =>
    request.get<ProductNode[], ProductNode[]>('/api/base/product/tree', {
      params: { accountId },
    }),
  create: (data: BaseProduct) =>
    request.post<BaseProduct, BaseProduct>('/api/base/product', data),
  update: (id: number, data: BaseProduct) =>
    request.put<BaseProduct, BaseProduct>(`/api/base/product/${id}`, data),
  remove: (id: number) =>
    request.delete<never, void>(`/api/base/product/${id}`),
}

export const materialElementApi = {
  list: (params?: PageQuery) =>
    request.get<PageResult<MaterialElement>, PageResult<MaterialElement>>(
      '/api/base/material-element',
      { params },
    ),
  create: (data: MaterialElement) =>
    request.post<MaterialElement, MaterialElement>('/api/base/material-element', data),
  update: (id: number, data: MaterialElement) =>
    request.put<MaterialElement, MaterialElement>(
      `/api/base/material-element/${id}`,
      data,
    ),
  remove: (id: number) =>
    request.delete<never, void>(`/api/base/material-element/${id}`),
}

export const packageStandardApi = {
  list: (params?: PageQuery) =>
    request.get<PageResult<PackageStandard>, PageResult<PackageStandard>>(
      '/api/base/package-standard',
      { params },
    ),
  create: (data: PackageStandard) =>
    request.post<PackageStandard, PackageStandard>('/api/base/package-standard', data),
  update: (id: number, data: PackageStandard) =>
    request.put<PackageStandard, PackageStandard>(
      `/api/base/package-standard/${id}`,
      data,
    ),
  remove: (id: number) =>
    request.delete<never, void>(`/api/base/package-standard/${id}`),
}

export const mobileConfigApi = {
  list: (params?: PageQuery) =>
    request.get<PageResult<MobileConfig>, PageResult<MobileConfig>>(
      '/api/base/mobile-config',
      { params },
    ),
  create: (data: MobileConfig) =>
    request.post<MobileConfig, MobileConfig>('/api/base/mobile-config', data),
  update: (id: number, data: MobileConfig) =>
    request.put<MobileConfig, MobileConfig>(`/api/base/mobile-config/${id}`, data),
  remove: (id: number) =>
    request.delete<never, void>(`/api/base/mobile-config/${id}`),
}
