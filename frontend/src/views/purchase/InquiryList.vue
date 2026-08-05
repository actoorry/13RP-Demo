<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import SearchBar from '../../components/common/SearchBar.vue'
import DataTable from '../../components/common/DataTable.vue'
import type { TableColumn } from '../../components/common/DataTable.vue'
import { inquiryApi } from '../../api/purchase'
import type { Inquiry } from '../../api/purchase'

const loading = ref(false)
const list = ref<Inquiry[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const query = reactive<{ keyword?: string; inquiryType?: string; status?: string }>({})

const INQUIRY_TYPES = ['急询价', '指定询价']
/** 状态统一 { label: 中文, value: 英文常量 }（后端状态机 CREATED → RECEIVED → REPLIED）。 */
const STATUS_OPTIONS = [
  { label: '待接收', value: 'CREATED' },
  { label: '已接收', value: 'RECEIVED' },
  { label: '已反馈', value: 'REPLIED' },
]

const columns: TableColumn[] = [
  { prop: 'inquiryNo', label: '询价单号', width: '160px' },
  { prop: 'inquiryType', label: '类型', width: '100px', slot: 'inquiryType' },
  { prop: 'productName', label: '品名', minWidth: '120px' },
  { prop: 'supplierName', label: '供应商', minWidth: '150px' },
  { prop: 'status', label: '状态', width: '100px', slot: 'status' },
  { prop: 'replyTime', label: '回复时间', width: '170px' },
  { prop: 'creator', label: '发起人', width: '100px' },
]

async function fetchList() {
  loading.value = true
  try {
    const data = await inquiryApi.list({ page: page.value, size: size.value, ...query })
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
  query.inquiryType = undefined
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
const dialogTitle = ref('新增询价')
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<Inquiry>({
  inquiryNo: '',
  inquiryType: '指定询价',
  productName: '',
  supplierName: '',
  status: 'CREATED',
  urgentFlag: 0,
  replyTime: '',
  creator: '',
})

const rules: FormRules = {
  inquiryNo: [{ required: true, message: '请输入询价单号', trigger: 'blur' }],
  productName: [{ required: true, message: '请输入品名', trigger: 'blur' }],
}

function openCreate() {
  dialogTitle.value = '新增询价'
  Object.assign(form, {
    id: undefined,
    inquiryNo: '',
    inquiryType: '指定询价',
    productName: '',
    supplierName: '',
    status: 'CREATED',
    urgentFlag: 0,
    replyTime: '',
    creator: '',
  })
  dialogVisible.value = true
}

function openEdit(row: Inquiry) {
  dialogTitle.value = '编辑询价'
  Object.assign(form, {
    id: row.id,
    inquiryNo: row.inquiryNo,
    inquiryType: row.inquiryType || '指定询价',
    productName: row.productName,
    supplierName: row.supplierName ?? '',
    status: row.status || 'CREATED',
    urgentFlag: row.urgentFlag ?? 0,
    replyTime: row.replyTime ?? '',
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
      await inquiryApi.update(form.id, form)
      ElMessage.success('询价已更新')
    } else {
      await inquiryApi.create(form)
      ElMessage.success('询价已新增')
    }
    dialogVisible.value = false
    fetchList()
  } catch {
    // 错误已由拦截器提示
  } finally {
    saving.value = false
  }
}

/** 询价状态流转：接收 → 反馈（CREATED → RECEIVED → REPLIED）。成功提示后刷新列表。 */
async function handleFlow(row: Inquiry, nextStatus: string, actionLabel: string) {
  if (row.id == null) return
  try {
    await inquiryApi.update(row.id, { status: nextStatus })
    ElMessage.success(`${actionLabel}成功`)
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
      <el-form-item label="类型">
        <el-select v-model="query.inquiryType" placeholder="全部" clearable style="width: 120px">
          <el-option v-for="t in INQUIRY_TYPES" :key="t" :value="t" :label="t" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
          <el-option v-for="opt in STATUS_OPTIONS" :key="opt.value" :value="opt.value" :label="opt.label" />
        </el-select>
      </el-form-item>
      <el-form-item label="关键词">
        <el-input
          v-model="query.keyword"
          placeholder="品名 / 单号"
          clearable
          style="width: 200px"
          @keyup.enter="handleSearch"
        />
      </el-form-item>
    </SearchBar>

    <div class="page-toolbar">
      <el-button type="primary" @click="openCreate">新增询价</el-button>
      <span class="page-tip">急询价：销售部从客户 CRM 发起；指定询价：销售部门户「采-我要询价」发起</span>
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
      <template #inquiryType="{ row }">
        <span class="urgent-tag">
          <el-tag v-if="row.urgentFlag === 1" type="danger" size="small">急</el-tag>
          <span>{{ row.inquiryType }}</span>
        </span>
      </template>
      <template #status="{ row }">
        <el-tag :type="row.status === 'REPLIED' ? 'success' : row.status === 'RECEIVED' ? 'warning' : 'info'" size="small">
          {{ STATUS_OPTIONS.find((o) => o.value === row.status)?.label ?? row.status }}
        </el-tag>
      </template>
      <template #actions>
        <el-table-column label="操作" width="180px" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button
              v-if="row.status === 'CREATED'"
              link
              type="primary"
              @click="handleFlow(row, 'RECEIVED', '接收')"
            >
              接收
            </el-button>
            <el-button
              v-else-if="row.status === 'RECEIVED'"
              link
              type="primary"
              @click="handleFlow(row, 'REPLIED', '反馈')"
            >
              反馈
            </el-button>
          </template>
        </el-table-column>
      </template>
    </DataTable>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="询价单号" prop="inquiryNo">
          <el-input v-model="form.inquiryNo" placeholder="如：XJ-20260805-001" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.inquiryType" style="width: 100%">
            <el-option v-for="t in INQUIRY_TYPES" :key="t" :value="t" :label="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="品名" prop="productName">
          <el-input v-model="form.productName" placeholder="如：稀土·镝" />
        </el-form-item>
        <el-form-item label="供应商">
          <el-input v-model="form.supplierName" placeholder="如：赣州中重稀土" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option v-for="opt in STATUS_OPTIONS" :key="opt.value" :value="opt.value" :label="opt.label" />
          </el-select>
        </el-form-item>
        <el-form-item label="标记急询">
          <el-switch v-model="form.urgentFlag" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="发起人">
          <el-input v-model="form.creator" placeholder="发起人" />
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

.urgent-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
</style>
