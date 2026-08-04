<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useDemoStore } from '../stores/demo'
import type { GameFactor, GameResultRow } from '../stores/demo'

const emit = defineEmits<{ (e: 'confirm', planId: string): void }>()

const store = useDemoStore()
const checkedFactors = ref<string[]>([])

interface ResultRow {
  planId: string
  planName: string
  winRateBefore: number
  winRateAfter: number
}

/** 博弈因素列表（无参加载时返回，驱动勾选框） */
const factors = ref<GameFactor[]>([])
/** 当前胜率表（对抗前/对抗后），随勾选变化刷新 */
const rows = ref<ResultRow[]>([])
/** 初始无参加载的 before 基线，带参查询失败时回退 */
const beforeRows = ref<ResultRow[]>([])

function planNameOf(planId: string): string {
  return store.solutions.find((s) => s.id === planId)?.name ?? planId
}

function toRows(results: GameResultRow[]): ResultRow[] {
  return results.map((r) => ({
    planId: r.planId,
    planName: planNameOf(r.planId),
    winRateBefore: r.winRateBefore,
    winRateAfter: r.winRateAfter,
  }))
}

/**
 * 方案 A：直接调后端 /api/demo/game-results。
 * 无参 → before 基线（同时保存供回退）；带参 → 命中表结果。
 * 注意 URL 中 '+' 会按空格解析，后端已支持 '+'/','/空白 分隔与字母序解析。
 */
async function fetchResults(factorsArg?: string[]) {
  const hasFactors = !!(factorsArg && factorsArg.length)
  const qs = hasFactors ? `?factors=${factorsArg!.slice().sort().join('+')}` : ''
  try {
    const res = await fetch(`/api/demo/game-results${qs}`)
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    const data = await res.json()
    factors.value = Array.isArray(data.factors) ? (data.factors as GameFactor[]) : []
    const results: GameResultRow[] = Array.isArray(data.results) ? data.results : []
    if (hasFactors) {
      rows.value = toRows(results)
    } else {
      beforeRows.value = toRows(results)
      rows.value = beforeRows.value
    }
  } catch (err) {
    console.error('[GamePanel] 拉取 game-results 失败', err)
    // 保底：带参查询失败时回退到已加载的 before 数据
    if (hasFactors && beforeRows.value.length) rows.value = beforeRows.value
  }
}

function initChecked() {
  const fs = factors.value
  const explicit = fs.filter((f) => f.defaultChecked).map((f) => f.id)
  checkedFactors.value = explicit.length ? explicit : fs.slice(0, 2).map((f) => f.id)
}

function onFactorChange() {
  void fetchResults(checkedFactors.value.slice().sort())
}

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
  await fetchResults()
  initChecked()
  // 默认勾选后也要触发一次带 factors 的查询，否则胜率停留在 before 基线
  if (checkedFactors.value.length) {
    await fetchResults(checkedFactors.value)
  }
})
</script>

<template>
  <section class="game-panel fade-up">
    <h3 class="panel-title">9RP 博弈对抗</h3>

    <div class="panel-section-title">对抗因素</div>
    <el-checkbox-group v-model="checkedFactors" class="factor-group" @change="onFactorChange">
      <el-checkbox v-for="f in factors" :key="f.id" :value="f.id">
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
