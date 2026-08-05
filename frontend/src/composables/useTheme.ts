import { ref } from 'vue'

export type ThemeMode = 'dark' | 'light'

/** localStorage 持久化 key（契约写死） */
const STORAGE_KEY = '13rp-theme'

/** 模块级单例：多组件共享同一主题状态（默认深色 = 现状不变） */
const stored = localStorage.getItem(STORAGE_KEY)
const theme = ref<ThemeMode>(stored === 'light' ? 'light' : 'dark')

function applyTheme(mode: ThemeMode) {
  document.documentElement.setAttribute('data-theme', mode)
  localStorage.setItem(STORAGE_KEY, mode)
}

// 模块加载即应用已保存主题（刷新恢复 / 首屏一致）
applyTheme(theme.value)

function toggleTheme() {
  theme.value = theme.value === 'dark' ? 'light' : 'dark'
  applyTheme(theme.value)
}

export function useTheme() {
  return { theme, toggleTheme }
}
