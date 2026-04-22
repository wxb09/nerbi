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
          :key="item.key"
          class="nav-item"
          :class="activeTab === item.key ? 'active' : ''"
          @click="activeTab = item.key"
        >
          <span class="iconify nav-icon" :data-icon="item.icon"></span>
          <span v-if="!sidebarCollapsed" class="nav-text">{{ item.label }}</span>
        </a>

        <p v-if="!sidebarCollapsed" class="px-4 pt-5 pb-2 text-[10px] font-bold text-white/25 uppercase tracking-[0.18em]">运营</p>
        <a
          v-for="item in opsItems"
          :key="item.key"
          class="nav-item"
          :class="activeTab === item.key ? 'active' : ''"
          @click="activeTab = item.key"
        >
          <span class="iconify nav-icon" :data-icon="item.icon"></span>
          <span v-if="!sidebarCollapsed" class="nav-text">{{ item.label }}</span>
        </a>

        <p v-if="!sidebarCollapsed" class="px-4 pt-5 pb-2 text-[10px] font-bold text-white/25 uppercase tracking-[0.18em]">设置</p>
        <a
          v-for="item in sysItems"
          :key="item.key"
          class="nav-item"
          :class="activeTab === item.key ? 'active' : ''"
          @click="activeTab = item.key"
        >
          <span class="iconify nav-icon" :data-icon="item.icon"></span>
          <span v-if="!sidebarCollapsed" class="nav-text">{{ item.label }}</span>
        </a>
      </nav>

      <div v-if="!sidebarCollapsed" class="p-4 border-t border-white/[0.06]">
        <div class="flex items-center px-3 py-3 rounded-xl bg-white/[0.05] hover:bg-white/[0.08] transition-colors cursor-pointer" @click="goHome">
          <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-[#C9A227] to-[#E8D48B] flex items-center justify-center flex-shrink-0">
            <span class="iconify text-lg text-[#1A1614]" data-icon="solar:share-circle-bold"></span>
          </div>
          <div class="ml-3">
            <p class="font-semibold text-sm">{{ adminName }}</p>
            <p class="text-[11px] text-white/35">管理员</p>
          </div>
          <button class="ml-auto text-white/30 hover:text-[#C9A227] transition-colors" @click.stop="goHome">
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
            <h2 class="text-xl font-bold text-[#3D3426]">{{ currentTitle }}</h2>
          </div>
        </div>
      </header>

      <div class="flex-1 overflow-y-auto p-7">
        <AdminDashboard v-if="activeTab === 'dashboard'" />
        <AdminDepositDisputes v-else-if="activeTab === 'deposit-disputes'" />
        <AdminUsers v-else-if="activeTab === 'users'" />
        <AdminItems v-else-if="activeTab === 'items'" />
        <AdminBorrows v-else-if="activeTab === 'borrows'" />
        <AdminVerifies v-else-if="activeTab === 'verifies'" />
        <AdminHome v-else-if="activeTab === 'home'" />
        <AdminCommunity v-else-if="activeTab === 'community'" />
        <AdminSensitiveWords v-else-if="activeTab === 'sensitive-words'" />
        <AdminSettings v-else-if="activeTab === 'settings'" />

        <footer class="mt-8 pt-6 border-t border-[#E8D48B]/15 text-center">
          <p class="text-xs text-[#B8AE9E]">© 2026 邻里共享平台 · 管理后台</p>
        </footer>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import AdminDashboard from '../components/admin/AdminDashboard.vue'
import AdminUsers from '../components/admin/AdminUsers.vue'
import AdminItems from '../components/admin/AdminItems.vue'
import AdminBorrows from '../components/admin/AdminBorrows.vue'
import AdminVerifies from '../components/admin/AdminVerifies.vue'
import AdminDepositDisputes from '../components/admin/AdminDepositDisputes.vue'
import AdminHome from '../components/admin/AdminHome.vue'
import AdminCommunity from '../components/admin/AdminCommunity.vue'
import AdminSettings from '../components/admin/AdminSettings.vue'
import AdminSensitiveWords from '../components/admin/AdminSensitiveWords.vue'

const router = useRouter()
const authStore = useAuthStore()
const sidebarCollapsed = ref(false)
const activeTab = ref('dashboard')

const adminName = computed(() => {
  return authStore.user?.nickname || '管理员'
})

const navItems = [
  { key: 'dashboard', label: '数据仪表盘', icon: 'solar:chart-square-bold' },
  { key: 'deposit-disputes', label: '押金纠纷', icon: 'solar:wallet-money-bold' },
  { key: 'items', label: '物品审核', icon: 'solar:box-bold' },
  { key: 'borrows', label: '借阅纠纷', icon: 'solar:shield-warning-bold' },
  { key: 'verifies', label: '认证审核', icon: 'solar:verified-check-bold' },
  { key: 'users', label: '用户管理', icon: 'solar:users-group-rounded-bold' },
]

const opsItems = [
  { key: 'home', label: '首页管理', icon: 'solar:home-bold' },
  { key: 'community', label: '论坛管理', icon: 'solar:buildings-bold' },
  { key: 'sensitive-words', label: '敏感词管理', icon: 'solar:shield-check-bold' },
]

const sysItems = [
  { key: 'settings', label: '系统设置', icon: 'solar:settings-bold' },
]

const currentTitle = computed(() => {
  const item = navItems.find(n => n.key === activeTab.value) || opsItems.find(n => n.key === activeTab.value) || sysItems.find(n => n.key === activeTab.value)
  return item?.label || '管理后台'
})

function goHome() {
  router.push('/index')
}
</script>

<style scoped>
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
</style>
