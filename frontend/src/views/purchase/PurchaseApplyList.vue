<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import SearchBar from '../../components/common/SearchBar.vue'
import DataTable from '../../components/common/DataTable.vue'
import type { TableColumn } from '../../components/common/DataTable.vue'
import { applyApi, inquiryApi } from '../../api/purchase'
import type { Inquiry, PurchaseApply } from '../../api/purchase'

const loading = ref(false)
const list = ref<PurchaseApply[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const query = reactive<{ keyword?: string; status?: string }>({})

/** 状态下拉/标签统一 { label: 中文, value: 英文常量 }（与后端状态机一致）。 */
const STATUS_OPTIONS = [
  { label: '待批准', value: 'PENDING_APPROVE' },
  { label: '已批准', value: 'APPROVED' },
  { label: '待复核', value: 'PENDING_REVIEW' },
  { label: '已复核', value: 'REVIEWED' },
]

/** 关联询价单列表（供新建表单选择，用询价单 id 提交）。 */
const inquiryOptions = ref<Inquiry[]>([])

const columns: TableColumn[] = [
  { prop: 'applyNo', label: '申请单号', width: '170px' },
  { prop: 'inquiryId', label: '引用询价单', width: '170px', slot: 'inquiryNo' },
  { prop: 'status', label: '状态', width: '100px', slot: 'status' },
  { prop: 'approver', label: '审批人', width: '110px' },
  { prop: 'approveTime', label: '审批时间', width: '170px' },
]

async function fetchList() {
  loading.value = true
  try {
    const data = await applyApi.list({ page: page.value, size: size.value, ...query })
    list.value = data?.list || []
    total.value = data?.total || 0
  } catch {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

async function loadInquiries() {
  try {
    const data = await inquiryApi.list({ page: 1, size: 100 })
    inquiryOptions.value = data?.list || []
  } catch {
    inquiryOptions.value = []
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

/** 通过 inquiryId 反查询价单号（后端仅存 inquiry_id）。 */
function inquiryNoOf(row: PurchaseApply): string {
  if (row.inquiryId == null) return '-'
  return inquiryOptions.value.find((i) => i.id === row.inquiryId)?.inquiryNo ?? `#${row.inquiryId}`
}

function statusLabel(status: string): string {
  return STATUS_OPTIONS.find((o) => o.value === status)?.label ?? status
}

function statusType(status: string): 'success' | 'warning' | 'primary' | 'info' {
  if (status === 'REVIEWED') return 'success'
  if (status === 'PENDING_REVIEW') return 'warning'
  if (status === 'APPROVED') return 'primary'
  return 'info'
}

// ---------- 新增 / 编辑 ----------
const dialogVisible = ref(false)
const dialogTitle = ref('新增采购申请')
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<PurchaseApply>({
  applyNo: '',
  inquiryId: undefined,
  status: 'PENDING_APPROVE',
  approver: '',
  approveTime: undefined,
})

const rules: FormRules = {
  applyNo: [{ required: true, message: '请输入申请单号', trigger: 'blur' }],
}

function openCreate() {
  dialogTitle.value = '新增采购申请'
  Object.assign(form, {
    id: undefined,
    applyNo: '',
    inquiryId: undefined,
    status: 'PENDING_APPROVE',
    approver: '',
    approveTime: undefined,
  })
  dialogVisible.value = true
}

function openEdit(row: PurchaseApply) {
  dialogTitle.value = '编辑采购申请'
  Object.assign(form, {
    id: row.id,
    applyNo: row.applyNo,
    inquiryId: row.inquiryId,
    status: row.status || 'PENDING_APPROVE',
    approver: row.approver ?? '',
    approveTime: row.approveTime ?? undefined,
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
      await applyApi.update(form.id, form)
      ElMessage.success('采购申请已更新')
    } else {
      await applyApi.create(form)
      ElMessage.success('采购申请已新增')
    }
    dialogVisible.value = false
    fetchList()
  } catch {
    // 错误已由拦截器提示
  } finally {
    saving.value = false
  }
}

/** 审批链流转：批准 → 送复核 → 复核通过。成功提示后刷新列表。 */
async function handleFlow(row: PurchaseApply, nextStatus: string, actionLabel: string) {
  if (row.id == null) return
  try {
    await applyApi.update(row.id, { status: nextStatus })
    ElMessage.success(`${actionLabel}成功`)
    fetchList()
  } catch {
    // 错误已由拦截器提示
  }
}

onMounted(() => {
  fetchList()
  loadInquiries()
})
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
          placeholder="申请单号"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
        />
      </el-form-item>
    </SearchBar>

    <div class="page-toolbar">
      <el-button type="primary" @click="openCreate">新增采购申请</el-button>
      <span class="page-tip">审批链：待审批（批准）→ 待复核（客服部复核批准 → 自动生成待审批订单）</span>
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
      <template #inquiryNo="{ row }">
        <span>{{ inquiryNoOf(row) }}</span>
      </template>
      <template #status="{ row }">
        <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
      </template>
      <template #actions>
        <el-table-column label="操作" width="180px" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button
              v-if="row.status === 'PENDING_APPROVE'"
              link
              type="primary"
              @click="handleFlow(row, 'APPROVED', '批准')"
            >
              批准
            </el-button>
            <el-button
              v-else-if="row.status === 'APPROVED'"
              link
              type="primary"
              @click="handleFlow(row, 'PENDING_REVIEW', '送复核')"
            >
              送复核
            </el-button>
            <el-button
              v-else-if="row.status === 'PENDING_REVIEW'"
              link
              type="primary"
              @click="handleFlow(row, 'REVIEWED', '复核通过')"
            >
              复核通过
            </el-button>
          </template>
        </el-table-column>
      </template>
    </DataTable>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="申请单号" prop="applyNo">
          <el-input v-model="form.applyNo" placeholder="如：CG-20260805-001" />
        </el-form-item>
        <el-form-item label="引用询价单">
          <el-select v-model="form.inquiryId" placeholder="选择关联的询价单" clearable style="width: 100%">
            <el-option
              v-for="inq in inquiryOptions"
              :key="inq.id"
              :value="inq.id"
              :label="`${inq.inquiryNo}（${inq.productName}）`"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option v-for="opt in STATUS_OPTIONS" :key="opt.value" :value="opt.value" :label="opt.label" />
          </el-select>
        </el-form-item>
        <el-form-item label="审批人">
          <el-input v-model="form.approver" placeholder="如：客服部复核人" />
        </el-form-item>
        <el-form-item label="审批时间">
          <el-date-picker
            v-model="form.approveTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="审批时间"
            style="width: 100%"
          />
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
