<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { permissionApi } from '../../api/org'
import type { PermissionInfo } from '../../api/org'

const loading = ref(false)
const refreshing = ref(false)
const info = ref<PermissionInfo>({ menus: [], permissions: [] })

async function fetchPermission() {
  loading.value = true
  try {
    const data = await permissionApi.get()
    info.value = data || { menus: [], permissions: [] }
  } catch {
    info.value = { menus: [], permissions: [] }
  } finally {
    loading.value = false
  }
}

async function handleRefresh() {
  refreshing.value = true
  try {
    const data = await permissionApi.refresh()
    info.value = data || { menus: [], permissions: [] }
    ElMessage.success('权限已刷新')
  } catch {
    // 错误已由拦截器提示
  } finally {
    refreshing.value = false
  }
}

const MENU_NAMES: Record<string, string> = {
  base: '基础数据',
  org: '组织权限',
  purchase: '采购',
  sale: '销售',
  inventory: '库存',
  finance: '财务',
  crm: 'CRM',
  flow: '流程引擎',
  todo: '待办事宜',
}

onMounted(fetchPermission)
</script>

<template>
  <div class="page fade-up">
    <div class="page-toolbar">
      <el-button type="primary" :loading="refreshing" @click="handleRefresh">刷新权限</el-button>
      <span class="page-tip">权限按 组织/岗位/组 控制功能可见性，越权访问将被后端拒绝</span>
    </div>

    <el-card v-loading="loading" class="perm-card" shadow="never">
      <div class="perm-section">
        <div class="perm-title">可访问菜单</div>
        <div class="perm-tags">
          <el-tag v-for="m in info.menus" :key="m" type="success" class="perm-tag">
            {{ MENU_NAMES[m] || m }}
          </el-tag>
          <el-empty
            v-if="!info.menus.length"
            description="暂无菜单权限"
            :image-size="48"
          />
        </div>
      </div>

      <el-divider />

      <div class="perm-section">
        <div class="perm-title">权限码（模块:资源:操作）</div>
        <div class="perm-list">
          <el-tag
            v-for="p in info.permissions"
            :key="p"
            type="info"
            effect="plain"
            class="perm-tag perm-code"
          >
            {{ p }}
          </el-tag>
          <el-empty
            v-if="!info.permissions.length"
            description="暂无操作权限"
            :image-size="48"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.page-toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.page-tip {
  font-size: 12px;
  color: var(--color-text-muted);
}

.perm-card {
  background: var(--color-bg-panel);
  border-color: var(--color-border);
}

.perm-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 12px;
}

.perm-tags,
.perm-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  min-height: 60px;
}

.perm-tag {
  font-size: 13px;
}

.perm-code {
  font-family: var(--font-mono);
}
</style>
