<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import SearchBar from '../../components/common/SearchBar.vue'
import DataTable from '../../components/common/DataTable.vue'
import type { TableColumn } from '../../components/common/DataTable.vue'
import DailyReportList from './DailyReportList.vue'
import InvoiceApplyList from './InvoiceApplyList.vue'
import { saleOrderApi } from '../../api/sale'
import type { SaleOrder } from '../../api/sale'

/** 销售域容器：销售明细（默认）/ 业务日报 / 开票申请，标签切换子页 */
const activeTab = ref('order')

const loading = ref(false)
const list = ref<SaleOrder[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const query = reactive<{ keyword?: string }>({})

function fmtMoney(v?: number): string {
  if (v == null || Number.isNaN(Number(v))) return '-'
  return Number(v).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function customerText(row: SaleOrder): string {
  return row.customerName || (row.customerId != null ? String(row.customerId) : '-')
}

const columns: TableColumn[] = [
  { prop: 'orderNo', label: '单号', width: '160px' },
  { prop: 'customerId', label: '客户', minWidth: '130px', slot: 'customer' },
  { prop: 'productName', label: '品名', minWidth: '130px' },
  { prop: 'qty', label: '数量', width: '90px', align: 'right' },
  { prop: 'amount', label: '金额', width: '120px', align: 'right', slot: 'amount' },
  { prop: 'profit', label: '利润', width: '110px', align: 'right', slot: 'profit' },
  { prop: 'cost', label: '成本', width: '110px', align: 'right', slot: 'cost' },
  { prop: 'fee', label: '费用', width: '110px', align: 'right', slot: 'fee' },
]

async function fetchList() {
  loading.value = true
  try {
    const data = await saleOrderApi.list({ page: page.value, size: size.value, ...query })
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
const dialogTitle = ref('新增销售明细')
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<SaleOrder>({
  orderNo: '',
  customerId: undefined,
  productName: '',
  qty: undefined,
  amount: undefined,
  profit: undefined,
  cost: undefined,
  fee: undefined,
})

const rules: FormRules = {
  orderNo: [{ required: true, message: '请输入单号', trigger: 'blur' }],
}

function openCreate() {
  dialogTitle.value = '新增销售明细'
  Object.assign(form, {
    id: undefined,
    orderNo: '',
    customerId: undefined,
    productName: '',
    qty: undefined,
    amount: undefined,
    profit: undefined,
    cost: undefined,
    fee: undefined,
  })
  dialogVisible.value = true
}

function openEdit(row: SaleOrder) {
  dialogTitle.value = '编辑销售明细'
  Object.assign(form, {
    id: row.id,
    orderNo: row.orderNo,
    customerId: row.customerId,
    productName: row.productName ?? '',
    qty: row.qty,
    amount: row.amount,
    profit: row.profit,
    cost: row.cost,
    fee: row.fee,
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
      await saleOrderApi.update(form.id, form)
      ElMessage.success('销售明细已更新')
    } else {
      await saleOrderApi.create(form)
      ElMessage.success('销售明细已新增')
    }
    dialogVisible.value = false
    fetchList()
  } catch {
    // 错误已由拦截器提示
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: SaleOrder) {
  try {
    await ElMessageBox.confirm(
      `确定删除单号「${row.orderNo ?? row.id}」的销售明细吗？`,
      '删除确认',
      { type: 'warning' },
    )
  } catch {
    return
  }
  try {
    await saleOrderApi.remove(row.id as number)
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
      <!-- 销售明细（默认页） -->
      <el-tab-pane label="销售明细" name="order">
        <SearchBar @search="handleSearch" @reset="handleReset" @export="handleExport">
          <el-form-item label="关键词">
            <el-input
              v-model="query.keyword"
              placeholder="单号 / 品名"
              clearable
              style="width: 220px"
              @keyup.enter="handleSearch"
            />
          </el-form-item>
        </SearchBar>

        <div class="page-toolbar">
          <el-button type="primary" @click="openCreate">新增销售明细</el-button>
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
          <template #amount="{ row }">
            <span class="mono">{{ fmtMoney(row.amount) }}</span>
          </template>
          <template #profit="{ row }">
            <span class="mono">{{ fmtMoney(row.profit) }}</span>
          </template>
          <template #cost="{ row }">
            <span class="mono">{{ fmtMoney(row.cost) }}</span>
          </template>
          <template #fee="{ row }">
            <span class="mono">{{ fmtMoney(row.fee) }}</span>
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

      <!-- 业务日报 -->
      <el-tab-pane label="业务日报" name="daily" lazy>
        <DailyReportList />
      </el-tab-pane>

      <!-- 开票申请 -->
      <el-tab-pane label="开票申请" name="invoice" lazy>
        <InvoiceApplyList />
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="单号" prop="orderNo">
          <el-input v-model="form.orderNo" placeholder="如：SO-20260805-301" />
        </el-form-item>
        <el-form-item label="客户 ID">
          <el-input-number v-model="form.customerId" :min="0" style="width: 100%" placeholder="客户 id" />
        </el-form-item>
        <el-form-item label="品名">
          <el-input v-model="form.productName" placeholder="如：电解铜" />
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="form.qty" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="金额">
          <el-input-number v-model="form.amount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="利润">
          <el-input-number v-model="form.profit" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="成本">
          <el-input-number v-model="form.cost" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="费用">
          <el-input-number v-model="form.fee" :min="0" :precision="2" style="width: 100%" />
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
