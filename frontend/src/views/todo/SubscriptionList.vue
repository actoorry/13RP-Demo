<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import SearchBar from '../../components/common/SearchBar.vue'
import DataTable from '../../components/common/DataTable.vue'
import type { TableColumn } from '../../components/common/DataTable.vue'
import PersonalList from './PersonalList.vue'
import { subscriptionApi } from '../../api/todo'
import type { TodoSubscription } from '../../api/todo'

/** 待办事宜容器：四板块订阅（默认）/ 个人待办 */
const activeTab = ref('subscription')

const loading = ref(false)
const list = ref<TodoSubscription[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const query = reactive<{ keyword?: string }>({})

const BOARD_TYPES = ['CRM', '采购', '销售', '财务']

/** configJson 可能为字符串或对象，统一格式化为 "键=值, 键=值" */
function configText(json?: string | Record<string, unknown>): string {
  if (json == null) return '-'
  let obj = json
  if (typeof obj === 'string') {
    try {
      obj = JSON.parse(obj) as Record<string, unknown>
    } catch {
      return obj
    }
  }
  const entries = Object.entries(obj)
  if (!entries.length) return '-'
  return entries.map(([k, v]) => `${k}=${v}`).join(', ')
}

function ownerText(row: TodoSubscription): string {
  return row.ownerId != null ? `#${row.ownerId}` : '-'
}

const columns: TableColumn[] = [
  { prop: 'boardType', label: '板块', width: '100px', slot: 'boardType' },
  { prop: 'subType', label: '订阅类型', minWidth: '160px' },
  { prop: 'configJson', label: '配置阀值', minWidth: '220px', slot: 'config' },
  { prop: 'ownerId', label: '订阅人', width: '100px', slot: 'owner' },
  { prop: 'enabled', label: '启用', width: '90px', slot: 'enabled' },
]

async function fetchList() {
  loading.value = true
  try {
    const data = await subscriptionApi.list({ page: page.value, size: size.value, ...query })
    list.value = data?.list || []
    total.value = data?.total || 0
  } catch {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  fetchList()
}

function handleReset() {
  query.keyword = ''
  page.value = 1
  fetchList()
}

function handleExport() {
  ElMessage.info('导出功能将在批次 2 提供')
}

function handlePageChange(p: number) {
  page.value = p
  fetchList()
}

function handleSizeChange(s: number) {
  size.value = s
  page.value = 1
  fetchList()
}

/** 行内启用/停用开关：直接更新 enabled */
async function handleToggleEnabled(row: TodoSubscription) {
  if (row.id == null) return
  try {
    await subscriptionApi.update(row.id, { enabled: row.enabled })
    ElMessage.success(row.enabled === 1 ? '订阅已启用' : '订阅已停用')
  } catch {
    fetchList()
  }
}

function handleEnabledChange(row: TodoSubscription, val: boolean | string | number) {
  row.enabled = val ? 1 : 0
  handleToggleEnabled(row)
}

// ---------- 新增 / 编辑 ----------
const dialogVisible = ref(false)
const dialogTitle = ref('新增订阅')
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<TodoSubscription>({
  boardType: '采购',
  subType: '',
  configJson: '',
  ownerId: undefined,
  enabled: 1,
})

const rules: FormRules = {
  boardType: [{ required: true, message: '请选择板块', trigger: 'change' }],
  subType: [{ required: true, message: '请输入订阅类型', trigger: 'blur' }],
}

function openCreate() {
  dialogTitle.value = '新增订阅'
  Object.assign(form, {
    id: undefined,
    boardType: '采购',
    subType: '',
    configJson: '',
    ownerId: undefined,
    enabled: 1,
  })
  dialogVisible.value = true
}

function openEdit(row: TodoSubscription) {
  dialogTitle.value = '编辑订阅'
  Object.assign(form, {
    id: row.id,
    boardType: row.boardType || '采购',
    subType: row.subType ?? '',
    configJson: typeof row.configJson === 'string' ? row.configJson : JSON.stringify(row.configJson ?? {}),
    ownerId: row.ownerId,
    enabled: row.enabled ?? 1,
  })
  dialogVisible.value = true
}

async function handleSave() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (form.id != null) {
      await subscriptionApi.update(form.id, form)
      ElMessage.success('订阅已更新')
    } else {
      await subscriptionApi.create(form)
      ElMessage.success('订阅已新增')
    }
    dialogVisible.value = false
    fetchList()
  } catch {
    // 错误已由拦截器提示
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: TodoSubscription) {
  try {
    await ElMessageBox.confirm(
      `确定删除「${row.boardType ?? ''}」板块的订阅「${row.subType ?? row.id}」吗？`,
      '删除确认',
      { type: 'warning' },
    )
  } catch {
    return
  }
  try {
    await subscriptionApi.remove(row.id as number)
    ElMessage.success('已删除')
    fetchList()
  } catch {
    // 错误已由拦截器提示
  }
}

onMounted(fetchList)
</script>

<template>
  <div class="page fade-up">
    <el-tabs v-model="activeTab" class="domain-tabs">
      <!-- 四板块订阅（默认页） -->
      <el-tab-pane label="四板块订阅" name="subscription">
        <SearchBar @search="handleSearch" @reset="handleReset" @export="handleExport">
          <el-form-item label="关键词">
            <el-input
              v-model="query.keyword"
              placeholder="板块 / 订阅类型"
              clearable
              style="width: 220px"
              @keyup.enter="handleSearch"
            />
          </el-form-item>
        </SearchBar>

        <div class="page-toolbar">
          <el-button type="primary" @click="openCreate">新增订阅</el-button>
          <span class="page-tip">四板块：CRM / 采购 / 销售 / 财务；配置阀值达到后触发待办提醒</span>
        </div>

        <DataTable
          :columns="columns"
          :data="list"
          :loading="loading"
          :total="total"
          :page="page"
          :size="size"
          @page-change="handlePageChange"
          @size-change="handleSizeChange"
        >
          <template #boardType="{ row }">
            <el-tag size="small">{{ row.boardType }}</el-tag>
          </template>
          <template #config="{ row }">
            <span class="mono">{{ configText(row.configJson) }}</span>
          </template>
          <template #owner="{ row }">
            <span>{{ ownerText(row) }}</span>
          </template>
          <template #enabled="{ row }">
            <el-switch
              :model-value="row.enabled === 1"
              @change="(val) => handleEnabledChange(row, val)"
            />
          </template>
          <template #actions>
            <el-table-column label="操作" width="140px" align="center">
              <template #default="{ row }">
                <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
                <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </template>
        </DataTable>
      </el-tab-pane>

      <!-- 个人待办 -->
      <el-tab-pane label="个人待办" name="personal" lazy>
        <PersonalList />
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="板块" prop="boardType">
          <el-select v-model="form.boardType" style="width: 100%">
            <el-option v-for="b in BOARD_TYPES" :key="b" :value="b" :label="b" />
          </el-select>
        </el-form-item>
        <el-form-item label="订阅类型" prop="subType">
          <el-input v-model="form.subType" placeholder="如：进项欠票 / 应收账款 / 库龄预警" />
        </el-form-item>
        <el-form-item label="配置阀值">
          <el-input v-model="form.configJson" placeholder='JSON，如 {"amount":10000,"days":7}' />
        </el-form-item>
        <el-form-item label="订阅人 ID">
          <el-input-number v-model="form.ownerId" :min="0" style="width: 100%" placeholder="订阅人 id" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.enabled" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.domain-tabs {
  margin-bottom: 4px;
}

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
</style>
