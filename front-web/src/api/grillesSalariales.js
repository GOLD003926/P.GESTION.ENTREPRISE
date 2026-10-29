import http from './http'

export const grillesSalarialesApi = {
  rechercher: (params = {}) =>
    http.get('/api/grilles-salariales', { params }).then((response) => response.data),
  obtenir: (id) => http.get(`/api/grilles-salariales/${id}`).then((response) => response.data),
  creer: (data) => http.post('/api/grilles-salariales', data).then((response) => response.data),
  modifier: (id, data) =>
    http.put(`/api/grilles-salariales/${id}`, data).then((response) => response.data),
  activer: (id) =>
    http.post(`/api/grilles-salariales/${id}/activer`).then((response) => response.data),
  desactiver: (id) =>
    http.post(`/api/grilles-salariales/${id}/desactiver`).then((response) => response.data),
}
