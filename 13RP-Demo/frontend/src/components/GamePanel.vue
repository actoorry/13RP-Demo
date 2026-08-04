<template>
  <div>
    <div style="margin-bottom:8px; font-size:13px; color:var(--text-dim)">
      加载对抗因素，观察胜率变化：
    </div>

    <el-checkbox-group v-model="checked" @change="recalc" style="display:flex; flex-direction:column; gap:6px; margin-bottom:12px">
      <el-checkbox v-for="f in factors" :key="f.id" :value="f.id" :label="f.name" />
    </el-checkbox-group>

    <div class="game-table">
      <div class="game-row head">
        <span>方案</span>
        <span>对抗前</span>
        <span>对抗后</span>
      </div>
      <div v-for="p in planRows" :key="p.planId" class="game-row" :class="{ 'best': p.planId === bestPlan }">
        <span>{{ planName(p.planId) }}</span>
        <span class="mono">{{ p.before }}%</span>
        <span class="mono" :class="p.after >= p.before ? 'up' : 'down'">
          {{ p.after }}% {{ p.after >= p.before ? '↑' : '↓' }}
        </span>
      </div>
    </div>

    <div style="margin:10px 0; font-size:12px; color:var(--text-dim); line-height:1.7">
      {{ analysis }}
    </div>

    <el-button type="primary" size="large" style="width:100%" @click="confirm">
      确认方案：{{ bestPlan === 'P2' ? '空运 P2' : bestPlan === 'P1' ? '改港青岛 P1' : '备选供应商 P3' }}
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';

const emit = defineEmits<{ (e: 'confirm', planId: string): void }>();

const factors = ref<any[]>([]);
const checked = ref<string[]>([]);
const results = ref<Record<string, any>>({});
const before = ref<Record<string, number>>({});

const planNames: Record<string, string> = {
  P1: '改港青岛 + 铁路转运',
  P2: '空运关键物料',
  P3: '启用备选供应商'
};

const planRows = computed(() =>
  ['P1', 'P2', 'P3'].map(id => ({
    planId: id,
    before: before.value[id] ?? 0,
    after: currentResults.value[id] ?? 0
  }))
);

const currentResults = computed(() => {
  const key = [...checked.value].sort().join('+') || 'none';
  return results.value[key] || before.value;
});

const bestPlan = computed(() => {
  const rows = planRows.value;
  return rows.reduce((a, b) => (b.after > a.after ? b : a)).planId;
});

const analysis = computed(() => {
  if (!checked.value.length) {
    return '未加载对抗因素：各方案胜率基于当前环境基线。';
  }
  return `已加载 ${checked.value.length} 个对抗因素。观察：${checked.value.includes('congestion') && checked.value.includes('competitor') ? '港口拥堵 + 对手抢舱削弱空运优势，改港方案胜率反超。' : '当前组合下方案排序已更新，建议重新评估。'}`;
});

function planName(id: string) { return planNames[id] || id; }

function recalc() {
  // 胜率数字滚动由 CSS 过渡呈现（简单实现）
}

onMounted(async () => {
  const res = await fetch('/api/demo/game-results');
  const data = await res.json();
  factors.value = data.factors || [];
  results.value = data.results || {};
  before.value = data.before || {};
  checked.value = (data.factors || []).filter((f: any) => f.defaultChecked).map((f: any) => f.id);
});

function confirm() {
  emit('confirm', bestPlan.value);
}
</script>

<style scoped>
.game-table { border: 1px solid var(--panel-border); border-radius: 8px; overflow: hidden; }
.game-row { display: grid; grid-template-columns: 1fr 70px 90px; padding: 8px 12px; font-size: 13px; border-bottom: 1px solid var(--panel-border); }
.game-row:last-child { border-bottom: none; }
.game-row.head { background: rgba(255,255,255,0.04); color: var(--text-dim); font-size: 12px; }
.game-row.best { background: rgba(0, 212, 170, 0.08); }
.up { color: var(--primary); }
.down { color: var(--alert); }
</style>
