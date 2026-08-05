<script setup lang="ts">
withDefaults(
  defineProps<{
    /** 是否显示"导出"按钮（默认显示） */
    showExport?: boolean
  }>(),
  { showExport: true },
)

const emit = defineEmits<{
  search: []
  reset: []
  export: []
}>()
</script>

<template>
  <el-card class="search-bar" shadow="never">
    <el-form inline class="search-form" @submit.prevent="emit('search')">
      <slot />
      <el-form-item class="search-actions">
        <el-button type="primary" @click="emit('search')">查询</el-button>
        <el-button @click="emit('reset')">重置</el-button>
        <el-button v-if="showExport" @click="emit('export')">导出</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<style scoped>
.search-bar {
  margin-bottom: 14px;
  background: var(--color-bg-panel);
  border-color: var(--color-border);
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
}

.search-actions {
  margin-right: 0;
}
</style>
