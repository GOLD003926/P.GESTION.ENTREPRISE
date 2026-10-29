import { ROLES } from '@/constants/roles'
import { ROUTE_NAMES } from '@/constants/routeNames'

const READ_ROLES = [ROLES.RH, ROLES.MANAGER, ROLES.FINANCE, ROLES.DG, ROLES.ADMIN]
const HR_ROLES = [ROLES.RH, ROLES.ADMIN]

export default [
  {
    path: 'organisation/departements',
    name: ROUTE_NAMES.DEPARTEMENTS,
    component: () => import('@/views/organisation/DepartementListView.vue'),
    meta: { title: 'Départements', roles: READ_ROLES },
  },
  {
    path: 'organisation/departements/nouveau',
    name: ROUTE_NAMES.DEPARTEMENT_CREATE,
    component: () => import('@/views/organisation/DepartementFormView.vue'),
    meta: { title: 'Nouveau département', roles: HR_ROLES },
  },
  {
    path: 'organisation/departements/:id/modifier',
    name: ROUTE_NAMES.DEPARTEMENT_EDIT,
    component: () => import('@/views/organisation/DepartementFormView.vue'),
    props: true,
    meta: { title: 'Modifier un département', roles: HR_ROLES },
  },
  {
    path: 'organisation/postes',
    name: ROUTE_NAMES.POSTES,
    component: () => import('@/views/organisation/PosteListView.vue'),
    meta: { title: 'Postes', roles: READ_ROLES },
  },
  {
    path: 'organisation/postes/nouveau',
    name: ROUTE_NAMES.POSTE_CREATE,
    component: () => import('@/views/organisation/PosteFormView.vue'),
    meta: { title: 'Nouveau poste', roles: [ROLES.RH] },
  },
  {
    path: 'organisation/postes/:id/modifier',
    name: ROUTE_NAMES.POSTE_EDIT,
    component: () => import('@/views/organisation/PosteFormView.vue'),
    props: true,
    meta: { title: 'Modifier un poste', roles: [ROLES.RH] },
  },
  {
    path: 'organisation/grilles-salariales',
    name: ROUTE_NAMES.GRILLES_SALARIALES,
    component: () => import('@/views/organisation/GrilleSalarialeListView.vue'),
    meta: { title: 'Grilles salariales', roles: READ_ROLES },
  },
  {
    path: 'organisation/grilles-salariales/nouveau',
    name: ROUTE_NAMES.GRILLE_SALARIALE_CREATE,
    component: () => import('@/views/organisation/GrilleSalarialeFormView.vue'),
    meta: { title: 'Nouvelle grille salariale', roles: HR_ROLES },
  },
  {
    path: 'organisation/grilles-salariales/:id/modifier',
    name: ROUTE_NAMES.GRILLE_SALARIALE_EDIT,
    component: () => import('@/views/organisation/GrilleSalarialeFormView.vue'),
    props: true,
    meta: { title: 'Modifier une grille salariale', roles: HR_ROLES },
  },
]
