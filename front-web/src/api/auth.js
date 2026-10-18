import http from './http'

// Miroir de AuthController. Chaque fonction renvoie directement les données (pas la réponse axios).
export const authApi = {
  login: (email, motDePasse) =>
    http.post('/api/auth/login', { email, motDePasse }).then((response) => response.data),

  me: () => http.get('/api/auth/me').then((response) => response.data),

  changePassword: (motDePasseActuel, nouveauMotDePasse) =>
    http.put('/api/auth/me/mot-de-passe', { motDePasseActuel, nouveauMotDePasse }),
}
