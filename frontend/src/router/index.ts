import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

import LoginView from '../views/LoginView.vue'
import IndexView from '../views/IndexView.vue'
import DetailView from '../views/DetailView.vue'
import ForumView from '../views/ForumView.vue'
import PostDetailView from '../views/PostDetailView.vue'
import PublishView from '../views/PublishView.vue'
import ProfileView from '../views/ProfileView.vue'
import DraftsView from '../views/DraftsView.vue'
import MessageView from '../views/MessageView.vue'
import AdminView from '../views/AdminView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/index' },
    { path: '/login', component: LoginView },
    { path: '/index', component: IndexView },
    { path: '/item/:id', component: DetailView },
    { path: '/forum', component: ForumView },
    { path: '/forum/post/:id', component: PostDetailView },
    { path: '/publish', component: PublishView, meta: { requiresAuth: true } },
    { path: '/publish/:id', component: PublishView, meta: { requiresAuth: true } },
    { path: '/drafts', component: DraftsView, meta: { requiresAuth: true } },
    { path: '/profile', component: ProfileView, meta: { requiresAuth: true } },
    { path: '/messages', component: MessageView, meta: { requiresAuth: true } },
    { path: '/admin', component: AdminView, meta: { requiresAuth: true, requiresAdmin: true } },
  ],
})

// 导航守卫
router.beforeEach((to, _from) => {
  const authStore = useAuthStore()
  
  if (to.meta.requiresAuth) {
    if (!authStore.isLoggedIn) {
      return '/login'
    }
  }
  if (to.meta.requiresAdmin) {
    if (authStore.user?.role !== 'ADMIN') {
      return '/index'
    }
  }
  return true
})

export default router
