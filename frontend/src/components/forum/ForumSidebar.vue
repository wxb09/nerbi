<template>
  <aside class="space-y-6">
    <div class="bg-gradient-to-br from-[#2D3436] to-[#3d4648] text-white p-6 rounded-3xl shadow-lg">
      <div class="flex items-center gap-2 mb-4">
        <span class="iconify text-xl text-[#E2B04D]" data-icon="solar:megaphone-bold"></span>
        <h3 class="font-bold text-lg">社区公告</h3>
      </div>
      
      <div v-if="loading" class="text-sm text-gray-400 py-4 text-center">
        加载中...
      </div>
      
      <div v-else-if="announcements.length === 0" class="text-sm text-gray-400 py-4 text-center">
        暂无公告
      </div>
      
      <div v-else class="space-y-3">
        <div
          v-for="announcement in announcements"
          :key="announcement.id"
          class="cursor-pointer group"
          @click="showAnnouncementDetail(announcement)"
        >
          <p class="text-sm text-gray-300 leading-relaxed group-hover:text-white transition-colors line-clamp-2">
            {{ announcement.title }}
          </p>
          <p class="text-xs text-gray-500 mt-1">{{ announcement.createdAt }}</p>
        </div>
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

    <Teleport to="body">
      <Transition name="modal">
        <div
          v-if="showModal"
          class="fixed inset-0 z-50 flex items-center justify-center p-4"
          @click.self="closeModal"
        >
          <div class="absolute inset-0 bg-black/50 backdrop-blur-sm"></div>
          <div class="relative bg-white rounded-3xl max-w-lg w-full max-h-[80vh] overflow-hidden shadow-2xl">
            <div class="bg-gradient-to-br from-[#2D3436] to-[#3d4648] text-white p-6">
              <div class="flex items-center justify-between">
                <div class="flex items-center gap-2">
                  <span class="iconify text-xl text-[#E2B04D]" data-icon="solar:megaphone-bold"></span>
                  <h3 class="font-bold text-lg">社区公告</h3>
                </div>
                <button
                  class="w-8 h-8 rounded-full bg-white/10 hover:bg-white/20 flex items-center justify-center transition-colors"
                  @click="closeModal"
                >
                  <span class="iconify text-lg" data-icon="solar:close-circle-bold"></span>
                </button>
              </div>
            </div>
            
            <div class="p-6 overflow-y-auto max-h-[calc(80vh-100px)]">
              <h4 class="text-xl font-bold text-[#2D3436] mb-2">{{ selectedAnnouncement?.title }}</h4>
              <p class="text-sm text-gray-400 mb-4">{{ selectedAnnouncement?.createdAt }}</p>
              <div class="text-gray-600 leading-relaxed whitespace-pre-wrap">
                {{ selectedAnnouncement?.content }}
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </aside>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { forumApi, type Announcement } from '../../api/forum'

const hotTags = ref([
  '邻里互助',
  '绿色生活',
  '周末活动',
  '闲置共享',
  '社区新闻'
])

const announcements = ref<Announcement[]>([])
const loading = ref(true)
const showModal = ref(false)
const selectedAnnouncement = ref<Announcement | null>(null)

defineEmits<{
  (e: 'searchTag', tag: string): void
}>()

const loadAnnouncements = async () => {
  try {
    loading.value = true
    const data = await forumApi.getAnnouncements() as Announcement[]
    announcements.value = data
  } catch (error) {
    console.error('加载公告失败', error)
  } finally {
    loading.value = false
  }
}

const showAnnouncementDetail = (announcement: Announcement) => {
  selectedAnnouncement.value = announcement
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  selectedAnnouncement.value = null
}

onMounted(() => {
  loadAnnouncements()
})
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
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
