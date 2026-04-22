<template>
  <article
    class="p-8 rounded-[2rem] post-card transition-all duration-300 cursor-pointer"
    :class="post.type === 'THANKS' ? 'bg-orange-50/50 border border-orange-100' : 'bg-white border border-gray-100 shadow-sm'"
    @click="$emit('click', post.id)"
  >
    <div class="flex items-center justify-between mb-6">
      <div class="flex items-center space-x-4">
        <img
          :src="post.author?.avatar || defaultAvatar"
          class="w-10 h-10 rounded-full border-2 border-white shadow-sm"
          :alt="post.author?.nickname"
        />
        <div>
          <h3 class="font-bold text-sm text-[#2D3436]">{{ post.author?.nickname || '匿名用户' }}</h3>
          <p class="text-[10px] text-gray-400 font-medium">
            {{ formatTime(post.createdAt) }}
            <span v-if="post.author?.building"> · {{ post.author.building }}</span>
            <span v-if="post.communityName"> · {{ post.communityName }}</span>
          </p>
        </div>
      </div>
      <span
        class="px-3 py-1 rounded-full text-[10px] font-bold uppercase tracking-widest shadow-sm"
        :class="getPostTypeClass(post.type)"
      >
        {{ getPostTypeText(post.type) }}
      </span>
    </div>

    <h4 v-if="post.title" class="font-bold text-lg mb-3 text-[#2D3436]">{{ post.title }}</h4>

    <p
      class="leading-relaxed mb-6"
      :class="post.type === 'THANKS' ? 'text-gray-700 font-medium' : 'text-gray-600'"
      v-html="formatContent(post.content)"
    ></p>

    <div v-if="post.images" class="flex gap-2 mb-4 overflow-x-auto">
      <img
        v-for="(img, idx) in parseImages(post.images)"
        :key="idx"
        :src="img"
        class="w-24 h-24 rounded-xl object-cover flex-shrink-0"
        alt="帖子图片"
      />
    </div>

    <div class="flex items-center justify-between">
      <div class="flex items-center space-x-6" :class="post.type === 'THANKS' ? 'text-gray-400' : 'text-gray-300'">
        <button
          @click.stop="$emit('like', post)"
          class="flex items-center space-x-1 hover:text-red-400 transition-colors group"
        >
          <span class="iconify text-xl transition-transform group-hover:scale-125" :data-icon="post.likedByMe ? 'solar:heart-bold' : 'solar:heart-linear'"></span>
          <span class="text-xs font-bold">{{ post.likeCount }}</span>
        </button>
        <button class="flex items-center space-x-1 hover:text-[#E2B04D] transition-colors group">
          <span class="iconify text-xl transition-transform group-hover:scale-125" data-icon="solar:chat-round-dots-bold"></span>
          <span class="text-xs font-bold">{{ post.commentCount }}</span>
        </button>
        <span class="flex items-center space-x-1 text-gray-300">
          <span class="iconify text-xl" data-icon="solar:eye-bold"></span>
          <span class="text-xs font-bold">{{ post.viewCount }}</span>
        </span>
      </div>
      <div v-if="post.tags" class="flex items-center gap-2">
        <span
          v-for="tag in parseTags(post.tags)"
          :key="tag"
          class="px-2 py-0.5 bg-gray-100 text-gray-500 rounded-full text-[10px] font-medium"
        >
          #{{ tag }}
        </span>
      </div>
    </div>
  </article>
</template>

<script setup lang="ts">
import type { PostList } from '../../api/forum'

defineProps<{
  post: PostList
  defaultAvatar: string
}>()

defineEmits<{
  (e: 'click', id: number): void
  (e: 'like', post: PostList): void
}>()

const getPostTypeClass = (type: string) => {
  const classes: Record<string, string> = {
    'THANKS': 'bg-white text-orange-600',
    'HELP': 'bg-blue-50 text-blue-600',
    'EXCHANGE': 'bg-green-50 text-green-600',
    'NORMAL': 'bg-gray-100 text-gray-600'
  }
  return classes[type] || 'bg-gray-100 text-gray-600'
}

const getPostTypeText = (type: string) => {
  const texts: Record<string, string> = {
    'THANKS': '邻里感谢信',
    'HELP': '物品求助',
    'EXCHANGE': '技能交换',
    'NORMAL': '社区动态'
  }
  return texts[type] || '社区动态'
}

const formatContent = (content: string) => {
  return content.replace(/@(\S+)/g, '<span class="text-[#E2B04D] font-bold">@$1</span>')
}

const formatTime = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}小时前`
  const days = Math.floor(hours / 24)
  if (days < 30) return `${days}天前`
  return date.toLocaleDateString()
}

const parseImages = (images: string | null) => {
  if (!images) return []
  return images.split(',').filter(s => s.trim())
}

const parseTags = (tags: string | null) => {
  if (!tags) return []
  return tags.split(',').filter(s => s.trim())
}
</script>

<style scoped>
.post-card {
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
.post-card:hover {
  transform: translateX(10px);
  background: white;
}
</style>
