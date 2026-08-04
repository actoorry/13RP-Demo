<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useDemoStore } from '../stores/demo'
import { PHASE_LABELS } from '../constants/demo'

defineProps<{ connected: boolean }>()

const store = useDemoStore()
const now = ref(new Date())
let timer: ReturnType<typeof setInterval> | null = null

const timeText = computed(() => now.value.toLocaleTimeString('zh-CN', { hour12: false }))
const phaseLabel = computed(() => PHASE_LABELS[store.phase] ?? store.phase)

onMounted(() => {
  timer = setInterval(() => {
    now.value = new Date()
  }, 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<template>
  <div class="status-bar">
    <div class="status-item">
      <span class="status-dot" :class="connected ? 'online' : 'offline'"></span>
      <span>WebSocket {{ connected ? '已连接' : '未连接' }}</span>
    </div>

    <div class="status-item">
      <span class="status-key">阶段</span>
      <span class="status-phase">{{ phaseLabel }}</span>
    </div>

    <div class="status-item">
      <span class="status-key">当前时间</span>
      <span class="mono">{{ timeText }}</span>
    </div>

    <div class="status-item status-version mono">V0.3</div>
  </div>
</template>

<style scoped>
.status-bar {
  display: flex;
  align-items: center;
  gap: 24px;
  height: 36px;
  padding: 0 16px;
  background: #0a0e13;
  border-top: 1px solid var(--color-border);
  font-size: 12px;
  color: var(--color-text-secondary);
  grid-area: status;
}

.status-item {
  display: flex;
  align-items: center;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
  margin-right: 6px;
}

.status-dot.online {
  background: var(--color-primary);
  box-shadow: 0 0 6px var(--color-primary);
}

.status-dot.offline {
  background: var(--color-alert);
  box-shadow: 0 0 6px var(--color-alert);
}

.status-key {
  color: var(--color-text-muted);
  margin-right: 6px;
}

.status-phase {
  color: var(--color-primary);
}

.status-version {
  margin-left: auto;
  letter-spacing: 1px;
  color: var(--color-text-muted);
}
</style>
