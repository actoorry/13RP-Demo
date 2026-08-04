<template>
  <div class="status-bar">
    <span>
      <span class="dot" :style="{ background: connected ? 'var(--primary)' : 'var(--alert)' }" />
      WebSocket {{ connected ? '已连接' : '重连中…' }}
    </span>
    <span>阶段：{{ PHASE_LABEL[store.phase] || store.phase }}</span>
    <span class="mono">{{ now }}</span>
    <span class="mono">博宇四方复现版 · 13RP 决策演示 V0.3</span>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useDemoStore } from '../stores/demo';
import { PHASE_LABEL } from '../utils/constants';

const store = useDemoStore();
const connected = ref(false);
const now = ref('');

// 通过 demo-state 消息到达率判断连接（简单实现）
store.$subscribe((_m, state) => {
  connected.value = true;
});
setInterval(() => {
  now.value = new Date().toLocaleTimeString('zh-CN', { hour12: false });
}, 1000);
</script>

<style scoped>
.status-bar {
  grid-column: 1 / -1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 12px;
  background: var(--panel);
  border: 1px solid var(--panel-border);
  border-radius: 6px;
  font-size: 12px;
  color: var(--text-dim);
}
.dot { display: inline-block; width: 8px; height: 8px; border-radius: 50%; margin-right: 4px; }
</style>
