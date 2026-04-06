import { useAuthStore } from '../stores/auth'

export interface WebSocketMessage {
  type: string
  data: any
}

type MessageHandler = (message: WebSocketMessage) => void

class WebSocketManager {
  private ws: WebSocket | null = null
  private reconnectTimer: number | null = null
  private heartbeatTimer: number | null = null
  private handlers: Map<string, MessageHandler[]> = new Map()
  private isConnecting = false
  private reconnectAttempts = 0
  private maxReconnectAttempts = 5
  private reconnectDelay = 3000

  connect(token: string): Promise<void> {
    return new Promise((resolve, reject) => {
      if (this.ws?.readyState === WebSocket.OPEN) {
        resolve()
        return
      }

      if (this.isConnecting) {
        resolve()
        return
      }

      this.isConnecting = true
      const wsUrl = `ws://localhost:8080/ws/message?token=${token}`
      
      try {
        this.ws = new WebSocket(wsUrl)

        this.ws.onopen = () => {
          console.log('[WebSocket] 连接成功')
          this.isConnecting = false
          this.reconnectAttempts = 0
          this.startHeartbeat()
          resolve()
        }

        this.ws.onmessage = (event) => {
          try {
            const message: WebSocketMessage = JSON.parse(event.data)
            console.log('[WebSocket] 收到消息:', message)
            this.handleMessage(message)
          } catch (error) {
            console.error('[WebSocket] 解析消息失败:', error)
          }
        }

        this.ws.onerror = (error) => {
          console.error('[WebSocket] 连接错误:', error)
          this.isConnecting = false
          reject(error)
        }

        this.ws.onclose = (event) => {
          console.log('[WebSocket] 连接关闭:', event.reason)
          this.isConnecting = false
          this.stopHeartbeat()
          this.scheduleReconnect()
        }
      } catch (error) {
        this.isConnecting = false
        reject(error)
      }
    })
  }

  disconnect() {
    this.stopHeartbeat()
    this.stopReconnect()
    
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
    
    console.log('[WebSocket] 已断开连接')
  }

  isConnected(): boolean {
    return this.ws?.readyState === WebSocket.OPEN
  }

  send(message: any): boolean {
    if (!this.isConnected()) {
      console.warn('[WebSocket] 未连接，无法发送消息')
      return false
    }

    try {
      this.ws!.send(JSON.stringify(message))
      return true
    } catch (error) {
      console.error('[WebSocket] 发送消息失败:', error)
      return false
    }
  }

  on(type: string, handler: MessageHandler) {
    if (!this.handlers.has(type)) {
      this.handlers.set(type, [])
    }
    this.handlers.get(type)!.push(handler)
  }

  off(type: string, handler: MessageHandler) {
    const handlers = this.handlers.get(type)
    if (handlers) {
      const index = handlers.indexOf(handler)
      if (index > -1) {
        handlers.splice(index, 1)
      }
    }
  }

  private handleMessage(message: WebSocketMessage) {
    const handlers = this.handlers.get(message.type)
    if (handlers) {
      handlers.forEach(handler => handler(message))
    }
    
    const allHandlers = this.handlers.get('*')
    if (allHandlers) {
      allHandlers.forEach(handler => handler(message))
    }
  }

  private startHeartbeat() {
    this.stopHeartbeat()
    this.heartbeatTimer = window.setInterval(() => {
      if (this.isConnected()) {
        this.send({ type: 'PING' })
      }
    }, 30000)
  }

  private stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
  }

  private scheduleReconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.log('[WebSocket] 达到最大重连次数，停止重连')
      return
    }

    this.stopReconnect()
    this.reconnectTimer = window.setTimeout(() => {
      this.reconnectAttempts++
      console.log(`[WebSocket] 尝试重连 (${this.reconnectAttempts}/${this.maxReconnectAttempts})`)
      
      const authStore = useAuthStore()
      if (authStore.token) {
        this.connect(authStore.token)
      }
    }, this.reconnectDelay)
  }

  private stopReconnect() {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
  }
}

export const wsManager = new WebSocketManager()
