<script setup lang="ts">
import { onMounted, onUnmounted, ref, watch } from 'vue'
import * as echarts from 'echarts'

// 数据统计 · 概览卡片
const statCards = [
  { label: '用户总计', value: '18,540' },
  { label: '今日新增用户', value: '126' },
  { label: '新增企业用户', value: '38' },
]

// 数据统计 · 月度新增用户趋势（6 个月模拟数据）
const months = ['2026-03', '2026-04', '2026-05', '2026-06', '2026-07', '2026-08']
const newUsers = [820, 1050, 1360, 980, 1520, 1260]
const newEntUsers = [180, 240, 310, 220, 380, 290]

const chartEl = ref<HTMLDivElement | null>(null)
const chart = ref<echarts.ECharts | null>(null)
// 当前主题模式：跟随 html[data-theme]，图表配色随主题切换重绘
const themeMode = ref<'dark' | 'light'>(
  document.documentElement.dataset.theme === 'light' ? 'light' : 'dark',
)
let themeObserver: MutationObserver | null = null

/** 当前是否日间模式（html[data-theme='light']） */
function isLight(): boolean {
  return document.documentElement.dataset.theme === 'light'
}

/** 图表轴标签/文字色：日间深灰、夜间浅灰，保证浅底可读 */
function chartTextColor(): string {
  return isLight() ? '#4a5568' : '#8b949e'
}

/** 轴线/分割线色：日间浅边框、夜间深线 */
function chartLineColor(): string {
  return isLight() ? '#e4e7ed' : '#21262d'
}

/** 系列主色：日间用方案 1 青绿/数据蓝，夜间用原驾驶舱青绿/蓝 */
function seriesPalette() {
  return isLight()
    ? { line1: '#00b894', line2: '#2f6fd6', area1: 'rgba(0, 184, 148, 0.12)', area2: 'rgba(47, 111, 214, 0.12)' }
    : { line1: '#00d4aa', line2: '#4a90d9', area1: 'rgba(0, 212, 170, 0.12)', area2: 'rgba(74, 144, 217, 0.12)' }
}

function buildOption(): echarts.EChartsOption {
  const palette = seriesPalette()
  return {
    backgroundColor: 'transparent',
    tooltip: { trigger: 'axis' },
    legend: {
      data: ['新增用户', '新增企业用户'],
      textStyle: { color: chartTextColor() },
    },
    grid: { left: 48, right: 24, top: 48, bottom: 32 },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: months,
      axisLine: { lineStyle: { color: chartLineColor() } },
      axisLabel: { color: chartTextColor() },
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: chartLineColor() } },
      axisLabel: { color: chartTextColor() },
    },
    series: [
      {
        name: '新增用户',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        itemStyle: { color: palette.line1 },
        areaStyle: { color: palette.area1 },
        data: newUsers,
      },
      {
        name: '新增企业用户',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        itemStyle: { color: palette.line2 },
        areaStyle: { color: palette.area2 },
        data: newEntUsers,
      },
    ],
  } as echarts.EChartsOption
}

function resizeChart() {
  chart.value?.resize()
}

onMounted(() => {
  if (!chartEl.value) return
  chart.value = echarts.init(chartEl.value)
  chart.value.setOption(buildOption())
  // 监听 html[data-theme] 属性变化，主题切换时同步 themeMode 触发重绘
  themeObserver = new MutationObserver(() => {
    themeMode.value = isLight() ? 'light' : 'dark'
  })
  themeObserver.observe(document.documentElement, {
    attributes: true,
    attributeFilter: ['data-theme'],
  })
  window.addEventListener('resize', resizeChart)
})

onUnmounted(() => {
  themeObserver?.disconnect()
  themeObserver = null
  window.removeEventListener('resize', resizeChart)
  chart.value?.dispose()
  chart.value = null
})

// 主题切换时重绘图表（setOption notMerge 避免旧配色残留）
watch(themeMode, () => {
  if (chart.value) chart.value.setOption(buildOption(), { notMerge: true })
})

// 数据统计 · 企业信息录入示例（列参照功能清单 §七.4）
interface EntRow {
  name: string
  owner: string
  level1: string
  level2: string
  contact: string
}
const entRows: EntRow[] = [
  { name: '威海恒邦矿冶发展有限公司', owner: '陈明', level1: '矿冶', level2: '有色金属', contact: '刘经理' },
  { name: '广州新城市投资控股集团有限公司', owner: '周婷', level1: '投资', level2: '房地产', contact: '李经理' },
  { name: '南京朗诗物业管理有限公司', owner: '吴刚', level1: '物业', level2: '住宅物业', contact: '王经理' },
  { name: '沈阳博宇会幸福实业有限公司', owner: '郑华', level1: '实业', level2: '金属贸易', contact: '赵经理' },
  { name: '北京某供应链服务有限公司', owner: '刘洋', level1: '物流', level2: '供应链', contact: '孙经理' },
]
</script>

<template>
  <div class="module-page">
    <h2 class="module-title">数据统计</h2>

    <el-tabs>
      <el-tab-pane label="数据总览">
        <div class="stat-cards">
          <el-card v-for="card in statCards" :key="card.label" class="stat-card">
            <div class="stat-value">{{ card.value }}</div>
            <div class="stat-label">{{ card.label }}</div>
          </el-card>
        </div>

        <el-card class="chart-card">
          <template #header>
            <span class="chart-title">月度新增用户趋势</span>
          </template>
          <div ref="chartEl" class="chart-box"></div>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="用户统计">
        <el-card>
          <el-empty description="用户统计（P1 规划中）" />
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="多维度统计">
        <el-card>
          <el-empty description="多维度统计（P1 规划中）" />
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="企业信息录入">
        <el-card class="table-card">
          <template #header>
            <span class="chart-title">企业信息录入示例</span>
          </template>
          <el-table :data="entRows" stripe>
            <el-table-column prop="name" label="企业名称" min-width="240" />
            <el-table-column prop="owner" label="负责人" width="120" />
            <el-table-column prop="level1" label="一级分类" width="120" />
            <el-table-column prop="level2" label="二级分类" width="140" />
            <el-table-column prop="contact" label="联系人" width="120" />
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<style scoped>
.module-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 16px;
  color: var(--color-text);
}

.stat-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.stat-value {
  font-family: var(--font-mono);
  font-size: 26px;
  font-weight: 600;
  color: var(--color-primary);
}

.stat-label {
  margin-top: 6px;
  font-size: 13px;
  color: var(--color-text-secondary);
}

.chart-card,
.table-card {
  margin-bottom: 16px;
}

.chart-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
}

.chart-box {
  height: 280px;
  width: 100%;
}
</style>
