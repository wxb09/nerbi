import api from './api'
import { localCache } from '../utils/localCache'

export interface Category {
  id: number
  name: string
  icon: string
  sortOrder: number
}

export interface Community {
  id: number
  name: string
  address: string
}

export const publicApi = {
  getCategories: async () => {
    const cached = localCache.get<Category[]>('categories')
    if (cached) return cached

    const data = await api.get<Category[]>('/categories')
    localCache.set('categories', data, 30)
    return data
  },

  getCommunities: async () => {
    const cached = localCache.get<Community[]>('communities')
    if (cached) return cached

    const data = await api.get<Community[]>('/communities')
    localCache.set('communities', data, 30)
    return data
  },

  getCarbonStats: () =>
    api.get<{ totalCo2Saved: number; todayCo2Saved: number }>('/stats/carbon'),

  clearCategoriesCache: () => {
    localCache.remove('categories')
  },

  clearCommunitiesCache: () => {
    localCache.remove('communities')
  }
}
