<template>
  <MainNav />
  <header class="max-w-7xl mx-auto px-6 pt-12 pb-8">
    <div class="bg-[#F3EFEA] rounded-[2rem] p-10 border border-[#E2B04D]/20 flex items-center justify-between gap-8">
      <div class="flex-[4]">
        <h1 class="text-4xl font-bold text-[#2D3436]">让社区物品<span class="text-[#E2B04D]">自由流动</span></h1>
        <p class="text-gray-600 mt-4">在 2026 年，我们重新定义邻里关系。不再购买只用一次的工具，通过<br />借用连接身边的信任。</p>
        <RouterLink class="inline-block mt-6 bg-[#E2B04D] text-white px-8 py-4 rounded-2xl font-bold shadow-[5px_5px_0_0_#2D3436] hover:translate-y-1 hover:shadow-none transition-all duration-200" to="/publish">我也要发布</RouterLink>
      </div>
      <div class="flex-[2] flex-shrink-0 hidden lg:block">
        <div class="relative">
          <img 
            src="https://modao.cc/agent-py/media/generated_images/2026-03-19/21bdd8ad59974a7cb41d15bf7841b460.jpg" 
            alt="邻里共享"
            class="w-64 h-48 object-cover rounded-2xl border-4 border-white shadow-[8px_8px_20px_rgba(0,0,0,0.15)] rotate-3 hover:rotate-0 transition-transform duration-300"
          />
        </div>
      </div>
    </div>
  </header>

  <section class="max-w-7xl mx-auto px-6 py-4 grid md:grid-cols-3 gap-4">
    <div class="relative">
      <input 
        v-model="searchQuery" 
        @input="handleSearchInput"
        @keyup.enter="searchItems" 
        class="w-full p-4 rounded-2xl border-2 border-gray-100 focus:border-[#E2B04D] focus:outline-none transition-colors pl-12" 
        placeholder="搜索：电钻、帐篷..."
      />
      <div class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400">
        <span class="iconify text-xl" data-icon="solar:magnifer"></span>
      </div>
    </div>
    <div class="relative">
      <select 
        v-model="selectedCommunityId" 
        @change="loadItems" 
        class="w-full p-4 pr-10 rounded-2xl border-2 border-gray-100 focus:border-[#E2B04D] focus:outline-none transition-all bg-white appearance-none cursor-pointer hover:border-gray-300 [&>option]:py-2 [&>option]:px-4"
      >
        <option value="">全部小区</option>
        <option v-for="community in communities" :key="community.id" :value="community.id">
          {{ community.name }}
        </option>
      </select>
      <div class="absolute right-4 top-1/2 -translate-y-1/2 pointer-events-none">
        <span class="iconify text-gray-400 text-xl" data-icon="solar:alt-arrow-down-bold"></span>
      </div>
    </div>
    <div class="relative">
      <select 
        v-model="selectedCategoryId" 
        @change="loadItems" 
        class="w-full p-4 pr-10 rounded-2xl border-2 border-gray-100 focus:border-[#E2B04D] focus:outline-none transition-all bg-white appearance-none cursor-pointer hover:border-gray-300 [&>option]:py-2 [&>option]:px-4"
      >
        <option value="">全部分类</option>
        <option v-for="category in categories" :key="category.id" :value="category.id">
          {{ category.name }}
        </option>
      </select>
      <div class="absolute right-4 top-1/2 -translate-y-1/2 pointer-events-none">
        <span class="iconify text-gray-400 text-xl" data-icon="solar:alt-arrow-down-bold"></span>
      </div>
    </div>
  </section>

  <main class="max-w-7xl mx-auto px-6 py-8">
    <div v-if="loading" class="text-center py-12">
      <div class="inline-block w-8 h-8 border-4 border-[#E2B04D] border-t-transparent rounded-full animate-spin"></div>
      <p class="text-gray-500 mt-4">加载中...</p>
    </div>
    <div v-else-if="items.length === 0" class="text-center py-12">
      <span class="iconify text-6xl text-gray-200" data-icon="solar:box-bold"></span>
      <p class="text-gray-500 mt-4">暂无物品</p>
      <RouterLink to="/publish" class="text-[#E2B04D] font-bold hover:underline inline-block mt-2">
        成为第一个发布者
      </RouterLink>
    </div>
    <div v-else class="grid sm:grid-cols-2 lg:grid-cols-4 gap-6">
      <article v-for="item in items" :key="item.id" class="bg-white rounded-3xl border-2 border-gray-100 overflow-hidden hover:shadow-md hover:-translate-y-1 transition-all duration-300 group">
        <div class="h-44 bg-gray-100 relative overflow-hidden">
          <img v-if="item.mainImage" :src="getImageUrl(item.mainImage)" :alt="item.name" class="rounded-t-3xl transition-transform duration-300 group-hover:scale-110" />
          <div v-else class="h-full flex items-center justify-center text-gray-400">
            <span class="iconify text-3xl mr-2" data-icon="solar:gallery-bold"></span>
            <span>无图片</span>
          </div>
        </div>
        <div class="p-5 space-y-2">
          <h3 class="font-bold text-[#333333]">{{ item.name }}</h3>
          <p class="text-sm text-gray-500">{{ item.locationText }}</p>
          <div class="flex flex-wrap gap-1">
            <template v-for="(tag, index) in displayTags(item.tags)" :key="index">
              <span class="px-2 py-1 bg-gray-100 text-xs text-gray-600 rounded-full">
                {{ tag }}
              </span>
            </template>
            <span v-if="item.tags && item.tags.length > 3" class="px-2 py-1 bg-gray-100 text-xs text-gray-400 rounded-full">
              +{{ item.tags.length - 3 }}
            </span>
          </div>
          <div class="flex justify-between items-center pt-2">
            <span class="text-[#E2B04D] font-bold">￥{{ item.pricePerDay }}/天</span>
            <RouterLink class="text-sm font-bold text-[#E2B04D] hover:underline transition-colors" :to="`/item/${item.id}`">查看详情</RouterLink>
          </div>
        </div>
      </article>
    </div>
    
    <div v-if="totalPages > 1" class="flex justify-center items-center gap-2 mt-8">
      <button 
        @click="changePage(currentPage - 1)" 
        :disabled="currentPage === 1"
        class="px-4 py-2 rounded-xl border-2 font-bold transition-all disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
        :class="currentPage === 1 ? 'border-gray-200 text-gray-400' : 'border-gray-200 text-gray-600'"
      >
        上一页
      </button>
      
      <button 
        v-for="page in displayPages" 
        :key="page"
        @click="changePage(page)"
        class="w-10 h-10 rounded-xl font-bold transition-all"
        :class="page === currentPage ? 'bg-[#E2B04D] text-white' : 'border-2 border-gray-200 hover:bg-gray-50'"
      >
        {{ page }}
      </button>
      
      <button 
        @click="changePage(currentPage + 1)" 
        :disabled="currentPage === totalPages"
        class="px-4 py-2 rounded-xl border-2 font-bold transition-all disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
        :class="currentPage === totalPages ? 'border-gray-200 text-gray-400' : 'border-gray-200 text-gray-600'"
      >
        下一页
      </button>
    </div>
  </main>

  <footer class="bg-[#2D3436] text-white mt-20 py-16 px-6">
    <div class="max-w-7xl mx-auto grid grid-cols-1 md:grid-cols-4 gap-12 text-sm">
      <div class="space-y-4">
        <div class="flex items-center space-x-2">
          <span class="iconify text-3xl text-[#E2B04D]" data-icon="solar:share-circle-bold-duotone"></span>
          <span class="text-2xl font-bold tracking-tight">邻里共享<span class="text-[#E2B04D]">.</span></span>
        </div>
        <p class="text-gray-400 leading-relaxed">2026年，让我们重新发现社区的价值。通过共享减少浪费，通过交流建立温暖连接。</p>
      </div>
      <div class="space-y-4">
        <h4 class="text-lg font-bold">平台指南</h4>
        <ul class="space-y-2 text-gray-400">
          <li><a class="hover:text-[#E2B04D]" href="#">借用流程说明</a></li>
          <li><a class="hover:text-[#E2B04D]" href="#">信用分计算规则</a></li>
          <li><a class="hover:text-[#E2B04D]" href="#">纠纷处理指南</a></li>
          <li><a class="hover:text-[#E2B04D]" href="#">免责声明</a></li>
        </ul>
      </div>
      <div class="space-y-4">
        <h4 class="text-lg font-bold">快速导航</h4>
        <ul class="space-y-2 text-gray-400">
          <li><RouterLink class="hover:text-[#E2B04D]" to="/index">发现所有物品</RouterLink></li>
          <li><RouterLink class="hover:text-[#E2B04D]" to="/forum">社区吐槽发帖</RouterLink></li>
          <li><RouterLink class="hover:text-[#E2B04D]" to="/publish">我要发布闲置</RouterLink></li>
          <li><RouterLink class="hover:text-[#E2B04D]" to="/profile">个人中心</RouterLink></li>
        </ul>
      </div>
      <div class="space-y-4">
        <h4 class="text-lg font-bold">联系我们</h4>
        <div class="flex space-x-4">
          <span class="iconify text-2xl cursor-pointer hover:text-[#E2B04D]" data-icon="ri:wechat-fill"></span>
          <span class="iconify text-2xl cursor-pointer hover:text-[#E2B04D]" data-icon="ri:weibo-fill"></span>
        </div>
        <p class="text-gray-400 mt-4">客服：400-820-2026</p>
        <p class="text-gray-500">© 2026 社区邻里共享平台</p>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import MainNav from '../components/MainNav.vue'
import { itemApi } from '../api/item'
import { publicApi, type Category, type Community } from '../api/public'
import { apiCache } from '../utils/cache'

const items = ref<any[]>([])
const loading = ref(true)
const searchQuery = ref('')
const selectedCommunityId = ref('')
const selectedCategoryId = ref('')
const communities = ref<Community[]>([])
const categories = ref<Category[]>([])

const currentPage = ref(1)
const pageSize = ref(8)
const totalPages = ref(1)
const totalElements = ref(0)

let searchTimer: any = null

const getCacheKey = () => {
  return `index_items_${currentPage.value}_${selectedCommunityId.value}_${selectedCategoryId.value}_${searchQuery.value}`
}

const loadItems = async () => {
  loading.value = true
  try {
    const params: any = {
      page: currentPage.value - 1,
      size: pageSize.value
    }
    if (selectedCommunityId.value) {
      params.communityId = selectedCommunityId.value
    }
    if (selectedCategoryId.value) {
      params.categoryId = selectedCategoryId.value
    }
    
    const res: any = await itemApi.getItems(params)
    items.value = res?.content || []
    totalPages.value = res?.totalPages || 1
    totalElements.value = res?.totalElements || 0
    currentPage.value = (res?.number || 0) + 1
  } catch (error) {
    console.error('加载物品列表失败', error)
  } finally {
    loading.value = false
  }
}

const handleSearchInput = () => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  searchTimer = setTimeout(() => {
    if (searchQuery.value.trim()) {
      searchItems()
    } else {
      loadItems()
    }
  }, 500)
}

const searchItems = async () => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  
  if (!searchQuery.value.trim()) {
    await loadItems()
    return
  }
  
  loading.value = true
  try {
    const res: any = await itemApi.searchItems(searchQuery.value)
    items.value = res?.content || []
    totalPages.value = res?.totalPages || 1
    totalElements.value = res?.totalElements || 0
  } catch (error) {
    console.error('搜索物品失败', error)
  } finally {
    loading.value = false
  }
}

const changePage = (page: number) => {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
  loadItems()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const displayPages = computed(() => {
  const pages: number[] = []
  const start = Math.max(1, currentPage.value - 2)
  const end = Math.min(totalPages.value, start + 4)
  
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  
  return pages
})

const displayTags = (tags: string[]) => {
  if (!tags) return []
  return tags.slice(0, 3)
}

const loadPublicData = async () => {
  try {
    const [communitiesData, categoriesData] = await Promise.all([
      publicApi.getCommunities(),
      publicApi.getCategories()
    ])
    communities.value = communitiesData
    categories.value = categoriesData
  } catch (error) {
    console.error('加载公共数据失败', error)
  }
}

const getImageUrl = (path: string) => {
  if (path.startsWith('http')) {
    return path
  }
  return `http://localhost:8080/uploads/${path}`
}

onMounted(async () => {
  await loadPublicData()
  await loadItems()
})
</script>
