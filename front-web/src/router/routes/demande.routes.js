import { ROLES } from '@/constants/roles'
import { ROUTE_NAMES } from '@/constants/routeNames'

export default [
  {
    path: 'demandes',
    name: ROUTE_NAMES.DEMANDES,
    component: () => import('@/views/demande/DemandeListView.vue'),
    meta: { title: 'Mes demandes', roles: [ROLES.MANAGER], navigationGroup: 'demandes' },
  },
  {
    path: 'demandes/nouvelle',
    name: ROUTE_NAMES.DEMANDE_CREATE,
    component: () => import('@/views/demande/DemandeFormView.vue'),
    meta: {
      title: 'Nouvelle demande',
      roles: [ROLES.MANAGER],
      navigationGroup: 'demandes',
    },
  },
  {
    path: 'demandes/:id/modifier',
    name: ROUTE_NAMES.DEMANDE_EDIT,
    component: () => import('@/views/demande/DemandeFormView.vue'),
    props: true,
    meta: {
      title: 'Modifier la demande',
      roles: [ROLES.MANAGER],
      navigationGroup: 'demandes',
    },
  },
  {
    path: 'demandes/:id',
    name: ROUTE_NAMES.DEMANDE_DETAIL,
    component: () => import('@/views/demande/DemandeDetailView.vue'),
    props: true,
    meta: {
      title: 'Détail de la demande',
      roles: [ROLES.MANAGER, ROLES.RH, ROLES.FINANCE, ROLES.DG],
      navigationGroup: 'demandes',
    },
  },
  {
    path: 'validations',
    name: ROUTE_NAMES.VALIDATIONS,
    component: () => import('@/views/demande/MesValidationsView.vue'),
    meta: {
      title: 'Validations en attente',
      roles: [ROLES.RH, ROLES.FINANCE, ROLES.DG],
      navigationGroup: 'validations',
    },
  },
]
