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
  base: '基础数据与系统管理',
  org: '组织与权限',
  purchase: '采购',
  sale: '销售',
  inventory: '库存',
  finance: '财务',
  crm: 'CRM',
  flow: '流程引擎',
  todo: '待办事宜',
}

/** 当前用户姓名（缺省回退账号）。 */
const displayName = () => info.value.user?.name || info.value.user?.account || '-'

onMounted(fetchPermission)
</script>

<template>
  <div class="page fade-up">
    <div class="page-toolbar">
      <el-button type="primary" :loading="refreshing" @click="handleRefresh">刷新权限</el-button>
      <span class="page-tip">权限由 组织/岗位/角色 控制功能可见性，越权访问将被后端拒绝</span>
    </div>

    <!-- ① 用户身份卡片 -->
    <el-card v-loading="loading" class="perm-card" shadow="never">
      <div class="identity-row">
        <el-avatar :size="52" class="identity-avatar">{{ displayName().charAt(0) }}</el-avatar>
        <div class="identity-info">
          <div class="identity-name">
            {{ displayName() }}
            <el-tag
              v-for="r in info.roles || []"
              :key="r"
              type="warning"
              effect="light"
              size="small"
              class="identity-role"
            >
              角色：{{ r }}
            </el-tag>
          </div>
          <div class="identity-meta">
            <span>账号：{{ info.user?.account || '-' }}</span>
            <el-divider direction="vertical" />
            <span>部门：{{ info.user?.dept || '未设置' }}</span>
            <el-divider direction="vertical" />
            <span>岗位：{{ info.user?.position || '未设置' }}</span>
          </div>
          <div v-if="(info.roles || []).length" class="identity-source">
            权限来源：
            <template v-for="r in info.roles" :key="r">
              <span class="source-chip">{{ r }}</span>
            </template>
          </div>
        </div>
      </div>
    </el-card>

    <!-- ② 可访问菜单 -->
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
    </el-card>

    <!-- ③ 权限明细（按模块分组 + 中文翻译） -->
    <el-card v-loading="loading" class="perm-card" shadow="never">
      <div class="perm-section">
        <div class="perm-title">
          操作权限明细
          <span class="perm-count">共 {{ info.permissionGroups?.reduce((n, g) => n + g.items.length, 0) || info.permissions.length }} 项</span>
        </div>

        <template v-if="(info.permissionGroups || []).length">
          <div v-for="g in info.permissionGroups" :key="g.module" class="perm-group">
            <div class="perm-group-title">
              <span class="perm-group-dot" />
              {{ g.moduleName }}
              <span class="perm-group-count">{{ g.items.length }}</span>
            </div>
            <div class="perm-group-items">
              <el-tag
                v-for="item in g.items"
                :key="item.code"
                type="info"
                effect="plain"
                class="perm-tag perm-item"
              >
                {{ item.name }}
              </el-tag>
            </div>
          </div>
        </template>
        <el-empty
          v-else
          description="暂无操作权限"
          :image-size="48"
        />
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
  margin-bottom: 14px;
}

/* ① 身份卡片 */
.identity-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.identity-avatar {
  flex-shrink: 0;
  background: var(--color-primary);
  color: #fff;
  font-size: 20px;
}

.identity-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text);
}

.identity-role {
  font-weight: 400;
}

.identity-meta {
  margin-top: 6px;
  font-size: 13px;
  color: var(--color-text-secondary);
}

.identity-source {
  margin-top: 6px;
  font-size: 12px;
  color: var(--color-text-muted);
}

.source-chip {
  display: inline-block;
  margin-right: 6px;
  padding: 1px 8px;
  border-radius: 4px;
  background: var(--color-bg-elevated);
  border: 1px solid var(--color-border);
  color: var(--color-text);
}

/* ② 菜单 / ③ 权限 */
.perm-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 12px;
}

.perm-count {
  margin-left: 8px;
  font-size: 12px;
  font-weight: 400;
  color: var(--color-text-muted);
}

.perm-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  min-height: 40px;
}

.perm-tag {
  font-size: 13px;
}

.perm-item {
  color: var(--color-text);
}

/* 分组 */
.perm-group {
  margin-bottom: 14px;
}

.perm-group-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 8px;
}

.perm-group-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--color-primary);
}

.perm-group-count {
  font-size: 12px;
  font-weight: 400;
  color: var(--color-text-muted);
}

.perm-group-items {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
</style>
