<script setup lang="ts">
import { computed, onMounted, watch } from 'vue'
import { useDemoStore } from '../stores/demo'
import { useWebSocket } from '../composables/useWebSocket'
import DashboardPanel from '../components/DashboardPanel.vue'
import LogisticsMap from '../components/LogisticsMap.vue'
import EventTrigger from '../components/EventTrigger.vue'
import SimulationProgress from '../components/SimulationProgress.vue'
import PlanCards from '../components/PlanCards.vue'
import GamePanel from '../components/GamePanel.vue'
import InstructionList from '../components/InstructionList.vue'
import StatusBar from '../components/StatusBar.vue'

const store = useDemoStore()
const { connected, send } = useWebSocket()

const rightPanel = computed(() => {
  switch (store.phase) {
    case 'EVENT_INJECTED':
      return 'event'
    case 'SIMULATING':
    case 'SIMULATION_DONE':
      return 'progress'
    case 'OPTIMIZING':
      return 'plans'
    case 'GAMING':
      return 'game'
    case 'PLAN_SELECTED':
    case 'DONE':
      return 'instruction'
    case 'INIT':
    default:
      return 'event'
  }
})

function handleStart(duration: number) {
  send('trigger-event', { eventType: 'supplier_shortage', duration })
}
function handleStartSimulation() {
  send('start-simulation')
}
function handleFastForward() {
  send('fast-forward')
}
function handleSkip() {
  send('skip-simulation')
}
function handleStartOptimization() {
  send('start-optimization')
}
function handleStartGaming() {
  send('start-gaming')
}
function handleConfirm(planId: string) {
  store.planId = planId
  send('confirm-plan', { planId })
}
function handleReset() {
  send('reset')
}

function restoreByPhase() {
  const phase = store.phase
  if (['OPTIMIZING', 'GAMING', 'PLAN_SELECTED', 'DONE'].includes(phase) && !store.solutions.length) {
    store.fetchSolutions()
  }
  if (['GAMING', 'PLAN_SELECTED', 'DONE'].includes(phase) && !store.gameResults) {
    store.fetchGameResults()
  }
  if (['PLAN_SELECTED', 'DONE'].includes(phase)) {
    store.fetchInstructions(store.planId || 'P1')
  }
}

// 刷新恢复：拉取 state 后按 phase 补拉数据（见已知坑 #8）
onMounted(async () => {
  await store.fetchState()
  restoreByPhase()
})

// 运行时 phase 推进：按需补拉方案/博弈/指令数据
watch(
  () => store.phase,
  (phase) => {
    if (phase === 'OPTIMIZING' && !store.solutions.length) store.fetchSolutions()
    if (phase === 'GAMING' && !store.gameResults) store.fetchGameResults()
    if ((phase === 'PLAN_SELECTED' || phase === 'DONE') && !store.instructions.length) {
      store.fetchInstructions(store.planId || 'P1')
    }
  },
)
</script>

<template>
  <div class="app-shell">
    <aside class="left-panel panel">
      <DashboardPanel />
    </aside>

    <main class="center-panel">
      <LogisticsMap />
    </main>

    <aside class="right-panel panel">
      <EventTrigger
        v-if="rightPanel === 'event'"
        @start="handleStart"
        @start-simulation="handleStartSimulation"
      />
      <SimulationProgress
        v-else-if="rightPanel === 'progress'"
        @fast-forward="handleFastForward"
        @skip="handleSkip"
        @start-optimization="handleStartOptimization"
      />
      <PlanCards v-else-if="rightPanel === 'plans'" @start-gaming="handleStartGaming" />
      <GamePanel v-else-if="rightPanel === 'game'" @confirm="handleConfirm" />
      <InstructionList v-else-if="rightPanel === 'instruction'" @reset="handleReset" />
    </aside>

    <StatusBar :connected="connected" />
  </div>
</template>

<style scoped>
/* 嵌入管理端内容区：三屏布局由视口满高降为容器内满高，避免与顶栏叠加产生纵向滚动 */
.app-shell {
  height: 100%;
  min-height: 480px;
}
</style>
