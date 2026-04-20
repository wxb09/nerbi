<template>
  <div class="fixed inset-0 z-50 flex items-center justify-center bg-black/50" @click.self="$emit('close')">
    <div class="bg-white rounded-3xl w-full max-w-lg mx-4 p-8 shadow-2xl animate-fadeIn">
      <div class="flex justify-between items-center mb-6">
        <h2 class="text-xl font-bold text-[#2D3436]">发布动态</h2>
        <button @click="$emit('close')" class="text-gray-400 hover:text-gray-600">
          <span class="iconify text-2xl" data-icon="solar:close-circle-bold"></span>
        </button>
      </div>

      <div class="space-y-4">
        <div>
          <label class="text-sm font-medium text-gray-600 mb-1 block">类型</label>
          <div class="flex gap-2">
            <button
              v-for="t in postTypes"
              :key="t.value"
              class="px-4 py-2 rounded-full text-xs font-bold transition-all"
              :class="form.type === t.value ? 'bg-[#E2B04D] text-white shadow-lg' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'"
              @click="form.type = t.value"
            >
              {{ t.label }}
            </button>
          </div>
        </div>

        <div v-if="form.type !== 'NORMAL'">
          <input
            v-model="form.title"
            class="w-full py-3 px-4 bg-gray-50 rounded-2xl focus:bg-white focus:outline-none focus:ring-2 focus:ring-[#E2B04D] text-sm"
            :placeholder="form.type === 'THANKS' ? '感谢谁？' : form.type === 'HELP' ? '你需要什么帮助？' : '交换什么技能？'"
          />
        </div>

        <div>
          <textarea
            v-model="form.content"
            rows="5"
            class="w-full py-3 px-4 bg-gray-50 rounded-2xl focus:bg-white focus:outline-none focus:ring-2 focus:ring-[#E2B04D] text-sm resize-none"
            placeholder="分享你的故事..."
          ></textarea>
        </div>

        <div>
          <label class="text-sm font-medium text-gray-600 mb-1 block">标签</label>
          <div class="flex flex-wrap gap-2">
            <button
              v-for="tag in availableTags"
              :key="tag"
              class="px-3 py-1 rounded-full text-xs font-medium transition-all"
              :class="form.tags.includes(tag) ? 'bg-[#E2B04D] text-white' : 'bg-gray-100 text-gray-500 hover:bg-gray-200'"
              @click="toggleTag(tag)"
            >
              #{{ tag }}
            </button>
          </div>
        </div>
      </div>

      <div class="flex gap-3 mt-6">
        <button
          class="flex-1 py-3 rounded-2xl font-bold text-sm border border-gray-200 text-gray-500 hover:bg-gray-50 transition-all"
          @click="$emit('close')"
        >
          取消
        </button>
        <button
          class="flex-1 py-3 rounded-2xl font-bold text-sm bg-[#E2B04D] text-white hover:bg-[#d4a045] transition-all disabled:opacity-50"
          :disabled="!form.content.trim() || submitting"
          @click="submit"
        >
          {{ submitting ? '发布中...' : '发布' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { forumApi } from '../api/forum'

const emit = defineEmits<{
  close: []
  published: []
}>()

const submitting = ref(false)

const postTypes = [
  { value: 'NORMAL', label: '日常分享' },
  { value: 'THANKS', label: '感谢信' },
  { value: 'HELP', label: '求助' },
  { value: 'EXCHANGE', label: '技能交换' }
]

const availableTags = [
  '邻里互助',
  '绿色生活',
  '周末活动',
  '闲置共享',
  '社区新闻',
  '美食',
  '宠物',
  '亲子'
]

const form = reactive({
  type: 'NORMAL',
  title: '',
  content: '',
  tags: [] as string[]
})

const toggleTag = (tag: string) => {
  const idx = form.tags.indexOf(tag)
  if (idx >= 0) {
    form.tags.splice(idx, 1)
  } else {
    form.tags.push(tag)
  }
}

const submit = async () => {
  if (!form.content.trim()) return
  submitting.value = true
  try {
    await forumApi.createPost({
      type: form.type,
      title: form.title || undefined,
      content: form.content.trim(),
      tags: form.tags.length > 0 ? form.tags : undefined
    })
    emit('published')
  } catch (error) {
    console.error('发布失败', error)
    alert('发布失败，请重试')
  } finally {
    submitting.value = false
  }
}
</script>
