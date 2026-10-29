import { ROUTE_NAMES } from '@/constants/routeNames'
import organisationRoutes from './organisation.routes.js'
import demandeRoutes from './demande.routes.js'
import recrutementRoutes from './recrutement.routes.js'
import employeRoutes from './employe.routes.js'
import contratRoutes from './contrat.routes.js'
import onboardingRoutes from './onboarding.routes.js'
import embaucheRoutes from './embauche.routes.js'

export default [
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
      {
        path: '',
        name: ROUTE_NAMES.DASHBOARD,
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: { title: 'Tableau de bord' },
      },
      {
        path: '403',
        name: ROUTE_NAMES.FORBIDDEN,
        component: () => import('@/views/errors/ForbiddenView.vue'),
        meta: { title: 'Accès refusé' },
      },
      {
        path: '404',
        name: ROUTE_NAMES.NOT_FOUND,
        component: () => import('@/views/errors/NotFoundView.vue'),
        meta: { title: 'Page introuvable' },
      },
      ...organisationRoutes,
      ...demandeRoutes,
      ...recrutementRoutes,
      ...employeRoutes,
      ...contratRoutes,
      ...onboardingRoutes,
      ...embaucheRoutes,
      {
        path: ':pathMatch(.*)*',
        component: () => import('@/views/errors/NotFoundView.vue'),
        meta: { title: 'Page introuvable' },
      },
    ],
  },
]
