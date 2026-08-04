<script setup lang="ts">
import { computed } from 'vue'
import { useDemoStore } from '../stores/demo'

const emit = defineEmits<{
  (e: 'fast-forward'): void
  (e: 'skip'): void
  (e: 'start-optimization'): void
}>()

const store = useDemoStore()
const done = computed(() => store.phase === 'SIMULATION_DONE')
const percent = computed(() => Math.max(0, Math.min(100, Math.round(store.progress))))
</script>

<template>
  <section class="sim-panel fade-up">
    <template v-if="!done">
      <h3 class="panel-title">7RP 方案推演</h3>
      <el-progress
        :percentage="percent"
        :stroke-width="18"
        :show-text="false"
        striped
        striped-flow
        color="var(--color-primary)"
      />
      <div class="sim-percent mono">{{ percent }}%</div>
      <div class="sim-msg">
        {{ store.progressMsg || '正在平行宇宙中遍历全部可行路径…' }}
      </div>
      <div class="sim-actions">
        <el-button size="small" @click="emit('fast-forward')">快进 ⏩</el-button>
        <el-button size="small" type="warning" plain @click="emit('skip')">跳过</el-button>
      </div>
    </template>

    <template v-else>
      <h3 class="panel-title">推演完成</h3>
      <div class="done-box">
        <div class="done-title">✅ 共评估 342 条路径，筛选出 3 条可行方案</div>
        <div class="done-sub">系统已完成帕累托前沿筛选，等待启动 8RP 多目标寻优。</div>
      </div>
      <el-button type="primary" class="inject-btn" @click="emit('start-optimization')">
        启动 8RP 多目标寻优
      </el-button>
    </template>
  </section>
</template>

<style scoped>
.sim-percent {
  font-size: 28px;
  font-weight: 700;
  color: var(--color-primary);
  text-align: center;
  margin: 12px 0 8px;
}

.sim-msg {
  font-size: 12px;
  color: var(--color-text-secondary);
  text-align: center;
  min-height: 36px;
  margin-bottom: 12px;
  line-height: 1.7;
}

.sim-actions {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.done-box {
  background: rgba(0, 212, 170, 0.06);
  border: 1px solid rgba(0, 212, 170, 0.3);
  border-radius: 6px;
  padding: 12px;
  margin-bottom: 16px;
}

.done-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-primary);
  margin-bottom: 4px;
}

.done-sub {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.inject-btn {
  width: 100%;
  font-weight: 600;
  letter-spacing: 1px;
}
</style>
