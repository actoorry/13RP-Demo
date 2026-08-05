<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import type { TreeInstance } from 'element-plus'
import ConfirmDialog from '../../components/common/ConfirmDialog.vue'
import { dictApi } from '../../api/org'
import type { OrgDict } from '../../api/org'

/** 树节点：字典项 + 分组根节点 */
interface DictNode extends OrgDict {
  type?: string
  children?: DictNode[]
}

const loading = ref(false)
const treeData = ref<DictNode[]>([])
const currentNode = ref<DictNode | null>(null)
const treeRef = ref<TreeInstance>()

const TYPE_NAMES: Record<string, string> = {
  org: '所属组织',
  position: '岗位人员',
}

async function fetchList() {
  loading.value = true
  try {
    const data = await dictApi.list({ page: 1, size: 1000 })
    const items = data?.list || []
    treeData.value = buildTree(items)
  } catch {
    treeData.value = []
  } finally {
    loading.value = false
  }
}

function buildTree(dicts: OrgDict[]): DictNode[] {
  const byType = new Map<string, OrgDict[]>()
  for (const d of dicts) {
    const key = d.dictType || 'org'
    if (!byType.has(key)) byType.set(key, [])
    byType.get(key)!.push(d)
  }
  const result: DictNode[] = []
  for (const [type, items] of byType) {
    result.push({
      id: -type.length, // 分组根节点用负 ID 占位，避免与真实字典项冲突
      name: TYPE_NAMES[type] || type,
      dictType: type,
      type: 'group',
      children: makeChildren(items),
    })
  }
  return result
}

function makeChildren(items: OrgDict[]): DictNode[] {
  const map = new Map<number, DictNode>()
  for (const item of items) {
    if (item.id == null) continue
    map.set(item.id, { ...item, type: item.dictType, children: [] })
  }
  const roots: DictNode[] = []
  for (const item of items) {
    if (item.id == null) continue
    const node = map.get(item.id)!
    const parent = item.parentId != null ? map.get(item.parentId) : undefined
    if (parent) parent.children!.push(node)
    else roots.push(node)
  }
  return roots
}

function handleNodeClick(data: DictNode) {
  currentNode.value = data
}

function getSelectedNode(): DictNode | null {
  if (currentNode.value) return currentNode.value
  const nodes = treeRef.value?.getCurrentNode()
  return (nodes as DictNode | undefined) || null
}

// ---------- 新增 / 编辑 ----------
const dialogVisible = ref(false)
const dialogTitle = ref('')
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<OrgDict>({ dictType: 'org', name: '', parentId: undefined, sort: 0 })

const rules: FormRules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
}

function resetForm() {
  Object.assign(form, { id: undefined, dictType: 'org', name: '', parentId: undefined, sort: 0 })
}

function openCreateTop() {
  resetForm()
  dialogTitle.value = '新增字典项'
  dialogVisible.value = true
}

function openCreateChild() {
  const node = getSelectedNode()
  if (!node) {
    ElMessage.warning('请先选中一个字典项')
    return
  }
  if (node.type === 'group') {
    resetForm()
    form.dictType = node.dictType || 'org'
    dialogTitle.value = `在「${TYPE_NAMES[form.dictType] || form.dictType}」下新增顶级字典项`
    dialogVisible.value = true
    return
  }
  resetForm()
  form.dictType = node.dictType || 'org'
  form.parentId = node.id
  dialogTitle.value = `在「${node.name}」下新增`
  dialogVisible.value = true
}

function openEdit() {
  const node = getSelectedNode()
  if (!node || node.type === 'group') {
    ElMessage.warning('请先选中一个字典项')
    return
  }
  Object.assign(form, {
    id: node.id,
    dictType: node.dictType || 'org',
    name: node.name,
    parentId: node.parentId,
    sort: node.sort ?? 0,
  })
  dialogTitle.value = `编辑「${node.name}」`
  dialogVisible.value = true
}

async function handleSave() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (form.id != null) {
      await dictApi.update(form.id, form)
      ElMessage.success('字典项已更新')
    } else {
      await dictApi.create(form)
      ElMessage.success('字典项已新增')
    }
    dialogVisible.value = false
    fetchList()
  } catch {
    // 错误已由拦截器提示
  } finally {
    saving.value = false
  }
}

// ---------- 删除 ----------
const deleteVisible = ref(false)
const deleteTarget = ref<DictNode | null>(null)
const deleting = ref(false)

function handleDelete() {
  const node = getSelectedNode()
  if (!node || node.type === 'group') {
    ElMessage.warning('请先选中一个字典项')
    return
  }
  deleteTarget.value = node
  deleteVisible.value = true
}

async function confirmDelete() {
  if (!deleteTarget.value) return
  deleting.value = true
  try {
    await dictApi.remove(deleteTarget.value.id as number)
    ElMessage.success('字典项已删除')
    deleteVisible.value = false
    fetchList()
  } catch {
    // 错误已由拦截器提示
  } finally {
    deleting.value = false
  }
}

onMounted(fetchList)
</script>

<template>
  <div class="page fade-up">
    <div class="page-toolbar">
      <el-button type="primary" @click="openCreateTop">新增字典项</el-button>
      <el-button @click="openCreateChild">新增下级</el-button>
      <el-button @click="openEdit">编辑</el-button>
      <el-button type="danger" plain @click="handleDelete">删除</el-button>
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
              <el-tag v-if="data.type === 'group'" size="small" type="primary">
                {{ data.dictType === 'org' ? '组织' : '岗位' }}
              </el-tag>
              <el-tag v-else size="small" type="info">{{ data.dictType === 'org' ? '组织' : '岗位' }}</el-tag>
            </span>
          </template>
        </el-tree>
        <el-empty v-if="!loading && !treeData.length" description="暂无字典数据" :image-size="72" />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="440px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="字典类型">
          <el-radio-group v-model="form.dictType">
            <el-radio value="org">组织</el-radio>
            <el-radio value="position">岗位</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="如：博宇集团 / 销售经理" />
        </el-form-item>
        <el-form-item label="显示顺序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <ConfirmDialog
      v-model="deleteVisible"
      title="删除字典项"
      :message="`确定删除字典项「${deleteTarget?.name ?? ''}」吗？其下级字典项也会被删除。`"
      :loading="deleting"
      @confirm="confirmDelete"
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
</style>
