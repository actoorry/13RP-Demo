<template>
  <div class="demo-layout">
    <!-- 左：4RP 看板 -->
    <DashboardPanel class="panel" />

    <!-- 中：物流网络图 -->
    <LogisticsMap class="panel" />

    <!-- 右：决策交互面板 -->
    <div class="panel">
      <div class="panel-title">
        <span>决策交互面板</span>
        <span class="badge">{{ PHASE_LABEL[store.phase] || store.phase }}</span>
      </div>

      <div v-if="store.phase === 'INIT'" class="fade-up">
        <EventTrigger @start="onTriggerEvent" />
      </div>

      <div v-else-if="store.phase === 'EVENT_INJECTED'" class="fade-up">
        <el-alert type="error" :closable="false" title="检测到一级物流中断事件" description="台风'海燕'致宁波舟山港/上海港封港 5 天，稀土·镝供应中断，订单交付告急。" style="margin-bottom:12px" />
        <el-button type="primary" size="large" style="width:100%" @click="send('start-simulation')">
          启动 7RP 方案推演
        </el-button>
      </div>

      <div v-else-if="store.phase === 'SIMULATING'" class="fade-up">
        <SimulationProgress />
      </div>

      <div v-else-if="store.phase === 'SIMULATION_DONE'" class="fade-up">
        <el-alert type="success" :closable="false" title="方案推演完成" description="共评估 342 条路径，筛选出 3 条可行路径。是否启动 8RP 多目标寻优？" style="margin-bottom:12px" />
        <el-button type="primary" size="large" style="width:100%" @click="startOptimization">
          启动 8RP 多目标寻优
        </el-button>
      </div>

      <div v-else-if="store.phase === 'OPTIMIZING'" class="fade-up">
        <PlanCards @gaming="send('start-gaming')" />
      </div>

      <div v-else-if="store.phase === 'GAMING'" class="fade-up">
        <GamePanel @confirm="confirmPlan" />
      </div>

      <div v-else-if="store.phase === 'PLAN_SELECTED' || store.phase === 'DONE'" class="fade-up">
        <InstructionList />
      </div>
    </div>

    <!-- 底部状态栏 -->
    <StatusBar class="status-bar" />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useDemoStore } from './stores/demo';
import { useWebSocket } from './composables/useWebSocket';
import { PHASE_LABEL } from './utils/constants';
import DashboardPanel from './components/DashboardPanel.vue';
import LogisticsMap from './components/LogisticsMap.vue';
import EventTrigger from './components/EventTrigger.vue';
import SimulationProgress from './components/SimulationProgress.vue';
import PlanCards from './components/PlanCards.vue';
import GamePanel from './components/GamePanel.vue';
import InstructionList from './components/InstructionList.vue';
import StatusBar from './components/StatusBar.vue';

const store = useDemoStore();

const { send } = useWebSocket((msg) => {
  store.applyWsMessage(msg);
});

function onTriggerEvent(duration: number) {
  send('trigger-event', { eventType: 'typhoon_port_closure', duration });
}

async function startOptimization() {
  send('start-optimization', { preference: 'balanced' });
  await loadSolutions('balanced');
}

async function loadSolutions(preference: string) {
  const res = await fetch(`/api/demo/solutions?preference=${preference}`);
  const data = await res.json();
  store.solutions = Array.isArray(data) ? data : [];
}

async function confirmPlan(planId: string) {
  send('confirm-plan', { planId });
  const res = await fetch(`/api/demo/instructions?planId=${planId}`);
  const data = await res.json();
  store.instructions = data.instructions || [];
  store.planName = data.planName || planId;
}

onMounted(async () => {
  const res = await fetch('/api/demo/state');
  const state = await res.json();
  store.phase = state.state || 'INIT';
});
</script>
