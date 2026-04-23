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
          <h3 class="font-bold text-sm text-[#3D3426]">{{ post.author?.nickname || '匿名用户' }}</h3>
          <p class="text-[10px] text-gray-400 font-medium">
            {{ formatTime(post.createdAt) }}
            <span v-if="post.author?.building"> · {{ post.author.building }}</span>
            <span v-if="post.communityName"> · {{ post.communityName }}</span>
          </p>
        </div>
      </div>
      <div class="flex items-center gap-2">
        <span
          class="px-3 py-1 rounded-full text-[10px] font-bold uppercase tracking-widest shadow-sm"
          :class="getPostTypeClass(post.type)"
        >
          {{ getPostTypeText(post.type) }}
        </span>
      </div>
    </div>

    <h4 v-if="post.title" class="font-bold text-lg mb-3 text-[#3D3426]">{{ post.title }}</h4>

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
      <div class="flex items-center gap-3">
        <div v-if="post.tags" class="flex items-center gap-2">
          <span
            v-for="tag in parseTags(post.tags)"
            :key="tag"
            class="px-2 py-0.5 bg-gray-100 text-gray-500 rounded-full text-[10px] font-medium"
          >
            #{{ tag }}
          </span>
        </div>
        <div class="relative">
          <button
            class="w-8 h-8 rounded-full hover:bg-gray-100 flex items-center justify-center transition-colors"
            @click.stop="toggleMenu"
          >
            <span class="iconify text-lg text-gray-400" data-icon="solar:menu-dots-bold"></span>
          </button>
          <Transition name="menu">
            <div
              v-if="showMenu"
              class="absolute right-0 top-full mt-1 bg-white rounded-xl shadow-lg border border-gray-100 py-1 w-32 z-10"
            >
              <button
                v-if="isMyPost"
                class="w-full px-4 py-2 text-left text-sm text-red-500 hover:bg-red-50 transition-colors flex items-center gap-2"
                @click.stop="handleDelete"
              >
                <span class="iconify" data-icon="solar:trash-bin-trash-bold"></span>
                删除
              </button>
              <button
                v-if="!isMyPost"
                class="w-full px-4 py-2 text-left text-sm text-gray-600 hover:bg-gray-50 transition-colors flex items-center gap-2"
                @click.stop="handleReport"
              >
                <span class="iconify" data-icon="solar:danger-triangle-bold"></span>
                举报
              </button>
            </div>
          </Transition>
        </div>
      </div>
    </div>

    <Teleport to="body">
      <Transition name="modal">
        <div
          v-if="showReportModal"
          class="fixed inset-0 z-50 flex items-center justify-center p-4"
          @click.self="closeReportModal"
        >
          <div class="absolute inset-0 bg-black/50 backdrop-blur-sm"></div>
          <div class="relative bg-white rounded-2xl w-full max-w-sm shadow-2xl p-6">
            <h3 class="text-lg font-bold text-[#3D3426] mb-4">举报帖子</h3>
            <textarea
              v-model="reportReason"
              rows="3"
              class="w-full px-4 py-2.5 rounded-xl border border-gray-200 focus:border-[#C9A227] focus:ring-2 focus:ring-[#C9A227]/20 outline-none transition-all resize-none text-sm"
              placeholder="请输入举报原因..."
            ></textarea>
            <div class="flex gap-3 mt-4">
              <button
                class="flex-1 px-4 py-2.5 rounded-xl border border-gray-200 text-gray-600 hover:bg-gray-50 transition-colors text-sm"
                @click="closeReportModal"
              >
                取消
              </button>
              <button
                class="flex-1 px-4 py-2.5 rounded-xl bg-[#C9A227] text-white font-medium hover:bg-[#B8911F] transition-colors text-sm disabled:opacity-50"
                :disabled="!reportReason.trim() || reporting"
                @click="submitReport"
              >
                {{ reporting ? '提交中...' : '举报' }}
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </article>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import type { PostList } from '../../api/forum'
import { forumApi } from '../../api/forum'
import { useAuthStore } from '../../stores/auth'

const props = defineProps<{
  post: PostList
  defaultAvatar: string
}>()

const emit = defineEmits<{
  (e: 'click', id: number): void
  (e: 'like', post: PostList): void
  (e: 'delete', post: PostList): void
}>()

const authStore = useAuthStore()

const showMenu = ref(false)
const showReportModal = ref(false)
const reportReason = ref('')
const reporting = ref(false)

const isMyPost = computed(() => {
  return authStore.user?.id && props.post.author?.id && Number(authStore.user.id) === props.post.author.id
})

const toggleMenu = () => {
  showMenu.value = !showMenu.value
}

const handleDelete = () => {
  showMenu.value = false
  if (confirm('确定要删除这条帖子吗？')) {
    emit('delete', props.post)
  }
}

const handleReport = () => {
  showMenu.value = false
  showReportModal.value = true
}

const closeReportModal = () => {
  showReportModal.value = false
  reportReason.value = ''
}

const submitReport = async () => {
  if (!reportReason.value.trim()) return
  try {
    reporting.value = true
    await forumApi.reportContent({
      targetType: 'POST',
      targetId: props.post.id,
      reason: reportReason.value.trim()
    })
    alert('举报已提交，管理员会尽快处理')
    closeReportModal()
  } catch (error: any) {
    alert(error.message || '举报失败')
  } finally {
    reporting.value = false
  }
}

const handleClickOutside = (event: MouseEvent) => {
  showMenu.value = false
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})

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

.menu-enter-active,
.menu-leave-active {
  transition: all 0.2s ease;
}

.menu-enter-from,
.menu-leave-to {
  opacity: 0;
  transform: translateY(-5px);
}

.modal-enter-active,
.modal-leave-active {
  transition: all 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from > div:last-child,
.modal-leave-to > div:last-child {
  transform: scale(0.95);
}
</style>
