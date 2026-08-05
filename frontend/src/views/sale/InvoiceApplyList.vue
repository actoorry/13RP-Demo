<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import SearchBar from '../../components/common/SearchBar.vue'
import DataTable from '../../components/common/DataTable.vue'
import type { TableColumn } from '../../components/common/DataTable.vue'
import { saleInvoiceApplyApi } from '../../api/sale'
import type { SaleInvoiceApply } from '../../api/sale'

const loading = ref(false)
const list = ref<SaleInvoiceApply[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const query = reactive<{ keyword?: string; status?: string }>({})

/** 状态统一 { label: 中文, value: 英文常量 } */
const STATUS_OPTIONS = [
  { label: '申请', value: 'APPLIED' },
  { label: '待开', value: 'PENDING' },
  { label: '已开', value: 'ISSUED' },
]

function statusLabel(v: string): string {
  return STATUS_OPTIONS.find((o) => o.value === v)?.label ?? v
}

function statusType(v: string): 'success' | 'warning' | 'primary' | 'info' {
  if (v === 'ISSUED') return 'success'
  if (v === 'PENDING') return 'warning'
  return 'info'
}

function customerText(row: SaleInvoiceApply): string {
  return row.customerName || (row.customerId != null ? String(row.customerId) : '-')
}

const columns: TableColumn[] = [
  { prop: 'applyNo', label: '申请单号', width: '170px' },
  { prop: 'customerId', label: '客户', minWidth: '130px', slot: 'customer' },
  { prop: 'invoiceNo', label: '发票号', width: '160px' },
  { prop: 'status', label: '状态', width: '100px', slot: 'status' },
  { prop: 'creator', label: '申请人', width: '110px' },
]

async function fetchList() {
  loading.value = true
  try {
    const data = await saleInvoiceApplyApi.list({
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
const dialogTitle = ref('新增开票申请')
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<SaleInvoiceApply>({
  applyNo: '',
  customerId: undefined,
  invoiceNo: '',
  status: 'APPLIED',
  creator: '',
})

const rules: FormRules = {
  applyNo: [{ required: true, message: '请输入申请单号', trigger: 'blur' }],
}

function openCreate() {
  dialogTitle.value = '新增开票申请'
  Object.assign(form, {
    id: undefined,
    applyNo: '',
    customerId: undefined,
    invoiceNo: '',
    status: 'APPLIED',
    creator: '',
  })
  dialogVisible.value = true
}

function openEdit(row: SaleInvoiceApply) {
  dialogTitle.value = '编辑开票申请'
  Object.assign(form, {
    id: row.id,
    applyNo: row.applyNo,
    customerId: row.customerId,
    invoiceNo: row.invoiceNo ?? '',
    status: row.status || 'APPLIED',
    creator: row.creator ?? '',
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
      await saleInvoiceApplyApi.update(form.id, form)
      ElMessage.success('开票申请已更新')
    } else {
      await saleInvoiceApplyApi.create(form)
      ElMessage.success('开票申请已新增')
    }
    dialogVisible.value = false
    fetchList()
  } catch {
    // 错误已由拦截器提示
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: SaleInvoiceApply) {
  try {
    await ElMessageBox.confirm(
      `确定删除开票申请「${row.applyNo ?? row.id}」吗？`,
      '删除确认',
      { type: 'warning' },
    )
  } catch {
    return
  }
  try {
    await saleInvoiceApplyApi.remove(row.id as number)
    ElMessage.success('已删除')
    fetchList()
  } catch {
    // 错误已由拦截器提示
  }
}

/** 状态流转：APPLIED → PENDING（申请开票）→ ISSUED（确认已开）；按钮只在当前可流转状态显示（前置校验） */
async function handleFlow(row: SaleInvoiceApply, nextStatus: string, action: string) {
  if (row.id == null) return
  try {
    await saleInvoiceApplyApi.update(row.id, { status: nextStatus })
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
          placeholder="申请单号 / 发票号"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
        />
      </el-form-item>
    </SearchBar>

    <div class="page-toolbar">
      <el-button type="primary" @click="openCreate">新增开票申请</el-button>
      <span class="page-tip">状态流转：申请 → 待开 → 已开</span>
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
      <template #customer="{ row }">
        <span>{{ customerText(row) }}</span>
      </template>
      <template #status="{ row }">
        <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
      </template>
      <template #actions>
        <el-table-column label="操作" width="200px" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button
              v-if="row.status === 'APPLIED'"
              link
              type="primary"
              @click="handleFlow(row, 'PENDING', '申请开票')"
            >
              申请开票
            </el-button>
            <el-button
              v-else-if="row.status === 'PENDING'"
              link
              type="primary"
              @click="handleFlow(row, 'ISSUED', '确认已开')"
            >
              确认已开
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </template>
    </DataTable>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="申请单号" prop="applyNo">
          <el-input v-model="form.applyNo" placeholder="如：AP-20260805-01" />
        </el-form-item>
        <el-form-item label="客户 ID">
          <el-input-number v-model="form.customerId" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="发票号">
          <el-input v-model="form.invoiceNo" placeholder="已开票后填写" />
        </el-form-item>
        <el-form-item label="状态">
          <el-tag :type="statusType(form.status)" size="small">{{ statusLabel(form.status) }}</el-tag>
          <span class="form-tip">状态通过操作栏「申请开票 / 确认已开」流转</span>
        </el-form-item>
        <el-form-item label="申请人">
          <el-input v-model="form.creator" placeholder="申请人" />
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
