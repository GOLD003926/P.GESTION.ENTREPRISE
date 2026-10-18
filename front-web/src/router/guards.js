import { ROUTE_NAMES } from '@/constants/routeNames'
import { useAuthStore } from '@/stores/auth'

export function installGuards(router) {
  router.beforeEach(async (to) => {
    const auth = useAuthStore()

    // 1. Pas de session valide : seules les pages publiques sont accessibles.
    if (!auth.hasValidSession()) {
      if (to.meta.public) return true
      return { name: ROUTE_NAMES.LOGIN, query: { redirect: to.fullPath } }
    }

    // 2. Page réservée aux visiteurs (login) : un utilisateur connecté n'y retourne pas.
    if (to.meta.guestOnly) return { name: ROUTE_NAMES.DASHBOARD }

    // 3. Premier passage depuis le chargement de la page : on confirme la session auprès de l'API.
    if (!auth.verified) {
      try {
        await auth.refreshProfile()
      } catch (error) {
        // 401 (jeton refusé) ou 403 (compte désactivé) : la session est terminée.
        if (error.status === 401 || error.status === 403) {
          auth.logout(true)
          return { name: ROUTE_NAMES.LOGIN, query: { redirect: to.fullPath } }
        }
        // Serveur injoignable ou autre erreur : on laisse passer, la session sera revérifiée plus tard.
      }
    }

    // 4. Pages réservées à certains rôles (utilisé par les modules suivants).
    const allowedRoles = to.meta.roles
    if (allowedRoles && !allowedRoles.includes(auth.role)) {
      return { name: ROUTE_NAMES.DASHBOARD }
    }

    return true
  })

  router.afterEach((to) => {
    document.title = to.meta.title ? `${to.meta.title} · SIRH` : 'SIRH'
  })
}
