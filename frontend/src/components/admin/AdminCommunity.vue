<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-2xl font-bold text-[#3D3426]">公告管理</h2>
        <p class="text-sm text-[#B8AE9E] mt-1">管理社区公告内容</p>
      </div>
      <button
        class="px-5 py-2.5 bg-[#C9A227] text-white rounded-xl font-medium hover:bg-[#B8911F] transition-colors flex items-center gap-2"
        @click="openCreateModal"
      >
        <span class="iconify text-lg" data-icon="solar:add-circle-bold"></span>
        发布公告
      </button>
    </div>

    <div v-if="loading && announcements.length === 0" class="card p-8 text-center">
      <span class="iconify text-4xl text-[#B8AE9E] animate-spin inline-block" data-icon="solar:refresh-bold"></span>
      <p class="text-[#B8AE9E] mt-2">加载中...</p>
    </div>

    <div v-else-if="announcements.length === 0" class="card p-8 text-center">
      <span class="iconify text-6xl text-[#B8AE9E]" data-icon="solar:megaphone-bold"></span>
      <p class="text-lg text-[#B8AE9E] mt-4">暂无公告</p>
      <p class="text-sm text-[#B8AE9E]/70 mt-1">点击上方按钮发布第一条公告</p>
    </div>

    <div v-else class="space-y-4">
      <div
        v-for="announcement in announcements"
        :key="announcement.id"
        class="card p-6 hover:shadow-md transition-shadow"
      >
        <div class="flex items-start justify-between gap-4">
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2 mb-2">
              <span
                class="px-2 py-0.5 rounded-full text-xs font-medium"
                :class="getTypeClass(announcement.type)"
              >
                {{ getTypeText(announcement.type) }}
              </span>
              <span class="text-xs text-[#B8AE9E]">{{ announcement.createdAt }}</span>
            </div>
            <h3 class="font-bold text-[#3D3426] mb-2 truncate">{{ announcement.title }}</h3>
            <p class="text-sm text-[#8C7D66] line-clamp-2">{{ announcement.content }}</p>
          </div>
          <div class="flex items-center gap-2 flex-shrink-0">
            <button
              class="w-9 h-9 rounded-lg bg-[#FFF9EE] text-[#C9A227] hover:bg-[#C9A227] hover:text-white transition-colors flex items-center justify-center"
              @click="openEditModal(announcement)"
              title="编辑"
            >
              <span class="iconify text-lg" data-icon="solar:pen-bold"></span>
            </button>
            <button
              class="w-9 h-9 rounded-lg bg-red-50 text-red-500 hover:bg-red-500 hover:text-white transition-colors flex items-center justify-center"
              @click="confirmDelete(announcement)"
              title="删除"
            >
              <span class="iconify text-lg" data-icon="solar:trash-bin-trash-bold"></span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <Pagination
      v-if="totalPages > 1"
      :page="currentPage"
      :total-pages="totalPages"
      :total-elements="totalElements"
      @change="goToPage"
      class="mt-6"
    />

    <Teleport to="body">
      <Transition name="modal">
        <div
          v-if="showModal"
          class="fixed inset-0 z-50 flex items-center justify-center p-4"
          @click.self="closeModal"
        >
          <div class="absolute inset-0 bg-black/50 backdrop-blur-sm"></div>
          <div class="relative bg-white rounded-2xl w-full max-w-lg shadow-2xl">
            <div class="p-6 border-b border-[#E8D48B]/20">
              <h3 class="text-xl font-bold text-[#3D3426]">
                {{ editingId ? '编辑公告' : '发布公告' }}
              </h3>
            </div>
            
            <div class="p-6 space-y-4">
              <div>
                <label class="block text-sm font-medium text-[#3D3426] mb-1.5">公告标题</label>
                <input
                  v-model="form.title"
                  type="text"
                  class="w-full px-4 py-2.5 rounded-xl border border-[#E8D48B]/30 focus:border-[#C9A227] focus:ring-2 focus:ring-[#C9A227]/20 outline-none transition-all"
                  placeholder="请输入公告标题"
                />
              </div>
              
              <div>
                <label class="block text-sm font-medium text-[#3D3426] mb-1.5">公告类型</label>
                <select
                  v-model="form.type"
                  class="w-full px-4 py-2.5 rounded-xl border border-[#E8D48B]/30 focus:border-[#C9A227] focus:ring-2 focus:ring-[#C9A227]/20 outline-none transition-all"
                >
                  <option value="normal">普通公告</option>
                  <option value="important">重要公告</option>
                  <option value="urgent">紧急公告</option>
                </select>
              </div>
              
              <div>
                <label class="block text-sm font-medium text-[#3D3426] mb-1.5">公告内容</label>
                <textarea
                  v-model="form.content"
                  rows="5"
                  class="w-full px-4 py-2.5 rounded-xl border border-[#E8D48B]/30 focus:border-[#C9A227] focus:ring-2 focus:ring-[#C9A227]/20 outline-none transition-all resize-none"
                  placeholder="请输入公告内容"
                ></textarea>
              </div>
            </div>
            
            <div class="p-6 border-t border-[#E8D48B]/20 flex justify-end gap-3">
              <button
                class="px-5 py-2.5 rounded-xl border border-[#E8D48B]/30 text-[#8C7D66] hover:bg-[#FFF9EE] transition-colors"
                @click="closeModal"
              >
                取消
              </button>
              <button
                class="px-5 py-2.5 rounded-xl bg-[#C9A227] text-white font-medium hover:bg-[#B8911F] transition-colors disabled:opacity-50"
                :disabled="!form.title.trim() || !form.content.trim() || submitting"
                @click="submitForm"
              >
                {{ submitting ? '提交中...' : (editingId ? '保存' : '发布') }}
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <Teleport to="body">
      <Transition name="modal">
        <div
          v-if="showDeleteConfirm"
          class="fixed inset-0 z-50 flex items-center justify-center p-4"
        >
          <div class="absolute inset-0 bg-black/50 backdrop-blur-sm"></div>
          <div class="relative bg-white rounded-2xl w-full max-w-sm shadow-2xl p-6">
            <div class="text-center">
              <div class="w-16 h-16 rounded-full bg-red-100 flex items-center justify-center mx-auto mb-4">
                <span class="iconify text-3xl text-red-500" data-icon="solar:danger-triangle-bold"></span>
              </div>
              <h3 class="text-lg font-bold text-[#3D3426] mb-2">确认删除</h3>
              <p class="text-sm text-[#8C7D66] mb-6">确定要删除公告「{{ deletingAnnouncement?.title }}」吗？此操作不可撤销。</p>
              <div class="flex gap-3">
                <button
                  class="flex-1 px-4 py-2.5 rounded-xl border border-[#E8D48B]/30 text-[#8C7D66] hover:bg-[#FFF9EE] transition-colors"
                  @click="showDeleteConfirm = false"
                >
                  取消
                </button>
                <button
                  class="flex-1 px-4 py-2.5 rounded-xl bg-red-500 text-white font-medium hover:bg-red-600 transition-colors"
                  @click="deleteAnnouncement"
                >
                  删除
                </button>
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { adminApi, type Announcement } from '../../api/admin'
import Pagination from '../common/Pagination.vue'

const loading = ref(true)
const announcements = ref<Announcement[]>([])
const currentPage = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)

const showModal = ref(false)
const showDeleteConfirm = ref(false)
const editingId = ref<number | null>(null)
const submitting = ref(false)
const deletingAnnouncement = ref<Announcement | null>(null)

const form = ref({
  title: '',
  content: '',
  type: 'normal'
})

const loadAnnouncements = async (page = 0) => {
  try {
    loading.value = true
    const data = await adminApi.getAnnouncements({ page, size: 10 }) as any
    announcements.value = data.content || []
    currentPage.value = data.number || 0
    totalPages.value = data.totalPages || 1
    totalElements.value = data.totalElements || 0
  } catch (error) {
    console.error('加载公告失败', error)
  } finally {
    loading.value = false
  }
}

const goToPage = (page: number) => {
  loadAnnouncements(page)
}

const openCreateModal = () => {
  editingId.value = null
  form.value = { title: '', content: '', type: 'normal' }
  showModal.value = true
}

const openEditModal = (announcement: Announcement) => {
  editingId.value = announcement.id
  form.value = {
    title: announcement.title,
    content: announcement.content,
    type: announcement.type || 'normal'
  }
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  editingId.value = null
}

const submitForm = async () => {
  if (!form.value.title.trim() || !form.value.content.trim()) return
  
  try {
    submitting.value = true
    if (editingId.value) {
      await adminApi.updateAnnouncement(editingId.value, form.value)
    } else {
      await adminApi.createAnnouncement(form.value)
    }
    closeModal()
    loadAnnouncements(currentPage.value)
  } catch (error) {
    console.error('保存公告失败', error)
  } finally {
    submitting.value = false
  }
}

const confirmDelete = (announcement: Announcement) => {
  deletingAnnouncement.value = announcement
  showDeleteConfirm.value = true
}

const deleteAnnouncement = async () => {
  if (!deletingAnnouncement.value) return
  
  try {
    await adminApi.deleteAnnouncement(deletingAnnouncement.value.id)
    showDeleteConfirm.value = false
    deletingAnnouncement.value = null
    loadAnnouncements(currentPage.value)
  } catch (error) {
    console.error('删除公告失败', error)
  }
}

const getTypeClass = (type: string) => {
  const classes: Record<string, string> = {
    'normal': 'bg-gray-100 text-gray-600',
    'important': 'bg-blue-100 text-blue-600',
    'urgent': 'bg-red-100 text-red-600'
  }
  return classes[type] || 'bg-gray-100 text-gray-600'
}

const getTypeText = (type: string) => {
  const texts: Record<string, string> = {
    'normal': '普通',
    'important': '重要',
    'urgent': '紧急'
  }
  return texts[type] || '普通'
}

onMounted(() => {
  loadAnnouncements()
})
</script>

<style scoped>
.card {
  background: white;
  border-radius: 16px;
  border: 1px solid rgba(201, 162, 39, 0.08);
}

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
