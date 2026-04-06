<template>
  <MainNav />
  <main class="max-w-4xl mx-auto px-6 py-8">
    <!-- 顶部标题 -->
    <div class="flex items-center justify-between mb-8">
      <div class="flex items-center gap-4">
        <button @click="goBack" class="text-gray-400 hover:text-gray-600 transition-colors">
          <span class="iconify text-2xl" data-icon="solar:alt-arrow-left-bold"></span>
        </button>
        <div>
          <h1 class="text-2xl font-bold">{{ pageTitle }}</h1>
          <p v-if="itemName" class="text-sm text-gray-500 mt-1">{{ itemName }}</p>
        </div>
      </div>
      <div v-if="!isNew" class="text-sm text-gray-500">
        最后编辑：{{ lastEditedAt }}
      </div>
    </div>

    <!-- 表单内容 -->
    <div class="bg-white rounded-3xl border border-gray-100 p-8 space-y-6">
      <!-- 图片上传 -->
      <div>
        <label class="block text-sm font-bold text-gray-700 mb-3">物品图片</label>
        <ImageUploader v-model="form.images" />
      </div>

      <!-- 物品名称 -->
      <div>
        <label class="block text-sm font-bold text-gray-700 mb-2">物品名称 <span class="text-red-500">*</span></label>
        <input 
          v-model="form.name" 
          type="text" 
          class="w-full p-4 rounded-xl border-2 border-gray-100 focus:border-[#E2B04D] focus:outline-none transition-colors"
          placeholder="请输入物品名称"
        />
      </div>

      <!-- 分类 -->
      <div>
        <label class="block text-sm font-bold text-gray-700 mb-2">分类 <span class="text-red-500">*</span></label>
        <select 
          v-model="form.categoryId" 
          class="w-full p-4 rounded-xl border-2 border-gray-100 focus:border-[#E2B04D] focus:outline-none transition-all bg-white appearance-none cursor-pointer"
        >
          <option value="">请选择分类</option>
          <option v-for="category in categories" :key="category.id" :value="category.id">
            {{ category.name }}
          </option>
        </select>
      </div>

      <!-- 物品描述 -->
      <div>
        <label class="block text-sm font-bold text-gray-700 mb-2">物品描述 <span class="text-red-500">*</span></label>
        <textarea 
          v-model="form.description" 
          rows="4" 
          class="w-full p-4 rounded-xl border-2 border-gray-100 focus:border-[#E2B04D] focus:outline-none transition-colors"
          placeholder="请描述物品的品牌、型号、新旧程度、功能特点等"
        ></textarea>
      </div>

      <!-- 背后的故事 -->
      <div>
        <label class="block text-sm font-bold text-gray-700 mb-2">背后的故事</label>
        <textarea 
          v-model="form.story" 
          rows="3" 
          class="w-full p-4 rounded-xl border-2 border-gray-100 focus:border-[#E2B04D] focus:outline-none transition-colors"
          placeholder="分享这个物品对你的特殊意义或使用经历"
        ></textarea>
      </div>

      <!-- 借阅规则 -->
      <div>
        <label class="block text-sm font-bold text-gray-700 mb-4">借阅规则</label>
        <div class="grid grid-cols-3 gap-4">
          <div>
            <label class="block text-xs text-gray-500 mb-2">租金（元/天）</label>
            <input 
              v-model.number="form.pricePerDay" 
              type="number" 
              step="0.01"
              min="0"
              class="w-full p-3 rounded-xl border-2 border-gray-100 focus:border-[#E2B04D] focus:outline-none transition-colors"
            />
          </div>
          <div>
            <label class="block text-xs text-gray-500 mb-2">押金（元）</label>
            <input 
              v-model.number="form.deposit" 
              type="number" 
              step="0.01"
              min="0"
              class="w-full p-3 rounded-xl border-2 border-gray-100 focus:border-[#E2B04D] focus:outline-none transition-colors"
            />
          </div>
          <div>
            <label class="block text-xs text-gray-500 mb-2">信用要求</label>
            <input 
              v-model.number="form.creditRequired" 
              type="number" 
              step="0.1"
              min="0"
              max="10"
              class="w-full p-3 rounded-xl border-2 border-gray-100 focus:border-[#E2B04D] focus:outline-none transition-colors"
            />
          </div>
        </div>
      </div>

      <!-- 归还要求 -->
      <div>
        <label class="block text-sm font-bold text-gray-700 mb-2">归还要求</label>
        <div class="space-y-2">
          <div v-for="(req, index) in form.returnRequirements" :key="index" class="flex gap-2">
            <input 
              v-model="form.returnRequirements[index]" 
              type="text" 
              class="flex-1 p-3 rounded-xl border-2 border-gray-100 focus:border-[#E2B04D] focus:outline-none transition-colors"
              placeholder="如：清理干净、晾干后归还"
            />
            <button 
              @click="removeReturnRequirement(index)" 
              type="button"
              class="px-4 py-2 text-red-500 hover:bg-red-50 rounded-xl transition-colors"
            >
              删除
            </button>
          </div>
          <button 
            @click="addReturnRequirement" 
            type="button"
            class="text-sm text-[#E2B04D] font-bold hover:underline"
          >
            + 添加归还要求
          </button>
        </div>
      </div>

      <!-- 标签 -->
      <div>
        <label class="block text-sm font-bold text-gray-700 mb-2">标签</label>
        <div class="flex flex-wrap gap-2 mb-3">
          <span 
            v-for="(tag, index) in form.tags" 
            :key="index"
            class="px-3 py-1 bg-gray-100 text-gray-700 rounded-full text-sm flex items-center gap-2"
          >
            {{ tag }}
            <button @click="removeTag(index)" class="text-gray-400 hover:text-red-500">×</button>
          </span>
        </div>
        <div class="flex gap-2">
          <input 
            v-model="newTag" 
            @keyup.enter="addTag"
            type="text" 
            class="flex-1 p-3 rounded-xl border-2 border-gray-100 focus:border-[#E2B04D] focus:outline-none transition-colors"
            placeholder="输入标签后按回车添加"
          />
        </div>
      </div>
    </div>

    <!-- 底部按钮 -->
    <div class="flex gap-4 mt-8 pt-6 border-t border-gray-100">
      <!-- 保存草稿按钮：仅新建和草稿模式显示 -->
      <button 
        v-if="showSaveDraftButton"
        @click="saveDraftHandler" 
        :disabled="submitting"
        class="flex-1 py-4 rounded-2xl border-2 border-gray-200 font-bold text-gray-600 hover:bg-gray-50 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
      >
        {{ submitting ? '保存中...' : '💾 保存草稿' }}
      </button>
      
      <!-- 发布/保存按钮 -->
      <button 
        @click="publishHandler" 
        :disabled="submitting || !canPublish"
        class="flex-1 py-4 rounded-2xl bg-[#E2B04D] text-white font-bold hover:bg-[#C49A2E] transition-all disabled:opacity-50 disabled:cursor-not-allowed"
      >
        {{ submitting ? '提交中...' : publishButtonText }}
      </button>
    </div>
  </main>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import MainNav from '../components/MainNav.vue'
import ImageUploader from '../components/ImageUploader.vue'
import { itemApi, type CreateItemRequest } from '../api/item'
import { publicApi, type Category } from '../api/public'

const router = useRouter()
const route = useRoute()

const form = ref<CreateItemRequest>({
  name: '',
  categoryId: 0,
  description: '',
  story: '',
  pricePerDay: 0,
  deposit: 0,
  creditRequired: 0,
  returnRequirements: [],
  tags: [],
  images: []
})

const newTag = ref('')
const categories = ref<Category[]>([])
const submitting = ref(false)
const itemStatus = ref<string>('')  // 物品实际状态

const isNew = computed(() => {
  return !route.params.id
})

const isDraft = computed(() => {
  return itemStatus.value === 'DRAFT'
})

const isEdit = computed(() => {
  return !!route.params.id && !isDraft.value
})

const pageTitle = computed(() => {
  if (isDraft.value) return '继续编辑草稿'
  if (isEdit.value) return '编辑物品'
  return '发布新物品'
})

const publishButtonText = computed(() => {
  if (isEdit.value) return '保存修改'
  return '发布物品'
})

const showSaveDraftButton = computed(() => {
  return isNew.value || isDraft.value
})

const itemName = computed(() => {
  return form.value.name || ''
})

const lastEditedAt = computed(() => {
  return new Date().toLocaleString('zh-CN')
})

const canPublish = computed(() => {
  return form.value.name && 
         form.value.categoryId && 
         form.value.description &&
         form.value.images.length > 0
})

// 方法
const goBack = () => {
  router.back()
}

const addReturnRequirement = () => {
  form.value.returnRequirements.push('')
}

const removeReturnRequirement = (index: number) => {
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

const loadCategories = async () => {
  try {
    categories.value = await publicApi.getCategories()
  } catch (error) {
    console.error('加载分类失败', error)
  }
}

const loadItemData = async () => {
  const id = route.params.id as string
  if (!id) return
  
  try {
    const item = await itemApi.getItemById(id)
    
    if (item.status === 'BORROWED') {
      alert('借出中的物品无法编辑')
      router.back()
      return
    }
    
    itemStatus.value = item.status || ''
    form.value = {
      name: item.name,
      categoryId: item.categoryId,
      description: item.description,
      story: item.story || '',
      pricePerDay: Number(item.pricePerDay),
      deposit: Number(item.deposit),
      creditRequired: Number(item.creditRequired),
      returnRequirements: item.returnRequirements || [],
      tags: item.tags || [],
      images: item.images || []
    }
  } catch (error) {
    console.error('加载物品数据失败', error)
    alert('加载失败')
    router.back()
  }
}

const saveDraftHandler = async () => {
  if (!form.value.name) {
    alert('请填写物品名称')
    return
  }
  
  submitting.value = true
  try {
    const draftData = {
      ...form.value,
      returnRequirements: JSON.stringify(form.value.returnRequirements),
      tags: JSON.stringify(form.value.tags)
    }
    
    if (isDraft.value) {
      const id = route.params.id as string
      await itemApi.updateItem(Number(id), { ...draftData, status: 'DRAFT' })
    } else {
      await itemApi.saveDraft(draftData)
    }
    
    alert('草稿已保存')
    goBack()
  } catch (error: any) {
    alert(error.message || '保存草稿失败')
  } finally {
    submitting.value = false
  }
}

const publishHandler = async () => {
  if (!canPublish.value) {
    alert('请填写完整信息并至少上传一张图片')
    return
  }
  
  const actionText = isEdit.value ? '保存修改' : '发布物品'
  if (!confirm(`确认${actionText}吗？`)) return
  
  submitting.value = true
  try {
    const itemData = {
      ...form.value,
      returnRequirements: JSON.stringify(form.value.returnRequirements),
      tags: JSON.stringify(form.value.tags)
    }
    
    if (isNew.value) {
      await itemApi.createItem(itemData)
      alert('发布成功')
    } else if (isDraft.value) {
      const id = route.params.id as string
      await itemApi.updateItem(Number(id), { ...itemData, status: 'AVAILABLE' })
      alert('发布成功')
    } else if (isEdit.value) {
      const id = route.params.id as string
      await itemApi.updateItem(Number(id), itemData)
      alert('修改已保存')
    }
    
    router.push('/')
  } catch (error: any) {
    alert(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await loadCategories()
  if (route.params.id) {
    await loadItemData()
  }
})
</script>
