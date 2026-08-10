<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import * as echarts from 'echarts'
import {
  accountApi,
  materialElementApi,
  mobileConfigApi,
  packageStandardApi,
  productApi,
} from '../../api/base'
import type { ProductNode } from '../../api/base'
import { flowAnmaApi, flowX5Api } from '../../api/flow'
import { personalApi, subscriptionApi } from '../../api/todo'
import { employeeApi } from '../../api/org'
import { certApi } from '../../api/crm'
import { supplierGradeApi } from '../../api/purchase'
import { useAccountStore } from '../../stores/account'

const accountStore = useAccountStore()

const loading = ref(false)

/** 数字卡片：产品品名总数 / 流程实例总数 / 待办总数 / 员工用户数 */
const statCards = ref([
  { label: '产品品名总数', value: 0 },
  { label: '流程实例总数', value: 0 },
  { label: '待办总数', value: 0 },
  { label: '员工/用户数', value: 0 },
])

/** 柱状图：各模块数据量 */
const moduleStats = ref<{ name: string; value: number }[]>([])
/** 饼图：证照类型分布（crm_cert 按 certType 统计） */
const certPie = ref<{ name: string; value: number }[]>([])
/** 饼图：供应商分级分布（purchase_supplier_grade 按 grade 统计） */
const supplierPie = ref<{ name: string; value: number }[]>([])

const hasBarData = computed(() => moduleStats.value.some((s) => s.value > 0))
const hasCertData = computed(() => certPie.value.length > 0)
const hasSupplierData = computed(() => supplierPie.value.length > 0)

const barEl = ref<HTMLDivElement | null>(null)
const certEl = ref<HTMLDivElement | null>(null)
const supplierEl = ref<HTMLDivElement | null>(null)
const barChart = ref<echarts.ECharts | null>(null)
const certChart = ref<echarts.ECharts | null>(null)
const supplierChart = ref<echarts.ECharts | null>(null)

/** 当前主题模式：跟随 html[data-theme]，图表配色随主题切换重绘 */
const themeMode = ref<'dark' | 'light'>(
  document.documentElement.dataset.theme === 'light' ? 'light' : 'dark',
)
let themeObserver: MutationObserver | null = null

/** 当前是否日间模式（html[data-theme='light']） */
function isLight(): boolean {
  return document.documentElement.dataset.theme === 'light'
}

/** 图表轴标签/文字色：日间深灰、夜间浅灰 */
function chartTextColor(): string {
  return isLight() ? '#4a5568' : '#8b949e'
}

/** 轴线/分割线色：日间浅边框、夜间深线 */
function chartLineColor(): string {
  return isLight() ? '#e4e7ed' : '#21262d'
}

/** 系列主色：日间宝蓝 #2563eb，夜间驾驶舱青绿 */
function chartPrimary(): string {
  return isLight() ? '#2563eb' : '#00d4aa'
}

/** 饼图配色：日间浅色调宝蓝系，夜间深色系 */
function piePalette(): string[] {
  return isLight()
    ? ['#2563eb', '#60a5fa', '#93c5fd', '#c7d2fe', '#dbeafe', '#9ca3af', '#d1d5db']
    : ['#00d4aa', '#4de1c4', '#4a90d9', '#7eb8e8', '#a3c6ef', '#6b7684', '#8b949e']
}

/** 统计品名节点数（type === 'product'），含递归下级 */
function countProductNodes(nodes: ProductNode[]): number {
  let count = 0
  for (const n of nodes) {
    if (n.type === 'product') count++
    if (n.children && n.children.length) count += countProductNodes(n.children)
  }
  return count
}

/** 按字段分组统计 list 记录数（证照类型 / 供应商分级共用） */
function groupCount<T>(list: T[], key: keyof T): { name: string; value: number }[] {
  const map = new Map<string, number>()
  for (const item of list) {
    const k = String(item[key] ?? '未分类')
    map.set(k, (map.get(k) ?? 0) + 1)
  }
  return Array.from(map.entries()).map(([name, value]) => ({ name, value }))
}

async function loadAll() {
  loading.value = true
  try {
    const accountId = accountStore.currentAccountId

    // 各 list 接口互不依赖，并行请求；任何单个失败仅该项取 0/空，不阻塞页面
    const [
      accountRes,
      productRes,
      materialRes,
      packageRes,
      mobileRes,
      x5Res,
      anmaRes,
      subRes,
      personalRes,
      employeeRes,
      certRes,
      supplierRes,
    ] = await Promise.allSettled([
      accountApi.list({ page: 1, size: 1 }),
      accountId != null ? productApi.tree(accountId) : Promise.resolve([]),
      materialElementApi.list({ page: 1, size: 1 }),
      packageStandardApi.list({ page: 1, size: 1 }),
      mobileConfigApi.list({ page: 1, size: 1 }),
      flowX5Api.list({ page: 1, size: 1 }),
      flowAnmaApi.list({ page: 1, size: 1 }),
      subscriptionApi.list({ page: 1, size: 1 }),
      personalApi.list({ page: 1, size: 1 }),
      employeeApi.list({ page: 1, size: 1 }),
      certApi.list({ page: 1, size: 10000 }),
      supplierGradeApi.list({ page: 1, size: 10000 }),
    ])

    const totalOf = (r: PromiseSettledResult<{ total?: number }>): number =>
      r.status === 'fulfilled' ? (r.value?.total ?? 0) : 0

    const productCount =
      productRes.status === 'fulfilled' && Array.isArray(productRes.value)
        ? countProductNodes(productRes.value)
        : 0
    const flowTotal = totalOf(x5Res) + totalOf(anmaRes)
    const todoTotal = totalOf(subRes) + totalOf(personalRes)
    const employeeTotal = totalOf(employeeRes)

    statCards.value = [
      { label: '产品品名总数', value: productCount },
      { label: '流程实例总数', value: flowTotal },
      { label: '待办总数', value: todoTotal },
      { label: '员工/用户数', value: employeeTotal },
    ]

    moduleStats.value = [
      { name: '账套', value: totalOf(accountRes) },
      { name: '产品品名', value: productCount },
      { name: '材质元素', value: totalOf(materialRes) },
      { name: '包装标准', value: totalOf(packageRes) },
      { name: '主营品种', value: totalOf(mobileRes) },
      { name: '流程实例', value: flowTotal },
      { name: '待办', value: todoTotal },
      { name: '员工', value: employeeTotal },
    ]

    certPie.value =
      certRes.status === 'fulfilled' && Array.isArray(certRes.value?.list)
        ? groupCount(certRes.value.list, 'certType')
        : []
    supplierPie.value =
      supplierRes.status === 'fulfilled' && Array.isArray(supplierRes.value?.list)
        ? groupCount(supplierRes.value.list, 'grade')
        : []

    // 等图表容器（v-if 渲染）就位后初始化/重绘图表
    await nextTick()
    renderAll()
  } finally {
    loading.value = false
  }
}

function buildBarOption(): echarts.EChartsOption {
  const c = { text: chartTextColor(), line: chartLineColor(), primary: chartPrimary() }
  return {
    backgroundColor: 'transparent',
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 48, right: 24, top: 32, bottom: 36 },
    xAxis: {
      type: 'category',
      data: moduleStats.value.map((s) => s.name),
      axisLine: { lineStyle: { color: c.line } },
      axisLabel: { color: c.text },
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: { lineStyle: { color: c.line } },
      axisLabel: { color: c.text },
    },
    series: [
      {
        name: '数据量',
        type: 'bar',
        barWidth: 26,
        itemStyle: { color: c.primary, borderRadius: [4, 4, 0, 0] },
        data: moduleStats.value.map((s) => s.value),
      },
    ],
  } as echarts.EChartsOption
}

function buildPieOption(data: { name: string; value: number }[]): echarts.EChartsOption {
  const c = { text: chartTextColor() }
  return {
    backgroundColor: 'transparent',
    color: piePalette(),
    tooltip: { trigger: 'item', formatter: '{b}: {c}（{d}%）' },
    legend: {
      orient: 'vertical',
      right: 8,
      top: 'center',
      itemWidth: 12,
      itemHeight: 12,
      textStyle: { color: c.text },
    },
    series: [
      {
        type: 'pie',
        radius: ['38%', '68%'],
        center: ['36%', '50%'],
        avoidLabelOverlap: true,
        label: { formatter: '{b}: {c}', color: c.text },
        data,
      },
    ],
  } as echarts.EChartsOption
}

function renderAll() {
  if (barEl.value && hasBarData.value) {
    if (!barChart.value) barChart.value = echarts.init(barEl.value)
    barChart.value.setOption(buildBarOption(), { notMerge: true })
  }
  if (certEl.value && hasCertData.value) {
    if (!certChart.value) certChart.value = echarts.init(certEl.value)
    certChart.value.setOption(buildPieOption(certPie.value), { notMerge: true })
  }
  if (supplierEl.value && hasSupplierData.value) {
    if (!supplierChart.value) supplierChart.value = echarts.init(supplierEl.value)
    supplierChart.value.setOption(buildPieOption(supplierPie.value), { notMerge: true })
  }
}

function resizeAll() {
  barChart.value?.resize()
  certChart.value?.resize()
  supplierChart.value?.resize()
}

onMounted(async () => {
  if (!accountStore.accounts.length) {
    await accountStore.fetchAccounts().catch(() => {})
  }
  // 监听 html[data-theme] 属性变化，主题切换时同步 themeMode 触发重绘
  themeObserver = new MutationObserver(() => {
    themeMode.value = isLight() ? 'light' : 'dark'
  })
  themeObserver.observe(document.documentElement, {
    attributes: true,
    attributeFilter: ['data-theme'],
  })
  window.addEventListener('resize', resizeAll)
  await loadAll()
})

onUnmounted(() => {
  themeObserver?.disconnect()
  themeObserver = null
  window.removeEventListener('resize', resizeAll)
  barChart.value?.dispose()
  certChart.value?.dispose()
  supplierChart.value?.dispose()
  barChart.value = null
  certChart.value = null
  supplierChart.value = null
})

// 主题切换时重绘图表（setOption notMerge 避免旧配色残留）
watch(themeMode, () => renderAll())
</script>

<template>
  <div class="page fade-up">
    <h2 class="module-title">数据概览</h2>

    <div v-loading="loading" class="stat-cards">
      <el-card v-for="card in statCards" :key="card.label" class="stat-card" shadow="never">
        <div class="stat-value">{{ card.value.toLocaleString() }}</div>
        <div class="stat-label">{{ card.label }}</div>
      </el-card>
    </div>

    <div class="chart-grid">
      <el-card class="chart-card" shadow="never">
        <template #header><span class="chart-title">各模块数据量</span></template>
        <div v-if="hasBarData" ref="barEl" class="chart-box"></div>
        <el-empty v-else description="暂无模块数据" :image-size="72" />
      </el-card>

      <el-card class="chart-card" shadow="never">
        <template #header><span class="chart-title">证照类型分布</span></template>
        <div v-if="hasCertData" ref="certEl" class="chart-box"></div>
        <el-empty v-else description="暂无证照数据" :image-size="72" />
      </el-card>
    </div>

    <div class="chart-grid">
      <el-card class="chart-card" shadow="never">
        <template #header><span class="chart-title">供应商分级分布</span></template>
        <div v-if="hasSupplierData" ref="supplierEl" class="chart-box"></div>
        <el-empty v-else description="暂无供应商分级数据" :image-size="72" />
      </el-card>

      <el-card class="chart-card" shadow="never">
        <template #header><span class="chart-title">数据说明</span></template>
        <div class="note-body">
          <p>· 各模块数据量实时读取对应 list 接口统计生成</p>
          <p>· 产品品名统计当前账套「品名」节点数</p>
          <p>· 流程实例 = X5 流程 + 安码流程</p>
          <p>· 待办 = 四板块订阅 + 个人待办</p>
        </div>
      </el-card>
    </div>
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
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.stat-card {
  min-height: 96px;
}

.stat-value {
  font-family: var(--font-mono);
  font-size: 28px;
  font-weight: 600;
  color: var(--color-primary);
}

.stat-label {
  margin-top: 6px;
  font-size: 13px;
  color: var(--color-text-secondary);
}

.chart-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}

.chart-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
}

.chart-box {
  height: 320px;
  width: 100%;
}

.note-body {
  color: var(--color-text-secondary);
  font-size: 13px;
  line-height: 2.2;
}

.note-body p {
  margin: 0;
}
</style>
