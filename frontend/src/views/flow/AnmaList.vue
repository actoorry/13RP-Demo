<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import SearchBar from '../../components/common/SearchBar.vue'
import DataTable from '../../components/common/DataTable.vue'
import type { TableColumn } from '../../components/common/DataTable.vue'
import { flowAnmaApi } from '../../api/flow'
import type { FlowAnma } from '../../api/flow'

const loading = ref(false)
const list = ref<FlowAnma[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const query = reactive<{ keyword?: string }>({})

const FLOW_TYPES = ['合同', '财务审批']
const STATUS_OPTIONS = [
  { label: '运行中', value: 'RUNNING' },
  { label: '完成', value: 'DONE' },
  { label: '驳回', value: 'REJECTED' },
]

function fmtMoney(v?: number): string {
  if (v == null || Number.isNaN(Number(v))) return '-'
  return Number(v).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function nameText(name?: string, id?: number): string {
  return name || (id != null ? String(id) : '-')
}

function statusLabel(v?: string): string {
  return STATUS_OPTIONS.find((o) => o.value === v)?.label ?? v ?? '-'
}

function statusType(v?: string): 'success' | 'warning' | 'info' | 'danger' {
  if (v === 'DONE') return 'success'
  if (v === 'REJECTED') return 'danger'
  return 'info'
}

const columns: TableColumn[] = [
  { prop: 'flowNo', label: '单号', width: '170px' },
  { prop: 'flowType', label: '类型', width: '110px', slot: 'flowType' },
  { prop: 'title', label: '标题', minWidth: '160px' },
  { prop: 'contractAmount', label: '合同金额', width: '120px', align: 'right', slot: 'contractAmount' },
  { prop: 'supplierName', label: '供应商', minWidth: '130px', slot: 'supplier' },
  { prop: 'customerName', label: '客户', minWidth: '130px', slot: 'customer' },
  { prop: 'currentStep', label: '当前步骤', width: '110px' },
  { prop: 'approver', label: '审批人', width: '100px' },
  { prop: 'status', label: '状态', width: '90px', slot: 'status' },
]

async function fetchList() {
  loading.value = true
  try {
    const data = await flowAnmaApi.list({ page: page.value, size: size.value, ...query })
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

/** 审批通过（RUNNING→DONE） */
async function handleApprove(row: FlowAnma) {
  if (row.id == null) return
  try {
    await flowAnmaApi.approve(row.id)
    ElMessage.success('审批通过')
    fetchList()
  } catch {
    // 错误已由拦截器提示
  }
}

/** 驳回（RUNNING→REJECTED） */
async function handleReject(row: FlowAnma) {
  if (row.id == null) return
  try {
    await ElMessageBox.confirm(
      `确定驳回流程「${row.flowNo ?? row.id}」吗？`,
      '驳回确认',
      { type: 'warning' },
    )
  } catch {
    return
  }
  try {
    await flowAnmaApi.reject(row.id)
    ElMessage.success('已驳回')
    fetchList()
  } catch {
    // 错误已由拦截器提示
  }
}

// ---------- 新增 / 编辑 ----------
const dialogVisible = ref(false)
const dialogTitle = ref('新增安码流程')
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<FlowAnma>({
  flowNo: '',
  flowType: '合同',
  title: '',
  contractAmount: undefined,
  supplierId: undefined,
  customerId: undefined,
  currentStep: '审批中',
  approver: '',
  status: 'RUNNING',
})

const rules: FormRules = {
  flowNo: [{ required: true, message: '请输入流程单号', trigger: 'blur' }],
  flowType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
}

function openCreate() {
  dialogTitle.value = '新增安码流程'
  Object.assign(form, {
    id: undefined,
    flowNo: '',
    flowType: '合同',
    title: '',
    contractAmount: undefined,
    supplierId: undefined,
    customerId: undefined,
    currentStep: '审批中',
    approver: '',
    status: 'RUNNING',
  })
  dialogVisible.value = true
}

function openEdit(row: FlowAnma) {
  dialogTitle.value = '编辑安码流程'
  Object.assign(form, {
    id: row.id,
    flowNo: row.flowNo ?? '',
    flowType: row.flowType || '合同',
    title: row.title ?? '',
    contractAmount: row.contractAmount,
    supplierId: row.supplierId,
    customerId: row.customerId,
    currentStep: row.currentStep ?? '审批中',
    approver: row.approver ?? '',
    status: row.status || 'RUNNING',
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
      await flowAnmaApi.update(form.id, form)
      ElMessage.success('流程已更新')
    } else {
      await flowAnmaApi.create(form)
      ElMessage.success('流程已新增')
    }
    dialogVisible.value = false
    fetchList()
  } catch {
    // 错误已由拦截器提示
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: FlowAnma) {
  try {
    await ElMessageBox.confirm(
      `确定删除流程「${row.flowNo ?? row.id}」吗？`,
      '删除确认',
      { type: 'warning' },
    )
  } catch {
    return
  }
  try {
    await flowAnmaApi.remove(row.id as number)
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
          placeholder="单号 / 标题 / 供应商 / 客户"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
        />
      </el-form-item>
    </SearchBar>

    <div class="page-toolbar">
      <el-button type="primary" @click="openCreate">新增安码流程</el-button>
      <span class="page-tip">安码流程：合同审批 / 财务审批</span>
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
      <template #flowType="{ row }">
        <el-tag size="small">{{ row.flowType }}</el-tag>
      </template>
      <template #contractAmount="{ row }">
        <span class="mono">{{ fmtMoney(row.contractAmount) }}</span>
      </template>
      <template #supplier="{ row }">
        <span>{{ nameText(row.supplierName, row.supplierId) }}</span>
      </template>
      <template #customer="{ row }">
        <span>{{ nameText(row.customerName, row.customerId) }}</span>
      </template>
      <template #status="{ row }">
        <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
      </template>
      <template #actions>
        <el-table-column label="操作" width="200px" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button
              v-if="row.status === 'RUNNING'"
              link
              type="success"
              @click="handleApprove(row)"
            >
              审批通过
            </el-button>
            <el-button
              v-if="row.status === 'RUNNING'"
              link
              type="danger"
              @click="handleReject(row)"
            >
              驳回
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </template>
    </DataTable>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="单号" prop="flowNo">
          <el-input v-model="form.flowNo" placeholder="如：ANMA-20260805-001" />
        </el-form-item>
        <el-form-item label="类型" prop="flowType">
          <el-select v-model="form.flowType" style="width: 100%">
            <el-option v-for="t in FLOW_TYPES" :key="t" :value="t" :label="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="标题" />
        </el-form-item>
        <el-form-item label="合同金额">
          <el-input-number v-model="form.contractAmount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="供应商 ID">
          <el-input-number v-model="form.supplierId" :min="0" style="width: 100%" placeholder="供应商 id" />
        </el-form-item>
        <el-form-item label="客户 ID">
          <el-input-number v-model="form.customerId" :min="0" style="width: 100%" placeholder="客户 id" />
        </el-form-item>
        <el-form-item label="当前步骤">
          <el-input v-model="form.currentStep" placeholder="当前步骤" />
        </el-form-item>
        <el-form-item label="审批人">
          <el-input v-model="form.approver" placeholder="当前审批人" />
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
