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
          <div class="flex gap-2">
            <button
              class="px-4 py-2 bg-white border border-gray-200 rounded-full text-xs font-bold hover:bg-gray-50 transition-all"
              :class="activeTab === 'latest' ? 'bg-[#E2B04D] text-white border-[#E2B04D] shadow-lg shadow-[#E2B04D]/20' : ''"
              @click="switchTab('latest')"
            >
              最新
            </button>
            <button
              class="px-4 py-2 rounded-full text-xs font-bold transition-all"
              :class="activeTab === 'hot' ? 'bg-[#E2B04D] text-white shadow-lg shadow-[#E2B04D]/20' : 'bg-white border border-gray-200 hover:bg-gray-50'"
              @click="switchTab('hot')"
            >
              最热
            </button>
            <button
              class="px-4 py-2 rounded-full text-xs font-bold transition-all"
              :class="activeType === 'THANKS' ? 'bg-orange-400 text-white shadow-lg' : 'bg-white border border-gray-200 hover:bg-gray-50'"
              @click="toggleType('THANKS')"
            >
              感谢信
            </button>
            <button
              class="px-4 py-2 rounded-full text-xs font-bold transition-all"
              :class="activeType === 'HELP' ? 'bg-blue-400 text-white shadow-lg' : 'bg-white border border-gray-200 hover:bg-gray-50'"
              @click="toggleType('HELP')"
            >
              求助
            </button>
          </div>
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
          <article
            v-for="post in posts"
            :key="post.id"
            class="p-8 rounded-[2rem] post-card transition-all duration-300 cursor-pointer"
            :class="post.type === 'THANKS' ? 'bg-orange-50/50 border border-orange-100' : 'bg-white border border-gray-100 shadow-sm'"
            @click="goToDetail(post.id)"
          >
            <div class="flex items-center justify-between mb-6">
              <div class="flex items-center space-x-4">
                <img
                  :src="post.author?.avatar || defaultAvatar"
                  class="w-10 h-10 rounded-full border-2 border-white shadow-sm"
                  :alt="post.author?.nickname"
                />
                <div>
                  <h3 class="font-bold text-sm text-[#2D3436]">{{ post.author?.nickname || '匿名用户' }}</h3>
                  <p class="text-[10px] text-gray-400 font-medium">
                    {{ formatTime(post.createdAt) }}
                    <span v-if="post.author?.building"> · {{ post.author.building }}</span>
                    <span v-if="post.communityName"> · {{ post.communityName }}</span>
                  </p>
                </div>
              </div>
              <span
                class="px-3 py-1 rounded-full text-[10px] font-bold uppercase tracking-widest shadow-sm"
                :class="getPostTypeClass(post.type)"
              >
                # {{ getPostTypeText(post.type) }}
              </span>
            </div>

            <h4 v-if="post.title" class="font-bold text-lg mb-3 text-[#2D3436]">{{ post.title }}</h4>

            <p
              class="leading-relaxed mb-6"
              :class="post.type === 'THANKS' ? 'text-gray-700 font-medium' : 'text-gray-600'"
              v-html="formatContent(post.content)"
            ></p>

            <div v-if="post.images" class="flex gap-2 mb-4 overflow-x-auto">
              <img
                v-for="(img, idx) in parseImages(post.images)"
                :key="idx"
                :src="img"
                class="w-24 h-24 rounded-xl object-cover flex-shrink-0"
                alt="帖子图片"
              />
            </div>

            <div class="flex items-center space-x-6" :class="post.type === 'THANKS' ? 'text-gray-400' : 'text-gray-300'">
              <button
                @click.stop="toggleLike(post)"
                class="flex items-center space-x-1 hover:text-red-400 transition-colors group"
              >
                <span class="iconify text-xl transition-transform group-hover:scale-125" :data-icon="post.likedByMe ? 'solar:heart-bold' : 'solar:heart-linear'"></span>
                <span class="text-xs font-bold">{{ post.likeCount }}</span>
              </button>
              <button class="flex items-center space-x-1 hover:text-[#E2B04D] transition-colors group">
                <span class="iconify text-xl transition-transform group-hover:scale-125" data-icon="solar:chat-round-dots-bold"></span>
                <span class="text-xs font-bold">{{ post.commentCount }}</span>
              </button>
              <span class="flex items-center space-x-1 text-gray-300">
                <span class="iconify text-xl" data-icon="solar:eye-bold"></span>
                <span class="text-xs font-bold">{{ post.viewCount }}</span>
              </span>
            </div>
          </article>
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

      <aside class="space-y-6">
        <div class="bg-gradient-to-br from-[#2D3436] to-[#3d4648] text-white p-6 rounded-3xl shadow-lg">
          <div class="flex items-center gap-2 mb-4">
            <span class="iconify text-xl text-[#E2B04D]" data-icon="solar:megaphone-bold"></span>
            <h3 class="font-bold text-lg">社区公告</h3>
          </div>
          <p class="text-sm text-gray-300 leading-relaxed">
            邻里借阅规则 2026 版更新。请大家遵守规则，共建和谐社区！
          </p>
          <div class="mt-4 pt-4 border-t border-gray-700">
            <p class="text-xs text-gray-400">发布于 2026-03-28</p>
          </div>
        </div>

        <div class="bg-white p-6 rounded-3xl border border-gray-100 shadow-sm">
          <div class="flex items-center gap-2 mb-4">
            <span class="iconify text-xl text-[#E2B04D]" data-icon="solar:hashtag-bold"></span>
            <h3 class="font-bold text-lg text-[#2D3436]">热门话题</h3>
          </div>
          <div class="flex flex-wrap gap-2">
            <span
              v-for="tag in hotTags"
              :key="tag"
              class="px-4 py-2 bg-[#F5E6C8] text-[#E2B04D] rounded-full text-sm font-medium hover:bg-[#E2B04D] hover:text-white transition-colors cursor-pointer"
              @click="searchByTag(tag)"
            >
              #{{ tag }}
            </span>
          </div>
        </div>
      </aside>
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

const hotTags = ref([
  '邻里互助',
  '绿色生活',
  '周末活动',
  '闲置共享',
  '社区新闻'
])

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

const getPostTypeClass = (type: string) => {
  const classes: Record<string, string> = {
    'THANKS': 'bg-white text-orange-600',
    'HELP': 'bg-blue-50 text-blue-600',
    'EXCHANGE': 'bg-green-50 text-green-600',
    'NORMAL': 'bg-gray-100 text-gray-600'
  }
  return classes[type] || 'bg-gray-100 text-gray-600'
}

const getPostTypeText = (type: string) => {
  const texts: Record<string, string> = {
    'THANKS': '邻里感谢信',
    'HELP': '物品求助',
    'EXCHANGE': '技能交换',
    'NORMAL': '社区动态'
  }
  return texts[type] || '社区动态'
}

const formatContent = (content: string) => {
  return content.replace(/@(\S+)/g, '<span class="text-[#E2B04D] font-bold">@$1</span>')
}

const formatTime = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}小时前`
  const days = Math.floor(hours / 24)
  if (days < 30) return `${days}天前`
  return date.toLocaleDateString()
}

const parseImages = (images: string | null) => {
  if (!images) return []
  return images.split(',').filter(s => s.trim())
}

onMounted(() => {
  loadPosts()
})
</script>

<style scoped>
.post-card {
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
.post-card:hover {
  transform: translateX(10px);
  background: white;
}
</style>
