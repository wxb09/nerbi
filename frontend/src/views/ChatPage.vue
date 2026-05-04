<template>
  <MainNav />
  <main class="max-w-7xl mx-auto px-6 py-8">
    <div class="bg-white rounded-[2rem] border border-gray-100 shadow-sm overflow-hidden h-[calc(100vh-180px)] flex">
      
      <div class="w-80 border-r border-gray-100 flex flex-col">
        <div class="px-6 py-5 border-b border-gray-50">
          <h2 class="text-xl font-bold">聊天</h2>
        </div>

        <div v-if="loadingConversations" class="flex-1 flex items-center justify-center">
          <span class="iconify text-3xl text-gray-300 animate-spin" data-icon="solar:refresh-bold"></span>
        </div>

        <div v-else-if="conversations.length === 0" class="flex-1 flex flex-col items-center justify-center px-6">
          <span class="iconify text-5xl text-gray-200 mb-3" data-icon="solar:chat-round-dots-bold"></span>
          <p class="text-gray-400 text-sm text-center">暂无聊天记录</p>
          <p class="text-xs text-gray-300 mt-2 text-center">在物品详情页点击"联系TA"开始聊天</p>
        </div>

        <div v-else class="flex-1 overflow-y-auto">
          <div 
            v-for="conv in conversations" 
            :key="conv.id"
            @click="selectConversation(conv)"
            :class="[
              'px-6 py-4 cursor-pointer transition-colors border-l-4',
              selectedConversation?.id === conv.id 
                ? 'bg-[#FDF8EE] border-[#E2B04D]' 
                : 'border-transparent hover:bg-gray-50'
            ]"
          >
            <div class="flex items-center gap-3">
              <div class="relative flex-shrink-0">
                <img 
                  :src="getImageUrl(conv.otherUser.avatar)" 
                  class="w-12 h-12 rounded-full object-cover border-2 border-gray-100"
                />
                <span 
                  v-if="conv.otherUser.isOnline"
                  class="absolute bottom-0 right-0 w-3 h-3 bg-green-500 border-2 border-white rounded-full"
                ></span>
              </div>
              
              <div class="flex-1 min-w-0">
                <div class="flex items-center justify-between mb-1">
                  <h3 class="font-bold text-sm text-gray-800 truncate">{{ conv.otherUser.nickname }}</h3>
                  <span class="text-[10px] text-gray-400">
                    {{ conv.lastMessage ? formatTime(conv.lastMessage.createdAt) : '' }}
                  </span>
                </div>
                
                <div class="flex items-center justify-between">
                  <p class="text-xs text-gray-500 truncate">
                    {{ conv.lastMessage ? conv.lastMessage.content : '暂无消息' }}
                  </p>
                  <span 
                    v-if="conv.unreadCount > 0"
                    class="ml-2 px-1.5 py-0.5 bg-red-500 text-white text-[10px] rounded-full font-bold"
                  >
                    {{ conv.unreadCount > 99 ? '99+' : conv.unreadCount }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="flex-1 flex flex-col">
        <div v-if="!selectedConversation" class="flex-1 flex flex-col items-center justify-center">
          <span class="iconify text-6xl text-gray-200 mb-4" data-icon="solar:chat-round-dots-bold"></span>
          <p class="text-gray-400">选择一个会话开始聊天</p>
        </div>

        <template v-else>
          <div class="px-6 py-4 border-b border-gray-50">
            <div class="flex items-center gap-3">
              <div class="relative">
                <img 
                  :src="getImageUrl(selectedConversation.otherUser.avatar)" 
                  class="w-10 h-10 rounded-full object-cover border-2 border-gray-100"
                />
                <span 
                  v-if="selectedConversation.otherUser.isOnline"
                  class="absolute bottom-0 right-0 w-3 h-3 bg-green-500 border-2 border-white rounded-full"
                ></span>
              </div>
              <div>
                <h3 class="font-bold text-gray-800">{{ selectedConversation.otherUser.nickname }}</h3>
                <p class="text-xs text-gray-400">
                  {{ selectedConversation.otherUser.isOnline ? '在线' : '离线' }}
                </p>
              </div>
            </div>
          </div>

          <div 
            ref="messagesContainer"
            class="flex-1 overflow-y-auto px-6 py-4 space-y-3"
          >
            <div v-if="loadingMessages" class="text-center py-8">
              <span class="iconify text-3xl text-gray-300 animate-spin" data-icon="solar:refresh-bold"></span>
            </div>

            <div v-else-if="messages.length === 0" class="text-center py-12">
              <p class="text-gray-400 text-sm">暂无消息，发送第一条消息开始聊天吧</p>
            </div>

            <div v-else>
              <div 
                v-for="msg in messages" 
                :key="msg.id"
                :class="[
                  'flex',
                  msg.senderId === currentUserId ? 'justify-end' : 'justify-start'
                ]"
              >
                <div 
                  :class="[
                    'max-w-[60%] px-4 py-2.5 rounded-2xl',
                    msg.senderId === currentUserId 
                      ? 'bg-[#E2B04D] text-white rounded-br-sm' 
                      : 'bg-gray-100 text-gray-800 rounded-bl-sm'
                  ]"
                >
                  <p class="text-sm leading-relaxed">{{ msg.content }}</p>
                  <p 
                    :class="[
                      'text-[10px] mt-1',
                      msg.senderId === currentUserId ? 'text-white/70' : 'text-gray-400'
                    ]"
                  >
                    {{ formatTime(msg.createdAt) }}
                  </p>
                </div>
              </div>
            </div>

            <div v-if="otherUserTyping" class="flex justify-start">
              <div class="bg-gray-100 text-gray-400 px-4 py-2 rounded-2xl rounded-bl-sm text-sm">
                对方正在输入...
              </div>
            </div>
          </div>

          <div class="px-6 py-4 border-t border-gray-50">
            <div class="flex items-center gap-3">
              <input 
                v-model="newMessage"
                @keyup.enter="sendMessage"
                @input="handleTyping"
                class="flex-1 px-4 py-2.5 rounded-full border border-gray-200 focus:border-[#E2B04D] focus:outline-none transition-all text-sm"
                placeholder="输入消息..."
              />
              <button 
                @click="sendMessage"
                :disabled="!newMessage.trim() || sending"
                :class="[
                  'px-5 py-2.5 rounded-full font-bold text-sm transition-all',
                  newMessage.trim() && !sending
                    ? 'bg-[#E2B04D] text-white hover:bg-[#d4a344]'
                    : 'bg-gray-200 text-gray-400 cursor-not-allowed'
                ]"
              >
                <span v-if="sending" class="flex items-center">
                  <span class="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></span>
                </span>
                <span v-else class="iconify text-lg" data-icon="solar:plain-bold"></span>
              </button>
            </div>
          </div>
        </template>
      </div>
    </div>
  </main>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import MainNav from '../components/MainNav.vue'
import { chatApi, type Conversation, type ChatMessage } from '../api/chat'
import { useAuthStore } from '../stores/auth'
import { wsManager } from '../utils/websocket'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const currentUserId = ref(Number(authStore.user?.id))
const conversations = ref<Conversation[]>([])
const selectedConversation = ref<Conversation | null>(null)
const messages = ref<ChatMessage[]>([])
const newMessage = ref('')
const loadingConversations = ref(true)
const loadingMessages = ref(false)
const sending = ref(false)
const otherUserTyping = ref(false)
const messagesContainer = ref<HTMLElement | null>(null)

let typingTimer: number | null = null

const loadConversations = async () => {
  loadingConversations.value = true
  try {
    const res = await chatApi.getConversations()
    conversations.value = res || []
    
    const conversationId = route.query.conversationId
    if (conversationId && !selectedConversation.value) {
      const conv = conversations.value.find(c => c.id === Number(conversationId))
      if (conv) {
        selectConversation(conv)
      }
    }
  } catch (error) {
    console.error('加载会话列表失败', error)
  } finally {
    loadingConversations.value = false
  }
}

const selectConversation = async (conv: Conversation) => {
  selectedConversation.value = conv
  messages.value = []
  
  router.replace({ query: { conversationId: conv.id.toString() } })
  
  await loadMessages()
  
  chatApi.markAsRead(conv.id)
  conv.unreadCount = 0
}

const loadMessages = async () => {
  if (!selectedConversation.value) return
  
  loadingMessages.value = true
  try {
    const res = await chatApi.getMessages(selectedConversation.value.id, 0, 100)
    messages.value = (res.content || []).reverse()
  } catch (error) {
    console.error('加载消息失败', error)
  } finally {
    loadingMessages.value = false
  }
  
  await scrollToBottom()
}

const sendMessage = async () => {
  if (!newMessage.value.trim() || sending.value || !selectedConversation.value) return
  
  sending.value = true
  try {
    await chatApi.sendMessage(selectedConversation.value.id, newMessage.value.trim())
    newMessage.value = ''
    await loadMessages()
    await loadConversations()
  } catch (error) {
    console.error('发送消息失败', error)
  } finally {
    sending.value = false
  }
}

const handleTyping = () => {
  if (!selectedConversation.value) return
  
  if (typingTimer) {
    clearTimeout(typingTimer)
  }
  
  wsManager.send({
    type: 'TYPING',
    data: {
      receiverId: selectedConversation.value.otherUser.id,
      isTyping: true
    }
  })
  
  typingTimer = window.setTimeout(() => {
    wsManager.send({
      type: 'TYPING',
      data: {
        receiverId: selectedConversation.value.otherUser.id,
        isTyping: false
      }
    })
  }, 2000)
}

const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
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
  
  const h = date.getHours().toString().padStart(2, '0')
  const m = date.getMinutes().toString().padStart(2, '0')
  return `${h}:${m}`
}

const handleWebSocketMessage = (message: any) => {
  if (message.type === 'CHAT_MESSAGE') {
    const msg = message.data as ChatMessage
    
    loadConversations()
    
    if (selectedConversation.value && msg.conversationId === selectedConversation.value.id) {
      messages.value.push(msg)
      scrollToBottom()
      chatApi.markAsRead(selectedConversation.value.id)
    }
  } else if (message.type === 'TYPING') {
    if (selectedConversation.value && message.data.userId === selectedConversation.value.otherUser.id) {
      otherUserTyping.value = message.data.isTyping
      if (otherUserTyping.value) {
        scrollToBottom()
      }
    }
  } else if (message.type === 'USER_ONLINE') {
    const conv = conversations.value.find(c => c.otherUser.id === message.data.userId)
    if (conv) {
      conv.otherUser.isOnline = true
    }
    if (selectedConversation.value && selectedConversation.value.otherUser.id === message.data.userId) {
      selectedConversation.value.otherUser.isOnline = true
    }
  } else if (message.type === 'USER_OFFLINE') {
    const conv = conversations.value.find(c => c.otherUser.id === message.data.userId)
    if (conv) {
      conv.otherUser.isOnline = false
    }
    if (selectedConversation.value && selectedConversation.value.otherUser.id === message.data.userId) {
      selectedConversation.value.otherUser.isOnline = false
    }
  }
}

onMounted(() => {
  loadConversations()
  
  wsManager.on('CHAT_MESSAGE', handleWebSocketMessage)
  wsManager.on('TYPING', handleWebSocketMessage)
  wsManager.on('USER_ONLINE', handleWebSocketMessage)
  wsManager.on('USER_OFFLINE', handleWebSocketMessage)
})

onUnmounted(() => {
  if (typingTimer) {
    clearTimeout(typingTimer)
  }
  
  wsManager.off('CHAT_MESSAGE', handleWebSocketMessage)
  wsManager.off('TYPING', handleWebSocketMessage)
  wsManager.off('USER_ONLINE', handleWebSocketMessage)
  wsManager.off('USER_OFFLINE', handleWebSocketMessage)
})

watch(() => route.query.conversationId, (newId) => {
  if (newId && !selectedConversation.value) {
    const conv = conversations.value.find(c => c.id === Number(newId))
    if (conv) {
      selectConversation(conv)
    }
  }
})
</script>
