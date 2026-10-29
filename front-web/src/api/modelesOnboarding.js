import http from './http'

export const modelesOnboardingApi = {
  rechercher: (params = {}) =>
    http.get('/api/modeles-onboarding', { params }).then((response) => response.data),
  obtenir: (id) => http.get(`/api/modeles-onboarding/${id}`).then((response) => response.data),
  creer: (data) => http.post('/api/modeles-onboarding', data).then((response) => response.data),
  modifier: (id, data) =>
    http.put(`/api/modeles-onboarding/${id}`, data).then((response) => response.data),
  activer: (id) => http.post(`/api/modeles-onboarding/${id}/activer`),
  desactiver: (id) => http.post(`/api/modeles-onboarding/${id}/desactiver`),
}
