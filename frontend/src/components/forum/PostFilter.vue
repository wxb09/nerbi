<template>
  <div class="flex gap-2">
    <button
      class="px-4 py-2 rounded-full text-xs font-bold transition-all"
      :class="activeTab === 'latest' ? 'bg-[#E2B04D] text-white shadow-lg shadow-[#E2B04D]/20' : 'bg-white border border-gray-200 hover:bg-gray-50'"
      @click="$emit('switchTab', 'latest')"
    >
      最新
    </button>
    <button
      class="px-4 py-2 rounded-full text-xs font-bold transition-all"
      :class="activeTab === 'hot' ? 'bg-[#E2B04D] text-white shadow-lg shadow-[#E2B04D]/20' : 'bg-white border border-gray-200 hover:bg-gray-50'"
      @click="$emit('switchTab', 'hot')"
    >
      最热
    </button>
    <button
      class="px-4 py-2 rounded-full text-xs font-bold transition-all"
      :class="activeType === 'THANKS' ? 'bg-orange-400 text-white shadow-lg' : 'bg-white border border-gray-200 hover:bg-gray-50'"
      @click="$emit('toggleType', 'THANKS')"
    >
      感谢信
    </button>
    <button
      class="px-4 py-2 rounded-full text-xs font-bold transition-all"
      :class="activeType === 'HELP' ? 'bg-blue-400 text-white shadow-lg' : 'bg-white border border-gray-200 hover:bg-gray-50'"
      @click="$emit('toggleType', 'HELP')"
    >
      求助
    </button>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  activeTab: string
  activeType: string | null
}>()

defineEmits<{
  (e: 'switchTab', tab: string): void
  (e: 'toggleType', type: string): void
}>()
</script>
