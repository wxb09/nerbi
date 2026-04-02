<template>
  <main class="min-h-screen flex items-center justify-center p-6 bg-[#121212]">
    <div class="w-full max-w-md bg-white rounded-3xl p-8 space-y-6">
      <div class="flex justify-between items-center">
        <h1 class="text-2xl font-bold">欢迎回来</h1>
        <RouterLink class="text-sm text-gray-400 hover:text-[#E2B04D]" to="/index">先去看看</RouterLink>
      </div>
      <div class="space-y-3">
        <input
          v-model="phone"
          class="w-full p-3 bg-gray-50 rounded-xl border"
          placeholder="手机号"
        />
        <div class="flex gap-2">
          <input
            v-model="verifyCode"
            class="flex-1 p-3 bg-gray-50 rounded-xl border"
            placeholder="验证码"
          />
          <button
            :disabled="countdown > 0"
            class="px-4 rounded-xl border bg-white"
            @click="sendVerifyCode"
          >
            {{ countdown > 0 ? `${countdown}秒` : '获取' }}
          </button>
        </div>
      </div>
      <button
        class="block w-full text-center py-3 rounded-xl bg-[#2D3436] text-white font-bold"
        @click="login"
      >
        验证并进入社区
      </button>
      <div class="text-xs text-gray-400 text-center">
        {{ message }}
      </div>
    </div>
  </main>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { authApi } from '../api/auth'

const router = useRouter()
const authStore = useAuthStore()
const phone = ref('13800002026')
const verifyCode = ref('123456')
const countdown = ref(0)
const message = ref('')

const sendVerifyCode = async () => {
  if (!phone.value) {
    message.value = '请输入手机号'
    return
  }
  try {
    const res = await authApi.sendVerifyCode(phone.value)
    message.value = '验证码已发送'
    startCountdown()
  } catch (error) {
    message.value = '发送失败，请重试'
  }
}

const startCountdown = () => {
  countdown.value = 60
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

const login = async () => {
  if (!phone.value || !verifyCode.value) {
    message.value = '请填写完整信息'
    return
  }
  try {
    const res = await authApi.login({ phone: phone.value, verifyCode: verifyCode.value })
    authStore.login(res.user, res.token)
    router.push('/index')
  } catch (error: any) {
    // 登录失败时，清除可能存在的旧登录状态
    authStore.logout()
    message.value = error.response?.data?.message || '登录失败，请重试'  
  }
}
</script>
