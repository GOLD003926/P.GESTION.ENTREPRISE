import { ROLES } from './roles'
import { ROUTE_NAMES } from './routeNames'

const ALL_ROLES = Object.values(ROLES)

export const NAVIGATION_ITEMS = Object.freeze([
  {
    id: 'dashboard',
    label: 'Tableau de bord',
    icon: 'dashboard',
    routeName: ROUTE_NAMES.DASHBOARD,
    roles: ALL_ROLES,
    enabled: true,
  },
  {
    id: 'organisation',
    label: 'Organisation',
    icon: 'organization',
    roles: [ROLES.ADMIN, ROLES.RH, ROLES.MANAGER, ROLES.FINANCE, ROLES.DG],
    enabled: false,
  },
  {
    id: 'demande',
    label: 'Demandes et validations',
    icon: 'requests',
    roles: [ROLES.MANAGER, ROLES.RH, ROLES.FINANCE, ROLES.DG],
    enabled: false,
  },
  {
    id: 'recrutement',
    label: 'Recrutement',
    icon: 'recruitment',
    roles: [ROLES.RH, ROLES.MANAGER, ROLES.DG],
    enabled: false,
  },
  {
    id: 'employes',
    label: 'Dossiers employés',
    icon: 'employees',
    roles: [ROLES.RH, ROLES.MANAGER, ROLES.PAIE, ROLES.SALARIE],
    enabled: false,
  },
  {
    id: 'contrats',
    label: 'Contrats',
    icon: 'contracts',
    roles: [ROLES.RH, ROLES.MANAGER, ROLES.PAIE, ROLES.SALARIE],
    enabled: false,
  },
  {
    id: 'onboarding',
    label: 'Onboarding',
    icon: 'onboarding',
    roles: [ROLES.RH, ROLES.MANAGER, ROLES.IT_SERVICES, ROLES.PAIE, ROLES.SALARIE],
    enabled: false,
  },
  {
    id: 'embauche',
    label: 'Propositions',
    icon: 'hiring',
    roles: [ROLES.RH, ROLES.MANAGER, ROLES.DG],
    enabled: false,
  },
])
