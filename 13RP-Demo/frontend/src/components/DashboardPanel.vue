<template>
  <div class="panel">
    <div class="panel-title">
      <span>4RP 监控看板</span>
      <span class="badge">实时推送 1s</span>
    </div>

    <div class="metric">
      <div class="metric-label">订单交付率</div>
      <div class="metric-value mono" :class="{ 'metric-alert': store.deliveryRate < 80, blink: store.deliveryRate < 70 }">
        {{ store.deliveryRate.toFixed(1) }}%
      </div>
      <el-progress :percentage="store.deliveryRate" :stroke-width="8"
        :color="store.deliveryRate < 80 ? '#ff4757' : '#00d4aa'" />
    </div>

    <div class="metric">
      <div class="metric-label">未交付订单</div>
      <div class="metric-value mono">{{ store.openOrders }} 单</div>
    </div>

    <div class="metric">
      <div class="metric-label">港口状态</div>
      <div class="port-list">
        <div v-for="(status, id) in store.ports" :key="id" class="port-row">
          <span class="dot" :style="{ background: colorOf(status) }" />
          <span class="port-name">{{ store.portNames[id] || id }}</span>
          <span class="port-status" :style="{ color: colorOf(status) }">{{ statusLabel(status) }}</span>
        </div>
      </div>
    </div>

    <div class="metric">
      <div class="metric-label">品类库存水位（吨）</div>
      <div v-for="(qty, name) in store.inventory" :key="name" class="inv-row">
        <span>{{ name }}</span>
        <span class="mono" :class="{ 'metric-alert': qty < 1000 }">{{ qty }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useDemoStore } from '../stores/demo';
import { PORT_STATUS_COLOR } from '../utils/constants';

const store = useDemoStore();

function colorOf(status: string) {
  return PORT_STATUS_COLOR[status] || '#8b949e';
}

function statusLabel(status: string) {
  return { NORMAL: '正常', CLOSED: '封港', CONGESTED: '拥堵' }[status] || status;
}
</script>

<style scoped>
.metric { margin-bottom: 14px; }
.metric-label { color: var(--text-dim); font-size: 12px; margin-bottom: 4px; }
.metric-value { font-size: 28px; font-weight: 700; }
.metric-alert { color: var(--alert); }
.port-list { display: flex; flex-direction: column; gap: 6px; }
.port-row { display: flex; align-items: center; gap: 8px; font-size: 13px; }
.dot { width: 8px; height: 8px; border-radius: 50%; }
.port-name { flex: 1; }
.port-status { font-size: 12px; }
.inv-row { display: flex; justify-content: space-between; font-size: 13px; padding: 2px 0; }
</style>
