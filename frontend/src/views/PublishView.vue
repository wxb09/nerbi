<template>
  <MainNav />
  <main class="max-w-4xl mx-auto px-6 py-12">
    <div class="bg-white rounded-3xl p-8 border space-y-6">
      <h1 class="text-2xl font-bold">发布闲置物品</h1>
      <div class="grid md:grid-cols-2 gap-4">
        <input v-model="form.name" class="p-3 rounded-xl border bg-gray-50" placeholder="物品名称" />
        <select v-model="form.categoryId" class="p-3 rounded-xl border bg-gray-50"><option value="">选择分类</option></select>
      </div>
      <textarea v-model="form.description" class="w-full p-3 rounded-xl border bg-gray-50 h-36" placeholder="物品描述/故事"></textarea>
      <div class="grid md:grid-cols-3 gap-4">
        <input v-model="form.pricePerDay" type="number" class="p-3 rounded-xl border" placeholder="每日租金" />
        <input v-model="form.deposit" type="number" class="p-3 rounded-xl border" placeholder="押金" />
        <input v-model="form.creditRequired" type="number" step="0.1" class="p-3 rounded-xl border" placeholder="最低信用要求" />
      </div>
      <div class="flex gap-3">
        <button class="flex-1 py-3 rounded-xl bg-[#E2B04D] text-white font-bold" @click="publish">立即确认发布</button>
        <button class="px-6 py-3 rounded-xl border-2 border-gray-800" @click="saveDraft">存为草稿</button>
      </div>
    </div>
  </main>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import MainNav from '../components/MainNav.vue'
import { itemApi } from '../api/item'

const router = useRouter()
const form = ref({
  name: '',
  categoryId: '',
  description: '',
  pricePerDay: '',
  deposit: '',
  creditRequired: ''
})

const publish = async () => {
  try {
    await itemApi.createItem(form.value)
    alert('发布成功')
    router.push('/index')
  } catch (error) {
    console.error('发布失败', error)
    alert('发布失败，请重试')
  }
}

const saveDraft = async () => {
  try {
    await itemApi.saveDraft(form.value)
    alert('保存草稿成功')
  } catch (error) {
    console.error('保存草稿失败', error)
    alert('保存草稿失败，请重试')
  }
}
</script>
