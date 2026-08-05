<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import SearchBar from '../../components/common/SearchBar.vue'
import DataTable from '../../components/common/DataTable.vue'
import type { TableColumn } from '../../components/common/DataTable.vue'
import ExpenseList from './ExpenseList.vue'
import InvoiceList from './InvoiceList.vue'
import LabFeeList from './LabFeeList.vue'
import ArApList from './ArApList.vue'
import { financeArrivalApi } from '../../api/finance'
import type { FinanceArrival } from '../../api/finance'

/** 财务域容器：到账公告（默认）/ 费用管理 / 发票管理 / 化验费 / 应收应付 */
const activeTab = ref('arrival')

const loading = ref(false)
const list = ref<FinanceArrival[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const query = reactive<{ keyword?: string }>({})

function fmtMoney(v?: number): string {
  if (v == null || Number.isNaN(Number(v))) return '-'
  return Number(v).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function accountText(row: FinanceArrival): string {
  return row.accountName || (row.accountId != null ? String(row.accountId) : '-')
}

function orgText(row: FinanceArrival): string {
  return row.orgName || (row.orgId != null ? String(row.orgId) : '-')
}

const columns: TableColumn[] = [
  { prop: 'accountId', label: '账套', width: '110px', slot: 'account' },
  { prop: 'orgId', label: '组织', width: '110px', slot: 'org' },
  { prop: 'amount', label: '金额', width: '140px', align: 'right', slot: 'amount' },
  { prop: 'arrivalTime', label: '到账时间', minWidth: '170px' },
  { prop: 'operator', label: '操作人', width: '110px' },
]

async function fetchList() {
  loading.value = true
  try {
    const data = await financeArrivalApi.list({ page: page.value, size: size.value, ...query })
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
const dialogTitle = ref('新增到账公告')
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<FinanceArrival>({
  accountId: undefined,
  orgId: undefined,
  amount: undefined,
  arrivalTime: '',
  operator: '',
})

const rules: FormRules = {
  amount: [{ required: true, message: '请输入到账金额', trigger: 'blur' }],
}

function openCreate() {
  dialogTitle.value = '新增到账公告'
  Object.assign(form, {
    id: undefined,
    accountId: undefined,
    orgId: undefined,
    amount: undefined,
    arrivalTime: '',
    operator: '',
  })
  dialogVisible.value = true
}

function openEdit(row: FinanceArrival) {
  dialogTitle.value = '编辑到账公告'
  Object.assign(form, {
    id: row.id,
    accountId: row.accountId,
    orgId: row.orgId,
    amount: row.amount,
    arrivalTime: row.arrivalTime,
    operator: row.operator ?? '',
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
      await financeArrivalApi.update(form.id, form)
      ElMessage.success('到账公告已更新')
    } else {
      await financeArrivalApi.create(form)
      ElMessage.success('到账公告已新增')
    }
    dialogVisible.value = false
    fetchList()
  } catch {
    // 错误已由拦截器提示
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: FinanceArrival) {
  try {
    await ElMessageBox.confirm(
      `确定删除操作人「${row.operator ?? row.id}」的到账公告吗？`,
      '删除确认',
      { type: 'warning' },
    )
  } catch {
    return
  }
  try {
    await financeArrivalApi.remove(row.id as number)
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
      <!-- 到账公告（默认页） -->
      <el-tab-pane label="到账公告" name="arrival">
        <SearchBar @search="handleSearch" @reset="handleReset" @export="handleExport">
          <el-form-item label="关键词">
            <el-input
              v-model="query.keyword"
              placeholder="操作人"
              clearable
              style="width: 220px"
              @keyup.enter="handleSearch"
            />
          </el-form-item>
        </SearchBar>

        <div class="page-toolbar">
          <el-button type="primary" @click="openCreate">新增到账公告</el-button>
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
          <template #account="{ row }">
            <span>{{ accountText(row) }}</span>
          </template>
          <template #org="{ row }">
            <span>{{ orgText(row) }}</span>
          </template>
          <template #amount="{ row }">
            <span class="mono">{{ fmtMoney(row.amount) }}</span>
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

      <el-tab-pane label="费用管理" name="expense" lazy>
        <ExpenseList />
      </el-tab-pane>
      <el-tab-pane label="发票管理" name="invoice" lazy>
        <InvoiceList />
      </el-tab-pane>
      <el-tab-pane label="化验费" name="lab-fee" lazy>
        <LabFeeList />
      </el-tab-pane>
      <el-tab-pane label="应收应付" name="ar-ap" lazy>
        <ArApList />
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="账套 ID">
          <el-input-number v-model="form.accountId" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="组织 ID">
          <el-input-number v-model="form.orgId" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="到账金额" prop="amount">
          <el-input-number v-model="form.amount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="到账时间">
          <el-date-picker v-model="form.arrivalTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="操作人">
          <el-input v-model="form.operator" placeholder="操作人" />
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
  margin-bottom: 14px;
}
</style>
