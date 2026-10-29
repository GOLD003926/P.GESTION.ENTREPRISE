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
    routeName: ROUTE_NAMES.DEPARTEMENTS,
    roles: [ROLES.ADMIN, ROLES.RH, ROLES.MANAGER, ROLES.FINANCE, ROLES.DG],
    enabled: true,
    children: [
      { label: 'Départements', routeName: ROUTE_NAMES.DEPARTEMENTS },
      { label: 'Postes', routeName: ROUTE_NAMES.POSTES },
      { label: 'Grilles salariales', routeName: ROUTE_NAMES.GRILLES_SALARIALES },
    ],
  },
  {
    id: 'demandes',
    label: 'Mes demandes',
    icon: 'requests',
    routeName: ROUTE_NAMES.DEMANDES,
    roles: [ROLES.MANAGER],
    enabled: true,
  },
  {
    id: 'validations',
    label: 'Validations',
    icon: 'requests',
    routeName: ROUTE_NAMES.VALIDATIONS,
    roles: [ROLES.RH, ROLES.FINANCE, ROLES.DG],
    enabled: true,
  },
  {
    id: 'recrutement',
    label: 'Recrutement',
    icon: 'recruitment',
    routeName: ROUTE_NAMES.OFFRES,
    roles: [ROLES.RH, ROLES.MANAGER, ROLES.DG],
    enabled: true,
    children: [
      {
        label: 'Offres',
        routeName: ROUTE_NAMES.OFFRES,
        roles: [ROLES.RH, ROLES.MANAGER, ROLES.DG],
      },
      {
        label: 'Candidatures',
        routeName: ROUTE_NAMES.CANDIDATURES,
        roles: [ROLES.RH, ROLES.MANAGER],
      },
      { label: 'Candidats', routeName: ROUTE_NAMES.CANDIDATS, roles: [ROLES.RH] },
      { label: 'Entretiens', routeName: ROUTE_NAMES.ENTRETIENS, roles: [ROLES.RH, ROLES.MANAGER] },
    ],
  },
  {
    id: 'employes',
    label: 'Dossiers employés',
    icon: 'employees',
    routeName: ROUTE_NAMES.MON_DOSSIER,
    roles: ALL_ROLES,
    enabled: true,
    children: [
      {
        label: 'Employés',
        routeName: ROUTE_NAMES.EMPLOYES,
        roles: [ROLES.RH, ROLES.MANAGER, ROLES.PAIE],
      },
      { label: 'Mon dossier', routeName: ROUTE_NAMES.MON_DOSSIER, roles: ALL_ROLES },
    ],
  },
  {
    id: 'contrats',
    label: 'Contrats',
    icon: 'contracts',
    routeName: ROUTE_NAMES.CONTRATS,
    roles: [ROLES.RH, ROLES.MANAGER, ROLES.PAIE],
    enabled: true,
    children: [
      { label: 'Tous les contrats', routeName: ROUTE_NAMES.CONTRATS },
      { label: 'Mes contrats', routeName: ROUTE_NAMES.MES_CONTRATS, roles: ALL_ROLES },
    ],
  },
  {
    id: 'mes-contrats',
    label: 'Mes contrats',
    icon: 'contracts',
    routeName: ROUTE_NAMES.MES_CONTRATS,
    roles: [ROLES.SALARIE],
    enabled: true,
  },
  {
    id: 'onboarding',
    label: 'Onboarding',
    icon: 'onboarding',
    routeName: ROUTE_NAMES.ONBOARDINGS,
    roles: [ROLES.RH, ROLES.MANAGER, ROLES.IT_SERVICES, ROLES.PAIE, ROLES.SALARIE],
    enabled: true,
    children: [
      { label: 'Onboardings', routeName: ROUTE_NAMES.ONBOARDINGS, roles: [ROLES.RH, ROLES.MANAGER] },
      { label: 'Mes tâches', routeName: ROUTE_NAMES.MES_TACHES, roles: [ROLES.RH, ROLES.MANAGER, ROLES.IT_SERVICES, ROLES.PAIE] },
      { label: 'Mon onboarding', routeName: ROUTE_NAMES.MON_ONBOARDING, roles: [ROLES.SALARIE] },
      { label: 'Modèles', routeName: ROUTE_NAMES.MODELES_ONBOARDING, roles: [ROLES.RH] },
    ],
  },
  {
    id: 'embauche',
    label: 'Propositions',
    icon: 'hiring',
    routeName: ROUTE_NAMES.PROPOSITIONS,
    roles: [ROLES.RH, ROLES.MANAGER, ROLES.DG],
    enabled: true,
  },
])
