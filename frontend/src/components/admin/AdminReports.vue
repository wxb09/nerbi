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

    <div v-else class="card overflow-hidden">
      <table class="w-full">
        <thead class="bg-[#FFF9EE]">
          <tr class="text-left text-xs font-medium text-[#8C7D66]">
            <th class="px-5 py-3">状态</th>
            <th class="px-5 py-3">举报内容</th>
            <th class="px-5 py-3">举报原因</th>
            <th class="px-5 py-3">举报人</th>
            <th class="px-5 py-3">时间</th>
            <th class="px-5 py-3 text-right">操作</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-50">
          <tr
            v-for="report in reports"
            :key="report.id"
            class="hover:bg-gray-50/50 transition-colors"
          >
            <td class="px-5 py-3">
              <span
                class="px-2 py-0.5 rounded-full text-xs font-medium whitespace-nowrap"
                :class="getStatusClass(report.auditResult)"
              >
                {{ getStatusText(report.auditResult) }}
              </span>
            </td>
            <td class="px-5 py-3">
              <div class="flex items-center gap-2">
                <span class="text-xs text-[#B8AE9E] whitespace-nowrap">{{ report.targetType }}</span>
                <span class="text-sm text-[#3D3426] font-medium">#{{ report.targetId }}</span>
                <button
                  class="text-xs text-[#C9A227] hover:text-[#B8911F] underline"
                  @click="showDetail(report)"
                >
                  查看详情
                </button>
              </div>
            </td>
            <td class="px-5 py-3">
              <p class="text-sm text-[#8C7D66] max-w-[200px] truncate">{{ report.reason }}</p>
            </td>
            <td class="px-5 py-3">
              <span class="text-sm text-[#3D3426]">{{ report.reporterId || '-' }}</span>
            </td>
            <td class="px-5 py-3">
              <span class="text-xs text-[#B8AE9E] whitespace-nowrap">{{ formatDate(report.createdAt) }}</span>
            </td>
            <td class="px-5 py-3 text-right">
              <div v-if="report.auditResult === 'PENDING'" class="flex items-center justify-end gap-2">
                <button
                  class="px-3 py-1.5 rounded-lg bg-red-50 text-red-500 hover:bg-red-500 hover:text-white transition-colors text-xs font-medium"
                  @click="openResolveModal(report, 'DELETE')"
                >
                  删除内容
                </button>
                <button
                  class="px-3 py-1.5 rounded-lg bg-[#FFF9EE] text-[#C9A227] hover:bg-[#C9A227] hover:text-white transition-colors text-xs font-medium"
                  @click="openResolveModal(report, 'REJECT')"
                >
                  驳回
                </button>
              </div>
              <span v-else class="text-xs text-[#B8AE9E]">
                处理人: {{ report.handlerId || '-' }}
              </span>
            </td>
          </tr>
        </tbody>
      </table>
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

    <Teleport to="body">
      <Transition name="modal">
        <div
          v-if="showDetailModal"
          class="fixed inset-0 z-50 flex items-center justify-center p-4"
          @click.self="closeDetailModal"
        >
          <div class="absolute inset-0 bg-black/50 backdrop-blur-sm"></div>
          <div class="relative bg-white rounded-2xl w-full max-w-lg shadow-2xl p-6">
            <div class="flex items-center justify-between mb-4">
              <h3 class="text-lg font-bold text-[#3D3426]">举报详情</h3>
              <button
                class="w-8 h-8 rounded-full hover:bg-gray-100 flex items-center justify-center transition-colors"
                @click="closeDetailModal"
              >
                <span class="iconify text-lg text-gray-400" data-icon="solar:close-circle-bold"></span>
              </button>
            </div>
            <div class="space-y-3">
              <div class="flex items-center gap-2">
                <span class="text-xs text-[#B8AE9E] w-16">举报类型</span>
                <span class="text-sm text-[#3D3426] font-medium">{{ selectedDetail?.targetType }}</span>
              </div>
              <div class="flex items-center gap-2">
                <span class="text-xs text-[#B8AE9E] w-16">目标ID</span>
                <span class="text-sm text-[#3D3426]">#{{ selectedDetail?.targetId }}</span>
              </div>
              <div class="flex items-center gap-2">
                <span class="text-xs text-[#B8AE9E] w-16">状态</span>
                <span
                  class="px-2 py-0.5 rounded-full text-xs font-medium"
                  :class="getStatusClass(selectedDetail?.auditResult)"
                >
                  {{ getStatusText(selectedDetail?.auditResult) }}
                </span>
              </div>
              <div class="flex items-center gap-2">
                <span class="text-xs text-[#B8AE9E] w-16">举报人</span>
                <span class="text-sm text-[#3D3426]">{{ selectedDetail?.reporterId || '-' }}</span>
              </div>
              <div class="flex items-center gap-2">
                <span class="text-xs text-[#B8AE9E] w-16">举报时间</span>
                <span class="text-sm text-[#3D3426]">{{ selectedDetail?.createdAt }}</span>
              </div>
              <div class="pt-2 border-t border-gray-100">
                <span class="text-xs text-[#B8AE9E]">举报原因</span>
                <p class="text-sm text-[#3D3426] mt-1">{{ selectedDetail?.reason || '-' }}</p>
              </div>
              <div v-if="selectedDetail?.contentSnapshot" class="pt-2 border-t border-gray-100">
                <span class="text-xs text-[#B8AE9E]">举报内容</span>
                <p class="text-sm text-[#8C7D66] mt-1">{{ selectedDetail?.contentSnapshot }}</p>
              </div>
              <div v-if="selectedDetail?.handlerId" class="pt-2 border-t border-gray-100">
                <span class="text-xs text-[#B8AE9E]">处理信息</span>
                <p class="text-sm text-[#8C7D66] mt-1">
                  处理人: {{ selectedDetail?.handlerId }}<br>
                  处理结果: {{ selectedDetail?.result }}
                </p>
              </div>
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

const showDetailModal = ref(false)
const selectedDetail = ref<any>(null)

const loadReports = async (page = 0) => {
  try {
    loading.value = true
    const params: any = { page, size: 15 }
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

const showDetail = (report: any) => {
  selectedDetail.value = report
  showDetailModal.value = true
}

const closeDetailModal = () => {
  showDetailModal.value = false
  selectedDetail.value = null
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

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
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

.truncate {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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
