import api from './api'

export const reviewApi = {
  createReview: (data: { 
    borrowId: number
    targetType: 'ITEM' | 'USER'
    ratingTag?: string
    ratingStar?: number
    content: string 
  }) =>
    api.post('/reviews', data),

  getReviewById: (id: number) =>
    api.get(`/reviews/${id}`),

  getItemReviews: (itemId: number, page = 0, size = 10) =>
    api.get(`/reviews/item/${itemId}`, { params: { page, size } }),

  getUserReceivedReviews: (userId: number, type?: string, page = 0, size = 10) =>
    api.get(`/reviews/user/${userId}/received`, { params: { type, page, size } }),

  getUserGivenReviews: (userId: number, type?: string, page = 0, size = 10) =>
    api.get(`/reviews/user/${userId}/given`, { params: { type, page, size } }),

  checkReviewStatus: (borrowId: number) =>
    api.get(`/reviews/borrow/${borrowId}/check`),

  deleteReview: (id: number) =>
    api.delete(`/reviews/${id}`)
}

export const RATING_TAGS = [
  { value: 'AS_DESCRIBED', label: '与描述相符', icon: '✓' },
  { value: 'BETTER_THAN_DESCRIBED', label: '比描述更好', icon: '★' },
  { value: 'SLIGHT_WEAR', label: '轻微磨损', icon: '◐' },
  { value: 'NOTICEABLE_WEAR', label: '明显磨损', icon: '◑' },
  { value: 'NOT_AS_DESCRIBED', label: '与描述不符', icon: '✗' }
] as const

export type RatingTagValue = typeof RATING_TAGS[number]['value']
