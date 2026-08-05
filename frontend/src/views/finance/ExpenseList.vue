<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import SearchBar from '../../components/common/SearchBar.vue'
import DataTable from '../../components/common/DataTable.vue'
import type { TableColumn } from '../../components/common/DataTable.vue'
import { financeExpenseApi } from '../../api/finance'
import type { FinanceExpense } from '../../api/finance'

const loading = ref(false)
const list = ref<FinanceExpense[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const query = reactive<{ keyword?: string; status?: string }>({})

/** 分摊状态统一 { label: 中文, value: 英文常量 } */
const ALLOCATE_OPTIONS = [
  { label: '未分摊', value: 'UNALLOCATED' },
  { label: '已分摊', value: 'ALLOCATED' },
]

function allocateLabel(v: string): string {
  return ALLOCATE_OPTIONS.find((o) => o.value === v)?.label ?? v
}

function allocateType(v: string): 'success' | 'warning' | 'primary' | 'info' {
  return v === 'ALLOCATED' ? 'success' : 'warning'
}

function fmtMoney(v?: number): string {
  if (v == null || Number.isNaN(Number(v))) return '-'
  return Number(v).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function customerText(row: FinanceExpense): string {
  return row.customerName || (row.customerId != null ? String(row.customerId) : '-')
}

/** 行标红：marked=1（兼容后端 TINYINT 映射为 Integer 或 Boolean） */
function rowClassName({ row }: { row: Record<string, unknown> }): string {
  const e = row as unknown as FinanceExpense
  return e.marked === 1 || e.marked === true ? 'row-warn' : ''
}

const columns: TableColumn[] = [
  { prop: 'expenseNo', label: '费用单号', width: '150px' },
  { prop: 'customerId', label: '客户', minWidth: '120px', slot: 'customer' },
  { prop: 'productName', label: '品名', minWidth: '120px' },
  { prop: 'amount', label: '金额', width: '120px', align: 'right', slot: 'amount' },
  { prop: 'taxAmount', label: '税额', width: '110px', align: 'right', slot: 'taxAmount' },
  { prop: 'allocateStatus', label: '分摊状态', width: '110px', slot: 'allocateStatus' },
]

async function fetchList() {
  loading.value = true
  try {
    const data = await financeExpenseApi.list({
      page: page.value,
      size: size.value,
      keyword: query.keyword,
      status: query.status,
    })
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
  query.status = undefined
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

// ---------- 新增 / 编辑 ----------
const dialogVisible = ref(false)
const dialogTitle = ref('新增费用')
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<FinanceExpense>({
  expenseNo: '',
  customerId: undefined,
  productName: '',
  amount: undefined,
  taxAmount: undefined,
  allocateType: '',
  allocateStatus: 'UNALLOCATED',
  marked: 0,
})

const rules: FormRules = {
  expenseNo: [{ required: true, message: '请输入费用单号', trigger: 'blur' }],
}

function openCreate() {
  dialogTitle.value = '新增费用'
  Object.assign(form, {
    id: undefined,
    expenseNo: '',
    customerId: undefined,
    productName: '',
    amount: undefined,
    taxAmount: undefined,
    allocateType: '',
    allocateStatus: 'UNALLOCATED',
    marked: 0,
  })
  dialogVisible.value = true
}

function openEdit(row: FinanceExpense) {
  dialogTitle.value = '编辑费用'
  Object.assign(form, {
    id: row.id,
    expenseNo: row.expenseNo,
    customerId: row.customerId,
    productName: row.productName ?? '',
    amount: row.amount,
    taxAmount: row.taxAmount,
    allocateType: row.allocateType ?? '',
    allocateStatus: row.allocateStatus || 'UNALLOCATED',
    marked: row.marked ?? 0,
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
      // 分摊状态只能走操作栏「分摊」流转，普通编辑提交体不含分摊状态字段
      const { allocateStatus, ...payload } = form
      await financeExpenseApi.update(form.id, payload)
      ElMessage.success('费用已更新')
    } else {
      await financeExpenseApi.create(form)
      ElMessage.success('费用已新增')
    }
    dialogVisible.value = false
    fetchList()
  } catch {
    // 错误已由拦截器提示
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: FinanceExpense) {
  try {
    await ElMessageBox.confirm(
      `确定删除费用单「${row.expenseNo ?? row.id}」吗？`,
      '删除确认',
      { type: 'warning' },
    )
  } catch {
    return
  }
  try {
    await financeExpenseApi.remove(row.id as number)
    ElMessage.success('已删除')
    fetchList()
  } catch {
    // 错误已由拦截器提示
  }
}

/** 分摊：UNALLOCATED → ALLOCATED；按钮只在 UNALLOCATED 显示（前置校验） */
async function handleAllocate(row: FinanceExpense) {
  if (row.id == null) return
  try {
    await financeExpenseApi.update(row.id, { allocateStatus: 'ALLOCATED' })
    ElMessage.success('分摊成功')
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
      <el-form-item label="分摊状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
          <el-option v-for="opt in ALLOCATE_OPTIONS" :key="opt.value" :value="opt.value" :label="opt.label" />
        </el-select>
      </el-form-item>
      <el-form-item label="关键词">
        <el-input
          v-model="query.keyword"
          placeholder="费用单号 / 品名"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
        />
      </el-form-item>
    </SearchBar>

    <div class="page-toolbar">
      <el-button type="primary" @click="openCreate">新增费用</el-button>
      <span class="page-tip">标记红色行为异常费用，未分摊可点击分摊</span>
    </div>

    <DataTable
      :columns="columns"
      :data="list"
      :loading="loading"
      :total="total"
      :page="page"
      :size="size"
      :row-class-name="rowClassName"
      @page-change="handlePageChange"
      @size-change="handleSizeChange"
    >
      <template #customer="{ row }">
        <span>{{ customerText(row) }}</span>
      </template>
      <template #amount="{ row }">
        <span class="mono">{{ fmtMoney(row.amount) }}</span>
      </template>
      <template #taxAmount="{ row }">
        <span class="mono">{{ fmtMoney(row.taxAmount) }}</span>
      </template>
      <template #allocateStatus="{ row }">
        <el-tag :type="allocateType(row.allocateStatus)" size="small">{{ allocateLabel(row.allocateStatus) }}</el-tag>
      </template>
      <template #actions>
        <el-table-column label="操作" width="180px" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button
              v-if="row.allocateStatus === 'UNALLOCATED'"
              link
              type="primary"
              @click="handleAllocate(row)"
            >
              分摊
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </template>
    </DataTable>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="费用单号" prop="expenseNo">
          <el-input v-model="form.expenseNo" placeholder="如：EX-20260805-01" />
        </el-form-item>
        <el-form-item label="客户 ID">
          <el-input-number v-model="form.customerId" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="品名">
          <el-input v-model="form.productName" placeholder="如：电解铜" />
        </el-form-item>
        <el-form-item label="金额">
          <el-input-number v-model="form.amount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="税额">
          <el-input-number v-model="form.taxAmount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="分摊类型">
          <el-input v-model="form.allocateType" placeholder="分摊类型" />
        </el-form-item>
        <el-form-item label="分摊状态">
          <el-tag :type="allocateType(form.allocateStatus)" size="small">{{ allocateLabel(form.allocateStatus) }}</el-tag>
          <span class="form-tip">状态通过操作栏「分摊」流转</span>
        </el-form-item>
        <el-form-item label="红色标记">
          <el-switch v-model="form.marked" :active-value="1" :inactive-value="0" />
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

.form-tip {
  margin-left: 10px;
  font-size: 12px;
  color: var(--color-text-muted);
}
</style>

<!-- 行标红样式需作用于 el-table 行内 td，使用非 scoped 全局类名（类名唯一，不影响其他页面） -->
<style>
.row-warn td.el-table__cell {
  background-color: color-mix(in srgb, var(--color-alert) 12%, transparent) !important;
}
</style>
