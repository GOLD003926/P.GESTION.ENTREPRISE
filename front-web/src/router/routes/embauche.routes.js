import { ROLES } from '@/constants/roles'
import { ROUTE_NAMES } from '@/constants/routeNames'

const PROPOSITION_READ_ROLES = [ROLES.RH, ROLES.MANAGER, ROLES.DG]

export default [
  {
    path: 'propositions',
    name: ROUTE_NAMES.PROPOSITIONS,
    component: () => import('@/views/embauche/PropositionListView.vue'),
    meta: {
      title: 'Propositions',
      roles: PROPOSITION_READ_ROLES,
      navigationGroup: 'embauche',
    },
  },
  {
    path: 'propositions/nouvelle',
    name: ROUTE_NAMES.PROPOSITION_CREATE,
    component: () => import('@/views/embauche/PropositionFormView.vue'),
    meta: { title: 'Nouvelle proposition', roles: [ROLES.RH], navigationGroup: 'embauche' },
  },
  {
    path: 'propositions/:id/modifier',
    name: ROUTE_NAMES.PROPOSITION_EDIT,
    component: () => import('@/views/embauche/PropositionFormView.vue'),
    props: true,
    meta: { title: 'Modifier la proposition', roles: [ROLES.RH], navigationGroup: 'embauche' },
  },
  {
    path: 'propositions/:id',
    name: ROUTE_NAMES.PROPOSITION_DETAIL,
    component: () => import('@/views/embauche/PropositionDetailView.vue'),
    props: true,
    meta: {
      title: 'Détail de la proposition',
      roles: PROPOSITION_READ_ROLES,
      navigationGroup: 'embauche',
    },
  },
]
