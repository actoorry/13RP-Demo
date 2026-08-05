<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import SearchBar from '../../components/common/SearchBar.vue'
import DataTable from '../../components/common/DataTable.vue'
import type { TableColumn } from '../../components/common/DataTable.vue'
import { financeArApApi } from '../../api/finance'
import type { FinanceArAp } from '../../api/finance'

const loading = ref(false)
const list = ref<FinanceArAp[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const query = reactive<{ keyword?: string }>({})

const PARTY_TYPES = [
  { label: '客户', value: 'CUSTOMER' },
  { label: '供应商', value: 'SUPPLIER' },
]

function partyTypeLabel(v: string): string {
  return PARTY_TYPES.find((o) => o.value === v)?.label ?? v
}

function partyTypeType(v: string): 'success' | 'warning' | 'primary' | 'info' {
  return v === 'SUPPLIER' ? 'warning' : 'primary'
}

function fmtMoney(v?: number): string {
  if (v == null || Number.isNaN(Number(v))) return '-'
  return Number(v).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function partyText(row: FinanceArAp): string {
  return row.partyName || (row.partyId != null ? String(row.partyId) : '-')
}

function accountText(row: FinanceArAp): string {
  return row.accountName || (row.accountId != null ? String(row.accountId) : '-')
}

function orgText(row: FinanceArAp): string {
  return row.orgName || (row.orgId != null ? String(row.orgId) : '-')
}

const columns: TableColumn[] = [
  { prop: 'partyType', label: '往来类型', width: '100px', slot: 'partyType' },
  { prop: 'partyId', label: '往来方', minWidth: '130px', slot: 'party' },
  { prop: 'accountId', label: '账套', width: '100px', slot: 'account' },
  { prop: 'orgId', label: '组织', width: '100px', slot: 'org' },
  { prop: 'receivable', label: '应收', width: '120px', align: 'right', slot: 'receivable' },
  { prop: 'payable', label: '应付', width: '120px', align: 'right', slot: 'payable' },
  { prop: 'balance', label: '余额', width: '120px', align: 'right', slot: 'balance' },
]

async function fetchList() {
  loading.value = true
  try {
    const data = await financeArApApi.list({ page: page.value, size: size.value, ...query })
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

// ---------- 新增 / 编辑 ----------
const dialogVisible = ref(false)
const dialogTitle = ref('新增应收应付')
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<FinanceArAp>({
  partyType: 'CUSTOMER',
  partyId: undefined,
  accountId: undefined,
  orgId: undefined,
  receivable: undefined,
  payable: undefined,
  balance: undefined,
})

const rules: FormRules = {}

function openCreate() {
  dialogTitle.value = '新增应收应付'
  Object.assign(form, {
    id: undefined,
    partyType: 'CUSTOMER',
    partyId: undefined,
    accountId: undefined,
    orgId: undefined,
    receivable: undefined,
    payable: undefined,
    balance: undefined,
  })
  dialogVisible.value = true
}

function openEdit(row: FinanceArAp) {
  dialogTitle.value = '编辑应收应付'
  Object.assign(form, {
    id: row.id,
    partyType: row.partyType ?? 'CUSTOMER',
    partyId: row.partyId,
    accountId: row.accountId,
    orgId: row.orgId,
    receivable: row.receivable,
    payable: row.payable,
    balance: row.balance,
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
      await financeArApApi.update(form.id, form)
      ElMessage.success('应收应付已更新')
    } else {
      await financeArApApi.create(form)
      ElMessage.success('应收应付已新增')
    }
    dialogVisible.value = false
    fetchList()
  } catch {
    // 错误已由拦截器提示
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: FinanceArAp) {
  try {
    await ElMessageBox.confirm(
      `确定删除往来方「${partyText(row)}」的应收应付记录吗？`,
      '删除确认',
      { type: 'warning' },
    )
  } catch {
    return
  }
  try {
    await financeArApApi.remove(row.id as number)
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
          placeholder="往来方 id / 账套 id"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
        />
      </el-form-item>
    </SearchBar>

    <div class="page-toolbar">
      <el-button type="primary" @click="openCreate">新增应收应付</el-button>
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
      <template #partyType="{ row }">
        <el-tag :type="partyTypeType(row.partyType)" size="small">{{ partyTypeLabel(row.partyType) }}</el-tag>
      </template>
      <template #party="{ row }">
        <span>{{ partyText(row) }}</span>
      </template>
      <template #account="{ row }">
        <span>{{ accountText(row) }}</span>
      </template>
      <template #org="{ row }">
        <span>{{ orgText(row) }}</span>
      </template>
      <template #receivable="{ row }">
        <span class="mono">{{ fmtMoney(row.receivable) }}</span>
      </template>
      <template #payable="{ row }">
        <span class="mono">{{ fmtMoney(row.payable) }}</span>
      </template>
      <template #balance="{ row }">
        <span class="mono">{{ fmtMoney(row.balance) }}</span>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="往来类型">
          <el-radio-group v-model="form.partyType">
            <el-radio v-for="t in PARTY_TYPES" :key="t.value" :value="t.value">{{ t.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="往来方 ID">
          <el-input-number v-model="form.partyId" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="账套 ID">
          <el-input-number v-model="form.accountId" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="组织 ID">
          <el-input-number v-model="form.orgId" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="应收">
          <el-input-number v-model="form.receivable" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="应付">
          <el-input-number v-model="form.payable" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="余额">
          <el-input-number v-model="form.balance" :precision="2" style="width: 100%" />
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
  margin-bottom: 14px;
}
</style>
