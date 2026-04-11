import { defineStore } from 'pinia'
import { wsManager } from '../utils/websocket'

interface User {
  id: string
  nickname: string
  avatar: string
  communityId: string
  role: string
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null as User | null,
    token: localStorage.getItem('token') || null
  }),
  
  getters: {
    isLoggedIn: (state) => !!state.token
  },
  
  actions: {
    login(user: User, token: string) {
      this.user = user
      this.token = token
      localStorage.setItem('user', JSON.stringify(user))
      localStorage.setItem('token', token)
      
      wsManager.connect(token).catch(err => {
        console.error('WebSocket 连接失败', err)
      })
    },
    
    logout() {
      wsManager.disconnect()
      this.user = null
      this.token = null
      localStorage.removeItem('user')
      localStorage.removeItem('token')
    },
    
    init() {
      const userStr = localStorage.getItem('user')
      const token = localStorage.getItem('token')
      
      if (token) {
        this.token = token
      }
      
      if (userStr && userStr !== 'undefined' && userStr !== 'null') {
        try {
          this.user = JSON.parse(userStr)
        } catch (error) {
          console.error('解析用户信息失败', error)
          localStorage.removeItem('user')
          localStorage.removeItem('token')
          this.user = null
          this.token = null
        }
      }
      
      if (this.token) {
        wsManager.connect(this.token).catch(err => {
          console.error('WebSocket 连接失败', err)
        })
      }
    }
  }
})
