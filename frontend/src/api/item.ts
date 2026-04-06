import api from './api'

export interface Item {
  id: number
  name: string
  categoryId: number
  categoryName?: string
  description: string
  story?: string
  pricePerDay: number
  deposit: number
  creditRequired: number
  returnRequirements: string[]
  tags: string[]
  images: string[]
  status: string
  borrowCount: number
  viewCount: number
  createdAt: string
}

export interface CreateItemRequest {
  name: string
  categoryId: number
  description: string
  story?: string
  pricePerDay: number
  deposit: number
  creditRequired: number
  returnRequirements: string[]
  tags: string[]
  images: string[]
}

export const itemApi = {
  getItems: (params: any) => 
    api.get('/items', { params }),
  
  searchItems: (keyword: string, params?: any) => 
    api.get('/items/search', { params: { keyword, ...params } }),
  
  getItemById: (id: string) => 
    api.get(`/items/${id}`),
  
  createItem: (data: CreateItemRequest) => 
    api.post('/items', data),
  
  updateItem: (id: number, data: any) => 
    api.put(`/items/${id}`, data),
  
  withdrawItem: (id: number) => 
    api.put(`/items/${id}/withdraw`),
  
  deleteItem: (id: number) => 
    api.delete(`/items/${id}`),
  
  saveDraft: (data: any) => 
    api.post('/items/draft', data),
  
  getMyDrafts: () => 
    api.get('/items/my-drafts'),
  
  getSimilarItems: (id: number, limit: number = 4) => 
    api.get(`/items/${id}/similar`, { params: { limit } })
}
