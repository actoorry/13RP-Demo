<template>
  <div class="panel" style="padding: 0">
    <div class="map-header">
      <div class="panel-title" style="margin: 0; padding: 10px 12px 0">
        <span>6RP 物流网络图</span>
        <span class="badge">中国 · 港口/仓库节点</span>
      </div>
    </div>
    <div ref="chartEl" class="map-body" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref, watch } from 'vue';
import * as echarts from 'echarts';
import { useDemoStore } from '../stores/demo';
import { NODES, ROUTES, PORT_STATUS_COLOR } from '../utils/constants';

const store = useDemoStore();
const chartEl = ref<HTMLDivElement>();
let chart: echarts.ECharts | null = null;
let chinaLoaded = false;

async function ensureChinaMap() {
  if (chinaLoaded) return;
  try {
    const res = await fetch('/geo/china.json');
    const geo = await res.json();
    echarts.registerMap('china', geo);
    chinaLoaded = true;
  } catch {
    chinaLoaded = false; // 地图加载失败时退化为纯散点（无底图）
  }
}

function buildOption() {
  const ports = store.ports || {};
  const portData = NODES.filter(n => n.type === 'port').map(n => ({
    name: n.name,
    value: [n.lng, n.lat, 1],
    status: ports[n.id] || 'NORMAL'
  }));
  const otherNodes = NODES.filter(n => n.type !== 'port').map(n => ({
    name: n.name, value: [n.lng, n.lat, 1]
  }));

  // 高亮路线：封港发生时显示替代路线（宁波→青岛 等）
  const hasClosure = Object.values(ports).some(s => s === 'CLOSED');
  const lineData = ROUTES.filter(r => hasClosure || r.mode === 'road').map(r => {
    const a = NODES.find(n => n.id === r.start)!;
    const b = NODES.find(n => n.id === r.end)!;
    return {
      coords: [[a.lng, a.lat], [b.lng, b.lat]],
      name: r.name
    };
  });

  return {
    backgroundColor: 'transparent',
    geo: {
      map: chinaLoaded ? 'china' : undefined,
      roam: true,
      zoom: 1.1,
      itemStyle: { areaColor: '#161b22', borderColor: '#30363d' },
      emphasis: { itemStyle: { areaColor: '#1f2937' } }
    },
    series: [
      {
        type: 'effectScatter',
        coordinateSystem: 'geo',
        rippleEffect: { scale: 3, brushType: 'stroke' },
        data: portData.map((d: any) => ({
          name: d.name, value: d.value,
          itemStyle: { color: PORT_STATUS_COLOR[d.status] || '#8b949e' }
        })),
        symbolSize: (val: number[]) => (hasClosure && val[2] === 1 ? 14 : 11),
        label: { show: true, position: 'right', color: '#e6edf3', fontSize: 11 },
        emphasis: { label: { show: true, fontWeight: 'bold' } }
      },
      {
        type: 'scatter',
        coordinateSystem: 'geo',
        data: otherNodes,
        symbolSize: 8,
        itemStyle: { color: '#8b949e' },
        label: { show: true, position: 'right', color: '#8b949e', fontSize: 11 }
      },
      {
        type: 'lines',
        coordinateSystem: 'geo',
        zlevel: 2,
        effect: { show: true, period: 4, trailLength: 0.3, symbol: 'arrow', symbolSize: 6, color: '#00d4aa' },
        lineStyle: { color: hasClosure ? '#ffa940' : '#4a90d9', width: 1.5, opacity: 0.6 },
        data: lineData
      }
    ]
  };
}

function render() {
  if (!chart) return;
  chart.setOption(buildOption(), true);
}

onMounted(async () => {
  await ensureChinaMap();
  chart = echarts.init(chartEl.value!);
  render();
  const onResize = () => chart?.resize();
  window.addEventListener('resize', onResize);
  onUnmounted(() => window.removeEventListener('resize', onResize));
});

// 港口状态变化 → 重绘
watch(() => store.ports, render, { deep: true });
</script>

<style scoped>
.map-body { flex: 1; min-height: 0; }
</style>
