<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import SearchBar from '../../components/common/SearchBar.vue'
import DataTable from '../../components/common/DataTable.vue'
import type { TableColumn } from '../../components/common/DataTable.vue'
import { financeInvoiceApi } from '../../api/finance'
import type { FinanceInvoice } from '../../api/finance'

const loading = ref(false)
const list = ref<FinanceInvoice[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const query = reactive<{ keyword?: string; status?: string }>({})

const INVOICE_TYPES = ['进项', '销项']

/** 状态统一 { label: 中文, value: 英文常量 } */
const STATUS_OPTIONS = [
  { label: '已新增', value: 'CREATED' },
  { label: '已审核', value: 'APPROVED' },
  { label: '已作废', value: 'VOID' },
]

function statusLabel(v: string): string {
  return STATUS_OPTIONS.find((o) => o.value === v)?.label ?? v
}

function statusType(v: string): 'success' | 'warning' | 'primary' | 'info' | 'danger' {
  if (v === 'VOID') return 'danger'
  if (v === 'APPROVED') return 'success'
  return 'info'
}

function fmtMoney(v?: number): string {
  if (v == null || Number.isNaN(Number(v))) return '-'
  return Number(v).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function customerText(row: FinanceInvoice): string {
  return row.customerName || (row.customerId != null ? String(row.customerId) : '-')
}

const columns: TableColumn[] = [
  { prop: 'invoiceNo', label: '发票号', width: '160px' },
  { prop: 'invoiceType', label: '类型', width: '90px', slot: 'invoiceType' },
  { prop: 'customerId', label: '客户', minWidth: '120px', slot: 'customer' },
  { prop: 'productName', label: '品名', minWidth: '120px' },
  { prop: 'amount', label: '金额', width: '120px', align: 'right', slot: 'amount' },
  { prop: 'status', label: '状态', width: '100px', slot: 'status' },
]

async function fetchList() {
  loading.value = true
  try {
    const data = await financeInvoiceApi.list({
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
const dialogTitle = ref('新增发票')
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<FinanceInvoice>({
  invoiceNo: '',
  invoiceType: '进项',
  customerId: undefined,
  productCode: '',
  productName: '',
  amount: undefined,
  status: 'CREATED',
  auditor: '',
})

const rules: FormRules = {
  invoiceNo: [{ required: true, message: '请输入发票号', trigger: 'blur' }],
}

function openCreate() {
  dialogTitle.value = '新增发票'
  Object.assign(form, {
    id: undefined,
    invoiceNo: '',
    invoiceType: '进项',
    customerId: undefined,
    productCode: '',
    productName: '',
    amount: undefined,
    status: 'CREATED',
    auditor: '',
  })
  dialogVisible.value = true
}

function openEdit(row: FinanceInvoice) {
  dialogTitle.value = '编辑发票'
  Object.assign(form, {
    id: row.id,
    invoiceNo: row.invoiceNo,
    invoiceType: row.invoiceType ?? '进项',
    customerId: row.customerId,
    productCode: row.productCode ?? '',
    productName: row.productName ?? '',
    amount: row.amount,
    status: row.status || 'CREATED',
    auditor: row.auditor ?? '',
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
      // 状态只能走操作栏「审核 / 反审核 / 作废」流转，普通编辑提交体不含状态字段
      const { status, ...payload } = form
      await financeInvoiceApi.update(form.id, payload)
      ElMessage.success('发票已更新')
    } else {
      await financeInvoiceApi.create(form)
      ElMessage.success('发票已新增')
    }
    dialogVisible.value = false
    fetchList()
  } catch {
    // 错误已由拦截器提示
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: FinanceInvoice) {
  try {
    await ElMessageBox.confirm(
      `确定删除发票「${row.invoiceNo ?? row.id}」吗？`,
      '删除确认',
      { type: 'warning' },
    )
  } catch {
    return
  }
  try {
    await financeInvoiceApi.remove(row.id as number)
    ElMessage.success('已删除')
    fetchList()
  } catch {
    // 错误已由拦截器提示
  }
}

/** 状态流转：CREATED 审核 → APPROVED；APPROVED 反审核 → CREATED / 作废 → VOID；按钮只在可流转状态显示（前置校验） */
async function handleFlow(row: FinanceInvoice, nextStatus: string, action: string) {
  if (row.id == null) return
  try {
    await financeInvoiceApi.update(row.id, { status: nextStatus })
    ElMessage.success(`${action}成功`)
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
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
          <el-option v-for="opt in STATUS_OPTIONS" :key="opt.value" :value="opt.value" :label="opt.label" />
        </el-select>
      </el-form-item>
      <el-form-item label="关键词">
        <el-input
          v-model="query.keyword"
          placeholder="发票号 / 品名"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
        />
      </el-form-item>
    </SearchBar>

    <div class="page-toolbar">
      <el-button type="primary" @click="openCreate">新增发票</el-button>
      <span class="page-tip">状态机：新增 → 审核 → 作废；已审核可反审核，作废后不可再操作</span>
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
      <template #invoiceType="{ row }">
        <el-tag size="small" :type="row.invoiceType === '销项' ? 'warning' : 'primary'">
          {{ row.invoiceType || '-' }}
        </el-tag>
      </template>
      <template #customer="{ row }">
        <span>{{ customerText(row) }}</span>
      </template>
      <template #amount="{ row }">
        <span class="mono">{{ fmtMoney(row.amount) }}</span>
      </template>
      <template #status="{ row }">
        <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
      </template>
      <template #actions>
        <el-table-column label="操作" width="240px" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button
              v-if="row.status === 'CREATED'"
              link
              type="primary"
              @click="handleFlow(row, 'APPROVED', '审核')"
            >
              审核
            </el-button>
            <template v-else-if="row.status === 'APPROVED'">
              <el-button link type="warning" @click="handleFlow(row, 'CREATED', '反审核')">反审核</el-button>
              <el-button link type="danger" @click="handleFlow(row, 'VOID', '作废')">作废</el-button>
            </template>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </template>
    </DataTable>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="发票号" prop="invoiceNo">
          <el-input v-model="form.invoiceNo" placeholder="如：INV-20260805-01" />
        </el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="form.invoiceType">
            <el-radio v-for="t in INVOICE_TYPES" :key="t" :value="t">{{ t }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="客户 ID">
          <el-input-number v-model="form.customerId" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="商品编码">
          <el-input v-model="form.productCode" placeholder="商品编码" />
        </el-form-item>
        <el-form-item label="品名">
          <el-input v-model="form.productName" placeholder="如：电解铜" />
        </el-form-item>
        <el-form-item label="金额">
          <el-input-number v-model="form.amount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-tag :type="statusType(form.status)" size="small">{{ statusLabel(form.status) }}</el-tag>
          <span class="form-tip">状态通过操作栏「审核 / 反审核 / 作废」流转</span>
        </el-form-item>
        <el-form-item label="审核人">
          <el-input v-model="form.auditor" placeholder="审核人" />
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
