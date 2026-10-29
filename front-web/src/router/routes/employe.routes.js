import { ROLES } from '@/constants/roles'
import { ROUTE_NAMES } from '@/constants/routeNames'

const EMPLOYEE_READ_ROLES = [ROLES.RH, ROLES.MANAGER, ROLES.PAIE]

export default [
  {
    path: 'employes',
    name: ROUTE_NAMES.EMPLOYES,
    component: () => import('@/views/employe/EmployeListView.vue'),
    meta: { title: 'Employés', roles: EMPLOYEE_READ_ROLES, navigationGroup: 'employes' },
  },
  {
    path: 'employes/nouveau',
    name: ROUTE_NAMES.EMPLOYE_CREATE,
    component: () => import('@/views/employe/EmployeFormView.vue'),
    meta: { title: 'Créer un employé', roles: [ROLES.RH], navigationGroup: 'employes' },
  },
  {
    path: 'employes/:id/modifier',
    name: ROUTE_NAMES.EMPLOYE_EDIT,
    component: () => import('@/views/employe/EmployeFormView.vue'),
    props: true,
    meta: { title: 'Modifier un employé', roles: [ROLES.RH], navigationGroup: 'employes' },
  },
  {
    path: 'employes/:id',
    name: ROUTE_NAMES.EMPLOYE_DETAIL,
    component: () => import('@/views/employe/EmployeDetailView.vue'),
    props: true,
    meta: { title: 'Dossier employé', roles: EMPLOYEE_READ_ROLES, navigationGroup: 'employes' },
  },
  {
    path: 'mon-dossier',
    name: ROUTE_NAMES.MON_DOSSIER,
    component: () => import('@/views/employe/EmployeDetailView.vue'),
    props: { moi: true },
    meta: { title: 'Mon dossier', navigationGroup: 'employes' },
  },
]
