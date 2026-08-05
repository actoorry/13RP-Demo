<script setup lang="ts">
import { computed, ref } from 'vue'
import { useDemoStore } from '../stores/demo'

const emit = defineEmits<{
  (e: 'start', duration: number): void
  (e: 'start-simulation'): void
}>()

const store = useDemoStore()
const dialogVisible = ref(false)
const duration = ref(5)
const isInjected = computed(() => store.phase === 'EVENT_INJECTED')

function openDialog() {
  duration.value = 5
  dialogVisible.value = true
}

function confirmInject() {
  dialogVisible.value = false
  emit('start', duration.value)
}
</script>

<template>
  <section v-if="!isInjected" class="event-panel fade-up">
    <h3 class="panel-title">事件注入</h3>
    <div class="intro">
      模拟一次一级供应中断事件，驱动 13RP 决策闭环：看板报警 → 推演 → 寻优 → 博弈 → 降维输出。
    </div>
    <el-button type="danger" class="inject-btn" @click="openDialog">
      ⚡ 注入供应商缺货事件
    </el-button>

    <el-dialog v-model="dialogVisible" title="注入供应商缺货事件" width="420px" align-center>
      <el-form label-width="90px">
        <el-form-item label="事件类型">
          <span>北方铜业突发停产</span>
        </el-form-item>
        <el-form-item label="影响供应商">
          <span>北方铜业</span>
        </el-form-item>
        <el-form-item label="缺货时长">
          <el-radio-group v-model="duration">
            <el-radio-button :value="3">3 天</el-radio-button>
            <el-radio-button :value="5">5 天</el-radio-button>
            <el-radio-button :value="7">7 天</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmInject">确认注入</el-button>
      </template>
    </el-dialog>
  </section>

  <section v-else class="alert-panel fade-up">
    <h3 class="panel-title" style="color: var(--color-alert)">⚠ 一级供应中断</h3>
    <div class="alert-box shake">
      <div class="alert-msg">检测到供应商缺货事件，北方铜业已停止供货。</div>
      <div class="alert-sub">订单交付率持续下降，建议立即启动 7RP 方案推演。</div>
    </div>
    <el-button type="primary" class="inject-btn" @click="emit('start-simulation')">
      启动 7RP 方案推演
    </el-button>
  </section>
</template>

<style scoped>
.intro {
  font-size: 12px;
  color: var(--color-text-secondary);
  line-height: 1.7;
  margin-bottom: 16px;
}

.inject-btn {
  width: 100%;
  font-weight: 600;
  letter-spacing: 1px;
}

.alert-box {
  background: rgba(255, 71, 87, 0.08);
  border: 1px solid rgba(255, 71, 87, 0.4);
  border-radius: 6px;
  padding: 12px;
  margin-bottom: 16px;
}

.alert-msg {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-alert);
  margin-bottom: 4px;
}

.alert-sub {
  font-size: 12px;
  color: var(--color-text-secondary);
}
</style>
