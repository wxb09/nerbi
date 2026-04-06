<template>
  <MainNav />
  <main class="max-w-7xl mx-auto px-6 py-8">
    <!-- 顶部标题 -->
    <div class="flex items-center justify-between mb-8">
      <div class="flex items-center gap-4">
        <button @click="goBack" class="text-gray-400 hover:text-gray-600 transition-colors">
          <span class="iconify text-2xl" data-icon="solar:alt-arrow-left-bold"></span>
        </button>
        <div>
          <h1 class="text-2xl font-bold">草稿箱</h1>
          <p class="text-sm text-gray-500 mt-1">
            {{ drafts.length }} 个草稿
          </p>
        </div>
      </div>
      <RouterLink 
        to="/publish" 
        class="px-6 py-3 bg-[#E2B04D] text-white rounded-2xl font-bold hover:bg-[#C49A2E] transition-colors"
      >
        发布新物品
      </RouterLink>
    </div>

    <!-- 草稿列表 -->
    <div v-if="loading" class="text-center py-12">
      <div class="inline-block w-8 h-8 border-4 border-[#E2B04D] border-t-transparent rounded-full animate-spin"></div>
      <p class="text-gray-500 mt-4">加载中...</p>
    </div>

    <div v-else-if="drafts.length === 0" class="text-center py-12">
      <span class="iconify text-6xl text-gray-200" data-icon="solar:file-bold"></span>
      <p class="text-gray-500 mt-4">暂无草稿</p>
      <RouterLink to="/publish" class="text-[#E2B04D] font-bold hover:underline inline-block mt-2">
        去发布第一个物品
      </RouterLink>
    </div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div 
        v-for="draft in drafts" 
        :key="draft.id" 
        class="bg-white rounded-3xl border border-gray-100 overflow-hidden hover:shadow-md transition-all"
      >
        <!-- 图片 -->
        <div class="h-48 bg-gray-100 relative">
          <img 
            v-if="draft.image" 
            :src="getImageUrl(draft.image)" 
            :alt="draft.name" 
            class="w-full h-full object-cover"
          />
          <div v-else class="flex items-center justify-center h-full">
            <span class="iconify text-4xl text-gray-300" data-icon="solar:box-bold"></span>
          </div>
          <span class="absolute top-3 right-3 px-3 py-1 bg-gray-100 text-gray-600 rounded-full text-xs font-bold">
            草稿
          </span>
        </div>

        <!-- 内容 -->
        <div class="p-5">
          <h3 class="font-bold text-lg mb-2 truncate">{{ draft.name }}</h3>
          <p class="text-sm text-gray-500 mb-3 line-clamp-2">{{ draft.description }}</p>
          <div class="flex items-center justify-between text-xs text-gray-400 mb-4">
            <span>分类：{{ draft.categoryName || '未选择' }}</span>
            <span>最后编辑：{{ formatTimeAgo(draft.updatedAt) }}</span>
          </div>

          <!-- 操作按钮 -->
          <div class="flex gap-2">
            <button 
              @click="editDraft(draft)" 
              class="flex-1 py-2.5 bg-[#E2B04D] text-white rounded-xl font-bold text-sm hover:bg-[#C49A2E] transition-colors"
            >
              继续编辑
            </button>
            <button 
              @click="deleteDraft(draft)" 
              class="px-4 py-2.5 border-2 border-red-200 text-red-500 rounded-xl font-bold text-sm hover:bg-red-50 transition-colors"
            >
              删除
            </button>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import MainNav from '../components/MainNav.vue'
import { itemApi } from '../api/item'

interface Draft {
  id: number
  name: string
  description: string
  categoryId: number
  categoryName?: string
  image?: string
  updatedAt: string
}

const router = useRouter()
const drafts = ref<Draft[]>([])
const loading = ref(true)

const goBack = () => {
  router.back()
}

const loadDrafts = async () => {
  loading.value = true
  try {
    drafts.value = await itemApi.getMyDrafts()
  } catch (error) {
    console.error('加载草稿失败', error)
  } finally {
    loading.value = false
  }
}

const editDraft = (draft: Draft) => {
  router.push(`/publish/${draft.id}`)
}

const deleteDraft = async (draft: Draft) => {
  if (!confirm(`确认要删除"${draft.name}"吗？删除后无法恢复。`)) return
  
  try {
    await itemApi.deleteItem(draft.id)
    alert('删除成功')
    loadDrafts()
  } catch (error: any) {
    alert(error.message || '删除失败')
  }
}

const formatTimeAgo = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const hours = Math.floor(diff / (1000 * 60 * 60))
  
  if (hours < 1) return '刚刚'
  if (hours < 24) return `${hours}小时前`
  const days = Math.floor(hours / 24)
  if (days < 7) return `${days}天前`
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

const getImageUrl = (path: string) => {
  if (!path) return ''
  if (path.startsWith('http')) {
    return path
  }
  // 如果已经有 /uploads/ 前缀，直接返回
  if (path.startsWith('/uploads/')) {
    return path
  }
  // 否则添加 /uploads/ 前缀
  return `/uploads/${path}`
}

onMounted(() => {
  loadDrafts()
})
</script>
