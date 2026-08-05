<script setup lang="ts">
export interface TableColumn {
  prop: string
  label: string
  width?: string
  minWidth?: string
  align?: 'left' | 'center' | 'right'
  /** 若指定，渲染同名作用域插槽（#slotName="{ row, index }"） */
  slot?: string
}

const props = withDefaults(
  defineProps<{
    columns: TableColumn[]
    data: unknown[]
    loading?: boolean
    total?: number
    page?: number
    size?: number
    rowKey?: string
    /**
     * 行样式类：字符串应用所有行，或函数按行返回（如"行标红"预警）。
     * 透传给 el-table 的 :row-class-name。
     */
    rowClassName?: string | ((data: { row: Record<string, unknown>; rowIndex: number }) => string)
  }>(),
  {
    loading: false,
    total: 0,
    page: 1,
    size: 10,
    rowKey: 'id',
  },
)

const emit = defineEmits<{
  'update:page': [number]
  'update:size': [number]
  'page-change': [number]
  'size-change': [number]
}>()
</script>

<template>
  <el-card class="data-table" shadow="never">
    <el-table
      v-loading="loading"
      :data="data"
      :row-key="rowKey"
      :row-class-name="props.rowClassName"
      class="data-table-body"
    >
      <el-table-column
        v-for="col in columns"
        :key="col.prop"
        :prop="col.prop"
        :label="col.label"
        :width="col.width"
        :min-width="col.minWidth"
        :align="col.align || 'left'"
      >
        <template v-if="col.slot" #default="scope">
          <slot :name="col.slot" :row="scope.row" :index="scope.$index" />
        </template>
      </el-table-column>
      <slot name="actions" />
      <template #empty>
        <el-empty description="暂无数据" :image-size="72" />
      </template>
    </el-table>

    <el-pagination
      v-if="total > 0"
      class="data-table-pagination"
      background
      layout="total, sizes, prev, pager, next, jumper"
      :total="total"
      :current-page="page"
      :page-size="size"
      :page-sizes="[10, 20, 50, 100]"
      @current-change="(p: number) => emit('page-change', p)"
      @size-change="(s: number) => emit('size-change', s)"
    />
  </el-card>
</template>

<style scoped>
.data-table {
  background: var(--color-bg-panel);
  border-color: var(--color-border);
}

.data-table-body {
  width: 100%;
}

.data-table-pagination {
  margin-top: 14px;
  justify-content: flex-end;
}
</style>
