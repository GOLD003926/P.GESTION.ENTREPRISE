import { ROUTE_NAMES } from '@/constants/routeNames'

export default [
  {
    path: '/',
    name: ROUTE_NAMES.DASHBOARD,
    component: () => import('@/views/dashboard/DashboardView.vue'),
    meta: { title: 'Tableau de bord' },
  },
]
