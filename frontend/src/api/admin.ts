import api from './api'

export interface AdminStats {
  totalUsers: number
  totalItems: number
  totalBorrows: number
  pendingReviewItems: number
  activeBorrows: number
  pendingDisputes: number
  bannedUsers: number
  thisMonthUsers: number
  thisMonthBorrows: number
}

export interface AdminUser {
  id: number
  nickname: string
  avatar: string
  phone: string
  communityName: string
  building: string
  creditScore: number
  borrowCount: number
  lendCount: number
  co2Saved: number
  status: string
  role: string
  createdAt: string
}

export interface AdminItem {
  id: number
  name: string
  mainImage: string | null
  status: string
  categoryName: string | null
  pricePerDay: number
  ownerNickname: string | null
  ownerId: number | null
  auditRemark: string | null
  createdAt: string
}

export interface AdminBorrow {
  id: number
  itemName: string | null
  itemId: number | null
  borrowerNickname: string | null
  borrowerId: number | null
  lenderNickname: string | null
  lenderId: number | null
  startDate: string | null
  endDate: string | null
  status: string
  purpose: string | null
  createdAt: string
}

export interface Dispute {
  id: number
  borrowId: number
  itemName: string | null
  reporterNickname: string | null
  reporterId: number | null
  reason: string
  status: string
  resolution: string | null
  resolvedByName: string | null
  createdAt: string
  resolvedAt: string | null
}

export interface DepositDispute {
  id: number
  paymentId: number
  borrowId: number
  itemName: string | null
  initiatorId: number
  initiatorName: string
  initiatorType: 'LENDER' | 'BORROWER'
  disputeType: 'DAMAGE' | 'LOSS' | 'OVERDUE' | 'MISSING_PARTS' | 'OTHER'
  description: string
  evidenceImages: string | null
  claimAmount: number
  claimReason: string | null
  status: 'PENDING' | 'PROCESSING' | 'APPROVED' | 'REJECTED' | 'CANCELLED'
  actualDeduction: number | null
  resolution: string | null
  handlerId: number | null
  handlerName: string | null
  handledAt: string | null
  depositAmount: number
  createdAt: string
}

export interface PageResult<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export interface Announcement {
  id: number
  title: string
  content: string
  type: string
  communityId: number | null
  communityName: string | null
  createdAt: string
}

export const adminApi = {
  getStats: () => api.get<AdminStats>('/admin/stats'),

  getUsers: (params: { keyword?: string; status?: string; role?: string; page?: number; size?: number }) =>
    api.get<PageResult<AdminUser>>('/admin/users', { params }),

  banUser: (id: number) => api.post(`/admin/users/${id}/ban`),

  unbanUser: (id: number) => api.post(`/admin/users/${id}/unban`),

  getPendingReviewItems: (params: { page?: number; size?: number }) =>
    api.get<PageResult<AdminItem>>('/admin/items/pending-review', { params }),

  getAllItems: (params: { status?: string; page?: number; size?: number }) =>
    api.get<PageResult<AdminItem>>('/admin/items', { params }),

  auditItem: (id: number, data: { action: string; remark?: string }) =>
    api.post(`/admin/items/${id}/audit`, data),

  getBorrows: (params: { status?: string; page?: number; size?: number }) =>
    api.get<PageResult<AdminBorrow>>('/admin/borrows', { params }),

  getDisputes: (params: { status?: string; page?: number; size?: number }) =>
    api.get<PageResult<Dispute>>('/admin/disputes', { params }),

  resolveDispute: (id: number, data: { action: string; resolution?: string }) =>
    api.post(`/admin/disputes/${id}/resolve`, data),

  createDispute: (borrowId: number, reason: string) =>
    api.post('/admin/disputes', { borrowId, reason }),

  getPendingAddressVerifies: () =>
    api.get<any[]>('/admin/address-verifies'),

  approveAddressVerify: (userId: number, approved: boolean) =>
    api.post(`/admin/address-verifies/${userId}?approved=${approved}`),

  getDepositDisputes: (params: { status?: string; page?: number; size?: number }) =>
    api.get<PageResult<DepositDispute>>('/admin/deposit-disputes', { params }),

  getDepositDispute: (id: number) =>
    api.get<DepositDispute>(`/admin/deposit-disputes/${id}`),

  resolveDepositDispute: (id: number, data: { action: string; actualDeduction?: number; resolution?: string }) =>
    api.post<DepositDispute>(`/admin/deposit-disputes/${id}/resolve`, data),

  getDepositDisputeStats: () =>
    api.get<{ pendingCount: number; processingCount: number; approvedCount: number; rejectedCount: number }>('/admin/deposit-disputes/stats'),

  getAnnouncements: (params: { page?: number; size?: number }) =>
    api.get<PageResult<Announcement>>('/admin/announcements', { params }),

  createAnnouncement: (data: { title: string; content: string; type?: string; communityId?: number }) =>
    api.post<Announcement>('/admin/announcements', data),

  updateAnnouncement: (id: number, data: { title?: string; content?: string; type?: string }) =>
    api.put<Announcement>(`/admin/announcements/${id}`, data),

  deleteAnnouncement: (id: number) =>
    api.delete(`/admin/announcements/${id}`),

  getReports: (params: { status?: string; page?: number; size?: number }) =>
    api.get<PageResult<any>>('/admin/reports', { params }),

  resolveReport: (id: number, data: { action: string; result: string }) =>
    api.post(`/admin/reports/${id}/resolve`, data),
}
