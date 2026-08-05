<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import SearchBar from '../../components/common/SearchBar.vue'
import DataTable from '../../components/common/DataTable.vue'
import type { TableColumn } from '../../components/common/DataTable.vue'
import { groupApi } from '../../api/org'
import type { GroupTransferParams, OrgGroup } from '../../api/org'

const loading = ref(false)
const list = ref<OrgGroup[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const query = reactive<{ keyword?: string }>({})

const columns: TableColumn[] = [
  { prop: 'id', label: 'ID', width: '70px' },
  { prop: 'groupName', label: '组名称', minWidth: '180px' },
  { prop: 'ownerName', label: '组负责人', width: '120px' },
  { prop: 'createTime', label: '创建时间', width: '180px' },
]

async function fetchList() {
  loading.value = true
  try {
    const data = await groupApi.list({ page: page.value, size: size.value, ...query })
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
const dialogTitle = ref('新增组')
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<OrgGroup>({ groupName: '', ownerId: undefined, ownerName: '' })

const rules: FormRules = {
  groupName: [{ required: true, message: '请输入组名称', trigger: 'blur' }],
}

function openCreate() {
  dialogTitle.value = '新增组'
  Object.assign(form, { id: undefined, groupName: '', ownerId: undefined, ownerName: '' })
  dialogVisible.value = true
}

function openEdit(row: OrgGroup) {
  dialogTitle.value = '编辑组'
  Object.assign(form, {
    id: row.id,
    groupName: row.groupName,
    ownerId: row.ownerId,
    ownerName: row.ownerName ?? '',
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
      await groupApi.update(form.id, form)
      ElMessage.success('组已更新')
    } else {
      await groupApi.create(form)
      ElMessage.success('组已新增')
    }
    dialogVisible.value = false
    fetchList()
  } catch {
    // 错误已由拦截器提示
  } finally {
    saving.value = false
  }
}

// ---------- 划拨 / 迁移 ----------
const transferVisible = ref(false)
const transferSaving = ref(false)
const transferForm = reactive<GroupTransferParams>({
  transferType: 'company',
  targetId: undefined,
  ownerId: undefined,
  customerIds: [],
})

const TRANSFER_TYPES = [
  { value: 'company', label: '划拨到公司' },
  { value: 'group', label: '划拨到组' },
  { value: 'owner', label: '批量迁移主要负责人' },
]

function openTransfer() {
  Object.assign(transferForm, {
    transferType: 'company',
    targetId: undefined,
    ownerId: undefined,
    customerIds: [],
  })
  transferVisible.value = true
}

async function handleTransfer() {
  if (!transferForm.targetId && transferForm.transferType !== 'company') {
    ElMessage.warning(transferForm.transferType === 'owner' ? '请输入主要负责人 ID' : '请输入目标 ID')
    return
  }
  transferSaving.value = true
  try {
    await groupApi.transfer({ ...transferForm })
    ElMessage.success('划拨/迁移已提交')
    transferVisible.value = false
    fetchList()
  } catch {
    // 错误已由拦截器提示
  } finally {
    transferSaving.value = false
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
          placeholder="组名称"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
        />
      </el-form-item>
    </SearchBar>

    <div class="page-toolbar">
      <el-button type="primary" @click="openCreate">新增组</el-button>
      <el-button type="warning" plain @click="openTransfer">划拨 / 迁移</el-button>
      <span class="page-tip">组内客户=组级共享 · 我的客户=个人负责</span>
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
        <el-table-column label="操作" width="120px" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </template>
    </DataTable>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="460px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="组名称" prop="groupName">
          <el-input v-model="form.groupName" placeholder="如：华东销售一组" />
        </el-form-item>
        <el-form-item label="负责人ID">
          <el-input-number v-model="form.ownerId" :min="1" placeholder="负责人（员工）ID" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="form.ownerName" placeholder="负责人姓名（展示）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="transferVisible" title="划拨到公司 / 划拨到组 / 批量迁移主要负责人" width="480px" :close-on-click-modal="false">
      <el-form label-width="110px">
        <el-form-item label="划拨类型">
          <el-select v-model="transferForm.transferType" style="width: 100%">
            <el-option
              v-for="t in TRANSFER_TYPES"
              :key="t.value"
              :value="t.value"
              :label="t.label"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="transferForm.transferType === 'group'" label="目标组ID">
          <el-input-number v-model="transferForm.targetId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="transferForm.transferType === 'owner'" label="主要负责人ID">
          <el-input-number v-model="transferForm.ownerId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="transferForm.transferType === 'company'" label="说明">
          <span class="page-tip">将当前选中的客户划拨到公司公共池（默认对全部客户生效）</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="transferVisible = false">取消</el-button>
        <el-button type="primary" :loading="transferSaving" @click="handleTransfer">提交</el-button>
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
