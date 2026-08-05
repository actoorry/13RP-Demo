<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import SearchBar from '../../components/common/SearchBar.vue'
import DataTable from '../../components/common/DataTable.vue'
import type { TableColumn } from '../../components/common/DataTable.vue'
import { supplierGradeApi } from '../../api/purchase'
import type { SupplierGrade } from '../../api/purchase'

const loading = ref(false)
const list = ref<SupplierGrade[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const query = reactive<{ keyword?: string }>({})

const GRADES = ['战略', '优选', '考察', '一般']

const columns: TableColumn[] = [
  { prop: 'id', label: 'ID', width: '70px' },
  { prop: 'supplierName', label: '供应商名称', minWidth: '180px' },
  { prop: 'grade', label: '分级', width: '100px', slot: 'grade' },
  { prop: 'contact', label: '联系人', width: '110px' },
  { prop: 'phone', label: '联系电话', width: '140px' },
]

const GRADE_TYPE: Record<string, 'success' | 'warning' | 'info' | 'danger'> = {
  战略: 'success',
  优选: 'primary',
  考察: 'warning',
  一般: 'info',
}

async function fetchList() {
  loading.value = true
  try {
    const data = await supplierGradeApi.list({ page: page.value, size: size.value, ...query })
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
const dialogTitle = ref('新增供应商分级')
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<SupplierGrade>({ supplierName: '', grade: '战略', contact: '', phone: '' })

const rules: FormRules = {
  supplierName: [{ required: true, message: '请输入供应商名称', trigger: 'blur' }],
  grade: [{ required: true, message: '请选择分级', trigger: 'change' }],
}

function openCreate() {
  dialogTitle.value = '新增供应商分级'
  Object.assign(form, { id: undefined, supplierName: '', grade: '战略', contact: '', phone: '' })
  dialogVisible.value = true
}

function openEdit(row: SupplierGrade) {
  dialogTitle.value = '编辑供应商分级'
  Object.assign(form, {
    id: row.id,
    supplierName: row.supplierName ?? '',
    grade: row.grade || '战略',
    contact: row.contact ?? '',
    phone: row.phone ?? '',
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
      await supplierGradeApi.update(form.id, form)
      ElMessage.success('供应商分级已更新')
    } else {
      await supplierGradeApi.create(form)
      ElMessage.success('供应商分级已新增')
    }
    dialogVisible.value = false
    fetchList()
  } catch {
    // 错误已由拦截器提示
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: SupplierGrade) {
  try {
    await ElMessageBox.confirm(
      `确定删除供应商「${row.supplierName ?? row.id}」的分级记录吗？`,
      '删除确认',
      { type: 'warning' },
    )
  } catch {
    return
  }
  try {
    await supplierGradeApi.remove(row.id as number)
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
          placeholder="供应商名称"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
        />
      </el-form-item>
    </SearchBar>

    <div class="page-toolbar">
      <el-button type="primary" @click="openCreate">新增供应商分级</el-button>
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
      <template #grade="{ row }">
        <el-tag :type="GRADE_TYPE[row.grade] || 'info'" size="small">{{ row.grade }}</el-tag>
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
        <el-form-item label="供应商名称" prop="supplierName">
          <el-input v-model="form.supplierName" placeholder="如：包头北方稀土矿业" />
        </el-form-item>
        <el-form-item label="分级" prop="grade">
          <el-select v-model="form.grade" style="width: 100%">
            <el-option v-for="g in GRADES" :key="g" :value="g" :label="g" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="form.contact" placeholder="联系人" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" placeholder="联系电话" />
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
