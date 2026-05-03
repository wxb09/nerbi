<template>
  <MainNav />
  <main class="max-w-4xl mx-auto px-6 py-8">
    <div class="bg-white rounded-[2rem] border border-gray-100 shadow-sm overflow-hidden">
      <div class="flex items-center justify-between px-8 py-6 border-b border-gray-50">
        <h2 class="text-xl font-bold">聊天</h2>
      </div>

      <div v-if="loading" class="p-12 text-center">
        <span class="iconify text-4xl text-gray-300 animate-spin" data-icon="solar:refresh-bold"></span>
        <p class="text-gray-400 mt-4">加载中...</p>
      </div>

      <div v-else-if="conversations.length === 0" class="p-12 text-center">
        <span class="iconify text-6xl text-gray-200 mb-4" data-icon="solar:chat-round-dots-bold"></span>
        <p class="text-gray-400">暂无聊天记录</p>
        <p class="text-sm text-gray-300 mt-2">在物品详情页点击"联系TA"开始聊天</p>
      </div>

      <div v-else class="divide-y divide-gray-50">
        <div 
          v-for="conv in conversations" 
          :key="conv.id"
          @click="openChat(conv)"
          class="px-8 py-5 cursor-pointer transition-colors hover:bg-gray-50/50"
        >
          <div class="flex items-center gap-4">
            <div class="relative flex-shrink-0">
              <img 
                :src="getImageUrl(conv.otherUser.avatar)" 
                class="w-14 h-14 rounded-full object-cover border-2 border-gray-100"
              />
              <span 
                v-if="conv.otherUser.isOnline"
                class="absolute bottom-0 right-0 w-4 h-4 bg-green-500 border-2 border-white rounded-full"
              ></span>
            </div>
            
            <div class="flex-1 min-w-0">
              <div class="flex items-center justify-between mb-1">
                <h3 class="font-bold text-gray-800">{{ conv.otherUser.nickname }}</h3>
                <span class="text-xs text-gray-400">
                  {{ conv.lastMessage ? formatTime(conv.lastMessage.createdAt) : '' }}
                </span>
              </div>
              
              <div class="flex items-center justify-between">
                <p class="text-sm text-gray-500 truncate">
                  {{ conv.lastMessage ? conv.lastMessage.content : '暂无消息' }}
                </p>
                <span 
                  v-if="conv.unreadCount > 0"
                  class="ml-2 px-2 py-0.5 bg-red-500 text-white text-[10px] rounded-full font-bold"
                >
                  {{ conv.unreadCount > 99 ? '99+' : conv.unreadCount }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import MainNav from '../components/MainNav.vue'
import { chatApi, type Conversation } from '../api/chat'
import { wsManager } from '../utils/websocket'

const router = useRouter()

const conversations = ref<Conversation[]>([])
const loading = ref(true)

const loadConversations = async () => {
  loading.value = true
  try {
    const res = await chatApi.getConversations()
    conversations.value = res || []
  } catch (error) {
    console.error('加载会话列表失败', error)
  } finally {
    loading.value = false
  }
}

const openChat = (conv: Conversation) => {
  router.push(`/chat/conversation/${conv.id}?userId=${conv.otherUser.id}`)
}

const getImageUrl = (path: string | undefined) => {
  if (!path) return 'https://modao.cc/agent-py/media/generated_images/2026-03-19/7a697da7cb0e46808f265a42ae7934f3.jpg'
  if (path.startsWith('http')) return path
  return `http://localhost:8080${path}`
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

const handleWebSocketMessage = (message: any) => {
  if (message.type === 'CHAT_MESSAGE') {
    loadConversations()
  } else if (message.type === 'USER_ONLINE' || message.type === 'USER_OFFLINE') {
    loadConversations()
  }
}

onMounted(() => {
  loadConversations()
  wsManager.on('CHAT_MESSAGE', handleWebSocketMessage)
  wsManager.on('USER_ONLINE', handleWebSocketMessage)
  wsManager.on('USER_OFFLINE', handleWebSocketMessage)
})

onUnmounted(() => {
  wsManager.off('CHAT_MESSAGE', handleWebSocketMessage)
  wsManager.off('USER_ONLINE', handleWebSocketMessage)
  wsManager.off('USER_OFFLINE', handleWebSocketMessage)
})
</script>
