import axios from 'axios'
import { useAuthStore } from '../stores/auth'

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000
})

// 请求拦截器
api.interceptors.request.use(config => {
  const authStore = useAuthStore()
  if (authStore.token) {
    config.headers.Authorization = `Bearer ${authStore.token}`
  }
  return config
})

// 响应拦截器
api.interceptors.response.use(
  response => {
    // 检查业务状态码
    const data = response.data
    if (data.code !== undefined && data.code !== 0) {
      // 业务错误，抛出异常
      const error = new Error(data.message || '请求失败')
      ;(error as any).response = { data }
      return Promise.reject(error)
    }
    // 成功时直接返回data.data，方便前端使用
    return data.data
  },
  error => {
    if (error.response?.data?.code === 1001) {
      // Token 无效，跳转到登录页
      const authStore = useAuthStore()
      authStore.logout()
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default api
