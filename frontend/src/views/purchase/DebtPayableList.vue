<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import SearchBar from '../../components/common/SearchBar.vue'
import DataTable from '../../components/common/DataTable.vue'
import type { TableColumn } from '../../components/common/DataTable.vue'
import { debtApi } from '../../api/purchase'
import type { PurchaseDebt } from '../../api/purchase'

const loading = ref(false)
const list = ref<PurchaseDebt[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const query = reactive<{ keyword?: string; status?: string }>({})

/** 状态统一 { label: 中文, value: 英文常量 }（后端常量 OPEN/SETTLED）。 */
const STATUS_OPTIONS = [
  { label: '未关联', value: 'OPEN' },
  { label: '已核销', value: 'SETTLED' },
]

const columns: TableColumn[] = [
  { prop: 'id', label: 'ID', width: '70px' },
  { prop: 'inboundNo', label: '入库单号', minWidth: '160px' },
  { prop: 'invoiceNo', label: '发票号', minWidth: '150px' },
  { prop: 'amount', label: '金额', width: '120px', slot: 'amount' },
  { prop: 'status', label: '状态', width: '100px', slot: 'status' },
]

async function fetchList() {
  loading.value = true
  try {
    const data = await debtApi.list({ page: page.value, size: size.value, ...query })
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
const dialogTitle = ref('新增进项欠票')
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<PurchaseDebt>({
  inboundNo: '',
  invoiceNo: '',
  amount: undefined,
  status: 'OPEN',
})

const rules: FormRules = {
  inboundNo: [{ required: true, message: '请输入入库单号', trigger: 'blur' }],
}

function openCreate() {
  dialogTitle.value = '新增进项欠票'
  Object.assign(form, { id: undefined, inboundNo: '', invoiceNo: '', amount: undefined, status: 'OPEN' })
  dialogVisible.value = true
}

function openEdit(row: PurchaseDebt) {
  dialogTitle.value = '编辑进项欠票'
  Object.assign(form, {
    id: row.id,
    inboundNo: row.inboundNo ?? '',
    invoiceNo: row.invoiceNo ?? '',
    amount: row.amount,
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
      // 状态只能走业务流转（核销后自动置 SETTLED），普通编辑提交体不含状态字段
      const { status, ...payload } = form
      await debtApi.update(form.id, payload)
      ElMessage.success('欠票已更新')
    } else {
      await debtApi.create(form)
      ElMessage.success('欠票已新增')
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
          placeholder="入库单号 / 发票号"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
        />
      </el-form-item>
    </SearchBar>

    <div class="page-toolbar">
      <el-button type="primary" @click="openCreate">新增进项欠票</el-button>
      <span class="page-tip">审核后入库单自动生成虚拟发票，一入库单一欠票；数量金额一致可一键生成</span>
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
      <template #amount="{ row }">
        <span class="mono">{{ row.amount ?? '-' }}</span>
      </template>
      <template #status="{ row }">
        <el-tag :type="row.status === 'SETTLED' ? 'success' : 'info'" size="small">
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
        <el-form-item label="入库单号" prop="inboundNo">
          <el-input v-model="form.inboundNo" placeholder="如：RK-20260805-001" />
        </el-form-item>
        <el-form-item label="发票号">
          <el-input v-model="form.invoiceNo" placeholder="关联正式发票号" />
        </el-form-item>
        <el-form-item label="金额">
          <el-input-number v-model="form.amount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-tag :type="form.status === 'SETTLED' ? 'success' : 'info'" size="small">
            {{ STATUS_OPTIONS.find((o) => o.value === form.status)?.label ?? form.status }}
          </el-tag>
          <span class="form-tip">状态由系统维护</span>
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
