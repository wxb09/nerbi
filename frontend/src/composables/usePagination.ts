import { ref, computed, type Ref } from 'vue'

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

export interface UsePaginationOptions<T> {
  pageSize?: number
  mode?: 'append' | 'replace'
  params?: Ref<Record<string, any>>
}

export function usePagination<T>(
  fetchFn: (page: number, size: number, params?: Record<string, any>) => Promise<any>,
  options: UsePaginationOptions<T> = {}
) {
  const {
    pageSize = 10,
    mode = 'replace',
    params
  } = options

  const loading = ref(false)
  const error = ref('')
  const data = ref<T[]>([]) as Ref<T[]>
  const page = ref(0)
  const totalPages = ref(1)
  const totalElements = ref(0)

  const hasMore = computed(() => page.value < totalPages.value)
  const isEmpty = computed(() => data.value.length === 0 && !loading.value)

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

    loading.value = true
    error.value = ''

    try {
      const res = await fetchFn(page.value, pageSize, params?.value)
      const pageData: PageResponse<T> = res

      if (reset || page.value === 0) {
        data.value = pageData.content as T[]
      } else {
        data.value.push(...pageData.content)
      }

      totalPages.value = pageData.totalPages
      totalElements.value = pageData.totalElements
      page.value++
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
    await load(true)
  }

  const goToPage = async (targetPage: number) => {
    if (targetPage < 0 || targetPage >= totalPages.value) return
    await load(true, targetPage)
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
    goToPage
  }
}
