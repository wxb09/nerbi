<template>
  <MainNav />
  <header class="max-w-7xl mx-auto px-6 pt-12 pb-8">
    <div class="bg-[#F3EFEA] rounded-[2rem] p-10 border border-[#E2B04D]/20">
      <h1 class="text-4xl font-bold text-[#2D3436]">让社区物品<span class="text-[#E2B04D]">自由流动</span></h1>
      <p class="text-gray-600 mt-4">先完成页面与跳转，接口后续接入。</p>
      <RouterLink class="inline-block mt-6 bg-[#E2B04D] text-white px-6 py-3 rounded-2xl font-bold" to="/publish">我也要发布</RouterLink>
    </div>
  </header>

  <section class="max-w-7xl mx-auto px-6 py-4 grid md:grid-cols-3 gap-4">
    <div class="relative">
      <input 
        v-model="searchQuery" 
        @keyup.enter="searchItems" 
        class="w-full p-4 rounded-2xl border-2 border-gray-100 focus:border-[#E2B04D] focus:outline-none transition-colors" 
        placeholder="搜索：电钻、帐篷..."
      />
      <button @click="searchItems" class="absolute right-4 top-1/2 -translate-y-1/2 text-gray-400 hover:text-[#E2B04D] transition-colors">
        🔍
      </button>
    </div>
    <select 
      v-model="selectedCommunityId" 
      @change="loadItems" 
      class="w-full p-4 rounded-2xl border-2 border-gray-100 focus:border-[#E2B04D] focus:outline-none transition-colors bg-white"
    >
      <option value="">全部小区</option>
      <option v-for="community in communities" :key="community.id" :value="community.id">
        {{ community.name }}
      </option>
    </select>
    <select 
      v-model="selectedCategoryId" 
      @change="loadItems" 
      class="w-full p-4 rounded-2xl border-2 border-gray-100 focus:border-[#E2B04D] focus:outline-none transition-colors bg-white"
    >
      <option value="">全部分类</option>
      <option v-for="category in categories" :key="category.id" :value="category.id">
        {{ category.name }}
      </option>
    </select>
  </section>

  <main class="max-w-7xl mx-auto px-6 py-8 grid sm:grid-cols-2 lg:grid-cols-4 gap-6">
    <div v-if="loading" class="col-span-full text-center py-12">
      <div class="inline-block w-8 h-8 border-4 border-[#E2B04D] border-t-transparent rounded-full animate-spin"></div>
      <p class="text-gray-500 mt-4">加载中...</p>
    </div>
    <div v-else-if="items.length === 0" class="col-span-full text-center py-12">
      <p class="text-gray-500">暂无物品</p>
    </div>
    <article v-for="item in items" :key="item.id" class="bg-white rounded-3xl border-2 border-gray-100 overflow-hidden hover:shadow-md transition-shadow">
      <div class="h-44 bg-gray-100 flex items-center justify-center">
        <img v-if="item.mainImage" :src="getImageUrl(item.mainImage)" :alt="item.name" class="h-full w-full object-cover rounded-t-3xl" />
        <div v-else class="text-gray-400">无图片</div>
      </div>
      <div class="p-5 space-y-2">
        <h3 class="font-bold text-[#333333]">{{ item.name }}</h3>
        <p class="text-sm text-gray-500">{{ item.locationText }}</p>
        <div class="flex flex-wrap gap-1">
          <span v-for="(tag, index) in item.tags" :key="index" class="px-2 py-1 bg-gray-100 text-xs text-gray-600 rounded-full">
            {{ tag }}
          </span>
        </div>
        <div class="flex justify-between items-center pt-2">
          <span class="text-[#E2B04D] font-bold">￥{{ item.pricePerDay }}/天</span>
          <RouterLink class="text-sm font-bold text-[#E2B04D] hover:underline transition-colors" :to="`/item/${item.id}`">查看详情</RouterLink>
        </div>
      </div>
    </article>
  </main>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import MainNav from '../components/MainNav.vue'
import { itemApi } from '../api/item'
import { publicApi, type Category, type Community } from '../api/public'

const items = ref<any[]>([])
const loading = ref(true)
const searchQuery = ref('')
const selectedCommunityId = ref('')
const selectedCategoryId = ref('')
const communities = ref<Community[]>([])
const categories = ref<Category[]>([])

const loadItems = async () => {
  loading.value = true
  try {
    const params: any = {}
    if (selectedCommunityId.value) {
      params.communityId = selectedCommunityId.value
    }
    if (selectedCategoryId.value) {
      params.categoryId = selectedCategoryId.value
    }
    
    const res = await itemApi.getItems(params)
    items.value = res?.content || []
  } catch (error) {
    console.error('加载物品列表失败', error)
  } finally {
    loading.value = false
  }
}

const searchItems = async () => {
  if (!searchQuery.value.trim()) {
    await loadItems()
    return
  }
  
  loading.value = true
  try {
    const res = await itemApi.searchItems(searchQuery.value)
    items.value = res?.content || []
  } catch (error) {
    console.error('搜索物品失败', error)
  } finally {
    loading.value = false
  }
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
