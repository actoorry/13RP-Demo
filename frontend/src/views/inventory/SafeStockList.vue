<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import SearchBar from '../../components/common/SearchBar.vue'
import DataTable from '../../components/common/DataTable.vue'
import type { TableColumn } from '../../components/common/DataTable.vue'
import { inventorySafeStockApi } from '../../api/inventory'
import type { InventorySafeStock } from '../../api/inventory'

const loading = ref(false)
const list = ref<InventorySafeStock[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const query = reactive<{ keyword?: string }>({})

function fmtQty(v?: number): string {
  if (v == null || Number.isNaN(Number(v))) return '-'
  return Number(v).toLocaleString('zh-CN', { maximumFractionDigits: 2 })
}

const columns: TableColumn[] = [
  { prop: 'productName', label: '品名', minWidth: '130px' },
  { prop: 'material', label: '材质', width: '100px' },
  { prop: 'serviceLevel', label: '有货率%', width: '90px', align: 'right', slot: 'serviceLevel' },
  { prop: 'zValue', label: 'Z值', width: '80px', align: 'right' },
  { prop: 'replenishCycle', label: '补货周期(天)', width: '110px', align: 'right' },
  { prop: 'economicQty', label: '经济量', width: '100px', align: 'right', slot: 'economicQty' },
  { prop: 'orderPointQty', label: '订货点', width: '100px', align: 'right', slot: 'orderPointQty' },
  { prop: 'maxQty', label: '最大', width: '100px', align: 'right', slot: 'maxQty' },
  { prop: 'safeStock', label: '安全库存', width: '100px', align: 'right', slot: 'safeStock' },
]

async function fetchList() {
  loading.value = true
  try {
    const data = await inventorySafeStockApi.list({ page: page.value, size: size.value, ...query })
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
const dialogTitle = ref('新增安全库存')
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<InventorySafeStock>({
  productName: '',
  material: '',
  serviceLevel: undefined,
  zValue: undefined,
  replenishCycle: undefined,
  economicQty: undefined,
  orderPointQty: undefined,
  maxQty: undefined,
  safeStock: undefined,
})

const rules: FormRules = {
  productName: [{ required: true, message: '请输入品名', trigger: 'blur' }],
}

function openCreate() {
  dialogTitle.value = '新增安全库存'
  Object.assign(form, {
    id: undefined,
    productName: '',
    material: '',
    serviceLevel: undefined,
    zValue: undefined,
    replenishCycle: undefined,
    economicQty: undefined,
    orderPointQty: undefined,
    maxQty: undefined,
    safeStock: undefined,
  })
  dialogVisible.value = true
}

function openEdit(row: InventorySafeStock) {
  dialogTitle.value = '编辑安全库存'
  Object.assign(form, {
    id: row.id,
    productName: row.productName ?? '',
    material: row.material ?? '',
    serviceLevel: row.serviceLevel,
    zValue: row.zValue,
    replenishCycle: row.replenishCycle,
    economicQty: row.economicQty,
    orderPointQty: row.orderPointQty,
    maxQty: row.maxQty,
    safeStock: row.safeStock,
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
      await inventorySafeStockApi.update(form.id, form)
      ElMessage.success('安全库存已更新')
    } else {
      await inventorySafeStockApi.create(form)
      ElMessage.success('安全库存已新增')
    }
    dialogVisible.value = false
    fetchList()
  } catch {
    // 错误已由拦截器提示
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: InventorySafeStock) {
  try {
    await ElMessageBox.confirm(
      `确定删除「${row.productName ?? row.id}」的安全库存吗？`,
      '删除确认',
      { type: 'warning' },
    )
  } catch {
    return
  }
  try {
    await inventorySafeStockApi.remove(row.id as number)
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
          placeholder="品名 / 材质"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
        />
      </el-form-item>
    </SearchBar>

    <div class="page-toolbar">
      <el-button type="primary" @click="openCreate">新增安全库存</el-button>
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
      <template #serviceLevel="{ row }">
        <span class="mono">{{ row.serviceLevel != null ? `${row.serviceLevel}%` : '-' }}</span>
      </template>
      <template #economicQty="{ row }">
        <span class="mono">{{ fmtQty(row.economicQty) }}</span>
      </template>
      <template #orderPointQty="{ row }">
        <span class="mono">{{ fmtQty(row.orderPointQty) }}</span>
      </template>
      <template #maxQty="{ row }">
        <span class="mono">{{ fmtQty(row.maxQty) }}</span>
      </template>
      <template #safeStock="{ row }">
        <span class="mono">{{ fmtQty(row.safeStock) }}</span>
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
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="品名" prop="productName">
          <el-input v-model="form.productName" placeholder="如：电解铜" />
        </el-form-item>
        <el-form-item label="材质">
          <el-input v-model="form.material" placeholder="如：铜" />
        </el-form-item>
        <el-form-item label="有货率(%)">
          <el-input-number v-model="form.serviceLevel" :min="0" :max="100" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="Z值">
          <el-input-number v-model="form.zValue" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="补货周期(天)">
          <el-input-number v-model="form.replenishCycle" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="经济补货量">
          <el-input-number v-model="form.economicQty" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="订货点量">
          <el-input-number v-model="form.orderPointQty" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="最大库存">
          <el-input-number v-model="form.maxQty" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="安全库存">
          <el-input-number v-model="form.safeStock" :min="0" :precision="2" style="width: 100%" />
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
