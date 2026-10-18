// Accès au localStorage sécurisé : il peut être indisponible (navigation privée, quota plein).
export const STORAGE_KEYS = Object.freeze({
  AUTH: 'sirh.auth',
})

export function readJson(key) {
  try {
    const raw = localStorage.getItem(key)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

export function writeJson(key, value) {
  try {
    localStorage.setItem(key, JSON.stringify(value))
  } catch {
    // Stockage indisponible : la session ne survivra simplement pas à un rechargement de page.
  }
}

export function remove(key) {
  try {
    localStorage.removeItem(key)
  } catch {
    // Rien à faire.
  }
}
