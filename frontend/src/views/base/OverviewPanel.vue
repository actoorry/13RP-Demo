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
import { orderApi, supplierGradeApi } from '../../api/purchase'
import { saleDailyReportApi, type SaleDailyReport } from '../../api/sale'
import { financeInvoiceApi } from '../../api/finance'
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
/** 柱状图：采购订单状态分布（purchase_order 按 status 统计） */
const orderBar = ref<{ name: string; value: number }[]>([])
/** 柱状图：发票状态分布（finance_invoice 按 status 统计） */
const invoiceBar = ref<{ name: string; value: number }[]>([])
/** 柱状图：流程实例状态分布（flow_x5 + flow_anma 合并按 status 统计） */
const flowBar = ref<{ name: string; value: number }[]>([])
/** 折线图：业务日报趋势（sale_daily_report 按 reportDate 统计） */
const dailyTrend = ref<SaleDailyReport[]>([])

const hasBarData = computed(() => moduleStats.value.some((s) => s.value > 0))
const hasCertData = computed(() => certPie.value.length > 0)
const hasSupplierData = computed(() => supplierPie.value.length > 0)
const hasOrderData = computed(() => orderBar.value.length > 0)
const hasInvoiceData = computed(() => invoiceBar.value.length > 0)
const hasFlowData = computed(() => flowBar.value.length > 0)
const hasDailyData = computed(() => dailyTrend.value.length > 0)

const barEl = ref<HTMLDivElement | null>(null)
const certEl = ref<HTMLDivElement | null>(null)
const supplierEl = ref<HTMLDivElement | null>(null)
const orderEl = ref<HTMLDivElement | null>(null)
const invoiceEl = ref<HTMLDivElement | null>(null)
const flowEl = ref<HTMLDivElement | null>(null)
const dailyEl = ref<HTMLDivElement | null>(null)
const barChart = ref<echarts.ECharts | null>(null)
const certChart = ref<echarts.ECharts | null>(null)
const supplierChart = ref<echarts.ECharts | null>(null)
const orderChart = ref<echarts.ECharts | null>(null)
const invoiceChart = ref<echarts.ECharts | null>(null)
const flowChart = ref<echarts.ECharts | null>(null)
const dailyChart = ref<echarts.ECharts | null>(null)

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

/** 统一多色调色板：日间 10 色（饱和高区分度），夜间亮色系（暗底可读） */
function palette(): string[] {
  return isLight()
    ? ['#2563eb', '#0ea5e9', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899', '#14b8a6', '#f97316', '#64748b']
    : ['#60a5fa', '#38bdf8', '#34d399', '#fbbf24', '#f87171', '#a78bfa', '#f472b6', '#2dd4bf', '#fb923c', '#94a3b8']
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

/** 状态英文 → 中文（图表分类轴显示；中文值原样保留） */
const STATUS_ZH: Record<string, string> = {
  CREATED: '已创建',
  PENDING: '待审批',
  PENDING_APPROVE: '待审批',
  APPROVED: '已审批',
  REJECTED: '已驳回',
  DONE: '已完成',
  VOID: '已作废',
  CLOSED: '已关闭',
  WAIT_PAY: '待付款',
  WAIT_INBOUND: '待入库',
  UNALLOCATED: '未分摊',
  ALLOCATED: '已分摊',
}

/** 按字段分组统计 list 记录数（证照类型 / 供应商分级 / 状态共用） */
function groupCount<T>(list: T[], key: keyof T): { name: string; value: number }[] {
  const map = new Map<string, number>()
  for (const item of list) {
    const raw = String(item[key] ?? '未分类')
    const k = STATUS_ZH[raw] ?? raw
    map.set(k, (map.get(k) ?? 0) + 1)
  }
  return Array.from(map.entries()).map(([name, value]) => ({ name, value }))
}

/** 合并 X5 与安码流程实例 list，按 status 分组统计（任一接口失败不影响另一项） */
function mergeFlowStatus(
  results: PromiseSettledResult<unknown>[],
): { name: string; value: number }[] {
  const items: Record<string, unknown>[] = []
  for (const r of results) {
    if (r.status !== 'fulfilled') continue
    const payload = r.value as { list?: unknown[] } | null
    if (payload && Array.isArray(payload.list)) {
      items.push(...(payload.list as Record<string, unknown>[]))
    }
  }
  return groupCount(items, 'status')
}

async function loadAll() {
  loading.value = true
  try {
    const accountId = accountStore.currentAccountId

    // 各 list 接口互不依赖，并行请求；任何单个失败仅该项取 0/空，不阻塞页面。
    // 饼图统计所需 list 统一取 size 10000
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
      orderRes,
      invoiceRes,
      dailyRes,
    ] = await Promise.allSettled([
      accountApi.list({ page: 1, size: 1 }),
      accountId != null ? productApi.tree(accountId) : Promise.resolve([]),
      materialElementApi.list({ page: 1, size: 1 }),
      packageStandardApi.list({ page: 1, size: 1 }),
      mobileConfigApi.list({ page: 1, size: 1 }),
      flowX5Api.list({ page: 1, size: 10000 }),
      flowAnmaApi.list({ page: 1, size: 10000 }),
      subscriptionApi.list({ page: 1, size: 1 }),
      personalApi.list({ page: 1, size: 1 }),
      employeeApi.list({ page: 1, size: 1 }),
      certApi.list({ page: 1, size: 10000 }),
      supplierGradeApi.list({ page: 1, size: 10000 }),
      orderApi.list({ page: 1, size: 10000 }),
      financeInvoiceApi.list({ page: 1, size: 10000 }),
      saleDailyReportApi.list({ page: 1, size: 10000 }),
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
    orderBar.value =
      orderRes.status === 'fulfilled' && Array.isArray(orderRes.value?.list)
        ? groupCount(orderRes.value.list, 'status')
        : []
    invoiceBar.value =
      invoiceRes.status === 'fulfilled' && Array.isArray(invoiceRes.value?.list)
        ? groupCount(invoiceRes.value.list, 'status')
        : []
    flowBar.value = mergeFlowStatus([x5Res, anmaRes])
    dailyTrend.value =
      dailyRes.status === 'fulfilled' && Array.isArray(dailyRes.value?.list)
        ? dailyRes.value.list.filter((r) => r.reportDate)
        : []

    // 等图表容器（v-if 渲染）就位后初始化/重绘图表
    await nextTick()
    renderAll()
  } finally {
    loading.value = false
  }
}

/** 通用柱状图：category 名称轴 + value 数量轴，每柱循环调色板取色，圆角 4px（各模块数据量/状态分布共用） */
function buildStatusBarOption(
  data: { name: string; value: number }[],
  seriesName = '数量',
): echarts.EChartsOption {
  const c = { text: chartTextColor(), line: chartLineColor() }
  const colors = palette()
  return {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}',
    },
    grid: { left: 48, right: 24, top: 32, bottom: 36 },
    xAxis: {
      type: 'category',
      data: data.map((s) => s.name),
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
        name: seriesName,
        type: 'bar',
        barWidth: 26,
        itemStyle: { borderRadius: [4, 4, 0, 0] },
        // 每根柱子循环调色板取色，保留 4px 圆角
        data: data.map((s, i) => ({
          value: s.value,
          itemStyle: { color: colors[i % colors.length], borderRadius: [4, 4, 0, 0] },
        })),
      },
    ],
  } as echarts.EChartsOption
}

/** 各模块数据量柱状图（全宽） */
function buildBarOption(): echarts.EChartsOption {
  return buildStatusBarOption(moduleStats.value, '数据量')
}

function buildPieOption(data: { name: string; value: number }[]): echarts.EChartsOption {
  const c = { text: chartTextColor() }
  return {
    backgroundColor: 'transparent',
    color: palette(),
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

/** 折线图：业务日报趋势（联系人数/线索数/成交数），按 reportDate 升序取后 14 条 */
function buildLineOption(): echarts.EChartsOption {
  const c = { text: chartTextColor(), line: chartLineColor() }
  const colors = palette()
  // 按 reportDate 升序排序；日期短显示全部，否则取后 14 条
  const sorted = [...dailyTrend.value].sort((a, b) =>
    String(a.reportDate ?? '').localeCompare(String(b.reportDate ?? '')),
  )
  const recent = sorted.slice(-14)
  const dates = recent.map((r) => r.reportDate ?? '')
  // dealCnt 存在且有非 0 数据时追加第三条"成交数"
  const hasDeal = recent.some((r) => Number(r.dealCnt ?? 0) > 0)
  // 折线工厂：平滑曲线 + 线宽 2 + 圆点 symbol + 半透明面积渐变
  const mkLine = (
    name: string,
    color: string,
    key: 'contactCnt' | 'leadCnt' | 'dealCnt',
  ): echarts.SeriesOption => ({
    name,
    type: 'line',
    smooth: true,
    symbol: 'circle',
    symbolSize: 6,
    lineStyle: { width: 2, color },
    itemStyle: { color },
    areaStyle: { opacity: 0.12 },
    data: recent.map((r) => Number(r[key] ?? 0)),
  })
  return {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      formatter: '{a}: {b}: {c}',
    },
    legend: {
      top: 0,
      itemWidth: 12,
      itemHeight: 12,
      textStyle: { color: c.text },
    },
    grid: { left: 48, right: 24, top: 40, bottom: 36 },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
      axisLine: { lineStyle: { color: c.line } },
      axisLabel: { color: c.text },
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: { lineStyle: { color: c.line } },
      axisLabel: { color: c.text },
    },
    // 前两色：宝蓝 + 翠绿；第三条"成交数"取第三色
    series: [
      mkLine('联系人数', colors[0], 'contactCnt'),
      mkLine('线索数', colors[1], 'leadCnt'),
      ...(hasDeal ? [mkLine('成交数', colors[2], 'dealCnt')] : []),
    ],
  } as echarts.EChartsOption
}

function renderAll() {
  if (barEl.value && hasBarData.value) {
    if (!barChart.value) barChart.value = echarts.init(barEl.value)
    barChart.value.setOption(buildBarOption(), { notMerge: true })
  }
  if (dailyEl.value && hasDailyData.value) {
    if (!dailyChart.value) dailyChart.value = echarts.init(dailyEl.value)
    dailyChart.value.setOption(buildLineOption(), { notMerge: true })
  }
  if (certEl.value && hasCertData.value) {
    if (!certChart.value) certChart.value = echarts.init(certEl.value)
    certChart.value.setOption(buildPieOption(certPie.value), { notMerge: true })
  }
  if (supplierEl.value && hasSupplierData.value) {
    if (!supplierChart.value) supplierChart.value = echarts.init(supplierEl.value)
    supplierChart.value.setOption(buildPieOption(supplierPie.value), { notMerge: true })
  }
  if (orderEl.value && hasOrderData.value) {
    if (!orderChart.value) orderChart.value = echarts.init(orderEl.value)
    orderChart.value.setOption(buildStatusBarOption(orderBar.value), { notMerge: true })
  }
  if (invoiceEl.value && hasInvoiceData.value) {
    if (!invoiceChart.value) invoiceChart.value = echarts.init(invoiceEl.value)
    invoiceChart.value.setOption(buildStatusBarOption(invoiceBar.value), { notMerge: true })
  }
  if (flowEl.value && hasFlowData.value) {
    if (!flowChart.value) flowChart.value = echarts.init(flowEl.value)
    flowChart.value.setOption(buildStatusBarOption(flowBar.value), { notMerge: true })
  }
}

function resizeAll() {
  barChart.value?.resize()
  dailyChart.value?.resize()
  certChart.value?.resize()
  supplierChart.value?.resize()
  orderChart.value?.resize()
  invoiceChart.value?.resize()
  flowChart.value?.resize()
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
  dailyChart.value?.dispose()
  certChart.value?.dispose()
  supplierChart.value?.dispose()
  orderChart.value?.dispose()
  invoiceChart.value?.dispose()
  flowChart.value?.dispose()
  barChart.value = null
  dailyChart.value = null
  certChart.value = null
  supplierChart.value = null
  orderChart.value = null
  invoiceChart.value = null
  flowChart.value = null
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

    <div class="chart-full">
      <el-card class="chart-card" shadow="never">
        <template #header><span class="chart-title">各模块数据量</span></template>
        <div v-if="hasBarData" ref="barEl" class="chart-box"></div>
        <el-empty v-else description="暂无模块数据" :image-size="72" />
      </el-card>
    </div>

    <div class="chart-full">
      <el-card class="chart-card" shadow="never">
        <template #header><span class="chart-title">业务日报趋势</span></template>
        <div v-if="hasDailyData" ref="dailyEl" class="chart-box"></div>
        <el-empty v-else description="暂无业务日报数据" :image-size="72" />
      </el-card>
    </div>

    <div class="chart-grid">
      <el-card class="chart-card" shadow="never">
        <template #header><span class="chart-title">证照类型分布</span></template>
        <div v-if="hasCertData" ref="certEl" class="chart-box"></div>
        <el-empty v-else description="暂无证照数据" :image-size="72" />
      </el-card>

      <el-card class="chart-card" shadow="never">
        <template #header><span class="chart-title">供应商分级分布</span></template>
        <div v-if="hasSupplierData" ref="supplierEl" class="chart-box"></div>
        <el-empty v-else description="暂无供应商分级数据" :image-size="72" />
      </el-card>

      <el-card class="chart-card" shadow="never">
        <template #header><span class="chart-title">采购订单状态分布</span></template>
        <div v-if="hasOrderData" ref="orderEl" class="chart-box"></div>
        <el-empty v-else description="暂无采购订单数据" :image-size="72" />
      </el-card>

      <el-card class="chart-card" shadow="never">
        <template #header><span class="chart-title">发票状态分布</span></template>
        <div v-if="hasInvoiceData" ref="invoiceEl" class="chart-box"></div>
        <el-empty v-else description="暂无发票数据" :image-size="72" />
      </el-card>

      <el-card class="chart-card" shadow="never">
        <template #header><span class="chart-title">流程实例状态分布</span></template>
        <div v-if="hasFlowData" ref="flowEl" class="chart-box"></div>
        <el-empty v-else description="暂无流程实例数据" :image-size="72" />
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

.chart-full {
  margin-bottom: 16px;
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.chart-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
}

.chart-box {
  height: 260px;
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
