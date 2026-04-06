import api from './api'

export interface Message {
  id: number
  type: string
  typeDesc: string
  title: string
  content: string
  relatedId: number | null
  isRead: boolean
  createdAt: string
}

export interface MessagePage {
  content: Message[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export const messageApi = {
  getMessages: (page = 0, size = 10) => 
    api.get<MessagePage>('/messages', { params: { page, size } }),
  
  getUnreadMessages: (page = 0, size = 10) => 
    api.get<MessagePage>('/messages/unread', { params: { page, size } }),
  
  getUnreadCount: () => 
    api.get<{ count: number }>('/messages/unread-count'),
  
  markAsRead: (id: number) => 
    api.post(`/messages/${id}/read`),
  
  markAllAsRead: () => 
    api.post('/messages/read-all')
}
