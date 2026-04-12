import api from './api'

export interface UserStats {
  pendingApprovalCount: number
  returnRequestedCount: number
  dueSoonCount: number
  todayCo2Saved: number
}

export const userApi = {
  getUser: () => api.get('/users/me'),
  
  updateUser: (data: Record<string, any>) => api.put('/users/me', data),
  
  getUserStats: () => api.get<UserStats>('/users/me/stats'),
  
  submitAddressVerify: (data: { communityId: number; building?: string; unit?: string }) =>
    api.post('/users/me/address-verify', data),
  
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
  
  getMyGivenReviews: () => 
    api.get('/users/me/reviews/given'),
  
  getMyDrafts: () => 
    api.get('/users/me/drafts'),
}
