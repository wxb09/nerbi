<template>
  <div>
    <div class="flex items-center justify-between mb-5">
      <div>
        <h3 class="text-base font-bold text-[#3D3426]">用户管理</h3>
        <p class="text-xs text-[#B8AE9E] mt-0.5">管理平台用户，封禁/解封违规账号</p>
      </div>
      <div class="flex gap-2">
        <input v-model="keyword" class="px-3 py-2 bg-[#FFF9EE] border border-[#E8D48B]/30 rounded-xl text-sm outline-none focus:ring-2 focus:ring-[#C9A227]/30 w-48" placeholder="搜索昵称/手机号" @keyup.enter="search" />
        <select v-model="statusFilter" class="px-3 py-2 bg-[#FFF9EE] border border-[#E8D48B]/30 rounded-xl text-sm outline-none" @change="search">
          <option value="">全部状态</option>
          <option value="ACTIVE">正常</option>
          <option value="BANNED">已封禁</option>
        </select>
        <button class="btn-primary" @click="search">搜索</button>
      </div>
    </div>

    <div v-if="loading" class="text-center py-12 text-[#9A9082]">加载中...</div>
    <div v-else-if="error" class="text-center py-12 text-[#D4644A]">{{ error }}</div>
    <template v-else>
      <div class="card overflow-hidden">
        <table class="data-table w-full">
          <thead>
            <tr>
              <th>用户信息</th>
              <th>手机号</th>
              <th>信用分</th>
              <th>借入/借出</th>
              <th>角色</th>
              <th>状态</th>
              <th>注册时间</th>
              <th class="text-center">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in users" :key="user.id">
              <td>
                <div class="flex items-center gap-2.5">
                  <div v-if="user.avatar" class="w-9 h-9 rounded-lg overflow-hidden flex-shrink-0">
                    <img :src="user.avatar.startsWith('http') ? user.avatar : `http://localhost:8080/uploads/${user.avatar}`" class="w-full h-full object-cover" />
                  </div>
                  <div v-else class="w-9 h-9 rounded-lg bg-[#EEE8DD] flex items-center justify-center flex-shrink-0">
                    <span class="iconify text-[#C4B69A]" data-icon="solar:user-bold"></span>
                  </div>
                  <span class="font-semibold text-sm">{{ user.nickname }}</span>
                </div>
              </td>
              <td class="text-sm text-[#9A9082]">{{ user.phone }}</td>
              <td><span class="font-bold text-sm" :class="user.creditScore >= 8 ? 'text-[#4A9D6E]' : user.creditScore >= 5 ? 'text-[#D4942A]' : 'text-[#D4644A]'">{{ user.creditScore }}</span></td>
              <td class="text-sm">{{ user.borrowCount }} / {{ user.lendCount }}</td>
              <td><span class="text-xs font-bold px-2 py-0.5 rounded-full" :class="user.role === 'ADMIN' ? 'bg-[#C9A227]/10 text-[#C9A227]' : 'bg-[#F5F0EA] text-[#9A9082]'">{{ user.role === 'ADMIN' ? '管理员' : '用户' }}</span></td>
              <td><span class="text-xs font-bold px-2 py-0.5 rounded-full" :class="user.status === 'ACTIVE' ? 'bg-emerald-50 text-emerald-600' : 'bg-red-50 text-red-500'">{{ user.status === 'ACTIVE' ? '正常' : '已封禁' }}</span></td>
              <td class="text-sm text-[#9A9082]">{{ formatDate(user.createdAt) }}</td>
              <td>
                <div class="flex items-center justify-center gap-1.5">
                  <button v-if="user.status === 'ACTIVE' && user.role !== 'ADMIN'" class="action-btn reject" title="封禁" @click="openConfirmModal(user, 'ban')">
                    <span class="iconify text-base" data-icon="solar:user-block-bold"></span>
                  </button>
                  <button v-if="user.status === 'BANNED'" class="action-btn approve" title="解封" @click="openConfirmModal(user, 'unban')">
                    <span class="iconify text-base" data-icon="solar:user-check-bold"></span>
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="users.length === 0">
              <td colspan="8" class="text-center py-8 text-[#B8AE9E]">暂无数据</td>
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
      <div v-if="confirmModal.show" class="modal-overlay" @click.self="confirmModal.show = false">
        <div class="modal-content">
          <div class="flex items-center justify-between mb-5">
            <h3 class="text-lg font-bold text-[#3D3426]">{{ confirmModal.action === 'ban' ? '封禁用户' : '解封用户' }}</h3>
            <button class="p-1 hover:bg-[#FFF9EE] rounded-lg" @click="confirmModal.show = false">
              <span class="iconify text-xl text-[#9A9082]" data-icon="solar:close-circle-bold"></span>
            </button>
          </div>
          <div class="mb-5 p-4 rounded-xl" :class="confirmModal.action === 'ban' ? 'bg-[#FEF2F2]' : 'bg-[#ECFDF5]'">
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 rounded-xl flex items-center justify-center flex-shrink-0" :class="confirmModal.action === 'ban' ? 'bg-[#FEE2E2]' : 'bg-[#D1FAE5]'">
                <span class="iconify text-xl" :class="confirmModal.action === 'ban' ? 'text-[#DC2626]' : 'text-[#059669]'" :data-icon="confirmModal.action === 'ban' ? 'solar:user-block-bold' : 'solar:user-check-bold'"></span>
              </div>
              <div>
                <p class="font-semibold text-sm text-[#3D3426]">{{ confirmModal.nickname }}</p>
                <p class="text-xs text-[#9A9082]">{{ confirmModal.phone }}</p>
              </div>
            </div>
          </div>
          <p v-if="confirmModal.action === 'ban'" class="text-sm text-[#9A9082] mb-5">封禁后该用户将无法登录，其所有上架物品将自动下架。确定要封禁吗？</p>
          <p v-else class="text-sm text-[#9A9082] mb-5">解封后该用户可以正常登录和使用平台。确定要解封吗？</p>
          <div class="flex gap-3 justify-end">
            <button class="btn-cancel" @click="confirmModal.show = false">取消</button>
            <button class="btn-submit" :class="confirmModal.action === 'ban' ? 'reject' : 'approve'" :disabled="confirmModal.submitting" @click="submitAction">
              {{ confirmModal.submitting ? '处理中...' : (confirmModal.action === 'ban' ? '确认封禁' : '确认解封') }}
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
import { adminApi, type AdminUser } from '../../api/admin'

const loading = ref(true)
const error = ref('')
const users = ref<AdminUser[]>([])
const page = ref(0)
const totalPages = ref(0)
const keyword = ref('')
const statusFilter = ref('')

const confirmModal = reactive({
  show: false, action: 'ban' as string, userId: 0,
  nickname: '', phone: '', submitting: false,
})
const toast = reactive({ show: false, message: '', type: 'success' as string })

function showToast(message: string, type: string = 'success') {
  toast.message = message; toast.type = type; toast.show = true
  setTimeout(() => { toast.show = false }, 2500)
}

function formatDate(dateStr: string | null) { return dateStr ? dateStr.substring(0, 10) : '-' }

async function search() {
  loading.value = true; error.value = ''
  try {
    const params: any = { page: page.value, size: 20 }
    if (keyword.value) params.keyword = keyword.value
    if (statusFilter.value) params.status = statusFilter.value
    const data = await adminApi.getUsers(params) as any
    users.value = data.content || []; totalPages.value = data.totalPages || 1
  } catch (e: any) { error.value = e.message || '加载用户列表失败' }
  finally { loading.value = false }
}

function openConfirmModal(user: AdminUser, action: string) {
  confirmModal.action = action; confirmModal.userId = user.id
  confirmModal.nickname = user.nickname; confirmModal.phone = user.phone
  confirmModal.submitting = false; confirmModal.show = true
}

async function submitAction() {
  confirmModal.submitting = true
  try {
    if (confirmModal.action === 'ban') {
      await adminApi.banUser(confirmModal.userId)
      showToast('用户已封禁，其上架物品已自动下架')
    } else {
      await adminApi.unbanUser(confirmModal.userId)
      showToast('用户已解封')
    }
    confirmModal.show = false
    await search()
  } catch (e: any) { showToast(e.message || '操作失败', 'error') }
  finally { confirmModal.submitting = false }
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
.page-btn { padding: 6px 14px; border-radius: 10px; font-size: 13px; background: #FFF9EE; color: #C9A227; border: none; cursor: pointer; transition: all 0.2s; }
.page-btn:hover { background: #FFF5D9; }
.page-btn.disabled { background: #F5F0EA; color: #C4B69A; cursor: not-allowed; }
.btn-primary { background: linear-gradient(135deg, #C9A227, #B8911F); color: white; font-weight: 600; border-radius: 14px; padding: 8px 18px; box-shadow: 0 4px 16px rgba(201, 162, 39, 0.3); transition: all 0.25s; cursor: pointer; border: none; font-size: 13px; }
.btn-primary:hover { transform: translateY(-2px); box-shadow: 0 8px 24px rgba(201, 162, 39, 0.4); }
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.4); backdrop-filter: blur(4px); z-index: 1000; display: flex; align-items: center; justify-content: center; }
.modal-content { background: white; border-radius: 24px; padding: 32px; width: 90%; max-width: 440px; box-shadow: 0 25px 60px rgba(0,0,0,0.15); animation: slideIn 0.25s ease; }
@keyframes slideIn { from { transform: translateY(20px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }
.btn-cancel { padding: 10px 22px; border-radius: 14px; font-size: 14px; font-weight: 600; background: #F5F0EA; color: #9A9082; border: none; cursor: pointer; transition: all 0.2s; }
.btn-cancel:hover { background: #EEE8DD; }
.btn-submit { padding: 10px 22px; border-radius: 14px; font-size: 14px; font-weight: 600; color: white; border: none; cursor: pointer; transition: all 0.2s; }
.btn-submit.approve { background: linear-gradient(135deg, #4A9D6E, #3D8A5F); box-shadow: 0 4px 12px rgba(74,157,110,0.3); }
.btn-submit.reject { background: linear-gradient(135deg, #D4644A, #C0503A); box-shadow: 0 4px 12px rgba(212,100,74,0.3); }
.btn-submit:disabled { opacity: 0.6; cursor: not-allowed; }
.toast-container { position: fixed; top: 24px; right: 24px; z-index: 2000; display: flex; align-items: center; gap: 8px; padding: 14px 20px; border-radius: 16px; animation: slideIn 0.25s ease; box-shadow: 0 8px 24px rgba(0,0,0,0.12); }
.toast-container.success { background: #ECFDF5; color: #059669; }
.toast-container.error { background: #FEF2F2; color: #DC2626; }
</style>
