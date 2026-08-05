<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import SearchBar from '../../components/common/SearchBar.vue'
import DataTable from '../../components/common/DataTable.vue'
import type { TableColumn } from '../../components/common/DataTable.vue'
import { varietyApi } from '../../api/crm'
import type { CrmVariety } from '../../api/crm'

const loading = ref(false)
const list = ref<CrmVariety[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const query = reactive<{ keyword?: string }>({})

const VARIETY_TYPES = ['使用', '生产', '经营']

function fmtQty(v?: number): string {
  if (v == null || Number.isNaN(Number(v))) return '-'
  return Number(v).toLocaleString('zh-CN', { maximumFractionDigits: 2 })
}

function customerText(row: CrmVariety): string {
  return row.customerName || (row.customerId != null ? String(row.customerId) : '-')
}

const columns: TableColumn[] = [
  { prop: 'customerName', label: '客户', minWidth: '150px', slot: 'customer' },
  { prop: 'varietyType', label: '类型', width: '90px', slot: 'varietyType' },
  { prop: 'productName', label: '品名', minWidth: '130px' },
  { prop: 'grade', label: '牌号', width: '110px' },
  { prop: 'material', label: '材质', width: '100px' },
  { prop: 'spec', label: '规格', width: '120px' },
  { prop: 'monthlyQty', label: '月用量', width: '110px', align: 'right', slot: 'monthlyQty' },
  { prop: 'nextMonthPlan', label: '下月计划', width: '110px', align: 'right', slot: 'nextMonthPlan' },
  { prop: 'competitor', label: '竞争对手', minWidth: '140px' },
]

async function fetchList() {
  loading.value = true
  try {
    const data = await varietyApi.list({ page: page.value, size: size.value, ...query })
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
const dialogTitle = ref('新增品种资料')
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<CrmVariety>({
  customerId: undefined,
  varietyType: '使用',
  productName: '',
  grade: '',
  material: '',
  spec: '',
  brandOrigin: '',
  competitor: '',
  swot: '',
  monthlyQty: undefined,
  nextMonthPlan: undefined,
})

const rules: FormRules = {
  productName: [{ required: true, message: '请输入品名', trigger: 'blur' }],
}

function openCreate() {
  dialogTitle.value = '新增品种资料'
  Object.assign(form, {
    id: undefined,
    customerId: undefined,
    varietyType: '使用',
    productName: '',
    grade: '',
    material: '',
    spec: '',
    brandOrigin: '',
    competitor: '',
    swot: '',
    monthlyQty: undefined,
    nextMonthPlan: undefined,
  })
  dialogVisible.value = true
}

function openEdit(row: CrmVariety) {
  dialogTitle.value = '编辑品种资料'
  Object.assign(form, {
    id: row.id,
    customerId: row.customerId,
    varietyType: row.varietyType || '使用',
    productName: row.productName ?? '',
    grade: row.grade ?? '',
    material: row.material ?? '',
    spec: row.spec ?? '',
    brandOrigin: row.brandOrigin ?? '',
    competitor: row.competitor ?? '',
    swot: row.swot ?? '',
    monthlyQty: row.monthlyQty,
    nextMonthPlan: row.nextMonthPlan,
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
      await varietyApi.update(form.id, form)
      ElMessage.success('品种资料已更新')
    } else {
      await varietyApi.create(form)
      ElMessage.success('品种资料已新增')
    }
    dialogVisible.value = false
    fetchList()
  } catch {
    // 错误已由拦截器提示
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: CrmVariety) {
  try {
    await ElMessageBox.confirm(
      `确定删除客户「${row.customerName ?? row.customerId ?? ''}」的品种「${row.productName ?? ''}」吗？`,
      '删除确认',
      { type: 'warning' },
    )
  } catch {
    return
  }
  try {
    await varietyApi.remove(row.id as number)
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
          placeholder="客户 / 品名"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
        />
      </el-form-item>
    </SearchBar>

    <div class="page-toolbar">
      <el-button type="primary" @click="openCreate">新增品种资料</el-button>
      <span class="page-tip">类型：使用/生产/经营；月用量与下月计划用于下月销售计划预测</span>
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
      <template #varietyType="{ row }">
        <el-tag size="small">{{ row.varietyType }}</el-tag>
      </template>
      <template #monthlyQty="{ row }">
        <span class="mono">{{ fmtQty(row.monthlyQty) }}</span>
      </template>
      <template #nextMonthPlan="{ row }">
        <span class="mono">{{ fmtQty(row.nextMonthPlan) }}</span>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="客户 ID">
          <el-input-number v-model="form.customerId" :min="0" style="width: 100%" placeholder="客户 id" />
        </el-form-item>
        <el-form-item label="类型" prop="varietyType">
          <el-select v-model="form.varietyType" style="width: 100%">
            <el-option v-for="t in VARIETY_TYPES" :key="t" :value="t" :label="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="品名" prop="productName">
          <el-input v-model="form.productName" placeholder="如：电解铜" />
        </el-form-item>
        <el-form-item label="牌号">
          <el-input v-model="form.grade" placeholder="如：1#" />
        </el-form-item>
        <el-form-item label="材质">
          <el-input v-model="form.material" placeholder="材质" />
        </el-form-item>
        <el-form-item label="规格">
          <el-input v-model="form.spec" placeholder="规格" />
        </el-form-item>
        <el-form-item label="品牌/产地">
          <el-input v-model="form.brandOrigin" placeholder="品牌/产地" />
        </el-form-item>
        <el-form-item label="竞争对手">
          <el-input v-model="form.competitor" placeholder="竞争对手" />
        </el-form-item>
        <el-form-item label="月用量">
          <el-input-number v-model="form.monthlyQty" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="下月计划">
          <el-input-number v-model="form.nextMonthPlan" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="SWOT">
          <el-input v-model="form.swot" type="textarea" :rows="2" placeholder="SWOT 分析" />
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
