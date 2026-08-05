<script setup lang="ts">
// 商品管理 · 频道信息管理
interface ChannelRow {
  name: string
  status: string
}
const channelRows: ChannelRow[] = [
  { name: '石油化工', status: '使用中' },
  { name: '火电业脑技术', status: '使用中' },
  { name: '办公设备', status: '使用中' },
  { name: '特种设备', status: '使用中' },
  { name: '农业机械', status: '待使用' },
]

// 商品管理 · 品类管理
interface CategoryRow {
  name: string
  channel: string
  sort: number
  visible: string
}
const categoryRows: CategoryRow[] = [
  { name: '办公设备-一级分类', channel: '办公设备', sort: 1, visible: '是' },
  { name: '特种设备-二级分类', channel: '特种设备', sort: 2, visible: '是' },
  { name: '仪器仪表', channel: '火电业脑技术', sort: 3, visible: '是' },
  { name: '房地产（三级）', channel: '办公设备', sort: 4, visible: '否' },
  { name: '信软技术服务（三级）', channel: '办公设备', sort: 5, visible: '是' },
]

// 商品管理 · 属性管理
interface AttributeRow {
  name: string
  category: string
  enabled: string
}
const attributeRows: AttributeRow[] = [
  { name: '品牌/产地', category: '办公设备', enabled: '是' },
  { name: '规格', category: '特种设备', enabled: '是' },
  { name: '材质', category: '石油化工', enabled: '是' },
  { name: '牌号', category: '仪器仪表', enabled: '是' },
  { name: '型号', category: '办公设备', enabled: '否' },
]

// 商品管理 · 规则管理
interface RuleRow {
  category: string
  attrName: string
  attrValue: string
  publishAt: string
}
const ruleRows: RuleRow[] = [
  { category: '铜系列', attrName: 'Cu', attrValue: '含量≥99.9%', publishAt: '2026-07-20' },
  { category: '铜系列', attrName: 'Zn', attrValue: '含量≥99%', publishAt: '2026-07-18' },
  { category: '铜系列', attrName: 'Al', attrValue: '含量≥99.95%', publishAt: '2026-07-15' },
]

// 商品管理 · 单位管理
interface UnitRow {
  name: string
  category: string
  enabled: string
}
const unitRows: UnitRow[] = [
  { name: '公斤', category: '石油化工', enabled: '是' },
  { name: '吨', category: '石油化工', enabled: '是' },
  { name: '件', category: '办公设备', enabled: '是' },
  { name: '台', category: '特种设备', enabled: '是' },
  { name: '米', category: '仪器仪表', enabled: '否' },
]

// 商品管理 · 单位换算管理
interface ConversionRow {
  unit: string
  target: string
  enabled: string
}
const conversionRows: ConversionRow[] = [
  { unit: '吨', target: '公斤（1 吨 = 1000 公斤）', enabled: '是' },
  { unit: '千克', target: '克（1 千克 = 1000 克）', enabled: '是' },
  { unit: '件', target: '箱（1 箱 = 12 件）', enabled: '是' },
  { unit: '米', target: '厘米（1 米 = 100 厘米）', enabled: '是' },
]

// 商品管理 · 生态管理
interface EcologyRow {
  category: string
  name: string
  creator: string
  createTime: string
}
const ecologyRows: EcologyRow[] = [
  { category: '铜原料', name: '供求信息发布', creator: '张工', createTime: '2026-07-01' },
  { category: '矿冶产品', name: '企业信息发布', creator: '张工', createTime: '2026-07-02' },
  { category: '石油化工', name: '金刚区', creator: '张工', createTime: '2026-06-30' },
]

// 商品管理 · 生态管理管理
interface EcologyMgmtRow {
  name: string
  creator: string
  createTime: string
  editTime: string
}
const ecologyMgmtRows: EcologyMgmtRow[] = [
  { name: '企业信息发布', creator: '李工', createTime: '2026-07-02', editTime: '2026-07-05' },
  { name: '供求信息发布', creator: '李工', createTime: '2026-07-01', editTime: '2026-07-04' },
  { name: '金刚区', creator: '李工', createTime: '2026-06-28', editTime: '2026-07-03' },
]

// 商品管理 · 合同要素管理
interface ContractRow {
  name: string
  channel: string
  creator: string
  settleMethod: string
  freightBearer: string
  availableQty: number
}
const contractRows: ContractRow[] = [
  { name: '电解铜供应合同', channel: '石油化工', creator: '王工', settleMethod: '现结', freightBearer: '供方承担', availableQty: 2000 },
  { name: '办公设备采购合同', channel: '办公设备', creator: '王工', settleMethod: '月结', freightBearer: '需方承担', availableQty: 500 },
  { name: '特种设备租赁合同', channel: '特种设备', creator: '王工', settleMethod: '现结', freightBearer: '供方承担', availableQty: 120 },
]

// 商品管理 · 合同要素项管理
interface ContractItemRow {
  name: string
  creator: string
  modifyTime: string
}
const contractItemRows: ContractItemRow[] = [
  { name: '装货地址', creator: '王工', modifyTime: '2026-07-03' },
  { name: '卸货地址', creator: '王工', modifyTime: '2026-07-03' },
  { name: '品名', creator: '王工', modifyTime: '2026-07-02' },
]
</script>

<template>
  <div class="module-page">
    <h2 class="module-title">商品管理</h2>

    <el-tabs>
      <el-tab-pane label="频道管理">
        <el-table :data="channelRows" stripe>
          <el-table-column prop="name" label="频道名称" min-width="200" />
          <el-table-column prop="status" label="状态" width="120" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="品类管理">
        <el-table :data="categoryRows" stripe>
          <el-table-column prop="name" label="品类名称" min-width="200" />
          <el-table-column prop="channel" label="所属频道" width="140" />
          <el-table-column prop="sort" label="排序" width="100" />
          <el-table-column prop="visible" label="是否显示" width="100" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="属性管理">
        <el-table :data="attributeRows" stripe>
          <el-table-column prop="name" label="属性名称" min-width="180" />
          <el-table-column prop="category" label="关联品类" min-width="160" />
          <el-table-column prop="enabled" label="是否启用" width="100" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="规则管理">
        <el-table :data="ruleRows" stripe>
          <el-table-column prop="category" label="关联品类" min-width="160" />
          <el-table-column prop="attrName" label="属性名" min-width="140" />
          <el-table-column prop="attrValue" label="属性值" min-width="180" />
          <el-table-column prop="publishAt" label="发布时间" width="140" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="单位管理">
        <el-table :data="unitRows" stripe>
          <el-table-column prop="name" label="单位名称" min-width="180" />
          <el-table-column prop="category" label="关联品类" min-width="160" />
          <el-table-column prop="enabled" label="是否启用" width="100" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="单位换算">
        <el-table :data="conversionRows" stripe>
          <el-table-column prop="unit" label="单位名称" min-width="160" />
          <el-table-column prop="target" label="转换单位" min-width="240" />
          <el-table-column prop="enabled" label="是否启用" width="100" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="生态管理">
        <el-table :data="ecologyRows" stripe>
          <el-table-column prop="category" label="品类名称" min-width="180" />
          <el-table-column prop="name" label="生态名称" min-width="180" />
          <el-table-column prop="creator" label="创建人" width="120" />
          <el-table-column prop="createTime" label="创建时间" width="140" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="生态管理管理">
        <el-table :data="ecologyMgmtRows" stripe>
          <el-table-column prop="name" label="文章生态名称" min-width="200" />
          <el-table-column prop="creator" label="创建人" width="120" />
          <el-table-column prop="createTime" label="创建时间" width="140" />
          <el-table-column prop="editTime" label="编辑时间" width="140" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="合同要素管理">
        <el-table :data="contractRows" stripe>
          <el-table-column prop="name" label="要素名称" min-width="200" />
          <el-table-column prop="channel" label="所属频道" width="140" />
          <el-table-column prop="creator" label="创建人" width="120" />
          <el-table-column prop="settleMethod" label="结算方式" width="120" />
          <el-table-column prop="freightBearer" label="运费承担" width="120" />
          <el-table-column prop="availableQty" label="可供数量" width="100" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="合同要素项管理">
        <el-table :data="contractItemRows" stripe>
          <el-table-column prop="name" label="要素项名称" min-width="200" />
          <el-table-column prop="creator" label="创建人" width="120" />
          <el-table-column prop="modifyTime" label="修改时间" width="140" />
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<style scoped>
.module-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 16px;
  color: var(--color-text);
}
</style>
