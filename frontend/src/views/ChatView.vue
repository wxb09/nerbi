<template>
  <MainNav />
  <main class="max-w-4xl mx-auto px-6 py-8">
    <div class="bg-white rounded-[2rem] border border-gray-100 shadow-sm overflow-hidden h-[calc(100vh-180px)] flex flex-col">
      <div class="flex items-center justify-between px-8 py-5 border-b border-gray-50">
        <div class="flex items-center gap-3">
          <button @click="goBack" class="p-1 hover:bg-gray-100 rounded-lg transition-colors">
            <span class="iconify text-xl text-gray-600" data-icon="solar:arrow-left-bold"></span>
          </button>
          <div class="flex items-center gap-3">
            <div class="relative">
              <img 
                :src="getImageUrl(otherUser?.avatar)" 
                class="w-10 h-10 rounded-full object-cover border-2 border-gray-100"
              />
              <span 
                v-if="otherUser?.isOnline"
                class="absolute bottom-0 right-0 w-3 h-3 bg-green-500 border-2 border-white rounded-full"
              ></span>
            </div>
            <div>
              <h3 class="font-bold text-gray-800">{{ otherUser?.nickname || '加载中...' }}</h3>
              <p class="text-xs text-gray-400">
                {{ otherUser?.isOnline ? '在线' : '离线' }}
              </p>
            </div>
          </div>
        </div>
      </div>

      <div 
        ref="messagesContainer"
        class="flex-1 overflow-y-auto px-8 py-6 space-y-4"
      >
        <div v-if="loadingMessages" class="text-center py-8">
          <span class="iconify text-3xl text-gray-300 animate-spin" data-icon="solar:refresh-bold"></span>
        </div>

        <div v-else-if="messages.length === 0" class="text-center py-12">
          <span class="iconify text-5xl text-gray-200 mb-3" data-icon="solar:chat-round-dots-bold"></span>
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
                'max-w-[70%] px-4 py-3 rounded-2xl',
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

      <div class="px-8 py-5 border-t border-gray-50">
        <div class="flex items-center gap-3">
          <input 
            v-model="newMessage"
            @keyup.enter="sendMessage"
            @input="handleTyping"
            class="flex-1 px-5 py-3 rounded-full border border-gray-200 focus:border-[#E2B04D] focus:outline-none transition-all"
            placeholder="输入消息..."
          />
          <button 
            @click="sendMessage"
            :disabled="!newMessage.trim() || sending"
            :class="[
              'px-6 py-3 rounded-full font-bold text-sm transition-all',
              newMessage.trim() && !sending
                ? 'bg-[#E2B04D] text-white hover:bg-[#d4a344]'
                : 'bg-gray-200 text-gray-400 cursor-not-allowed'
            ]"
          >
            <span v-if="sending" class="flex items-center gap-2">
              <span class="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></span>
            </span>
            <span v-else class="iconify text-lg" data-icon="solar:plain-bold"></span>
          </button>
        </div>
      </div>
    </div>
  </main>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import MainNav from '../components/MainNav.vue'
import { chatApi, type ChatMessage } from '../api/chat'
import { useAuthStore } from '../stores/auth'
import { wsManager } from '../utils/websocket'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const conversationId = ref(Number(route.params.id))
const otherUserId = ref(Number(route.query.userId))
const currentUserId = ref(Number(authStore.user?.id))

const messages = ref<ChatMessage[]>([])
const otherUser = ref<{ id: number; nickname: string; avatar: string; isOnline: boolean } | null>(null)
const newMessage = ref('')
const loadingMessages = ref(true)
const sending = ref(false)
const otherUserTyping = ref(false)
const messagesContainer = ref<HTMLElement | null>(null)

let typingTimer: number | null = null

const loadMessages = async () => {
  loadingMessages.value = true
  try {
    const res = await chatApi.getMessages(conversationId.value, 0, 100)
    messages.value = (res.content || []).reverse()
    
    await scrollToBottom()
  } catch (error) {
    console.error('加载消息失败', error)
  } finally {
    loadingMessages.value = false
  }
}

const loadOtherUserStatus = async () => {
  if (!otherUserId.value) return
  try {
    const res = await chatApi.getUserStatus(otherUserId.value)
    if (otherUser.value) {
      otherUser.value.isOnline = res.isOnline
    }
  } catch (error) {
    console.error('加载用户状态失败', error)
  }
}

const sendMessage = async () => {
  if (!newMessage.value.trim() || sending.value) return
  
  sending.value = true
  try {
    await chatApi.sendMessage(conversationId.value, newMessage.value.trim())
    newMessage.value = ''
    await loadMessages()
  } catch (error) {
    console.error('发送消息失败', error)
  } finally {
    sending.value = false
  }
}

const handleTyping = () => {
  if (typingTimer) {
    clearTimeout(typingTimer)
  }
  
  wsManager.send({
    type: 'TYPING',
    data: {
      receiverId: otherUserId.value,
      isTyping: true
    }
  })
  
  typingTimer = window.setTimeout(() => {
    wsManager.send({
      type: 'TYPING',
      data: {
        receiverId: otherUserId.value,
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

const goBack = () => {
  router.push('/chat')
}

const getImageUrl = (path: string | undefined) => {
  if (!path) return 'https://modao.cc/agent-py/media/generated_images/2026-03-19/7a697da7cb0e46808f265a42ae7934f3.jpg'
  if (path.startsWith('http')) return path
  return `http://localhost:8080${path}`
}

const formatTime = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${hours}:${minutes}`
}

const handleWebSocketMessage = (message: any) => {
  if (message.type === 'CHAT_MESSAGE') {
    const msg = message.data as ChatMessage
    if (msg.conversationId === conversationId.value) {
      messages.value.push(msg)
      scrollToBottom()
      
      chatApi.markAsRead(conversationId.value)
    }
  } else if (message.type === 'TYPING') {
    if (message.data.userId === otherUserId.value) {
      otherUserTyping.value = message.data.isTyping
      if (otherUserTyping.value) {
        scrollToBottom()
      }
    }
  } else if (message.type === 'USER_ONLINE') {
    if (message.data.userId === otherUserId.value && otherUser.value) {
      otherUser.value.isOnline = true
    }
  } else if (message.type === 'USER_OFFLINE') {
    if (message.data.userId === otherUserId.value && otherUser.value) {
      otherUser.value.isOnline = false
    }
  }
}

onMounted(async () => {
  await loadMessages()
  await loadOtherUserStatus()
  
  if (messages.value.length > 0) {
    const lastMsg = messages.value[messages.value.length - 1]
    otherUser.value = {
      id: lastMsg.senderId === currentUserId.value ? lastMsg.receiverId : lastMsg.senderId,
      nickname: '用户',
      avatar: '',
      isOnline: false
    }
  }
  
  wsManager.on('CHAT_MESSAGE', handleWebSocketMessage)
  wsManager.on('TYPING', handleWebSocketMessage)
  wsManager.on('USER_ONLINE', handleWebSocketMessage)
  wsManager.on('USER_OFFLINE', handleWebSocketMessage)
  
  chatApi.markAsRead(conversationId.value)
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

watch(() => route.params.id, (newId) => {
  if (newId) {
    conversationId.value = Number(newId)
    loadMessages()
  }
})
</script>
