<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import SearchBar from '../../components/common/SearchBar.vue'
import DataTable from '../../components/common/DataTable.vue'
import type { TableColumn } from '../../components/common/DataTable.vue'
import { forecastApi } from '../../api/purchase'
import type { ForecastPlan } from '../../api/purchase'

const loading = ref(false)
const list = ref<ForecastPlan[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const query = reactive<{ keyword?: string; planType?: string }>({})

const PLAN_TYPES = ['年规划', '月计划', '周优化', '日执行']

const PLAN_TYPE_TAG: Record<string, 'primary' | 'success' | 'warning' | 'danger'> = {
  年规划: 'primary',
  月计划: 'success',
  周优化: 'warning',
  日执行: 'danger',
}

const columns: TableColumn[] = [
  { prop: 'id', label: 'ID', width: '70px' },
  { prop: 'planType', label: '预案类型', width: '100px', slot: 'planType' },
  { prop: 'planName', label: '预案名称', minWidth: '180px' },
  { prop: 'periodStart', label: '起始日期', width: '130px' },
  { prop: 'periodEnd', label: '结束日期', width: '130px' },
  { prop: 'creator', label: '创建人', width: '110px' },
]

async function fetchList() {
  loading.value = true
  try {
    const data = await forecastApi.list({ page: page.value, size: size.value, ...query })
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
  query.planType = undefined
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
const dialogTitle = ref('新增预测预案')
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<ForecastPlan>({
  planType: '月计划',
  planName: '',
  periodStart: '',
  periodEnd: '',
  creator: '',
})

const rules: FormRules = {
  planName: [{ required: true, message: '请输入预案名称', trigger: 'blur' }],
  planType: [{ required: true, message: '请选择预案类型', trigger: 'change' }],
}

function openCreate() {
  dialogTitle.value = '新增预测预案'
  Object.assign(form, {
    id: undefined,
    planType: '月计划',
    planName: '',
    periodStart: '',
    periodEnd: '',
    creator: '',
  })
  dialogVisible.value = true
}

function openEdit(row: ForecastPlan) {
  dialogTitle.value = '编辑预测预案'
  Object.assign(form, {
    id: row.id,
    planType: row.planType || '月计划',
    planName: row.planName,
    periodStart: row.periodStart ?? '',
    periodEnd: row.periodEnd ?? '',
    creator: row.creator ?? '',
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
      await forecastApi.update(form.id, form)
      ElMessage.success('预测预案已更新')
    } else {
      await forecastApi.create(form)
      ElMessage.success('预测预案已新增')
    }
    dialogVisible.value = false
    fetchList()
  } catch {
    // 错误已由拦截器提示
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: ForecastPlan) {
  try {
    await ElMessageBox.confirm(`确定删除预测预案「${row.planName}」吗？`, '删除确认', {
      type: 'warning',
    })
  } catch {
    return
  }
  try {
    await forecastApi.remove(row.id as number)
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
      <el-form-item label="预案类型">
        <el-select v-model="query.planType" placeholder="全部" clearable style="width: 130px">
          <el-option v-for="t in PLAN_TYPES" :key="t" :value="t" :label="t" />
        </el-select>
      </el-form-item>
      <el-form-item label="关键词">
        <el-input
          v-model="query.keyword"
          placeholder="预案名称"
          clearable
          style="width: 200px"
          @keyup.enter="handleSearch"
        />
      </el-form-item>
    </SearchBar>

    <div class="page-toolbar">
      <el-button type="primary" @click="openCreate">新增预测预案</el-button>
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
      <template #planType="{ row }">
        <el-tag :type="PLAN_TYPE_TAG[row.planType] || 'info'" size="small">{{ row.planType }}</el-tag>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="预案类型" prop="planType">
          <el-select v-model="form.planType" style="width: 100%">
            <el-option v-for="t in PLAN_TYPES" :key="t" :value="t" :label="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="预案名称" prop="planName">
          <el-input v-model="form.planName" placeholder="如：8 月电解铜采购计划" />
        </el-form-item>
        <el-form-item label="起始日期">
          <el-date-picker
            v-model="form.periodStart"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="起始日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker
            v-model="form.periodEnd"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="结束日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="创建人">
          <el-input v-model="form.creator" placeholder="创建人" />
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
