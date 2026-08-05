<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import SearchBar from '../../components/common/SearchBar.vue'
import DataTable from '../../components/common/DataTable.vue'
import type { TableColumn } from '../../components/common/DataTable.vue'
import { personalApi } from '../../api/todo'
import type { TodoPersonal } from '../../api/todo'

const loading = ref(false)
const list = ref<TodoPersonal[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const query = reactive<{ keyword?: string }>({})

const TODO_TYPES = ['公共', '指派']
const TEMPLATE_TYPES = ['出库模板', '入库模板']
const STATUS_OPTIONS = [
  { label: '待办', value: 'PENDING' },
  { label: '已办', value: 'DONE' },
]

function fmtDateTime(v?: string): string {
  return v ? v.replace('T', ' ') : '-'
}

function statusLabel(v?: string): string {
  return STATUS_OPTIONS.find((o) => o.value === v)?.label ?? v ?? '-'
}

function statusType(v?: string): 'success' | 'warning' | 'info' | 'danger' {
  if (v === 'DONE') return 'success'
  if (v === 'PENDING') return 'warning'
  return 'info'
}

const columns: TableColumn[] = [
  { prop: 'todoType', label: '类型', width: '90px', slot: 'todoType' },
  { prop: 'templateType', label: '模板', width: '120px' },
  { prop: 'remindTime', label: '提醒时间', width: '170px', slot: 'remindTime' },
  { prop: 'assignee', label: '指派人员', width: '120px' },
  { prop: 'status', label: '状态', width: '90px', slot: 'status' },
]

async function fetchList() {
  loading.value = true
  try {
    const data = await personalApi.list({ page: page.value, size: size.value, ...query })
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

/** 完成（PENDING→DONE） */
async function handleComplete(row: TodoPersonal) {
  if (row.id == null) return
  try {
    await personalApi.complete(row.id)
    ElMessage.success('待办已完成')
    fetchList()
  } catch {
    // 错误已由拦截器提示
  }
}

// ---------- 新增 / 编辑 ----------
const dialogVisible = ref(false)
const dialogTitle = ref('新增个人待办')
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<TodoPersonal>({
  userId: undefined,
  todoType: '指派',
  templateType: '出库模板',
  remindTime: '',
  assignee: '',
  status: 'PENDING',
})

const rules: FormRules = {
  todoType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  templateType: [{ required: true, message: '请选择模板', trigger: 'change' }],
}

function openCreate() {
  dialogTitle.value = '新增个人待办'
  Object.assign(form, {
    id: undefined,
    userId: undefined,
    todoType: '指派',
    templateType: '出库模板',
    remindTime: '',
    assignee: '',
    status: 'PENDING',
  })
  dialogVisible.value = true
}

function openEdit(row: TodoPersonal) {
  dialogTitle.value = '编辑个人待办'
  Object.assign(form, {
    id: row.id,
    userId: row.userId,
    todoType: row.todoType || '指派',
    templateType: row.templateType || '出库模板',
    remindTime: row.remindTime ?? '',
    assignee: row.assignee ?? '',
    status: row.status || 'PENDING',
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
      await personalApi.update(form.id, form)
      ElMessage.success('个人待办已更新')
    } else {
      await personalApi.create(form)
      ElMessage.success('个人待办已新增')
    }
    dialogVisible.value = false
    fetchList()
  } catch {
    // 错误已由拦截器提示
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: TodoPersonal) {
  try {
    await ElMessageBox.confirm(
      `确定删除待办「${row.templateType ?? row.id}」吗？`,
      '删除确认',
      { type: 'warning' },
    )
  } catch {
    return
  }
  try {
    await personalApi.remove(row.id as number)
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
    <SearchBar @search="handleSearch" @reset="handleReset" @export="handleExport">
      <el-form-item label="关键词">
        <el-input
          v-model="query.keyword"
          placeholder="模板 / 指派人员"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
        />
      </el-form-item>
    </SearchBar>

    <div class="page-toolbar">
      <el-button type="primary" @click="openCreate">新增个人待办</el-button>
      <span class="page-tip">类型：公共/指派；完成即 PENDING→DONE</span>
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
      <template #todoType="{ row }">
        <el-tag size="small">{{ row.todoType }}</el-tag>
      </template>
      <template #remindTime="{ row }">
        <span>{{ fmtDateTime(row.remindTime) }}</span>
      </template>
      <template #status="{ row }">
        <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
      </template>
      <template #actions>
        <el-table-column label="操作" width="180px" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button
              v-if="row.status === 'PENDING'"
              link
              type="success"
              @click="handleComplete(row)"
            >
              完成
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </template>
    </DataTable>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="用户 ID">
          <el-input-number v-model="form.userId" :min="0" style="width: 100%" placeholder="用户 id" />
        </el-form-item>
        <el-form-item label="类型" prop="todoType">
          <el-select v-model="form.todoType" style="width: 100%">
            <el-option v-for="t in TODO_TYPES" :key="t" :value="t" :label="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="模板" prop="templateType">
          <el-select v-model="form.templateType" style="width: 100%">
            <el-option v-for="t in TEMPLATE_TYPES" :key="t" :value="t" :label="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="提醒时间">
          <el-date-picker
            v-model="form.remindTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="指派人员">
          <el-input v-model="form.assignee" placeholder="指派人员" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option v-for="opt in STATUS_OPTIONS" :key="opt.value" :value="opt.value" :label="opt.label" />
          </el-select>
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
