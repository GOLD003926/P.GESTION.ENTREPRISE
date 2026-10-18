import { createRouter, createWebHistory } from 'vue-router'
import { ROUTE_NAMES } from '@/constants/routeNames'
import { installGuards } from './guards.js'
import authRoutes from './routes/auth.routes.js'
import dashboardRoutes from './routes/dashboard.routes.js'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    ...authRoutes,
    ...dashboardRoutes,
    // Toute adresse inconnue renvoie vers l'accueil (la garde renverra au login si besoin).
    { path: '/:pathMatch(.*)*', redirect: { name: ROUTE_NAMES.DASHBOARD } },
  ],
})

installGuards(router)

export default router
