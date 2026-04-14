import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [vue(), tailwindcss()],
  server: {
    //host: '0.0.0.0'
    proxy: {
      '/uploads': {
        target: 'http://localhost:8080',
        //target: 'http://192.168.73.181:8080',
        changeOrigin: true
      }
    }
  }
})
