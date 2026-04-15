<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="visible" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-black/50" @click="handleClose"></div>

        <div class="relative bg-white rounded-3xl w-full max-w-md shadow-2xl overflow-hidden">
          <div class="p-6 border-b border-gray-100">
            <div class="flex items-center justify-between">
              <h3 class="text-xl font-bold">确认取货并支付</h3>
              <button @click="handleClose" class="text-gray-400 hover:text-gray-600 transition-colors">
                <span class="iconify text-2xl" data-icon="solar:close-circle-bold"></span>
              </button>
            </div>
          </div>

          <div class="p-6 space-y-5">
            <div v-if="borrowInfo" class="flex items-center space-x-4 p-4 bg-gray-50 rounded-2xl">
              <div class="w-16 h-16 rounded-xl bg-gray-200 flex-shrink-0 overflow-hidden">
                <img v-if="borrowInfo.itemImage" :src="getImageUrl(borrowInfo.itemImage)" class="w-full h-full object-cover" />
                <span v-else class="iconify text-2xl text-gray-300 w-full h-full flex items-center justify-center" data-icon="solar:box-bold"></span>
              </div>
              <div class="flex-1 min-w-0">
                <p class="font-bold truncate">{{ borrowInfo.itemName }}</p>
                <p class="text-sm text-gray-400">借出人：{{ borrowInfo.counterpartyName }}</p>
                <p class="text-xs text-gray-400">{{ borrowInfo.startTime }} ~ {{ borrowInfo.endTime }}</p>
              </div>
            </div>

            <div class="p-4 rounded-2xl border border-gray-100 bg-white space-y-3">
              <div class="flex items-center gap-2">
                <span class="iconify text-lg text-[#E2B04D]" data-icon="solar:document-text-bold"></span>
                <h4 class="font-bold text-sm text-[#2D3436]">费用明细</h4>
              </div>
              <div class="space-y-2">
                <div class="flex justify-between text-sm">
                  <span class="text-gray-500">日租金</span>
                  <span class="font-medium">￥{{ borrowInfo?.pricePerDay || 0 }}/天</span>
                </div>
                <div class="flex justify-between text-sm">
                  <span class="text-gray-500">借阅天数</span>
                  <span class="font-medium">{{ borrowInfo?.borrowDays || 1 }} 天</span>
                </div>
                <div class="flex justify-between text-sm">
                  <span class="text-gray-500">租金小计</span>
                  <span class="font-medium text-[#E2B04D]">￥{{ rentAmount }}</span>
                </div>
                <div class="flex justify-between text-sm">
                  <span class="text-gray-500">押金</span>
                  <span class="font-medium text-blue-600">￥{{ borrowInfo?.deposit || 0 }}</span>
                </div>
                <div class="border-t border-gray-100 pt-2 flex justify-between">
                  <span class="font-bold text-sm">合计支付</span>
                  <span class="font-bold text-lg text-[#E2B04D]">￥{{ totalAmount }}</span>
                </div>
              </div>
            </div>

            <div class="p-3 bg-amber-50 rounded-xl flex items-start gap-2">
              <span class="iconify text-amber-500 text-lg flex-shrink-0 mt-0.5" data-icon="solar:info-circle-bold"></span>
              <p class="text-xs text-amber-700 leading-relaxed">取货前需完成支付，押金将在物品归还后退还给您。</p>
            </div>
          </div>

          <div class="p-6 border-t border-gray-100 space-y-3">
            <button
              @click="handlePay"
              :disabled="paying"
              class="w-full py-3.5 bg-[#E2B04D] text-white rounded-xl font-bold hover:bg-[#C49A2E] transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
            >
              <span v-if="paying" class="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></span>
              <span class="iconify" v-else data-icon="solar:wallet-bold"></span>
              {{ paying ? '正在跳转...' : '去支付' }}
            </button>
            <button
              @click="handleSkip"
              :disabled="skipping"
              class="w-full py-3 border border-dashed border-gray-300 rounded-xl text-gray-400 text-sm hover:border-gray-400 hover:text-gray-500 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {{ skipping ? '处理中...' : '跳过支付（测试用）' }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { paymentApi } from '../api/payment'

interface BorrowInfo {
  id: number
  itemName: string
  itemImage?: string
  counterpartyName: string
  pricePerDay: number
  deposit: number
  borrowDays: number
  startTime: string
  endTime: string
}

const props = defineProps<{
  visible: boolean
  borrowInfo: BorrowInfo | null
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'success'): void
}>()

const paying = ref(false)
const skipping = ref(false)

const rentAmount = computed(() => {
  const days = props.borrowInfo?.borrowDays || 1
  const price = props.borrowInfo?.pricePerDay || 0
  return (price * days).toFixed(2)
})

const totalAmount = computed(() => {
  const rent = parseFloat(rentAmount.value)
  const deposit = props.borrowInfo?.deposit || 0
  return (rent + deposit).toFixed(2)
})

watch(() => props.visible, (val) => {
  if (val) {
    paying.value = false
    skipping.value = false
  }
})

const handleClose = () => {
  emit('close')
}

const handlePay = async () => {
  if (!props.borrowInfo || paying.value) return
  paying.value = true
  try {
    const res = await paymentApi.createPayment(props.borrowInfo.id) as any
    if (res.paymentForm) {
      const div = document.createElement('div')
      div.innerHTML = res.paymentForm
      document.body.appendChild(div)
      const form = div.querySelector('form')
      if (form) {
        form.submit()
      } else {
        alert('支付表单生成异常')
      }
    }
  } catch (error: any) {
    alert(error.message || '创建支付失败')
  } finally {
    paying.value = false
  }
}

const handleSkip = async () => {
  if (!props.borrowInfo || skipping.value) return
  skipping.value = true
  try {
    await paymentApi.skipPayment(props.borrowInfo.id)
    alert('已跳过支付，借阅已激活')
    emit('success')
    handleClose()
  } catch (error: any) {
    alert(error.message || '操作失败')
  } finally {
    skipping.value = false
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
