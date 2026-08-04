<script setup lang="ts">
// 公司架构 · 机构管理（列参照功能清单 §二）
interface OrgRow {
  name: string
  sort: number
}
const orgRows: OrgRow[] = [
  { name: '博宇集团', sort: 1 },
  { name: '总裁办', sort: 2 },
  { name: '幸福盟事业部', sort: 3 },
  { name: '世贸项目部', sort: 4 },
  { name: '数据录入组', sort: 5 },
  { name: '采购供应部', sort: 6 },
]

// 公司架构 · 人员管理
interface StaffRow {
  account: string
  name: string
  phone: string
  dept: string
  position: string
}
const staffRows: StaffRow[] = [
  { account: 'admin', name: '系统管理员', phone: '13800000001', dept: '总裁办', position: '管理员' },
  { account: 'zhangsan', name: '张三', phone: '13900000002', dept: '世贸项目部', position: '数据录入' },
  { account: 'lisi', name: '李四', phone: '13700000003', dept: '幸福盟事业部', position: '组长' },
  { account: 'wangwu', name: '王五', phone: '13600000004', dept: '采购供应部', position: '数据录入' },
  { account: 'zhaoliu', name: '赵六', phone: '13500000005', dept: '总裁办', position: '组员' },
  { account: 'sunqi', name: '孙七', phone: '13400000006', dept: '数据录入组', position: '组员' },
]

// 公司架构 · 角色管理
interface RoleRow {
  name: string
  sort: number
}
const roleRows: RoleRow[] = [
  { name: '系统管理', sort: 1 },
  { name: 'CMS 权限', sort: 2 },
  { name: '组长权限', sort: 3 },
  { name: '组员权限', sort: 4 },
  { name: '数据录入', sort: 5 },
]
</script>

<template>
  <div class="module-page">
    <h2 class="module-title">公司架构</h2>

    <el-tabs>
      <el-tab-pane label="机构管理">
        <el-table :data="orgRows" stripe>
          <el-table-column prop="name" label="机构名称" min-width="180" />
          <el-table-column prop="sort" label="排序" width="100" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="人员管理">
        <el-table :data="staffRows" stripe>
          <el-table-column prop="account" label="账号" min-width="120" />
          <el-table-column prop="name" label="姓名" min-width="120" />
          <el-table-column prop="phone" label="手机" min-width="140" />
          <el-table-column prop="dept" label="部门" min-width="140" />
          <el-table-column prop="position" label="职位" min-width="120" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="角色管理">
        <el-table :data="roleRows" stripe>
          <el-table-column prop="name" label="角色名称" min-width="180" />
          <el-table-column prop="sort" label="排序" width="100" />
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
