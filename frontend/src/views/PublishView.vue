<template>
  <MainNav />
  <main class="max-w-4xl mx-auto px-6 py-12">
    <div class="bg-white rounded-[2rem] p-8 border border-gray-100 shadow-sm space-y-8">
      <h1 class="text-2xl font-bold text-[#2D3436]">发布闲置物品</h1>
      
      <!-- 图片上传 -->
      <div>
        <label class="block text-sm font-medium text-[#333333] mb-3">物品图片</label>
        <ImageUploader v-model="form.images" :max-count="9" />
      </div>
      
      <!-- 基本信息 -->
      <div class="grid md:grid-cols-2 gap-6">
        <div>
          <label class="block text-sm font-medium text-[#333333] mb-3">物品名称 <span class="text-red-500">*</span></label>
          <input 
            v-model="form.name" 
            class="w-full px-4 py-3 border border-gray-200 rounded-xl focus:border-[#E2B04D] focus:outline-none transition-colors" 
            placeholder="请输入物品名称"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-[#333333] mb-3">物品分类 <span class="text-red-500">*</span></label>
          <select 
            v-model="form.categoryId" 
            class="w-full px-4 py-3 border border-gray-200 rounded-xl focus:border-[#E2B04D] focus:outline-none transition-colors bg-white"
          >
            <option value="">请选择分类</option>
            <option v-for="category in categories" :key="category.id" :value="category.id">
              {{ category.name }}
            </option>
          </select>
        </div>
      </div>
      
      <!-- 描述 -->
      <div>
        <label class="block text-sm font-medium text-[#333333] mb-3">物品描述</label>
        <textarea 
          v-model="form.description" 
          class="w-full px-4 py-3 border border-gray-200 rounded-xl focus:border-[#E2B04D] focus:outline-none transition-colors h-24 resize-none" 
          placeholder="描述物品的基本情况，如新旧程度、购买时间等"
        ></textarea>
      </div>
      
      <!-- 故事 -->
      <div>
        <label class="block text-sm font-medium text-[#333333] mb-3">物品故事</label>
        <textarea 
          v-model="form.story" 
          class="w-full px-4 py-3 border border-gray-200 rounded-xl focus:border-[#E2B04D] focus:outline-none transition-colors h-24 resize-none" 
          placeholder="分享这个物品背后的故事，让邻居更想借用它"
        ></textarea>
      </div>
      
      <!-- 租金设置 -->
      <div>
        <label class="block text-sm font-medium text-[#333333] mb-3">租金设置</label>
        <div class="grid md:grid-cols-3 gap-4">
          <div class="relative">
            <input 
              v-model="form.pricePerDay" 
              type="number" 
              step="0.01"
              class="w-full px-4 py-3 border border-gray-200 rounded-xl focus:border-[#E2B04D] focus:outline-none transition-colors pr-10" 
              placeholder="0.00"
            />
            <span class="absolute right-4 top-1/2 -translate-y-1/2 text-[#999999] text-sm">元/天</span>
          </div>
          <div class="relative">
            <input 
              v-model="form.deposit" 
              type="number" 
              step="0.01"
              class="w-full px-4 py-3 border border-gray-200 rounded-xl focus:border-[#E2B04D] focus:outline-none transition-colors pr-10" 
              placeholder="0.00"
            />
            <span class="absolute right-4 top-1/2 -translate-y-1/2 text-[#999999] text-sm">押金</span>
          </div>
          <div class="relative">
            <input 
              v-model="form.creditRequired" 
              type="number" 
              step="0.1"
              min="0"
              max="10"
              class="w-full px-4 py-3 border border-gray-200 rounded-xl focus:border-[#E2B04D] focus:outline-none transition-colors pr-16" 
              placeholder="0-10"
            />
            <span class="absolute right-4 top-1/2 -translate-y-1/2 text-[#999999] text-sm">信用要求</span>
          </div>
        </div>
      </div>
      
      <!-- 归还要求 -->
      <div>
        <label class="block text-sm font-medium text-[#333333] mb-3">归还要求</label>
        <div class="space-y-3">
          <div v-for="(req, index) in form.returnRequirements" :key="index" class="flex gap-3">
            <input 
              v-model="form.returnRequirements[index]" 
              class="flex-1 px-4 py-3 border border-gray-200 rounded-xl focus:border-[#E2B04D] focus:outline-none transition-colors" 
              placeholder="如：已消毒、保持原样、及时归还等"
            />
            <button 
              @click="removeRequirement(index)" 
              class="px-4 py-3 text-red-500 hover:bg-red-50 rounded-xl transition-colors"
            >
              删除
            </button>
          </div>
          <button 
            @click="addRequirement" 
            class="text-[#E2B04D] hover:text-[#C49A2E] font-medium text-sm transition-colors"
          >
            + 添加归还要求
          </button>
        </div>
      </div>
      
      <!-- 标签 -->
      <div>
        <label class="block text-sm font-medium text-[#333333] mb-3">标签</label>
        <div class="flex flex-wrap gap-2">
          <span 
            v-for="(tag, index) in form.tags" 
            :key="index"
            class="px-3 py-1 bg-gray-100 text-[#333333] rounded-full text-sm flex items-center gap-1"
          >
            {{ tag }}
            <button @click="removeTag(index)" class="text-gray-400 hover:text-red-500 transition-colors">×</button>
          </span>
          <input 
            v-model="newTag"
            @keyup.enter="addTag"
            class="px-3 py-1 border border-gray-200 rounded-full text-sm w-24 focus:border-[#E2B04D] focus:outline-none transition-colors"
            placeholder="添加标签"
          />
        </div>
      </div>
      
      <!-- 按钮 -->
      <div class="flex gap-4 pt-4">
        <button 
          class="flex-1 py-3 rounded-xl bg-[#E2B04D] text-white font-bold hover:bg-[#C49A2E] transition-colors" 
          @click="publish"
          :disabled="submitting"
        >
          {{ submitting ? '发布中...' : '立即确认发布' }}
        </button>
        <button 
          class="px-8 py-3 rounded-xl border-2 border-[#2D3436] text-[#2D3436] font-bold hover:bg-gray-50 transition-colors" 
          @click="saveDraft"
          :disabled="submitting"
        >
          存为草稿
        </button>
      </div>
    </div>
  </main>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import MainNav from '../components/MainNav.vue'
import ImageUploader from '../components/ImageUploader.vue'
import { itemApi } from '../api/item'
import { publicApi, type Category } from '../api/public'

const router = useRouter()
const submitting = ref(false)
const categories = ref<Category[]>([])
const newTag = ref('')

const form = ref({
  name: '',
  categoryId: '',
  description: '',
  story: '',
  pricePerDay: '',
  deposit: '',
  creditRequired: '',
  images: [] as string[],
  returnRequirements: [] as string[],
  tags: [] as string[]
})

onMounted(async () => {
  try {
    categories.value = await publicApi.getCategories()
  } catch (error) {
    console.error('获取分类失败', error)
  }
})

const addRequirement = () => {
  form.value.returnRequirements.push('')
}

const removeRequirement = (index: number) => {
  form.value.returnRequirements.splice(index, 1)
}

const addTag = () => {
  const tag = newTag.value.trim()
  if (tag && !form.value.tags.includes(tag)) {
    form.value.tags.push(tag)
  }
  newTag.value = ''
}

const removeTag = (index: number) => {
  form.value.tags.splice(index, 1)
}

const validateForm = () => {
  if (!form.value.name.trim()) {
    alert('请输入物品名称')
    return false
  }
  if (!form.value.categoryId) {
    alert('请选择物品分类')
    return false
  }
  if (form.value.images.length === 0) {
    alert('请至少上传一张图片')
    return false
  }
  return true
}

const buildSubmitData = () => {
  return {
    name: form.value.name,
    categoryId: Number(form.value.categoryId),
    description: form.value.description,
    story: form.value.story,
    pricePerDay: Number(form.value.pricePerDay) || 0,
    deposit: Number(form.value.deposit) || 0,
    creditRequired: Number(form.value.creditRequired) || 0,
    images: form.value.images,
    returnRequirements: JSON.stringify(form.value.returnRequirements.filter(r => r.trim())),
    tags: JSON.stringify(form.value.tags.filter(t => t.trim()))
  }
}

const publish = async () => {
  if (!validateForm()) return
  
  submitting.value = true
  try {
    await itemApi.createItem(buildSubmitData())
    alert('发布成功')
    router.push('/index')
  } catch (error: any) {
    console.error('发布失败', error)
    alert(error.message || '发布失败，请重试')
  } finally {
    submitting.value = false
  }
}

const saveDraft = async () => {
  submitting.value = true
  try {
    await itemApi.saveDraft(buildSubmitData())
    alert('保存草稿成功')
  } catch (error: any) {
    console.error('保存草稿失败', error)
    alert(error.message || '保存草稿失败，请重试')
  } finally {
    submitting.value = false
  }
}
</script>
