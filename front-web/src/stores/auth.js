import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { authApi } from '@/api/auth'
import { STORAGE_KEYS, readJson, remove, writeJson } from '@/utils/storage'

// Relit la session sauvegardée. Une session expirée est supprimée tout de suite.
function loadSession() {
  const stored = readJson(STORAGE_KEYS.AUTH)
  if (!stored?.token || !stored?.expiresAt) return { session: null, expired: false }
  if (new Date(stored.expiresAt) <= new Date()) {
    remove(STORAGE_KEYS.AUTH)
    return { session: null, expired: true }
  }
  return { session: stored, expired: false }
}

export const useAuthStore = defineStore('auth', () => {
  const { session, expired } = loadSession()

  const token = ref(session?.token ?? null)
  const expiresAt = ref(session?.expiresAt ?? null)
  const user = ref(session?.user ?? null)
  // true quand la session a été confirmée par l'API pendant cette visite de la page
  const verified = ref(false)
  // true quand l'utilisateur doit être prévenu que sa session a pris fin
  const sessionExpired = ref(expired)

  const isAuthenticated = computed(() => !!token.value)
  const role = computed(() => user.value?.role ?? null)
  const fullName = computed(() => (user.value ? `${user.value.prenom} ${user.value.nom}` : ''))

  function persist() {
    writeJson(STORAGE_KEYS.AUTH, {
      token: token.value,
      expiresAt: expiresAt.value,
      user: user.value,
    })
  }

  async function login(email, motDePasse) {
    const response = await authApi.login(email, motDePasse)
    token.value = response.accessToken
    expiresAt.value = response.expiresAt
    user.value = response.utilisateur
    verified.value = true
    sessionExpired.value = false
    persist()
  }

  // Relit le profil auprès de l'API : confirme que le jeton est valide et le compte toujours actif.
  async function refreshProfile() {
    user.value = await authApi.me()
    verified.value = true
    persist()
  }

  function logout(expired = false) {
    token.value = null
    expiresAt.value = null
    user.value = null
    verified.value = false
    sessionExpired.value = expired
    remove(STORAGE_KEYS.AUTH)
  }

  // Vérifie l'expiration au moment de l'appel (un computed ne verrait pas le temps passer).
  function hasValidSession() {
    if (!token.value) return false
    if (new Date(expiresAt.value) <= new Date()) {
      logout(true)
      return false
    }
    return true
  }

  return {
    token,
    user,
    verified,
    sessionExpired,
    isAuthenticated,
    role,
    fullName,
    login,
    refreshProfile,
    logout,
    hasValidSession,
  }
})
