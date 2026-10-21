import { createRouter, createWebHistory } from 'vue-router'
import { installGuards } from './guards.js'
import authRoutes from './routes/auth.routes.js'
import dashboardRoutes from './routes/dashboard.routes.js'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [...authRoutes, ...dashboardRoutes],
})

installGuards(router)

export default router
