<template>
  <MainNav />
  <main class="max-w-7xl mx-auto px-6 py-12 grid lg:grid-cols-3 gap-8">
    <section class="lg:col-span-2 space-y-6">
      <div class="flex justify-between items-center">
        <h1 class="text-3xl font-bold">情感联结流</h1>
        <div class="flex gap-2">
          <button class="px-4 py-2 rounded-full border">最新</button>
          <button class="px-4 py-2 rounded-full bg-[#E2B04D] text-white">最热</button>
        </div>
      </div>
      <div class="bg-white p-4 rounded-2xl border flex gap-3">
        <input class="flex-1 p-3 rounded-xl bg-gray-50" placeholder="分享今天的邻里小故事..." />
        <button class="px-4 rounded-xl border" @click="todo">发布</button>
      </div>
      <article class="bg-white p-6 rounded-3xl border" v-for="post in posts" :key="post.id">
        <h3 class="font-bold">{{ post.title }}</h3>
        <p class="text-gray-600 mt-2">{{ post.content }}</p>
        <div class="mt-4 flex gap-4 text-sm">
          <button @click="todo">点赞 {{ post.likes }}</button>
          <button @click="todo">评论 {{ post.comments }}</button>
        </div>
      </article>
    </section>
    <aside class="space-y-4">
      <div class="bg-[#2D3436] text-white p-6 rounded-3xl">
        <h3 class="font-bold">社区公告</h3>
        <p class="text-sm text-gray-300 mt-2">邻里借阅规则 2026 版更新。</p>
      </div>
      <div class="bg-white p-6 rounded-3xl border">
        <h3 class="font-bold">三月绿色榜</h3>
        <p class="text-sm text-gray-500 mt-2">李大明白 +82g CO2</p>
      </div>
    </aside>
  </main>
</template>

<script setup lang="ts">
import MainNav from '../components/MainNav.vue'

const posts = [
  { id: 1, title: '感谢邻居借我吸尘器', content: '真的帮了大忙，社区太温暖了。', likes: 128, comments: 24 },
  { id: 2, title: '求借水粉画架 3 天', content: '女儿写生活动急需，在线等。', likes: 42, comments: 15 },
]
const todo = () => alert('接口占位：论坛功能')
</script>
