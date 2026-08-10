<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useTheme } from './composables/useTheme'

// 应用级主题初始化：useTheme 为模块级单例，模块加载即应用已保存主题（localStorage 持久化，刷新恢复）
useTheme()

const route = useRoute()

// 决策演示入口：/demo 页面本身无需入口；登录页与管理端页右下角保留浮动入口
const showDemoEntry = computed(() => route.path !== '/demo')
</script>

<template>
  <router-view />
  <router-link v-if="showDemoEntry" to="/demo" class="demo-entry">
    决策演示
  </router-link>
</template>

<style scoped>
.demo-entry {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 2000;
  padding: 8px 16px;
  font-size: 13px;
  color: var(--color-primary);
  background: var(--color-bg-elevated);
  border: 1px solid var(--color-border);
  border-radius: 20px;
  text-decoration: none;
  letter-spacing: 1px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.demo-entry:hover {
  color: var(--color-bg);
  background: var(--color-primary);
  border-color: var(--color-primary);
}
</style>
