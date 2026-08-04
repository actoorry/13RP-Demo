<template>
  <div>
    <div style="margin-bottom: 12px; color: var(--text-dim); font-size: 13px; line-height: 1.6">
      演示开始：系统实时监控全国港口与品类库存。<br />
      点击下方按钮模拟突发事件注入。
    </div>
    <el-button type="danger" size="large" style="width: 100%" @click="dialogVisible = true">
      ⚡ 模拟：台风封港
    </el-button>

    <el-dialog v-model="dialogVisible" title="注入突发事件" width="420px">
      <el-form label-width="90px">
        <el-form-item label="事件类型">
          <el-input :model-value="'台风\u2018海燕\u2019登陆华东'" disabled />
        </el-form-item>
        <el-form-item label="影响港口">
          <el-input :model-value="'宁波舟山港 / 上海港'" disabled />
        </el-form-item>
        <el-form-item label="持续时长">
          <el-radio-group v-model="duration">
            <el-radio-button :value="3">3 天</el-radio-button>
            <el-radio-button :value="5">5 天</el-radio-button>
            <el-radio-button :value="7">7 天</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirm">确认注入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const emit = defineEmits<{ (e: 'start', duration: number): void }>();
const dialogVisible = ref(false);
const duration = ref(5);

function confirm() {
  dialogVisible.value = false;
  emit('start', duration.value);
}
</script>
