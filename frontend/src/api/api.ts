import axios from 'axios'
import { useAuthStore } from '../stores/auth'
import { apiCache } from '../utils/cache'

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000
})

const CACHE_CONFIG: Record<string, number> = {
  '/categories': 30 * 60 * 1000,
  '/communities': 30 * 60 * 1000,
  '/items': 2 * 60 * 1000,
  '/items/search': 2 * 60 * 1000,
  '/forum/posts': 2 * 60 * 1000,
  '/forum/posts/my': 2 * 60 * 1000,
  '/announcements': 5 * 60 * 1000,
}

const CACHE_PATTERNS = [
  { pattern: /^\/items\/\d+$/, ttl: 5 * 60 * 1000 },
  { pattern: /^\/forum\/posts\/\d+$/, ttl: 5 * 60 * 1000 },
  { pattern: /^\/forum\/comments\/post\/\d+$/, ttl: 2 * 60 * 1000 },
  { pattern: /^\/announcements\/\d+$/, ttl: 5 * 60 * 1000 },
]

function getCacheConfig(url: string): number | undefined {
  if (CACHE_CONFIG[url]) return CACHE_CONFIG[url]

  for (const item of CACHE_PATTERNS) {
    if (item.pattern.test(url)) return item.ttl
  }

  return undefined
}

function getInvalidatePattern(method: string, url: string): string | undefined {
  if (method !== 'POST' && method !== 'PUT' && method !== 'DELETE') {
    return undefined
  }

  if (url === '/items' || url.startsWith('/items/')) {
    return '/items'
  }

  if (url === '/forum/posts' || url.startsWith('/forum/posts/')) {
    return '/forum/posts'
  }

  if (url.startsWith('/forum/comments')) {
    return '/forum/comments'
  }

  if (url.startsWith('/announcements')) {
    return '/announcements'
  }

  return undefined
}

api.interceptors.request.use(config => {
  const authStore = useAuthStore()
  if (authStore.token) {
    config.headers.Authorization = `Bearer ${authStore.token}`
  }

  if (config.method?.toLowerCase() === 'get' && config.url) {
    const cacheTime = getCacheConfig(config.url)
    if (cacheTime) {
      const cached = apiCache.get(config.url, config.params)
      if (cached) {
        return Promise.reject({
          __cached: true,
          data: cached
        })
      }
    }
  }

  return config
})

api.interceptors.response.use(
  response => {
    const data = response.data
    if (data.code !== undefined && data.code !== 0) {
      const error = new Error(data.message || '请求失败')
      ;(error as any).response = { data }
      return Promise.reject(error)
    }

    const config = response.config
    const method = config.method?.toLowerCase()

    if (method === 'get' && config.url) {
      const cacheTime = getCacheConfig(config.url)
      if (cacheTime) {
        apiCache.set(config.url, config.params, data.data, cacheTime)
      }
    }

    if ((method === 'post' || method === 'put' || method === 'delete') && config.url) {
      const invalidatePattern = getInvalidatePattern(method.toUpperCase(), config.url)
      if (invalidatePattern) {
        apiCache.clear(invalidatePattern)
      }
    }

    return data.data
  },
  error => {
    if (error.__cached) {
      return Promise.resolve(error.data)
    }

    if (error.response?.data?.code === 1001) {
      const authStore = useAuthStore()
      authStore.logout()
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default api
