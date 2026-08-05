<script setup lang="ts">
withDefaults(
  defineProps<{
    modelValue: boolean
    title?: string
    message?: string
    loading?: boolean
  }>(),
  {
    title: '确认操作',
    message: '',
    loading: false,
  },
)

const emit = defineEmits<{
  'update:modelValue': [boolean]
  confirm: []
  cancel: []
}>()
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    :title="title"
    width="420px"
    :close-on-click-modal="false"
    @update:model-value="(v: boolean) => emit('update:modelValue', v)"
    @close="emit('cancel')"
  >
    <div class="confirm-message">{{ message }}</div>
    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button type="danger" :loading="loading" @click="emit('confirm')">确定</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.confirm-message {
  font-size: 14px;
  color: var(--color-text);
  line-height: 1.7;
  word-break: break-all;
}
</style>
