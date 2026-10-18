import axios from 'axios'
import { API_URL } from '@/config/env'
import { STORAGE_KEYS, readJson } from '@/utils/storage'
import { normalizeError } from '@/utils/errors'

/**
 * Le client HTTP UNIQUE de l'application. Aucun composant n'appelle axios directement :
 * tout passe par un fichier de src/api/ (un fichier par contrôleur de l'API).
 */
const http = axios.create({
  baseURL: API_URL,
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
})

// Branché dans main.js. Évite que ce fichier importe le store (dépendance circulaire).
let onUnauthorized = () => {}
export function setUnauthorizedHandler(handler) {
  onUnauthorized = handler
}

// Joint le jeton à chaque requête.
http.interceptors.request.use((config) => {
  const session = readJson(STORAGE_KEYS.AUTH)
  if (session?.token) {
    config.headers.Authorization = `Bearer ${session.token}`
  }
  return config
})

// Toute erreur est normalisée : les appelants reçoivent { status, code, message, fieldErrors }.
http.interceptors.response.use(
  (response) => response,
  (error) => {
    const normalized = normalizeError(error)

    // 401 sur une route protégée = jeton expiré ou invalide : on ferme la session.
    // 401 sur /login = simple mauvais mot de passe : on ne déconnecte rien.
    const isLoginCall = error.config?.url?.includes('/api/auth/login')
    if (normalized.status === 401 && !isLoginCall) {
      onUnauthorized()
    }
    return Promise.reject(normalized)
  },
)

export default http
