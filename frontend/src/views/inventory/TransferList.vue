<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import SearchBar from '../../components/common/SearchBar.vue'
import DataTable from '../../components/common/DataTable.vue'
import type { TableColumn } from '../../components/common/DataTable.vue'
import { inventoryTransferApi } from '../../api/inventory'
import type { InventoryTransfer } from '../../api/inventory'

const loading = ref(false)
const list = ref<InventoryTransfer[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const query = reactive<{ keyword?: string }>({})

const columns: TableColumn[] = [
  { prop: 'transferNo', label: '单号', width: '160px' },
  { prop: 'batchNo', label: '批次号', width: '150px' },
  { prop: 'qty', label: '实提数量', width: '110px', align: 'right' },
  { prop: 'targetLocation', label: '目标库位', minWidth: '140px' },
  { prop: 'status', label: '状态', width: '100px' },
]

async function fetchList() {
  loading.value = true
  try {
    const data = await inventoryTransferApi.list({ page: page.value, size: size.value, ...query })
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
const dialogTitle = ref('新增调拨单')
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<InventoryTransfer>({
  transferNo: '',
  batchNo: '',
  qty: undefined,
  targetLocation: '',
  status: 'CREATED',
})

const rules: FormRules = {
  transferNo: [{ required: true, message: '请输入调拨单号', trigger: 'blur' }],
}

function openCreate() {
  dialogTitle.value = '新增调拨单'
  Object.assign(form, {
    id: undefined,
    transferNo: '',
    batchNo: '',
    qty: undefined,
    targetLocation: '',
    status: 'CREATED',
  })
  dialogVisible.value = true
}

function openEdit(row: InventoryTransfer) {
  dialogTitle.value = '编辑调拨单'
  Object.assign(form, {
    id: row.id,
    transferNo: row.transferNo,
    batchNo: row.batchNo ?? '',
    qty: row.qty,
    targetLocation: row.targetLocation ?? '',
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
      await inventoryTransferApi.update(form.id, form)
      ElMessage.success('调拨单已更新')
    } else {
      await inventoryTransferApi.create(form)
      ElMessage.success('调拨单已新增')
    }
    dialogVisible.value = false
    fetchList()
  } catch {
    // 错误已由拦截器提示
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: InventoryTransfer) {
  try {
    await ElMessageBox.confirm(
      `确定删除调拨单「${row.transferNo ?? row.id}」吗？`,
      '删除确认',
      { type: 'warning' },
    )
  } catch {
    return
  }
  try {
    await inventoryTransferApi.remove(row.id as number)
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
          placeholder="单号 / 批次号"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
        />
      </el-form-item>
    </SearchBar>

    <div class="page-toolbar">
      <el-button type="primary" @click="openCreate">新增调拨单</el-button>
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
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="调拨单号" prop="transferNo">
          <el-input v-model="form.transferNo" placeholder="如：TR-20260805-01" />
        </el-form-item>
        <el-form-item label="批次号">
          <el-input v-model="form.batchNo" placeholder="批次号" />
        </el-form-item>
        <el-form-item label="实提数量">
          <el-input-number v-model="form.qty" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="目标库位">
          <el-input v-model="form.targetLocation" placeholder="如：A区-01 库位" />
        </el-form-item>
        <el-form-item label="状态">
          <el-input v-model="form.status" placeholder="CREATED" />
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
