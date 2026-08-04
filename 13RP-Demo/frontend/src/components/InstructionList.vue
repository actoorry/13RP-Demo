<template>
  <div>
    <el-alert type="success" :closable="false" style="margin-bottom:12px">
      <template #title>执行方案：{{ store.planName || '已确认方案' }}</template>
      5 条指令 · 精确到负责人 / 截止时间 / 失效条件
    </el-alert>

    <el-timeline style="max-height: 460px; overflow-y: auto">
      <el-timeline-item v-for="ins in store.instructions" :key="ins.seq" :timestamp="`指令 ${ins.seq}`" placement="top">
        <div class="ins-card fade-up">
          <div class="ins-action">{{ ins.seq }}. {{ ins.action }}</div>
          <div class="ins-fields">
            <el-tag v-for="(v, k) in ins.contractFields" :key="k" size="small" type="info" class="field-tag">
              {{ k }}：{{ v }}
            </el-tag>
          </div>
          <div class="ins-meta mono">
            👤 {{ ins.owner }} · ⏰ {{ ins.deadline }} · 📄 {{ ins.docNo }}
          </div>
          <div class="ins-falsifiable">⚠ 失效条件：{{ ins.falsifiableCondition }}</div>
        </div>
      </el-timeline-item>
    </el-timeline>

    <div style="margin-top:12px; font-size:12px; color:var(--text-dim)">
      可证伪性：每条指令附带"什么条件下会失效"——这是 13RP 的核心设计原则。
    </div>

    <el-button size="small" style="width:100%; margin-top:10px" @click="resetDemo">
      重置演示（重新开始）
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { useDemoStore } from '../stores/demo';
import { useWebSocket } from '../composables/useWebSocket';

const store = useDemoStore();
const { send } = useWebSocket(() => {});

function resetDemo() {
  send('reset');
  store.solutions = [];
  store.instructions = [];
  store.planName = '';
}
</script>

<style scoped>
.ins-card {
  border: 1px solid var(--panel-border);
  border-radius: 8px;
  padding: 10px 12px;
  background: rgba(255, 255, 255, 0.02);
}
.ins-action { font-size: 14px; font-weight: 600; margin-bottom: 6px; }
.ins-fields { display: flex; flex-wrap: wrap; gap: 4px; margin-bottom: 6px; }
.field-tag { --el-tag-bg-color: rgba(74, 144, 217, 0.12); --el-tag-border-color: rgba(74, 144, 217, 0.3); --el-tag-text-color: #9ec5f0; }
.ins-meta { font-size: 12px; color: var(--text-dim); margin-bottom: 4px; }
.ins-falsifiable { font-size: 12px; color: var(--warn); }
</style>
