<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import SearchBar from '../../components/common/SearchBar.vue'
import DataTable from '../../components/common/DataTable.vue'
import type { TableColumn } from '../../components/common/DataTable.vue'
import VarietyList from './VarietyList.vue'
import CertList from './CertList.vue'
import CustomerList from './CustomerList.vue'
import LeadList from './LeadList.vue'
import { activityApi } from '../../api/crm'
import type { CrmActivity } from '../../api/crm'

/** CRM 域容器：活动管理（默认）/ 品种资料 / 证照风控 / 客户基本资料 / 线索管理 */
const activeTab = ref('activity')

const loading = ref(false)
const list = ref<CrmActivity[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const query = reactive<{ keyword?: string }>({})

const ACTIVITY_TYPES = ['使用', '生产', '经营']
const RELATIONS = ['主客', '次客', '主潜', '次潜', '大中', '主供', '次供']

function fmtMoney(v?: number): string {
  if (v == null || Number.isNaN(Number(v))) return '-'
  return Number(v).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

/** 后端联表返回名称；未返回时回退 id，保证列表不空白 */
function nameText(name?: string, id?: number): string {
  return name || (id != null ? String(id) : '-')
}

function fmtDateTime(v?: string): string {
  return v ? v.replace('T', ' ') : '-'
}

const columns: TableColumn[] = [
  { prop: 'customerName', label: '客户', minWidth: '150px', slot: 'customer' },
  { prop: 'contactName', label: '联系人', width: '110px', slot: 'contact' },
  { prop: 'activityType', label: '活动类型', width: '100px', slot: 'activityType' },
  { prop: 'relation', label: '关系', width: '90px', slot: 'relation' },
  { prop: 'productName', label: '品名', minWidth: '130px' },
  { prop: 'price', label: '价格', width: '120px', align: 'right', slot: 'price' },
  { prop: 'preNeedTime', label: '预需时间', width: '170px', slot: 'preNeedTime' },
  { prop: 'content', label: '内容', minWidth: '200px', slot: 'content' },
  { prop: 'creator', label: '创建人', width: '100px' },
]

async function fetchList() {
  loading.value = true
  try {
    const data = await activityApi.list({ page: page.value, size: size.value, ...query })
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
const dialogTitle = ref('新增活动')
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<CrmActivity>({
  customerId: undefined,
  contactId: undefined,
  activityType: '使用',
  relation: '主客',
  productName: '',
  price: undefined,
  preNeedTime: '',
  content: '',
  creator: '',
})

const rules: FormRules = {
  activityType: [{ required: true, message: '请选择活动类型', trigger: 'change' }],
}

function openCreate() {
  dialogTitle.value = '新增活动'
  Object.assign(form, {
    id: undefined,
    customerId: undefined,
    contactId: undefined,
    activityType: '使用',
    relation: '主客',
    productName: '',
    price: undefined,
    preNeedTime: '',
    content: '',
    creator: '',
  })
  dialogVisible.value = true
}

function openEdit(row: CrmActivity) {
  dialogTitle.value = '编辑活动'
  Object.assign(form, {
    id: row.id,
    customerId: row.customerId,
    contactId: row.contactId,
    activityType: row.activityType || '使用',
    relation: row.relation || '主客',
    productName: row.productName ?? '',
    price: row.price,
    preNeedTime: row.preNeedTime ?? '',
    content: row.content ?? '',
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
      await activityApi.update(form.id, form)
      ElMessage.success('活动已更新')
    } else {
      await activityApi.create(form)
      ElMessage.success('活动已新增')
    }
    dialogVisible.value = false
    fetchList()
  } catch {
    // 错误已由拦截器提示
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: CrmActivity) {
  try {
    await ElMessageBox.confirm(
      `确定删除客户「${row.customerName ?? row.customerId ?? ''}」的活动记录吗？`,
      '删除确认',
      { type: 'warning' },
    )
  } catch {
    return
  }
  try {
    await activityApi.remove(row.id as number)
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
    <el-tabs v-model="activeTab" class="domain-tabs">
      <!-- 活动管理（默认页） -->
      <el-tab-pane label="活动管理" name="activity">
        <SearchBar @search="handleSearch" @reset="handleReset" @export="handleExport">
          <el-form-item label="关键词">
            <el-input
              v-model="query.keyword"
              placeholder="客户 / 品名 / 创建人"
              clearable
              style="width: 220px"
              @keyup.enter="handleSearch"
            />
          </el-form-item>
        </SearchBar>

        <div class="page-toolbar">
          <el-button type="primary" @click="openCreate">新增活动</el-button>
          <span class="page-tip">活动类型：使用/生产/经营；关系：主客/次客/主潜/次潜/大中/主供/次供</span>
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
            <span>{{ nameText(row.customerName, row.customerId) }}</span>
          </template>
          <template #contact="{ row }">
            <span>{{ nameText(row.contactName, row.contactId) }}</span>
          </template>
          <template #activityType="{ row }">
            <el-tag size="small">{{ row.activityType }}</el-tag>
          </template>
          <template #relation="{ row }">
            <span>{{ row.relation || '-' }}</span>
          </template>
          <template #price="{ row }">
            <span class="mono">{{ fmtMoney(row.price) }}</span>
          </template>
          <template #preNeedTime="{ row }">
            <span>{{ fmtDateTime(row.preNeedTime) }}</span>
          </template>
          <template #content="{ row }">
            <el-text line-clamp="1">{{ row.content || '-' }}</el-text>
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
      </el-tab-pane>

      <!-- 品种资料 -->
      <el-tab-pane label="品种资料" name="variety" lazy>
        <VarietyList />
      </el-tab-pane>

      <!-- 证照风控 -->
      <el-tab-pane label="证照风控" name="cert" lazy>
        <CertList />
      </el-tab-pane>

      <!-- 客户基本资料 -->
      <el-tab-pane label="客户基本资料" name="customer" lazy>
        <CustomerList />
      </el-tab-pane>

      <!-- 线索管理 -->
      <el-tab-pane label="线索管理" name="lead" lazy>
        <LeadList />
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="客户 ID">
          <el-input-number v-model="form.customerId" :min="0" style="width: 100%" placeholder="客户 id" />
        </el-form-item>
        <el-form-item label="联系人 ID">
          <el-input-number v-model="form.contactId" :min="0" style="width: 100%" placeholder="联系人 id" />
        </el-form-item>
        <el-form-item label="活动类型" prop="activityType">
          <el-select v-model="form.activityType" style="width: 100%">
            <el-option v-for="t in ACTIVITY_TYPES" :key="t" :value="t" :label="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="关系">
          <el-select v-model="form.relation" style="width: 100%">
            <el-option v-for="r in RELATIONS" :key="r" :value="r" :label="r" />
          </el-select>
        </el-form-item>
        <el-form-item label="品名">
          <el-input v-model="form.productName" placeholder="如：电解铜" />
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="预需时间">
          <el-date-picker
            v-model="form.preNeedTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="2" placeholder="活动内容" />
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
.domain-tabs {
  margin-bottom: 4px;
}

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
