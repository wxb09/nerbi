<template>
  <MainNav />
  <main class="max-w-7xl mx-auto px-6 py-12 animate-fadeIn">
    <div class="grid lg:grid-cols-3 gap-8">
      <!-- 左侧：主要动态流 -->
      <section class="lg:col-span-2 space-y-8">
        <!-- 标题和标签 -->
        <div class="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
          <h1 class="text-3xl font-bold italic text-[#2D3436]">
            情感联结流 
            <span class="text-sm not-italic font-normal text-gray-400 ml-2">栖霞苑社区</span>
          </h1>
          <div class="flex gap-2">
            <button 
              class="px-4 py-2 bg-white border border-gray-200 rounded-full text-xs font-bold hover:bg-gray-50 transition-all"
              :class="activeTab === 'latest' ? 'bg-[#E2B04D] text-white border-[#E2B04D] shadow-lg shadow-[#E2B04D]/20' : ''"
              @click="activeTab = 'latest'"
            >
              最新
            </button>
            <button 
              class="px-4 py-2 rounded-full text-xs font-bold transition-all"
              :class="activeTab === 'hot' ? 'bg-[#E2B04D] text-white shadow-lg shadow-[#E2B04D]/20' : 'bg-white border border-gray-200 hover:bg-gray-50'"
              @click="activeTab = 'hot'"
            >
              最热
            </button>
            <button class="px-4 py-2 bg-white border border-gray-200 rounded-full text-xs font-bold hover:bg-gray-50 transition-all">
              感谢信
            </button>
          </div>
        </div>

        <!-- 发帖入口 -->
        <div class="bg-white border-2 border-dashed border-gray-200 p-6 rounded-[2rem] flex items-center space-x-4">
          <img 
            :src="currentUserAvatar" 
            class="w-12 h-12 rounded-full border border-gray-100 flex-shrink-0"
            alt="User avatar"
          />
          <input 
            v-model="newPost.content"
            class="flex-1 text-left py-3 px-6 bg-gray-50 text-gray-400 rounded-2xl hover:bg-gray-100 transition-all focus:bg-white focus:text-gray-700 focus:outline-none focus:ring-2 focus:ring-[#E2B04D]"
            placeholder="分享今天的邻里小故事或是求助..."
            @keyup.enter="publishPost"
          />
          <div class="flex gap-4">
            <span class="iconify text-2xl text-gray-300 hover:text-[#E2B04D] cursor-pointer transition-colors" data-icon="solar:camera-bold"></span>
            <span class="iconify text-2xl text-gray-300 hover:text-[#E2B04D] cursor-pointer transition-colors" data-icon="solar:link-bold"></span>
          </div>
        </div>

        <!-- 帖子列表 -->
        <div class="space-y-6">
          <article 
            v-for="post in posts" 
            :key="post.id" 
            class="p-8 rounded-[2rem] post-card transition-all duration-300"
            :class="post.type === 'thanks' ? 'bg-orange-50/50 border border-orange-100' : 'bg-white border border-gray-100 shadow-sm'"
          >
            <!-- 帖子头部 -->
            <div class="flex items-center justify-between mb-6">
              <div class="flex items-center space-x-4">
                <img 
                  :src="post.avatar || defaultAvatar" 
                  class="w-10 h-10 rounded-full border-2 border-white shadow-sm"
                  :alt="post.author"
                />
                <div>
                  <h3 class="font-bold text-sm text-[#2D3436]">{{ post.author }}</h3>
                  <p class="text-[10px] text-gray-400 font-medium">{{ post.time }}发布于 {{ post.location }}</p>
                </div>
              </div>
              <span 
                class="px-3 py-1 rounded-full text-[10px] font-bold uppercase tracking-widest shadow-sm"
                :class="getPostTypeClass(post.type)"
              >
                # {{ getPostTypeText(post.type) }}
              </span>
            </div>

            <!-- 帖子标题（如果有） -->
            <h4 v-if="post.title" class="font-bold text-lg mb-3 text-[#2D3436]">{{ post.title }}</h4>

            <!-- 帖子内容 -->
            <p 
              class="leading-relaxed mb-6"
              :class="post.type === 'thanks' ? 'text-gray-700 font-medium' : 'text-gray-600'"
              v-html="formatContent(post.content)"
            ></p>

            <!-- 帖子操作 -->
            <div class="flex items-center space-x-6" :class="post.type === 'thanks' ? 'text-gray-400' : 'text-gray-300'">
              <button 
                @click="toggleLike(post)"
                class="flex items-center space-x-1 hover:text-red-400 transition-colors group"
              >
                <span class="iconify text-xl transition-transform group-hover:scale-125" :data-icon="post.liked ? 'solar:heart-bold' : 'solar:heart-linear'"></span>
                <span class="text-xs font-bold">{{ post.likes }}</span>
              </button>
              <button class="flex items-center space-x-1 hover:text-[#E2B04D] transition-colors group">
                <span class="iconify text-xl transition-transform group-hover:scale-125" data-icon="solar:chat-round-dots-bold"></span>
                <span class="text-xs font-bold">{{ post.comments }}</span>
              </button>
              <button class="flex items-center space-x-1 hover:text-blue-400 transition-colors group">
                <span class="iconify text-xl transition-transform group-hover:scale-125" data-icon="solar:share-bold"></span>
                <span class="text-xs font-bold">分享</span>
              </button>
            </div>
          </article>
        </div>

        <!-- 加载更多 -->
        <div class="text-center py-10">
          <button class="bg-[#2D3436] text-white px-8 py-3 rounded-full font-bold text-sm hover:scale-105 transition-all">
            加载更多邻里动态
          </button>
        </div>
      </section>

      <!-- 右侧：边栏 -->
      <aside class="space-y-6">
        <!-- 社区公告 -->
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

        <!-- 绿色榜 -->
        <div class="bg-white p-6 rounded-3xl border border-gray-100 shadow-sm">
          <div class="flex items-center gap-2 mb-4">
            <span class="iconify text-xl text-green-500" data-icon="solar:leaf-bold"></span>
            <h3 class="font-bold text-lg text-[#2D3436]">三月绿色榜</h3>
          </div>
          <div class="space-y-4">
            <div 
              v-for="(item, index) in greenRank" 
              :key="index"
              class="flex items-center justify-between p-3 rounded-xl hover:bg-gray-50 transition-colors"
            >
              <div class="flex items-center gap-3">
                <div 
                  class="w-8 h-8 rounded-full flex items-center justify-center font-bold text-sm"
                  :class="index === 0 ? 'bg-yellow-400 text-yellow-900' : index === 1 ? 'bg-gray-300 text-gray-700' : index === 2 ? 'bg-orange-300 text-orange-900' : 'bg-gray-100 text-gray-500'"
                >
                  {{ index + 1 }}
                </div>
                <img :src="item.avatar || defaultAvatar" class="w-10 h-10 rounded-full" :alt="item.name" />
                <span class="font-medium text-[#333333]">{{ item.name }}</span>
              </div>
              <span class="font-bold text-green-600 text-sm">+{{ item.co2 }}g CO₂</span>
            </div>
          </div>
        </div>

        <!-- 热门话题 -->
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
            >
              #{{ tag }}
            </span>
          </div>
        </div>
      </aside>
    </div>
  </main>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import MainNav from '../components/MainNav.vue'

const activeTab = ref('latest')
const currentUserAvatar = 'https://modao.cc/agent-py/media/generated_images/2026-03-19/7a697da7cb0e46808f265a42ae7934f3.jpg'
const defaultAvatar = 'https://modao.cc/agent-py/media/generated_images/2026-03-19/7a697da7cb0e46808f265a42ae7934f3.jpg'

const newPost = ref({
  content: ''
})

const posts = ref([
  { 
    id: 1, 
    type: 'thanks',
    author: '小雅雅', 
    avatar: 'https://modao.cc/agent-py/media/generated_images/2026-03-19/7a697da7cb0e46808f265a42ae7934f3.jpg',
    location: '栖霞苑3号楼',
    content: '特别感谢 5号楼的 <b>@李大明白</b> 大哥！我家吸尘器坏了，大哥直接把他的戴森借我用了整整一星期，解决了我家猫脱皮期的尴尬。大哥还送了我两个替换过滤网，感动哭了！在这个社区住真的太温暖了。', 
    likes: 128, 
    comments: 24,
    time: '刚刚',
    liked: false
  },
  { 
    id: 2, 
    type: 'help',
    author: '陈工', 
    avatar: 'https://modao.cc/agent-py/media/generated_images/2026-03-19/7a697da7cb0e46808f265a42ae7934f3.jpg',
    location: '栖霞苑12号楼',
    title: '求借一套专业的水粉画架！',
    content: '女儿马上要参加校外写生活动，临时发现画架高度不够。想向邻居借用三五天。用完一定清洗干净并附赠精美小礼品！在线等！', 
    likes: 42, 
    comments: 15,
    time: '1小时前',
    liked: false
  },
  { 
    id: 3, 
    type: 'share',
    author: '王阿姨', 
    avatar: 'https://modao.cc/agent-py/media/generated_images/2026-03-19/7a697da7cb0e46808f265a42ae7934f3.jpg',
    location: '栖霞苑8号楼',
    content: '老伴儿钓鱼回来带了好多草鱼，邻居谁家想要？免费拿走，直接来8号楼1单元楼下就行。别客气哦，都是新鲜的！', 
    likes: 89, 
    comments: 31,
    time: '3小时前',
    liked: false
  },
])

const greenRank = ref([
  { name: '李大明白', co2: 82, avatar: 'https://modao.cc/agent-py/media/generated_images/2026-03-19/7a697da7cb0e46808f265a42ae7934f3.jpg' },
  { name: '张小美', co2: 67, avatar: 'https://modao.cc/agent-py/media/generated_images/2026-03-19/7a697da7cb0e46808f265a42ae7934f3.jpg' },
  { name: '王叔叔', co2: 54, avatar: 'https://modao.cc/agent-py/media/generated_images/2026-03-19/7a697da7cb0e46808f265a42ae7934f3.jpg' },
  { name: '刘阿姨', co2: 41, avatar: 'https://modao.cc/agent-py/media/generated_images/2026-03-19/7a697da7cb0e46808f265a42ae7934f3.jpg' },
])

const hotTags = ref([
  '邻里互助',
  '绿色生活',
  '周末活动',
  '闲置共享',
  '社区新闻'
])

const getPostTypeClass = (type: string) => {
  const classes: Record<string, string> = {
    'thanks': 'bg-white text-orange-600',
    'help': 'bg-blue-50 text-blue-600',
    'share': 'bg-gray-100 text-gray-600'
  }
  return classes[type] || 'bg-gray-100 text-gray-600'
}

const getPostTypeText = (type: string) => {
  const texts: Record<string, string> = {
    'thanks': '邻里感谢信',
    'help': '物品求助',
    'share': '闲置分享'
  }
  return texts[type] || '社区动态'
}

const formatContent = (content: string) => {
  // 简单处理 @提及 高亮
  return content.replace(/@(\S+)/g, '<span class="text-[#E2B04D] font-bold">@$1</span>')
}

const toggleLike = (post: any) => {
  post.liked = !post.liked
  post.likes += post.liked ? 1 : -1
}

const publishPost = () => {
  if (!newPost.value.content.trim()) return
  
  posts.value.unshift({
    id: Date.now(),
    type: 'share',
    author: '我',
    avatar: currentUserAvatar,
    location: '栖霞苑',
    content: newPost.value.content,
    likes: 0,
    comments: 0,
    time: '刚刚',
    liked: false
  })
  
  newPost.value.content = ''
  alert('发布成功！')
}
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
