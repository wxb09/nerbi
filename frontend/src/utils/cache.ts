interface CacheEntry<T> {
  data: T
  timestamp: number
  expiresAt: number
}

class ApiCache {
  private cache = new Map<string, CacheEntry<any>>()
  private pendingRequests = new Map<string, Promise<any>>()

  private readonly DEFAULT_TTL = 5 * 60 * 1000

  private getKey(url: string, params?: any): string {
    return params ? `${url}?${JSON.stringify(params)}` : url
  }

  get<T>(url: string, params?: any): T | null {
    const key = this.getKey(url, params)
    const entry = this.cache.get(key)

    if (!entry) return null
    if (Date.now() > entry.expiresAt) {
      this.cache.delete(key)
      return null
    }
    return entry.data
  }

  set<T>(url: string, params: any, data: T, ttl?: number): void {
    const key = this.getKey(url, params)
    this.cache.set(key, {
      data,
      timestamp: Date.now(),
      expiresAt: Date.now() + (ttl || this.DEFAULT_TTL)
    })
  }

  clear(pattern?: string): void {
    if (!pattern) {
      this.cache.clear()
      return
    }
    for (const key of this.cache.keys()) {
      if (key.includes(pattern)) this.cache.delete(key)
    }
  }

  async dedupe<T>(key: string, request: () => Promise<T>): Promise<T> {
    if (this.pendingRequests.has(key)) {
      return this.pendingRequests.get(key)!
    }

    const promise = request().finally(() => {
      this.pendingRequests.delete(key)
    })

    this.pendingRequests.set(key, promise)
    return promise
  }
}

export const apiCache = new ApiCache()
