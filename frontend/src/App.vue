<script setup lang="ts">
// 13RP 决策演示——P0 深色占位页
// 后续任务将在此挂载 4RP 看板 / 6RP 孪生 / 决策交互面板
</script>

<template>
  <div class="placeholder-page">
    <div class="grid-overlay" aria-hidden="true"></div>

    <main class="content">
      <div class="badge">13RP · 博宇宙十三维决策操作系统</div>
      <h1 class="title">13RP 决策演示</h1>
      <p class="subtitle">多宇宙资源规划与决策系统 —— P0 阶段</p>

      <div class="status-row">
        <el-tag type="success" effect="dark">骨架就绪</el-tag>
        <el-tag type="info" effect="dark">Vue3 + Vite6 + Element Plus</el-tag>
      </div>
    </main>

    <footer class="footer">V0.3</footer>
  </div>
</template>

<style scoped>
.placeholder-page {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #0d1117;
  color: #e6edf3;
  overflow: hidden;
}

/* 决策驾驶舱网格底纹 */
.grid-overlay {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(0, 212, 170, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 212, 170, 0.05) 1px, transparent 1px);
  background-size: 40px 40px;
  pointer-events: none;
}

.content {
  position: relative;
  text-align: center;
  padding: 24px;
}

.badge {
  font-size: 14px;
  letter-spacing: 4px;
  color: #00d4aa;
  margin-bottom: 16px;
}

.title {
  font-size: 48px;
  font-weight: 600;
  margin: 0 0 12px;
  letter-spacing: 2px;
}

.subtitle {
  font-size: 16px;
  color: #8b949e;
  margin: 0 0 32px;
}

.status-row {
  display: flex;
  justify-content: center;
  gap: 12px;
}

.footer {
  position: absolute;
  bottom: 20px;
  font-size: 13px;
  color: #484f58;
  letter-spacing: 1px;
}
</style>
