<template>
  <div class="flex h-screen overflow-hidden">
    <aside
      class="bg-[#1A1614] text-white flex flex-col flex-shrink-0 transition-all duration-300"
      :class="sidebarCollapsed ? 'w-[76px]' : 'w-[260px]'"
    >
      <div class="h-[72px] flex items-center px-6 border-b border-white/[0.06]">
        <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-[#C9A227] to-[#E8D48B] flex items-center justify-center shadow-lg shadow-[#C9A227]/20 flex-shrink-0">
          <span class="iconify text-xl text-[#1A1614]" data-icon="solar:share-circle-bold"></span>
        </div>
        <div v-if="!sidebarCollapsed" class="ml-3">
          <h1 class="font-bold text-base tracking-wide">邻里共享</h1>
          <p class="text-[10px] text-white/35 uppercase tracking-[0.2em] mt-0.5">Admin Panel</p>
        </div>
      </div>

      <nav class="flex-1 overflow-y-auto py-5 px-4 space-y-0.5">
        <a
          v-for="item in navItems"
          :key="item.label"
          class="nav-item"
          :class="item.active ? 'active' : ''"
        >
          <span class="iconify nav-icon" :data-icon="item.icon"></span>
          <span v-if="!sidebarCollapsed" class="nav-text">{{ item.label }}</span>
          <span
            v-if="item.badge && !sidebarCollapsed"
            class="badge ml-auto"
            :class="item.badgeClass"
          >{{ item.badge }}</span>
        </a>

        <p v-if="!sidebarCollapsed" class="px-4 pt-5 pb-2 text-[10px] font-bold text-white/25 uppercase tracking-[0.18em]">运营</p>
        <a
          v-for="item in opsItems"
          :key="item.label"
          class="nav-item"
        >
          <span class="iconify nav-icon" :data-icon="item.icon"></span>
          <span v-if="!sidebarCollapsed" class="nav-text">{{ item.label }}</span>
        </a>

        <p v-if="!sidebarCollapsed" class="px-4 pt-5 pb-2 text-[10px] font-bold text-white/25 uppercase tracking-[0.18em]">系统</p>
        <a
          v-for="item in sysItems"
          :key="item.label"
          class="nav-item"
        >
          <span class="iconify nav-icon" :data-icon="item.icon"></span>
          <span v-if="!sidebarCollapsed" class="nav-text">{{ item.label }}</span>
        </a>
      </nav>

      <div v-if="!sidebarCollapsed" class="p-4 border-t border-white/[0.06]">
        <div class="flex items-center px-3 py-3 rounded-xl bg-white/[0.05] hover:bg-white/[0.08] transition-colors cursor-pointer">
          <img
            alt="Admin"
            class="w-10 h-10 rounded-xl object-cover ring-2 ring-[#C9A227]/60 flex-shrink-0"
            src="https://modao.cc/agent-py/media/generated_images/2026-03-19/a453981eedaa427c81b6f1552d5d5dbc.jpg"
          />
          <div class="ml-3">
            <p class="font-semibold text-sm">管理员</p>
            <p class="text-[11px] text-white/35">超级管理员</p>
          </div>
          <button class="ml-auto text-white/30 hover:text-[#C9A227] transition-colors">
            <span class="iconify text-lg" data-icon="solar:logout-2-bold"></span>
          </button>
        </div>
      </div>
    </aside>

    <main class="flex-1 flex flex-col overflow-hidden">
      <header class="h-[72px] bg-white/80 backdrop-blur-md border-b border-[#C9A227]/[0.06] flex items-center justify-between px-8 flex-shrink-0">
        <div class="flex items-center">
          <button class="mr-4 p-2 hover:bg-[#FFF9EE] rounded-lg lg:hidden" @click="sidebarCollapsed = !sidebarCollapsed">
            <span class="iconify text-xl text-[#8C7D66]" data-icon="solar:hamburger-menu-bold"></span>
          </button>
          <div>
            <h2 class="text-xl font-bold text-[#3D3426]">数据仪表盘</h2>
            <p class="text-xs text-[#9A9082] mt-0.5">栖霞苑社区 · 2026年4月10日</p>
          </div>
        </div>

        <div class="flex items-center gap-4">
          <div class="relative hidden md:block">
            <span class="iconify absolute left-4 top-1/2 -translate-y-1/2 text-[#B8AE9E]" data-icon="solar:magnifer-linear"></span>
            <input
              class="pl-11 pr-4 py-2.5 bg-[#FFF9EE] border border-[#E8D48B]/30 rounded-xl w-56 outline-none focus:ring-2 focus:ring-[#C9A227]/30 focus:border-[#C9A227]/50 text-sm placeholder:text-[#C4B69A]"
              placeholder="搜索物品、用户..."
              type="text"
            />
          </div>

          <button class="relative p-2.5 bg-[#FFF9EE] rounded-xl hover:bg-[#FFF5D9] transition-colors">
            <span class="iconify text-lg text-[#8C7D66]" data-icon="solar:bell-bold"></span>
            <span class="absolute top-2 right-2 w-2 h-2 bg-[#D4644A] rounded-full pulse-dot"></span>
          </button>

          <button class="btn-primary">
            <svg xmlns="http://www.w3.org/2000/svg" width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="m3 11 18-5v12L3 14v-3z"/><path d="M11.6 16.8a3 3 0 1 1-5.8-1.6"/></svg>
            发布公告
          </button>
        </div>
      </header>

      <div class="flex-1 overflow-y-auto p-7">
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5 mb-7">
          <div
            v-for="stat in statCards"
            :key="stat.label"
            class="card stat-card p-6"
          >
            <div class="flex items-start justify-between mb-4 relative z-10">
              <div class="w-11 h-11 rounded-2xl flex items-center justify-center" :class="stat.iconBg">
                <span class="iconify text-xl" :class="stat.iconColor" :data-icon="stat.icon"></span>
              </div>
              <span class="text-xs font-bold px-2.5 py-1 rounded-full" :class="stat.trendBg">{{ stat.trend }}</span>
            </div>
            <p class="text-[28px] font-black text-[#3D3426] leading-none tracking-tight" v-html="stat.value"></p>
            <p class="text-[13px] text-[#9A9082] mt-1.5 font-medium">{{ stat.label }}</p>
            <div v-if="stat.progress !== undefined" class="mt-4 progress-bar">
              <div class="progress-fill" :class="stat.progressGradient" :style="{ width: stat.progress + '%' }"></div>
            </div>
            <p v-if="stat.extra" class="mt-4 flex items-center gap-1.5 text-xs text-[#7A8B6E]">
              <span class="iconify text-sm" data-icon="solar:check-circle-bold"></span>{{ stat.extra }}
            </p>
            <button v-if="stat.action" class="mt-4 text-xs font-bold text-[#C9A227] hover:text-[#B8911F] transition-colors">{{ stat.action }}</button>
          </div>
        </div>

        <div class="grid grid-cols-1 xl:grid-cols-3 gap-6">
          <div class="xl:col-span-2 space-y-6">
            <div class="card overflow-hidden">
              <div class="px-7 py-5 border-b border-[#C9A227]/[0.06] flex items-center justify-between">
                <div>
                  <h3 class="text-base font-bold text-[#3D3426]">待审核物品</h3>
                  <p class="text-xs text-[#B8AE9E] mt-0.5">共 12 件物品等待上架审核</p>
                </div>
                <div class="flex gap-1.5 bg-[#FBF7F0] p-1 rounded-xl">
                  <button
                    v-for="tab in reviewTabs"
                    :key="tab"
                    class="px-3.5 py-1.5 text-xs font-semibold rounded-lg transition-all"
                    :class="activeReviewTab === tab
                      ? 'text-[#3D3426] bg-white shadow-sm'
                      : 'text-[#9A9082] hover:bg-white hover:text-[#3D3426]'"
                    @click="activeReviewTab = tab"
                  >{{ tab }}</button>
                </div>
              </div>

              <div class="overflow-x-auto">
                <table class="data-table w-full">
                  <thead>
                    <tr>
                      <th>物品信息</th>
                      <th>发布者</th>
                      <th>提交时间</th>
                      <th class="text-center">操作</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="row in reviewItems" :key="row.name">
                      <td>
                        <div class="flex items-center gap-3.5">
                          <div v-if="row.image" class="w-[52px] h-[52px] rounded-xl overflow-hidden ring-1 ring-[#E8D48B]/20 flex-shrink-0">
                            <img :src="row.image" class="w-full h-full object-cover" />
                          </div>
                          <div v-else class="w-[52px] h-[52px] rounded-xl bg-[#F5F0EA] flex items-center justify-center ring-1 ring-[#E8D48B]/20 flex-shrink-0">
                            <span class="iconify text-xl text-[#C4B69A]" data-icon="solar:camera-bold"></span>
                          </div>
                          <div>
                            <p class="font-semibold text-[#3D3426] text-sm">{{ row.name }}</p>
                            <p class="text-[11px] text-[#B8AE9E] mt-0.5">{{ row.category }} · {{ row.price }}</p>
                          </div>
                        </div>
                      </td>
                      <td>
                        <div class="flex items-center gap-2.5">
                          <div v-if="row.avatar" class="w-8 h-8 rounded-lg overflow-hidden flex-shrink-0">
                            <img :src="row.avatar" class="w-full h-full object-cover" />
                          </div>
                          <div v-else class="w-8 h-8 rounded-lg bg-[#EEE8DD] flex-shrink-0"></div>
                          <div>
                            <p class="text-sm font-semibold">{{ row.publisher }}</p>
                            <p class="text-[10px] text-[#B8AE9E]">信用 {{ row.credit }}</p>
                          </div>
                        </div>
                      </td>
                      <td class="text-sm text-[#9A9082]">{{ row.time }}</td>
                      <td>
                        <div class="flex items-center justify-center gap-1.5">
                          <button class="p-2 rounded-lg bg-emerald-50 text-emerald-600 hover:bg-emerald-100 transition-colors" title="通过">
                            <span class="iconify text-base" data-icon="solar:check-circle-bold"></span>
                          </button>
                          <button class="p-2 rounded-lg bg-red-50 text-red-500 hover:bg-red-100 transition-colors" title="驳回">
                            <span class="iconify text-base" data-icon="solar:close-circle-bold"></span>
                          </button>
                          <button class="p-2 rounded-lg bg-[#FFF9EE] text-[#C9A227] hover:bg-[#FFF5D9] transition-colors" title="查看">
                            <span class="iconify text-base" data-icon="solar:eye-bold"></span>
                          </button>
                        </div>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
              <div class="px-7 py-3.5 bg-[#FCFAF6] border-t border-[#C9A227]/[0.06]">
                <button class="text-xs font-bold text-[#C9A227] hover:text-[#B8911F] transition-colors">查看全部待审核物品 →</button>
              </div>
            </div>

            <div class="grid grid-cols-2 sm:grid-cols-4 gap-4">
              <button
                v-for="action in quickActions"
                :key="action.label"
                class="card p-5 group text-left"
              >
                <div class="w-10 h-10 rounded-xl flex items-center justify-center mb-3 transition-all duration-300" :class="action.iconBg">
                  <span class="iconify text-xl transition-colors" :class="action.iconColor" :data-icon="action.icon"></span>
                </div>
                <p class="font-bold text-sm text-[#3D3426]">{{ action.label }}</p>
                <p class="text-[11px] text-[#B8AE9E] mt-0.5">{{ action.desc }}</p>
              </button>
            </div>
          </div>

          <div class="space-y-6">
            <div class="rounded-2xl p-6 relative overflow-hidden" style="background: linear-gradient(160deg, #FFFBF5 0%, #FFF5E3 35%, #FDF6E3 70%, #F5ECD0 100%);">
              <div class="absolute -top-10 -right-10 w-36 h-36 rounded-full opacity-[0.12]" style="background: radial-gradient(circle, #C9A227, transparent);"></div>
              <div class="absolute -bottom-8 -left-8 w-28 h-28 rounded-full opacity-[0.08]" style="background: radial-gradient(circle, #E8D48B, transparent);"></div>

              <div class="relative z-10">
                <div class="flex items-center justify-between mb-5">
                  <h3 class="font-bold text-[#3D3426] text-base">实时动态</h3>
                  <div class="flex items-center gap-1.5">
                    <span class="w-2 h-2 rounded-full bg-[#C9A227] pulse-dot"></span>
                    <span class="text-[10px] text-[#B8AE9E] font-medium">LIVE</span>
                  </div>
                </div>

                <div class="space-y-0 max-h-[320px] overflow-y-auto pr-1">
                  <div
                    v-for="(event, i) in liveEvents"
                    :key="i"
                    class="feed-item flex items-start gap-3"
                  >
                    <div class="w-2 h-2 rounded-full mt-2 flex-shrink-0" :class="event.dotColor"></div>
                    <div>
                      <p class="text-[13px]" v-html="event.text"></p>
                      <p class="text-[11px] text-[#B8AE9E] mt-0.5">{{ event.detail }}</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="card p-6">
              <h3 class="font-bold text-[#3D3426] text-base mb-5">本月数据概览</h3>
              <div class="space-y-5">
                <div v-for="bar in monthlyBars" :key="bar.label">
                  <div class="flex justify-between text-[13px] mb-2">
                    <span class="text-[#9A9082] font-medium">{{ bar.label }}</span>
                    <span class="font-bold" :class="bar.valueColor || 'text-[#3D3426]'">{{ bar.value }}</span>
                  </div>
                  <div class="progress-bar">
                    <div class="progress-fill" :class="bar.barGradient" :style="{ width: bar.percent + '%' }"></div>
                  </div>
                </div>
              </div>
            </div>

            <div class="rounded-2xl p-6" style="background: linear-gradient(160deg, #FDF6E3 0%, #FFF9EE 100%); border: 1px solid rgba(201,162,39,0.1);">
              <h3 class="font-bold text-[#C9A227] text-base mb-4 flex items-center gap-2">
                <span class="iconify" data-icon="solar:bolt-bold"></span>
                快捷工具
              </h3>
              <div class="grid grid-cols-2 gap-2.5">
                <button
                  v-for="tool in quickTools"
                  :key="tool"
                  class="py-2.5 px-3 bg-white rounded-xl text-[13px] font-semibold text-[#5C5244] hover:text-[#C9A227] hover:shadow-md hover:-translate-y-0.5 transition-all"
                >{{ tool }}</button>
              </div>
            </div>
          </div>
        </div>

        <footer class="mt-8 pt-6 border-t border-[#E8D48B]/15 text-center">
          <p class="text-xs text-[#B8AE9E]">© 2026 邻里共享平台 · 管理后台系统 v2.0</p>
          <p class="text-[11px] text-[#D4CBB8] mt-1">上次数据同步：2026-04-10 09:56:23</p>
        </footer>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const sidebarCollapsed = ref(false)
const activeReviewTab = ref('待审核')

const navItems = [
  { label: '数据仪表盘', icon: 'solar:chart-square-bold', active: true },
  { label: '物品管理', icon: 'solar:box-bold', badge: '12', badgeClass: 'badge-warm' },
  { label: '借阅审核', icon: 'solar:clipboard-check-bold', badge: '5', badgeClass: 'badge-gold' },
  { label: '用户管理', icon: 'solar:users-group-rounded-bold' },
]

const opsItems = [
  { label: '帖子管理', icon: 'solar:chat-square-bold' },
  { label: '公告发布', icon: 'solar:add-circle-bold' },
  { label: '绿色榜单', icon: 'solar:medal-ribbons-star-bold' },
]

const sysItems = [
  { label: '系统设置', icon: 'solar:settings-bold' },
  { label: '权限管理', icon: 'solar:shield-check-bold' },
]

const statCards = [
  {
    icon: 'solar:box-bold',
    iconBg: 'bg-gradient-to-br from-[#FFF5E3] to-[#FDF6E3]',
    iconColor: 'text-[#C9A227]',
    trend: '+12%', trendBg: 'text-emerald-600 bg-emerald-50',
    value: '1,284', label: '平台总物品',
    progress: 75, progressGradient: 'bg-gradient-to-r from-[#C9A227] to-[#E8D48B]',
  },
  {
    icon: 'solar:hand-shake-bold',
    iconBg: 'bg-gradient-to-br from-[#FEF3DB] to-[#FFF5E3]',
    iconColor: 'text-[#D4942A]',
    trend: '+8%', trendBg: 'text-emerald-600 bg-emerald-50',
    value: '342', label: '本月借阅次数',
    progress: 60, progressGradient: 'bg-gradient-to-r from-[#D4942A] to-[#E8A84D]',
  },
  {
    icon: 'solar:leaf-bold',
    iconBg: 'bg-gradient-to-br from-[#EDF7EF] to-[#F2FAF4]',
    iconColor: 'text-[#4A9D6E]',
    trend: '累计', trendBg: 'text-[#9A9082] bg-[#F5F0EA]',
    value: '2.4<span class="text-base text-[#9A9082] font-semibold ml-0.5">吨</span>', label: '减少碳排放',
    extra: '相当于种植 120 棵树',
  },
  {
    icon: 'solar:alarm-bold',
    iconBg: 'bg-gradient-to-br from-[#FDF0EB] to-[#FEF5F1]',
    iconColor: 'text-[#D4644A]',
    trend: '需关注', trendBg: 'text-[#D4644A] bg-[#FDF0EB]',
    value: '5', label: '待处理纠纷',
    action: '立即处理 →',
  },
]

const reviewTabs = ['全部', '待审核', '已驳回']

const reviewItems = [
  {
    image: 'https://modao.cc/agent-py/media/generated_images/2026-03-19/2708e2c946b24f0087330e2e974cbd6c.jpg',
    name: '博世专业级电钻套装', category: '五金工具', price: '￥0/天',
    avatar: 'https://modao.cc/agent-py/media/generated_images/2026-03-19/4d9bd78d593745d0b800d9220fde2bed.jpg',
    publisher: '老周', credit: '9.8', time: '10分钟前',
  },
  {
    image: 'https://modao.cc/agent-py/media/generated_images/2026-03-19/4f9f6627f2f84d878b046babe25d4158.jpg',
    name: '黑狗轻量化帐篷', category: '户外装备', price: '￥15/天',
    avatar: 'https://modao.cc/agent-py/media/generated_images/2026-03-19/b98358a8bc9e4e5c976cb8720c029348.jpg',
    publisher: '阿强', credit: '9.2', time: '1小时前',
  },
  {
    image: '',
    name: '富士 X100VI 相机', category: '数码影音', price: '￥80/天',
    avatar: '', publisher: 'Linda', credit: '9.9', time: '3小时前',
  },
]

const quickActions = [
  {
    icon: 'solar:user-plus-bold', label: '添加用户', desc: '手动录入业主信息',
    iconBg: 'bg-gradient-to-br from-[#FFF5E3] to-[#FDF6E3] group-hover:from-[#C9A227] group-hover:to-[#E8D48B] group-hover:shadow-lg',
    iconColor: 'text-[#C9A227] group-hover:text-white',
  },
  {
    icon: 'solar:document-text-bold', label: '生成报表', desc: '导出月度运营数据',
    iconBg: 'bg-gradient-to-br from-[#FEF3DB] to-[#FFF5E3] group-hover:from-[#D4942A] group-hover:to-[#E8A84D] group-hover:shadow-lg',
    iconColor: 'text-[#D4942A] group-hover:text-white',
  },
  {
    icon: 'solar:leaf-bold', label: '环保统计', desc: '查看碳减排详情',
    iconBg: 'bg-gradient-to-br from-[#EDF7EF] to-[#F2FAF4] group-hover:from-[#4A9D6E] group-hover:to-[#6AB88A] group-hover:shadow-lg',
    iconColor: 'text-[#4A9D6E] group-hover:text-white',
  },
  {
    icon: 'solar:shield-warning-bold', label: '风险监控', desc: '异常行为检测',
    iconBg: 'bg-gradient-to-br from-[#FDF0EB] to-[#FEF5F1] group-hover:from-[#D4644A] group-hover:to-[#E8886A] group-hover:shadow-lg',
    iconColor: 'text-[#D4644A] group-hover:text-white',
  },
]

const liveEvents = [
  { dotColor: 'bg-emerald-500', text: '<span class="font-bold text-[#C9A227]">老周</span> 发布了新物品', detail: '博世电钻套装 · 刚刚' },
  { dotColor: 'bg-[#D4942A]', text: '<span class="font-bold text-[#3D3426]">小雅</span> 借阅了戴森吸尘器', detail: '来自 李大明白 · 5分钟前' },
  { dotColor: 'bg-[#E8A84D]', text: '<span class="font-bold text-[#3D3426]">王阿姨</span> 发布了感谢信', detail: '收到草鱼分享 · 12分钟前' },
  { dotColor: 'bg-[#D4644A]', text: '系统检测到<span class="font-bold text-[#D4644A]">异常登录</span>', detail: 'IP: 192.168.1.xxx · 1小时前' },
  { dotColor: 'bg-emerald-500', text: '<span class="font-bold text-[#C9A227]">陈工</span> 归还了物品', detail: '水粉画架 · 2小时前' },
]

const monthlyBars = [
  { label: '物品上架率', value: '85%', percent: 85, barGradient: 'bg-gradient-to-r from-[#C9A227] to-[#E8D48B]' },
  { label: '借阅成功率', value: '92%', percent: 92, barGradient: 'bg-gradient-to-r from-[#D4942A] to-[#E8A84D]' },
  { label: '用户满意度', value: '4.8/5.0', percent: 96, barGradient: 'bg-gradient-to-r from-[#4A9D6E] to-[#6AB88A]' },
  { label: '纠纷处理率', value: '待提高 65%', percent: 65, barGradient: 'bg-gradient-to-r from-[#D4644A] to-[#E8886A]', valueColor: 'text-[#D4644A]' },
]

const quickTools = ['批量审核', '信用调整', '紧急冻结', '数据备份']
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

.nav-item {
  display: flex;
  align-items: center;
  padding: 11px 16px;
  border-radius: 13px;
  color: #9A9082;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.25s;
  margin-bottom: 3px;
  cursor: pointer;
}
.nav-item:hover {
  color: #E8D48B;
  background: rgba(201, 162, 39, 0.08);
}
.nav-item.active {
  color: white;
  background: linear-gradient(135deg, #C9A227 0%, #B8911F 100%);
  box-shadow: 0 4px 15px rgba(201, 162, 39, 0.3);
}
.nav-icon {
  width: 21px;
  height: 21px;
  flex-shrink: 0;
}
.nav-text {
  margin-left: 12px;
  flex: 1;
}

.badge {
  padding: 2px 9px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.02em;
}
.badge-gold { background: #C9A227; color: white; }
.badge-warm { background: #E8A854; color: white; }
.badge-red { background: #D4644A; color: white; }

.data-table th {
  background: #FFF5E3;
  color: #8C7D66;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  padding: 14px 20px;
  text-align: left;
}
.data-table td {
  padding: 16px 20px;
  border-bottom: 1px solid #FFF5E3;
  vertical-align: middle;
  font-size: 14px;
}
.data-table tbody tr { transition: background 0.2s; }
.data-table tbody tr:hover { background: #FFFBF5; }

.feed-item {
  padding: 14px 0;
  border-bottom: 1px dashed rgba(201, 162, 39, 0.15);
}
.feed-item:last-child { border-bottom: none; }

.progress-bar {
  height: 6px;
  background: #F5E6C8;
  border-radius: 10px;
  overflow: hidden;
}
.progress-fill {
  height: 100%;
  border-radius: 10px;
  transition: width 0.8s ease;
}

.btn-primary {
  background: linear-gradient(135deg, #C9A227 0%, #B8911F 100%);
  color: white;
  font-weight: 600;
  border-radius: 14px;
  padding: 10px 22px;
  box-shadow: 0 4px 16px rgba(201, 162, 39, 0.3);
  transition: all 0.25s;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  border: none;
  font-size: 14px;
}
.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(201, 162, 39, 0.4);
}
.btn-primary:active { transform: translateY(0); }

@keyframes pulse-dot {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(1.3); }
}
.pulse-dot { animation: pulse-dot 2s infinite; }
</style>
