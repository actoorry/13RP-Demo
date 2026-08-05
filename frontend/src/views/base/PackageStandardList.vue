<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import SearchBar from '../../components/common/SearchBar.vue'
import DataTable from '../../components/common/DataTable.vue'
import ConfirmDialog from '../../components/common/ConfirmDialog.vue'
import type { TableColumn } from '../../components/common/DataTable.vue'
import { packageStandardApi } from '../../api/base'
import type { PackageStandard } from '../../api/base'

const loading = ref(false)
const list = ref<PackageStandard[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const query = reactive<{ keyword?: string }>({})

const columns: TableColumn[] = [
  { prop: 'id', label: 'ID', width: '70px' },
  { prop: 'packageName', label: '包装名称', minWidth: '160px' },
  { prop: 'damageCompensation', label: '破损赔偿', minWidth: '220px' },
  { prop: 'status', label: '状态', width: '90px', slot: 'status' },
]

async function fetchList() {
  loading.value = true
  try {
    const data = await packageStandardApi.list({ page: page.value, size: size.value, ...query })
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
const dialogTitle = ref('新增包装标准')
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<PackageStandard>({ packageName: '', damageCompensation: '', status: 1 })

const rules: FormRules = {
  packageName: [{ required: true, message: '请输入包装名称', trigger: 'blur' }],
}

function openCreate() {
  dialogTitle.value = '新增包装标准'
  Object.assign(form, { id: undefined, packageName: '', damageCompensation: '', status: 1 })
  dialogVisible.value = true
}

function openEdit(row: PackageStandard) {
  dialogTitle.value = '编辑包装标准'
  Object.assign(form, {
    id: row.id,
    packageName: row.packageName,
    damageCompensation: row.damageCompensation ?? '',
    status: row.status ?? 1,
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
      await packageStandardApi.update(form.id, form)
      ElMessage.success('包装标准已更新')
    } else {
      await packageStandardApi.create(form)
      ElMessage.success('包装标准已新增')
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
const deleteTarget = ref<PackageStandard | null>(null)
const deleting = ref(false)

function handleDelete(row: PackageStandard) {
  deleteTarget.value = row
  deleteVisible.value = true
}

async function confirmDelete() {
  if (!deleteTarget.value) return
  deleting.value = true
  try {
    await packageStandardApi.remove(deleteTarget.value.id as number)
    ElMessage.success('包装标准已删除')
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
          placeholder="包装名称"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
        />
      </el-form-item>
    </SearchBar>

    <div class="page-toolbar">
      <el-button type="primary" @click="openCreate">新增包装标准</el-button>
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
        <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
          {{ row.status === 1 ? '启用' : '停用' }}
        </el-tag>
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
        <el-form-item label="包装名称" prop="packageName">
          <el-input v-model="form.packageName" placeholder="如：袋装 / 托盘 / 纸箱" />
        </el-form-item>
        <el-form-item label="破损赔偿">
          <el-input
            v-model="form.damageCompensation"
            type="textarea"
            :rows="3"
            placeholder="如：破损率 ≤1‰，超出部分按货值赔偿"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <ConfirmDialog
      v-model="deleteVisible"
      title="删除包装标准"
      :message="`确定删除包装标准「${deleteTarget?.packageName ?? ''}」吗？`"
      :loading="deleting"
      @confirm="confirmDelete"
    />
  </div>
</template>

<style scoped>
.page-toolbar {
  margin-bottom: 14px;
}
</style>
