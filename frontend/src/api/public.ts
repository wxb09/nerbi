import api from './api'

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
  getCategories: () => 
    api.get<Category[]>('/categories'),
  
  getCommunities: () => 
    api.get<Community[]>('/communities'),
  
  getCarbonStats: () => 
    api.get<{ totalCo2Saved: number; todayCo2Saved: number }>('/stats/carbon')
}
