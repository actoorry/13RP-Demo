<script setup lang="ts">
import { computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
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
import ThemeToggle from '../components/ThemeToggle.vue'

const router = useRouter()
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
    <div class="demo-theme-toggle">
      <el-tooltip content="返回管理端" placement="bottom">
        <button
          type="button"
          class="back-admin"
          aria-label="返回管理端"
          @click="router.push('/admin')"
        >
          <span class="back-admin-text">返回管理端</span>
        </button>
      </el-tooltip>
      <ThemeToggle />
    </div>

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
  position: relative;
  height: 100%;
  min-height: 480px;
}

/* 右上角工具区：绝对定位右上角，不参与三屏 grid 布局，z-index 置顶；左返回管理端、右主题切换 */
.demo-theme-toggle {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 1000;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 返回管理端按钮：与 ThemeToggle 同风格，深色驾驶舱 + 日间模式均用主题变量 */
.back-admin {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 32px;
  padding: 0 12px;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-bg-elevated);
  color: var(--color-text-secondary);
  font-family: var(--font-main);
  font-size: 13px;
  cursor: pointer;
  transition: background-color 0.3s var(--ease-out), border-color 0.3s var(--ease-out),
    color 0.3s var(--ease-out);
}

.back-admin:hover {
  border-color: var(--color-primary);
  color: var(--color-text);
}
</style>
