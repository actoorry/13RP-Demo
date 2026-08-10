import { ref } from 'vue'

export type ThemeMode = 'dark' | 'light'

/** localStorage 持久化 key（契约写死） */
const STORAGE_KEY = '13rp-theme'

/** 模块级单例：多组件共享同一主题状态（默认浅色 = 方案 1 霁青简白；已存 dark 的保留 dark） */
const stored = localStorage.getItem(STORAGE_KEY)
const theme = ref<ThemeMode>(stored === 'dark' ? 'dark' : 'light')

function applyTheme(mode: ThemeMode) {
  document.documentElement.setAttribute('data-theme', mode)
  // Element Plus dark 主题联动：dark 模式加 class，light 模式移除
  document.documentElement.classList.toggle('dark', mode === 'dark')
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
