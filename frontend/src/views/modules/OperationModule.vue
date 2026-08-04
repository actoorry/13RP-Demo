<script setup lang="ts">
import { ref } from 'vue'

// 运营管理 · 客户运营（用户管理）
interface UserRow {
  name: string
  phone: string
  certified: string
  status: string
  regTime: string
  channel: string
}
const userRows: UserRow[] = [
  { name: '陈明', phone: '13512340001', certified: '是', status: '使用', regTime: '2026-08-01 10:20', channel: '微信小程序' },
  { name: '刘洋', phone: '13512340002', certified: '否', status: '使用', regTime: '2026-07-30 14:05', channel: 'Android' },
  { name: '周婷', phone: '13512340003', certified: '是', status: '使用', regTime: '2026-07-28 09:40', channel: 'iPhone' },
  { name: '吴刚', phone: '13512340004', certified: '否', status: '冻结', regTime: '2026-07-25 16:30', channel: '微信小程序' },
  { name: '郑华', phone: '13512340005', certified: '是', status: '使用', regTime: '2026-07-22 11:15', channel: 'Android' },
]

// 运营管理 · 认证管理
interface AuthRow {
  userName: string
  entName: string
  status: string
  authTime: string
}
const authRows: AuthRow[] = [
  { userName: '陈明', entName: '沈阳博宇会幸福实业有限公司', status: '待认证', authTime: '2026-08-01 10:25' },
  { userName: '周婷', entName: '威海恒邦矿冶发展有限公司', status: '待认证', authTime: '2026-07-28 09:45' },
  { userName: '郑华', entName: '广州新城市投资控股集团有限公司', status: '待认证', authTime: '2026-07-22 11:20' },
]

// 运营管理 · 多名头关联
interface MultiHeadRow {
  groupName: string
  entName: string
  creator: string
  createTime: string
  progress: string
}
const multiHeadRows: MultiHeadRow[] = [
  { groupName: '沈阳有色金属加工有限公司', entName: '銮麟金属贸易（沈阳）有限公司', creator: 'admin', createTime: '2026-07-20', progress: '已启用' },
  { groupName: '博宇集团', entName: '沈阳博宇会幸福实业有限公司', creator: 'admin', createTime: '2026-07-19', progress: '已启用' },
]

// 共享数据开关
const sharedData = ref(false)
</script>

<template>
  <div class="module-page">
    <h2 class="module-title">运营管理</h2>

    <el-tabs>
      <el-tab-pane label="客户运营">
        <el-table :data="userRows" stripe>
          <el-table-column prop="name" label="用户名称" min-width="120" />
          <el-table-column prop="phone" label="注册手机号" width="140" />
          <el-table-column prop="certified" label="企业是否认证" width="120" />
          <el-table-column prop="status" label="状态" width="100" />
          <el-table-column prop="regTime" label="注册时间" width="160" />
          <el-table-column prop="channel" label="注册渠道" width="120" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="认证管理">
        <el-table :data="authRows" stripe>
          <el-table-column prop="userName" label="用户名称" min-width="120" />
          <el-table-column prop="entName" label="企业名称" min-width="240" />
          <el-table-column prop="status" label="认证状态" width="120" />
          <el-table-column prop="authTime" label="认证时间" width="160" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="共享数据">
        <el-card class="shared-card">
          <div class="shared-row">
            <span class="shared-label">共享数据</span>
            <el-switch v-model="sharedData" />
          </div>
          <p class="shared-tip">当共享数据开启时，所有平台都可以看到数据。</p>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="多名头关联">
        <el-table :data="multiHeadRows" stripe>
          <el-table-column prop="groupName" label="集团名称（对外展示名称）" min-width="220" />
          <el-table-column prop="entName" label="关联名头企业名称" min-width="220" />
          <el-table-column prop="creator" label="创建人" width="120" />
          <el-table-column prop="createTime" label="创建时间" width="140" />
          <el-table-column prop="progress" label="进度" width="120" />
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

.shared-card {
  max-width: 480px;
}

.shared-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.shared-label {
  font-size: 14px;
  color: var(--color-text);
}

.shared-tip {
  margin: 12px 0 0;
  font-size: 13px;
  color: var(--color-text-secondary);
}
</style>
