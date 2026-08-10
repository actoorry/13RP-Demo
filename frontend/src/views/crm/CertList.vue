<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import SearchBar from '../../components/common/SearchBar.vue'
import DataTable from '../../components/common/DataTable.vue'
import type { TableColumn } from '../../components/common/DataTable.vue'
import { certApi } from '../../api/crm'
import type { CrmCert } from '../../api/crm'

const loading = ref(false)
const list = ref<CrmCert[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const query = reactive<{ keyword?: string }>({})

const CERT_TYPES = ['营业执照', '组织机构代码证', '税务登记证', '法人身份证', '阳光协议']

function fmtMoney(v?: number): string {
  if (v == null || Number.isNaN(Number(v))) return '-'
  return Number(v).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function customerText(row: CrmCert): string {
  return row.customerName || (row.customerId != null ? String(row.customerId) : '-')
}

const columns: TableColumn[] = [
  { prop: 'customerName', label: '客户', minWidth: '150px', slot: 'customer' },
  { prop: 'certType', label: '证照类型', minWidth: '140px' },
  { prop: 'certImage', label: '证照图片', width: '90px', slot: 'certImage' },
  { prop: 'expireDate', label: '到期日期', width: '120px' },
  { prop: 'registeredCapital', label: '注册资本', width: '120px', align: 'right', slot: 'registeredCapital' },
  { prop: 'taxNo', label: '税号', minWidth: '150px' },
  { prop: 'verifiedFlag', label: '资料已核实', width: '110px', slot: 'verifiedFlag' },
  { prop: 'tradeAllowedFlag', label: '允许交易', width: '110px', slot: 'tradeAllowedFlag' },
]

async function fetchList() {
  loading.value = true
  try {
    const data = await certApi.list({ page: page.value, size: size.value, ...query })
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

/** 行内切换"允许交易"：仅资料已核实的证照可开启；后端亦会校验，此处做交互层提示 */
async function handleToggleTrade(row: CrmCert) {
  if (row.id == null) return
  if (row.verifiedFlag !== 1) {
    ElMessage.warning('资料未核实，禁止做业务单据')
    fetchList()
    return
  }
  try {
    await certApi.update(row.id, { tradeAllowedFlag: row.tradeAllowedFlag })
    ElMessage.success(row.tradeAllowedFlag === 1 ? '已允许交易' : '已禁止交易')
  } catch {
    fetchList()
  }
}

function handleTradeChange(row: CrmCert, val: boolean | string | number) {
  row.tradeAllowedFlag = val ? 1 : 0
  handleToggleTrade(row)
}

// ---------- 证照图片查看（Demo 占位图，不落库） ----------
const previewVisible = ref(false)
const previewRow = ref<CrmCert | null>(null)

function openPreview(row: CrmCert) {
  previewRow.value = row
  previewVisible.value = true
}

function fmtVerified(v?: number): string {
  return v === 1 ? '已核实' : '未核实'
}

function fmtTradeAllowed(v?: number): string {
  return v === 1 ? '允许' : '禁止'
}

// ---------- 新增 / 编辑 ----------
const dialogVisible = ref(false)
const dialogTitle = ref('新增证照')
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<CrmCert>({
  customerId: undefined,
  certType: '营业执照',
  expireDate: '',
  registeredCapital: undefined,
  taxNo: '',
  verifiedFlag: 0,
  tradeAllowedFlag: 0,
})

const rules: FormRules = {
  certType: [{ required: true, message: '请选择证照类型', trigger: 'change' }],
}

function openCreate() {
  dialogTitle.value = '新增证照'
  Object.assign(form, {
    id: undefined,
    customerId: undefined,
    certType: '营业执照',
    expireDate: '',
    registeredCapital: undefined,
    taxNo: '',
    verifiedFlag: 0,
    tradeAllowedFlag: 0,
  })
  dialogVisible.value = true
}

function openEdit(row: CrmCert) {
  dialogTitle.value = '编辑证照'
  Object.assign(form, {
    id: row.id,
    customerId: row.customerId,
    certType: row.certType || '营业执照',
    expireDate: row.expireDate ?? '',
    registeredCapital: row.registeredCapital,
    taxNo: row.taxNo ?? '',
    verifiedFlag: row.verifiedFlag ?? 0,
    tradeAllowedFlag: row.tradeAllowedFlag ?? 0,
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
      await certApi.update(form.id, form)
      ElMessage.success('证照已更新')
    } else {
      await certApi.create(form)
      ElMessage.success('证照已新增')
    }
    dialogVisible.value = false
    fetchList()
  } catch {
    // 错误已由拦截器提示
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: CrmCert) {
  try {
    await ElMessageBox.confirm(
      `确定删除客户「${row.customerName ?? row.customerId ?? ''}」的证照「${row.certType ?? ''}」吗？`,
      '删除确认',
      { type: 'warning' },
    )
  } catch {
    return
  }
  try {
    await certApi.remove(row.id as number)
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
          placeholder="客户 / 证照类型 / 税号"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
        />
      </el-form-item>
    </SearchBar>

    <div class="page-toolbar">
      <el-button type="primary" @click="openCreate">新增证照</el-button>
      <el-tag type="danger" size="small" effect="plain" class="risk-tip">
        风控提示：资料未核实的客户禁止做业务单据
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
      <template #customer="{ row }">
        <span>{{ customerText(row) }}</span>
      </template>
      <template #certImage="{ row }">
        <el-image
          :src="'/cert-placeholder.svg'"
          fit="cover"
          class="cert-thumb"
          @click="openPreview(row)"
        />
      </template>
      <template #registeredCapital="{ row }">
        <span class="mono">{{ fmtMoney(row.registeredCapital) }}</span>
      </template>
      <template #verifiedFlag="{ row }">
        <el-tag :type="row.verifiedFlag === 1 ? 'success' : 'danger'" size="small">
          {{ row.verifiedFlag === 1 ? '已核实' : '未核实' }}
        </el-tag>
      </template>
      <template #tradeAllowedFlag="{ row }">
        <el-tooltip
          :content="row.verifiedFlag !== 1 ? '资料未核实，禁止交易' : ''"
          placement="top"
          :disabled="row.verifiedFlag === 1"
        >
          <el-switch
            :model-value="row.tradeAllowedFlag === 1"
            :disabled="row.verifiedFlag !== 1"
            @change="(val) => handleTradeChange(row, val)"
          />
        </el-tooltip>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="客户 ID">
          <el-input-number v-model="form.customerId" :min="0" style="width: 100%" placeholder="客户 id" />
        </el-form-item>
        <el-form-item label="证照类型" prop="certType">
          <el-select v-model="form.certType" style="width: 100%">
            <el-option v-for="t in CERT_TYPES" :key="t" :value="t" :label="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="到期日期">
          <el-date-picker v-model="form.expireDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="注册资本">
          <el-input-number v-model="form.registeredCapital" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="税号">
          <el-input v-model="form.taxNo" placeholder="税号" />
        </el-form-item>
        <el-form-item label="资料已核实">
          <el-switch v-model="form.verifiedFlag" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="允许交易">
          <el-switch
            v-model="form.tradeAllowedFlag"
            :active-value="1"
            :inactive-value="0"
            :disabled="form.verifiedFlag !== 1"
          />
          <div class="form-tip">资料未核实的客户禁止做业务单据，需先核实资料后方可允许交易</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="previewVisible"
      :title="`证照查看 - ${previewRow?.certType ?? ''}`"
      width="640px"
      :close-on-click-modal="false"
    >
      <div class="preview-body">
        <el-image :src="'/cert-placeholder.svg'" fit="contain" class="preview-image" />
        <el-descriptions :column="1" border class="preview-desc">
          <el-descriptions-item label="客户">{{ previewRow ? customerText(previewRow) : '-' }}</el-descriptions-item>
          <el-descriptions-item label="证照类型">{{ previewRow?.certType ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="证照编号（税号）">{{ previewRow?.taxNo ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="到期日期">{{ previewRow?.expireDate ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="注册资本">{{ previewRow ? fmtMoney(previewRow.registeredCapital) : '-' }}</el-descriptions-item>
          <el-descriptions-item label="资料已核实">{{ previewRow ? fmtVerified(previewRow.verifiedFlag) : '-' }}</el-descriptions-item>
          <el-descriptions-item label="允许交易">{{ previewRow ? fmtTradeAllowed(previewRow.tradeAllowedFlag) : '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="previewVisible = false">关闭</el-button>
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

.form-tip {
  font-size: 12px;
  color: var(--color-text-muted);
  line-height: 1.6;
}

.cert-thumb {
  width: 48px;
  height: 34px;
  border-radius: 4px;
  cursor: pointer;
}

.preview-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.preview-image {
  width: 80%;
  max-width: 400px;
  border-radius: 6px;
}

.preview-desc {
  width: 100%;
}
</style>
