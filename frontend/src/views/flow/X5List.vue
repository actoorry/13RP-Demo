<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import SearchBar from '../../components/common/SearchBar.vue'
import DataTable from '../../components/common/DataTable.vue'
import type { TableColumn } from '../../components/common/DataTable.vue'
import AnmaList from './AnmaList.vue'
import TaskList from './TaskList.vue'
import { flowX5Api } from '../../api/flow'
import type { FlowX5 } from '../../api/flow'

/** 流程引擎容器：X5 流程（默认）/ 安码流程 / 待办已办 */
const activeTab = ref('x5')

const loading = ref(false)
const list = ref<FlowX5[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const query = reactive<{ keyword?: string }>({})

const FLOW_TYPES = ['报销', '借款', '付款', '退款']
const STATUS_OPTIONS = [
  { label: '运行中', value: 'RUNNING' },
  { label: '完成', value: 'DONE' },
  { label: '驳回', value: 'REJECTED' },
]

function fmtMoney(v?: number): string {
  if (v == null || Number.isNaN(Number(v))) return '-'
  return Number(v).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
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
  { prop: 'flowType', label: '类型', width: '90px', slot: 'flowType' },
  { prop: 'title', label: '标题', minWidth: '160px' },
  { prop: 'amount', label: '金额', width: '120px', align: 'right', slot: 'amount' },
  { prop: 'applicant', label: '申请人', width: '100px' },
  { prop: 'currentStep', label: '当前步骤', width: '110px' },
  { prop: 'approver', label: '审批人', width: '100px' },
  { prop: 'status', label: '状态', width: '90px', slot: 'status' },
]

async function fetchList() {
  loading.value = true
  try {
    const data = await flowX5Api.list({ page: page.value, size: size.value, ...query })
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
async function handleApprove(row: FlowX5) {
  if (row.id == null) return
  try {
    await flowX5Api.approve(row.id)
    ElMessage.success('审批通过')
    fetchList()
  } catch {
    // 错误已由拦截器提示
  }
}

/** 驳回（RUNNING→REJECTED） */
async function handleReject(row: FlowX5) {
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
    await flowX5Api.reject(row.id)
    ElMessage.success('已驳回')
    fetchList()
  } catch {
    // 错误已由拦截器提示
  }
}

// ---------- 新增 / 编辑 ----------
const dialogVisible = ref(false)
const dialogTitle = ref('新增 X5 流程')
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<FlowX5>({
  flowNo: '',
  flowType: '报销',
  title: '',
  amount: undefined,
  applicant: '',
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
  dialogTitle.value = '新增 X5 流程'
  Object.assign(form, {
    id: undefined,
    flowNo: '',
    flowType: '报销',
    title: '',
    amount: undefined,
    applicant: '',
    currentStep: '审批中',
    approver: '',
    status: 'RUNNING',
  })
  dialogVisible.value = true
}

function openEdit(row: FlowX5) {
  dialogTitle.value = '编辑 X5 流程'
  Object.assign(form, {
    id: row.id,
    flowNo: row.flowNo ?? '',
    flowType: row.flowType || '报销',
    title: row.title ?? '',
    amount: row.amount,
    applicant: row.applicant ?? '',
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
      // 状态只能走操作栏「审批通过 / 驳回」流转，普通编辑提交体不含状态字段
      const { status, ...payload } = form
      await flowX5Api.update(form.id, payload)
      ElMessage.success('流程已更新')
    } else {
      await flowX5Api.create(form)
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

async function handleDelete(row: FlowX5) {
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
    await flowX5Api.remove(row.id as number)
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
      <!-- X5 流程（默认页） -->
      <el-tab-pane label="X5 流程" name="x5">
        <SearchBar @search="handleSearch" @reset="handleReset" @export="handleExport">
          <el-form-item label="关键词">
            <el-input
              v-model="query.keyword"
              placeholder="单号 / 标题 / 申请人"
              clearable
              style="width: 220px"
              @keyup.enter="handleSearch"
            />
          </el-form-item>
        </SearchBar>

        <div class="page-toolbar">
          <el-button type="primary" @click="openCreate">新增 X5 流程</el-button>
          <el-tag type="warning" size="small" effect="plain" class="risk-tip">
            500 元分级审批：借款 &gt;500 元需区域经理审批；500 元以下总部财务直接审批
          </el-tag>
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
          <template #amount="{ row }">
            <span class="mono">{{ fmtMoney(row.amount) }}</span>
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
      </el-tab-pane>

      <!-- 安码流程 -->
      <el-tab-pane label="安码流程" name="anma" lazy>
        <AnmaList />
      </el-tab-pane>

      <!-- 待办/已办 -->
      <el-tab-pane label="待办/已办" name="task" lazy>
        <TaskList />
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="单号" prop="flowNo">
          <el-input v-model="form.flowNo" placeholder="如：X5-20260805-001" />
        </el-form-item>
        <el-form-item label="类型" prop="flowType">
          <el-select v-model="form.flowType" style="width: 100%">
            <el-option v-for="t in FLOW_TYPES" :key="t" :value="t" :label="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="标题" />
        </el-form-item>
        <el-form-item label="金额">
          <el-input-number v-model="form.amount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="申请人">
          <el-input v-model="form.applicant" placeholder="申请人" />
        </el-form-item>
        <el-form-item label="当前步骤">
          <el-input v-model="form.currentStep" placeholder="当前步骤" />
        </el-form-item>
        <el-form-item label="审批人">
          <el-input v-model="form.approver" placeholder="当前审批人" />
        </el-form-item>
        <el-form-item label="状态">
          <el-tag :type="statusType(form.status)" size="small">{{ statusLabel(form.status) }}</el-tag>
          <span class="form-tip">状态通过操作栏「审批通过 / 驳回」流转</span>
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

.form-tip {
  margin-left: 10px;
  font-size: 12px;
  color: var(--color-text-muted);
}
</style>
