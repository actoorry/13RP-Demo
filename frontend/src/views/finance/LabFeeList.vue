<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import SearchBar from '../../components/common/SearchBar.vue'
import DataTable from '../../components/common/DataTable.vue'
import type { TableColumn } from '../../components/common/DataTable.vue'
import { financeLabFeeApi } from '../../api/finance'
import type { FinanceLabFee } from '../../api/finance'

const loading = ref(false)
const list = ref<FinanceLabFee[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const query = reactive<{ keyword?: string }>({})

/** 报告状态统一 { label: 中文, value: 英文常量 } */
const REPORT_OPTIONS = [
  { label: '未上传', value: 'PENDING' },
  { label: '合格', value: 'PASS' },
  { label: '不合格', value: 'FAIL' },
]

/** 付款状态统一 { label: 中文, value: 英文常量 } */
const PAY_OPTIONS = [
  { label: '未付款', value: 'UNPAID' },
  { label: '已付款', value: 'PAID' },
  { label: '已冲账', value: 'REIMBURSED' },
]

function reportLabel(v: string): string {
  return REPORT_OPTIONS.find((o) => o.value === v)?.label ?? v
}

function reportType(v: string): 'success' | 'warning' | 'primary' | 'info' | 'danger' {
  if (v === 'FAIL') return 'danger'
  if (v === 'PASS') return 'success'
  return 'warning'
}

function payLabel(v: string): string {
  return PAY_OPTIONS.find((o) => o.value === v)?.label ?? v
}

function payType(v: string): 'success' | 'warning' | 'primary' | 'info' {
  if (v === 'REIMBURSED') return 'success'
  if (v === 'PAID') return 'warning'
  return 'info'
}

function fmtMoney(v?: number): string {
  if (v == null || Number.isNaN(Number(v))) return '-'
  return Number(v).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const columns: TableColumn[] = [
  { prop: 'inboundId', label: '入库单', width: '100px', align: 'right' },
  { prop: 'labName', label: '化验机构', minWidth: '130px' },
  { prop: 'sampleNo', label: '样品编号', width: '130px' },
  { prop: 'element', label: '元素', width: '90px' },
  { prop: 'labFee', label: '化验费', width: '110px', align: 'right', slot: 'labFee' },
  { prop: 'reportStatus', label: '报告状态', width: '100px', slot: 'reportStatus' },
  { prop: 'payStatus', label: '付款状态', width: '100px', slot: 'payStatus' },
]

async function fetchList() {
  loading.value = true
  try {
    const data = await financeLabFeeApi.list({ page: page.value, size: size.value, ...query })
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
const dialogTitle = ref('新增化验费')
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<FinanceLabFee>({
  inboundId: undefined,
  labName: '',
  sampleNo: '',
  element: '',
  labFee: undefined,
  reportStatus: 'PENDING',
  payStatus: 'UNPAID',
  voucherNo: '',
})

const rules: FormRules = {
  sampleNo: [{ required: true, message: '请输入样品编号', trigger: 'blur' }],
}

function openCreate() {
  dialogTitle.value = '新增化验费'
  Object.assign(form, {
    id: undefined,
    inboundId: undefined,
    labName: '',
    sampleNo: '',
    element: '',
    labFee: undefined,
    reportStatus: 'PENDING',
    payStatus: 'UNPAID',
    voucherNo: '',
  })
  dialogVisible.value = true
}

function openEdit(row: FinanceLabFee) {
  dialogTitle.value = '编辑化验费'
  Object.assign(form, {
    id: row.id,
    inboundId: row.inboundId,
    labName: row.labName ?? '',
    sampleNo: row.sampleNo ?? '',
    element: row.element ?? '',
    labFee: row.labFee,
    reportStatus: row.reportStatus || 'PENDING',
    payStatus: row.payStatus || 'UNPAID',
    voucherNo: row.voucherNo ?? '',
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
      await financeLabFeeApi.update(form.id, form)
      ElMessage.success('化验费已更新')
    } else {
      await financeLabFeeApi.create(form)
      ElMessage.success('化验费已新增')
    }
    dialogVisible.value = false
    fetchList()
  } catch {
    // 错误已由拦截器提示
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: FinanceLabFee) {
  try {
    await ElMessageBox.confirm(
      `确定删除样品「${row.sampleNo ?? row.id}」的化验费记录吗？`,
      '删除确认',
      { type: 'warning' },
    )
  } catch {
    return
  }
  try {
    await financeLabFeeApi.remove(row.id as number)
    ElMessage.success('已删除')
    fetchList()
  } catch {
    // 错误已由拦截器提示
  }
}

/** 报告流转：PENDING → PASS / FAIL；按钮只在 PENDING 显示（前置校验） */
async function handleReport(row: FinanceLabFee, nextStatus: string, action: string) {
  if (row.id == null) return
  try {
    await financeLabFeeApi.update(row.id, { reportStatus: nextStatus })
    ElMessage.success(`${action}成功`)
    fetchList()
  } catch {
    // 错误已由拦截器提示
  }
}

/** 付款流转：UNPAID → PAID（付款）；PAID → REIMBURSED（冲账）；未付款不可冲账（前置校验） */
async function handlePay(row: FinanceLabFee, nextStatus: string, action: string) {
  if (row.id == null) return
  try {
    await financeLabFeeApi.update(row.id, { payStatus: nextStatus })
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
      <el-form-item label="关键词">
        <el-input
          v-model="query.keyword"
          placeholder="样品编号 / 化验机构"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
        />
      </el-form-item>
    </SearchBar>

    <div class="page-toolbar">
      <el-button type="primary" @click="openCreate">新增化验费</el-button>
      <span class="page-tip">报告：未上传 → 合格/不合格；付款：未付款 → 已付款 → 冲账</span>
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
      <template #labFee="{ row }">
        <span class="mono">{{ fmtMoney(row.labFee) }}</span>
      </template>
      <template #reportStatus="{ row }">
        <el-tag :type="reportType(row.reportStatus)" size="small">{{ reportLabel(row.reportStatus) }}</el-tag>
      </template>
      <template #payStatus="{ row }">
        <el-tag :type="payType(row.payStatus)" size="small">{{ payLabel(row.payStatus) }}</el-tag>
      </template>
      <template #actions>
        <el-table-column label="操作" width="260px" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <template v-if="row.reportStatus === 'PENDING'">
              <el-button link type="success" @click="handleReport(row, 'PASS', '合格')">合格</el-button>
              <el-button link type="danger" @click="handleReport(row, 'FAIL', '不合格')">不合格</el-button>
            </template>
            <el-button
              v-if="row.payStatus === 'UNPAID'"
              link
              type="primary"
              @click="handlePay(row, 'PAID', '付款')"
            >
              付款
            </el-button>
            <el-button
              v-else-if="row.payStatus === 'PAID'"
              link
              type="warning"
              @click="handlePay(row, 'REIMBURSED', '冲账')"
            >
              冲账
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </template>
    </DataTable>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="样品编号" prop="sampleNo">
          <el-input v-model="form.sampleNo" placeholder="样品编号" />
        </el-form-item>
        <el-form-item label="入库单 ID">
          <el-input-number v-model="form.inboundId" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="化验机构">
          <el-input v-model="form.labName" placeholder="如：XX检测中心" />
        </el-form-item>
        <el-form-item label="元素">
          <el-input v-model="form.element" placeholder="如：Cu" />
        </el-form-item>
        <el-form-item label="化验费">
          <el-input-number v-model="form.labFee" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="报告状态">
          <el-select v-model="form.reportStatus" style="width: 100%">
            <el-option v-for="opt in REPORT_OPTIONS" :key="opt.value" :value="opt.value" :label="opt.label" />
          </el-select>
        </el-form-item>
        <el-form-item label="付款状态">
          <el-select v-model="form.payStatus" style="width: 100%">
            <el-option v-for="opt in PAY_OPTIONS" :key="opt.value" :value="opt.value" :label="opt.label" />
          </el-select>
        </el-form-item>
        <el-form-item label="凭证号">
          <el-input v-model="form.voucherNo" placeholder="凭证号" />
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
