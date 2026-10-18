import { ROUTE_NAMES } from '@/constants/routeNames'

// Convention : un fichier de routes par module, exporté en tableau, assemblé dans router/index.js.
// meta.public     : accessible sans être connecté
// meta.guestOnly  : réservé aux visiteurs (un utilisateur connecté en est renvoyé)
// meta.roles      : (autres modules) liste des rôles autorisés
// meta.title      : titre de l'onglet
export default [
  {
    path: '/login',
    name: ROUTE_NAMES.LOGIN,
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { public: true, guestOnly: true, title: 'Connexion' },
  },
]
