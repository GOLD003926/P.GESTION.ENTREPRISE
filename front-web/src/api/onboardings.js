import http from './http'

export const onboardingsApi = {
  rechercher: (params = {}) =>
    http.get('/api/onboardings', { params }).then((response) => response.data),
  obtenir: (id) => http.get(`/api/onboardings/${id}`).then((response) => response.data),
  monOnboarding: () =>
    http.get('/api/onboardings/mon-onboarding').then((response) => response.data),
  historique: (id) =>
    http.get(`/api/onboardings/${id}/historique`).then((response) => response.data),
  modifier: (id, data) => http.put(`/api/onboardings/${id}`, data).then((response) => response.data),
}
