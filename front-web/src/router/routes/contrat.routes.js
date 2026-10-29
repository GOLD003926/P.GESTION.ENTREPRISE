import { ROLES } from '@/constants/roles'
import { ROUTE_NAMES } from '@/constants/routeNames'

const CONTRACT_READ_ROLES = [ROLES.RH, ROLES.MANAGER, ROLES.PAIE]

export default [
  {
    path: 'contrats',
    name: ROUTE_NAMES.CONTRATS,
    component: () => import('@/views/contrat/ContratListView.vue'),
    meta: { title: 'Contrats', roles: CONTRACT_READ_ROLES, navigationGroup: 'contrats' },
  },
  {
    path: 'contrats/nouveau',
    name: ROUTE_NAMES.CONTRAT_CREATE,
    component: () => import('@/views/contrat/ContratFormView.vue'),
    meta: { title: 'Créer un contrat', roles: [ROLES.RH], navigationGroup: 'contrats' },
  },
  {
    path: 'contrats/:id/modifier',
    name: ROUTE_NAMES.CONTRAT_EDIT,
    component: () => import('@/views/contrat/ContratFormView.vue'),
    props: true,
    meta: { title: 'Modifier un contrat', roles: [ROLES.RH], navigationGroup: 'contrats' },
  },
  {
    path: 'contrats/:id',
    name: ROUTE_NAMES.CONTRAT_DETAIL,
    component: () => import('@/views/contrat/ContratDetailView.vue'),
    props: true,
    meta: { title: 'Détail du contrat', roles: CONTRACT_READ_ROLES, navigationGroup: 'contrats' },
  },
  {
    path: 'mes-contrats',
    name: ROUTE_NAMES.MES_CONTRATS,
    component: () => import('@/views/contrat/ContratListView.vue'),
    props: { personnels: true },
    meta: { title: 'Mes contrats', navigationGroup: 'contrats' },
  },
]