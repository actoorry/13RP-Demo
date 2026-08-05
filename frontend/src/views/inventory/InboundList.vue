<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import SearchBar from '../../components/common/SearchBar.vue'
import DataTable from '../../components/common/DataTable.vue'
import type { TableColumn } from '../../components/common/DataTable.vue'
import { inventoryInboundApi } from '../../api/inventory'
import type { InventoryInbound } from '../../api/inventory'

const loading = ref(false)
const list = ref<InventoryInbound[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const query = reactive<{ keyword?: string; status?: string }>({})

const INBOUND_TYPES = ['估价', '代销', '内部']

/** 状态统一 { label: 中文, value: 英文常量 } */
const STATUS_OPTIONS = [
  { label: '制单完成', value: 'CREATED' },
  { label: '已批准', value: 'APPROVED' },
  { label: '已审核', value: 'CHECKED' },
]

function statusLabel(v: string): string {
  return STATUS_OPTIONS.find((o) => o.value === v)?.label ?? v
}

function statusType(v: string): 'success' | 'warning' | 'primary' | 'info' {
  if (v === 'CHECKED') return 'success'
  if (v === 'APPROVED') return 'warning'
  return 'info'
}

const columns: TableColumn[] = [
  { prop: 'inboundNo', label: '单号', width: '160px' },
  { prop: 'inboundType', label: '类型', width: '100px', slot: 'inboundType' },
  { prop: 'sourceOrderNo', label: '来源单号', width: '150px' },
  { prop: 'productName', label: '品名', minWidth: '130px' },
  { prop: 'qty', label: '数量', width: '100px', align: 'right' },
  { prop: 'status', label: '状态', width: '100px', slot: 'status' },
]

async function fetchList() {
  loading.value = true
  try {
    const data = await inventoryInboundApi.list({
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
const dialogTitle = ref('新增入库单')
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<InventoryInbound>({
  inboundNo: '',
  inboundType: '估价',
  sourceOrderNo: '',
  productName: '',
  qty: undefined,
  settleQty: undefined,
  status: 'CREATED',
  checker: '',
  auditLevel: '',
})

const rules: FormRules = {
  inboundNo: [{ required: true, message: '请输入入库单号', trigger: 'blur' }],
}

function openCreate() {
  dialogTitle.value = '新增入库单'
  Object.assign(form, {
    id: undefined,
    inboundNo: '',
    inboundType: '估价',
    sourceOrderNo: '',
    productName: '',
    qty: undefined,
    settleQty: undefined,
    status: 'CREATED',
    checker: '',
    auditLevel: '',
  })
  dialogVisible.value = true
}

function openEdit(row: InventoryInbound) {
  dialogTitle.value = '编辑入库单'
  Object.assign(form, {
    id: row.id,
    inboundNo: row.inboundNo,
    inboundType: row.inboundType ?? '估价',
    sourceOrderNo: row.sourceOrderNo ?? '',
    productName: row.productName ?? '',
    qty: row.qty,
    settleQty: row.settleQty,
    status: row.status || 'CREATED',
    checker: row.checker ?? '',
    auditLevel: row.auditLevel ?? '',
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
      // 状态/审核人/分级审核只能走操作栏「批准 / 保管员审核」流转，普通编辑提交体不含这些字段
      const { status, checker, auditLevel, ...payload } = form
      await inventoryInboundApi.update(form.id, payload)
      ElMessage.success('入库单已更新')
    } else {
      await inventoryInboundApi.create(form)
      ElMessage.success('入库单已新增')
    }
    dialogVisible.value = false
    fetchList()
  } catch {
    // 错误已由拦截器提示
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: InventoryInbound) {
  try {
    await ElMessageBox.confirm(
      `确定删除入库单「${row.inboundNo ?? row.id}」吗？`,
      '删除确认',
      { type: 'warning' },
    )
  } catch {
    return
  }
  try {
    await inventoryInboundApi.remove(row.id as number)
    ElMessage.success('已删除')
    fetchList()
  } catch {
    // 错误已由拦截器提示
  }
}

/** 状态流转：CREATED → APPROVED（批准）→ CHECKED（保管员审核）；按钮只在当前可流转状态显示（前置校验）。
 * 流转请求携带 auditLevel（批准时）与 checker（保管员审核时），避免后端收到 null 覆盖库中原值。 */
async function handleFlow(row: InventoryInbound, nextStatus: string, action: string) {
  if (row.id == null) return
  try {
    await inventoryInboundApi.update(row.id, {
      status: nextStatus,
      auditLevel: row.auditLevel ?? '',
      checker: row.checker ?? '',
    })
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
          placeholder="单号 / 品名"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
        />
      </el-form-item>
    </SearchBar>

    <div class="page-toolbar">
      <el-button type="primary" @click="openCreate">新增入库单</el-button>
      <span class="page-tip">状态机：制单完成 → 批准 → 保管员审核</span>
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
      <template #inboundType="{ row }">
        <el-tag size="small" :type="row.inboundType === '代销' ? 'warning' : row.inboundType === '内部' ? 'info' : 'primary'">
          {{ row.inboundType || '-' }}
        </el-tag>
      </template>
      <template #status="{ row }">
        <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
      </template>
      <template #actions>
        <el-table-column label="操作" width="220px" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button
              v-if="row.status === 'CREATED'"
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
              @click="handleFlow(row, 'CHECKED', '保管员审核')"
            >
              保管员审核
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </template>
    </DataTable>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="入库单号" prop="inboundNo">
          <el-input v-model="form.inboundNo" placeholder="如：IB-20260805-01" />
        </el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="form.inboundType">
            <el-radio v-for="t in INBOUND_TYPES" :key="t" :value="t">{{ t }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="来源单号">
          <el-input v-model="form.sourceOrderNo" placeholder="来源采购/销售单号" />
        </el-form-item>
        <el-form-item label="品名">
          <el-input v-model="form.productName" placeholder="如：电解铜" />
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="form.qty" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="账面结算量">
          <el-input-number v-model="form.settleQty" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="审核人">
          <el-input v-model="form.checker" placeholder="审核人" />
        </el-form-item>
        <el-form-item label="分级审核">
          <el-input v-model="form.auditLevel" placeholder="≤合理称差直接审核 / >合理称差总监经理审核" />
        </el-form-item>
        <el-form-item label="状态">
          <el-tag :type="statusType(form.status)" size="small">{{ statusLabel(form.status) }}</el-tag>
          <span class="form-tip">状态通过操作栏「批准 / 保管员审核」流转</span>
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
