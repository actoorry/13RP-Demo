<script setup lang="ts">
import { onMounted, onUnmounted, ref, shallowRef, watch } from 'vue'
import * as echarts from 'echarts'
import { useDemoStore } from '../stores/demo'
import { SUPPLY_NODES, ROUTES, STATUS_COLOR } from '../constants/demo'

const store = useDemoStore()
const chartEl = ref<HTMLDivElement | null>(null)
const chart = shallowRef<echarts.ECharts | null>(null)
const hasMap = ref(false)

let resizeObserver: ResizeObserver | null = null

const defs = Object.fromEntries(SUPPLY_NODES.map((d) => [d.code, d]))

function shortageActive(): boolean {
  return store.suppliers.some((p) => p.code === 'BAOTOU' && p.status === 'SHORTAGE')
}

function nodeStatusColor(status: string): string {
  return STATUS_COLOR[(status as keyof typeof STATUS_COLOR)] ?? STATUS_COLOR.NORMAL
}

async function loadMap() {
  try {
    const res = await fetch('/geo/china.json')
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    const geoJson = await res.json()
    echarts.registerMap('china', geoJson as Parameters<typeof echarts.registerMap>[1])
    hasMap.value = true
  } catch (err) {
    console.error('[LogisticsMap] 中国地图加载失败，降级为无底图坐标视图', err)
    hasMap.value = false
  }
  render()
}

function buildSupplierData() {
  return store.suppliers
    .filter((p) => p.kind === 'supplier')
    .map((p) => ({
      name: p.name,
      value: [p.lng, p.lat, 1] as [number, number, number],
      itemStyle: { color: nodeStatusColor(p.status) },
    }))
}

function buildNodeData() {
  return store.suppliers
    .filter((p) => p.kind !== 'supplier')
    .map((p) => ({
      name: p.name,
      value: [p.lng, p.lat] as [number, number],
      itemStyle: { color: '#4a90d9' },
    }))
}

function buildRouteData() {
  const shortage = shortageActive()
  const result: { coords: [number, number][]; lineStyle: Record<string, unknown> }[] = []
  for (const r of ROUTES) {
    const from = defs[r.from]
    const to = defs[r.to]
    if (!from || !to) continue
    const alt = shortage && !!r.alternative
    const dimmed = shortage && (r.from === 'BAOTOU' || r.to === 'BAOTOU')
    result.push({
      coords: [
        [from.lng, from.lat] as [number, number],
        [to.lng, to.lat] as [number, number],
      ],
      lineStyle: {
        color: alt ? '#00d4aa' : dimmed ? 'rgba(255,71,87,0.35)' : 'rgba(0,212,170,0.35)',
        width: alt ? 3 : 1.5,
        opacity: dimmed ? 0.4 : 1,
      },
    })
  }
  return result
}

function buildFallbackOption(): echarts.EChartsOption {
  const points = store.suppliers.map((p) => ({
    name: p.name,
    value: [p.lng, p.lat] as [number, number],
    itemStyle: { color: p.kind === 'supplier' ? nodeStatusColor(p.status) : '#4a90d9' },
  }))
  return {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      backgroundColor: '#1c2128',
      borderColor: '#30363d',
      textStyle: { color: '#e6edf3' },
    },
    grid: { left: 50, right: 50, top: 50, bottom: 50 },
    xAxis: {
      type: 'value',
      min: 105,
      max: 126,
      axisLabel: { color: '#8b949e' },
      splitLine: { lineStyle: { color: 'rgba(139,148,158,0.12)' } },
    },
    yAxis: {
      type: 'value',
      min: 18,
      max: 43,
      axisLabel: { color: '#8b949e' },
      splitLine: { lineStyle: { color: 'rgba(139,148,158,0.12)' } },
    },
    series: [
      {
        type: 'scatter',
        symbolSize: 12,
        label: { show: true, position: 'right', formatter: '{b}', color: '#8b949e' },
        data: points,
      },
    ],
  } as echarts.EChartsOption
}

function buildOption(): echarts.EChartsOption {
  if (!hasMap.value) return buildFallbackOption()
  const supplierData = buildSupplierData()
  const nodeData = buildNodeData()
  const routeData = buildRouteData()

  return {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      backgroundColor: '#1c2128',
      borderColor: '#30363d',
      textStyle: { color: '#e6edf3' },
    },
    geo: {
      map: 'china',
      roam: true,
      zoom: 1.12,
      label: { show: false },
      itemStyle: { areaColor: '#161b22', borderColor: '#30363d' },
      emphasis: { itemStyle: { areaColor: '#1c2128' }, label: { show: false } },
    },
    series: [
      {
        name: '供应商',
        type: 'effectScatter',
        coordinateSystem: 'geo',
        zlevel: 2,
        symbolSize: 13,
        rippleEffect: { scale: 3.2, brushType: 'stroke' },
        label: { show: true, position: 'right', formatter: '{b}', color: '#8b949e', fontSize: 11 },
        data: supplierData,
      },
      {
        name: '工厂/基地',
        type: 'scatter',
        coordinateSystem: 'geo',
        zlevel: 2,
        symbolSize: 9,
        symbol: 'diamond',
        label: { show: true, position: 'top', formatter: '{b}', color: '#8b949e', fontSize: 10 },
        data: nodeData,
      },
      {
        name: '物流路线',
        type: 'lines',
        coordinateSystem: 'geo',
        zlevel: 1,
        effect: { show: true, period: 4, trailLength: 0.4, symbol: 'arrow', symbolSize: 6 },
        lineStyle: { curveness: 0.18 },
        data: routeData,
      },
    ],
  } as echarts.EChartsOption
}

function render() {
  if (!chartEl.value) return
  if (!chart.value) chart.value = echarts.init(chartEl.value)
  chart.value.setOption(buildOption(), { notMerge: true })
}

function handleResize() {
  chart.value?.resize()
}

onMounted(() => {
  loadMap()
  resizeObserver = new ResizeObserver(handleResize)
  if (chartEl.value) resizeObserver.observe(chartEl.value)
})

onUnmounted(() => {
  resizeObserver?.disconnect()
  resizeObserver = null
  chart.value?.dispose()
  chart.value = null
})

watch(
  () => store.suppliers,
  () => render(),
  { deep: true },
)
</script>

<template>
  <div class="logistics-map">
    <div ref="chartEl" class="map-canvas"></div>
    <div v-if="!hasMap" class="map-fallback-tip">地图资源不可用，已降级为坐标散点视图</div>
    <div class="map-legend">
      <span class="legend-item"><i class="legend-dot" style="background:#00d4aa"></i>正常</span>
      <span class="legend-item"><i class="legend-dot" style="background:#ff4757"></i>缺货</span>
      <span class="legend-item"><i class="legend-dot" style="background:#ffa940"></i>紧张</span>
    </div>
  </div>
</template>

<style scoped>
.logistics-map {
  position: relative;
  width: 100%;
  height: 100%;
  background: var(--color-bg);
}

.map-canvas {
  width: 100%;
  height: 100%;
}

.map-fallback-tip {
  position: absolute;
  top: 10px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 12px;
  color: var(--color-text-muted);
  background: rgba(13, 17, 23, 0.8);
  padding: 2px 10px;
  border-radius: 4px;
}

.map-legend {
  position: absolute;
  right: 12px;
  top: 10px;
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: var(--color-text-secondary);
  background: rgba(22, 27, 34, 0.85);
  padding: 4px 10px;
  border-radius: 4px;
  border: 1px solid var(--color-border);
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.legend-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
}
</style>
