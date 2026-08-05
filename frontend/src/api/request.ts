import axios, { type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'

/**
 * 后端统一返回结构（契约 §3.1）
 * code=0 成功；非 0 失败（message 中文提示）；分页返回 data: { list, total }
 */
export interface Result<T = unknown> {
  code: number
  message: string
  data: T
}

/** 分页返回 data 结构 */
export interface PageResult<T = unknown> {
  list: T[]
  total: number
}

/**
 * axios 实例：baseURL 留空，走 Vite 代理 /api → 后端 8080。
 * 请求拦截器注入 Authorization: Bearer <token>；
 * 响应拦截器解包 {code,message,data}：code!==0 报错、401 跳登录。
 */
const request = axios.create({
  baseURL: '',
  timeout: 15000,
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const body = response.data as Result
    // 决策演示等非 Result 结构直接放行
    if (body && typeof body === 'object' && 'code' in body) {
      if (body.code !== 0) {
        ElMessage.error(body.message || '请求失败')
        return Promise.reject(new Error(body.message || '请求失败'))
      }
      // 拦截器解包 data 供 api 层直接使用；TS 断言为 AxiosResponse 以满足 axios 签名
      return body.data as unknown as AxiosResponse
    }
    return body as unknown as AxiosResponse
  },
  (error) => {
    const status: number | undefined = error?.response?.status
    if (status === 401) {
      // 清空本地登录态，回登录页（hash 路由直接改 hash，避免与 router 循环依赖）
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      localStorage.removeItem('menus')
      localStorage.removeItem('permissions')
      window.location.hash = '#/login'
      ElMessage.warning('登录已过期，请重新登录')
    } else {
      const msg =
        error?.response?.data?.message || error?.message || '网络错误，请稍后重试'
      ElMessage.error(msg)
    }
    return Promise.reject(error)
  },
)

export default request
