import { ROLES } from '@/constants/roles'
import { ROUTE_NAMES } from '@/constants/routeNames'

const OFFER_READ_ROLES = [ROLES.RH, ROLES.MANAGER, ROLES.DG]
const RECRUITMENT_ROLES = [ROLES.RH, ROLES.MANAGER]

export default [
  {
    path: 'offres',
    name: ROUTE_NAMES.OFFRES,
    component: () => import('@/views/recrutement/OffreListView.vue'),
    meta: { title: 'Offres d’emploi', roles: OFFER_READ_ROLES, navigationGroup: 'recrutement' },
  },
  {
    path: 'offres/nouvelle',
    name: ROUTE_NAMES.OFFRE_CREATE,
    component: () => import('@/views/recrutement/OffreFormView.vue'),
    meta: { title: 'Nouvelle offre', roles: [ROLES.RH], navigationGroup: 'recrutement' },
  },
  {
    path: 'offres/:id/modifier',
    name: ROUTE_NAMES.OFFRE_EDIT,
    component: () => import('@/views/recrutement/OffreFormView.vue'),
    props: true,
    meta: { title: 'Modifier l’offre', roles: [ROLES.RH], navigationGroup: 'recrutement' },
  },
  {
    path: 'offres/:id',
    name: ROUTE_NAMES.OFFRE_DETAIL,
    component: () => import('@/views/recrutement/OffreDetailView.vue'),
    props: true,
    meta: { title: 'Détail de l’offre', roles: OFFER_READ_ROLES, navigationGroup: 'recrutement' },
  },
  {
    path: 'candidats',
    name: ROUTE_NAMES.CANDIDATS,
    component: () => import('@/views/recrutement/CandidatListView.vue'),
    meta: { title: 'Candidats', roles: [ROLES.RH], navigationGroup: 'recrutement' },
  },
  {
    path: 'candidats/nouveau',
    name: ROUTE_NAMES.CANDIDAT_CREATE,
    component: () => import('@/views/recrutement/CandidatFormView.vue'),
    meta: { title: 'Nouveau candidat', roles: [ROLES.RH], navigationGroup: 'recrutement' },
  },
  {
    path: 'candidats/:id/modifier',
    name: ROUTE_NAMES.CANDIDAT_EDIT,
    component: () => import('@/views/recrutement/CandidatFormView.vue'),
    props: true,
    meta: { title: 'Modifier le candidat', roles: [ROLES.RH], navigationGroup: 'recrutement' },
  },
  {
    path: 'candidats/:id',
    name: ROUTE_NAMES.CANDIDAT_DETAIL,
    component: () => import('@/views/recrutement/CandidatDetailView.vue'),
    props: true,
    meta: { title: 'Détail du candidat', roles: [ROLES.RH], navigationGroup: 'recrutement' },
  },
  {
    path: 'candidatures',
    name: ROUTE_NAMES.CANDIDATURES,
    component: () => import('@/views/recrutement/CandidatureListView.vue'),
    meta: { title: 'Candidatures', roles: RECRUITMENT_ROLES, navigationGroup: 'recrutement' },
  },
  {
    path: 'candidatures/nouvelle',
    name: ROUTE_NAMES.CANDIDATURE_CREATE,
    component: () => import('@/views/recrutement/CandidatureFormView.vue'),
    meta: {
      title: 'Enregistrer une candidature',
      roles: [ROLES.RH],
      navigationGroup: 'recrutement',
    },
  },
  {
    path: 'candidatures/:id',
    name: ROUTE_NAMES.CANDIDATURE_DETAIL,
    component: () => import('@/views/recrutement/CandidatureDetailView.vue'),
    props: true,
    meta: {
      title: 'Détail de la candidature',
      roles: RECRUITMENT_ROLES,
      navigationGroup: 'recrutement',
    },
  },
  {
    path: 'entretiens',
    name: ROUTE_NAMES.ENTRETIENS,
    component: () => import('@/views/recrutement/EntretienListView.vue'),
    meta: { title: 'Entretiens', roles: RECRUITMENT_ROLES, navigationGroup: 'recrutement' },
  },
  {
    path: 'entretiens/:id',
    name: ROUTE_NAMES.ENTRETIEN_DETAIL,
    component: () => import('@/views/recrutement/EntretienDetailView.vue'),
    props: true,
    meta: {
      title: 'Détail de l’entretien',
      roles: RECRUITMENT_ROLES,
      navigationGroup: 'recrutement',
    },
  },
]
