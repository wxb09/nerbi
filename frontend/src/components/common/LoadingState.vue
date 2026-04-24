<template>
  <div class="flex flex-col items-center justify-center py-16">
    <div class="relative">
      <div
        class="w-12 h-12 border-4 rounded-full animate-spin"
        :class="[
          color === 'primary' ? 'border-[#E2B04D] border-t-transparent' : 'border-gray-300 border-t-transparent'
        ]"
      ></div>
      <div
        v-if="showPulse"
        class="absolute inset-0 w-12 h-12 rounded-full animate-ping opacity-20"
        :class="color === 'primary' ? 'bg-[#E2B04D]' : 'bg-gray-400'"
      ></div>
    </div>
    <p v-if="text" class="mt-4 text-sm text-gray-400 animate-pulse">{{ text }}</p>
  </div>
</template>

<script setup lang="ts">
interface Props {
  text?: string
  color?: 'primary' | 'default'
  showPulse?: boolean
}

withDefaults(defineProps<Props>(), {
  text: '加载中...',
  color: 'primary',
  showPulse: false
})
</script>
