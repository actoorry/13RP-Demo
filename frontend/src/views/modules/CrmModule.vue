<script setup lang="ts">
// CRM · 线索/客户（列参照功能清单 §三）
interface CrmRow {
  name: string
  source: string
  entType: string
  phone: string
  level: string
  nextContact: string
}
const leadRows: CrmRow[] = [
  { name: '沈阳某化工采购线索', source: '官网咨询', entType: '生产企业', phone: '13812340001', level: 'A', nextContact: '2026-08-06' },
  { name: '宁波港物流客户', source: '展会', entType: '物流企业', phone: '13812340002', level: 'B', nextContact: '2026-08-10' },
  { name: '上海某贸易公司', source: '转介绍', entType: '贸易企业', phone: '13812340003', level: 'A', nextContact: '2026-08-05' },
  { name: '青岛制造业客户', source: '平台注册', entType: '生产企业', phone: '13812340004', level: 'C', nextContact: '2026-08-15' },
  { name: '广州某电商企业', source: '电话营销', entType: '电商企业', phone: '13812340005', level: 'B', nextContact: '2026-08-08' },
]
const customerRows: CrmRow[] = [
  { name: '威海恒邦矿冶发展有限公司', source: '平台注册', entType: '矿冶企业', phone: '13912340001', level: 'A', nextContact: '2026-08-07' },
  { name: '广州新城市投资控股集团', source: '转介绍', entType: '投资集团', phone: '13912340002', level: 'B', nextContact: '2026-08-12' },
  { name: '南京朗诗物业管理有限公司', source: '官网咨询', entType: '物业企业', phone: '13912340003', level: 'A', nextContact: '2026-08-05' },
  { name: '北京某供应链公司', source: '展会', entType: '供应链企业', phone: '13912340004', level: 'C', nextContact: '2026-08-20' },
  { name: '杭州某科技企业', source: '平台注册', entType: '科技企业', phone: '13912340005', level: 'B', nextContact: '2026-08-09' },
]

// 自定义字段设置（列参照功能清单 §三.5）
interface FieldRow {
  module: string
  updatedAt: string
}
const fieldRows: FieldRow[] = [
  { module: '线索管理', updatedAt: '2026-08-01 10:30' },
  { module: '客户管理', updatedAt: '2026-07-28 14:20' },
  { module: '联系人管理', updatedAt: '2026-07-25 09:00' },
  { module: '产品管理', updatedAt: '2026-07-20 16:45' },
]
</script>

<template>
  <div class="module-page">
    <h2 class="module-title">CRM</h2>

    <el-tabs>
      <el-tab-pane label="线索管理">
        <el-table :data="leadRows" stripe>
          <el-table-column prop="name" label="线索名称" min-width="200" />
          <el-table-column prop="source" label="线索来源" width="120" />
          <el-table-column prop="entType" label="企业类型" width="120" />
          <el-table-column prop="phone" label="手机" width="140" />
          <el-table-column prop="level" label="客户级别" width="100" />
          <el-table-column prop="nextContact" label="下次联系时间" width="140" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="客户管理">
        <el-table :data="customerRows" stripe>
          <el-table-column prop="name" label="客户名称" min-width="200" />
          <el-table-column prop="source" label="客户来源" width="120" />
          <el-table-column prop="entType" label="企业类型" width="120" />
          <el-table-column prop="phone" label="手机" width="140" />
          <el-table-column prop="level" label="客户级别" width="100" />
          <el-table-column prop="nextContact" label="下次联系时间" width="140" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="线索池">
        <el-table :data="leadRows" stripe>
          <el-table-column prop="name" label="线索名称" min-width="200" />
          <el-table-column prop="source" label="线索来源" width="120" />
          <el-table-column prop="entType" label="企业类型" width="120" />
          <el-table-column prop="phone" label="手机" width="140" />
          <el-table-column prop="level" label="客户级别" width="100" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="客户公海">
        <el-table :data="customerRows" stripe>
          <el-table-column prop="name" label="客户名称" min-width="200" />
          <el-table-column prop="source" label="客户来源" width="120" />
          <el-table-column prop="entType" label="企业类型" width="120" />
          <el-table-column prop="phone" label="手机" width="140" />
          <el-table-column prop="level" label="客户级别" width="100" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="自定义字段">
        <el-table :data="fieldRows" stripe>
          <el-table-column prop="module" label="模块" min-width="180" />
          <el-table-column prop="updatedAt" label="更新时间" width="180" />
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
