<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import SearchBar from '../../components/common/SearchBar.vue'
import DataTable from '../../components/common/DataTable.vue'
import type { TableColumn } from '../../components/common/DataTable.vue'
import { flowTaskApi } from '../../api/flow'
import type { FlowTask } from '../../api/flow'

const loading = ref(false)
const list = ref<FlowTask[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const query = reactive<{ keyword?: string }>({})

const STATUS_OPTIONS = [
  { label: '待办', value: 'PENDING' },
  { label: '已办', value: 'DONE' },
]

function statusLabel(v?: string): string {
  return STATUS_OPTIONS.find((o) => o.value === v)?.label ?? v ?? '-'
}

function statusType(v?: string): 'success' | 'warning' | 'info' | 'danger' {
  if (v === 'DONE') return 'success'
  if (v === 'PENDING') return 'warning'
  return 'info'
}

const columns: TableColumn[] = [
  { prop: 'instanceId', label: '实例', width: '100px', slot: 'instance' },
  { prop: 'stepName', label: '步骤', minWidth: '140px' },
  { prop: 'assignee', label: '办理人', width: '120px' },
  { prop: 'status', label: '状态', width: '100px', slot: 'status' },
  { prop: 'remark', label: '备注', minWidth: '180px' },
]

async function fetchList() {
  loading.value = true
  try {
    const data = await flowTaskApi.list({ page: page.value, size: size.value, ...query })
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

/** 办理（PENDING→DONE；多办理人任一通过即联动流程实例状态） */
async function handleComplete(row: FlowTask) {
  if (row.id == null) return
  try {
    await flowTaskApi.complete(row.id)
    ElMessage.success('办理完成')
    fetchList()
  } catch {
    // 错误已由拦截器提示
  }
}

// ---------- 新增 / 编辑 ----------
const dialogVisible = ref(false)
const dialogTitle = ref('新增待办任务')
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<FlowTask>({
  instanceId: undefined,
  stepName: '',
  assignee: '',
  status: 'PENDING',
  remark: '',
})

const rules: FormRules = {
  stepName: [{ required: true, message: '请输入步骤名称', trigger: 'blur' }],
}

function openCreate() {
  dialogTitle.value = '新增待办任务'
  Object.assign(form, {
    id: undefined,
    instanceId: undefined,
    stepName: '',
    assignee: '',
    status: 'PENDING',
    remark: '',
  })
  dialogVisible.value = true
}

function openEdit(row: FlowTask) {
  dialogTitle.value = '编辑待办任务'
  Object.assign(form, {
    id: row.id,
    instanceId: row.instanceId,
    stepName: row.stepName ?? '',
    assignee: row.assignee ?? '',
    status: row.status || 'PENDING',
    remark: row.remark ?? '',
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
      await flowTaskApi.update(form.id, form)
      ElMessage.success('待办任务已更新')
    } else {
      await flowTaskApi.create(form)
      ElMessage.success('待办任务已新增')
    }
    dialogVisible.value = false
    fetchList()
  } catch {
    // 错误已由拦截器提示
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: FlowTask) {
  try {
    await ElMessageBox.confirm(
      `确定删除待办任务「${row.stepName ?? row.id}」吗？`,
      '删除确认',
      { type: 'warning' },
    )
  } catch {
    return
  }
  try {
    await flowTaskApi.remove(row.id as number)
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
          placeholder="步骤 / 办理人"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
        />
      </el-form-item>
    </SearchBar>

    <div class="page-toolbar">
      <el-button type="primary" @click="openCreate">新增待办任务</el-button>
      <span class="page-tip">多办理人任一通过即联动流程实例状态（RUNNING→DONE）</span>
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
      <template #instance="{ row }">
        <span class="mono">#{{ row.instanceId ?? '-' }}</span>
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
              办理
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </template>
    </DataTable>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="实例 ID">
          <el-input-number v-model="form.instanceId" :min="0" style="width: 100%" placeholder="流程实例 id" />
        </el-form-item>
        <el-form-item label="步骤" prop="stepName">
          <el-input v-model="form.stepName" placeholder="如：部门审批" />
        </el-form-item>
        <el-form-item label="办理人">
          <el-input v-model="form.assignee" placeholder="办理人" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option v-for="opt in STATUS_OPTIONS" :key="opt.value" :value="opt.value" :label="opt.label" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="备注" />
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
