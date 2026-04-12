<template>
  <div>
    <div class="flex items-center justify-between mb-5">
      <div>
        <h3 class="text-base font-bold text-[#3D3426]">认证审核</h3>
        <p class="text-xs text-[#B8AE9E] mt-0.5">审核用户的小区认证申请</p>
      </div>
    </div>

    <div v-if="loading" class="text-center py-12 text-[#9A9082]">加载中...</div>
    <div v-else-if="error" class="text-center py-12 text-[#D4644A]">{{ error }}</div>
    <div v-else-if="verifies.length === 0" class="text-center py-12">
      <span class="iconify text-5xl text-[#C4B69A]" data-icon="solar:inbox-bold"></span>
      <p class="text-[#9A9082] mt-3">暂无待审核的认证申请</p>
    </div>
    <template v-else>
      <div class="card overflow-hidden">
        <table class="data-table w-full">
          <thead>
            <tr>
              <th>用户信息</th>
              <th>申请小区</th>
              <th>楼栋/单元</th>
              <th>申请时间</th>
              <th class="text-center">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="v in verifies" :key="v.userId">
              <td>
                <div class="flex items-center gap-2.5">
                  <div v-if="v.avatar" class="w-9 h-9 rounded-lg overflow-hidden flex-shrink-0">
                    <img :src="getAvatarUrl(v.avatar)" class="w-full h-full object-cover" />
                  </div>
                  <div v-else class="w-9 h-9 rounded-lg bg-[#EEE8DD] flex items-center justify-center flex-shrink-0">
                    <span class="iconify text-[#C4B69A]" data-icon="solar:user-bold"></span>
                  </div>
                  <div>
                    <span class="font-semibold text-sm block">{{ v.nickname }}</span>
                    <span class="text-xs text-[#9A9082]">{{ v.phone }}</span>
                  </div>
                </div>
              </td>
              <td class="text-sm">{{ v.communityName || '-' }}</td>
              <td class="text-sm">{{ v.building || '-' }} {{ v.unit || '' }}</td>
              <td class="text-sm text-[#9A9082]">-</td>
              <td>
                <div class="flex items-center gap-1.5">
                  <button class="action-btn approve" title="通过" @click="handleApprove(v.userId, true)">
                    <span class="iconify text-base" data-icon="solar:check-circle-bold"></span>
                  </button>
                  <button class="action-btn reject" title="拒绝" @click="handleApprove(v.userId, false)">
                    <span class="iconify text-base" data-icon="solar:close-circle-bold"></span>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>

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
import { adminApi } from '../../api/admin'

const loading = ref(true)
const error = ref('')
const verifies = ref<any[]>([])

const toast = reactive({ show: false, message: '', type: 'success' as string })

function showToast(message: string, type: string = 'success') {
  toast.message = message; toast.type = type; toast.show = true
  setTimeout(() => { toast.show = false }, 2500)
}

function getAvatarUrl(avatar: string | null): string {
  if (!avatar) return ''
  if (avatar.startsWith('http')) return avatar
  if (avatar.startsWith('/uploads')) return `http://localhost:8080${avatar}`
  return `http://localhost:8080/uploads/${avatar}`
}

async function loadVerifies() {
  loading.value = true
  error.value = ''
  try {
    verifies.value = await adminApi.getPendingAddressVerifies()
  } catch (e: any) {
    error.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

async function handleApprove(userId: number, approved: boolean) {
  try {
    await adminApi.approveAddressVerify(userId, approved)
    showToast(approved ? '认证已通过' : '认证已拒绝')
    await loadVerifies()
  } catch (e: any) {
    showToast(e.message || '操作失败', 'error')
  }
}

onMounted(() => loadVerifies())
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
.toast-container { position: fixed; top: 24px; right: 24px; z-index: 2000; display: flex; align-items: center; gap: 8px; padding: 14px 20px; border-radius: 16px; animation: slideIn 0.25s ease; box-shadow: 0 8px 24px rgba(0,0,0,0.12); }
.toast-container.success { background: #ECFDF5; color: #059669; }
.toast-container.error { background: #FEF2F2; color: #DC2626; }
@keyframes slideIn { from { transform: translateY(-20px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }
</style>
