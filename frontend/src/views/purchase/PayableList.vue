<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import SearchBar from '../../components/common/SearchBar.vue'
import DataTable from '../../components/common/DataTable.vue'
import type { TableColumn } from '../../components/common/DataTable.vue'
import { payableApi } from '../../api/purchase'
import type { PurchasePayable } from '../../api/purchase'

const loading = ref(false)
const list = ref<PurchasePayable[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const query = reactive<{ keyword?: string; status?: string }>({})

/** 状态统一 { label: 中文, value: 英文常量 }（后端常量 OPEN/PAID）。 */
const STATUS_OPTIONS = [
  { label: '未支付', value: 'OPEN' },
  { label: '已支付', value: 'PAID' },
]

const columns: TableColumn[] = [
  { prop: 'id', label: 'ID', width: '70px' },
  { prop: 'supplierName', label: '供应商', minWidth: '180px' },
  { prop: 'balance', label: '应付余额', width: '130px', slot: 'balance' },
  { prop: 'dueDate', label: '到期日期', width: '130px' },
  { prop: 'status', label: '状态', width: '100px', slot: 'status' },
]

async function fetchList() {
  loading.value = true
  try {
    const data = await payableApi.list({ page: page.value, size: size.value, ...query })
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
const dialogTitle = ref('新增应付记录')
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<PurchasePayable>({
  supplierName: '',
  balance: undefined,
  dueDate: '',
  status: 'OPEN',
})

const rules: FormRules = {
  supplierName: [{ required: true, message: '请输入供应商', trigger: 'blur' }],
}

function openCreate() {
  dialogTitle.value = '新增应付记录'
  Object.assign(form, { id: undefined, supplierName: '', balance: undefined, dueDate: '', status: 'OPEN' })
  dialogVisible.value = true
}

function openEdit(row: PurchasePayable) {
  dialogTitle.value = '编辑应付记录'
  Object.assign(form, {
    id: row.id,
    supplierName: row.supplierName ?? '',
    balance: row.balance,
    dueDate: row.dueDate ?? '',
    status: row.status || 'OPEN',
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
      await payableApi.update(form.id, form)
      ElMessage.success('应付记录已更新')
    } else {
      await payableApi.create(form)
      ElMessage.success('应付记录已新增')
    }
    dialogVisible.value = false
    fetchList()
  } catch {
    // 错误已由拦截器提示
  } finally {
    saving.value = false
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
          placeholder="供应商"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
        />
      </el-form-item>
    </SearchBar>

    <div class="page-toolbar">
      <el-button type="primary" @click="openCreate">新增应付记录</el-button>
      <span class="page-tip">按品种负责人权限显示，匹配应付余额可直接生成付款单</span>
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
      <template #balance="{ row }">
        <span class="mono">{{ row.balance ?? '-' }}</span>
      </template>
      <template #status="{ row }">
        <el-tag :type="row.status === 'PAID' ? 'success' : 'warning'" size="small">
          {{ STATUS_OPTIONS.find((o) => o.value === row.status)?.label ?? row.status }}
        </el-tag>
      </template>
      <template #actions>
        <el-table-column label="操作" width="100px" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </template>
    </DataTable>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="供应商" prop="supplierName">
          <el-input v-model="form.supplierName" placeholder="如：宁波东方磁材" />
        </el-form-item>
        <el-form-item label="应付余额">
          <el-input-number v-model="form.balance" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="到期日期">
          <el-date-picker
            v-model="form.dueDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="到期日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option v-for="opt in STATUS_OPTIONS" :key="opt.value" :value="opt.value" :label="opt.label" />
          </el-select>
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
