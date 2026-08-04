<template>
  <div>
    <div style="display:flex; align-items:center; gap:8px; margin-bottom:12px">
      <span style="font-size:13px; color:var(--text-dim)">本次优化偏好：</span>
      <el-radio-group v-model="preference" @change="onPreferenceChange" size="small">
        <el-radio-button value="delivery_first">优先保交付</el-radio-button>
        <el-radio-button value="balanced">平衡成本与交付</el-radio-button>
        <el-radio-button value="cost_first">优先降低成本</el-radio-button>
      </el-radio-group>
    </div>

    <div ref="radarEl" style="width:100%; height:210px" />

    <div style="display:flex; flex-direction:column; gap:8px; margin-top:10px">
      <div v-for="(p, idx) in sorted" :key="p.id"
        class="plan-card" :class="{ selected: selectedId === p.id, shake: shaking === p.id }"
        @click="selectedId = p.id">
        <div style="display:flex; justify-content:space-between; align-items:center">
          <div>
            <span class="rank">{{ idx + 1 }}</span>
            <strong>{{ p.name }}</strong>
          </div>
          <span class="mono risk" :style="{ color: p.metrics.risk < 0.2 ? 'var(--primary)' : p.metrics.risk < 0.3 ? 'var(--warn)' : 'var(--alert)' }">
            风险 {{ p.metrics.risk.toFixed(2) }}
          </span>
        </div>
        <div class="metrics mono">
          成本 {{ p.metrics.cost }}万 · 碳排 {{ p.metrics.carbon }}吨 · 交期 {{ p.metrics.delivery }}天
        </div>
        <div v-if="selectedId === p.id" class="exec-steps fade-up">
          <div v-for="(s, i) in p.execSteps" :key="i" class="step">• {{ s }}</div>
          <el-tag size="small" type="info" style="margin-top:6px">非劣解：任意维度提升将牺牲另一维度</el-tag>
        </div>
      </div>
    </div>

    <el-button type="primary" size="large" style="width:100%; margin-top:12px" @click="emit('gaming')">
      加入 9RP 博弈对抗层
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import * as echarts from 'echarts';
import { useDemoStore } from '../stores/demo';

const emit = defineEmits<{ (e: 'gaming'): void }>();
const store = useDemoStore();
const preference = ref('balanced');
const selectedId = ref('');
const shaking = ref('');
const radarEl = ref<HTMLDivElement>();
let radar: echarts.ECharts | null = null;

const sorted = computed(() => {
  const rank = (p: any) => p.preferenceRank?.[preference.value] ?? 99;
  return [...store.solutions].sort((a, b) => rank(a) - rank(b));
});

async function onPreferenceChange(val: string) {
  // 触发排序动画：先标记 shake
  const first = sorted.value[0]?.id;
  if (first) { shaking.value = first; setTimeout(() => (shaking.value = ''), 500); }
  const res = await fetch(`/api/demo/solutions?preference=${val}`);
  const data = await res.json();
  store.solutions = Array.isArray(data) ? data : [];
  selectedId.value = store.solutions[0]?.id || '';
  renderRadar();
}

function renderRadar() {
  if (!radar || !store.solutions.length) return;
  radar.setOption({
    backgroundColor: 'transparent',
    radar: {
      indicator: [
        { name: '成本', max: 400 }, { name: '碳排', max: 250 },
        { name: '交期', max: 12 }, { name: '风险', max: 0.5 }
      ],
      axisName: { color: '#8b949e' },
      splitArea: { areaStyle: { color: ['rgba(0,212,170,0.02)', 'rgba(0,212,170,0.05)'] } },
      splitLine: { lineStyle: { color: '#30363d' } }
    },
    series: [{
      type: 'radar',
      data: store.solutions.map((p: any, i: number) => ({
        name: p.name,
        value: [p.metrics.cost, p.metrics.carbon, p.metrics.delivery, p.metrics.risk],
        lineStyle: { color: ['#00d4aa', '#4a90d9', '#ffa940'][i], width: 2 },
        itemStyle: { color: ['#00d4aa', '#4a90d9', '#ffa940'][i] },
        areaStyle: { opacity: 0.08 }
      }))
    }]
  }, true);
}

watch(() => store.solutions, renderRadar, { deep: true });

onMounted(() => {
  radar = echarts.init(radarEl.value!);
  renderRadar();
  if (store.solutions.length) selectedId.value = store.solutions[0].id;
});
</script>

<style scoped>
.plan-card {
  border: 1px solid var(--panel-border);
  border-radius: 8px;
  padding: 10px 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.plan-card:hover { border-color: var(--primary); }
.plan-card.selected { border-color: var(--primary); background: rgba(0, 212, 170, 0.06); }
.rank {
  display: inline-block; width: 18px; height: 18px; line-height: 18px;
  text-align: center; border-radius: 50%; background: var(--primary);
  color: #0d1117; font-size: 11px; font-weight: 700; margin-right: 8px;
}
.metrics { font-size: 12px; color: var(--text-dim); margin-top: 4px; }
.risk { font-size: 12px; }
.exec-steps { margin-top: 8px; padding-top: 8px; border-top: 1px dashed var(--panel-border); font-size: 12px; color: var(--text-dim); line-height: 1.8; }
.step { color: var(--text); }
</style>
