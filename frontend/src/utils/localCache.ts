export const localCache = {
  set<T>(key: string, data: T, ttlMinutes: number = 60): void {
    const entry = {
      data,
      expiresAt: Date.now() + ttlMinutes * 60 * 1000
    }
    localStorage.setItem(`neighbor_cache_${key}`, JSON.stringify(entry))
  },

  get<T>(key: string): T | null {
    const raw = localStorage.getItem(`neighbor_cache_${key}`)
    if (!raw) return null

    try {
      const entry = JSON.parse(raw)
      if (Date.now() > entry.expiresAt) {
        localStorage.removeItem(`neighbor_cache_${key}`)
        return null
      }
      return entry.data
    } catch {
      return null
    }
  },

  remove(key: string): void {
    localStorage.removeItem(`neighbor_cache_${key}`)
  },

  clear(): void {
    for (let i = localStorage.length - 1; i >= 0; i--) {
      const key = localStorage.key(i)
      if (key?.startsWith('neighbor_cache_')) {
        localStorage.removeItem(key)
      }
    }
  }
}
