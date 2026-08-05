import request from './request'

/** 登录用户信息（契约 §3.2） */
export interface LoginUser {
  id: number
  account: string
  name: string
}

/** 登录返回：JWT + 用户 + 菜单 + 权限 */
export interface LoginResult {
  token: string
  user: LoginUser
  menus: string[]
  permissions: string[]
}

export interface LoginParams {
  account: string
  password: string
}

/** 登录（匿名放行） */
export function login(data: LoginParams) {
  return request.post<LoginResult, LoginResult>('/api/auth/login', data)
}

/** 当前登录用户信息（需 JWT） */
export function fetchMe() {
  return request.get<LoginUser, LoginUser>('/api/auth/me')
}
