<template>
  <div class="animate-pulse">
    <div v-if="type === 'card'" class="bg-white rounded-3xl border-2 border-gray-100 overflow-hidden">
      <div class="h-44 bg-gray-200"></div>
      <div class="p-5 space-y-3">
        <div class="h-5 bg-gray-200 rounded w-3/4"></div>
        <div class="h-4 bg-gray-200 rounded w-1/2"></div>
        <div class="flex gap-1">
          <div class="h-6 bg-gray-200 rounded-full w-16"></div>
          <div class="h-6 bg-gray-200 rounded-full w-16"></div>
        </div>
        <div class="flex justify-between items-center pt-2">
          <div class="h-5 bg-gray-200 rounded w-20"></div>
          <div class="h-4 bg-gray-200 rounded w-16"></div>
        </div>
      </div>
    </div>

    <div v-else-if="type === 'post'" class="bg-white rounded-2xl border border-gray-100 p-5 space-y-4">
      <div class="flex items-center space-x-3">
        <div class="w-10 h-10 bg-gray-200 rounded-full"></div>
        <div class="space-y-2 flex-1">
          <div class="h-4 bg-gray-200 rounded w-24"></div>
          <div class="h-3 bg-gray-200 rounded w-16"></div>
        </div>
      </div>
      <div class="space-y-2">
        <div class="h-4 bg-gray-200 rounded w-full"></div>
        <div class="h-4 bg-gray-200 rounded w-5/6"></div>
        <div class="h-4 bg-gray-200 rounded w-4/6"></div>
      </div>
      <div class="flex gap-4 pt-2">
        <div class="h-8 bg-gray-200 rounded-lg w-20"></div>
        <div class="h-8 bg-gray-200 rounded-lg w-20"></div>
        <div class="h-8 bg-gray-200 rounded-lg w-20"></div>
      </div>
    </div>

    <div v-else-if="type === 'table'" class="space-y-3">
      <div v-for="i in count" :key="i" class="flex items-center space-x-4 py-4">
        <div class="h-12 bg-gray-200 rounded-xl w-12"></div>
        <div class="space-y-2 flex-1">
          <div class="h-4 bg-gray-200 rounded w-1/3"></div>
          <div class="h-3 bg-gray-200 rounded w-1/4"></div>
        </div>
        <div class="h-8 bg-gray-200 rounded-lg w-20"></div>
      </div>
    </div>

    <div v-else-if="type === 'text'" class="space-y-3">
      <div v-for="i in count" :key="i" class="h-4 bg-gray-200 rounded" :class="i % 3 === 0 ? 'w-2/3' : 'w-full'"></div>
    </div>

    <div v-else-if="type === 'image'" class="bg-gray-200 rounded-2xl" :style="{ height: height + 'px' }"></div>

    <div v-else class="bg-gray-200 rounded-xl" :style="{ width: width, height: height }"></div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  type?: 'card' | 'post' | 'table' | 'text' | 'image' | 'default'
  count?: number
  width?: string
  height?: string | number
}

withDefaults(defineProps<Props>(), {
  type: 'default',
  count: 1,
  width: '100%',
  height: '100%'
})
</script>
