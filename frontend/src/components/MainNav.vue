<template>
  <nav class="sticky top-0 z-50 bg-white border-b border-gray-100 px-6 py-4">   
    <div class="max-w-7xl mx-auto flex justify-between items-center">
      <RouterLink class="flex items-center space-x-2" to="/index">
        <span class="iconify text-3xl text-[#E2B04D]" data-icon="solar:share-circle-bold-duotone"></span>
        <span class="text-2xl font-bold tracking-tight text-[#2D3436]">邻里共享<span class="text-[#E2B04D]">.</span></span>
      </RouterLink>
      <div class="hidden md:flex space-x-8 font-medium">
        <RouterLink class="hover:text-[#E2B04D]" to="/index">发现物品</RouterLink>
        <RouterLink class="hover:text-[#E2B04D]" to="/forum">社区论坛</RouterLink>
        <RouterLink class="hover:text-[#E2B04D]" to="/publish">发布闲置</RouterLink>
      </div>
      <div class="flex items-center gap-4">
        <div v-if="authStore.isLoggedIn" class="flex items-center gap-2">
          <span class="text-sm">{{ authStore.user?.nickname }}</span>
          <RouterLink 
            class="relative p-2 hover:bg-gray-100 rounded-full transition-colors" 
            to="/messages"
          >
            <span class="iconify text-xl text-gray-600" data-icon="solar:bell-bold"></span>
            <span 
              v-if="unreadCount > 0"
              class="absolute -top-0.5 -right-0.5 bg-red-500 text-white text-[10px] w-4 h-4 rounded-full flex items-center justify-center font-bold"
            >
              {{ unreadCount > 9 ? '9+' : unreadCount }}
            </span>
          </RouterLink>
          <RouterLink 
            class="relative p-2 hover:bg-gray-100 rounded-full transition-colors" 
            to="/chat"
          >
            <span class="iconify text-xl text-gray-600" data-icon="solar:chat-round-dots-bold"></span>
            <span 
              v-if="chatUnreadCount > 0"
              class="absolute -top-0.5 -right-0.5 bg-red-500 text-white text-[10px] w-4 h-4 rounded-full flex items-center justify-center font-bold"
            >
              {{ chatUnreadCount > 9 ? '9+' : chatUnreadCount }}
            </span>
          </RouterLink>
          <RouterLink 
            class="text-sm px-3 py-1.5 rounded-full border border-gray-200 relative" 
            to="/profile"
          >
            我的
            <span 
              v-if="pendingTotal > 0"
              class="absolute -top-1 -right-1 bg-red-500 text-white text-xs w-5 h-5 rounded-full flex items-center justify-center"
            >
              {{ pendingTotal > 9 ? '9+' : pendingTotal }}
            </span>
          </RouterLink>
          <button class="text-sm bg-[#2D3436] text-white px-4 py-2 rounded-xl" @click="logout">
            退出
          </button>
        </div>
        <div v-else>
          <RouterLink class="text-sm px-3 py-1.5 rounded-full border border-gray-200" to="/profile">
            我的
          </RouterLink>
          <RouterLink class="text-sm bg-[#E2B04D] text-white px-4 py-2 rounded-xl" to="/login">
            登录
          </RouterLink>
        </div>
      </div>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { userApi } from '../api/user'
import { messageApi } from '../api/message'
import { chatApi } from '../api/chat'
import { wsManager } from '../utils/websocket'

const router = useRouter()
const authStore = useAuthStore()

const pendingApprovalCount = ref(0)
const returnRequestedCount = ref(0)
const unreadCount = ref(0)
const chatUnreadCount = ref(0)

const pendingTotal = computed(() => pendingApprovalCount.value + returnRequestedCount.value)

const loadPendingCount = async () => {
  if (!authStore.isLoggedIn) return
  try {
    const res = await userApi.getUserStats()
    pendingApprovalCount.value = res.pendingApprovalCount || 0
    returnRequestedCount.value = res.returnRequestedCount || 0
  } catch (error) {
    console.error('加载待处理数量失败', error)
  }
}

const loadUnreadCount = async () => {
  if (!authStore.isLoggedIn) return
  try {
    const res = await messageApi.getUnreadCount()
    unreadCount.value = res.count || 0
  } catch (error) {
    console.error('加载未读消息数量失败', error)
  }
}

const loadChatUnreadCount = async () => {
  if (!authStore.isLoggedIn) return
  try {
    const res = await chatApi.getUnreadCount()
    chatUnreadCount.value = res.count || 0
  } catch (error) {
    console.error('加载聊天未读数量失败', error)
  }
}

const handleWebSocketMessage = (message: any) => {
  console.log('收到 WebSocket 消息:', message)
  
  const { type, data } = message
  
  switch (type) {
    case 'CONNECTED':
      loadChatUnreadCount()
      break
      
    case 'NEW_MESSAGE':
      unreadCount.value++
      break
      
    case 'UNREAD_COUNT':
      unreadCount.value = data.count
      break
      
    case 'CHAT_MESSAGE':
      chatUnreadCount.value++
      break
      
    case 'NEW_BORROW_APPLY':
      if (data.pendingCount !== undefined) {
        pendingApprovalCount.value = data.pendingCount
      }
      break
      
    case 'ITEM_STATUS_CHANGED':
      if (data.pendingCount !== undefined) {
        const total = data.pendingCount
        pendingApprovalCount.value = total
      }
      break
      
    case 'RETURN_REQUESTED':
      if (data.pendingCount !== undefined) {
        returnRequestedCount.value = data.pendingCount - pendingApprovalCount.value
      }
      break
      
    case 'RETURN_CONFIRMED':
      if (data.pendingCount !== undefined) {
        const total = data.pendingCount
        returnRequestedCount.value = Math.max(0, returnRequestedCount.value - 1)
      }
      break
      
    case 'REQUEST_APPROVED':
    case 'REQUEST_REJECTED':
    case 'BORROW_RETURNED':
      break
  }
}

onMounted(() => {
  authStore.init()
  loadPendingCount()
  loadUnreadCount()
  loadChatUnreadCount()
  
  wsManager.on('*', handleWebSocketMessage)
})

onUnmounted(() => {
  wsManager.off('*', handleWebSocketMessage)
})

const logout = () => {
  authStore.logout()
  router.push('/login')
}
</script>
