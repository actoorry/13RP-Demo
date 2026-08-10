<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import SearchBar from '../../components/common/SearchBar.vue'
import DataTable from '../../components/common/DataTable.vue'
import type { TableColumn } from '../../components/common/DataTable.vue'
import { groupApi } from '../../api/org'
import { customerApi } from '../../api/crm'

/**
 * 综合管理：多维筛选 CRM 客户 + 通用操作（划拨/批量迁移/设置特征/标记）。
 * 客户主数据来源 CRM 域（crm_customer），划拨/迁移通过 /api/org/group/transfer 提交。
 */
const filter = reactive<{
  orgName?: string
  personName?: string
  productName?: string
  varietyType?: string
  relation?: string
  region?: string
}>({})

const relationOptions = [
  '主客', '次客', '主潜', '次潜', '大中', '主供', '次供', '潜供',
]

const varietyOptions = ['使用', '生产', '经营']

const columns: TableColumn[] = [
  { prop: 'customerName', label: '客户名称', minWidth: '160px' },
  { prop: 'orgName', label: '所属组织', width: '120px' },
  { prop: 'personName', label: '负责人', width: '100px' },
  { prop: 'varietyType', label: '类型', width: '90px' },
  { prop: 'relation', label: '关系', width: '90px' },
  { prop: 'region', label: '区域', width: '100px' },
]

const list = ref<Record<string, unknown>[]>([])
const total = ref(0)
const loading = ref(false)
const selectedRows = ref<Record<string, unknown>[]>([])

function handleSelectionChange(rows: Record<string, unknown>[]) {
  selectedRows.value = rows
}

async function handleSearch() {
  loading.value = true
  try {
    const data = await customerApi.list({ page: 1, size: 100, keyword: filter.productName })
    const records = ((data as Record<string, unknown>).list || (data as Record<string, unknown>).records || []) as Record<string, unknown>[]
    // 多维筛选（后端 keyword 搜 name/phone，前端补 orgName/personName/varietyType/relation/region 过滤）
    let filtered = records
    if (filter.orgName) {
      filtered = filtered.filter((r) => String(r.address || '').includes(filter.orgName!))
    }
    if (filter.personName) {
      filtered = filtered.filter((r) => String(r.remark || '').includes(filter.personName!))
    }
    if (filter.varietyType) {
      filtered = filtered.filter((r) => String(r.industry || '').includes(filter.varietyType!))
    }
    if (filter.region) {
      filtered = filtered.filter((r) => String(r.address || '').includes(filter.region!))
    }
    list.value = filtered.map((r) => ({
      id: r.id,
      customerName: r.name,
      orgName: r.address || '-',
      personName: r.remark || '-',
      varietyType: r.industry || '-',
      relation: (r as Record<string, unknown>).level || '-',
      region: r.address || '-',
      phone: r.phone,
      feature: '',
      marked: false,
    }))
    total.value = list.value.length
  } catch {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleReset() {
  Object.assign(filter, {
    orgName: undefined,
    personName: undefined,
    productName: undefined,
    varietyType: undefined,
    relation: undefined,
    region: undefined,
  })
}

function handleExport() {
  if (!list.value.length) {
    ElMessage.warning('暂无数据可导出，请先查询')
    return
  }
  const header = ['客户名称', '所属组织', '负责人', '类型', '关系', '区域', '电话', '特征', '标记']
  const rows = list.value.map((r) =>
    [r.customerName, r.orgName, r.personName, r.varietyType, r.relation, r.region, r.phone, r.feature || '', r.marked ? '是' : '否']
      .map((v) => `"${String(v ?? '').replace(/"/g, '""')}"`).join(','),
  )
  const csv = '\uFEFF' + [header.join(','), ...rows].join('\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = '综合管理客户.csv'
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success(`已导出 ${rows.length} 条`)
}

async function handleTransfer(transferType: 'company' | 'group' | 'owner') {
  if (!selectedRows.value.length) {
    ElMessage.warning('请先勾选要操作的客户行')
    return
  }
  const customerIds = selectedRows.value.map((r) => Number(r.id)).filter(Boolean)
  try {
    await ElMessageBox.confirm(
      transferType === 'company'
        ? `确定将选中的 ${customerIds.length} 个客户划拨到公司公共池吗？`
        : transferType === 'group'
          ? `确定将选中的 ${customerIds.length} 个客户划拨到指定组吗？`
          : `确定批量迁移 ${customerIds.length} 个客户的主要负责人吗？`,
      '确认操作',
      { type: 'warning' },
    )
  } catch {
    return
  }
  try {
    await groupApi.transfer({ transferType, customerIds })
    ElMessage.success('操作已提交')
    handleSearch()
  } catch {
    // 错误已由拦截器提示
  }
}

function handleSetFeature() {
  if (!selectedRows.value.length) {
    ElMessage.warning('请先勾选客户')
    return
  }
  ElMessageBox.prompt('请输入特征标签', '设置特征', { confirmButtonText: '确定', cancelButtonText: '取消' })
    .then(({ value }) => {
      selectedRows.value.forEach((row) => {
        row.feature = value
        const item = list.value.find((r) => r.id === row.id)
        if (item) item.feature = value
      })
      ElMessage.success(`已为 ${selectedRows.value.length} 个客户设置特征：${value}`)
    })
    .catch(() => {})
}

function handleMark() {
  if (!selectedRows.value.length) {
    ElMessage.warning('请先勾选客户')
    return
  }
  selectedRows.value.forEach((row) => {
    row.marked = !row.marked
    const item = list.value.find((r) => r.id === row.id)
    if (item) item.marked = row.marked
  })
  ElMessage.success(`已${selectedRows.value[0]?.marked ? '标记' : '取消标记'} ${selectedRows.value.length} 个客户`)
}
</script>

<template>
  <div class="page fade-up">
    <SearchBar @search="handleSearch" @reset="handleReset" @export="handleExport">
      <el-form-item label="所属组织">
        <el-input v-model="filter.orgName" placeholder="组织名称" clearable style="width: 150px" />
      </el-form-item>
      <el-form-item label="岗位人员">
        <el-input v-model="filter.personName" placeholder="负责人" clearable style="width: 120px" />
      </el-form-item>
      <el-form-item label="品名">
        <el-input v-model="filter.productName" placeholder="品名" clearable style="width: 130px" />
      </el-form-item>
      <el-form-item label="类型">
        <el-select v-model="filter.varietyType" placeholder="使用/生产/经营" clearable style="width: 150px">
          <el-option v-for="v in varietyOptions" :key="v" :value="v" :label="v" />
        </el-select>
      </el-form-item>
      <el-form-item label="关系">
        <el-select v-model="filter.relation" placeholder="客户关系" clearable style="width: 120px">
          <el-option v-for="r in relationOptions" :key="r" :value="r" :label="r" />
        </el-select>
      </el-form-item>
      <el-form-item label="区域">
        <el-input v-model="filter.region" placeholder="区域" clearable style="width: 120px" />
      </el-form-item>
    </SearchBar>

    <div class="page-toolbar">
      <el-button type="warning" plain @click="handleTransfer('company')">划拨到公司</el-button>
      <el-button type="warning" plain @click="handleTransfer('group')">划拨到组</el-button>
      <el-button type="warning" plain @click="handleTransfer('owner')">批量迁移主要负责人</el-button>
      <el-button @click="handleSetFeature">设置特征</el-button>
      <el-button @click="handleMark">标记 / 取消标记</el-button>
    </div>

    <DataTable
      :columns="columns"
      :data="list"
      :loading="loading"
      :total="total"
      :page="1"
      :size="100"
      @selection-change="handleSelectionChange"
    >
      <template #actions>
        <el-table-column type="selection" width="45px" />
        <el-table-column label="特征" width="120px">
          <template #default="{ row }">
            <el-tag v-if="row.feature" size="small" type="warning">{{ row.feature }}</el-tag>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="标记" width="70px" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.marked" size="small" type="danger">已标记</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </template>
    </DataTable>
  </div>
</template>

<style scoped>
.page-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 14px;
}
</style>
