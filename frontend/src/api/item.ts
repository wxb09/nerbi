import api from './api'

export const itemApi = {
  getItems: (params?: Record<string, any>) => 
    api.get<Array<Record<string, any>>>('/items', { params }),
  
  searchItems: (keyword: string, params?: Record<string, any>) => 
    api.get<Array<Record<string, any>>>('/items/search', { params: { keyword, ...params } }),
  
  getItemById: (id: string) => 
    api.get<Record<string, any>>(`/items/${id}`),
  
  createItem: (data: Record<string, any>) => 
    api.post<Record<string, any>>('/items', data),
  
  updateItem: (id: string, data: Record<string, any>) => 
    api.put<Record<string, any>>(`/items/${id}`, data),
  
  withdrawItem: (id: string) => 
    api.put<Record<string, any>>(`/items/${id}/withdraw`),
  
  saveDraft: (data: Record<string, any>) => 
    api.post<Record<string, any>>('/items/draft', data),
  
  getSimilarItems: (id: string) => 
    api.get<Array<Record<string, any>>>(`/items/${id}/similar`)
}
