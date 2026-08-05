<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import SearchBar from '../../components/common/SearchBar.vue'
import DataTable from '../../components/common/DataTable.vue'
import type { TableColumn } from '../../components/common/DataTable.vue'
import { groupApi } from '../../api/org'

/**
 * 综合管理：多维筛选 + 通用操作（划拨/批量迁移/设置特征/标记）。
 * 客户主数据归属 CRM 域（批次 2 实现），本页先提供操作框架，
 * 划拨/迁移通过 /api/org/group/transfer 提交。
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

const list = ref<Record<string, string>[]>([])
const total = ref(0)

function handleSearch() {
  // 客户数据源属 CRM 域，批次 2 接入后按筛选条件查询
  ElMessage.info('客户主数据在批次 2（CRM 域）接入，当前展示操作框架')
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
  ElMessage.info('导出功能将在批次 2 提供')
}

async function handleTransfer(transferType: 'company' | 'group' | 'owner') {
  try {
    await ElMessageBox.confirm(
      transferType === 'company'
        ? '确定将当前筛选客户划拨到公司公共池吗？'
        : transferType === 'group'
          ? '确定将当前筛选客户划拨到指定组吗？'
          : '确定批量迁移这些客户的主要负责人吗？',
      '确认操作',
      { type: 'warning' },
    )
  } catch {
    return
  }
  try {
    await groupApi.transfer({ transferType, customerIds: [] })
    ElMessage.success('操作已提交（批次 2 接入客户数据后生效）')
  } catch {
    // 错误已由拦截器提示
  }
}

function handleSetFeature() {
  ElMessage.info('设置特征（标记客户特征）将在批次 2 提供')
}

function handleMark() {
  ElMessage.info('标记 / 取消标记将在批次 2 提供')
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
      :loading="false"
      :total="total"
      :page="1"
      :size="10"
    />
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
