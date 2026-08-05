<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import SearchBar from '../../components/common/SearchBar.vue'
import DataTable from '../../components/common/DataTable.vue'
import type { TableColumn } from '../../components/common/DataTable.vue'
import { employeeApi } from '../../api/org'
import type { OrgEmployee } from '../../api/org'

const loading = ref(false)
const list = ref<OrgEmployee[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const query = reactive<{ keyword?: string }>({})

const columns: TableColumn[] = [
  { prop: 'id', label: 'ID', width: '70px' },
  { prop: 'name', label: '姓名', width: '110px' },
  { prop: 'account', label: '账号', width: '120px' },
  { prop: 'phone', label: '手机号', width: '140px' },
  { prop: 'dept', label: '所属部门', minWidth: '130px' },
  { prop: 'position', label: '岗位', minWidth: '110px' },
  { prop: 'status', label: '状态', width: '90px', slot: 'status' },
]

async function fetchList() {
  loading.value = true
  try {
    const data = await employeeApi.list({ page: page.value, size: size.value, ...query })
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
const dialogTitle = ref('新增员工')
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<OrgEmployee>({
  name: '',
  account: '',
  phone: '',
  dept: '',
  position: '',
  status: 1,
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  account: [{ required: true, message: '请输入登录账号', trigger: 'blur' }],
}

function openCreate() {
  dialogTitle.value = '新增员工'
  Object.assign(form, {
    id: undefined,
    name: '',
    account: '',
    phone: '',
    dept: '',
    position: '',
    status: 1,
  })
  dialogVisible.value = true
}

function openEdit(row: OrgEmployee) {
  dialogTitle.value = '编辑员工'
  Object.assign(form, {
    id: row.id,
    name: row.name,
    account: row.account ?? '',
    phone: row.phone ?? '',
    dept: row.dept ?? '',
    position: row.position ?? '',
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
      await employeeApi.update(form.id, form)
      ElMessage.success('员工已更新')
    } else {
      await employeeApi.create(form)
      ElMessage.success('员工已新增')
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
      <el-form-item label="关键词">
        <el-input
          v-model="query.keyword"
          placeholder="姓名 / 账号 / 手机号"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
        />
      </el-form-item>
    </SearchBar>

    <div class="page-toolbar">
      <el-button type="primary" @click="openCreate">新增员工</el-button>
      <span class="page-tip">员工知识 / 通信 / 活动 / 录音 / 来电去电在批次 2 完善</span>
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
          {{ row.status === 1 ? '在职' : '离职' }}
        </el-tag>
      </template>
      <template #actions>
        <el-table-column label="操作" width="120px" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </template>
    </DataTable>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="员工姓名" />
        </el-form-item>
        <el-form-item label="登录账号" prop="account">
          <el-input v-model="form.account" placeholder="登录账号" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="手机号" />
        </el-form-item>
        <el-form-item label="所属部门">
          <el-input v-model="form.dept" placeholder="如：销采服务部" />
        </el-form-item>
        <el-form-item label="岗位">
          <el-input v-model="form.position" placeholder="如：销售经理" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">在职</el-radio>
            <el-radio :value="0">离职</el-radio>
          </el-radio-group>
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
