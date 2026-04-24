<template>
  <div :class="gridClass">
    <SkeletonLoader
      v-for="i in count"
      :key="i"
      :type="type"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import SkeletonLoader from './SkeletonLoader.vue'

interface Props {
  type?: 'card' | 'post' | 'table'
  count?: number
  cols?: number
  smCols?: number
  mdCols?: number
  lgCols?: number
}

const props = withDefaults(defineProps<Props>(), {
  type: 'card',
  count: 4,
  cols: 1,
  smCols: 2,
  mdCols: 3,
  lgCols: 4
})

const gridClass = computed(() => {
  return `grid grid-cols-${props.cols} sm:grid-cols-${props.smCols} md:grid-cols-${props.mdCols} lg:grid-cols-${props.lgCols} gap-6`
})
</script>
