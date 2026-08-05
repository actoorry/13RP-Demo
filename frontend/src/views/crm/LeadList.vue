<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import SearchBar from '../../components/common/SearchBar.vue'
import DataTable from '../../components/common/DataTable.vue'
import type { TableColumn } from '../../components/common/DataTable.vue'
import { leadApi } from '../../api/crm'
import type { CrmLead } from '../../api/crm'

const loading = ref(false)
const list = ref<CrmLead[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const query = reactive<{ keyword?: string }>({})

function fmtDateTime(v?: string): string {
  return v ? v.replace('T', ' ') : '-'
}

/** 创建时间：后端若补充 createTime 直接用；未返回时回退到最近跟进/下次联系时间 */
function createTimeText(row: CrmLead): string {
  return fmtDateTime(row.createTime || row.lastFollowTime || row.nextContactTime)
}

const columns: TableColumn[] = [
  { prop: 'name', label: '线索名', minWidth: '160px' },
  { prop: 'source', label: '来源', width: '120px' },
  { prop: 'companyType', label: '类型', width: '110px' },
  { prop: 'convertedFlag', label: '状态', width: '100px', slot: 'convertedFlag' },
  { prop: 'createTime', label: '创建时间', width: '170px', slot: 'createTime' },
]

async function fetchList() {
  loading.value = true
  try {
    const data = await leadApi.list({ page: page.value, size: size.value, ...query })
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
const dialogTitle = ref('新增线索')
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<CrmLead>({
  name: '',
  source: '',
  companyType: '',
  phone: '',
  tel: '',
  email: '',
  address: '',
  industry: '',
  level: '',
  remark: '',
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入线索名称', trigger: 'blur' }],
}

function openCreate() {
  dialogTitle.value = '新增线索'
  Object.assign(form, {
    id: undefined,
    name: '',
    source: '',
    companyType: '',
    phone: '',
    tel: '',
    email: '',
    address: '',
    industry: '',
    level: '',
    remark: '',
  })
  dialogVisible.value = true
}

function openEdit(row: CrmLead) {
  dialogTitle.value = '编辑线索'
  Object.assign(form, {
    id: row.id,
    name: row.name ?? '',
    source: row.source ?? '',
    companyType: row.companyType ?? '',
    phone: row.phone ?? '',
    tel: row.tel ?? '',
    email: row.email ?? '',
    address: row.address ?? '',
    industry: row.industry ?? '',
    level: row.level ?? '',
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
      await leadApi.update(form.id, form)
      ElMessage.success('线索已更新')
    } else {
      await leadApi.create(form)
      ElMessage.success('线索已新增')
    }
    dialogVisible.value = false
    fetchList()
  } catch {
    // 错误已由拦截器提示
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: CrmLead) {
  try {
    await ElMessageBox.confirm(
      `确定删除线索「${row.name ?? row.id}」吗？`,
      '删除确认',
      { type: 'warning' },
    )
  } catch {
    return
  }
  try {
    await leadApi.remove(row.id as number)
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
          placeholder="线索名称 / 来源"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
        />
      </el-form-item>
    </SearchBar>

    <div class="page-toolbar">
      <el-button type="primary" @click="openCreate">新增线索</el-button>
      <span class="page-tip">线索可跟进并转化为客户；已转化线索进入客户基本资料</span>
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
      <template #convertedFlag="{ row }">
        <el-tag :type="row.convertedFlag === 1 ? 'success' : 'warning'" size="small">
          {{ row.convertedFlag === 1 ? '已转化' : '跟进中' }}
        </el-tag>
      </template>
      <template #createTime="{ row }">
        <span>{{ createTimeText(row) }}</span>
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
        <el-form-item label="线索名" prop="name">
          <el-input v-model="form.name" placeholder="线索名称" />
        </el-form-item>
        <el-form-item label="来源">
          <el-input v-model="form.source" placeholder="来源，如：展会/转介绍" />
        </el-form-item>
        <el-form-item label="类型">
          <el-input v-model="form.companyType" placeholder="公司类型" />
        </el-form-item>
        <el-form-item label="行业">
          <el-input v-model="form.industry" placeholder="行业" />
        </el-form-item>
        <el-form-item label="等级">
          <el-input v-model="form.level" placeholder="等级" />
        </el-form-item>
        <el-form-item label="手机">
          <el-input v-model="form.phone" placeholder="手机" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.tel" placeholder="电话" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="邮箱" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" placeholder="地址" />
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
