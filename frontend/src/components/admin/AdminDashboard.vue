<template>
  <div>
    <div v-if="loading" class="text-center py-12 text-[#9A9082]">加载中...</div>
    <div v-else-if="error" class="text-center py-12 text-[#D4644A]">{{ error }}</div>
    <template v-else>
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5 mb-7">
        <div v-for="stat in statCards" :key="stat.label" class="card stat-card p-6">
          <div class="flex items-start justify-between mb-4 relative z-10">
            <div class="w-11 h-11 rounded-2xl flex items-center justify-center" :class="stat.iconBg">
              <span class="iconify text-xl" :class="stat.iconColor" :data-icon="stat.icon"></span>
            </div>
          </div>
          <p class="text-[28px] font-black text-[#3D3426] leading-none tracking-tight">{{ stat.value }}</p>
          <p class="text-[13px] text-[#9A9082] mt-1.5 font-medium">{{ stat.label }}</p>
        </div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div class="card p-6">
          <h3 class="font-bold text-[#3D3426] text-base mb-4">待处理事项</h3>
          <div class="space-y-3">
            <div class="flex items-center justify-between py-3 px-4 rounded-xl bg-[#FFF9EE]">
              <div class="flex items-center gap-3">
                <span class="iconify text-lg text-[#C9A227]" data-icon="solar:box-bold"></span>
                <span class="text-sm font-medium text-[#3D3426]">待审核物品</span>
              </div>
              <span class="text-lg font-bold text-[#C9A227]">{{ stats?.pendingReviewItems ?? 0 }}</span>
            </div>
            <div class="flex items-center justify-between py-3 px-4 rounded-xl bg-[#FDF0EB]">
              <div class="flex items-center gap-3">
                <span class="iconify text-lg text-[#D4644A]" data-icon="solar:alarm-bold"></span>
                <span class="text-sm font-medium text-[#3D3426]">待处理纠纷</span>
              </div>
              <span class="text-lg font-bold text-[#D4644A]">{{ stats?.pendingDisputes ?? 0 }}</span>
            </div>
            <div class="flex items-center justify-between py-3 px-4 rounded-xl bg-[#FDF0EB]">
              <div class="flex items-center gap-3">
                <span class="iconify text-lg text-[#D4644A]" data-icon="solar:user-block-bold"></span>
                <span class="text-sm font-medium text-[#3D3426]">封禁用户</span>
              </div>
              <span class="text-lg font-bold text-[#D4644A]">{{ stats?.bannedUsers ?? 0 }}</span>
            </div>
          </div>
        </div>

        <div class="card p-6">
          <h3 class="font-bold text-[#3D3426] text-base mb-4">本月数据</h3>
          <div class="space-y-3">
            <div class="flex items-center justify-between py-3 px-4 rounded-xl bg-[#EDF7EF]">
              <div class="flex items-center gap-3">
                <span class="iconify text-lg text-[#4A9D6E]" data-icon="solar:user-plus-bold"></span>
                <span class="text-sm font-medium text-[#3D3426]">新增用户</span>
              </div>
              <span class="text-lg font-bold text-[#4A9D6E]">{{ stats?.thisMonthUsers ?? 0 }}</span>
            </div>
            <div class="flex items-center justify-between py-3 px-4 rounded-xl bg-[#FFF5E3]">
              <div class="flex items-center gap-3">
                <span class="iconify text-lg text-[#D4942A]" data-icon="solar:hand-shake-bold"></span>
                <span class="text-sm font-medium text-[#3D3426]">新增借阅</span>
              </div>
              <span class="text-lg font-bold text-[#D4942A]">{{ stats?.thisMonthBorrows ?? 0 }}</span>
            </div>
            <div class="flex items-center justify-between py-3 px-4 rounded-xl bg-[#F2FAF4]">
              <div class="flex items-center gap-3">
                <span class="iconify text-lg text-[#4A9D6E]" data-icon="solar:hand-shake-bold"></span>
                <span class="text-sm font-medium text-[#3D3426]">进行中借阅</span>
              </div>
              <span class="text-lg font-bold text-[#4A9D6E]">{{ stats?.activeBorrows ?? 0 }}</span>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { adminApi, type AdminStats } from '../../api/admin'

const loading = ref(true)
const error = ref('')
const stats = ref<AdminStats | null>(null)

const statCards = ref<any[]>([])

function buildCards(data: AdminStats) {
  statCards.value = [
    { icon: 'solar:users-group-rounded-bold', iconBg: 'bg-gradient-to-br from-[#FFF5E3] to-[#FDF6E3]', iconColor: 'text-[#C9A227]', value: data.totalUsers, label: '总用户数' },
    { icon: 'solar:box-bold', iconBg: 'bg-gradient-to-br from-[#FEF3DB] to-[#FFF5E3]', iconColor: 'text-[#D4942A]', value: data.totalItems, label: '总物品数' },
    { icon: 'solar:hand-shake-bold', iconBg: 'bg-gradient-to-br from-[#EDF7EF] to-[#F2FAF4]', iconColor: 'text-[#4A9D6E]', value: data.totalBorrows, label: '总借阅次数' },
    { icon: 'solar:alarm-bold', iconBg: 'bg-gradient-to-br from-[#FDF0EB] to-[#FEF5F1]', iconColor: 'text-[#D4644A]', value: data.pendingDisputes, label: '待处理纠纷' },
  ]
}

onMounted(async () => {
  try {
    const data = await adminApi.getStats() as any
    stats.value = data
    buildCards(data)
  } catch (e: any) {
    error.value = e.message || '加载统计数据失败'
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.card {
  background: white;
  border-radius: 20px;
  border: 1px solid rgba(201, 162, 39, 0.08);
  transition: all 0.35s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}
.card:hover {
  transform: translateY(-3px);
  box-shadow: 0 16px 40px rgba(201, 162, 39, 0.12), 0 4px 12px rgba(0,0,0,0.04);
  border-color: rgba(201, 162, 39, 0.18);
}
.stat-card {
  position: relative;
  overflow: hidden;
}
.stat-card::after {
  content: '';
  position: absolute;
  top: -30%;
  right: -15%;
  width: 120px;
  height: 120px;
  background: radial-gradient(circle, rgba(201, 162, 39, 0.15) 0%, transparent 70%);
  border-radius: 50%;
  pointer-events: none;
}
</style>
