<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import { useAccountStore } from '../stores/account'
import ThemeToggle from '../components/ThemeToggle.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const accountStore = useAccountStore()

const accountLoading = ref(false)

const currentUser = computed(() => auth.user?.name || auth.user?.account || '未登录')

/** 当前激活菜单 = 完整路由路径（el-menu router 模式） */
const activeMenu = computed(() => route.path)

/** 动态展开当前所在业务域子菜单（共用底座下含嵌套：/admin-common → /admin/base） */
const openedMenus = computed(() => {
  const path = route.path
  if (path.startsWith('/admin/base')) return ['/admin-common', '/admin/base']
  if (path.startsWith('/admin/org')) return ['/admin/org']
  if (path.startsWith('/admin/purchase')) return ['/admin/purchase']
  if (path.startsWith('/admin/flow') || path.startsWith('/admin/todo')) return ['/admin-common']
  if (path.startsWith('/admin/sale') || path.startsWith('/admin/crm')) return ['/admin-sale']
  if (path.startsWith('/admin/inventory')) return ['/admin-inventory']
  if (path.startsWith('/admin/finance')) return ['/admin-finance']
  return []
})

onMounted(async () => {
  auth.refreshMe().catch(() => {
    // 刷新用户信息失败不影响布局展示
  })
  accountLoading.value = true
  try {
    await accountStore.fetchAccounts()
  } catch {
    // 账套拉取失败时仅提示一次，仍可进入页面
    ElMessage.warning('账套列表加载失败')
  } finally {
    accountLoading.value = false
  }
})

function handleAccountChange(id: number) {
  accountStore.setAccount(id)
  ElMessage.success('已切换到账套：' + (accountStore.currentAccount?.name ?? id))
}

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定退出登录吗？', '提示', { type: 'warning' })
  } catch {
    return
  }
  auth.logout()
  router.push('/login')
}
</script>

<template>
  <div class="admin-layout">
    <header class="admin-header">
      <div class="admin-header-left">
        <span class="admin-title">13RP 管理端</span>
        <span class="admin-subtitle">博宇企业管理平台</span>
      </div>

      <div class="admin-header-right">
        <span class="admin-select-label">账套</span>
        <el-select
          v-model="accountStore.currentAccountId"
          class="admin-account-select"
          :loading="accountLoading"
          placeholder="选择账套"
          @change="handleAccountChange"
        >
          <el-option
            v-for="acc in accountStore.accounts"
            :key="acc.id"
            :label="acc.name"
            :value="acc.id"
          />
        </el-select>

        <ThemeToggle />

        <el-divider direction="vertical" />

        <el-dropdown>
          <span class="admin-user">
            <span class="admin-user-dot" />
            <span class="admin-user-name">{{ currentUser }}</span>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="router.push('/demo')">决策演示</el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <div class="admin-body">
      <aside class="admin-sider">
        <el-menu :default-active="activeMenu" :default-openeds="openedMenus" router>
          <!-- 共用底座 -->
          <el-sub-menu index="/admin-common">
            <template #title>共用底座</template>
            <el-sub-menu index="/admin/base">
              <template #title>基础数据与系统管理</template>
              <el-menu-item index="/admin/base/account">账套管理</el-menu-item>
              <el-menu-item index="/admin/base/product">产品主数据</el-menu-item>
              <el-menu-item index="/admin/base/material-element">材质元素</el-menu-item>
              <el-menu-item index="/admin/base/package-standard">合同包装验收标准</el-menu-item>
              <el-menu-item index="/admin/base/mobile-config">移动端主营品种</el-menu-item>
            </el-sub-menu>
            <el-menu-item index="/admin/flow">流程引擎</el-menu-item>
            <el-menu-item index="/admin/todo">待办事宜</el-menu-item>
          </el-sub-menu>

          <!-- 采购模块 -->
          <el-sub-menu index="/admin/purchase">
            <template #title>采购模块</template>
            <el-menu-item index="/admin/purchase/supplier-grade">供应商分级</el-menu-item>
            <el-menu-item index="/admin/purchase/forecast">预测预案</el-menu-item>
            <el-menu-item index="/admin/purchase/inquiry">询价管理</el-menu-item>
            <el-menu-item index="/admin/purchase/apply">采购申请</el-menu-item>
            <el-menu-item index="/admin/purchase/order">待审批订单</el-menu-item>
            <el-menu-item index="/admin/purchase/debt">进项欠票</el-menu-item>
            <el-menu-item index="/admin/purchase/payable">应付列表</el-menu-item>
          </el-sub-menu>

          <!-- 销售模块 -->
          <el-sub-menu index="/admin-sale">
            <template #title>销售模块</template>
            <el-menu-item index="/admin/sale">销售</el-menu-item>
            <el-menu-item index="/admin/crm">CRM</el-menu-item>
          </el-sub-menu>

          <!-- 库存模块 -->
          <el-sub-menu index="/admin-inventory">
            <template #title>库存模块</template>
            <el-menu-item index="/admin/inventory">库存</el-menu-item>
          </el-sub-menu>

          <!-- 财务模块 -->
          <el-sub-menu index="/admin-finance">
            <template #title>财务模块</template>
            <el-menu-item index="/admin/finance">财务</el-menu-item>
          </el-sub-menu>

          <!-- 人力模块 -->
          <el-sub-menu index="/admin/org">
            <template #title>人力模块</template>
            <el-menu-item index="/admin/org/dict">组织/岗位字典</el-menu-item>
            <el-menu-item index="/admin/org/group">组管理</el-menu-item>
            <el-menu-item index="/admin/org/comprehensive">综合管理</el-menu-item>
            <el-menu-item index="/admin/org/employee">员工管理</el-menu-item>
            <el-menu-item index="/admin/org/permission">我的权限</el-menu-item>
          </el-sub-menu>
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

.admin-subtitle {
  font-size: 12px;
  color: var(--color-text-secondary);
  letter-spacing: 1px;
}

.admin-select-label {
  font-size: 13px;
  color: var(--color-text-secondary);
}

.admin-account-select {
  width: 180px;
}

.admin-user {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 4px 10px;
  cursor: pointer;
  font-size: 13px;
  color: var(--color-text);
  border-radius: 6px;
}

.admin-user:hover {
  background: var(--color-bg-elevated);
}

.admin-user-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--color-primary);
}

.admin-user-name {
  max-width: 140px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
