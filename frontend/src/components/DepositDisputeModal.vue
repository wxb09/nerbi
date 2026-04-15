<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="visible" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-black/50" @click="handleClose"></div>

        <div class="relative bg-white rounded-3xl w-full max-w-lg shadow-2xl overflow-hidden max-h-[90vh] flex flex-col">
          <div class="p-6 border-b border-gray-100 flex-shrink-0">
            <div class="flex items-center justify-between">
              <h3 class="text-xl font-bold">申请押金纠纷</h3>
              <button @click="handleClose" class="text-gray-400 hover:text-gray-600 transition-colors">
                <span class="iconify text-2xl" data-icon="solar:close-circle-bold"></span>
              </button>
            </div>
          </div>

          <div class="p-6 space-y-5 overflow-y-auto flex-1">
            <div v-if="borrowInfo" class="flex items-center space-x-4 p-4 bg-gray-50 rounded-2xl">
              <div class="w-14 h-14 rounded-xl bg-gray-200 flex-shrink-0 overflow-hidden">
                <img v-if="borrowInfo.itemImage" :src="getImageUrl(borrowInfo.itemImage)" class="w-full h-full object-cover" />
                <span v-else class="iconify text-xl text-gray-300 w-full h-full flex items-center justify-center" data-icon="solar:box-bold"></span>
              </div>
              <div class="flex-1 min-w-0">
                <p class="font-bold truncate">{{ borrowInfo.itemName }}</p>
                <p class="text-sm text-gray-400">押金：￥{{ borrowInfo.deposit || 0 }}</p>
              </div>
            </div>

            <div class="space-y-2">
              <label class="block text-sm font-medium text-gray-700">纠纷类型 <span class="text-red-500">*</span></label>
              <select v-model="form.disputeType" class="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl text-sm outline-none focus:ring-2 focus:ring-[#E2B04D]/30">
                <option value="">请选择纠纷类型</option>
                <option value="DAMAGE">物品损坏</option>
                <option value="LOSS">物品丢失</option>
                <option value="OVERDUE">逾期未还</option>
                <option value="MISSING_PARTS">配件缺失</option>
                <option value="OTHER">其他争议</option>
              </select>
            </div>

            <div class="space-y-2">
              <label class="block text-sm font-medium text-gray-700">纠纷描述 <span class="text-red-500">*</span></label>
              <textarea 
                v-model="form.description" 
                class="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl text-sm outline-none focus:ring-2 focus:ring-[#E2B04D]/30 resize-none"
                rows="3"
                placeholder="请详细描述纠纷情况..."
              ></textarea>
            </div>

            <div class="space-y-2">
              <label class="block text-sm font-medium text-gray-700">申请扣款金额 <span class="text-red-500">*</span></label>
              <div class="relative">
                <span class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400">￥</span>
                <input 
                  type="number" 
                  v-model.number="form.claimAmount" 
                  class="w-full pl-8 pr-4 py-3 bg-gray-50 border border-gray-200 rounded-xl text-sm outline-none focus:ring-2 focus:ring-[#E2B04D]/30"
                  :max="borrowInfo?.deposit || 0"
                  min="0.01"
                  step="0.01"
                />
              </div>
              <p class="text-xs text-gray-400">押金总额：￥{{ borrowInfo?.deposit || 0 }}，申请金额不能超过押金</p>
            </div>

            <div class="space-y-2">
              <label class="block text-sm font-medium text-gray-700">申请理由</label>
              <textarea 
                v-model="form.claimReason" 
                class="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl text-sm outline-none focus:ring-2 focus:ring-[#E2B04D]/30 resize-none"
                rows="2"
                placeholder="请说明申请扣款的理由..."
              ></textarea>
            </div>

            <div class="p-3 bg-amber-50 rounded-xl flex items-start gap-2">
              <span class="iconify text-amber-500 text-lg flex-shrink-0 mt-0.5" data-icon="solar:info-circle-bold"></span>
              <p class="text-xs text-amber-700 leading-relaxed">提交后管理员将介入处理，处理结果将通过消息通知您。</p>
            </div>
          </div>

          <div class="p-6 border-t border-gray-100 space-y-3 flex-shrink-0">
            <button
              @click="handleSubmit"
              :disabled="submitting || !isFormValid"
              class="w-full py-3.5 bg-[#E2B04D] text-white rounded-xl font-bold hover:bg-[#C49A2E] transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
            >
              <span v-if="submitting" class="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></span>
              <span class="iconify" v-else data-icon="solar:shield-warning-bold"></span>
              {{ submitting ? '提交中...' : '提交申请' }}
            </button>
            <button
              @click="handleClose"
              class="w-full py-3 border border-gray-200 rounded-xl text-gray-500 text-sm hover:bg-gray-50 transition-colors"
            >
              取消
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed, watch, reactive } from 'vue'
import { depositDisputeApi } from '../api/depositDispute'

interface BorrowInfo {
  id: number
  itemName: string
  itemImage?: string
  counterpartyName: string
  deposit: number
}

const props = defineProps<{
  visible: boolean
  borrowInfo: BorrowInfo | null
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'success'): void
}>()

const submitting = ref(false)

const form = reactive({
  disputeType: '',
  description: '',
  claimAmount: 0,
  claimReason: ''
})

const isFormValid = computed(() => {
  return form.disputeType && 
         form.description.trim() && 
         form.claimAmount > 0 && 
         form.claimAmount <= (props.borrowInfo?.deposit || 0)
})

watch(() => props.visible, (val) => {
  if (val) {
    submitting.value = false
    form.disputeType = ''
    form.description = ''
    form.claimAmount = 0
    form.claimReason = ''
  }
})

const handleClose = () => {
  emit('close')
}

const handleSubmit = async () => {
  if (!props.borrowInfo || submitting.value || !isFormValid.value) return
  
  submitting.value = true
  try {
    await depositDisputeApi.createDispute({
      borrowId: props.borrowInfo.id,
      disputeType: form.disputeType,
      description: form.description,
      claimAmount: form.claimAmount,
      claimReason: form.claimReason || undefined
    })
    alert('押金纠纷申请已提交，请等待管理员处理')
    emit('success')
    handleClose()
  } catch (error: any) {
    alert(error.message || '提交失败')
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
