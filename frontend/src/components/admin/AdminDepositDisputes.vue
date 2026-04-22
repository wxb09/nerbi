<template>
  <div>
    <div class="flex items-center justify-between mb-5">
      <div>
        <h3 class="text-base font-bold text-[#3D3426]">押金纠纷管理</h3>
        <p class="text-xs text-[#B8AE9E] mt-0.5">处理押金扣款纠纷，支持部分扣款</p>
      </div>
      <div class="flex gap-2">
        <select v-model="statusFilter" class="px-3 py-2 bg-[#FFF9EE] border border-[#E8D48B]/30 rounded-xl text-sm outline-none" @change="loadDisputes">
          <option value="">全部状态</option>
          <option value="PENDING">待处理</option>
          <option value="PROCESSING">处理中</option>
          <option value="APPROVED">已同意扣款</option>
          <option value="REJECTED">已驳回</option>
        </select>
      </div>
    </div>

    <div class="grid grid-cols-4 gap-4 mb-5">
      <div class="card p-4 text-center">
        <p class="text-2xl font-bold text-[#D4942A]">{{ stats.pendingCount }}</p>
        <p class="text-xs text-[#9A9082]">待处理</p>
      </div>
      <div class="card p-4 text-center">
        <p class="text-2xl font-bold text-blue-600">{{ stats.processingCount }}</p>
        <p class="text-xs text-[#9A9082]">处理中</p>
      </div>
      <div class="card p-4 text-center">
        <p class="text-2xl font-bold text-emerald-600">{{ stats.approvedCount }}</p>
        <p class="text-xs text-[#9A9082]">已同意扣款</p>
      </div>
      <div class="card p-4 text-center">
        <p class="text-2xl font-bold text-[#9A9082]">{{ stats.rejectedCount }}</p>
        <p class="text-xs text-[#9A9082]">已驳回</p>
      </div>
    </div>

    <div v-if="loading" class="text-center py-12 text-[#9A9082]">加载中...</div>
    <div v-else-if="error" class="text-center py-12 text-[#D4644A]">{{ error }}</div>
    <template v-else>
      <div class="card overflow-hidden">
        <table class="data-table w-full">
          <thead>
            <tr>
              <th>纠纷ID</th>
              <th>物品</th>
              <th>发起人</th>
              <th>类型</th>
              <th>申请扣款</th>
              <th>押金</th>
              <th>状态</th>
              <th>提交时间</th>
              <th class="text-center">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="d in disputes" :key="d.id">
              <td class="font-semibold text-sm">#{{ d.id }}</td>
              <td class="text-sm">{{ d.itemName || '-' }}</td>
              <td class="text-sm">
                <span class="text-xs px-1.5 py-0.5 rounded" :class="d.initiatorType === 'LENDER' ? 'bg-blue-50 text-blue-600' : 'bg-purple-50 text-purple-600'">
                  {{ d.initiatorType === 'LENDER' ? '借出者' : '借入者' }}
                </span>
                {{ d.initiatorName }}
              </td>
              <td><span class="text-xs font-medium px-2 py-0.5 rounded-full" :class="disputeTypeClass(d.disputeType)">{{ disputeTypeLabel(d.disputeType) }}</span></td>
              <td class="text-sm font-medium text-[#E2B04D]">￥{{ d.claimAmount }}</td>
              <td class="text-sm text-blue-600">￥{{ d.depositAmount }}</td>
              <td><span class="text-xs font-bold px-2 py-0.5 rounded-full" :class="statusClass(d.status)">{{ statusLabel(d.status) }}</span></td>
              <td class="text-sm text-[#9A9082]">{{ formatDate(d.createdAt) }}</td>
              <td>
                <div class="flex items-center gap-2">
                  <button v-if="d.status === 'PENDING' || d.status === 'PROCESSING'" class="action-btn approve" title="处理纠纷" @click="openResolveModal(d)">
                    <span class="iconify text-base" data-icon="solar:check-circle-bold"></span>
                  </button>
                  <button class="action-btn view" title="查看详情" @click="openDetailModal(d)">
                    <span class="iconify text-base" data-icon="solar:eye-bold"></span>
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="disputes.length === 0">
              <td colspan="9" class="text-center py-8 text-[#B8AE9E]">暂无押金纠纷</td>
            </tr>
          </tbody>
        </table>
      </div>
      <Pagination
        v-if="totalPages > 1"
        :page="page"
        :total-pages="totalPages"
        :total-elements="totalElements"
        @change="goToPage"
        class="mt-5"
      />
    </template>

    <Teleport to="body">
      <div v-if="resolveModal.show" class="modal-overlay" @click.self="resolveModal.show = false">
        <div class="modal-content max-w-lg">
          <div class="flex items-center justify-between mb-5">
            <h3 class="text-lg font-bold text-[#3D3426]">处理押金纠纷</h3>
            <button class="p-1 hover:bg-[#FFF9EE] rounded-lg" @click="resolveModal.show = false">
              <span class="iconify text-xl text-[#9A9082]" data-icon="solar:close-circle-bold"></span>
            </button>
          </div>
          
          <div class="mb-4 p-4 bg-[#FFF9EE] rounded-xl space-y-2">
            <div class="flex justify-between text-sm">
              <span class="text-[#9A9082]">纠纷ID</span>
              <span class="font-semibold">#{{ resolveModal.id }}</span>
            </div>
            <div class="flex justify-between text-sm">
              <span class="text-[#9A9082]">物品</span>
              <span>{{ resolveModal.itemName }}</span>
            </div>
            <div class="flex justify-between text-sm">
              <span class="text-[#9A9082]">发起人</span>
              <span>{{ resolveModal.initiatorName }}（{{ resolveModal.initiatorType === 'LENDER' ? '借出者' : '借入者' }}）</span>
            </div>
            <div class="flex justify-between text-sm">
              <span class="text-[#9A9082]">纠纷类型</span>
              <span>{{ disputeTypeLabel(resolveModal.disputeType) }}</span>
            </div>
            <div class="flex justify-between text-sm">
              <span class="text-[#9A9082]">申请扣款</span>
              <span class="font-medium text-[#E2B04D]">￥{{ resolveModal.claimAmount }}</span>
            </div>
            <div class="flex justify-between text-sm">
              <span class="text-[#9A9082]">押金总额</span>
              <span class="text-blue-600">￥{{ resolveModal.depositAmount }}</span>
            </div>
            <div class="pt-2 border-t border-[#E8D48B]/30">
              <p class="text-xs text-[#9A9082] mb-1">纠纷描述</p>
              <p class="text-sm">{{ resolveModal.description }}</p>
            </div>
          </div>

          <div class="mb-4">
            <label class="block text-sm font-medium text-[#3D3426] mb-2">处理操作</label>
            <div class="flex gap-2">
              <button 
                class="flex-1 py-2 rounded-xl text-sm font-medium transition-all"
                :class="resolveModal.action === 'investigate' ? 'bg-blue-500 text-white' : 'bg-blue-50 text-blue-600 hover:bg-blue-100'"
                @click="resolveModal.action = 'investigate'"
              >
                开始调查
              </button>
              <button 
                class="flex-1 py-2 rounded-xl text-sm font-medium transition-all"
                :class="resolveModal.action === 'approve' ? 'bg-emerald-500 text-white' : 'bg-emerald-50 text-emerald-600 hover:bg-emerald-100'"
                @click="resolveModal.action = 'approve'"
              >
                同意扣款
              </button>
              <button 
                class="flex-1 py-2 rounded-xl text-sm font-medium transition-all"
                :class="resolveModal.action === 'reject' ? 'bg-red-500 text-white' : 'bg-red-50 text-red-600 hover:bg-red-100'"
                @click="resolveModal.action = 'reject'"
              >
                驳回申请
              </button>
            </div>
          </div>

          <div v-if="resolveModal.action === 'approve'" class="mb-4">
            <label class="block text-sm font-medium text-[#3D3426] mb-2">实际扣款金额 <span class="text-red-500">*</span></label>
            <div class="relative">
              <span class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400">￥</span>
              <input 
                type="number" 
                v-model.number="resolveModal.actualDeduction" 
                class="w-full pl-8 pr-4 py-3 bg-[#FFF9EE] border border-[#E8D48B]/30 rounded-xl text-sm outline-none focus:ring-2 focus:ring-[#C9A227]/30"
                :max="resolveModal.depositAmount"
                min="0.01"
                step="0.01"
              />
            </div>
            <p class="text-xs text-[#9A9082] mt-1">剩余 ￥{{ (resolveModal.depositAmount - (resolveModal.actualDeduction || 0)).toFixed(2) }} 将退还给借入者</p>
          </div>

          <div class="mb-5">
            <label class="block text-sm font-medium text-[#3D3426] mb-2">处理说明</label>
            <textarea v-model="resolveModal.resolution" class="w-full px-4 py-3 bg-[#FFF9EE] border border-[#E8D48B]/30 rounded-xl text-sm outline-none focus:ring-2 focus:ring-[#C9A227]/30 resize-none" rows="3" placeholder="请填写处理说明..."></textarea>
          </div>

          <div class="flex gap-3 justify-end">
            <button class="btn-cancel" @click="resolveModal.show = false">取消</button>
            <button 
              class="btn-submit" 
              :class="resolveModal.action === 'approve' ? 'approve' : resolveModal.action === 'investigate' ? 'warn' : 'reject'" 
              :disabled="resolveModal.submitting || (resolveModal.action === 'approve' && !resolveModal.actualDeduction)" 
              @click="submitResolve"
            >
              {{ resolveModal.submitting ? '处理中...' : submitBtnText }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <Teleport to="body">
      <div v-if="detailModal.show" class="modal-overlay" @click.self="detailModal.show = false">
        <div class="modal-content max-w-lg">
          <div class="flex items-center justify-between mb-5">
            <h3 class="text-lg font-bold text-[#3D3426]">纠纷详情</h3>
            <button class="p-1 hover:bg-[#FFF9EE] rounded-lg" @click="detailModal.show = false">
              <span class="iconify text-xl text-[#9A9082]" data-icon="solar:close-circle-bold"></span>
            </button>
          </div>
          
          <div class="space-y-4">
            <div class="p-4 bg-[#FFF9EE] rounded-xl space-y-2">
              <div class="flex justify-between text-sm">
                <span class="text-[#9A9082]">纠纷ID</span>
                <span class="font-semibold">#{{ detailModal.id }}</span>
              </div>
              <div class="flex justify-between text-sm">
                <span class="text-[#9A9082]">物品</span>
                <span>{{ detailModal.itemName }}</span>
              </div>
              <div class="flex justify-between text-sm">
                <span class="text-[#9A9082]">发起人</span>
                <span>{{ detailModal.initiatorName }}（{{ detailModal.initiatorType === 'LENDER' ? '借出者' : '借入者' }}）</span>
              </div>
              <div class="flex justify-between text-sm">
                <span class="text-[#9A9082]">纠纷类型</span>
                <span>{{ disputeTypeLabel(detailModal.disputeType) }}</span>
              </div>
              <div class="flex justify-between text-sm">
                <span class="text-[#9A9082]">状态</span>
                <span class="text-xs font-bold px-2 py-0.5 rounded-full" :class="statusClass(detailModal.status)">{{ statusLabel(detailModal.status) }}</span>
              </div>
            </div>

            <div class="p-4 border border-gray-100 rounded-xl space-y-2">
              <p class="text-xs text-[#9A9082] font-medium">纠纷描述</p>
              <p class="text-sm">{{ detailModal.description }}</p>
            </div>

            <div class="p-4 border border-gray-100 rounded-xl space-y-2">
              <p class="text-xs text-[#9A9082] font-medium">申请信息</p>
              <div class="flex justify-between text-sm">
                <span class="text-[#9A9082]">申请扣款金额</span>
                <span class="font-medium text-[#E2B04D]">￥{{ detailModal.claimAmount }}</span>
              </div>
              <div class="flex justify-between text-sm">
                <span class="text-[#9A9082]">押金总额</span>
                <span class="text-blue-600">￥{{ detailModal.depositAmount }}</span>
              </div>
              <p v-if="detailModal.claimReason" class="text-sm text-[#9A9082] mt-2">申请理由：{{ detailModal.claimReason }}</p>
            </div>

            <div v-if="detailModal.status !== 'PENDING'" class="p-4 border border-gray-100 rounded-xl space-y-2">
              <p class="text-xs text-[#9A9082] font-medium">处理结果</p>
              <div class="flex justify-between text-sm">
                <span class="text-[#9A9082]">处理人</span>
                <span>{{ detailModal.handlerName || '-' }}</span>
              </div>
              <div v-if="detailModal.actualDeduction" class="flex justify-between text-sm">
                <span class="text-[#9A9082]">实际扣款</span>
                <span class="font-medium text-red-500">￥{{ detailModal.actualDeduction }}</span>
              </div>
              <p v-if="detailModal.resolution" class="text-sm text-[#9A9082] mt-2">处理说明：{{ detailModal.resolution }}</p>
              <p v-if="detailModal.handledAt" class="text-xs text-[#9A9082] mt-2">处理时间：{{ formatDate(detailModal.handledAt) }}</p>
            </div>
          </div>

          <div class="flex gap-3 justify-end mt-5">
            <button class="btn-cancel" @click="detailModal.show = false">关闭</button>
          </div>
        </div>
      </div>
    </Teleport>

    <Teleport to="body">
      <div v-if="toast.show" class="toast-container" :class="toast.type">
        <span class="iconify text-lg" :data-icon="toast.type === 'success' ? 'solar:check-circle-bold' : 'solar:danger-circle-bold'"></span>
        <span class="text-sm font-medium">{{ toast.message }}</span>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { adminApi, type DepositDispute } from '../../api/admin'
import Pagination from '../common/Pagination.vue'

const loading = ref(true)
const error = ref('')

const disputes = ref<DepositDispute[]>([])
const page = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)
const statusFilter = ref('')

const stats = reactive({
  pendingCount: 0,
  processingCount: 0,
  approvedCount: 0,
  rejectedCount: 0
})

const resolveModal = reactive({
  show: false,
  id: 0,
  itemName: '',
  initiatorName: '',
  initiatorType: '' as 'LENDER' | 'BORROWER',
  disputeType: '' as string,
  description: '',
  claimAmount: 0,
  depositAmount: 0,
  action: 'investigate' as string,
  actualDeduction: null as number | null,
  resolution: '',
  submitting: false
})

const detailModal = reactive({
  show: false,
  id: 0,
  itemName: '',
  initiatorName: '',
  initiatorType: '' as 'LENDER' | 'BORROWER',
  disputeType: '' as string,
  description: '',
  claimAmount: 0,
  claimReason: '',
  depositAmount: 0,
  status: '' as string,
  actualDeduction: null as number | null,
  resolution: '',
  handlerName: '',
  handledAt: ''
})

const toast = reactive({ show: false, message: '', type: 'success' as string })

const submitBtnText = computed(() => {
  switch (resolveModal.action) {
    case 'investigate': return '开始调查'
    case 'approve': return '确认扣款'
    case 'reject': return '确认驳回'
    default: return '确认'
  }
})

function showToast(message: string, type: string = 'success') {
  toast.message = message; toast.type = type; toast.show = true
  setTimeout(() => { toast.show = false }, 2500)
}

function formatDate(dateStr: string | null) { 
  return dateStr ? dateStr.substring(0, 16).replace('T', ' ') : '-' 
}

function disputeTypeLabel(t: string) {
  const m: Record<string, string> = { DAMAGE: '物品损坏', LOSS: '物品丢失', OVERDUE: '逾期未还', MISSING_PARTS: '配件缺失', OTHER: '其他争议' }
  return m[t] || t
}

function disputeTypeClass(t: string) {
  const m: Record<string, string> = { DAMAGE: 'bg-red-50 text-red-600', LOSS: 'bg-red-100 text-red-700', OVERDUE: 'bg-amber-50 text-amber-600', MISSING_PARTS: 'bg-blue-50 text-blue-600', OTHER: 'bg-gray-50 text-gray-600' }
  return m[t] || 'bg-gray-50 text-gray-600'
}

function statusLabel(s: string) {
  const m: Record<string, string> = { PENDING: '待处理', PROCESSING: '处理中', APPROVED: '已同意扣款', REJECTED: '已驳回', CANCELLED: '已撤销' }
  return m[s] || s
}

function statusClass(s: string) {
  const m: Record<string, string> = { PENDING: 'bg-[#FFF5E3] text-[#D4942A]', PROCESSING: 'bg-blue-50 text-blue-600', APPROVED: 'bg-emerald-50 text-emerald-600', REJECTED: 'bg-[#F5F0EA] text-[#9A9082]', CANCELLED: 'bg-gray-100 text-gray-500' }
  return m[s] || 'bg-[#F5F0EA] text-[#9A9082]'
}

async function loadStats() {
  try {
    const data = await adminApi.getDepositDisputeStats() as any
    stats.pendingCount = data.pendingCount || 0
    stats.processingCount = data.processingCount || 0
    stats.approvedCount = data.approvedCount || 0
    stats.rejectedCount = data.rejectedCount || 0
  } catch (e) {
    console.error('加载统计失败', e)
  }
}

async function loadDisputes() {
  try {
    const params: any = { page: page.value, size: 12 }
    if (statusFilter.value) params.status = statusFilter.value
    const data = await adminApi.getDepositDisputes(params) as any
    disputes.value = data.content || []
    totalPages.value = data.totalPages || 1
    totalElements.value = data.totalElements || 0
  } catch (e: any) { 
    error.value = e.message || '加载失败' 
  }
}

function goToPage(targetPage: number) {
  page.value = targetPage
  loadDisputes()
}

function openResolveModal(d: DepositDispute) {
  resolveModal.id = d.id
  resolveModal.itemName = d.itemName || '-'
  resolveModal.initiatorName = d.initiatorName
  resolveModal.initiatorType = d.initiatorType
  resolveModal.disputeType = d.disputeType
  resolveModal.description = d.description
  resolveModal.claimAmount = d.claimAmount
  resolveModal.depositAmount = d.depositAmount
  resolveModal.action = 'investigate'
  resolveModal.actualDeduction = d.claimAmount
  resolveModal.resolution = ''
  resolveModal.submitting = false
  resolveModal.show = true
}

function openDetailModal(d: DepositDispute) {
  detailModal.id = d.id
  detailModal.itemName = d.itemName || '-'
  detailModal.initiatorName = d.initiatorName
  detailModal.initiatorType = d.initiatorType
  detailModal.disputeType = d.disputeType
  detailModal.description = d.description
  detailModal.claimAmount = d.claimAmount
  detailModal.claimReason = d.claimReason || ''
  detailModal.depositAmount = d.depositAmount
  detailModal.status = d.status
  detailModal.actualDeduction = d.actualDeduction
  detailModal.resolution = d.resolution || ''
  detailModal.handlerName = d.handlerName || ''
  detailModal.handledAt = d.handledAt || ''
  detailModal.show = true
}

async function submitResolve() {
  if (resolveModal.action === 'approve' && (!resolveModal.actualDeduction || resolveModal.actualDeduction <= 0)) {
    showToast('请输入扣款金额', 'error')
    return
  }
  
  resolveModal.submitting = true
  try {
    await adminApi.resolveDepositDispute(resolveModal.id, {
      action: resolveModal.action,
      actualDeduction: resolveModal.action === 'approve' ? resolveModal.actualDeduction! : undefined,
      resolution: resolveModal.resolution || undefined
    })
    resolveModal.show = false
    showToast(resolveModal.action === 'approve' ? '扣款已执行' : resolveModal.action === 'investigate' ? '已开始调查' : '申请已驳回')
    await Promise.all([loadDisputes(), loadStats()])
  } catch (e: any) { 
    showToast(e.message || '处理失败', 'error') 
  }
  finally { resolveModal.submitting = false }
}

onMounted(async () => {
  try { 
    await Promise.all([loadDisputes(), loadStats()]) 
  }
  finally { loading.value = false }
})
</script>

<style scoped>
.card { background: white; border-radius: 20px; border: 1px solid rgba(201, 162, 39, 0.08); }
.data-table th { background: #FFF5E3; color: #8C7D66; font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.06em; padding: 14px 16px; text-align: left; }
.data-table td { padding: 14px 16px; border-bottom: 1px solid #FFF5E3; vertical-align: middle; font-size: 14px; }
.data-table tbody tr { transition: background 0.2s; }
.data-table tbody tr:hover { background: #FFFBF5; }
.action-btn { padding: 8px; border-radius: 10px; transition: all 0.2s; cursor: pointer; display: inline-flex; border: none; }
.action-btn.approve { background: #ECFDF5; color: #059669; }
.action-btn.approve:hover { background: #D1FAE5; }
.action-btn.reject { background: #FEF2F2; color: #DC2626; }
.action-btn.reject:hover { background: #FEE2E2; }
.action-btn.view { background: #F5F0EA; color: #9A9082; }
.action-btn.view:hover { background: #EEE8DD; }
.action-btn.warn { background: #FFF9EE; color: #D4942A; }
.action-btn.warn:hover { background: #FFF5D9; }
.page-btn { padding: 6px 14px; border-radius: 10px; font-size: 13px; background: #FFF9EE; color: #C9A227; border: none; cursor: pointer; transition: all 0.2s; }
.page-btn:hover { background: #FFF5D9; }
.page-btn.disabled { background: #F5F0EA; color: #C4B69A; cursor: not-allowed; }
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.4); backdrop-filter: blur(4px); z-index: 1000; display: flex; align-items: center; justify-content: center; }
.modal-content { background: white; border-radius: 24px; padding: 32px; width: 90%; max-width: 480px; box-shadow: 0 25px 60px rgba(0,0,0,0.15); animation: slideIn 0.25s ease; }
@keyframes slideIn { from { transform: translateY(20px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }
.btn-cancel { padding: 10px 22px; border-radius: 14px; font-size: 14px; font-weight: 600; background: #F5F0EA; color: #9A9082; border: none; cursor: pointer; transition: all 0.2s; }
.btn-cancel:hover { background: #EEE8DD; }
.btn-submit { padding: 10px 22px; border-radius: 14px; font-size: 14px; font-weight: 600; color: white; border: none; cursor: pointer; transition: all 0.2s; }
.btn-submit.approve { background: linear-gradient(135deg, #4A9D6E, #3D8A5F); box-shadow: 0 4px 12px rgba(74,157,110,0.3); }
.btn-submit.reject { background: linear-gradient(135deg, #D4644A, #C0503A); box-shadow: 0 4px 12px rgba(212,100,74,0.3); }
.btn-submit.warn { background: linear-gradient(135deg, #3B82F6, #2563EB); box-shadow: 0 4px 12px rgba(59,130,246,0.3); }
.btn-submit:disabled { opacity: 0.6; cursor: not-allowed; }
.toast-container { position: fixed; top: 24px; right: 24px; z-index: 2000; display: flex; align-items: center; gap: 8px; padding: 14px 20px; border-radius: 16px; animation: slideIn 0.25s ease; box-shadow: 0 8px 24px rgba(0,0,0,0.12); }
.toast-container.success { background: #ECFDF5; color: #059669; }
.toast-container.error { background: #FEF2F2; color: #DC2626; }
</style>
