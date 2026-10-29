import { ROLES } from '@/constants/roles'
import { ROUTE_NAMES } from '@/constants/routeNames'

const ONBOARDING_READ_ROLES = [ROLES.RH, ROLES.MANAGER]
const ONBOARDING_TASK_ROLES = [ROLES.RH, ROLES.MANAGER, ROLES.IT_SERVICES, ROLES.PAIE]

export default [
  {
    path: 'onboardings',
    name: ROUTE_NAMES.ONBOARDINGS,
    component: () => import('@/views/onboarding/OnboardingListView.vue'),
    meta: { title: 'Onboardings', roles: ONBOARDING_READ_ROLES, navigationGroup: 'onboarding' },
  },
  {
    path: 'mon-onboarding',
    name: ROUTE_NAMES.MON_ONBOARDING,
    component: () => import('@/views/onboarding/OnboardingDetailView.vue'),
    props: { mode: 'self' },
    meta: { title: 'Mon onboarding', roles: [ROLES.SALARIE], navigationGroup: 'onboarding' },
  },
  {
    path: 'onboardings/:id',
    name: ROUTE_NAMES.ONBOARDING_DETAIL,
    component: () => import('@/views/onboarding/OnboardingDetailView.vue'),
    props: true,
    meta: { title: 'Détail onboarding', roles: ONBOARDING_READ_ROLES, navigationGroup: 'onboarding' },
  },
  {
    path: 'mes-taches',
    name: ROUTE_NAMES.MES_TACHES,
    component: () => import('@/views/onboarding/MesTachesView.vue'),
    meta: { title: 'Mes tâches', roles: ONBOARDING_TASK_ROLES, navigationGroup: 'onboarding' },
  },
  {
    path: 'modeles-onboarding',
    name: ROUTE_NAMES.MODELES_ONBOARDING,
    component: () => import('@/views/onboarding/ModeleOnboardingListView.vue'),
    meta: { title: 'Modèles onboarding', roles: [ROLES.RH], navigationGroup: 'onboarding' },
  },
  {
    path: 'modeles-onboarding/nouveau',
    name: ROUTE_NAMES.MODELE_ONBOARDING_CREATE,
    component: () => import('@/views/onboarding/ModeleOnboardingFormView.vue'),
    meta: { title: 'Nouveau modèle', roles: [ROLES.RH], navigationGroup: 'onboarding' },
  },
  {
    path: 'modeles-onboarding/:id/modifier',
    name: ROUTE_NAMES.MODELE_ONBOARDING_EDIT,
    component: () => import('@/views/onboarding/ModeleOnboardingFormView.vue'),
    props: true,
    meta: { title: 'Modifier le modèle', roles: [ROLES.RH], navigationGroup: 'onboarding' },
  },
]
