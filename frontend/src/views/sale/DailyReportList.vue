<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import SearchBar from '../../components/common/SearchBar.vue'
import DataTable from '../../components/common/DataTable.vue'
import type { TableColumn } from '../../components/common/DataTable.vue'
import { saleDailyReportApi } from '../../api/sale'
import type { SaleDailyReport } from '../../api/sale'

const loading = ref(false)
const list = ref<SaleDailyReport[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const query = reactive<{ keyword?: string }>({})

const columns: TableColumn[] = [
  { prop: 'reportDate', label: '日期', width: '140px' },
  { prop: 'contactCnt', label: '联系家数', width: '120px', align: 'right' },
  { prop: 'leadCnt', label: '线索家数', width: '120px', align: 'right' },
  { prop: 'dealCnt', label: '成交家数', width: '120px', align: 'right' },
  { prop: 'orgId', label: '所属组织', width: '110px' },
]

async function fetchList() {
  loading.value = true
  try {
    const data = await saleDailyReportApi.list({ page: page.value, size: size.value, ...query })
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
const dialogTitle = ref('新增业务日报')
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<SaleDailyReport>({
  reportDate: '',
  contactCnt: 0,
  leadCnt: 0,
  dealCnt: 0,
})

const rules: FormRules = {
  reportDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
}

function openCreate() {
  dialogTitle.value = '新增业务日报'
  Object.assign(form, { id: undefined, reportDate: '', contactCnt: 0, leadCnt: 0, dealCnt: 0 })
  dialogVisible.value = true
}

function openEdit(row: SaleDailyReport) {
  dialogTitle.value = '编辑业务日报'
  Object.assign(form, {
    id: row.id,
    reportDate: row.reportDate,
    contactCnt: row.contactCnt ?? 0,
    leadCnt: row.leadCnt ?? 0,
    dealCnt: row.dealCnt ?? 0,
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
      await saleDailyReportApi.update(form.id, form)
      ElMessage.success('业务日报已更新')
    } else {
      await saleDailyReportApi.create(form)
      ElMessage.success('业务日报已新增')
    }
    dialogVisible.value = false
    fetchList()
  } catch {
    // 错误已由拦截器提示
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: SaleDailyReport) {
  try {
    await ElMessageBox.confirm(
      `确定删除 ${row.reportDate ?? row.id} 的业务日报吗？`,
      '删除确认',
      { type: 'warning' },
    )
  } catch {
    return
  }
  try {
    await saleDailyReportApi.remove(row.id as number)
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
          placeholder="日期（YYYY-MM-DD）"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
        />
      </el-form-item>
    </SearchBar>

    <div class="page-toolbar">
      <el-button type="primary" @click="openCreate">新增业务日报</el-button>
      <span class="page-tip">漏斗语义：联系家数 → 线索家数（报价）→ 成交家数（开单）</span>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="460px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="日期" prop="reportDate">
          <el-date-picker v-model="form.reportDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="联系家数">
          <el-input-number v-model="form.contactCnt" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="线索家数">
          <el-input-number v-model="form.leadCnt" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="成交家数">
          <el-input-number v-model="form.dealCnt" :min="0" style="width: 100%" />
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
