<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const loading = ref(false)
// 默认提示账号 admin / 密码 123456
const form = reactive({
  account: 'admin',
  password: '123456',
})

async function handleLogin() {
  if (!form.account.trim() || !form.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }
  loading.value = true
  try {
    await auth.login({ account: form.account.trim(), password: form.password })
    ElMessage.success('登录成功')
    const redirect = (route.query.redirect as string) || '/admin'
    router.push(redirect)
  } catch {
    // 错误提示已由响应拦截器统一处理
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-card fade-up">
      <div class="login-brand">
        <div class="login-title">博宇宙十三维决策操作系统</div>
        <div class="login-subtitle">博宇企业管理平台 · 通用五模块</div>
      </div>

      <el-form label-position="top" @submit.prevent="handleLogin">
        <el-form-item label="账号">
          <el-input
            v-model="form.account"
            placeholder="请输入账号"
            autocomplete="username"
            size="large"
            clearable
          />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            autocomplete="current-password"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-button
          type="primary"
          size="large"
          class="login-btn"
          :loading="loading"
          @click="handleLogin"
        >
          登 录
        </el-button>
      </el-form>

      <div class="login-tip">默认账号：admin　默认密码：123456</div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background: radial-gradient(1200px 600px at 70% 20%, var(--color-glow, #16212e) 0%, var(--color-bg) 55%);
}

.login-card {
  width: 380px;
  padding: 36px 32px 24px;
  border-radius: 12px;
  background: var(--color-bg-panel);
  border: 1px solid var(--color-border);
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.12);
}

.login-brand {
  margin-bottom: 28px;
  text-align: center;
}

.login-title {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 3px;
  color: var(--color-primary);
}

.login-subtitle {
  margin-top: 8px;
  font-size: 12px;
  color: var(--color-text-secondary);
  letter-spacing: 1px;
}

.login-btn {
  width: 100%;
  margin-top: 8px;
}

.login-tip {
  margin-top: 18px;
  font-size: 12px;
  color: var(--color-text-muted);
  text-align: center;
}
</style>
