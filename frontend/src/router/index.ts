import { createRouter, createWebHistory } from 'vue-router'
import Sessions from '../views/Sessions.vue'
import Messages from '../views/Messages.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/sessions' },
    { path: '/sessions', component: Sessions },
    { path: '/messages', component: Messages }
  ]
})

export default router
