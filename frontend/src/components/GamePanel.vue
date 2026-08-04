<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useDemoStore } from '../stores/demo'

const emit = defineEmits<{ (e: 'confirm', planId: string): void }>()

const store = useDemoStore()
const checkedFactors = ref<string[]>([])

interface ResultRow {
  planId: string
  planName: string
  winRateBefore: number
  winRateAfter: number
}

async function load(factors?: string[]) {
  await store.fetchGameResults(factors)
}

function initChecked() {
  const fs = store.gameResults?.factors ?? []
  const explicit = fs.filter((f) => f.defaultChecked).map((f) => f.id)
  checkedFactors.value = explicit.length ? explicit : fs.slice(0, 2).map((f) => f.id)
}

function onFactorChange() {
  const factors = checkedFactors.value.slice().sort()
  load(factors)
}

const rows = computed<ResultRow[]>(() => {
  const results = store.gameResults?.results ?? []
  const byPlan = new Map(store.solutions.map((s) => [s.id, s.name]))
  return results.map((r) => ({
    planId: r.planId,
    planName: byPlan.get(r.planId) ?? r.planId,
    winRateBefore: r.winRateBefore,
    winRateAfter: r.winRateAfter,
  }))
})

const maxAfter = computed(() => {
  if (!rows.value.length) return -1
  return Math.max(...rows.value.map((r) => r.winRateAfter))
})

const winner = computed(() => rows.value.find((r) => r.winRateAfter === maxAfter.value))

function rowClass({ row }: { row: ResultRow }): string {
  return row.winRateAfter === maxAfter.value ? 'winner-row' : ''
}

function trendClass(row: ResultRow): string {
  if (row.winRateAfter > row.winRateBefore) return 'trend-up'
  if (row.winRateAfter < row.winRateBefore) return 'trend-down'
  return ''
}

function confirmWinner() {
  if (winner.value) emit('confirm', winner.value.planId)
}

onMounted(async () => {
  if (!store.gameResults) await load()
  initChecked()
})
</script>

<template>
  <section class="game-panel fade-up">
    <h3 class="panel-title">9RP 博弈对抗</h3>

    <div class="panel-section-title">对抗因素</div>
    <el-checkbox-group v-model="checkedFactors" class="factor-group" @change="onFactorChange">
      <el-checkbox v-for="f in store.gameResults?.factors ?? []" :key="f.id" :value="f.id">
        {{ f.name }}
      </el-checkbox>
    </el-checkbox-group>

    <div class="panel-section-title">胜率对比</div>
    <el-table :data="rows" size="small" :row-class-name="rowClass" class="win-table">
      <el-table-column prop="planName" label="方案" min-width="110" />
      <el-table-column label="对抗前" width="90" align="center">
        <template #default="{ row }">
          <span class="mono">{{ row.winRateBefore }}%</span>
        </template>
      </el-table-column>
      <el-table-column label="对抗后" width="110" align="center">
        <template #default="{ row }">
          <span class="mono" :class="trendClass(row)">
            {{ row.winRateAfter }}%
            <span v-if="row.winRateAfter > row.winRateBefore">↑</span>
            <span v-else-if="row.winRateAfter < row.winRateBefore">↓</span>
          </span>
        </template>
      </el-table-column>
    </el-table>

    <el-button
      type="primary"
      class="confirm-btn"
      :disabled="!winner"
      @click="confirmWinner"
    >
      确认方案：{{ winner ? winner.planName : '—' }}
    </el-button>
  </section>
</template>

<style scoped>
.factor-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.factor-group :deep(.el-checkbox) {
  margin-right: 0;
}

.win-table {
  margin-bottom: 16px;
}

.win-table :deep(.winner-row) {
  background: rgba(0, 212, 170, 0.12) !important;
}

.trend-up {
  color: var(--color-primary);
  font-weight: 600;
}

.trend-down {
  color: var(--color-alert);
  font-weight: 600;
}

.confirm-btn {
  width: 100%;
  font-weight: 600;
  letter-spacing: 1px;
}
</style>
