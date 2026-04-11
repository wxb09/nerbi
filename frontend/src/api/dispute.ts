import api from './api'

export const disputeApi = {
  createDispute: (borrowId: number, reason: string) =>
    api.post('/disputes', { borrowId, reason }),
}
