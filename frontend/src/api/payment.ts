import api from './api'

export const paymentApi = {
  createPayment: (borrowId: number) =>
    api.post('/payments/create', { borrowId }),

  getPaymentByBorrowId: (borrowId: number) =>
    api.get(`/payments/borrow/${borrowId}`),

  getMyPayments: () =>
    api.get('/payments/my/paid'),

  getReceivedPayments: () =>
    api.get('/payments/my/received'),

  refundDeposit: (borrowId: number) =>
    api.post(`/payments/${borrowId}/refund`),

  skipPayment: (borrowId: number) =>
    api.post(`/payments/${borrowId}/skip-pay`),

  skipRefund: (borrowId: number) =>
    api.post(`/payments/${borrowId}/skip-refund`)
}
