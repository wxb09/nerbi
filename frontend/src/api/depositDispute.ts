import api from './api'

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

export const depositDisputeApi = {
  createDispute: (data: {
    borrowId: number
    disputeType: string
    description: string
    evidenceImages?: string
    claimAmount: number
    claimReason?: string
  }) => api.post<DepositDispute>('/deposit-disputes', data),

  getDispute: (id: number) => api.get<DepositDispute>(`/deposit-disputes/${id}`),

  getMyDisputes: (params?: { page?: number; size?: number }) =>
    api.get<PageResult<DepositDispute>>('/deposit-disputes/my', { params }),

  cancelDispute: (id: number) => api.post(`/deposit-disputes/${id}/cancel`),
}
