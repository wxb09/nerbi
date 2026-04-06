<template>
  <MainNav />
  <main class="max-w-4xl mx-auto px-6 py-8">
    <div class="bg-white rounded-[2rem] border border-gray-100 shadow-sm overflow-hidden">
      <div class="flex items-center justify-between px-8 py-6 border-b border-gray-50">
        <h2 class="text-xl font-bold">消息中心</h2>
        <button 
          v-if="unreadCount > 0"
          @click="markAllAsRead"
          class="text-sm text-[#E2B04D] hover:underline"
        >
          全部已读
        </button>
      </div>

      <div class="flex border-b border-gray-100">
        <button 
          @click="filter = 'all'"
          :class="[
            'px-6 py-4 text-sm font-bold transition-colors',
            filter === 'all' 
              ? 'text-[#E2B04D] border-b-2 border-[#E2B04D]' 
              : 'text-gray-400 hover:text-gray-600'
          ]"
        >
          全部消息
        </button>
        <button 
          @click="filter = 'unread'"
          :class="[
            'px-6 py-4 text-sm font-bold transition-colors relative',
            filter === 'unread' 
              ? 'text-[#E2B04D] border-b-2 border-[#E2B04D]' 
              : 'text-gray-400 hover:text-gray-600'
          ]"
        >
          未读消息
          <span 
            v-if="unreadCount > 0"
            class="ml-1 px-1.5 py-0.5 bg-red-500 text-white text-[10px] rounded-full"
          >
            {{ unreadCount > 99 ? '99+' : unreadCount }}
          </span>
        </button>
      </div>

      <div v-if="loading" class="p-12 text-center">
        <span class="iconify text-4xl text-gray-300 animate-spin" data-icon="solar:refresh-bold"></span>
        <p class="text-gray-400 mt-4">加载中...</p>
      </div>

      <div v-else-if="messages.length === 0" class="p-12 text-center">
        <span class="iconify text-6xl text-gray-200 mb-4" data-icon="solar:bell-bold"></span>
        <p class="text-gray-400">{{ filter === 'unread' ? '暂无未读消息' : '暂无消息' }}</p>
      </div>

      <div v-else class="divide-y divide-gray-50">
        <div 
          v-for="msg in messages" 
          :key="msg.id"
          @click="handleClick(msg)"
          :class="[
            'px-8 py-6 cursor-pointer transition-colors hover:bg-gray-50/50',
            !msg.isRead && 'bg-orange-50/30'
          ]"
        >
          <div class="flex items-start gap-4">
            <div 
              :class="[
                'w-10 h-10 rounded-full flex items-center justify-center flex-shrink-0',
                getTypeIconClass(msg.type)
              ]"
            >
              <span class="iconify text-lg text-white" :data-icon="getTypeIcon(msg.type)"></span>
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2 mb-1">
                <span class="text-xs px-2 py-0.5 rounded-full" :class="getTypeTagClass(msg.type)">
                  {{ msg.typeDesc }}
                </span>
                <span class="text-xs text-gray-400">{{ formatTime(msg.createdAt) }}</span>
                <span 
                  v-if="!msg.isRead"
                  class="w-2 h-2 bg-red-500 rounded-full"
                ></span>
              </div>
              <h4 class="font-bold text-gray-800 mb-1">{{ msg.title }}</h4>
              <p class="text-sm text-gray-500 line-clamp-2">{{ msg.content }}</p>
            </div>
          </div>
        </div>
      </div>

      <div v-if="totalPages > 1" class="p-6 border-t border-gray-50 flex items-center justify-center gap-2">
        <button 
          @click="page > 0 && loadMessages(page - 1)"
          :disabled="page === 0"
          class="px-3 py-1 text-sm border rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
        >
          上一页
        </button>
        <span class="text-sm text-gray-400">{{ page + 1 }} / {{ totalPages }}</span>
        <button 
          @click="page < totalPages - 1 && loadMessages(page + 1)"
          :disabled="page >= totalPages - 1"
          class="px-3 py-1 text-sm border rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
        >
          下一页
        </button>
      </div>
    </div>
  </main>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import MainNav from '../components/MainNav.vue'
import { messageApi, type Message } from '../api/message'
import { wsManager } from '../utils/websocket'

const router = useRouter()

const messages = ref<Message[]>([])
const loading = ref(true)
const page = ref(0)
const totalPages = ref(1)
const filter = ref('all')
const unreadCount = ref(0)

const loadMessages = async (p = 0) => {
  loading.value = true
  try {
    const res = filter.value === 'unread' 
      ? await messageApi.getUnreadMessages(p, 10)
      : await messageApi.getMessages(p, 10)
    
    messages.value = res.content || []
    totalPages.value = res.totalPages || 1
    page.value = p
  } catch (error) {
    console.error('加载消息失败', error)
  } finally {
    loading.value = false
  }
}

const loadUnreadCount = async () => {
  try {
    const res = await messageApi.getUnreadCount()
    unreadCount.value = res.count || 0
  } catch (error) {
    console.error('加载未读数量失败', error)
  }
}

const handleClick = async (msg: Message) => {
  if (!msg.isRead) {
    try {
      await messageApi.markAsRead(msg.id)
      msg.isRead = true
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (error) {
      console.error('标记已读失败', error)
    }
  }
  
  if (msg.relatedId) {
    router.push(`/profile`)
  }
}

const markAllAsRead = async () => {
  try {
    await messageApi.markAllAsRead()
    messages.value.forEach(m => m.isRead = true)
    unreadCount.value = 0
  } catch (error) {
    console.error('全部标记已读失败', error)
  }
}

const handleNewMessage = (message: any) => {
  if (message.type === 'NEW_MESSAGE') {
    const newMsg = message.data as Message
    if (filter.value === 'all') {
      messages.value.unshift(newMsg)
    } else if (!newMsg.isRead) {
      messages.value.unshift(newMsg)
    }
  } else if (message.type === 'UNREAD_COUNT') {
    unreadCount.value = message.data.count
  }
}

const getTypeIcon = (type: string) => {
  const icons: Record<string, string> = {
    'SYSTEM': 'solar:bell-bold',
    'BORROW_APPLY': 'solar:hand-shake-bold',
    'BORROW_APPROVED': 'solar:check-circle-bold',
    'BORROW_REJECTED': 'solar:close-circle-bold',
    'RETURN_DUE': 'solar:alarm-bold',
    'RETURN_OVERDUE': 'solar:danger-triangle-bold',
    'RETURN_CONFIRM': 'solar:box-bold'
  }
  return icons[type] || 'solar:bell-bold'
}

const getTypeIconClass = (type: string) => {
  const classes: Record<string, string> = {
    'SYSTEM': 'bg-gray-400',
    'BORROW_APPLY': 'bg-blue-500',
    'BORROW_APPROVED': 'bg-green-500',
    'BORROW_REJECTED': 'bg-red-500',
    'RETURN_DUE': 'bg-orange-500',
    'RETURN_OVERDUE': 'bg-red-600',
    'RETURN_CONFIRM': 'bg-purple-500'
  }
  return classes[type] || 'bg-gray-400'
}

const getTypeTagClass = (type: string) => {
  const classes: Record<string, string> = {
    'SYSTEM': 'bg-gray-100 text-gray-600',
    'BORROW_APPLY': 'bg-blue-100 text-blue-600',
    'BORROW_APPROVED': 'bg-green-100 text-green-600',
    'BORROW_REJECTED': 'bg-red-100 text-red-600',
    'RETURN_DUE': 'bg-orange-100 text-orange-600',
    'RETURN_OVERDUE': 'bg-red-100 text-red-600',
    'RETURN_CONFIRM': 'bg-purple-100 text-purple-600'
  }
  return classes[type] || 'bg-gray-100 text-gray-600'
}

const formatTime = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const hours = Math.floor(diff / (1000 * 60 * 60))
  
  if (hours < 1) return '刚刚'
  if (hours < 24) return `${hours}小时前`
  const days = Math.floor(hours / 24)
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString('zh-CN')
}

watch(filter, () => {
  loadMessages(0)
})

onMounted(() => {
  loadMessages()
  loadUnreadCount()
  
  wsManager.on('NEW_MESSAGE', handleNewMessage)
  wsManager.on('UNREAD_COUNT', handleNewMessage)
})

onUnmounted(() => {
  wsManager.off('NEW_MESSAGE', handleNewMessage)
  wsManager.off('UNREAD_COUNT', handleNewMessage)
})
</script>
