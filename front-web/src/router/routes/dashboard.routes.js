import { ROUTE_NAMES } from '@/constants/routeNames'

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
      {
        path: ':pathMatch(.*)*',
        component: () => import('@/views/errors/NotFoundView.vue'),
        meta: { title: 'Page introuvable' },
      },
    ],
  },
]
