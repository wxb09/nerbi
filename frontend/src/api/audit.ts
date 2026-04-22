import api from './api'

export interface SensitiveWord {
  id: number
  word: string
  category: string
  severity: number
  status: number
  createdAt: string
}

export interface CreateSensitiveWordRequest {
  word: string
  category?: string
  severity?: number
}

export const auditApi = {
  getAuditStatus: () => api.get('/admin/audit/status'),

  setAuditStatus: (enabled: boolean) => api.post(`/admin/audit/status?enabled=${enabled}`),

  getWords: (params: {
    keyword?: string
    category?: string
    page?: number
    size?: number
  }) => api.get('/admin/audit/words', { params }),

  addWord: (data: CreateSensitiveWordRequest) => api.post('/admin/audit/words', data),

  deleteWord: (id: number) => api.delete(`/admin/audit/words/${id}`),

  toggleWordStatus: (id: number) => api.put(`/admin/audit/words/${id}/toggle`),

  getCategories: () => api.get('/admin/audit/categories'),

  refreshWordBuffer: () => api.post('/admin/audit/refresh'),
}
