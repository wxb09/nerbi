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

        <div class="bg-white border-2 border-dashed border-gray-200 p-6 rounded-[2rem]">
          <div class="flex items-center space-x-4 mb-4">
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

          <div class="flex flex-wrap items-center gap-3 pt-4 border-t border-gray-100">
            <div class="flex flex-wrap gap-2">
              <button
                v-for="t in postTypes"
                :key="t.value"
                class="px-3 py-1.5 rounded-full text-xs font-bold transition-all"
                :class="newPostType === t.value ? 'bg-[#E2B04D] text-white shadow-md' : 'bg-white border border-gray-200 text-gray-500 hover:border-[#E2B04D] hover:text-[#E2B04D]'"
                @click="newPostType = t.value"
              >
                {{ t.label }}
              </button>
            </div>

            <div class="h-4 w-px bg-gray-200 mx-1 hidden sm:block"></div>

            <div class="flex flex-wrap items-center gap-2 flex-1">
              <button
                v-for="tag in availableTags"
                :key="tag"
                class="px-3 py-1.5 rounded-full text-xs font-medium transition-all"
                :class="selectedTags.includes(tag) ? 'bg-[#E2B04D] text-white shadow-md' : 'bg-white border border-gray-200 text-gray-500 hover:border-[#E2B04D] hover:text-[#E2B04D]'"
                @click="toggleTag(tag)"
              >
                #{{ tag }}
              </button>
              <div class="flex items-center">
                <input
                  v-model="customTagInput"
                  type="text"
                  class="w-20 px-3 py-1.5 rounded-full text-xs border border-dashed border-gray-300 focus:border-[#E2B04D] focus:outline-none transition-all"
                  placeholder="自定义"
                  @keyup.enter="addCustomTag"
                />
                <button
                  v-if="customTagInput.trim()"
                  class="ml-1 w-6 h-6 rounded-full bg-[#E2B04D] text-white flex items-center justify-center text-xs hover:scale-110 transition-transform"
                  @click="addCustomTag"
                >
                  +
                </button>
              </div>
            </div>

            <button
              class="px-5 py-2 rounded-full text-xs font-bold bg-[#2D3436] text-white hover:scale-105 transition-all disabled:opacity-50 disabled:hover:scale-100"
              :disabled="!newPostContent.trim() || publishing"
              @click="publishPost"
            >
              {{ publishing ? '发布中...' : '发布' }}
            </button>
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
            @delete="handleDeletePost"
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

    <Transition name="fade">
      <button
        v-if="showBackToTop"
        class="fixed bottom-8 lg:right-[calc((100%-1280px)/2+1280px/6-20px)] right-6 w-12 h-12 bg-[#2D3436] text-white rounded-full shadow-lg hover:bg-[#E2B04D] transition-all hover:scale-110 flex items-center justify-center z-50"
        @click="scrollToTop"
      >
        <span class="iconify text-xl" data-icon="solar:alt-arrow-up-bold"></span>
      </button>
    </Transition>
  </main>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import MainNav from '../components/MainNav.vue'
import PostEditor from '../components/PostEditor.vue'
import PostCard from '../components/forum/PostCard.vue'
import PostFilter from '../components/forum/PostFilter.vue'
import ForumSidebar from '../components/forum/ForumSidebar.vue'
import { forumApi, type PostList } from '../api/forum'
import { useAuthStore } from '../stores/auth'
import { usePagination } from '../composables/usePagination'

const router = useRouter()
const authStore = useAuthStore()

const defaultAvatar = 'https://modao.cc/agent-py/media/generated_images/2026-03-19/7a697da7cb0e46808f265a42ae7934f3.jpg'

const activeTab = ref('latest')
const activeType = ref<string | null>(null)
const showPostEditor = ref(false)
const showBackToTop = ref(false)

const newPostContent = ref('')
const newPostType = ref('NORMAL')
const selectedTags = ref<string[]>([])
const customTagInput = ref('')
const publishing = ref(false)

const postTypes = [
  { value: 'NORMAL', label: '日常' },
  { value: 'THANKS', label: '感谢' },
  { value: 'HELP', label: '求助' },
]

const availableTags = ref([
  '邻里互助',
  '绿色生活',
  '周末活动',
])

const toggleTag = (tag: string) => {
  const idx = selectedTags.value.indexOf(tag)
  if (idx >= 0) {
    selectedTags.value.splice(idx, 1)
  } else {
    selectedTags.value.push(tag)
  }
}

const addCustomTag = () => {
  const tag = customTagInput.value.trim()
  if (!tag) return
  if (!availableTags.value.includes(tag)) {
    availableTags.value.unshift(tag)
  }
  if (!selectedTags.value.includes(tag)) {
    selectedTags.value.push(tag)
  }
  customTagInput.value = ''
}

const params = computed(() => ({
  sort: activeTab.value,
  type: activeType.value || undefined
}))

const {
  loading,
  error,
  data: posts,
  hasMore,
  isEmpty,
  loadMore,
  refresh,
  clearCache
} = usePagination<PostList>(
  (page, size, p) => forumApi.getPosts({ ...p, page, size }),
  { pageSize: 10, mode: 'append', params, cacheKey: 'forum_posts', cacheTTL: 2 * 60 * 1000 }
)

const reloadPosts = () => refresh()

const switchTab = (tab: string) => {
  activeTab.value = tab
  activeType.value = null
  reloadPosts()
}

const toggleType = (type: string) => {
  activeType.value = activeType.value === type ? null : type
  reloadPosts()
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

  publishing.value = true
  try {
    await forumApi.createPost({
      content: newPostContent.value.trim(),
      type: newPostType.value,
      tags: selectedTags.value.length > 0 ? selectedTags.value : undefined
    })
    newPostContent.value = ''
    newPostType.value = 'NORMAL'
    selectedTags.value = []
    clearCache()
    reloadPosts()
  } catch (error) {
    console.error('发布失败', error)
  } finally {
    publishing.value = false
  }
}

const onPostPublished = () => {
  showPostEditor.value = false
  clearCache()
  reloadPosts()
}

const goToDetail = (postId: number) => {
  router.push(`/forum/post/${postId}`)
}

const searchByTag = (tag: string) => {
  newPostContent.value = ''
  reloadPosts()
}

const handleDeletePost = async (post: PostList) => {
  try {
    await forumApi.deletePost(post.id)
    clearCache()
    reloadPosts()
  } catch (error) {
    console.error('删除帖子失败', error)
  }
}

watch([activeTab, activeType], () => {
  reloadPosts()
})

const handleScroll = () => {
  showBackToTop.value = window.scrollY > 300
}

const scrollToTop = () => {
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  })
}

onMounted(() => {
  refresh()
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(20px);
}
</style>
