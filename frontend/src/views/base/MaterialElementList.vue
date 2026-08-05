<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import SearchBar from '../../components/common/SearchBar.vue'
import DataTable from '../../components/common/DataTable.vue'
import ConfirmDialog from '../../components/common/ConfirmDialog.vue'
import type { TableColumn } from '../../components/common/DataTable.vue'
import { materialElementApi } from '../../api/base'
import type { MaterialElement } from '../../api/base'

const loading = ref(false)
const list = ref<MaterialElement[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const query = reactive<{ keyword?: string }>({})

const columns: TableColumn[] = [
  { prop: 'id', label: 'ID', width: '70px' },
  { prop: 'symbol', label: '元素符号', width: '120px' },
  { prop: 'commonValue', label: '常用值(含量)', width: '140px' },
  { prop: 'rangeMin', label: '含量下限', width: '110px' },
  { prop: 'rangeMax', label: '含量上限', width: '110px' },
  { prop: 'gradeIndependent', label: '牌号材质独立', width: '130px', slot: 'gradeIndependent' },
  { prop: 'sort', label: '显示顺序', width: '100px' },
]

async function fetchList() {
  loading.value = true
  try {
    const data = await materialElementApi.list({ page: page.value, size: size.value, ...query })
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
const dialogTitle = ref('新增材质元素')
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<MaterialElement>({
  symbol: '',
  sort: 0,
  commonValue: '',
  rangeMin: undefined,
  rangeMax: undefined,
  gradeIndependent: 0,
})

const rules: FormRules = {
  symbol: [{ required: true, message: '请输入元素符号', trigger: 'blur' }],
}

function openCreate() {
  dialogTitle.value = '新增材质元素'
  Object.assign(form, {
    id: undefined,
    symbol: '',
    sort: 0,
    commonValue: '',
    rangeMin: undefined,
    rangeMax: undefined,
    gradeIndependent: 0,
  })
  dialogVisible.value = true
}

function openEdit(row: MaterialElement) {
  dialogTitle.value = '编辑材质元素'
  Object.assign(form, {
    id: row.id,
    symbol: row.symbol,
    sort: row.sort ?? 0,
    commonValue: row.commonValue ?? '',
    rangeMin: row.rangeMin,
    rangeMax: row.rangeMax,
    gradeIndependent: row.gradeIndependent ?? 0,
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
      await materialElementApi.update(form.id, form)
      ElMessage.success('材质元素已更新')
    } else {
      await materialElementApi.create(form)
      ElMessage.success('材质元素已新增')
    }
    dialogVisible.value = false
    fetchList()
  } catch {
    // 错误已由拦截器提示
  } finally {
    saving.value = false
  }
}

// ---------- 删除 ----------
const deleteVisible = ref(false)
const deleteTarget = ref<MaterialElement | null>(null)
const deleting = ref(false)

function handleDelete(row: MaterialElement) {
  deleteTarget.value = row
  deleteVisible.value = true
}

async function confirmDelete() {
  if (!deleteTarget.value) return
  deleting.value = true
  try {
    await materialElementApi.remove(deleteTarget.value.id as number)
    ElMessage.success('材质元素已删除')
    deleteVisible.value = false
    fetchList()
  } catch {
    // 错误已由拦截器提示
  } finally {
    deleting.value = false
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
          placeholder="元素符号"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
        />
      </el-form-item>
    </SearchBar>

    <div class="page-toolbar">
      <el-button type="primary" @click="openCreate">新增材质元素</el-button>
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
      <template #gradeIndependent="{ row }">
        <el-tag v-if="row.gradeIndependent === 1" type="warning" size="small">独立</el-tag>
        <el-tag v-else type="info" size="small">共用</el-tag>
      </template>
      <template #actions>
        <el-table-column label="操作" width="160px" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </template>
    </DataTable>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="元素符号" prop="symbol">
          <el-input v-model="form.symbol" placeholder="如：Cu" />
        </el-form-item>
        <el-form-item label="常用值(含量)">
          <el-input v-model="form.commonValue" placeholder="如：≥99% 或 0.05%" />
        </el-form-item>
        <el-form-item label="含量区间">
          <div class="range-row">
            <el-input-number v-model="form.rangeMin" :controls="false" placeholder="下限" />
            <span class="range-sep">~</span>
            <el-input-number v-model="form.rangeMax" :controls="false" placeholder="上限" />
          </div>
        </el-form-item>
        <el-form-item label="牌号材质独立">
          <el-switch
            v-model="form.gradeIndependent"
            :active-value="1"
            :inactive-value="0"
          />
        </el-form-item>
        <el-form-item label="显示顺序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <ConfirmDialog
      v-model="deleteVisible"
      title="删除材质元素"
      :message="`确定删除元素「${deleteTarget?.symbol ?? ''}」吗？`"
      :loading="deleting"
      @confirm="confirmDelete"
    />
  </div>
</template>

<style scoped>
.page-toolbar {
  margin-bottom: 14px;
}

.range-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.range-sep {
  color: var(--color-text-muted);
}
</style>
