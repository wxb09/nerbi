<template>
  <MainNav />
  <main class="max-w-7xl mx-auto px-6 py-12 grid lg:grid-cols-2 gap-12">
    <div class="space-y-4">
      <div class="h-[420px] rounded-3xl bg-gray-100 flex items-center justify-center overflow-hidden">
        <img v-if="item?.images && item.images.length > 0" :src="getImageUrl(item.images[0])" :alt="item.name" class="h-full w-full object-cover" />
        <div v-else class="text-gray-400">无图片</div>
      </div>
      <div class="grid grid-cols-4 gap-3">
        <div v-for="(image, index) in item?.images || []" :key="index" class="h-20 rounded-xl bg-gray-100 flex items-center justify-center overflow-hidden">
          <img :src="getImageUrl(image)" :alt="item.name" class="h-full w-full object-cover rounded-xl" />
        </div>
        <div v-if="(!item?.images || item.images.length === 0)" class="h-20 rounded-xl bg-gray-100 flex items-center justify-center text-gray-400">
          无图片
        </div>
      </div>
    </div>
    <div class="space-y-6">
      <div>
        <h1 class="text-3xl font-bold">{{ item?.name || '加载中...' }}</h1>
        <p class="text-gray-600 mt-2">{{ item?.description }}</p>
      </div>
      
      <div class="flex items-center space-x-4">
        <span :class="getStatusClass(item?.status)">{{ getStatusText(item?.status) }}</span>
        <span class="text-sm text-gray-400">{{ item?.borrowCount || 0 }}次借阅 · {{ item?.viewCount || 0 }}次浏览</span>
      </div>

      <div class="bg-[#F8F9FA] p-6 rounded-3xl border space-y-4">
        <h3 class="font-bold text-lg">借阅规则</h3>
        <div class="grid grid-cols-3 gap-4 text-center">
          <div class="bg-white p-4 rounded-2xl">
            <p class="text-2xl font-bold text-[#E2B04D]">￥{{ item?.pricePerDay || 0 }}</p>
            <p class="text-xs text-gray-400 mt-1">每天</p>
          </div>
          <div class="bg-white p-4 rounded-2xl">
            <p class="text-2xl font-bold text-[#E2B04D]">￥{{ item?.deposit || 0 }}</p>
            <p class="text-xs text-gray-400 mt-1">押金</p>
          </div>
          <div class="bg-white p-4 rounded-2xl">
            <p class="text-2xl font-bold text-[#E2B04D]">{{ item?.creditRequired || 0 }}+</p>
            <p class="text-xs text-gray-400 mt-1">信用分</p>
          </div>
        </div>
        <div v-if="item?.returnRequirements && item.returnRequirements.length > 0">
          <p class="text-sm font-bold mb-2">归还要求</p>
          <div class="flex flex-wrap gap-2">
            <span v-for="req in item.returnRequirements" :key="req" class="px-3 py-1 bg-white rounded-full text-xs">{{ req }}</span>
          </div>
        </div>
      </div>

      <div v-if="item?.status === 'AVAILABLE'" class="bg-[#F8F9FA] p-6 rounded-3xl border space-y-4">
        <h3 class="font-bold text-lg">申请借用</h3>
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="text-sm text-gray-500 mb-1 block">开始日期</label>
            <input v-model="borrowForm.startDate" class="w-full p-3 rounded-xl border" type="date" :min="minDate" />
          </div>
          <div>
            <label class="text-sm text-gray-500 mb-1 block">结束日期</label>
            <input v-model="borrowForm.endDate" class="w-full p-3 rounded-xl border" type="date" :min="borrowForm.startDate || minDate" />
          </div>
        </div>
        <textarea v-model="borrowForm.purpose" class="w-full p-3 rounded-xl border h-24" placeholder="用途简述（必填）"></textarea>
        <div class="flex items-center space-x-2">
          <input type="checkbox" id="agreeTerms" v-model="borrowForm.agreeTerms" class="w-4 h-4" />
          <label for="agreeTerms" class="text-sm text-gray-600">我已阅读并同意借阅规则</label>
        </div>
        <button 
          @click="submitBorrow" 
          :disabled="!canSubmit"
          :class="[
            'w-full py-3 rounded-xl font-bold transition-colors',
            canSubmit ? 'bg-[#2D3436] text-white hover:bg-[#1a1a1a]' : 'bg-gray-200 text-gray-400 cursor-not-allowed'
          ]"
        >
          {{ submitting ? '提交中...' : '提交借用申请' }}
        </button>
        <p v-if="errorMsg" class="text-red-500 text-sm text-center">{{ errorMsg }}</p>
      </div>

      <div v-else-if="item?.status === 'BORROWED'" class="bg-yellow-50 p-6 rounded-3xl border border-yellow-200 text-center">
        <p class="text-yellow-700 font-bold">该物品正在借出中</p>
        <p class="text-sm text-yellow-600 mt-2">暂时无法申请借用</p>
      </div>

      <div v-else-if="item?.status === 'OFFLINE'" class="bg-gray-50 p-6 rounded-3xl border border-gray-200 text-center">
        <p class="text-gray-600 font-bold">该物品已下架</p>
      </div>

      <div class="bg-white p-6 rounded-3xl border space-y-4">
        <h3 class="font-bold text-lg">物品故事</h3>
        <p class="text-gray-600 leading-relaxed">{{ item?.story || '暂无故事' }}</p>
      </div>

      <div class="bg-white p-6 rounded-3xl border space-y-4">
        <h3 class="font-bold text-lg">物品主人</h3>
        <div class="flex items-center justify-between">
          <div class="flex items-center space-x-3">
            <img :src="getImageUrl(item?.owner?.avatar || 'https://modao.cc/agent-py/media/generated_images/2026-03-19/7a697da7cb0e46808f265a42ae7934f3.jpg')" class="w-12 h-12 rounded-full" />
            <div>
              <p class="font-bold">{{ item?.owner?.nickname || '未知' }}</p>
              <p class="text-sm text-gray-400">{{ item?.communityName || '' }} {{ item?.building || '' }}</p>
            </div>
          </div>
          <RouterLink class="px-4 py-2 border rounded-xl text-sm font-bold hover:bg-gray-50" :to="`/user/${item?.owner?.id}`">
            查看主页
          </RouterLink>
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
    'AVAILABLE': 'px-3 py-1 bg-green-100 text-green-700 rounded-full text-sm font-bold',
    'BORROWED': 'px-3 py-1 bg-yellow-100 text-yellow-700 rounded-full text-sm font-bold',
    'OFFLINE': 'px-3 py-1 bg-gray-100 text-gray-500 rounded-full text-sm font-bold',
    'DRAFT': 'px-3 py-1 bg-gray-100 text-gray-600 rounded-full text-sm font-bold'
  }
  return classes[status] || 'px-3 py-1 bg-gray-100 text-gray-700 rounded-full text-sm font-bold'
}

const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    'AVAILABLE': '可借',
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
  // 如果路径已经是 /uploads/ 开头，直接返回
  if (path.startsWith('/uploads/')) {
    return path
  }
  // 否则添加 /uploads/ 前缀（物品图片的情况）
  return `/uploads/${path}`
}

onMounted(() => {
  loadItemDetail()
})
</script>
