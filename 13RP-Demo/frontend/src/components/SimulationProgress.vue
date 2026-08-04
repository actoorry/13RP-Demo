<template>
  <div>
    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px">
      <span style="font-size:13px; color:var(--text-dim)">7RP 方案推演</span>
      <span>
        <el-button size="small" @click="send('fast-forward')">快进 ⏩</el-button>
        <el-button size="small" type="warning" @click="send('skip-simulation')">跳过</el-button>
      </span>
    </div>

    <el-progress :percentage="Math.round(store.progress)" :stroke-width="14"
      :color="'#00d4aa'" striped striped-flow />

    <div class="sim-msg mono">{{ store.progressMsg || '推演启动中…' }}</div>

    <div style="margin-top:12px; color:var(--text-dim); font-size:12px; line-height:1.8">
      模拟蒙特卡洛扰动：1000 次随机采样<br />
      正在评估：改港 / 空运 / 备选供应商 / 组合微调…
    </div>
  </div>
</template>

<script setup lang="ts">
import { useDemoStore } from '../stores/demo';
import { useWebSocket } from '../composables/useWebSocket';

const store = useDemoStore();
const { send } = useWebSocket(() => {});
</script>

<style scoped>
.sim-msg {
  margin-top: 10px;
  font-size: 13px;
  color: var(--primary);
  min-height: 20px;
}
</style>
