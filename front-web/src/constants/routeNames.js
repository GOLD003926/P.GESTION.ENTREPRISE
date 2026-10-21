// Noms de routes : on navigue toujours par nom (router.push({ name: ROUTE_NAMES.X })), jamais par chemin écrit en dur.
export const ROUTE_NAMES = Object.freeze({
  LOGIN: 'login',
  DASHBOARD: 'dashboard',
  FORBIDDEN: 'forbidden',
  NOT_FOUND: 'not-found',
})
