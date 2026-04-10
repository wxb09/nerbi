<template>
  <MainNav />
  <main class="max-w-7xl mx-auto px-6 py-12 animate-fadeIn">
    <div v-if="loading" class="flex flex-col items-center justify-center py-20">
      <div class="w-16 h-16 border-4 border-[#E2B04D] border-t-transparent rounded-full animate-spin"></div>
      <p class="text-gray-400 mt-6 font-medium">加载中...</p>
    </div>
    
    <div v-else class="grid lg:grid-cols-2 gap-12">
      <div class="space-y-4">
        <div class="relative group">
          <div class="h-[420px] rounded-3xl bg-gradient-to-br from-gray-50 to-gray-100 relative overflow-hidden shadow-lg">
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
          <div class="absolute top-4 right-4">
            <span :class="getStatusClass(item?.status)" class="shadow-md">
              {{ getStatusText(item?.status) }}
            </span>
          </div>
        </div>
        
        <div class="grid grid-cols-4 gap-3">
          <div 
            v-for="(image, index) in item?.images || []" 
            :key="index" 
            class="h-20 rounded-xl bg-gray-100 relative overflow-hidden cursor-pointer hover:ring-2 hover:ring-[#E2B04D] transition-all"
          >
            <img :src="getImageUrl(image)" :alt="item.name" class="rounded-xl" />
          </div>
          <div v-if="(!item?.images || item.images.length === 0)" class="h-20 rounded-xl bg-gray-100 flex items-center justify-center text-gray-400 col-span-4">
            <span class="iconify text-2xl mr-2" data-icon="solar:gallery-bold"></span>
            <span class="text-sm">暂无更多图片</span>
          </div>
        </div>
      </div>
      
      <div class="space-y-6">
        <div class="animate-slideIn">
          <h1 class="text-3xl font-bold text-[#2D3436] leading-tight">{{ item?.name || '加载中...' }}</h1>
          <p class="text-gray-500 mt-3 leading-relaxed">{{ item?.description }}</p>
        </div>
        
        <div class="flex items-center space-x-4 text-sm">
          <span class="text-gray-400 flex items-center gap-1">
            <span class="iconify" data-icon="solar:eye-bold"></span>
            {{ item?.viewCount || 0 }} 次浏览
          </span>
          <span class="text-gray-300">|</span>
          <span class="text-gray-400 flex items-center gap-1">
            <span class="iconify" data-icon="solar:hand-shake-bold"></span>
            {{ item?.borrowCount || 0 }} 次借阅
          </span>
        </div>

        <div class="bg-white p-6 rounded-3xl border border-gray-100 shadow-sm space-y-5">
          <div class="flex items-center gap-2">
            <span class="iconify text-xl text-[#E2B04D]" data-icon="solar:document-text-bold"></span>
            <h3 class="font-bold text-lg text-[#2D3436]">借阅规则</h3>
          </div>
          <div class="grid grid-cols-3 gap-4">
            <div class="bg-gradient-to-br from-[#F5E6C8] to-[#FDF8EE] p-5 rounded-2xl text-center transform hover:scale-105 transition-transform">
              <p class="text-3xl font-bold text-[#E2B04D]">￥{{ item?.pricePerDay || 0 }}</p>
              <p class="text-xs text-gray-500 mt-2 font-medium">每天租金</p>
            </div>
            <div class="bg-gradient-to-br from-blue-50 to-blue-100/50 p-5 rounded-2xl text-center transform hover:scale-105 transition-transform">
              <p class="text-3xl font-bold text-blue-600">￥{{ item?.deposit || 0 }}</p>
              <p class="text-xs text-gray-500 mt-2 font-medium">押金</p>
            </div>
            <div class="bg-gradient-to-br from-green-50 to-green-100/50 p-5 rounded-2xl text-center transform hover:scale-105 transition-transform">
              <p class="text-3xl font-bold text-green-600">{{ item?.creditRequired || 0 }}+</p>
              <p class="text-xs text-gray-500 mt-2 font-medium">信用分要求</p>
            </div>
          </div>
          <div v-if="item?.returnRequirements && item.returnRequirements.length > 0">
            <p class="text-sm font-bold mb-3 text-gray-700 flex items-center gap-2">
              <span class="iconify text-[#E2B04D]" data-icon="solar:checklist-bold"></span>
              归还要求
            </p>
            <div class="flex flex-wrap gap-2">
              <span 
                v-for="req in item.returnRequirements" 
                :key="req" 
                class="px-4 py-2 bg-gray-50 rounded-full text-sm text-gray-600 border border-gray-100 hover:border-[#E2B04D] hover:bg-[#F5E6C8]/30 transition-colors"
              >
                {{ req }}
              </span>
            </div>
          </div>
        </div>

        <div v-if="item?.status === 'AVAILABLE'" class="bg-white p-6 rounded-3xl border border-gray-100 shadow-sm space-y-5">
          <div class="flex items-center gap-2">
            <span class="iconify text-xl text-[#E2B04D]" data-icon="solar:calendar-bold"></span>
            <h3 class="font-bold text-lg text-[#2D3436]">申请借用</h3>
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="text-sm text-gray-500 mb-2 block font-medium">开始日期</label>
              <input 
                v-model="borrowForm.startDate" 
                class="w-full p-4 rounded-xl border-2 border-gray-100 focus:border-[#E2B04D] focus:outline-none transition-all bg-gray-50 hover:bg-white" 
                type="date" 
                :min="minDate" 
              />
            </div>
            <div>
              <label class="text-sm text-gray-500 mb-2 block font-medium">结束日期</label>
              <input 
                v-model="borrowForm.endDate" 
                class="w-full p-4 rounded-xl border-2 border-gray-100 focus:border-[#E2B04D] focus:outline-none transition-all bg-gray-50 hover:bg-white" 
                type="date" 
                :min="borrowForm.startDate || minDate" 
              />
            </div>
          </div>
          <div>
            <label class="text-sm text-gray-500 mb-2 block font-medium">用途简述</label>
            <textarea 
              v-model="borrowForm.purpose" 
              class="w-full p-4 rounded-xl border-2 border-gray-100 focus:border-[#E2B04D] focus:outline-none transition-all bg-gray-50 hover:bg-white h-24 resize-none" 
              placeholder="请简要说明借用目的..."
            ></textarea>
          </div>
          <label class="flex items-center gap-3 cursor-pointer group">
            <input type="checkbox" id="agreeTerms" v-model="borrowForm.agreeTerms" class="w-5 h-5 rounded border-gray-300 text-[#E2B04D] focus:ring-[#E2B04D]" />
            <span class="text-sm text-gray-600 group-hover:text-gray-800 transition-colors">我已阅读并同意借阅规则</span>
          </label>
          <button 
            @click="submitBorrow" 
            :disabled="!canSubmit"
            :class="[
              'w-full py-4 rounded-2xl font-bold text-lg transition-all duration-300',
              canSubmit 
                ? 'bg-gradient-to-r from-[#2D3436] to-[#3d4648] text-white hover:shadow-lg hover:-translate-y-0.5' 
                : 'bg-gray-200 text-gray-400 cursor-not-allowed'
            ]"
          >
            <span v-if="submitting" class="flex items-center justify-center gap-2">
              <span class="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></span>
              提交中...
            </span>
            <span v-else class="flex items-center justify-center gap-2">
              <span class="iconify" data-icon="solar:hand-shake-bold"></span>
              提交借用申请
            </span>
          </button>
          <p v-if="errorMsg" class="text-red-500 text-sm text-center flex items-center justify-center gap-2">
            <span class="iconify" data-icon="solar:danger-circle-bold"></span>
            {{ errorMsg }}
          </p>
        </div>

        <div v-else-if="item?.status === 'BORROWED'" class="bg-gradient-to-br from-yellow-50 to-orange-50 p-6 rounded-3xl border border-yellow-200 text-center">
          <span class="iconify text-5xl text-yellow-500 mb-3" data-icon="solar:clock-circle-bold"></span>
          <p class="text-yellow-700 font-bold text-lg">该物品正在借出中</p>
          <p class="text-sm text-yellow-600 mt-2">暂时无法申请借用，请稍后再试</p>
        </div>

        <div v-else-if="item?.status === 'OFFLINE'" class="bg-gradient-to-br from-gray-50 to-gray-100 p-6 rounded-3xl border border-gray-200 text-center">
          <span class="iconify text-5xl text-gray-400 mb-3" data-icon="solar:box-minimalistic-bold"></span>
          <p class="text-gray-600 font-bold text-lg">该物品已下架</p>
          <p class="text-sm text-gray-500 mt-2">物品主人暂时停止出借</p>
        </div>

        <div class="bg-white p-6 rounded-3xl border border-gray-100 shadow-sm space-y-4">
          <div class="flex items-center gap-2">
            <span class="iconify text-xl text-[#E2B04D]" data-icon="solar:book-bold"></span>
            <h3 class="font-bold text-lg text-[#2D3436]">物品故事</h3>
          </div>
          <p class="text-gray-600 leading-relaxed">{{ item?.story || '暂无故事' }}</p>
        </div>

        <div class="bg-white p-6 rounded-3xl border border-gray-100 shadow-sm">
          <div class="flex items-center gap-2 mb-4">
            <span class="iconify text-xl text-[#E2B04D]" data-icon="solar:user-bold"></span>
            <h3 class="font-bold text-lg text-[#2D3436]">物品主人</h3>
          </div>
          <div class="flex items-center justify-between">
            <div class="flex items-center space-x-4">
              <img 
                :src="getImageUrl(item?.owner?.avatar || 'https://modao.cc/agent-py/media/generated_images/2026-03-19/7a697da7cb0e46808f265a42ae7934f3.jpg')" 
                class="w-14 h-14 rounded-full border-2 border-[#E2B04D]/20 shadow-md" 
              />
              <div>
                <p class="font-bold text-[#2D3436]">{{ item?.owner?.nickname || '未知' }}</p>
                <p class="text-sm text-gray-400 flex items-center gap-1">
                  <span class="iconify" data-icon="solar:map-point-bold"></span>
                  {{ item?.communityName || '' }} {{ item?.building || '' }}
                </p>
              </div>
            </div>
            <RouterLink 
              class="px-5 py-2.5 border-2 border-gray-200 rounded-xl text-sm font-bold hover:border-[#E2B04D] hover:text-[#E2B04D] hover:bg-[#F5E6C8]/30 transition-all" 
              :to="`/user/${item?.owner?.id}`"
            >
              查看主页
            </RouterLink>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import MainNav from '../components/MainNav.vue'
import { itemApi } from '../api/item'
import { borrowApi } from '../api/borrow'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const item = ref<any>(null)
const loading = ref(true)
const submitting = ref(false)
const errorMsg = ref('')

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
    'AVAILABLE': 'px-4 py-2 bg-gradient-to-r from-green-100 to-green-50 text-green-700 rounded-full text-sm font-bold border border-green-200',
    'BORROWED': 'px-4 py-2 bg-gradient-to-r from-yellow-100 to-yellow-50 text-yellow-700 rounded-full text-sm font-bold border border-yellow-200',
    'OFFLINE': 'px-4 py-2 bg-gradient-to-r from-gray-100 to-gray-50 text-gray-500 rounded-full text-sm font-bold border border-gray-200',
    'DRAFT': 'px-4 py-2 bg-gradient-to-r from-gray-100 to-gray-50 text-gray-600 rounded-full text-sm font-bold border border-gray-200'
  }
  return classes[status] || 'px-4 py-2 bg-gray-100 text-gray-700 rounded-full text-sm font-bold'
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
