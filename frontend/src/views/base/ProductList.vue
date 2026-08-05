<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import type { TreeInstance } from 'element-plus'
import ConfirmDialog from '../../components/common/ConfirmDialog.vue'
import { productApi } from '../../api/base'
import type { BaseProduct, ProductNode } from '../../api/base'
import { useAccountStore } from '../../stores/account'

const accountStore = useAccountStore()

const loading = ref(false)
const treeData = ref<ProductNode[]>([])
const currentNode = ref<ProductNode | null>(null)
const treeRef = ref<TreeInstance>()

const currentNodeType = computed(() => currentNode.value?.type ?? '')

/** 树节点类型名称 */
const TYPE_LABEL: Record<string, string> = {
  product: '品名',
  grade: '牌号',
  material: '材质',
}

async function fetchTree() {
  if (!accountStore.currentAccountId) {
    treeData.value = []
    return
  }
  loading.value = true
  try {
    const data = await productApi.tree(accountStore.currentAccountId)
    treeData.value = Array.isArray(data) ? data : []
  } catch {
    treeData.value = []
  } finally {
    loading.value = false
  }
}

function handleNodeClick(data: ProductNode) {
  currentNode.value = data
}

function getSelectedNode(): ProductNode | null {
  if (currentNode.value) return currentNode.value
  const nodes = treeRef.value?.getCurrentNode()
  return (nodes as ProductNode | undefined) || null
}

// ---------- 新增 / 编辑 ----------
const dialogVisible = ref(false)
const dialogTitle = ref('')
/** 表单类型：product 品名 / grade 牌号 / material 材质 */
const formType = ref<'product' | 'grade' | 'material'>('product')
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<BaseProduct>({
  accountId: undefined,
  name: '',
  grade: '',
  material: '',
  spec: '',
  brandOrigin: '',
  other: '',
  parentId: undefined,
  sort: 0,
  status: 1,
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入品名', trigger: 'blur' }],
  grade: [{ required: true, message: '请输入牌号', trigger: 'blur' }],
  material: [{ required: true, message: '请输入材质', trigger: 'blur' }],
}

function resetForm() {
  Object.assign(form, {
    id: undefined,
    accountId: accountStore.currentAccountId ?? undefined,
    name: '',
    grade: '',
    material: '',
    spec: '',
    brandOrigin: '',
    other: '',
    parentId: undefined,
    sort: 0,
    status: 1,
  })
}

/** 新增顶级品名 */
function openCreateProduct() {
  resetForm()
  formType.value = 'product'
  dialogTitle.value = '新增品名'
  dialogVisible.value = true
}

/** 新增下级（牌号 / 材质） */
function openCreateChild() {
  const node = getSelectedNode()
  if (!node) {
    ElMessage.warning('请先选中一个品名或牌号节点')
    return
  }
  resetForm()
  form.parentId = node.id
  if (node.type === 'product') {
    formType.value = 'grade'
    form.name = node.name
    dialogTitle.value = `为品名「${node.name}」新增牌号`
  } else if (node.type === 'grade') {
    formType.value = 'material'
    form.name = node.name
    form.grade = node.name
    dialogTitle.value = `为牌号「${node.name}」新增材质`
  } else {
    ElMessage.warning('材质节点下不能再新增下级')
    return
  }
  dialogVisible.value = true
}

function openEdit() {
  const node = getSelectedNode()
  if (!node) {
    ElMessage.warning('请先选中一个树节点')
    return
  }
  resetForm()
  formType.value = (node.type as 'product' | 'grade' | 'material') || 'product'
  form.id = node.id
  if (node.type === 'product') {
    form.name = node.name
    dialogTitle.value = `编辑品名「${node.name}」`
  } else if (node.type === 'grade') {
    form.grade = node.name
    dialogTitle.value = `编辑牌号「${node.name}」`
  } else {
    form.material = node.name
    dialogTitle.value = `编辑材质「${node.name}」`
  }
  dialogVisible.value = true
}

/** 根据表单类型组装提交载荷 */
function buildPayload(): BaseProduct {
  const base = { accountId: form.accountId, sort: form.sort ?? 0, status: form.status ?? 1 }
  if (formType.value === 'product') {
    return {
      ...base,
      name: form.name,
      spec: form.spec,
      brandOrigin: form.brandOrigin,
      other: form.other,
    }
  }
  if (formType.value === 'grade') {
    return { ...base, name: form.name, grade: form.grade, parentId: form.parentId }
  }
  return {
    ...base,
    name: form.name,
    grade: form.grade,
    material: form.material,
    parentId: form.parentId,
  }
}

async function handleSave() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const payload = buildPayload()
    if (form.id != null) {
      await productApi.update(form.id, payload)
      ElMessage.success('产品主数据已更新')
    } else {
      await productApi.create(payload)
      ElMessage.success('产品主数据已新增')
    }
    dialogVisible.value = false
    fetchTree()
  } catch {
    // 错误已由拦截器提示
  } finally {
    saving.value = false
  }
}

// ---------- 作废（级联） ----------
const obsoleteVisible = ref(false)
const obsoleteTarget = ref<ProductNode | null>(null)
const obsoleting = ref(false)

function handleObsolete() {
  const node = getSelectedNode()
  if (!node) {
    ElMessage.warning('请先选中一个树节点')
    return
  }
  obsoleteTarget.value = node
  obsoleteVisible.value = true
}

async function confirmObsolete() {
  if (!obsoleteTarget.value) return
  obsoleting.value = true
  try {
    // 作废走 DELETE /api/base/product/{id} → 后端级联作废下级数据
    await productApi.remove(obsoleteTarget.value.id)
    ElMessage.success(`「${obsoleteTarget.value.name}」已作废，下级数据一并作废`)
    obsoleteVisible.value = false
    fetchTree()
  } catch {
    // 错误已由拦截器提示
  } finally {
    obsoleting.value = false
  }
}

onMounted(async () => {
  if (!accountStore.accounts.length) {
    await accountStore.fetchAccounts().catch(() => {})
  }
  fetchTree()
})
</script>

<template>
  <div class="page fade-up">
    <div class="page-toolbar">
      <span class="page-account">
        当前账套：{{ accountStore.currentAccount?.name || '未选择' }}
      </span>
      <el-button type="primary" @click="openCreateProduct">新增品名</el-button>
      <el-button :disabled="currentNodeType !== 'product'" @click="openCreateChild">
        新增牌号
      </el-button>
      <el-button :disabled="currentNodeType !== 'grade'" @click="openCreateChild">
        新增材质
      </el-button>
      <el-button :disabled="!currentNode" @click="openEdit">编辑</el-button>
      <el-button type="danger" plain :disabled="!currentNode" @click="handleObsolete">
        作废
      </el-button>
    </div>

    <el-card class="tree-card" shadow="never">
      <div v-loading="loading" class="tree-body">
        <el-tree
          ref="treeRef"
          :data="treeData"
          :props="{ label: 'name', children: 'children' }"
          node-key="id"
          default-expand-all
          highlight-current
          @node-click="handleNodeClick"
        >
          <template #default="{ data }">
            <span class="tree-node">
              <span class="tree-node-name">{{ data.name }}</span>
              <el-tag size="small" class="tree-node-tag" :type="data.type === 'product' ? 'primary' : data.type === 'grade' ? 'warning' : 'info'">
                {{ TYPE_LABEL[data.type] || data.type }}
              </el-tag>
            </span>
          </template>
        </el-tree>
        <el-empty v-if="!loading && !treeData.length" description="暂无产品数据，点击「新增品名」开始维护" :image-size="72" />
      </div>
    </el-card>

    <!-- 新增 / 编辑 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item v-if="formType === 'product'" label="品名" prop="name">
          <el-input v-model="form.name" placeholder="如：电解铜" />
        </el-form-item>
        <el-form-item v-if="formType === 'product'" label="规格">
          <el-input v-model="form.spec" placeholder="如：99.99%" />
        </el-form-item>
        <el-form-item v-if="formType === 'product'" label="品牌/产地">
          <el-input v-model="form.brandOrigin" placeholder="如：北方铜业/国产" />
        </el-form-item>
        <el-form-item v-if="formType === 'product'" label="其他">
          <el-input v-model="form.other" placeholder="备注信息" />
        </el-form-item>

        <el-form-item v-if="formType === 'grade'" label="牌号" prop="grade">
          <el-input v-model="form.grade" placeholder="如：1#" />
        </el-form-item>
        <el-form-item v-if="formType === 'material'" label="材质" prop="material">
          <el-input v-model="form.material" placeholder="如：Cu 电解铜" />
        </el-form-item>

        <el-form-item label="显示顺序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">作废</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 作废级联确认 -->
    <ConfirmDialog
      v-model="obsoleteVisible"
      title="作废产品"
      :message="`确定作废「${obsoleteTarget?.name ?? ''}」吗？其下级数据（牌号/材质）都会被作废。`"
      :loading="obsoleting"
      @confirm="confirmObsolete"
    />
  </div>
</template>

<style scoped>
.page-toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.page-account {
  margin-right: auto;
  font-size: 13px;
  color: var(--color-text-secondary);
}

.tree-card {
  background: var(--color-bg-panel);
  border-color: var(--color-border);
}

.tree-body {
  min-height: 360px;
  max-height: 560px;
  overflow: auto;
}

.tree-node {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.tree-node-name {
  font-size: 13px;
}

.tree-node-tag {
  transform: scale(0.85);
}
</style>
