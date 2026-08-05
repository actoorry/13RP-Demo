<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import SearchBar from '../../components/common/SearchBar.vue'
import DataTable from '../../components/common/DataTable.vue'
import type { TableColumn } from '../../components/common/DataTable.vue'
import { inventoryBatchApi } from '../../api/inventory'
import type { InventoryBatch } from '../../api/inventory'

const loading = ref(false)
const list = ref<InventoryBatch[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const query = reactive<{ keyword?: string }>({})

const columns: TableColumn[] = [
  { prop: 'batchNo', label: '批号', width: '160px' },
  { prop: 'productName', label: '品名', minWidth: '140px' },
  { prop: 'createDate', label: '创建日期', width: '130px' },
  { prop: 'creator', label: '创建人', width: '110px' },
  { prop: 'remark', label: '备注', minWidth: '180px' },
]

async function fetchList() {
  loading.value = true
  try {
    const data = await inventoryBatchApi.list({ page: page.value, size: size.value, ...query })
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
const dialogTitle = ref('新增批号')
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<InventoryBatch>({
  batchNo: '',
  productName: '',
  createDate: '',
  creator: '',
  remark: '',
})

const rules: FormRules = {
  batchNo: [{ required: true, message: '请输入批号', trigger: 'blur' }],
}

function openCreate() {
  dialogTitle.value = '新增批号'
  Object.assign(form, {
    id: undefined,
    batchNo: '',
    productName: '',
    createDate: '',
    creator: '',
    remark: '',
  })
  dialogVisible.value = true
}

function openEdit(row: InventoryBatch) {
  dialogTitle.value = '编辑批号'
  Object.assign(form, {
    id: row.id,
    batchNo: row.batchNo,
    productName: row.productName ?? '',
    createDate: row.createDate,
    creator: row.creator ?? '',
    remark: row.remark ?? '',
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
      await inventoryBatchApi.update(form.id, form)
      ElMessage.success('批号已更新')
    } else {
      await inventoryBatchApi.create(form)
      ElMessage.success('批号已新增')
    }
    dialogVisible.value = false
    fetchList()
  } catch {
    // 错误已由拦截器提示
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: InventoryBatch) {
  try {
    await ElMessageBox.confirm(
      `确定删除批号「${row.batchNo ?? row.id}」吗？`,
      '删除确认',
      { type: 'warning' },
    )
  } catch {
    return
  }
  try {
    await inventoryBatchApi.remove(row.id as number)
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
          placeholder="批号 / 品名"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
        />
      </el-form-item>
    </SearchBar>

    <div class="page-toolbar">
      <el-button type="primary" @click="openCreate">新增批号</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="批号" prop="batchNo">
          <el-input v-model="form.batchNo" placeholder="如：B20260805-01" />
        </el-form-item>
        <el-form-item label="品名">
          <el-input v-model="form.productName" placeholder="如：电解铜" />
        </el-form-item>
        <el-form-item label="创建日期">
          <el-date-picker v-model="form.createDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="创建人">
          <el-input v-model="form.creator" placeholder="创建人" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="备注" />
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
