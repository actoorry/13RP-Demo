<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import SearchBar from '../../components/common/SearchBar.vue'
import DataTable from '../../components/common/DataTable.vue'
import type { TableColumn } from '../../components/common/DataTable.vue'
import { orderApi } from '../../api/purchase'
import type { PurchaseOrder } from '../../api/purchase'

const loading = ref(false)
const list = ref<PurchaseOrder[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const query = reactive<{ keyword?: string; status?: string }>({})

const SOURCE_OPTIONS = ['供应商活动', '销采部', '客服部']
const SETTLE_METHODS = ['现款后货', '先货后款']
/** 状态统一 { label: 中文, value: 英文常量 }；待付款/待入库取 settlementStatus 字段。 */
const STATUS_OPTIONS = [
  { label: '待审批', value: 'PENDING_APPROVE' },
  { label: '待付款', value: 'WAIT_PAY' },
  { label: '待入库', value: 'WAIT_INBOUND' },
]

const columns: TableColumn[] = [
  { prop: 'orderNo', label: '订单编号', width: '170px' },
  { prop: 'source', label: '来源', width: '110px' },
  { prop: 'settleMethod', label: '结算方式', width: '110px', slot: 'settleMethod' },
  { prop: 'supplierName', label: '供应商', minWidth: '150px' },
  { prop: 'payAmount', label: '金额', width: '120px', slot: 'payAmount' },
  { prop: 'status', label: '状态', width: '100px', slot: 'status' },
  { prop: 'creator', label: '制单人', width: '100px' },
]

async function fetchList() {
  loading.value = true
  try {
    const params: Record<string, unknown> = { page: page.value, size: size.value, keyword: query.keyword }
    // 待审批按 status 过滤；待付款/待入库按 settlementStatus 过滤
    if (query.status === 'PENDING_APPROVE') {
      params.status = 'PENDING_APPROVE'
    } else if (query.status) {
      params.settlementStatus = query.status
    }
    const data = await orderApi.list(params)
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

/** 综合 status + settlementStatus 得到展示状态（英文 value）。 */
function orderStatusValue(row: PurchaseOrder): string {
  if (row.status === 'PENDING_APPROVE') return 'PENDING_APPROVE'
  if (row.settlementStatus === 'WAIT_PAY') return 'WAIT_PAY'
  if (row.settlementStatus === 'WAIT_INBOUND') return 'WAIT_INBOUND'
  return row.status || '-'
}

function orderStatusLabel(row: PurchaseOrder): string {
  const v = orderStatusValue(row)
  return STATUS_OPTIONS.find((o) => o.value === v)?.label ?? v
}

function orderStatusType(row: PurchaseOrder): 'success' | 'warning' | 'primary' | 'info' {
  const v = orderStatusValue(row)
  if (v === 'WAIT_INBOUND') return 'success'
  if (v === 'WAIT_PAY') return 'warning'
  if (v === 'PENDING_APPROVE') return 'info'
  return 'primary'
}

// ---------- 新增 / 编辑 ----------
const dialogVisible = ref(false)
const dialogTitle = ref('新增订单')
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<PurchaseOrder>({
  orderNo: '',
  source: '销采部',
  settleMethod: '现款后货',
  supplierName: '',
  payAmount: undefined,
  status: 'PENDING_APPROVE',
  creator: '',
})

const rules: FormRules = {
  orderNo: [{ required: true, message: '请输入订单编号', trigger: 'blur' }],
  settleMethod: [{ required: true, message: '请选择结算方式', trigger: 'change' }],
}

function openCreate() {
  dialogTitle.value = '新增订单'
  Object.assign(form, {
    id: undefined,
    orderNo: '',
    source: '销采部',
    settleMethod: '现款后货',
    supplierName: '',
    payAmount: undefined,
    status: 'PENDING_APPROVE',
    creator: '',
  })
  dialogVisible.value = true
}

function openEdit(row: PurchaseOrder) {
  dialogTitle.value = '编辑订单'
  Object.assign(form, {
    id: row.id,
    orderNo: row.orderNo,
    source: row.source ?? '销采部',
    settleMethod: row.settleMethod || '现款后货',
    supplierName: row.supplierName ?? '',
    payAmount: row.payAmount,
    status: row.status || 'PENDING_APPROVE',
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
      await orderApi.update(form.id, form)
      ElMessage.success('订单已更新')
    } else {
      await orderApi.create(form)
      ElMessage.success('订单已新增')
    }
    dialogVisible.value = false
    fetchList()
  } catch {
    // 错误已由拦截器提示
  } finally {
    saving.value = false
  }
}

/** 审批：{status:'APPROVED'}，后端按结算方式自动分流（现款后货→WAIT_PAY / 先货后款→WAIT_INBOUND）。 */
async function handleApprove(row: PurchaseOrder) {
  if (row.id == null) return
  try {
    await orderApi.update(row.id, { status: 'APPROVED' })
    ElMessage.success('审批成功')
    fetchList()
  } catch {
    // 错误已由拦截器提示
  }
}

/** 付款：{action:'PAY'}，仅结算状态 WAIT_PAY（现款后货）时显示。 */
async function handlePay(row: PurchaseOrder) {
  if (row.id == null) return
  try {
    await orderApi.pay(row.id)
    ElMessage.success('付款成功')
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
          placeholder="订单编号 / 供应商"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
        />
      </el-form-item>
    </SearchBar>

    <div class="page-toolbar">
      <el-button type="primary" @click="openCreate">新增订单</el-button>
      <span class="page-tip">结算分流：现款后货 → 待付款 → 待入库；先货后款 → 自动到待入库</span>
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
      <template #settleMethod="{ row }">
        <el-tag :type="row.settleMethod === '现款后货' ? 'warning' : 'primary'" size="small">
          {{ row.settleMethod }}
        </el-tag>
      </template>
      <template #payAmount="{ row }">
        <span class="mono">{{ row.payAmount ?? '-' }}</span>
      </template>
      <template #status="{ row }">
        <el-tag :type="orderStatusType(row)" size="small">{{ orderStatusLabel(row) }}</el-tag>
      </template>
      <template #actions>
        <el-table-column label="操作" width="180px" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button
              v-if="row.status === 'PENDING_APPROVE'"
              link
              type="primary"
              @click="handleApprove(row)"
            >
              审批
            </el-button>
            <el-button
              v-else-if="row.settlementStatus === 'WAIT_PAY'"
              link
              type="primary"
              @click="handlePay(row)"
            >
              付款
            </el-button>
          </template>
        </el-table-column>
      </template>
    </DataTable>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="订单编号" prop="orderNo">
          <el-input v-model="form.orderNo" placeholder="如：PO-20260805-301" />
        </el-form-item>
        <el-form-item label="来源">
          <el-select v-model="form.source" style="width: 100%">
            <el-option v-for="s in SOURCE_OPTIONS" :key="s" :value="s" :label="s" />
          </el-select>
        </el-form-item>
        <el-form-item label="结算方式" prop="settleMethod">
          <el-radio-group v-model="form.settleMethod">
            <el-radio v-for="m in SETTLE_METHODS" :key="m" :value="m">{{ m }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="供应商">
          <el-input v-model="form.supplierName" placeholder="如：包头北方稀土矿业" />
        </el-form-item>
        <el-form-item label="金额">
          <el-input-number v-model="form.payAmount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option v-for="opt in STATUS_OPTIONS" :key="opt.value" :value="opt.value" :label="opt.label" />
          </el-select>
        </el-form-item>
        <el-form-item label="制单人">
          <el-input v-model="form.creator" placeholder="制单人" />
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
