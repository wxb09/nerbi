<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="visible" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-black/50" @click="handleClose"></div>
        
        <div class="relative bg-white rounded-3xl w-full max-w-md shadow-2xl overflow-hidden">
          <div class="p-6 border-b border-gray-100">
            <div class="flex items-center justify-between mb-4">
              <h3 class="text-xl font-bold">发表评价</h3>
              <button @click="handleClose" class="text-gray-400 hover:text-gray-600 transition-colors">
                <span class="iconify text-2xl" data-icon="solar:close-circle-bold"></span>
              </button>
            </div>
            
            <div class="flex bg-gray-100 rounded-xl p-1">
              <button 
                v-if="canSelectItemType"
                @click="reviewType = 'ITEM'"
                :class="[
                  'flex-1 py-2 rounded-lg text-sm font-bold transition-all',
                  reviewType === 'ITEM' 
                    ? 'bg-white text-[#E2B04D] shadow-sm' 
                    : 'text-gray-500 hover:text-gray-700'
                ]"
              >
                评价物品
              </button>
              <button 
                @click="reviewType = 'USER'"
                :class="[
                  'py-2 rounded-lg text-sm font-bold transition-all',
                  canSelectItemType ? 'flex-1' : 'flex-1',
                  reviewType === 'USER' 
                    ? 'bg-white text-[#E2B04D] shadow-sm' 
                    : 'text-gray-500 hover:text-gray-700'
                ]"
              >
                评价用户
              </button>
            </div>
          </div>

          <div class="p-6 space-y-6">
            <div v-if="borrowInfo" class="flex items-center space-x-4 p-4 bg-gray-50 rounded-2xl">
              <div class="w-16 h-16 rounded-xl bg-gray-200 flex-shrink-0 overflow-hidden">
                <img v-if="borrowInfo.itemImage" :src="getImageUrl(borrowInfo.itemImage)" class="w-full h-full object-cover" />
                <span v-else class="iconify text-2xl text-gray-300 w-full h-full flex items-center justify-center" data-icon="solar:box-bold"></span>
              </div>
              <div class="flex-1 min-w-0">
                <p class="font-bold truncate">{{ borrowInfo.itemName }}</p>
                <p class="text-sm text-gray-400">
                  {{ reviewType === 'ITEM' ? '物品' : (isBorrower ? '借出人' : '借入人') }}：
                  {{ reviewType === 'ITEM' ? borrowInfo.itemName : borrowInfo.counterpartyName }}
                </p>
              </div>
            </div>

            <div v-if="reviewType === 'ITEM'">
              <label class="block text-sm font-bold text-gray-700 mb-3">物品状况</label>
              <div class="grid grid-cols-2 gap-2">
                <button
                  v-for="tag in ratingTags"
                  :key="tag.value"
                  @click="selectedTag = tag.value"
                  :class="[
                    'p-3 rounded-xl border-2 transition-all text-left',
                    selectedTag === tag.value
                      ? 'border-[#E2B04D] bg-[#E2B04D]/10'
                      : 'border-gray-200 hover:border-gray-300'
                  ]"
                >
                  <div class="flex items-center space-x-2">
                    <span class="text-lg">{{ tag.icon }}</span>
                    <span class="text-sm font-medium">{{ tag.label }}</span>
                  </div>
                </button>
              </div>
            </div>

            <div v-if="reviewType === 'USER'">
              <label class="block text-sm font-bold text-gray-700 mb-3">用户评分</label>
              <div class="flex items-center justify-center space-x-2">
                <button
                  v-for="i in 5"
                  :key="i"
                  @click="ratingStar = i"
                  class="text-3xl transition-transform hover:scale-110"
                >
                  <span 
                    class="iconify"
                    :class="i <= ratingStar ? 'text-yellow-400' : 'text-gray-300'"
                    :data-icon="i <= ratingStar ? 'solar:star-bold' : 'solar:star-line-duotone'"
                  ></span>
                </button>
              </div>
              <p class="text-center text-sm text-gray-500 mt-2">
                {{ ratingStar ? ratingTexts[ratingStar] : '点击星星评分' }}
              </p>
            </div>

            <div>
              <label class="block text-sm font-bold text-gray-700 mb-3">评价内容</label>
              <textarea
                v-model="content"
                rows="4"
                :placeholder="reviewType === 'ITEM' ? '分享你的使用体验吧~' : '评价一下这位用户的表现~'"
                maxlength="500"
                class="w-full p-4 border border-gray-200 rounded-2xl resize-none focus:outline-none focus:border-[#E2B04D] focus:ring-1 focus:ring-[#E2B04D] transition-all"
              ></textarea>
              <p class="text-right text-xs text-gray-400 mt-1">{{ content.length }}/500</p>
            </div>
          </div>

          <div class="p-6 border-t border-gray-100 flex space-x-3">
            <button 
              @click="handleClose"
              class="flex-1 py-3 border border-gray-200 rounded-xl font-bold text-gray-600 hover:bg-gray-50 transition-colors"
            >
              取消
            </button>
            <button 
              @click="handleSubmit"
              :disabled="!canSubmit || submitting"
              class="flex-1 py-3 bg-[#E2B04D] text-white rounded-xl font-bold hover:bg-[#C49A2E] transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {{ submitting ? '提交中...' : '提交评价' }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { reviewApi, RATING_TAGS } from '../api/review'

interface BorrowInfo {
  id: number
  itemName: string
  itemImage?: string
  counterpartyName: string
  counterpartyId: number
  isBorrower?: boolean
  defaultReviewType?: 'ITEM' | 'USER'
}

const props = defineProps<{
  visible: boolean
  borrowInfo: BorrowInfo | null
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'success', type: 'ITEM' | 'USER'): void
}>()

const reviewType = ref<'ITEM' | 'USER'>('ITEM')
const selectedTag = ref<string>('')
const ratingStar = ref<number>(0)
const content = ref('')
const submitting = ref(false)

const ratingTags = RATING_TAGS

const ratingTexts: Record<number, string> = {
  1: '非常差',
  2: '较差',
  3: '一般',
  4: '很好',
  5: '非常好'
}

const isBorrower = computed(() => props.borrowInfo?.isBorrower ?? true)
const canSelectItemType = computed(() => isBorrower.value)

const canSubmit = computed(() => {
  if (reviewType.value === 'ITEM') {
    return selectedTag.value && content.value.trim().length > 0
  } else {
    return ratingStar.value > 0 && content.value.trim().length > 0
  }
})

watch(() => props.visible, (val) => {
  if (val) {
    reviewType.value = props.borrowInfo?.defaultReviewType || 'ITEM'
    selectedTag.value = ''
    ratingStar.value = 0
    content.value = ''
    submitting.value = false
  }
})

const handleClose = () => {
  emit('close')
}

const handleSubmit = async () => {
  if (!canSubmit.value || !props.borrowInfo) return
  
  submitting.value = true
  try {
    const data: any = {
      borrowId: props.borrowInfo.id,
      targetType: reviewType.value,
      content: content.value.trim()
    }
    
    if (reviewType.value === 'ITEM') {
      data.ratingTag = selectedTag.value
    } else {
      data.ratingStar = ratingStar.value
    }
    
    await reviewApi.createReview(data)
    alert('评价成功！')
    emit('success', reviewType.value)
    handleClose()
  } catch (error: any) {
    alert(error.message || '评价失败')
  } finally {
    submitting.value = false
  }
}

const getImageUrl = (path: string | undefined) => {
  if (!path) return 'https://modao.cc/agent-py/media/generated_images/2026-03-19/7a697da7cb0e46808f265a42ae7934f3.jpg'
  if (path.startsWith('http')) return path
  if (path.startsWith('/uploads/')) return path
  return `/uploads/${path}`
}
</script>

<style scoped>
.modal-enter-active,
.modal-leave-active {
  transition: all 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from > div:last-child,
.modal-leave-to > div:last-child {
  transform: scale(0.9);
}
</style>
