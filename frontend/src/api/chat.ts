import api from './api'

export interface ChatMessage {
  id: number
  conversationId: number
  senderId: number
  receiverId: number
  content: string
  type: string
  isRead: boolean
  createdAt: string
}

export interface Conversation {
  id: number
  otherUser: {
    id: number
    nickname: string
    avatar: string
    isOnline: boolean
  }
  lastMessage: {
    content: string
    type: string
    createdAt: string
    isRead: boolean
  } | null
  unreadCount: number
}

export interface MessagePage {
  content: ChatMessage[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export const chatApi = {
  getConversations: () => 
    api.get<Conversation[]>('/chat/conversations'),
  
  getOrCreateConversation: (userId: number) => 
    api.post<Conversation>('/chat/conversations', { userId }),
  
  getMessages: (conversationId: number, page = 0, size = 20) => 
    api.get<MessagePage>(`/chat/conversations/${conversationId}/messages`, { 
      params: { page, size } 
    }),
  
  sendMessage: (conversationId: number, content: string, type = 'TEXT') => 
    api.post<ChatMessage>(`/chat/conversations/${conversationId}/messages`, {
      content,
      type
    }),
  
  markAsRead: (conversationId: number) => 
    api.put(`/chat/conversations/${conversationId}/read`),
  
  getUserStatus: (userId: number) => 
    api.get<{ userId: number; isOnline: boolean }>(`/chat/users/${userId}/status`),
  
  getUnreadCount: () => 
    api.get<{ count: number }>('/chat/unread-count')
}
