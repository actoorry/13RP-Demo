<script setup lang="ts">
import { ref } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

// 顶部栏公司/平台选择（模拟数据，仅用于演示切换）
const companyOptions = [{ value: 'boyu', label: '沈阳博宇会幸福实业有限公司' }]
const platformOptions = [
  { value: 'global', label: '全球发展' },
  { value: 'domestic', label: '国内发展' },
]

const currentCompany = ref('boyu')
const currentPlatform = ref('global')

// 侧边栏一级导航：index 即路由 path，el-menu router 模式直接跳转
const menuItems = [
  { index: '/demo', label: '决策演示' },
  { index: '/company', label: '公司架构' },
  { index: '/crm', label: 'CRM' },
  { index: '/product', label: '商品管理' },
  { index: '/system', label: '系统管理' },
  { index: '/cms', label: 'CMS' },
  { index: '/operation', label: '运营管理' },
  { index: '/stats', label: '数据统计' },
]
</script>

<template>
  <div class="admin-layout">
    <header class="admin-header">
      <div class="admin-header-left">
        <span class="admin-title">13RP管理端</span>
      </div>
      <div class="admin-header-right">
        <span class="admin-select-label">公司</span>
        <el-select v-model="currentCompany" class="admin-select">
          <el-option
            v-for="opt in companyOptions"
            :key="opt.value"
            :value="opt.value"
            :label="opt.label"
          />
        </el-select>
        <span class="admin-select-label">平台</span>
        <el-select v-model="currentPlatform" class="admin-select">
          <el-option
            v-for="opt in platformOptions"
            :key="opt.value"
            :value="opt.value"
            :label="opt.label"
          />
        </el-select>
      </div>
    </header>

    <div class="admin-body">
      <aside class="admin-sider">
        <el-menu :default-active="route.path" router class="admin-menu">
          <el-menu-item v-for="item in menuItems" :key="item.index" :index="item.index">
            {{ item.label }}
          </el-menu-item>
        </el-menu>
      </aside>

      <main class="admin-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<style scoped>
.admin-title {
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px;
  color: var(--color-text);
}

.admin-select-label {
  font-size: 13px;
  color: var(--color-text-secondary);
}

.admin-select {
  width: 220px;
}
</style>
