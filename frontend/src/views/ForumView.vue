<template>
  <MainNav />
  <main class="max-w-7xl mx-auto px-6 py-12 animate-fadeIn">
    <div class="grid lg:grid-cols-3 gap-8">
      <section class="lg:col-span-2 space-y-8">
        <div class="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
          <h1 class="text-3xl font-bold italic text-[#2D3436]">
            情感联结流
            <span class="text-sm not-italic font-normal text-gray-400 ml-2">社区论坛</span>
          </h1>
          <PostFilter
            :active-tab="activeTab"
            :active-type="activeType"
            @switch-tab="switchTab"
            @toggle-type="toggleType"
          />
        </div>

        <div class="bg-white border-2 border-dashed border-gray-200 p-6 rounded-[2rem] flex items-center space-x-4">
          <img
            :src="authStore.user?.avatar || defaultAvatar"
            class="w-12 h-12 rounded-full border border-gray-100 flex-shrink-0"
            alt="User avatar"
          />
          <input
            v-model="newPostContent"
            class="flex-1 text-left py-3 px-6 bg-gray-50 text-gray-400 rounded-2xl hover:bg-gray-100 transition-all focus:bg-white focus:text-gray-700 focus:outline-none focus:ring-2 focus:ring-[#E2B04D]"
            placeholder="分享今天的邻里小故事或是求助..."
            @keyup.enter="publishPost"
          />
          <div class="flex gap-4">
            <span class="iconify text-2xl text-gray-300 hover:text-[#E2B04D] cursor-pointer transition-colors" data-icon="solar:camera-bold" @click="showPostEditor = true"></span>
          </div>
        </div>

        <div v-if="loading && posts.length === 0" class="text-center py-20 text-gray-400">
          <span class="iconify text-4xl animate-spin inline-block" data-icon="solar:refresh-bold"></span>
          <p class="mt-2">加载中...</p>
        </div>

        <div v-else-if="posts.length === 0" class="text-center py-20 text-gray-400">
          <span class="iconify text-5xl" data-icon="solar:chat-round-dots-bold"></span>
          <p class="mt-4 text-lg">还没有帖子，快来发布第一条吧！</p>
        </div>

        <div v-else class="space-y-6">
          <PostCard
            v-for="post in posts"
            :key="post.id"
            :post="post"
            :default-avatar="defaultAvatar"
            @click="goToDetail"
            @like="toggleLike"
          />
        </div>

        <div v-if="posts.length > 0" class="text-center py-10">
          <button
            v-if="hasMore"
            class="bg-[#2D3436] text-white px-8 py-3 rounded-full font-bold text-sm hover:scale-105 transition-all"
            :disabled="loading"
            @click="loadMore"
          >
            {{ loading ? '加载中...' : '加载更多邻里动态' }}
          </button>
          <p v-else class="text-gray-400 text-sm">没有更多了</p>
        </div>
      </section>

      <ForumSidebar @search-tag="searchByTag" />
    </div>

    <PostEditor
      v-if="showPostEditor"
      @close="showPostEditor = false"
      @published="onPostPublished"
    />
  </main>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import MainNav from '../components/MainNav.vue'
import PostEditor from '../components/PostEditor.vue'
import PostCard from '../components/forum/PostCard.vue'
import PostFilter from '../components/forum/PostFilter.vue'
import ForumSidebar from '../components/forum/ForumSidebar.vue'
import { forumApi, type PostList } from '../api/forum'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const defaultAvatar = 'https://modao.cc/agent-py/media/generated_images/2026-03-19/7a697da7cb0e46808f265a42ae7934f3.jpg'

const activeTab = ref('latest')
const activeType = ref<string | null>(null)
const posts = ref<PostList[]>([])
const loading = ref(false)
const currentPage = ref(0)
const hasMore = ref(true)
const newPostContent = ref('')
const showPostEditor = ref(false)

const switchTab = (tab: string) => {
  activeTab.value = tab
  activeType.value = null
  reloadPosts()
}

const toggleType = (type: string) => {
  activeType.value = activeType.value === type ? null : type
  reloadPosts()
}

const reloadPosts = () => {
  posts.value = []
  currentPage.value = 0
  hasMore.value = true
  loadPosts()
}

const loadPosts = async () => {
  loading.value = true
  try {
    const params: any = {
      sort: activeTab.value,
      page: currentPage.value,
      size: 10
    }
    if (activeType.value) {
      params.type = activeType.value
    }
    const res = await forumApi.getPosts(params)
    const pageData = res as any
    const newPosts = pageData.content || []
    if (currentPage.value === 0) {
      posts.value = newPosts
    } else {
      posts.value.push(...newPosts)
    }
    hasMore.value = !pageData.last
  } catch (error) {
    console.error('加载帖子失败', error)
  } finally {
    loading.value = false
  }
}

const loadMore = () => {
  currentPage.value++
  loadPosts()
}

const toggleLike = async (post: PostList) => {
  if (!authStore.isLoggedIn) {
    router.push('/login')
    return
  }
  try {
    const res = await forumApi.toggleLike('POST', post.id) as any
    post.likeCount = res.likeCount
  } catch (error) {
    console.error('点赞失败', error)
  }
}

const publishPost = async () => {
  if (!authStore.isLoggedIn) {
    router.push('/login')
    return
  }
  if (!newPostContent.value.trim()) return

  try {
    await forumApi.createPost({
      content: newPostContent.value.trim(),
      type: 'NORMAL'
    })
    newPostContent.value = ''
    reloadPosts()
  } catch (error) {
    console.error('发布失败', error)
  }
}

const onPostPublished = () => {
  showPostEditor.value = false
  reloadPosts()
}

const goToDetail = (postId: number) => {
  router.push(`/forum/post/${postId}`)
}

const searchByTag = (tag: string) => {
  newPostContent.value = ''
  reloadPosts()
}

onMounted(() => {
  loadPosts()
})
</script>
