<template>
  <main class="min-h-screen flex items-center justify-center p-6 bg-[#121212]">
    <div class="max-w-5xl w-full min-h-[800px] grid grid-cols-1 md:grid-cols-2 gap-0 overflow-hidden shadow-2xl ">
    <!--桌面端圆角效果 ：

- 整个容器添加 rounded-3xl
- 左侧金色区域： rounded-l-3xl （左侧圆角）
- 右侧登录卡片： border-radius: 0 24px 24px 0 （右侧圆角） 251行左右-->
      <div class="hidden md:block bg-[#E2B04D] p-12 relative overflow-hidden">
        <div class="relative z-10 h-full flex flex-col justify-between text-white">
          <div class="flex items-center space-x-2">
            <span class="iconify text-4xl" data-icon="solar:share-circle-bold-duotone"></span>
            <span class="text-2xl font-bold italic">2026 邻里精神.</span>
          </div>
          <div>
            <h2 class="text-4xl font-bold leading-tight mb-4">通过每一件物品<br/>连接一个真实的人。</h2>
            <p class="text-orange-900 border-l-4 border-orange-900 pl-4 py-2 font-medium">加入 24,501 个正在共享的邻里单位</p>
          </div>
          <div class="text-sm opacity-60">© 2026 COMMUNITY SHARING INTERFACE</div>
        </div>
        <div class="absolute -right-20 -bottom-20 w-80 h-80 bg-white opacity-10 rounded-full"></div>
        <div class="absolute top-10 left-10 w-20 h-20 bg-black opacity-5 rounded-full"></div>
      </div>

      <div class="login-card p-10 md:p-16 flex flex-col">
        <div class="mb-10 flex justify-between items-center">
          <h1 class="text-3xl font-bold text-[#2D3426]">欢迎回来</h1>
          <RouterLink class="text-xs text-gray-400 hover:text-[#E2B04D]" to="/index">先去随便看看 →</RouterLink>
        </div>

        <div class="flex space-x-8 mb-10 border-b border-gray-100">
          <button 
            class="pb-4 font-bold border-b-2 transition-colors"
            :class="loginMode === 'phone' ? 'border-[#E2B04D] text-[#E2B04D]' : 'border-transparent text-gray-300 hover:text-gray-400'"
            @click="loginMode = 'phone'"
          >
            手机号登录
          </button>
          <button 
            class="pb-4 font-bold border-b-2 transition-colors"
            :class="loginMode === 'password' ? 'border-[#E2B04D] text-[#E2B04D]' : 'border-transparent text-gray-300 hover:text-gray-400'"
            @click="loginMode = 'password'"
          >
            密码登录
          </button>
        </div>

        <form class="space-y-6 flex-1" @submit.prevent="handleSubmit">
          <div class="space-y-2">
            <label class="text-xs font-bold text-gray-400 uppercase tracking-widest">认证手机号</label>
            <div class="relative">
              <span class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-300">
                <span class="iconify" data-icon="solar:phone-bold"></span>
              </span>
              <input
                v-model="phone"
                class="w-full pl-12 pr-4 py-4 bg-gray-50 border-transparent rounded-2xl outline-none focus:ring-2 ring-[#E2B04D]/20 focus:bg-white transition-all"
                placeholder="请输入手机号"
                type="tel"
                maxlength="11"
              />
            </div>
          </div>

          <div class="space-y-2 min-h-[88px]">
            <label class="text-xs font-bold text-gray-400 uppercase tracking-widest">
              {{ loginMode === 'phone' ? '动态验证码' : '登录密码' }}
            </label>
            <div v-if="loginMode === 'phone'" class="flex gap-4">
              <div class="relative flex-1">
                <span class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-300">
                  <span class="iconify" data-icon="solar:lock-keyhole-bold"></span>
                </span>
                <input
                  v-model="verifyCode"
                  class="w-full pl-12 pr-4 py-4 bg-gray-50 border-transparent rounded-2xl outline-none focus:ring-2 ring-[#E2B04D]/20 focus:bg-white transition-all"
                  placeholder="请输入验证码"
                  type="text"
                  maxlength="6"
                />
              </div>
              <button
                type="button"
                :disabled="countdown > 0"
                class="px-6 py-4 bg-white border-2 border-gray-100 rounded-2xl font-bold text-sm hover:border-[#E2B04D] hover:text-[#E2B04D] transition-all disabled:opacity-50 disabled:cursor-not-allowed whitespace-nowrap"
                @click="sendVerifyCode"
              >
                {{ countdown > 0 ? `${countdown}s` : '获取' }}
              </button>
            </div>
            <div v-else class="relative">
              <span class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-300">
                <span class="iconify" data-icon="solar:lock-password-bold"></span>
              </span>
              <input
                v-model="password"
                class="w-full pl-12 pr-12 py-4 bg-gray-50 border-transparent rounded-2xl outline-none focus:ring-2 ring-[#E2B04D]/20 focus:bg-white transition-all"
                placeholder="请输入密码"
                :type="showPassword ? 'text' : 'password'"
                maxlength="20"
              />
              <button
                type="button"
                class="absolute right-4 top-1/2 -translate-y-1/2 text-gray-300 hover:text-gray-500"
                @click="showPassword = !showPassword"
              >
                <span class="iconify" :data-icon="showPassword ? 'solar:eye-bold' : 'solar:eye-closed-bold'"></span>
              </button>
            </div>
          </div>

          <button
            type="submit"
            :disabled="submitting"
            class="block w-full text-center py-5 bg-[#2D3436] text-white rounded-[2rem] font-bold text-sm tracking-widest uppercase transition-all hover:shadow-[0_0_20px_#E2B04D] hover:scale-[1.02] disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:scale-100 disabled:hover:shadow-none"
          >
            {{ submitting ? '验证中...' : '验证并进入社区' }}
          </button>
        </form>

        <div v-if="message" class="mt-4 text-center text-sm" :class="isError ? 'text-red-500' : 'text-green-500'">
          {{ message }}
        </div>

        <div class="mt-auto pt-10">
          <div class="h-[18px] text-center text-[10px] text-gray-400">
            {{ loginMode === 'phone' ? '测试验证码：123456' : '' }}
          </div>
          <p class="mt-2 text-center text-[10px] text-gray-300 leading-relaxed">
            点击登录即同意 <a class="underline hover:text-[#E2B04D]" href="#">《用户协议》</a> 和 <a class="underline hover:text-[#E2B04D]" href="#">《隐私政策》</a>
          </p>
        </div>
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

const loginMode = ref<'phone' | 'password'>('phone')
const phone = ref('13800002026')
const verifyCode = ref('123456')
const password = ref('')
const showPassword = ref(false)
const countdown = ref(0)
const message = ref('')
const isError = ref(false)
const submitting = ref(false)

const sendVerifyCode = async () => {
  if (!phone.value || phone.value.length !== 11) {
    message.value = '请输入正确的手机号'
    isError.value = true
    return
  }
  try {
    await authApi.sendVerifyCode(phone.value)
    message.value = '验证码已发送'
    isError.value = false
    startCountdown()
  } catch (error) {
    message.value = '发送失败，请重试'
    isError.value = true
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

const handleSubmit = async () => {
  if (!phone.value || phone.value.length !== 11) {
    message.value = '请输入正确的手机号'
    isError.value = true
    return
  }

  if (loginMode.value === 'phone') {
    if (!verifyCode.value) {
      message.value = '请输入验证码'
      isError.value = true
      return
    }
    await loginByPhone()
  } else {
    if (!password.value) {
      message.value = '请输入密码'
      isError.value = true
      return
    }
    await loginByPassword()
  }
}

const loginByPhone = async () => {
  submitting.value = true
  try {
    const res = await authApi.loginByPhone({ phone: phone.value, verifyCode: verifyCode.value })
    authStore.login(res.user, res.token)
    message.value = ''
    if (res.user?.role === 'ADMIN') {
      router.push('/admin')
    } else {
      router.push('/index')
    }
  } catch (error: any) {
    authStore.logout()
    message.value = error.message || '登录失败，请重试'
    isError.value = true
  } finally {
    submitting.value = false
  }
}

const loginByPassword = async () => {
  submitting.value = true
  try {
    const res = await authApi.loginByPassword({ phone: phone.value, password: password.value })
    authStore.login(res.user, res.token)
    message.value = ''
    if (res.user?.role === 'ADMIN') {
      router.push('/admin')
    } else {
      router.push('/index')
    }
  } catch (error: any) {
    authStore.logout()
    message.value = error.message || '手机号或密码错误'
    isError.value = true
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.login-card {
  background: #FDFBFA;
  border-radius: 40px 10px 40px 40px;
}
@media (min-width: 768px) {
  .login-card {
    border-radius: 24 10px 24px 24px;
  }
}
</style>
