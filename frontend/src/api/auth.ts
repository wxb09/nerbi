import api from './api'

export interface LoginRequest {
  phone: string
  verifyCode: string
}

export interface LoginResponse {
  token: string
  user: {
    id: string
    nickname: string
    avatar: string
    communityId: string
  }
}

export interface VerifyCodeResponse {
  success: boolean
  expireSeconds: number
}

export const authApi = {
  login: (data: LoginRequest) => 
    api.post<LoginResponse>('/auth/login/phone', data),
  
  sendVerifyCode: (phone: string) => 
    api.post<VerifyCodeResponse>('/auth/verify-code', { phone }),
  
  logout: () => 
    api.post('/auth/logout'),
  
  guest: () => 
    api.get('/auth/guest')
}
