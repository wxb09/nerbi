<template>
  <nav class="sticky top-0 z-50 bg-white border-b border-gray-100 px-6 py-4">   
    <div class="max-w-7xl mx-auto flex justify-between items-center">
      <RouterLink class="flex items-center space-x-2" to="/index">
        <span class="iconify text-3xl text-[#E2B04D]" data-icon="solar:share-circle-bold-duotone"></span>
        <span class="text-2xl font-bold tracking-tight text-[#2D3436]">邻里共享<span class="text-[#E2B04D]">.</span></span>
      </RouterLink>
      <div class="hidden md:flex space-x-8 font-medium">
        <RouterLink class="hover:text-[#E2B04D]" to="/index">发现物品</RouterLink>
        <RouterLink class="hover:text-[#E2B04D]" to="/forum">社区论坛</RouterLink>
        <RouterLink class="hover:text-[#E2B04D]" to="/publish">发布闲置</RouterLink>
      </div>
      <div class="flex items-center gap-4">
        <div v-if="authStore.isLoggedIn" class="flex items-center gap-2">
          <span class="text-sm">{{ authStore.user?.nickname }}</span>
          <RouterLink class="text-sm px-3 py-1.5 rounded-full border border-gray-200" to="/profile">
            我的
          </RouterLink>
          <button class="text-sm bg-[#2D3436] text-white px-4 py-2 rounded-xl" @click="logout">
            退出
          </button>
        </div>
        <div v-else>
          <RouterLink class="text-sm px-3 py-1.5 rounded-full border border-gray-200" to="/profile">
            我的
          </RouterLink>
          <RouterLink class="text-sm bg-[#2D3436] text-white px-4 py-2 rounded-xl" to="/login">
            登录
          </RouterLink>
        </div>
      </div>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { authApi } from '../api/auth'

const router = useRouter()
const authStore = useAuthStore()

onMounted(() => {
  authStore.init()
})

const logout = () => {
  authStore.logout()
  router.push('/login')
}
</script>
