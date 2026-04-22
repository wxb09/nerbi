<template>
  <div>
    <div class="flex items-center justify-between mb-5">
      <div>
        <h3 class="text-base font-bold text-[#3D3426]">借阅纠纷</h3>
        <p class="text-xs text-[#B8AE9E] mt-0.5">处理借阅过程中的纠纷与争议</p>
      </div>
      <div class="flex gap-2">
        <select v-model="disputeStatusFilter" class="px-3 py-2 bg-[#FFF9EE] border border-[#E8D48B]/30 rounded-xl text-sm outline-none" @change="loadDisputes">
          <option value="">全部纠纷</option>
          <option value="PENDING">待处理</option>
          <option value="INVESTIGATING">调查中</option>
          <option value="RESOLVED">已解决</option>
          <option value="DISMISSED">已驳回</option>
        </select>
        <select v-model="borrowStatusFilter" class="px-3 py-2 bg-[#FFF9EE] border border-[#E8D48B]/30 rounded-xl text-sm outline-none" @change="loadBorrows">
          <option value="">全部借阅</option>
          <option value="DISPUTED">纠纷中</option>
          <option value="OVERDUE">已超期</option>
          <option value="ACTIVE">借用中</option>
          <option value="RETURN_REQUESTED">申请归还</option>
        </select>
      </div>
    </div>

    <div class="flex gap-2 mb-5 bg-[#FBF7F0] p-1 rounded-xl w-fit">
      <button class="tab-btn" :class="activeTab === 'disputes' ? 'active' : ''" @click="activeTab = 'disputes'">纠纷列表</button>
      <button class="tab-btn" :class="activeTab === 'borrows' ? 'active' : ''" @click="activeTab = 'borrows'">借阅记录</button>
    </div>

    <div v-if="loading" class="text-center py-12 text-[#9A9082]">加载中...</div>
    <div v-else-if="error" class="text-center py-12 text-[#D4644A]">{{ error }}</div>
    <template v-else>
      <div v-if="activeTab === 'disputes'">
        <div class="card overflow-hidden">
          <table class="data-table w-full">
            <thead>
              <tr>
                <th>纠纷ID</th>
                <th>申诉人</th>
                <th>物品</th>
                <th>原因</th>
                <th>状态</th>
                <th>提交时间</th>
                <th class="text-center">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="d in disputes" :key="d.id">
                <td class="font-semibold text-sm">#{{ d.id }}</td>
                <td class="text-sm">{{ d.reporterNickname || '-' }}</td>
                <td class="text-sm">{{ d.itemName || '-' }}</td>
                <td class="text-sm max-w-[200px] truncate" :title="d.reason">{{ d.reason }}</td>
                <td><span class="text-xs font-bold px-2 py-0.5 rounded-full" :class="disputeStatusClass(d.status)">{{ disputeStatusLabel(d.status) }}</span></td>
                <td class="text-sm text-[#9A9082]">{{ formatDate(d.createdAt) }}</td>
                <td>
                  <div class="flex items-center gap-2">
                    <button v-if="d.status === 'PENDING'" class="action-btn approve" title="解决纠纷" @click="openResolveModal(d, 'resolve')">
                      <span class="iconify text-base" data-icon="solar:check-circle-bold"></span>
                    </button>
                    <button v-if="d.status === 'PENDING'" class="action-btn reject" title="驳回纠纷" @click="openResolveModal(d, 'dismiss')">
                      <span class="iconify text-base" data-icon="solar:close-circle-bold"></span>
                    </button>
                  </div>
                </td>
              </tr>
              <tr v-if="disputes.length === 0">
                <td colspan="7" class="text-center py-8 text-[#B8AE9E]">暂无纠纷</td>
              </tr>
            </tbody>
          </table>
        </div>
        <div v-if="disputeTotalPages > 1" class="flex items-center justify-center gap-2 mt-5">
          <button class="page-btn" :class="disputePage > 0 ? '' : 'disabled'" :disabled="disputePage <= 0" @click="disputePage--; loadDisputes()">上一页</button>
          <span class="text-sm text-[#9A9082]">{{ disputePage + 1 }} / {{ disputeTotalPages }}</span>
          <button class="page-btn" :class="disputePage < disputeTotalPages - 1 ? '' : 'disabled'" :disabled="disputePage >= disputeTotalPages - 1" @click="disputePage++; loadDisputes()">下一页</button>
        </div>
      </div>

      <div v-else>
        <div class="card overflow-hidden">
          <table class="data-table w-full">
            <thead>
              <tr>
                <th>借阅ID</th>
                <th>物品</th>
                <th>借入者</th>
                <th>借出者</th>
                <th>日期</th>
                <th>状态</th>
                <th class="text-center">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="b in borrows" :key="b.id">
                <td class="font-semibold text-sm">#{{ b.id }}</td>
                <td class="text-sm">{{ b.itemName || '-' }}</td>
                <td class="text-sm">{{ b.borrowerNickname || '-' }}</td>
                <td class="text-sm">{{ b.lenderNickname || '-' }}</td>
                <td class="text-sm text-[#9A9082]">{{ b.startDate }} ~ {{ b.endDate }}</td>
                <td><span class="text-xs font-bold px-2 py-0.5 rounded-full" :class="borrowStatusClass(b.status)">{{ borrowStatusLabel(b.status) }}</span></td>
                <td>
                  <div class="flex items-center gap-1.5">
                    <button v-if="b.status !== 'DISPUTED' && b.status !== 'RETURNED' && b.status !== 'CANCELLED'" class="action-btn warn" title="发起纠纷" @click="openDisputeModal(b)">
                      <span class="iconify text-base" data-icon="solar:shield-warning-bold"></span>
                    </button>
                  </div>
                </td>
              </tr>
              <tr v-if="borrows.length === 0">
                <td colspan="7" class="text-center py-8 text-[#B8AE9E]">暂无数据</td>
              </tr>
            </tbody>
          </table>
        </div>
        <div v-if="borrowTotalPages > 1" class="flex items-center justify-center gap-2 mt-5">
          <button class="page-btn" :class="borrowPage > 0 ? '' : 'disabled'" :disabled="borrowPage <= 0" @click="borrowPage--; loadBorrows()">上一页</button>
          <span class="text-sm text-[#9A9082]">{{ borrowPage + 1 }} / {{ borrowTotalPages }}</span>
          <button class="page-btn" :class="borrowPage < borrowTotalPages - 1 ? '' : 'disabled'" :disabled="borrowPage >= borrowTotalPages - 1" @click="borrowPage++; loadBorrows()">下一页</button>
        </div>
      </div>
    </template>

    <Teleport to="body">
      <div v-if="resolveModal.show" class="modal-overlay" @click.self="resolveModal.show = false">
        <div class="modal-content">
          <div class="flex items-center justify-between mb-5">
            <h3 class="text-lg font-bold text-[#3D3426]">{{ resolveModal.action === 'resolve' ? '解决纠纷' : resolveModal.action === 'investigate' ? '开始调研' : '驳回纠纷' }}</h3>
            <button class="p-1 hover:bg-[#FFF9EE] rounded-lg" @click="resolveModal.show = false">
              <span class="iconify text-xl text-[#9A9082]" data-icon="solar:close-circle-bold"></span>
            </button>
          </div>
          <div class="mb-4 p-4 bg-[#FFF9EE] rounded-xl">
            <p class="text-sm text-[#3D3426]">纠纷ID：<span class="font-semibold">#{{ resolveModal.disputeId }}</span></p>
            <p class="text-sm text-[#9A9082] mt-1">物品：{{ resolveModal.itemName }}</p>
            <p class="text-sm text-[#9A9082] mt-1">申诉人：{{ resolveModal.reporterName }}</p>
            <p class="text-sm text-[#3D3426] mt-2">原因：{{ resolveModal.reason }}</p>
          </div>
          <div class="mb-5">
            <label class="block text-sm font-medium text-[#3D3426] mb-2">
              {{ resolveModal.action === 'resolve' ? '处理结果' : resolveModal.action === 'investigate' ? '调研备注' : '驳回原因' }}
            </label>
            <textarea v-model="resolveModal.resolution" class="w-full px-4 py-3 bg-[#FFF9EE] border border-[#E8D48B]/30 rounded-xl text-sm outline-none focus:ring-2 focus:ring-[#C9A227]/30 resize-none" rows="3" :placeholder="resolveModal.action === 'resolve' ? '请填写处理结果...' : resolveModal.action === 'investigate' ? '请填写调研备注...' : '请填写驳回原因...'"></textarea>
          </div>
          <div class="flex gap-3 justify-end">
            <button class="btn-cancel" @click="resolveModal.show = false">取消</button>
            <button class="btn-submit" :class="resolveModal.action === 'resolve' ? 'approve' : resolveModal.action === 'investigate' ? 'warn' : 'reject'" :disabled="resolveModal.submitting" @click="submitResolve">
              {{ resolveModal.submitting ? '处理中...' : (resolveModal.action === 'resolve' ? '确认解决' : resolveModal.action === 'investigate' ? '开始调研' : '确认驳回') }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <Teleport to="body">
      <div v-if="disputeModal.show" class="modal-overlay" @click.self="disputeModal.show = false">
        <div class="modal-content">
          <div class="flex items-center justify-between mb-5">
            <h3 class="text-lg font-bold text-[#3D3426]">发起纠纷</h3>
            <button class="p-1 hover:bg-[#FFF9EE] rounded-lg" @click="disputeModal.show = false">
              <span class="iconify text-xl text-[#9A9082]" data-icon="solar:close-circle-bold"></span>
            </button>
          </div>
          <div class="mb-4 p-4 bg-[#FFF9EE] rounded-xl">
            <p class="text-sm text-[#3D3426]">借阅ID：<span class="font-semibold">#{{ disputeModal.borrowId }}</span></p>
            <p class="text-sm text-[#9A9082] mt-1">物品：{{ disputeModal.itemName }}</p>
            <p class="text-sm text-[#9A9082] mt-1">借入者：{{ disputeModal.borrowerName }} / 借出者：{{ disputeModal.lenderName }}</p>
          </div>
          <div class="mb-5">
            <label class="block text-sm font-medium text-[#3D3426] mb-2">纠纷原因</label>
            <textarea v-model="disputeModal.reason" class="w-full px-4 py-3 bg-[#FFF9EE] border border-[#E8D48B]/30 rounded-xl text-sm outline-none focus:ring-2 focus:ring-[#C9A227]/30 resize-none" rows="3" placeholder="请填写纠纷原因..."></textarea>
          </div>
          <div class="flex gap-3 justify-end">
            <button class="btn-cancel" @click="disputeModal.show = false">取消</button>
            <button class="btn-submit warn" :disabled="disputeModal.submitting || !disputeModal.reason.trim()" @click="submitDispute">
              {{ disputeModal.submitting ? '提交中...' : '确认发起' }}
            </button>
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
import { ref, reactive, onMounted } from 'vue'
import { adminApi, type Dispute, type AdminBorrow } from '../../api/admin'

const loading = ref(true)
const error = ref('')
const activeTab = ref('disputes')

const disputes = ref<Dispute[]>([])
const disputePage = ref(0)
const disputeTotalPages = ref(0)
const disputeStatusFilter = ref('')

const borrows = ref<AdminBorrow[]>([])
const borrowPage = ref(0)
const borrowTotalPages = ref(0)
const borrowStatusFilter = ref('')

const resolveModal = reactive({
  show: false, action: 'resolve' as string, disputeId: 0, itemName: '',
  reporterName: '', reason: '', resolution: '', submitting: false,
})
const disputeModal = reactive({
  show: false, borrowId: 0, itemName: '', borrowerName: '', lenderName: '',
  reason: '', submitting: false,
})
const toast = reactive({ show: false, message: '', type: 'success' as string })

function showToast(message: string, type: string = 'success') {
  toast.message = message; toast.type = type; toast.show = true
  setTimeout(() => { toast.show = false }, 2500)
}

function formatDate(dateStr: string | null) { return dateStr ? dateStr.substring(0, 10) : '-' }

function disputeStatusLabel(s: string) {
  const m: Record<string, string> = { PENDING: '待处理', INVESTIGATING: '调查中', RESOLVED: '已解决', DISMISSED: '已驳回' }
  return m[s] || s
}
function disputeStatusClass(s: string) {
  const m: Record<string, string> = { PENDING: 'bg-[#FFF5E3] text-[#D4942A]', INVESTIGATING: 'bg-blue-50 text-blue-600', RESOLVED: 'bg-emerald-50 text-emerald-600', DISMISSED: 'bg-[#F5F0EA] text-[#9A9082]' }
  return m[s] || 'bg-[#F5F0EA] text-[#9A9082]'
}
function borrowStatusLabel(s: string) {
  const m: Record<string, string> = { PENDING: '待审批', APPROVED: '已同意', ACTIVE: '借用中', RETURN_REQUESTED: '申请归还', RETURNED: '已归还', OVERDUE: '已超期', DISPUTED: '纠纷中', REJECTED: '已拒绝', CANCELLED: '已取消' }
  return m[s] || s
}
function borrowStatusClass(s: string) {
  const m: Record<string, string> = { PENDING: 'bg-[#FFF5E3] text-[#D4942A]', APPROVED: 'bg-blue-50 text-blue-600', ACTIVE: 'bg-emerald-50 text-emerald-600', RETURN_REQUESTED: 'bg-[#FFF5E3] text-[#D4942A]', RETURNED: 'bg-[#F5F0EA] text-[#9A9082]', OVERDUE: 'bg-red-50 text-red-500', DISPUTED: 'bg-red-50 text-red-500', REJECTED: 'bg-[#F5F0EA] text-[#9A9082]', CANCELLED: 'bg-[#F5F0EA] text-[#9A9082]' }
  return m[s] || 'bg-[#F5F0EA] text-[#9A9082]'
}

async function loadDisputes() {
  try {
    const params: any = { page: disputePage.value, size: 12 }
    if (disputeStatusFilter.value) params.status = disputeStatusFilter.value
    const data = await adminApi.getDisputes(params) as any
    disputes.value = data.content || []; disputeTotalPages.value = data.totalPages || 1
  } catch (e: any) { error.value = e.message || '加载纠纷列表失败' }
}

async function loadBorrows() {
  try {
    const params: any = { page: borrowPage.value, size: 12 }
    if (borrowStatusFilter.value) params.status = borrowStatusFilter.value
    const data = await adminApi.getBorrows(params) as any
    borrows.value = data.content || []; borrowTotalPages.value = data.totalPages || 1
  } catch (e: any) { error.value = e.message || '加载借阅列表失败' }
}

function openResolveModal(d: Dispute, action: string) {
  resolveModal.action = action; resolveModal.disputeId = d.id
  resolveModal.itemName = d.itemName || '-'; resolveModal.reporterName = d.reporterNickname || '-'
  resolveModal.reason = d.reason; resolveModal.resolution = ''; resolveModal.submitting = false
  resolveModal.show = true
}

async function submitResolve() {
  resolveModal.submitting = true
  try {
    await adminApi.resolveDispute(resolveModal.disputeId, { action: resolveModal.action, resolution: resolveModal.resolution || undefined })
    resolveModal.show = false
    showToast(resolveModal.action === 'resolve' ? '纠纷已解决' : '纠纷已驳回')
    await loadDisputes()
  } catch (e: any) { showToast(e.message || '处理失败', 'error') }
  finally { resolveModal.submitting = false }
}

function openDisputeModal(b: AdminBorrow) {
  disputeModal.borrowId = b.id; disputeModal.itemName = b.itemName || '-'
  disputeModal.borrowerName = b.borrowerNickname || '-'; disputeModal.lenderName = b.lenderNickname || '-'
  disputeModal.reason = ''; disputeModal.submitting = false; disputeModal.show = true
}

async function submitDispute() {
  disputeModal.submitting = true
  try {
    await adminApi.createDispute(disputeModal.borrowId, disputeModal.reason)
    disputeModal.show = false
    showToast('纠纷已发起，借阅状态已更新')
    await Promise.all([loadBorrows(), loadDisputes()])
  } catch (e: any) { showToast(e.message || '发起纠纷失败', 'error') }
  finally { disputeModal.submitting = false }
}

onMounted(async () => {
  try { await Promise.all([loadDisputes(), loadBorrows()]) }
  finally { loading.value = false }
})
</script>

<style scoped>
.card { background: white; border-radius: 20px; border: 1px solid rgba(201, 162, 39, 0.08); }
.data-table th { background: #FFF5E3; color: #8C7D66; font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.06em; padding: 14px 16px; text-align: left; }
.data-table td { padding: 14px 16px; border-bottom: 1px solid #FFF5E3; vertical-align: middle; font-size: 14px; }
.data-table tbody tr { transition: background 0.2s; }
.data-table tbody tr:hover { background: #FFFBF5; }
.tab-btn { padding: 8px 18px; font-size: 13px; font-weight: 600; border-radius: 10px; transition: all 0.2s; border: none; cursor: pointer; color: #9A9082; background: transparent; }
.tab-btn:hover { background: white; }
.tab-btn.active { color: #3D3426; background: white; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.action-btn { padding: 8px; border-radius: 10px; transition: all 0.2s; cursor: pointer; display: inline-flex; border: none; }
.action-btn.approve { background: #ECFDF5; color: #059669; }
.action-btn.approve:hover { background: #D1FAE5; }
.action-btn.reject { background: #FEF2F2; color: #DC2626; }
.action-btn.reject:hover { background: #FEE2E2; }
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
.btn-submit.warn { background: linear-gradient(135deg, #D4942A, #C08020); box-shadow: 0 4px 12px rgba(212,148,42,0.3); }
.btn-submit:disabled { opacity: 0.6; cursor: not-allowed; }
.toast-container { position: fixed; top: 24px; right: 24px; z-index: 2000; display: flex; align-items: center; gap: 8px; padding: 14px 20px; border-radius: 16px; animation: slideIn 0.25s ease; box-shadow: 0 8px 24px rgba(0,0,0,0.12); }
.toast-container.success { background: #ECFDF5; color: #059669; }
.toast-container.error { background: #FEF2F2; color: #DC2626; }
</style>
