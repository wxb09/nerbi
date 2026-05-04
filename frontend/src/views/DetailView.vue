<template>
  <MainNav />
  <main class="max-w-7xl mx-auto px-4 sm:px-6 py-8 animate-fadeIn">
    <div v-if="loading" class="flex flex-col items-center justify-center py-20">
      <div class="w-16 h-16 border-4 border-[#E2B04D] border-t-transparent rounded-full animate-spin"></div>
      <p class="text-gray-400 mt-6 font-medium">加载中...</p>
    </div>
    
    <div v-else class="grid lg:grid-cols-2 gap-8 lg:gap-10">
      <div class="space-y-5">
        <div class="relative group">
          <div class="h-[380px] sm:h-[420px] rounded-2xl bg-gray-50 relative overflow-hidden">
            <img 
              v-if="item?.images && item.images.length > 0" 
              :src="getImageUrl(item.images[0])" 
              :alt="item.name" 
              class="transition-transform duration-500 group-hover:scale-105" 
            />
            <div v-else class="h-full flex flex-col items-center justify-center text-gray-300">
              <span class="iconify text-6xl mb-3" data-icon="solar:gallery-bold"></span>
              <span class="text-sm">暂无图片</span>
            </div>
          </div>
          <div class="absolute top-3 right-3">
            <span :class="getStatusClass(item?.status)">
              {{ getStatusText(item?.status) }}
            </span>
          </div>
        </div>
        
        <div class="grid grid-cols-4 gap-2">
          <div 
            v-for="(image, index) in item?.images || []" 
            :key="index" 
            class="h-16 sm:h-20 rounded-lg bg-gray-50 relative overflow-hidden cursor-pointer hover:ring-2 hover:ring-[#E2B04D] transition-all"
          >
            <img :src="getImageUrl(image)" :alt="item.name" class="rounded-lg" />
          </div>
          <div v-if="(!item?.images || item.images.length === 0)" class="h-16 sm:h-20 rounded-lg bg-gray-50 flex items-center justify-center text-gray-400 col-span-4">
            <span class="iconify text-2xl mr-2" data-icon="solar:gallery-bold"></span>
            <span class="text-sm">暂无更多图片</span>
          </div>
        </div>

        <div class="p-5 rounded-2xl border border-gray-100 bg-white space-y-3">
          <div class="flex items-center gap-2">
            <span class="iconify text-lg text-[#E2B04D]" data-icon="solar:book-bold"></span>
            <h3 class="font-bold text-[#2D3436]">物品故事</h3>
          </div>
          <p class="text-gray-500 text-sm leading-relaxed">{{ item?.story || '暂无故事' }}</p>
        </div>

        <div class="p-5 rounded-2xl border border-gray-100 bg-white">
          <div class="flex items-center gap-2 mb-3">
            <span class="iconify text-lg text-[#E2B04D]" data-icon="solar:user-bold"></span>
            <h3 class="font-bold text-[#2D3436]">物品主人</h3>
          </div>
          <div class="flex items-center justify-between">
            <div class="flex items-center space-x-3">
              <img 
                :src="getImageUrl(item?.owner?.avatar || 'https://modao.cc/agent-py/media/generated_images/2026-03-19/7a697da7cb0e46808f265a42ae7934f3.jpg')" 
                class="w-12 h-12 rounded-full border border-[#E2B04D]/20" 
              />
              <div>
                <p class="font-bold text-sm text-[#2D3436]">{{ item?.owner?.nickname || '未知' }}</p>
                <p class="text-xs text-gray-400 flex items-center gap-1">
                  <span class="iconify" data-icon="solar:map-point-bold"></span>
                  {{ item?.communityName || '' }} {{ item?.building || '' }}
                </p>
              </div>
            </div>
            <div class="flex gap-2">
              <button 
                @click="showUserModal = true"
                class="px-4 py-2 border border-gray-200 rounded-lg text-xs font-bold hover:border-[#E2B04D] hover:text-[#E2B04D] hover:bg-[#F5E6C8]/30 transition-all"
              >
                查看主页
              </button>
              <button 
                v-if="item?.owner?.id !== currentUserId"
                @click="startChat"
                class="px-4 py-2 bg-[#E2B04D] text-white rounded-lg text-xs font-bold hover:bg-[#d4a344] transition-all flex items-center gap-1"
              >
                <span class="iconify" data-icon="solar:chat-round-dots-bold"></span>
                联系TA
              </button>
            </div>
          </div>
        </div>
      </div>
      
      <div class="space-y-5">
        <div class="animate-slideIn">
          <h1 class="text-2xl sm:text-3xl font-bold text-[#2D3436] leading-tight">{{ item?.name || '加载中...' }}</h1>
          <p class="text-gray-500 mt-2 text-sm leading-relaxed">{{ item?.description }}</p>
        </div>
        
        <div class="flex items-center space-x-3 text-xs text-gray-400">
          <span class="flex items-center gap-1">
            <span class="iconify" data-icon="solar:eye-bold"></span>
            {{ item?.viewCount || 0 }} 浏览
          </span>
          <span class="text-gray-200">|</span>
          <span class="flex items-center gap-1">
            <span class="iconify" data-icon="solar:hand-shake-bold"></span>
            {{ item?.borrowCount || 0 }} 借阅
          </span>
        </div>

        <div class="p-4 rounded-2xl border border-gray-100 bg-white space-y-3">
          <div class="flex items-center gap-2">
            <span class="iconify text-lg text-[#E2B04D]" data-icon="solar:document-text-bold"></span>
            <h3 class="font-bold text-sm text-[#2D3436]">借阅规则</h3>
          </div>
          <div class="grid grid-cols-3 gap-3">
            <div class="bg-[#FDF8EE] p-3 rounded-xl text-center">
              <p class="text-xl font-bold text-[#E2B04D]">￥{{ item?.pricePerDay || 0 }}</p>
              <p class="text-[10px] text-gray-400 mt-1">每天租金</p>
            </div>
            <div class="bg-blue-50/60 p-3 rounded-xl text-center">
              <p class="text-xl font-bold text-blue-600">￥{{ item?.deposit || 0 }}</p>
              <p class="text-[10px] text-gray-400 mt-1">押金</p>
            </div>
            <div class="bg-green-50/60 p-3 rounded-xl text-center">
              <p class="text-xl font-bold text-green-600">{{ item?.creditRequired || 0 }}+</p>
              <p class="text-[10px] text-gray-400 mt-1">信用分</p>
            </div>
          </div>
          <div v-if="item?.returnRequirements && item.returnRequirements.length > 0" class="flex flex-wrap gap-1.5">
            <span 
              v-for="req in item.returnRequirements" 
              :key="req" 
              class="px-3 py-1 bg-gray-50 rounded-full text-xs text-gray-500 border border-gray-100"
            >
              {{ req }}
            </span>
          </div>
        </div>

        <div v-if="item?.status === 'AVAILABLE'" class="p-4 rounded-2xl border border-[#E2B04D]/20 bg-[#FDF8EE]/50 space-y-3">
          <div class="flex items-center gap-2">
            <span class="iconify text-lg text-[#E2B04D]" data-icon="solar:calendar-bold"></span>
            <h3 class="font-bold text-sm text-[#2D3436]">申请借用</h3>
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="text-xs text-gray-400 mb-1 block">开始日期</label>
              <input 
                v-model="borrowForm.startDate" 
                class="w-full p-3 rounded-lg border border-gray-200 focus:border-[#E2B04D] focus:outline-none transition-all bg-white text-sm" 
                type="date" 
                :min="minDate" 
              />
            </div>
            <div>
              <label class="text-xs text-gray-400 mb-1 block">结束日期</label>
              <input 
                v-model="borrowForm.endDate" 
                class="w-full p-3 rounded-lg border border-gray-200 focus:border-[#E2B04D] focus:outline-none transition-all bg-white text-sm" 
                type="date" 
                :min="borrowForm.startDate || minDate" 
              />
            </div>
          </div>
          <div>
            <label class="text-xs text-gray-400 mb-1 block">用途简述</label>
            <textarea 
              v-model="borrowForm.purpose" 
              class="w-full p-3 rounded-lg border border-gray-200 focus:border-[#E2B04D] focus:outline-none transition-all bg-white h-16 resize-none text-sm" 
              placeholder="请简要说明借用目的..."
            ></textarea>
          </div>
          <label class="flex items-center gap-2 cursor-pointer">
            <input type="checkbox" id="agreeTerms" v-model="borrowForm.agreeTerms" class="w-4 h-4 rounded border-gray-300 text-[#E2B04D] focus:ring-[#E2B04D]" />
            <span class="text-xs text-gray-500">我已阅读并同意借阅规则</span>
          </label>
          <button 
            @click="submitBorrow" 
            :disabled="!canSubmit"
            :class="[
              'w-full py-3 rounded-xl font-bold text-sm transition-all duration-300',
              canSubmit 
                ? 'bg-[#2D3436] text-white hover:bg-[#3d4648]' 
                : 'bg-gray-200 text-gray-400 cursor-not-allowed'
            ]"
          >
            <span v-if="submitting" class="flex items-center justify-center gap-2">
              <span class="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></span>
              提交中...
            </span>
            <span v-else class="flex items-center justify-center gap-2">
              <span class="iconify" data-icon="solar:hand-shake-bold"></span>
              提交借用申请
            </span>
          </button>
          <p v-if="errorMsg" class="text-red-500 text-xs text-center flex items-center justify-center gap-1">
            <span class="iconify" data-icon="solar:danger-circle-bold"></span>
            {{ errorMsg }}
          </p>
        </div>

        <div v-else-if="item?.status === 'BORROWED'" class="p-5 rounded-2xl border border-yellow-200 bg-yellow-50/50 text-center">
          <span class="iconify text-4xl text-yellow-500 mb-2" data-icon="solar:clock-circle-bold"></span>
          <p class="text-yellow-700 font-bold">该物品正在借出中</p>
          <p class="text-xs text-yellow-600 mt-1">暂时无法申请借用，请稍后再试</p>
        </div>

        <div v-else-if="item?.status === 'OFFLINE'" class="p-5 rounded-2xl border border-gray-200 bg-gray-50/50 text-center">
          <span class="iconify text-4xl text-gray-400 mb-2" data-icon="solar:box-minimalistic-bold"></span>
          <p class="text-gray-600 font-bold">该物品已下架</p>
          <p class="text-xs text-gray-500 mt-1">物品主人暂时停止出借</p>
        </div>
      </div>
    </div>
  </main>

  <UserInfoModal 
    :visible="showUserModal" 
    :user="item?.owner"
    :showContact="item?.owner?.id !== currentUserId"
    @close="showUserModal = false"
    @contact="startChat"
  />
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import MainNav from '../components/MainNav.vue'
import UserInfoModal from '../components/UserInfoModal.vue'
import { itemApi } from '../api/item'
import { borrowApi } from '../api/borrow'
import { chatApi } from '../api/chat'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const item = ref<any>(null)
const loading = ref(true)
const submitting = ref(false)
const errorMsg = ref('')
const currentUserId = ref(Number(authStore.user?.id))
const showUserModal = ref(false)

const borrowForm = ref({
  startDate: '',
  endDate: '',
  purpose: '',
  agreeTerms: false
})

const minDate = computed(() => {
  const today = new Date()
  return today.toISOString().split('T')[0]
})

const canSubmit = computed(() => {
  return borrowForm.value.startDate && 
         borrowForm.value.endDate && 
         borrowForm.value.purpose.trim() && 
         borrowForm.value.agreeTerms &&
         !submitting.value
})

const getStatusClass = (status: string) => {
  const classes: Record<string, string> = {
    'AVAILABLE': 'px-3 py-1.5 bg-green-50 text-green-700 rounded-full text-xs font-bold border border-green-200',
    'BORROWED': 'px-3 py-1.5 bg-yellow-50 text-yellow-700 rounded-full text-xs font-bold border border-yellow-200',
    'OFFLINE': 'px-3 py-1.5 bg-gray-50 text-gray-500 rounded-full text-xs font-bold border border-gray-200',
    'DRAFT': 'px-3 py-1.5 bg-gray-50 text-gray-600 rounded-full text-xs font-bold border border-gray-200'
  }
  return classes[status] || 'px-3 py-1.5 bg-gray-50 text-gray-700 rounded-full text-xs font-bold'
}

const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    'AVAILABLE': '✓ 可借',
    'BORROWED': '借出中',
    'OFFLINE': '已下架',
    'DRAFT': '草稿'
  }
  return texts[status] || status
}

const loadItemDetail = async () => {
  const id = route.params.id as string
  loading.value = true
  try {
    const res = await itemApi.getItemById(id)
    item.value = res
  } catch (error) {
    console.error('加载物品详情失败', error)
  } finally {
    loading.value = false
  }
}

const submitBorrow = async () => {
  if (!authStore.isLoggedIn) {
    router.push('/login')
    return
  }

  if (!canSubmit.value) return

  submitting.value = true
  errorMsg.value = ''

  try {
    await borrowApi.createBorrow({
      itemId: Number(route.params.id),
      startDate: borrowForm.value.startDate,
      endDate: borrowForm.value.endDate,
      purpose: borrowForm.value.purpose,
      agreeTerms: borrowForm.value.agreeTerms
    })
    alert('借阅申请已提交！')
    router.push('/profile')
  } catch (error: any) {
    errorMsg.value = error.message || '提交失败，请重试'
  } finally {
    submitting.value = false
  }
}

const startChat = async () => {
  if (!authStore.isLoggedIn) {
    router.push('/login')
    return
  }

  if (!item.value?.owner?.id) return

  try {
    const res = await chatApi.getOrCreateConversation(item.value.owner.id)
    router.push(`/chat?conversationId=${res.id}`)
  } catch (error) {
    console.error('创建会话失败', error)
    alert('创建会话失败，请重试')
  }
}

const getImageUrl = (path: string) => {
  if (!path) return ''
  if (path.startsWith('http')) {
    return path
  }
  if (path.startsWith('/uploads/')) {
    return path
  }
  return `/uploads/${path}`
}

onMounted(() => {
  loadItemDetail()
})
</script>
