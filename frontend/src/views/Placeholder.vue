<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const title = computed(() => (route.meta.title as string) || '该业务域')
</script>

<template>
  <div class="placeholder-page">
    <el-empty :description="`${title} · 批次 2 开发中`">
      <span class="placeholder-tip">本批次交付基础数据 / 组织权限 / 采购三大业务域，其余业务域在批次 2 实现</span>
    </el-empty>
  </div>
</template>

<style scoped>
.placeholder-page {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  min-height: 480px;
}

.placeholder-tip {
  font-size: 12px;
  color: var(--color-text-muted);
}
</style>
