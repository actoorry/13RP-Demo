<script setup lang="ts">
import { useTheme } from '../composables/useTheme'

const { theme, toggleTheme } = useTheme()

const tipText = () => (theme.value === 'dark' ? '切换到日间模式' : '切换到夜间模式')
</script>

<template>
  <el-tooltip :content="tipText()" placement="bottom">
    <button
      type="button"
      class="theme-toggle"
      :aria-label="tipText()"
      @click="toggleTheme"
    >
      <span class="theme-toggle-icon">{{ theme === 'dark' ? '🌙' : '☀️' }}</span>
    </button>
  </el-tooltip>
</template>

<style scoped>
.theme-toggle {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  padding: 0;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-bg-elevated);
  cursor: pointer;
  transition: background-color 0.3s var(--ease-out), border-color 0.3s var(--ease-out);
}

.theme-toggle:hover {
  border-color: var(--color-primary);
}

.theme-toggle-icon {
  font-size: 16px;
  line-height: 1;
}
</style>
