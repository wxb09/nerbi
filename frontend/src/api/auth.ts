import api from './api'

export interface LoginPhoneRequest {
  phone: string
  verifyCode: string
}

export interface LoginPasswordRequest {
  phone: string
  password: string
}

export interface RegisterRequest {
  phone: string
  password: string
  nickname?: string
}

export interface LoginResponse {
  token: string
  user: {
    id: string
    nickname: string
    avatar: string
    communityId: string
    role?: string
  }
}

export interface VerifyCodeResponse {
  success: boolean
  expireSeconds: number
}

export const authApi = {
  loginByPhone: (data: LoginPhoneRequest) => 
    api.post<LoginResponse>('/auth/login/phone', data),
  
  loginByPassword: (data: LoginPasswordRequest) =>
    api.post<LoginResponse>('/auth/login/password', data),
  
  register: (data: RegisterRequest) =>
    api.post<LoginResponse>('/auth/register', data),
  
  sendVerifyCode: (phone: string) => 
    api.post<VerifyCodeResponse>('/auth/verify-code', { phone }),
  
  logout: () => 
    api.post('/auth/logout'),
  
  guest: () => 
    api.get('/auth/guest'),

  login: (data: LoginPhoneRequest) => 
    api.post<LoginResponse>('/auth/login/phone', data),
}
