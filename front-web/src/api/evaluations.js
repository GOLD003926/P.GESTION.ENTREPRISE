import http from './http'

export const evaluationsApi = {
  rechercher: (params = {}) =>
    http.get('/api/evaluations', { params }).then((response) => response.data),
  creer: (data) => http.post('/api/evaluations', data).then((response) => response.data),
  modifier: (id, data) =>
    http.put(`/api/evaluations/${id}`, data).then((response) => response.data),
  verrouiller: (id) =>
    http.post(`/api/evaluations/${id}/verrouiller`).then((response) => response.data),
  corriger: (id, data) =>
    http.post(`/api/evaluations/${id}/corriger`, data).then((response) => response.data),
}
