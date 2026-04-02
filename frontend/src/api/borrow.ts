import api from './api'

export const borrowApi = {
  getBorrowById: (id: string | number) => 
    api.get(`/borrows/${id}`),
  
  createBorrow: (data: {
    itemId: number
    startDate: string
    endDate: string
    purpose: string
    agreeTerms: boolean
  }) => 
    api.post('/borrows', data),
  
  approveBorrow: (id: number, data: { approved: boolean; reason?: string }) => 
    api.post(`/borrows/${id}/approve`, data),
  
  confirmPickup: (id: number) => 
    api.post(`/borrows/${id}/pickup`),
  
  confirmReturn: (id: number) => 
    api.post(`/borrows/${id}/return`),
  
  remindReturn: (id: number) => 
    api.post(`/borrows/${id}/remind`),
  
  cancelBorrow: (id: number) => 
    api.post(`/borrows/${id}/cancel`),
  
  applyReturn: (id: number) => 
    api.post(`/borrows/${id}/apply-return`),
  
  getMyBorrowed: (page = 0, size = 10) => 
    api.get(`/borrows/my/borrowed?page=${page}&size=${size}`),
  
  getMyLent: (page = 0, size = 10) => 
    api.get(`/borrows/my/lent?page=${page}&size=${size}`),
  
  getMyPending: () => 
    api.get('/borrows/my/pending')
}
