<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import SearchBar from '../../components/common/SearchBar.vue'
import DataTable from '../../components/common/DataTable.vue'
import type { TableColumn } from '../../components/common/DataTable.vue'
import { inventoryOutboundApi } from '../../api/inventory'
import type { InventoryOutbound } from '../../api/inventory'

const loading = ref(false)
const list = ref<InventoryOutbound[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const query = reactive<{ keyword?: string; status?: string }>({})

const FREIGHT_BEARERS = ['博宇承担', '对方承担']

/** 状态统一 { label: 中文, value: 英文常量 } */
const STATUS_OPTIONS = [
  { label: '已制单', value: 'CREATED' },
  { label: '已批准', value: 'APPROVED' },
]

function statusLabel(v: string): string {
  return STATUS_OPTIONS.find((o) => o.value === v)?.label ?? v
}

function statusType(v: string): 'success' | 'warning' | 'primary' | 'info' {
  return v === 'APPROVED' ? 'success' : 'info'
}

const columns: TableColumn[] = [
  { prop: 'outboundNo', label: '单号', width: '160px' },
  { prop: 'saleOrderNo', label: '销售订单号', width: '150px' },
  { prop: 'productName', label: '品名', minWidth: '130px' },
  { prop: 'qty', label: '数量', width: '100px', align: 'right' },
  { prop: 'freightBearer', label: '运费承担方', width: '110px', slot: 'freightBearer' },
  { prop: 'carrier', label: '承运方', width: '110px' },
  { prop: 'plateNo', label: '车牌', width: '100px' },
  { prop: 'driver', label: '司机', width: '80px' },
  { prop: 'status', label: '状态', width: '90px', slot: 'status' },
]

async function fetchList() {
  loading.value = true
  try {
    const data = await inventoryOutboundApi.list({
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
const dialogTitle = ref('新增出库单')
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<InventoryOutbound>({
  outboundNo: '',
  saleOrderNo: '',
  productName: '',
  qty: undefined,
  freightBearer: '博宇承担',
  carrier: '',
  plateNo: '',
  driver: '',
  driverPhone: '',
  status: 'CREATED',
})

const rules: FormRules = {
  outboundNo: [{ required: true, message: '请输入出库单号', trigger: 'blur' }],
}

function openCreate() {
  dialogTitle.value = '新增出库单'
  Object.assign(form, {
    id: undefined,
    outboundNo: '',
    saleOrderNo: '',
    productName: '',
    qty: undefined,
    freightBearer: '博宇承担',
    carrier: '',
    plateNo: '',
    driver: '',
    driverPhone: '',
    status: 'CREATED',
  })
  dialogVisible.value = true
}

function openEdit(row: InventoryOutbound) {
  dialogTitle.value = '编辑出库单'
  Object.assign(form, {
    id: row.id,
    outboundNo: row.outboundNo,
    saleOrderNo: row.saleOrderNo ?? '',
    productName: row.productName ?? '',
    qty: row.qty,
    freightBearer: row.freightBearer ?? '博宇承担',
    carrier: row.carrier ?? '',
    plateNo: row.plateNo ?? '',
    driver: row.driver ?? '',
    driverPhone: row.driverPhone ?? '',
    status: row.status || 'CREATED',
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
      await inventoryOutboundApi.update(form.id, form)
      ElMessage.success('出库单已更新')
    } else {
      await inventoryOutboundApi.create(form)
      ElMessage.success('出库单已新增')
    }
    dialogVisible.value = false
    fetchList()
  } catch {
    // 错误已由拦截器提示
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: InventoryOutbound) {
  try {
    await ElMessageBox.confirm(
      `确定删除出库单「${row.outboundNo ?? row.id}」吗？`,
      '删除确认',
      { type: 'warning' },
    )
  } catch {
    return
  }
  try {
    await inventoryOutboundApi.remove(row.id as number)
    ElMessage.success('已删除')
    fetchList()
  } catch {
    // 错误已由拦截器提示
  }
}

/** 状态流转：CREATED → APPROVED；按钮只在 CREATED 显示（前置校验） */
async function handleApprove(row: InventoryOutbound) {
  if (row.id == null) return
  try {
    await inventoryOutboundApi.update(row.id, { status: 'APPROVED' })
    ElMessage.success('批准成功')
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
      <el-button type="primary" @click="openCreate">新增出库单</el-button>
      <span class="page-tip">状态机：制单 → 批准</span>
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
      <template #freightBearer="{ row }">
        <el-tag size="small" :type="row.freightBearer === '对方承担' ? 'warning' : 'primary'">
          {{ row.freightBearer || '-' }}
        </el-tag>
      </template>
      <template #status="{ row }">
        <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
      </template>
      <template #actions>
        <el-table-column label="操作" width="180px" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button
              v-if="row.status === 'CREATED'"
              link
              type="primary"
              @click="handleApprove(row)"
            >
              批准
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </template>
    </DataTable>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="出库单号" prop="outboundNo">
          <el-input v-model="form.outboundNo" placeholder="如：OB-20260805-01" />
        </el-form-item>
        <el-form-item label="销售订单号">
          <el-input v-model="form.saleOrderNo" placeholder="关联销售订单号" />
        </el-form-item>
        <el-form-item label="品名">
          <el-input v-model="form.productName" placeholder="如：电解铜" />
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="form.qty" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="运费承担方">
          <el-radio-group v-model="form.freightBearer">
            <el-radio v-for="b in FREIGHT_BEARERS" :key="b" :value="b">{{ b }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="承运方">
          <el-input v-model="form.carrier" placeholder="承运方" />
        </el-form-item>
        <el-form-item label="车牌">
          <el-input v-model="form.plateNo" maxlength="7" placeholder="≤7 位" />
        </el-form-item>
        <el-form-item label="司机">
          <el-input v-model="form.driver" maxlength="5" placeholder="≤5 位" />
        </el-form-item>
        <el-form-item label="司机电话">
          <el-input v-model="form.driverPhone" maxlength="11" placeholder="≤11 位" />
        </el-form-item>
        <el-form-item label="状态">
          <el-tag :type="statusType(form.status)" size="small">{{ statusLabel(form.status) }}</el-tag>
          <span class="form-tip">状态通过操作栏「批准」流转</span>
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
