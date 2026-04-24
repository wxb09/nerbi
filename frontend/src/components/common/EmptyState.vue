<template>
  <div class="flex flex-col items-center justify-center py-16 px-4">
    <div class="relative mb-6">
      <div class="w-24 h-24 bg-gray-50 rounded-full flex items-center justify-center">
        <span
          class="iconify text-5xl"
          :class="iconColor"
          :data-icon="icon"
        ></span>
      </div>
      <div v-if="showDecoration" class="absolute -top-1 -right-1 w-6 h-6 bg-[#E2B04D]/10 rounded-full"></div>
      <div v-if="showDecoration" class="absolute -bottom-2 -left-2 w-4 h-4 bg-[#E2B04D]/20 rounded-full"></div>
    </div>

    <h3 class="text-lg font-bold text-gray-700 mb-2">{{ title }}</h3>
    <p class="text-sm text-gray-400 text-center max-w-xs mb-6">{{ description }}</p>

    <slot>
      <RouterLink
        v-if="actionLink && actionText"
        :to="actionLink"
        class="inline-flex items-center justify-center px-6 py-3 bg-[#E2B04D] text-white rounded-2xl font-bold text-sm hover:opacity-90 transition-opacity"
      >
        {{ actionText }}
      </RouterLink>
    </slot>
  </div>
</template>

<script setup lang="ts">
interface Props {
  icon?: string
  iconColor?: string
  title?: string
  description?: string
  actionText?: string
  actionLink?: string
  actionIcon?: string
  showDecoration?: boolean
}

withDefaults(defineProps<Props>(), {
  icon: 'solar:box-bold',
  iconColor: 'text-gray-300',
  title: '暂无数据',
  description: '这里还没有内容',
  actionText: '',
  actionLink: '',
  actionIcon: 'solar:plus-circle-bold',
  showDecoration: true
})
</script>
