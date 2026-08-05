import request from './request'
import type { PageResult } from './request'

/** 组织/岗位字典 */
export interface OrgDict {
  id?: number
  /** 字典类型：org 组织 / position 岗位 */
  dictType: string
  name: string
  parentId?: number
  sort?: number
}

/** 组管理（组内客户=组级共享 / 我的客户=个人负责） */
export interface OrgGroup {
  id?: number
  groupName: string
  ownerId?: number
  ownerName?: string
  createTime?: string
}

/** 组划拨/迁移参数 */
export interface GroupTransferParams {
  /** transferType: company 划拨到公司 / group 划拨到组 / owner 批量迁移主要负责人 */
  transferType: 'company' | 'group' | 'owner'
  targetId?: number
  customerIds?: number[]
  ownerId?: number
}

/** 员工管理 */
export interface OrgEmployee {
  id?: number
  name: string
  account?: string
  phone?: string
  dept?: string
  position?: string
  status?: number
  institutionId?: number
}

/** 我的权限 */
export interface PermissionInfo {
  /** 当前登录用户（后端 /api/org/permission 返回 user） */
  user?: { id: number; account: string; name: string }
  menus: string[]
  permissions: string[]
}

export interface PageQuery {
  page?: number
  size?: number
  keyword?: string
  [key: string]: unknown
}

export const dictApi = {
  list: (params?: PageQuery) =>
    request.get<PageResult<OrgDict>, PageResult<OrgDict>>('/api/org/dict', { params }),
  create: (data: OrgDict) => request.post<OrgDict, OrgDict>('/api/org/dict', data),
  update: (id: number, data: OrgDict) =>
    request.put<OrgDict, OrgDict>(`/api/org/dict/${id}`, data),
  remove: (id: number) => request.delete<never, void>(`/api/org/dict/${id}`),
}

export const groupApi = {
  list: (params?: PageQuery) =>
    request.get<PageResult<OrgGroup>, PageResult<OrgGroup>>('/api/org/group', {
      params,
    }),
  create: (data: OrgGroup) => request.post<OrgGroup, OrgGroup>('/api/org/group', data),
  update: (id: number, data: OrgGroup) =>
    request.put<OrgGroup, OrgGroup>(`/api/org/group/${id}`, data),
  transfer: (data: GroupTransferParams) =>
    request.post<never, void>('/api/org/group/transfer', data),
}

export const employeeApi = {
  list: (params?: PageQuery) =>
    request.get<PageResult<OrgEmployee>, PageResult<OrgEmployee>>('/api/org/employee', {
      params,
    }),
  create: (data: OrgEmployee) =>
    request.post<OrgEmployee, OrgEmployee>('/api/org/employee', data),
  update: (id: number, data: OrgEmployee) =>
    request.put<OrgEmployee, OrgEmployee>(`/api/org/employee/${id}`, data),
}

export const permissionApi = {
  get: () =>
    request.get<PermissionInfo, PermissionInfo>('/api/org/permission'),
  refresh: () =>
    request.put<PermissionInfo, PermissionInfo>('/api/org/permission'),
}
