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
    <input class="p-4 rounded-2xl border-2 border-gray-100" placeholder="搜索：电钻、帐篷..." />
    <select class="p-4 rounded-2xl border-2 border-gray-100">
      <option value="">全部小区</option>
      <option value="1">栖霞苑小区</option>
      <option value="2">阳光花园</option>
      <option value="3">翠湖新城</option>
    </select>
    <select class="p-4 rounded-2xl border-2 border-gray-100"><option>全部领域</option></select>
  </section>

  <main class="max-w-7xl mx-auto px-6 py-8 grid sm:grid-cols-2 lg:grid-cols-4 gap-6">
    <div v-if="loading" class="col-span-full text-center py-12">
      <p class="text-gray-500">加载中...</p>
    </div>
    <div v-else-if="items.length === 0" class="col-span-full text-center py-12">
      <p class="text-gray-500">暂无物品</p>
    </div>
    <article v-for="item in items" :key="item.id" class="bg-white rounded-3xl border-2 border-gray-100 overflow-hidden">
      <div class="h-44 bg-gray-100 flex items-center justify-center">
        <img v-if="item.mainImage" :src="item.mainImage" alt="{{ item.name }}" class="h-full w-full object-cover rounded-t-3xl" />
        <div v-else class="text-gray-400">无图片</div>
      </div>
      <div class="p-5 space-y-2">
        <h3 class="font-bold">{{ item.name }}</h3>
        <p class="text-sm text-gray-500">{{ item.communityName }} {{ item.building }}</p>
        <div class="flex justify-between items-center pt-2">
          <span class="text-[#E2B04D] font-bold">￥{{ item.pricePerDay }}/天</span>
          <RouterLink class="text-sm font-bold text-[#E2B04D]" :to="`/item/${item.id}`">查看详情</RouterLink>
        </div>
      </div>
    </article>
  </main>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import MainNav from '../components/MainNav.vue'
import { itemApi } from '../api/item'

const items = ref<any[]>([])
const loading = ref(true)

const loadItems = async () => {
  try {
    const res = await itemApi.getItems()
    items.value = res?.content || []
  } catch (error) {
    console.error('加载物品列表失败', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadItems()
})
</script>
