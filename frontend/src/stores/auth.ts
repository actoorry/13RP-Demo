import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { fetchMe, login as loginApi } from '../api/auth'
import type { LoginParams, LoginUser } from '../api/auth'

const TOKEN_KEY = 'token'
const USER_KEY = 'user'
const MENUS_KEY = 'menus'
const PERMS_KEY = 'permissions'

function readJSON<T>(key: string, fallback: T): T {
  try {
    const raw = localStorage.getItem(key)
    return raw ? (JSON.parse(raw) as T) : fallback
  } catch {
    return fallback
  }
}

/** 登录态/用户/菜单/权限（localStorage 持久化，刷新恢复） */
export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const user = ref<LoginUser | null>(readJSON<LoginUser | null>(USER_KEY, null))
  const menus = ref<string[]>(readJSON<string[]>(MENUS_KEY, []))
  const permissions = ref<string[]>(readJSON<string[]>(PERMS_KEY, []))

  const isLoggedIn = computed(() => !!token.value)

  function persist() {
    if (token.value) {
      localStorage.setItem(TOKEN_KEY, token.value)
    } else {
      localStorage.removeItem(TOKEN_KEY)
    }
    if (user.value) {
      localStorage.setItem(USER_KEY, JSON.stringify(user.value))
    } else {
      localStorage.removeItem(USER_KEY)
    }
    localStorage.setItem(MENUS_KEY, JSON.stringify(menus.value))
    localStorage.setItem(PERMS_KEY, JSON.stringify(permissions.value))
  }

  async function login(params: LoginParams) {
    const data = await loginApi(params)
    token.value = data.token
    user.value = data.user
    menus.value = data.menus || []
    permissions.value = data.permissions || []
    persist()
    return data
  }

  /** 刷新当前用户信息（登录后调用，保持与后端一致） */
  async function refreshMe() {
    if (!token.value) return
    const me = await fetchMe()
    if (me) {
      // 后端 /api/auth/me 返回 { user: {id, account, name} }，兼容两种结构
      const meUser = (me as unknown as { user?: LoginUser }).user
      user.value = meUser ?? (me as unknown as LoginUser)
    }
    persist()
  }

  function hasPermission(perm: string) {
    return permissions.value.includes(perm)
  }

  function logout() {
    token.value = ''
    user.value = null
    menus.value = []
    permissions.value = []
    persist()
  }

  return { token, user, menus, permissions, isLoggedIn, login, refreshMe, hasPermission, logout }
})
