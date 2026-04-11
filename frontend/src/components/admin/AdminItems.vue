<template>
  <div>
    <div class="flex items-center justify-between mb-5">
      <div>
        <h3 class="text-base font-bold text-[#3D3426]">物品审核</h3>
        <p class="text-xs text-[#B8AE9E] mt-0.5">审核用户发布的物品，通过后即可在平台展示</p>
      </div>
      <div class="flex gap-2">
        <select v-model="statusFilter" class="px-3 py-2 bg-[#FFF9EE] border border-[#E8D48B]/30 rounded-xl text-sm outline-none" @change="search">
          <option value="">全部状态</option>
          <option value="PENDING_REVIEW">待审核</option>
          <option value="AVAILABLE">已上架</option>
          <option value="DRAFT">草稿</option>
          <option value="OFFLINE">已下架</option>
          <option value="BORROWED">借出中</option>
        </select>
      </div>
    </div>

    <div v-if="loading" class="text-center py-12 text-[#9A9082]">加载中...</div>
    <div v-else-if="error" class="text-center py-12 text-[#D4644A]">{{ error }}</div>
    <template v-else>
      <div class="card overflow-hidden">
        <table class="data-table w-full">
          <thead>
            <tr>
              <th>物品信息</th>
              <th>发布者</th>
              <th>分类</th>
              <th>租金</th>
              <th>状态</th>
              <th>提交时间</th>
              <th class="text-center">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in items" :key="item.id">
              <td>
                <div class="flex items-center gap-3">
                  <div v-if="item.mainImage" class="w-[48px] h-[48px] rounded-xl overflow-hidden ring-1 ring-[#E8D48B]/20 flex-shrink-0">
                    <img :src="item.mainImage.startsWith('http') ? item.mainImage : `http://localhost:8080/uploads/${item.mainImage}`" class="w-full h-full object-cover" />
                  </div>
                  <div v-else class="w-[48px] h-[48px] rounded-xl bg-[#F5F0EA] flex items-center justify-center ring-1 ring-[#E8D48B]/20 flex-shrink-0">
                    <span class="iconify text-xl text-[#C4B69A]" data-icon="solar:camera-bold"></span>
                  </div>
                  <div>
                    <p class="font-semibold text-[#3D3426] text-sm">{{ item.name }}</p>
                    <p v-if="item.auditRemark" class="text-[11px] text-[#D4644A] mt-0.5">{{ item.auditRemark }}</p>
                  </div>
                </div>
              </td>
              <td class="text-sm">{{ item.ownerNickname || '-' }}</td>
              <td class="text-sm text-[#9A9082]">{{ item.categoryName || '-' }}</td>
              <td class="text-sm">{{ item.pricePerDay > 0 ? `￥${item.pricePerDay}/天` : '免费' }}</td>
              <td><span class="text-xs font-bold px-2 py-0.5 rounded-full" :class="statusClass(item.status)">{{ statusLabel(item.status) }}</span></td>
              <td class="text-sm text-[#9A9082]">{{ formatDate(item.createdAt) }}</td>
              <td>
                <div class="flex items-center gap-1.5">
                  <button v-if="item.status === 'PENDING_REVIEW'" class="action-btn approve" title="通过" @click="openAuditModal(item, 'approve')">
                    <span class="iconify text-base" data-icon="solar:check-circle-bold"></span>
                  </button>
                  <button v-if="item.status === 'PENDING_REVIEW'" class="action-btn reject" title="驳回" @click="openAuditModal(item, 'reject')">
                    <span class="iconify text-base" data-icon="solar:close-circle-bold"></span>
                  </button>
                  <a :href="`/item/${item.id}`" target="_blank" class="action-btn view" title="查看">
                    <span class="iconify text-base" data-icon="solar:eye-bold"></span>
                  </a>
                </div>
              </td>
            </tr>
            <tr v-if="items.length === 0">
              <td colspan="7" class="text-center py-8 text-[#B8AE9E]">暂无数据</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="totalPages > 1" class="flex items-center justify-center gap-2 mt-5">
        <button class="page-btn" :class="page > 0 ? '' : 'disabled'" :disabled="page <= 0" @click="page--; search()">上一页</button>
        <span class="text-sm text-[#9A9082]">{{ page + 1 }} / {{ totalPages }}</span>
        <button class="page-btn" :class="page < totalPages - 1 ? '' : 'disabled'" :disabled="page >= totalPages - 1" @click="page++; search()">下一页</button>
      </div>
    </template>

    <Teleport to="body">
      <div v-if="auditModal.show" class="modal-overlay" @click.self="auditModal.show = false">
        <div class="modal-content">
          <div class="flex items-center justify-between mb-5">
            <h3 class="text-lg font-bold text-[#3D3426]">{{ auditModal.action === 'approve' ? '通过审核' : '驳回物品' }}</h3>
            <button class="p-1 hover:bg-[#FFF9EE] rounded-lg" @click="auditModal.show = false">
              <span class="iconify text-xl text-[#9A9082]" data-icon="solar:close-circle-bold"></span>
            </button>
          </div>
          <div class="mb-4">
            <p class="text-sm text-[#3D3426] mb-1">物品：<span class="font-semibold">{{ auditModal.itemName }}</span></p>
            <p class="text-sm text-[#9A9082]">发布者：{{ auditModal.ownerName }}</p>
          </div>
          <div class="mb-5">
            <label class="block text-sm font-medium text-[#3D3426] mb-2">
              {{ auditModal.action === 'approve' ? '审核备注（可选）' : '驳回原因' }}
            </label>
            <textarea v-model="auditModal.remark" class="w-full px-4 py-3 bg-[#FFF9EE] border border-[#E8D48B]/30 rounded-xl text-sm outline-none focus:ring-2 focus:ring-[#C9A227]/30 resize-none" rows="3" :placeholder="auditModal.action === 'approve' ? '可填写审核备注...' : '请填写驳回原因...'"></textarea>
          </div>
          <div class="flex gap-3 justify-end">
            <button class="btn-cancel" @click="auditModal.show = false">取消</button>
            <button class="btn-submit" :class="auditModal.action === 'approve' ? 'approve' : 'reject'" :disabled="auditModal.submitting" @click="submitAudit">
              {{ auditModal.submitting ? '处理中...' : (auditModal.action === 'approve' ? '确认通过' : '确认驳回') }}
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
import { adminApi, type AdminItem } from '../../api/admin'

const loading = ref(true)
const error = ref('')
const items = ref<AdminItem[]>([])
const page = ref(0)
const totalPages = ref(0)
const statusFilter = ref('PENDING_REVIEW')

const auditModal = reactive({
  show: false,
  action: 'approve' as string,
  itemId: 0,
  itemName: '',
  ownerName: '',
  remark: '',
  submitting: false,
})

const toast = reactive({ show: false, message: '', type: 'success' as string })

function showToast(message: string, type: string = 'success') {
  toast.message = message
  toast.type = type
  toast.show = true
  setTimeout(() => { toast.show = false }, 2500)
}

function formatDate(dateStr: string | null) {
  if (!dateStr) return '-'
  return dateStr.substring(0, 10)
}

function statusLabel(status: string) {
  const map: Record<string, string> = {
    PENDING_REVIEW: '待审核', AVAILABLE: '已上架', DRAFT: '草稿',
    OFFLINE: '已下架', BORROWED: '借出中', DELETED: '已删除',
  }
  return map[status] || status
}

function statusClass(status: string) {
  const map: Record<string, string> = {
    PENDING_REVIEW: 'bg-[#FFF5E3] text-[#D4942A]', AVAILABLE: 'bg-emerald-50 text-emerald-600',
    DRAFT: 'bg-[#F5F0EA] text-[#9A9082]', OFFLINE: 'bg-[#F5F0EA] text-[#9A9082]',
    BORROWED: 'bg-blue-50 text-blue-600', DELETED: 'bg-red-50 text-red-500',
  }
  return map[status] || 'bg-[#F5F0EA] text-[#9A9082]'
}

async function search() {
  loading.value = true
  error.value = ''
  try {
    const params: any = { page: page.value, size: 20 }
    if (statusFilter.value) params.status = statusFilter.value
    const data = await adminApi.getAllItems(params) as any
    items.value = data.content || []
    totalPages.value = data.totalPages || 1
  } catch (e: any) {
    error.value = e.message || '加载物品列表失败'
  } finally {
    loading.value = false
  }
}

function openAuditModal(item: AdminItem, action: string) {
  auditModal.action = action
  auditModal.itemId = item.id
  auditModal.itemName = item.name
  auditModal.ownerName = item.ownerNickname || '-'
  auditModal.remark = ''
  auditModal.submitting = false
  auditModal.show = true
}

async function submitAudit() {
  auditModal.submitting = true
  try {
    await adminApi.auditItem(auditModal.itemId, { action: auditModal.action, remark: auditModal.remark || undefined })
    auditModal.show = false
    showToast(auditModal.action === 'approve' ? '审核通过，物品已上架' : '已驳回该物品')
    await search()
  } catch (e: any) {
    showToast(e.message || '审核操作失败', 'error')
  } finally {
    auditModal.submitting = false
  }
}

onMounted(() => search())
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
.action-btn.view { background: #FFF9EE; color: #C9A227; }
.action-btn.view:hover { background: #FFF5D9; }
.page-btn { padding: 6px 14px; border-radius: 10px; font-size: 13px; background: #FFF9EE; color: #C9A227; border: none; cursor: pointer; transition: all 0.2s; }
.page-btn:hover { background: #FFF5D9; }
.page-btn.disabled { background: #F5F0EA; color: #C4B69A; cursor: not-allowed; }
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.4); backdrop-filter: blur(4px); z-index: 1000; display: flex; align-items: center; justify-content: center; }
.modal-content { background: white; border-radius: 24px; padding: 32px; width: 90%; max-width: 460px; box-shadow: 0 25px 60px rgba(0,0,0,0.15); animation: slideIn 0.25s ease; }
@keyframes slideIn { from { transform: translateY(20px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }
.btn-cancel { padding: 10px 22px; border-radius: 14px; font-size: 14px; font-weight: 600; background: #F5F0EA; color: #9A9082; border: none; cursor: pointer; transition: all 0.2s; }
.btn-cancel:hover { background: #EEE8DD; }
.btn-submit { padding: 10px 22px; border-radius: 14px; font-size: 14px; font-weight: 600; color: white; border: none; cursor: pointer; transition: all 0.2s; }
.btn-submit.approve { background: linear-gradient(135deg, #4A9D6E, #3D8A5F); box-shadow: 0 4px 12px rgba(74,157,110,0.3); }
.btn-submit.approve:hover { box-shadow: 0 6px 20px rgba(74,157,110,0.4); }
.btn-submit.reject { background: linear-gradient(135deg, #D4644A, #C0503A); box-shadow: 0 4px 12px rgba(212,100,74,0.3); }
.btn-submit.reject:hover { box-shadow: 0 6px 20px rgba(212,100,74,0.4); }
.btn-submit:disabled { opacity: 0.6; cursor: not-allowed; }
.toast-container { position: fixed; top: 24px; right: 24px; z-index: 2000; display: flex; align-items: center; gap: 8px; padding: 14px 20px; border-radius: 16px; animation: slideIn 0.25s ease; box-shadow: 0 8px 24px rgba(0,0,0,0.12); }
.toast-container.success { background: #ECFDF5; color: #059669; }
.toast-container.error { background: #FEF2F2; color: #DC2626; }
</style>
