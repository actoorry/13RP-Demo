<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import * as echarts from 'echarts'
import { useDemoStore } from '../stores/demo'
import { PLAN_COLORS, PREFERENCE_OPTIONS } from '../constants/demo'
import type { SolutionPlan } from '../stores/demo'

const emit = defineEmits<{ (e: 'start-gaming'): void }>()

const store = useDemoStore()
const preference = ref('balanced')
const radarEl = ref<HTMLDivElement | null>(null)
const radarChart = ref<echarts.ECharts | null>(null)
const expandedId = ref('')
const themeMode = ref<'dark' | 'light'>(document.documentElement.dataset.theme === 'light' ? 'light' : 'dark')

let themeObserver: MutationObserver | null = null

const plans = computed(() => store.solutions)

/** 当前是否日间模式（html[data-theme='light']） */
function isLight(): boolean {
  return document.documentElement.dataset.theme === 'light'
}

/** 轴标签/文字色：夜间浅灰 #8b949e、日间深灰 #4a5568，保证浅底可读 */
function axisTextColor(): string {
  return isLight() ? '#4a5568' : '#8b949e'
}

type MetricKey = keyof SolutionPlan['metrics']

function refresh() {
  store.fetchSolutions(preference.value)
}

function metricValues(key: MetricKey): number[] {
  return plans.value.map((p) => p.metrics[key])
}

/** 各维度按"越小越优"归一化为 0-100 得分，得分越高越好 */
function benefit(key: MetricKey): number[] {
  const values = metricValues(key)
  if (!values.length) return []
  const min = Math.min(...values)
  const max = Math.max(...values)
  const span = max - min || 1
  return values.map((v) => ((max - v) / span) * 100)
}

function renderRadar() {
  if (!radarEl.value || !plans.value.length) return
  if (!radarChart.value) radarChart.value = echarts.init(radarEl.value)

  const dims: MetricKey[] = ['cost', 'carbon', 'delivery', 'risk']
  const names: Record<MetricKey, string> = {
    cost: '成本',
    carbon: '碳排',
    delivery: '交期',
    risk: '风险',
  }

  radarChart.value.setOption(
    {
      backgroundColor: 'transparent',
      tooltip: { trigger: 'item' },
      legend: {
        bottom: 0,
        textStyle: { color: axisTextColor() },
        data: plans.value.map((p) => p.id),
      },
      radar: {
        indicator: dims.map((d) => ({ name: names[d], max: 100 })),
        radius: '62%',
        center: ['50%', '46%'],
        splitArea: {
          areaStyle: { color: ['rgba(0,212,170,0.03)', 'rgba(0,212,170,0.06)'] },
        },
        axisLine: { lineStyle: { color: 'rgba(0,212,170,0.25)' } },
        splitLine: { lineStyle: { color: 'rgba(0,212,170,0.25)' } },
        axisName: { color: axisTextColor(), fontSize: 11 },
      },
      series: [
        {
          type: 'radar',
          symbolSize: 4,
          data: plans.value.map((p, i) => ({
            name: p.id,
            value: dims.map((d) => benefit(d)[i]),
            areaStyle: { opacity: 0.18 },
            lineStyle: { color: PLAN_COLORS[i % PLAN_COLORS.length], width: 2 },
            itemStyle: { color: PLAN_COLORS[i % PLAN_COLORS.length] },
          })),
        },
      ],
    } as echarts.EChartsOption,
    { notMerge: true },
  )
}

function riskColor(risk: number): string {
  if (risk >= 0.35) return 'var(--color-alert)'
  if (risk >= 0.22) return 'var(--color-warn)'
  return 'var(--color-primary)'
}

function toggle(id: string) {
  expandedId.value = expandedId.value === id ? '' : id
}

onMounted(() => {
  if (!plans.value.length) refresh()
  nextTick(() => renderRadar())
  // 监听 html[data-theme] 属性变化，主题切换时同步 themeMode 触发重绘
  themeObserver = new MutationObserver(() => {
    themeMode.value = isLight() ? 'light' : 'dark'
  })
  themeObserver.observe(document.documentElement, {
    attributes: true,
    attributeFilter: ['data-theme'],
  })
})

onUnmounted(() => {
  themeObserver?.disconnect()
  themeObserver = null
  radarChart.value?.dispose()
  radarChart.value = null
})

watch(preference, () => refresh())
watch(plans, () => nextTick(() => renderRadar()), { deep: true })
// 主题切换时重绘雷达图（setOption notMerge 避免旧配色残留）
watch(themeMode, () => renderRadar())
</script>

<template>
  <section class="plans-panel fade-up">
    <h3 class="panel-title">8RP 多目标寻优</h3>

    <div class="pref-row">
      <span class="pref-label">优化偏好</span>
      <el-radio-group v-model="preference">
        <el-radio-button
          v-for="opt in PREFERENCE_OPTIONS"
          :key="opt.value"
          :value="opt.value"
        >
          {{ opt.label }}
        </el-radio-button>
      </el-radio-group>
    </div>

    <div ref="radarEl" class="radar-box"></div>
    <div class="pareto-tip">
      3 个方案均为非劣解（任意一个维度的提升都将牺牲另一个维度）
    </div>

    <TransitionGroup name="plan-move" tag="div" class="plan-list">
      <div
        v-for="(p, idx) in plans"
        :key="p.id"
        class="plan-card"
        :class="{ 'plan-active': expandedId === p.id }"
        @click="toggle(p.id)"
      >
        <div class="plan-head">
          <span class="plan-rank mono">#{{ idx + 1 }}</span>
          <span class="plan-name">{{ p.name }}</span>
          <el-tag size="small" effect="dark" type="success">非劣解</el-tag>
        </div>
        <div class="plan-metrics mono">
          <span>成本 {{ p.metrics.cost }}</span>
          <span>碳排 {{ p.metrics.carbon }}</span>
          <span>交期 {{ p.metrics.delivery }}天</span>
          <span :style="{ color: riskColor(p.metrics.risk) }">风险 {{ p.metrics.risk }}</span>
        </div>
        <ul v-if="expandedId === p.id" class="exec-steps">
          <li v-for="(s, i) in p.execSteps ?? []" :key="i">{{ i + 1 }}. {{ s }}</li>
        </ul>
      </div>
    </TransitionGroup>

    <el-button type="primary" class="gaming-btn" @click="emit('start-gaming')">
      加入 9RP 博弈对抗层
    </el-button>
  </section>
</template>

<style scoped>
.pref-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.pref-label {
  font-size: 12px;
  color: var(--color-text-secondary);
  white-space: nowrap;
}

.radar-box {
  width: 100%;
  height: 220px;
}

.pareto-tip {
  font-size: 11px;
  color: var(--color-text-muted);
  text-align: center;
  margin: 0 0 12px;
}

.plan-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 12px;
}

.plan-card {
  background: var(--color-bg-elevated);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  padding: 10px 12px;
  cursor: pointer;
  transition: border-color 0.3s var(--ease-out), transform 0.3s var(--ease-out);
}

.plan-card:hover {
  border-color: var(--color-primary);
}

.plan-card.plan-active {
  border-color: var(--color-primary);
  box-shadow: 0 0 8px rgba(0, 212, 170, 0.25);
}

.plan-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.plan-rank {
  font-size: 14px;
  font-weight: 700;
  color: var(--color-data);
}

.plan-name {
  flex: 1;
  font-size: 13px;
  font-weight: 600;
}

.plan-metrics {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 12px;
  font-size: 12px;
  color: var(--color-text-secondary);
}

.exec-steps {
  margin: 8px 0 0;
  padding-left: 18px;
  font-size: 12px;
  color: var(--color-text-secondary);
  line-height: 1.8;
  border-top: 1px dashed var(--color-border);
}

.gaming-btn {
  width: 100%;
  font-weight: 600;
  letter-spacing: 1px;
}
</style>
