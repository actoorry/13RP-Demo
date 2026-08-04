<script setup lang="ts">
import { computed } from 'vue'
import { useDemoStore } from '../stores/demo'

const emit = defineEmits<{ (e: 'reset'): void }>()

const store = useDemoStore()
const instructions = computed(() => store.instructions)
</script>

<template>
  <section class="instruction-panel fade-up">
    <div class="inst-head">
      <h3 class="panel-title">5RP 降维输出 — 可执行指令</h3>
      <span v-if="store.planName" class="plan-name mono">{{ store.planName }}</span>
    </div>

    <el-timeline v-if="instructions.length" class="inst-timeline">
      <el-timeline-item
        v-for="ins in instructions"
        :key="ins.seq"
        :timestamp="`#${ins.docNo}`"
        placement="top"
      >
        <div class="inst-card">
          <div class="inst-action">{{ ins.seq }}. {{ ins.action }}</div>
          <div class="inst-fields">
            <el-tag
              v-for="(v, k) in ins.contractFields"
              :key="k"
              size="small"
              effect="plain"
              class="inst-tag"
            >
              {{ k }}：{{ v }}
            </el-tag>
          </div>
          <div class="inst-meta mono">
            <span>负责人：{{ ins.owner }}</span>
            <span>截止：{{ ins.deadline }}</span>
          </div>
          <div v-if="ins.falsifiableCondition" class="inst-falsify">
            ⚠ 失效条件：{{ ins.falsifiableCondition }}
          </div>
        </div>
      </el-timeline-item>
    </el-timeline>
    <div v-else class="empty-tip">暂无指令数据</div>

    <el-button type="danger" plain class="reset-btn" @click="emit('reset')">
      重置演示
    </el-button>
  </section>
</template>

<style scoped>
.inst-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 8px;
}

.inst-head .panel-title {
  flex: 1;
}

.plan-name {
  font-size: 12px;
  color: var(--color-primary);
  white-space: nowrap;
}

.inst-timeline {
  padding-left: 4px;
}

.inst-card {
  background: var(--color-bg-elevated);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  padding: 10px 12px;
}

.inst-action {
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 6px;
}

.inst-fields {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-bottom: 6px;
}

.inst-tag {
  background: rgba(74, 144, 217, 0.12);
  border-color: rgba(74, 144, 217, 0.35);
  color: var(--color-data);
}

.inst-meta {
  display: flex;
  gap: 14px;
  font-size: 12px;
  color: var(--color-text-secondary);
}

.inst-falsify {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed var(--color-border);
  font-size: 12px;
  color: var(--color-warn);
}

.reset-btn {
  width: 100%;
  margin-top: 12px;
}
</style>
