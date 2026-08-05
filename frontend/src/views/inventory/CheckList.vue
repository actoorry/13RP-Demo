<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import SearchBar from '../../components/common/SearchBar.vue'
import DataTable from '../../components/common/DataTable.vue'
import type { TableColumn } from '../../components/common/DataTable.vue'
import { inventoryCheckApi } from '../../api/inventory'
import type { InventoryCheck } from '../../api/inventory'

const loading = ref(false)
const list = ref<InventoryCheck[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const query = reactive<{ keyword?: string }>({})

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
  { prop: 'checkNo', label: '单号', width: '160px' },
  { prop: 'batchNo', label: '批号', width: '150px' },
  { prop: 'actualQty', label: '实盘数量', width: '120px', align: 'right' },
  { prop: 'status', label: '状态', width: '100px', slot: 'status' },
]

async function fetchList() {
  loading.value = true
  try {
    const data = await inventoryCheckApi.list({ page: page.value, size: size.value, ...query })
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
const dialogTitle = ref('新增盘点单')
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<InventoryCheck>({
  checkNo: '',
  batchNo: '',
  actualQty: undefined,
  status: 'CREATED',
})

const rules: FormRules = {
  checkNo: [{ required: true, message: '请输入盘点单号', trigger: 'blur' }],
}

function openCreate() {
  dialogTitle.value = '新增盘点单'
  Object.assign(form, {
    id: undefined,
    checkNo: '',
    batchNo: '',
    actualQty: undefined,
    status: 'CREATED',
  })
  dialogVisible.value = true
}

function openEdit(row: InventoryCheck) {
  dialogTitle.value = '编辑盘点单'
  Object.assign(form, {
    id: row.id,
    checkNo: row.checkNo,
    batchNo: row.batchNo ?? '',
    actualQty: row.actualQty,
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
      await inventoryCheckApi.update(form.id, form)
      ElMessage.success('盘点单已更新')
    } else {
      await inventoryCheckApi.create(form)
      ElMessage.success('盘点单已新增')
    }
    dialogVisible.value = false
    fetchList()
  } catch {
    // 错误已由拦截器提示
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: InventoryCheck) {
  try {
    await ElMessageBox.confirm(
      `确定删除盘点单「${row.checkNo ?? row.id}」吗？`,
      '删除确认',
      { type: 'warning' },
    )
  } catch {
    return
  }
  try {
    await inventoryCheckApi.remove(row.id as number)
    ElMessage.success('已删除')
    fetchList()
  } catch {
    // 错误已由拦截器提示
  }
}

/** 状态流转：CREATED → APPROVED（批准）→ CHECKED（保管员审核）；按钮只在当前可流转状态显示（前置校验） */
async function handleFlow(row: InventoryCheck, nextStatus: string, action: string) {
  if (row.id == null) return
  try {
    await inventoryCheckApi.update(row.id, { status: nextStatus })
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
          placeholder="单号 / 批号"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
        />
      </el-form-item>
    </SearchBar>

    <div class="page-toolbar">
      <el-button type="primary" @click="openCreate">新增盘点单</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="460px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="盘点单号" prop="checkNo">
          <el-input v-model="form.checkNo" placeholder="如：CK-20260805-01" />
        </el-form-item>
        <el-form-item label="批号">
          <el-input v-model="form.batchNo" placeholder="批号" />
        </el-form-item>
        <el-form-item label="实盘数量">
          <el-input-number v-model="form.actualQty" :min="0" :precision="2" style="width: 100%" />
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
  margin-bottom: 14px;
}

.form-tip {
  margin-left: 10px;
  font-size: 12px;
  color: var(--color-text-muted);
}
</style>
