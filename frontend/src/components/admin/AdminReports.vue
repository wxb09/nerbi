<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-2xl font-bold text-[#3D3426]">举报审核</h2>
        <p class="text-sm text-[#B8AE9E] mt-1">处理用户举报的内容</p>
      </div>
      <div class="flex gap-2">
        <button
          class="px-4 py-2 rounded-xl text-sm font-medium transition-colors"
          :class="filterStatus === '' ? 'bg-[#C9A227] text-white' : 'bg-white border border-[#E8D48B]/30 text-[#8C7D66] hover:bg-[#FFF9EE]'"
          @click="filterStatus = ''"
        >
          全部
        </button>
        <button
          class="px-4 py-2 rounded-xl text-sm font-medium transition-colors"
          :class="filterStatus === 'PENDING' ? 'bg-[#C9A227] text-white' : 'bg-white border border-[#E8D48B]/30 text-[#8C7D66] hover:bg-[#FFF9EE]'"
          @click="filterStatus = 'PENDING'"
        >
          待处理
        </button>
      </div>
    </div>

    <div v-if="loading && reports.length === 0" class="card p-8 text-center">
      <span class="iconify text-4xl text-[#B8AE9E] animate-spin inline-block" data-icon="solar:refresh-bold"></span>
      <p class="text-[#B8AE9E] mt-2">加载中...</p>
    </div>

    <div v-else-if="reports.length === 0" class="card p-8 text-center">
      <span class="iconify text-6xl text-[#B8AE9E]" data-icon="solar:shield-check-bold"></span>
      <p class="text-lg text-[#B8AE9E] mt-4">暂无举报记录</p>
    </div>

    <div v-else class="space-y-4">
      <div
        v-for="report in reports"
        :key="report.id"
        class="card p-6"
      >
        <div class="flex items-start justify-between gap-4">
          <div class="flex-1">
            <div class="flex items-center gap-2 mb-2">
              <span
                class="px-2 py-0.5 rounded-full text-xs font-medium"
                :class="getStatusClass(report.auditResult)"
              >
                {{ getStatusText(report.auditResult) }}
              </span>
              <span class="text-xs text-[#B8AE9E]">{{ report.createdAt }}</span>
            </div>
            <p class="text-sm text-[#3D3426] mb-1">
              <span class="font-medium">举报类型:</span> {{ report.targetType }} #{{ report.targetId }}
            </p>
            <p class="text-sm text-[#8C7D66] mb-2">
              <span class="font-medium">举报原因:</span> {{ report.result }}
            </p>
            <p v-if="report.contentSnapshot" class="text-xs text-gray-400 line-clamp-2">
              内容快照: {{ report.contentSnapshot }}
            </p>
          </div>
          <div v-if="report.auditResult === 'PENDING'" class="flex items-center gap-2 flex-shrink-0">
            <button
              class="px-4 py-2 rounded-lg bg-red-50 text-red-500 hover:bg-red-500 hover:text-white transition-colors text-sm font-medium"
              @click="openResolveModal(report, 'DELETE')"
            >
              删除内容
            </button>
            <button
              class="px-4 py-2 rounded-lg bg-[#FFF9EE] text-[#C9A227] hover:bg-[#C9A227] hover:text-white transition-colors text-sm font-medium"
              @click="openResolveModal(report, 'REJECT')"
            >
              驳回举报
            </button>
          </div>
          <div v-else class="text-xs text-[#B8AE9E] flex-shrink-0">
            处理人: {{ report.handlerId || '-' }}
          </div>
        </div>
      </div>
    </div>

    <Pagination
      v-if="totalPages > 1"
      :page="currentPage"
      :total-pages="totalPages"
      :total-elements="totalElements"
      @change="goToPage"
      class="mt-6"
    />

    <Teleport to="body">
      <Transition name="modal">
        <div
          v-if="showModal"
          class="fixed inset-0 z-50 flex items-center justify-center p-4"
          @click.self="closeModal"
        >
          <div class="absolute inset-0 bg-black/50 backdrop-blur-sm"></div>
          <div class="relative bg-white rounded-2xl w-full max-w-lg shadow-2xl">
            <div class="p-6 border-b border-[#E8D48B]/20">
              <h3 class="text-xl font-bold text-[#3D3426]">
                {{ resolveAction === 'DELETE' ? '删除内容' : '驳回举报' }}
              </h3>
            </div>
            <div class="p-6">
              <p class="text-sm text-[#8C7D66] mb-4">
                {{ resolveAction === 'DELETE' ? '确定要删除该内容吗？删除后内容将不再显示。' : '确定要驳回该举报吗？内容将保持正常显示。' }}
              </p>
              <textarea
                v-model="resolveResult"
                rows="3"
                class="w-full px-4 py-2.5 rounded-xl border border-[#E8D48B]/30 focus:border-[#C9A227] focus:ring-2 focus:ring-[#C9A227]/20 outline-none transition-all resize-none"
                placeholder="请输入处理说明..."
              ></textarea>
            </div>
            <div class="p-6 border-t border-[#E8D48B]/20 flex justify-end gap-3">
              <button
                class="px-5 py-2.5 rounded-xl border border-[#E8D48B]/30 text-[#8C7D66] hover:bg-[#FFF9EE] transition-colors"
                @click="closeModal"
              >
                取消
              </button>
              <button
                class="px-5 py-2.5 rounded-xl font-medium text-white transition-colors disabled:opacity-50"
                :class="resolveAction === 'DELETE' ? 'bg-red-500 hover:bg-red-600' : 'bg-[#C9A227] hover:bg-[#B8911F]'"
                :disabled="!resolveResult.trim() || submitting"
                @click="submitResolve"
              >
                {{ submitting ? '提交中...' : '确认' }}
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { adminApi } from '../../api/admin'
import Pagination from '../common/Pagination.vue'

const loading = ref(true)
const reports = ref<any[]>([])
const currentPage = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)
const filterStatus = ref('')

const showModal = ref(false)
const submitting = ref(false)
const selectedReport = ref<any>(null)
const resolveAction = ref('')
const resolveResult = ref('')

const loadReports = async (page = 0) => {
  try {
    loading.value = true
    const params: any = { page, size: 10 }
    if (filterStatus.value) params.status = filterStatus.value
    const data = await adminApi.getReports(params) as any
    reports.value = data.content || []
    currentPage.value = data.number || 0
    totalPages.value = data.totalPages || 1
    totalElements.value = data.totalElements || 0
  } catch (error) {
    console.error('加载举报失败', error)
  } finally {
    loading.value = false
  }
}

const goToPage = (page: number) => {
  loadReports(page)
}

const openResolveModal = (report: any, action: string) => {
  selectedReport.value = report
  resolveAction.value = action
  resolveResult.value = ''
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  selectedReport.value = null
}

const submitResolve = async () => {
  if (!selectedReport.value || !resolveResult.value.trim()) return
  try {
    submitting.value = true
    await adminApi.resolveReport(selectedReport.value.id, {
      action: resolveAction.value,
      result: resolveResult.value.trim()
    })
    closeModal()
    loadReports(currentPage.value)
  } catch (error) {
    console.error('处理举报失败', error)
  } finally {
    submitting.value = false
  }
}

const getStatusClass = (status: string) => {
  const classes: Record<string, string> = {
    'PENDING': 'bg-yellow-100 text-yellow-600',
    'RESOLVED': 'bg-green-100 text-green-600',
    'REJECTED': 'bg-gray-100 text-gray-600'
  }
  return classes[status] || 'bg-gray-100 text-gray-600'
}

const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    'PENDING': '待处理',
    'RESOLVED': '已处理',
    'REJECTED': '已驳回'
  }
  return texts[status] || status
}

watch(filterStatus, () => {
  loadReports(0)
})

onMounted(() => {
  loadReports()
})
</script>

<style scoped>
.card {
  background: white;
  border-radius: 16px;
  border: 1px solid rgba(201, 162, 39, 0.08);
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

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
  transform: scale(0.95);
}
</style>
