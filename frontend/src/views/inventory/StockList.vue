<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import SearchBar from '../../components/common/SearchBar.vue'
import DataTable from '../../components/common/DataTable.vue'
import type { TableColumn } from '../../components/common/DataTable.vue'
import SafeStockList from './SafeStockList.vue'
import InboundList from './InboundList.vue'
import OutboundList from './OutboundList.vue'
import TransferList from './TransferList.vue'
import CheckList from './CheckList.vue'
import BatchList from './BatchList.vue'
import { inventoryStockApi } from '../../api/inventory'
import type { InventoryStock } from '../../api/inventory'

/** 库存域容器：库存统计（默认）/ 安全库存 / 入库管理 / 出库发货 / 调拨 / 盘点 / 批号管理 */
const activeTab = ref('stock')

const loading = ref(false)
const list = ref<InventoryStock[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const query = reactive<{ keyword?: string }>({})

function fmtQty(v?: number): string {
  if (v == null || Number.isNaN(Number(v))) return '-'
  return Number(v).toLocaleString('zh-CN', { maximumFractionDigits: 2 })
}

/** 行标红：库龄 ≥ 预警阈值 */
function rowClassName({ row }: { row: Record<string, unknown> }): string {
  const s = row as unknown as InventoryStock
  const warnDays = s.ageWarnDays ?? 15
  if ((s.stockAge ?? 0) >= warnDays) return 'row-warn'
  return ''
}

const columns: TableColumn[] = [
  { prop: 'productName', label: '品名', minWidth: '140px' },
  { prop: 'grade', label: '牌号', width: '110px' },
  { prop: 'spec', label: '规格', minWidth: '110px' },
  { prop: 'actualQty', label: '实际', width: '110px', align: 'right', slot: 'actualQty' },
  { prop: 'transitQty', label: '在途', width: '110px', align: 'right', slot: 'transitQty' },
  { prop: 'stockAge', label: '库龄(天)', width: '100px', align: 'right', slot: 'stockAge' },
  { prop: 'ageWarnDays', label: '预警阈值', width: '100px', align: 'right' },
]

async function fetchList() {
  loading.value = true
  try {
    const data = await inventoryStockApi.list({ page: page.value, size: size.value, ...query })
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
const dialogTitle = ref('新增库存统计')
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<InventoryStock>({
  productName: '',
  grade: '',
  spec: '',
  actualQty: undefined,
  transitQty: undefined,
  stockAge: 0,
  ageWarnDays: 15,
})

const rules: FormRules = {
  productName: [{ required: true, message: '请输入品名', trigger: 'blur' }],
}

function openCreate() {
  dialogTitle.value = '新增库存统计'
  Object.assign(form, {
    id: undefined,
    productName: '',
    grade: '',
    spec: '',
    actualQty: undefined,
    transitQty: undefined,
    stockAge: 0,
    ageWarnDays: 15,
  })
  dialogVisible.value = true
}

function openEdit(row: InventoryStock) {
  dialogTitle.value = '编辑库存统计'
  Object.assign(form, {
    id: row.id,
    productName: row.productName ?? '',
    grade: row.grade ?? '',
    spec: row.spec ?? '',
    actualQty: row.actualQty,
    transitQty: row.transitQty,
    stockAge: row.stockAge ?? 0,
    ageWarnDays: row.ageWarnDays ?? 15,
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
      await inventoryStockApi.update(form.id, form)
      ElMessage.success('库存统计已更新')
    } else {
      await inventoryStockApi.create(form)
      ElMessage.success('库存统计已新增')
    }
    dialogVisible.value = false
    fetchList()
  } catch {
    // 错误已由拦截器提示
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: InventoryStock) {
  try {
    await ElMessageBox.confirm(
      `确定删除「${row.productName ?? row.id}」的库存统计吗？`,
      '删除确认',
      { type: 'warning' },
    )
  } catch {
    return
  }
  try {
    await inventoryStockApi.remove(row.id as number)
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
      <!-- 库存统计（默认页） -->
      <el-tab-pane label="库存统计" name="stock">
        <SearchBar @search="handleSearch" @reset="handleReset" @export="handleExport">
          <el-form-item label="关键词">
            <el-input
              v-model="query.keyword"
              placeholder="品名 / 牌号 / 规格"
              clearable
              style="width: 220px"
              @keyup.enter="handleSearch"
            />
          </el-form-item>
        </SearchBar>

        <div class="page-toolbar">
          <el-button type="primary" @click="openCreate">新增库存统计</el-button>
          <span class="page-tip">库龄 ≥ 预警阈值时整行标红预警</span>
        </div>

        <DataTable
          :columns="columns"
          :data="list"
          :loading="loading"
          :total="total"
          :page="page"
          :size="size"
          :row-class-name="rowClassName"
          @page-change="handlePageChange"
          @size-change="handleSizeChange"
        >
          <template #actualQty="{ row }">
            <span class="mono">{{ fmtQty(row.actualQty) }}</span>
          </template>
          <template #transitQty="{ row }">
            <span class="mono">{{ fmtQty(row.transitQty) }}</span>
          </template>
          <template #stockAge="{ row }">
            <span
              class="mono"
              :class="{ 'stock-age-warn': (row.stockAge ?? 0) >= (row.ageWarnDays ?? 15) }"
            >
              {{ row.stockAge ?? '-' }}
            </span>
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

      <el-tab-pane label="安全库存" name="safe-stock" lazy>
        <SafeStockList />
      </el-tab-pane>
      <el-tab-pane label="入库管理" name="inbound" lazy>
        <InboundList />
      </el-tab-pane>
      <el-tab-pane label="出库/发货" name="outbound" lazy>
        <OutboundList />
      </el-tab-pane>
      <el-tab-pane label="调拨" name="transfer" lazy>
        <TransferList />
      </el-tab-pane>
      <el-tab-pane label="盘点" name="check" lazy>
        <CheckList />
      </el-tab-pane>
      <el-tab-pane label="批号管理" name="batch" lazy>
        <BatchList />
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="品名" prop="productName">
          <el-input v-model="form.productName" placeholder="如：电解铜" />
        </el-form-item>
        <el-form-item label="牌号">
          <el-input v-model="form.grade" placeholder="如：Cu-CATH-1" />
        </el-form-item>
        <el-form-item label="规格">
          <el-input v-model="form.spec" placeholder="如：标准阴极" />
        </el-form-item>
        <el-form-item label="实际库存">
          <el-input-number v-model="form.actualQty" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="在途库存">
          <el-input-number v-model="form.transitQty" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="库龄(天)">
          <el-input-number v-model="form.stockAge" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="预警阈值">
          <el-input-number v-model="form.ageWarnDays" :min="1" style="width: 100%" />
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

.stock-age-warn {
  color: var(--color-alert);
  font-weight: 600;
}
</style>

<!-- 行标红样式需作用于 el-table 行内 td，使用非 scoped 全局类名（类名唯一，不影响其他页面） -->
<style>
.row-warn td.el-table__cell {
  background-color: color-mix(in srgb, var(--color-alert) 12%, transparent) !important;
}
</style>
