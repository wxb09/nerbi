<template>
  <div class="flex items-center justify-between px-4 py-3 bg-white rounded-xl border border-[#E8D48B]/20">
    <div class="text-sm text-[#9A9082]">
      共 {{ totalElements }} 条记录，第 {{ page + 1 }} / {{ totalPages }} 页
    </div>

    <div class="flex items-center gap-2">
      <button
        class="px-3 py-1.5 rounded-lg text-sm font-medium border border-[#E8D48B]/30 text-[#9A9082] hover:border-[#C9A227] hover:text-[#C9A227] disabled:opacity-50 disabled:cursor-not-allowed transition-colors flex items-center gap-1 whitespace-nowrap"
        :disabled="page <= 0"
        @click="$emit('change', page - 1)"
      >
        <span class="iconify" data-icon="solar:alt-arrow-left-bold"></span>
        <span>上一页</span>
      </button>

      <template v-if="totalPages <= 7">
        <button
          v-for="p in totalPages"
          :key="p"
          class="w-8 h-8 rounded-lg text-sm font-medium transition-colors"
          :class="p - 1 === page ? 'bg-[#C9A227] text-white' : 'text-[#9A9082] hover:bg-[#FFF9EE]'"
          @click="$emit('change', p - 1)"
        >
          {{ p }}
        </button>
      </template>

      <template v-else>
        <button
          class="w-8 h-8 rounded-lg text-sm font-medium"
          :class="0 === page ? 'bg-[#C9A227] text-white' : 'text-[#9A9082] hover:bg-[#FFF9EE]'"
          @click="$emit('change', 0)"
        >
          1
        </button>

        <span v-if="page > 2" class="text-[#9A9082] px-1">...</span>

        <button
          v-for="p in visiblePages"
          :key="p"
          class="w-8 h-8 rounded-lg text-sm font-medium transition-colors"
          :class="p - 1 === page ? 'bg-[#C9A227] text-white' : 'text-[#9A9082] hover:bg-[#FFF9EE]'"
          @click="$emit('change', p - 1)"
        >
          {{ p }}
        </button>

        <span v-if="page < totalPages - 3" class="text-[#9A9082] px-1">...</span>

        <button
          class="w-8 h-8 rounded-lg text-sm font-medium"
          :class="totalPages - 1 === page ? 'bg-[#C9A227] text-white' : 'text-[#9A9082] hover:bg-[#FFF9EE]'"
          @click="$emit('change', totalPages - 1)"
        >
          {{ totalPages }}
        </button>
      </template>

      <button
        class="px-3 py-1.5 rounded-lg text-sm font-medium border border-[#E8D48B]/30 text-[#9A9082] hover:border-[#C9A227] hover:text-[#C9A227] disabled:opacity-50 disabled:cursor-not-allowed transition-colors flex items-center gap-1 whitespace-nowrap"
        :disabled="page >= totalPages - 1"
        @click="$emit('change', page + 1)"
      >
        <span>下一页</span>
        <span class="iconify" data-icon="solar:alt-arrow-right-bold"></span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  page: number
  totalPages: number
  totalElements: number
}

interface Emits {
  (e: 'change', page: number): void
}

const props = defineProps<Props>()
defineEmits<Emits>()

const visiblePages = computed(() => {
  const pages: number[] = []
  const start = Math.max(2, props.page - 1)
  const end = Math.min(props.totalPages - 1, props.page + 1)

  for (let i = start; i <= end; i++) {
    pages.push(i + 1)
  }

  return pages
})
</script>
