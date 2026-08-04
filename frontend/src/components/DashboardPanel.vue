<script setup lang="ts">
import { computed } from 'vue'
import { useDemoStore } from '../stores/demo'
import { STATUS_COLOR, STATUS_LABEL, type PortStatus } from '../constants/demo'

const store = useDemoStore()

const deliveryRate = computed(() =>
  Math.max(0, Math.min(100, Math.round(store.deliveryRate))),
)
const deliveryIsLow = computed(() => store.deliveryRate < 80)
const ports = computed(() => store.ports.filter((p) => p.kind === 'port'))
const inventoryEntries = computed(() => Object.entries(store.inventory))
const invMax = computed(() =>
  Math.max(1, ...Object.values(store.inventory).map((v) => v.qty)),
)

function statusColor(status: string): string {
  return STATUS_COLOR[(status as PortStatus)] ?? STATUS_COLOR.NORMAL
}

function statusLabel(status: string): string {
  return STATUS_LABEL[(status as PortStatus)] ?? status
}
</script>

<template>
  <div class="dashboard">
    <h3 class="panel-title">4RP 实时看板</h3>

    <section class="panel-section">
      <div class="metric-row">
        <div class="metric-label">订单交付率</div>
        <div
          class="metric-value mono"
          :style="{ color: deliveryIsLow ? 'var(--color-alert)' : 'var(--color-primary)' }"
        >
          {{ deliveryRate }}%
        </div>
      </div>
      <div class="rate-bar">
        <div
          class="rate-bar-fill"
          :style="{
            width: deliveryRate + '%',
            background: deliveryIsLow ? 'var(--color-alert)' : 'var(--color-primary)',
          }"
        ></div>
      </div>
      <div v-if="deliveryIsLow" class="rate-warn blink">⚠ 交付率低于 80%，触发预警</div>
    </section>

    <section class="panel-section">
      <div class="metric-row">
        <div class="metric-label">未交付订单</div>
        <div class="metric-value mono">{{ store.openOrders }}</div>
      </div>
    </section>

    <section class="panel-section">
      <div class="panel-section-title">港口状态</div>
      <ul class="port-list">
        <li v-for="p in ports" :key="p.code" class="port-item">
          <span class="port-dot" :style="{ background: statusColor(p.status) }"></span>
          <span class="port-name">{{ p.name }}</span>
          <span
            class="port-status mono"
            :style="{ color: statusColor(p.status) }"
          >
            {{ statusLabel(p.status) }}
          </span>
        </li>
      </ul>
    </section>

    <section class="panel-section">
      <div class="panel-section-title">品类库存</div>
      <ul v-if="inventoryEntries.length" class="inv-list">
        <li v-for="[name, value] in inventoryEntries" :key="name" class="inv-item">
          <span class="inv-name">{{ name }}</span>
          <span class="inv-bar">
            <span
              class="inv-bar-fill"
              :style="{ width: Math.round((value.qty / invMax) * 100) + '%' }"
            ></span>
          </span>
          <span class="inv-value mono">
            {{ value.qty }}<span v-if="value.unit" class="inv-unit">{{ value.unit }}</span>
          </span>
        </li>
      </ul>
      <div v-else class="empty-tip">等待数据推送…</div>
    </section>
  </div>
</template>

<style scoped>
.metric-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 6px;
}

.metric-label {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.metric-value {
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
}

.rate-bar {
  height: 8px;
  background: var(--color-bg-elevated);
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 6px;
}

.rate-bar-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.5s var(--ease-out), background 0.3s var(--ease-out);
}

.rate-warn {
  font-size: 12px;
  color: var(--color-alert);
}

.port-list,
.inv-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.port-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 0;
  border-bottom: 1px dashed var(--color-border);
}

.port-item:last-child {
  border-bottom: none;
}

.port-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  box-shadow: 0 0 6px currentColor;
}

.port-name {
  flex: 1;
  font-size: 13px;
}

.port-status {
  font-size: 12px;
  font-weight: 600;
}

.inv-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 5px 0;
}

.inv-name {
  width: 64px;
  font-size: 12px;
  color: var(--color-text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.inv-bar {
  flex: 1;
  height: 6px;
  background: var(--color-bg-elevated);
  border-radius: 3px;
  overflow: hidden;
}

.inv-bar-fill {
  display: block;
  height: 100%;
  background: var(--color-data);
  border-radius: 3px;
  transition: width 0.5s var(--ease-out);
}

.inv-value {
  width: 56px;
  text-align: right;
  font-size: 12px;
  color: var(--color-text);
}

.inv-unit {
  margin-left: 2px;
  font-size: 11px;
  color: var(--color-text-muted);
}
</style>
