import api from './api'

export const userApi = {
  getCurrentUser: () => 
    api.get('/users/me'),
  
  updateUser: (data: Record<string, any>) => 
    api.put('/users/me', data),
  
  getUserProfile: (id: string) => 
    api.get(`/users/${id}/profile`),
  
  getMyItems: () => 
    api.get('/users/me/items'),
  
  getMyLent: () => 
    api.get('/users/me/lent'),
  
  getMyBorrowed: () => 
    api.get('/users/me/borrowed'),
  
  getMyPending: () => 
    api.get('/users/me/pending'),
  
  getMyReviews: () => 
    api.get('/users/me/reviews'),
  
  getMyDrafts: () => 
    api.get('/users/me/drafts'),
  
  getUserStats: () => 
    api.get('/users/me/stats')
}
