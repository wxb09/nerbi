import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { vInfiniteScroll } from './directives/vInfiniteScroll'
import './style.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.directive('infinite-scroll', vInfiniteScroll)
app.mount('#app')
