<template>
  <MainNav />
  <main class="max-w-3xl mx-auto px-6 py-12 animate-fadeIn">
    <div v-if="loading" class="text-center py-20 text-gray-400">
      <span class="iconify text-4xl animate-spin inline-block" data-icon="solar:refresh-bold"></span>
      <p class="mt-2">加载中...</p>
    </div>

    <div v-else-if="post" class="space-y-8">
      <button class="text-sm text-gray-400 hover:text-[#E2B04D] transition-colors" @click="router.back()">
        <span class="iconify inline-block mr-1" data-icon="solar:arrow-left-bold"></span>
        返回论坛
      </button>

      <article class="bg-white p-8 rounded-[2rem] border border-gray-100 shadow-sm">
        <div class="flex items-center justify-between mb-6">
          <div class="flex items-center space-x-4">
            <img
              :src="post.author?.avatar || defaultAvatar"
              class="w-12 h-12 rounded-full border-2 border-white shadow-sm"
              :alt="post.author?.nickname"
            />
            <div>
              <h3 class="font-bold text-[#2D3436]">{{ post.author?.nickname || '匿名用户' }}</h3>
              <p class="text-xs text-gray-400">
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

        <h1 v-if="post.title" class="text-2xl font-bold mb-4 text-[#2D3436]">{{ post.title }}</h1>

        <div
          class="leading-relaxed text-gray-600 mb-6 whitespace-pre-wrap"
          v-html="formatContent(post.content)"
        ></div>

        <div v-if="post.images && post.images.length > 0" class="grid grid-cols-3 gap-3 mb-6">
          <img
            v-for="(img, idx) in post.images"
            :key="idx"
            :src="img"
            class="w-full aspect-square rounded-2xl object-cover"
            alt="帖子图片"
          />
        </div>

        <div v-if="post.tags && post.tags.length > 0" class="flex flex-wrap gap-2 mb-6">
          <span
            v-for="tag in post.tags"
            :key="tag"
            class="px-3 py-1 bg-[#F5E6C8] text-[#E2B04D] rounded-full text-xs font-medium"
          >
            #{{ tag }}
          </span>
        </div>

        <div class="flex items-center space-x-8 text-gray-300 pt-4 border-t border-gray-100">
          <button
            @click="toggleLike"
            class="flex items-center space-x-2 hover:text-red-400 transition-colors group"
          >
            <span class="iconify text-2xl transition-transform group-hover:scale-125" :data-icon="post.likedByMe ? 'solar:heart-bold' : 'solar:heart-linear'"></span>
            <span class="text-sm font-bold" :class="post.likedByMe ? 'text-red-400' : ''">{{ post.likeCount }}</span>
          </button>
          <span class="flex items-center space-x-2">
            <span class="iconify text-2xl" data-icon="solar:chat-round-dots-bold"></span>
            <span class="text-sm font-bold">{{ post.commentCount }}</span>
          </span>
          <span class="flex items-center space-x-2">
            <span class="iconify text-2xl" data-icon="solar:eye-bold"></span>
            <span class="text-sm font-bold">{{ post.viewCount }}</span>
          </span>
        </div>
      </article>

      <section class="bg-white p-8 rounded-[2rem] border border-gray-100 shadow-sm">
        <h3 class="text-lg font-bold text-[#2D3436] mb-6">评论 ({{ post.commentCount }})</h3>

        <div v-if="authStore.isLoggedIn" class="flex items-start gap-3 mb-8">
          <img
            :src="authStore.user?.avatar || defaultAvatar"
            class="w-10 h-10 rounded-full border border-gray-100 flex-shrink-0"
            alt="我的头像"
          />
          <div class="flex-1">
            <textarea
              v-model="newComment"
              rows="3"
              class="w-full py-3 px-4 bg-gray-50 rounded-2xl focus:bg-white focus:outline-none focus:ring-2 focus:ring-[#E2B04D] text-sm resize-none"
              placeholder="写下你的评论..."
            ></textarea>
            <div class="flex justify-end mt-2">
              <button
                class="px-6 py-2 bg-[#E2B04D] text-white rounded-full text-sm font-bold hover:bg-[#d4a045] transition-all disabled:opacity-50"
                :disabled="!newComment.trim() || submittingComment"
                @click="submitComment"
              >
                {{ submittingComment ? '发送中...' : '发送' }}
              </button>
            </div>
          </div>
        </div>

        <div v-else class="text-center py-6 mb-6 bg-gray-50 rounded-2xl">
          <p class="text-gray-400 text-sm">
            <RouterLink to="/login" class="text-[#E2B04D] font-bold">登录</RouterLink> 后可以发表评论
          </p>
        </div>

        <div v-if="comments.length === 0" class="text-center py-10 text-gray-400">
          <p>暂无评论，来说两句吧~</p>
        </div>

        <div v-else class="space-y-6">
          <div v-for="comment in comments" :key="comment.id" class="group">
            <div class="flex items-start gap-3">
              <img
                :src="comment.author?.avatar || defaultAvatar"
                class="w-9 h-9 rounded-full border border-gray-100 flex-shrink-0"
                :alt="comment.author?.nickname"
              />
              <div class="flex-1">
                <div class="flex items-center gap-2 mb-1">
                  <span class="font-bold text-sm text-[#2D3436]">{{ comment.author?.nickname || '匿名' }}</span>
                  <span class="text-[10px] text-gray-400">{{ formatTime(comment.createdAt) }}</span>
                </div>
                <p class="text-sm text-gray-600 leading-relaxed">{{ comment.content }}</p>
                <div class="flex items-center gap-4 mt-2 text-gray-300">
                  <button
                    @click="toggleCommentLike(comment)"
                    class="flex items-center gap-1 hover:text-red-400 transition-colors text-xs"
                  >
                    <span class="iconify" :data-icon="comment.likedByMe ? 'solar:heart-bold' : 'solar:heart-linear'"></span>
                    {{ comment.likeCount }}
                  </button>
                  <button
                    v-if="authStore.isLoggedIn"
                    class="hover:text-[#E2B04D] transition-colors text-xs"
                    @click="replyTo = comment.id"
                  >
                    回复
                  </button>
                </div>

                <div v-if="replyTo === comment.id" class="mt-3 flex items-start gap-2">
                  <input
                    v-model="replyContent"
                    class="flex-1 py-2 px-4 bg-gray-50 rounded-full text-sm focus:bg-white focus:outline-none focus:ring-2 focus:ring-[#E2B04D]"
                    :placeholder="`回复 ${comment.author?.nickname}...`"
                    @keyup.enter="submitReply(comment.id)"
                  />
                  <button
                    class="px-4 py-2 bg-[#E2B04D] text-white rounded-full text-xs font-bold disabled:opacity-50"
                    :disabled="!replyContent.trim()"
                    @click="submitReply(comment.id)"
                  >
                    发送
                  </button>
                  <button
                    class="px-3 py-2 text-gray-400 text-xs"
                    @click="replyTo = null; replyContent = ''"
                  >
                    取消
                  </button>
                </div>

                <div v-if="commentReplies[comment.id]?.length" class="mt-4 ml-4 pl-4 border-l-2 border-gray-100 space-y-4">
                  <div v-for="reply in commentReplies[comment.id]" :key="reply.id" class="flex items-start gap-2">
                    <img
                      :src="reply.author?.avatar || defaultAvatar"
                      class="w-7 h-7 rounded-full border border-gray-100 flex-shrink-0"
                      :alt="reply.author?.nickname"
                    />
                    <div>
                      <div class="flex items-center gap-2 mb-1">
                        <span class="font-bold text-xs text-[#2D3436]">{{ reply.author?.nickname || '匿名' }}</span>
                        <span class="text-[10px] text-gray-400">{{ formatTime(reply.createdAt) }}</span>
                      </div>
                      <p class="text-xs text-gray-600">{{ reply.content }}</p>
                    </div>
                  </div>
                  <button
                    v-if="hasMoreReplies[comment.id]"
                    class="text-xs text-[#E2B04D] font-bold hover:underline"
                    @click="loadReplies(comment.id)"
                  >
                    加载更多回复
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="comments.length > 0 && hasMoreComments" class="text-center mt-6">
          <button
            class="text-sm text-[#E2B04D] font-bold hover:underline"
            @click="loadMoreComments"
          >
            加载更多评论
          </button>
        </div>
      </section>
    </div>

    <div v-else class="text-center py-20 text-gray-400">
      <p>帖子不存在或已被删除</p>
    </div>
  </main>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import MainNav from '../components/MainNav.vue'
import { forumApi, type PostDetail, type CommentData } from '../api/forum'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const defaultAvatar = 'https://modao.cc/agent-py/media/generated_images/2026-03-19/7a697da7cb0e46808f265a42ae7934f3.jpg'

const post = ref<PostDetail | null>(null)
const comments = ref<CommentData[]>([])
const commentReplies = reactive<Record<number, CommentData[]>>({})
const hasMoreReplies = reactive<Record<number, boolean>>({})
const loading = ref(true)
const newComment = ref('')
const submittingComment = ref(false)
const replyTo = ref<number | null>(null)
const replyContent = ref('')
const commentPage = ref(0)
const hasMoreComments = ref(true)

const loadPost = async () => {
  const postId = Number(route.params.id)
  try {
    post.value = await forumApi.getPostById(postId) as any
  } catch (error) {
    console.error('加载帖子失败', error)
  } finally {
    loading.value = false
  }
}

const loadComments = async () => {
  const postId = Number(route.params.id)
  try {
    const res = await forumApi.getComments(postId, { page: commentPage.value, size: 20 }) as any
    const pageData = res
    const newComments = pageData.content || []
    if (commentPage.value === 0) {
      comments.value = newComments
    } else {
      comments.value.push(...newComments)
    }
    hasMoreComments.value = !pageData.last

    for (const comment of newComments) {
      if (!commentReplies[comment.id]) {
        commentReplies[comment.id] = []
        hasMoreReplies[comment.id] = false
      }
    }
  } catch (error) {
    console.error('加载评论失败', error)
  }
}

const loadMoreComments = () => {
  commentPage.value++
  loadComments()
}

const loadReplies = async (parentId: number) => {
  try {
    const res = await forumApi.getReplies(parentId, { page: 0, size: 20 }) as any
    const pageData = res
    commentReplies[parentId] = pageData.content || []
    hasMoreReplies[parentId] = !pageData.last
  } catch (error) {
    console.error('加载回复失败', error)
  }
}

const toggleLike = async () => {
  if (!authStore.isLoggedIn) {
    router.push('/login')
    return
  }
  if (!post.value) return
  try {
    const res = await forumApi.toggleLike('POST', post.value.id) as any
    post.value.likedByMe = res.liked
    post.value.likeCount = res.likeCount
  } catch (error) {
    console.error('点赞失败', error)
  }
}

const toggleCommentLike = async (comment: CommentData) => {
  if (!authStore.isLoggedIn) {
    router.push('/login')
    return
  }
  try {
    const res = await forumApi.toggleLike('COMMENT', comment.id) as any
    comment.likedByMe = res.liked
    comment.likeCount = res.likeCount
  } catch (error) {
    console.error('点赞失败', error)
  }
}

const submitComment = async () => {
  if (!newComment.value.trim()) return
  submittingComment.value = true
  try {
    await forumApi.createComment({
      postId: Number(route.params.id),
      content: newComment.value.trim()
    })
    newComment.value = ''
    commentPage.value = 0
    loadComments()
    if (post.value) {
      post.value.commentCount++
    }
  } catch (error) {
    console.error('评论失败', error)
  } finally {
    submittingComment.value = false
  }
}

const submitReply = async (parentId: number) => {
  if (!replyContent.value.trim()) return
  try {
    await forumApi.createComment({
      postId: Number(route.params.id),
      parentId,
      content: replyContent.value.trim()
    })
    replyContent.value = ''
    replyTo.value = null
    loadReplies(parentId)
    if (post.value) {
      post.value.commentCount++
    }
  } catch (error) {
    console.error('回复失败', error)
  }
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

onMounted(() => {
  loadPost()
  loadComments()
})
</script>
