import { ref, computed, type Ref } from 'vue'
import { apiCache } from '../utils/cache'

export interface PageResponse<T> {
  content: T[]
  number: number
  size: number
  totalPages: number
  totalElements: number
  first: boolean
  last: boolean
  hasNext: boolean
  hasPrevious: boolean
}

export interface UsePaginationOptions {
  pageSize?: number
  mode?: 'append' | 'replace'
  params?: Ref<Record<string, any>>
  cacheKey?: string
  cacheTTL?: number
}

export function usePagination<T>(
  fetchFn: (page: number, size: number, params?: Record<string, any>) => Promise<any>,
  options: UsePaginationOptions = {}
) {
  const {
    pageSize = 10,
    mode = 'replace',
    params,
    cacheKey,
    cacheTTL = 2 * 60 * 1000
  } = options

  const loading = ref(false)
  const error = ref('')
  const data = ref<T[]>([]) as Ref<T[]>
  const page = ref(0)
  const totalPages = ref(1)
  const totalElements = ref(0)

  const hasMore = computed(() => page.value < totalPages.value)
  const isEmpty = computed(() => data.value.length === 0 && !loading.value)

  const getCacheKey = (pageNum: number, pageParams?: Record<string, any>): string | undefined => {
    if (!cacheKey) return undefined
    return `${cacheKey}_page${pageNum}_${JSON.stringify(pageParams || {})}`
  }

  const load = async (reset = false, targetPage?: number) => {
    if (loading.value) return

    if (reset) {
      if (targetPage !== undefined) {
        page.value = targetPage
      } else {
        page.value = 0
      }
      if (mode === 'replace') {
        data.value = [] as T[]
      }
    }

    const currentPageNum = page.value
    const currentParams = params?.value

    if (reset && cacheKey && currentPageNum === 0) {
      const key = getCacheKey(currentPageNum, currentParams)
      if (key) {
        const cached = apiCache.get<PageResponse<T>>(key)
        if (cached) {
          data.value = cached.content as T[]
          totalPages.value = cached.totalPages
          totalElements.value = cached.totalElements
          page.value = currentPageNum + 1
          return
        }
      }
    }

    loading.value = true
    error.value = ''

    try {
      const res = await fetchFn(currentPageNum, pageSize, currentParams)
      const pageData: PageResponse<T> = res

      if (reset || currentPageNum === 0) {
        data.value = pageData.content as T[]
      } else {
        data.value.push(...pageData.content)
      }

      totalPages.value = pageData.totalPages
      totalElements.value = pageData.totalElements
      page.value = currentPageNum + 1

      if (cacheKey && currentPageNum === 0) {
        const key = getCacheKey(currentPageNum, currentParams)
        if (key) {
          apiCache.set(key, undefined, pageData, cacheTTL)
        }
      }
    } catch (e: any) {
      error.value = e.message || '加载失败'
      console.error('分页加载错误:', e)
    } finally {
      loading.value = false
    }
  }

  const loadMore = async () => {
    if (!hasMore.value || loading.value) return
    await load(false)
  }

  const refresh = async () => {
    if (cacheKey) {
      apiCache.clear(cacheKey)
    }
    await load(true)
  }

  const goToPage = async (targetPage: number) => {
    if (targetPage < 0 || targetPage >= totalPages.value) return
    await load(true, targetPage)
  }

  const clearCache = () => {
    if (cacheKey) {
      apiCache.clear(cacheKey)
    }
  }

  return {
    loading,
    error,
    data,
    page: computed(() => Math.max(0, page.value - 1)),
    totalPages,
    totalElements,
    hasMore,
    isEmpty,
    loadMore,
    refresh,
    goToPage,
    clearCache
  }
}
