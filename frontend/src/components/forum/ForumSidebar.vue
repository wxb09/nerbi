<template>
  <aside class="space-y-6">
    <div class="bg-gradient-to-br from-[#2D3436] to-[#3d4648] text-white p-6 rounded-3xl shadow-lg">
      <div class="flex items-center gap-2 mb-4">
        <span class="iconify text-xl text-[#E2B04D]" data-icon="solar:megaphone-bold"></span>
        <h3 class="font-bold text-lg">社区公告</h3>
      </div>
      <p class="text-sm text-gray-300 leading-relaxed">
        邻里借阅规则 2026 版更新。请大家遵守规则，共建和谐社区！
      </p>
      <div class="mt-4 pt-4 border-t border-gray-700">
        <p class="text-xs text-gray-400">发布于 2026-03-28</p>
      </div>
    </div>

    <div class="bg-white p-6 rounded-3xl border border-gray-100 shadow-sm">
      <div class="flex items-center gap-2 mb-4">
        <span class="iconify text-xl text-[#E2B04D]" data-icon="solar:hashtag-bold"></span>
        <h3 class="font-bold text-lg text-[#2D3436]">热门话题</h3>
      </div>
      <div class="flex flex-wrap gap-2">
        <span
          v-for="tag in hotTags"
          :key="tag"
          class="px-4 py-2 bg-[#F5E6C8] text-[#E2B04D] rounded-full text-sm font-medium hover:bg-[#E2B04D] hover:text-white transition-colors cursor-pointer"
          @click="$emit('searchTag', tag)"
        >
          #{{ tag }}
        </span>
      </div>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const hotTags = ref([
  '邻里互助',
  '绿色生活',
  '周末活动',
  '闲置共享',
  '社区新闻'
])

defineEmits<{
  (e: 'searchTag', tag: string): void
}>()
</script>
