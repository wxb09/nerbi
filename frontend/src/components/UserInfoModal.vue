<template>
  <div v-if="visible" class="fixed inset-0 z-50 flex items-center justify-center p-4">
    <div class="absolute inset-0 bg-black/50" @click="close"></div>
    
    <div class="relative bg-white rounded-[2rem] w-full max-w-md shadow-2xl overflow-hidden animate-fadeIn">
      <div class="relative bg-gradient-to-br from-[#FDF8EE] to-white px-8 py-10 text-center">
        <button 
          @click="close"
          class="absolute top-4 right-4 p-2 hover:bg-gray-100 rounded-full transition-colors"
        >
          <span class="iconify text-xl text-gray-400" data-icon="solar:close-circle-bold"></span>
        </button>
        
        <div class="relative inline-block mb-4">
          <img 
            :src="getImageUrl(user?.avatar)" 
            class="w-24 h-24 rounded-full border-4 border-white shadow-lg object-cover"
          />
          <span 
            v-if="isOnline"
            class="absolute bottom-1 right-1 w-5 h-5 bg-green-500 border-3 border-white rounded-full"
          ></span>
        </div>
        
        <h2 class="text-2xl font-bold text-gray-800 mb-1">{{ user?.nickname || '用户' }}</h2>
        <p class="text-sm text-gray-500 flex items-center justify-center gap-1">
          <span class="iconify" data-icon="solar:map-point-bold"></span>
          {{ user?.communityName || '未设置社区' }} {{ user?.building || '' }}
        </p>
        
        <div class="flex items-center justify-center gap-2 mt-3">
          <span 
            :class="[
              'px-3 py-1 rounded-full text-xs font-bold',
              isOnline ? 'bg-green-100 text-green-600' : 'bg-gray-100 text-gray-500'
            ]"
          >
            {{ isOnline ? '🟢 在线' : '⚫ 离线' }}
          </span>
        </div>
      </div>
      
      <div class="px-8 py-6">
        <div class="grid grid-cols-2 gap-4 mb-6">
          <div class="bg-gray-50 rounded-xl p-4 text-center">
            <p class="text-2xl font-bold text-[#E2B04D]">{{ user?.borrowCount || 0 }}</p>
            <p class="text-xs text-gray-500 mt-1">累计借入</p>
          </div>
          <div class="bg-gray-50 rounded-xl p-4 text-center">
            <p class="text-2xl font-bold text-[#E2B04D]">{{ user?.lendCount || 0 }}</p>
            <p class="text-xs text-gray-500 mt-1">累计借出</p>
          </div>
        </div>
        
        <div v-if="user?.bio" class="mb-6">
          <h3 class="text-sm font-bold text-gray-700 mb-2">个人简介</h3>
          <p class="text-sm text-gray-600 leading-relaxed">{{ user.bio }}</p>
        </div>
        
        <div class="space-y-3">
          <button 
            v-if="showContact"
            @click="handleContact"
            class="w-full py-3 bg-[#E2B04D] text-white rounded-xl font-bold hover:bg-[#d4a344] transition-all flex items-center justify-center gap-2"
          >
            <span class="iconify text-lg" data-icon="solar:chat-round-dots-bold"></span>
            联系TA
          </button>
          
          <button 
            @click="close"
            class="w-full py-3 border border-gray-200 text-gray-600 rounded-xl font-bold hover:bg-gray-50 transition-all"
          >
            关闭
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { chatApi } from '../api/chat'

interface User {
  id: number
  nickname: string
  avatar: string
  communityName?: string
  building?: string
  borrowCount?: number
  lendCount?: number
  bio?: string
}

interface Props {
  visible: boolean
  user: User | null
  showContact?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  showContact: true
})

const emit = defineEmits<{
  close: []
  contact: []
}>()

const isOnline = ref(false)

const close = () => {
  emit('close')
}

const handleContact = () => {
  emit('contact')
  close()
}

const getImageUrl = (path: string | undefined) => {
  if (!path) return 'https://modao.cc/agent-py/media/generated_images/2026-03-19/7a697da7cb0e46808f265a42ae7934f3.jpg'
  if (path.startsWith('http')) return path
  return `http://localhost:8080${path}`
}

const loadUserStatus = async () => {
  if (!props.user?.id) return
  try {
    const res = await chatApi.getUserStatus(props.user.id)
    isOnline.value = res.isOnline
  } catch (error) {
    console.error('加载用户状态失败', error)
  }
}

watch(() => props.visible, (newVal) => {
  if (newVal && props.user?.id) {
    loadUserStatus()
  }
})
</script>

<style scoped>
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.animate-fadeIn {
  animation: fadeIn 0.2s ease-out;
}
</style>
