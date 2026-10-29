import http from './http'

export const departementsApi = {
  rechercher: (params = {}) =>
    http.get('/api/departements', { params }).then((response) => response.data),
  obtenir: (id) => http.get(`/api/departements/${id}`).then((response) => response.data),
  creer: (data) => http.post('/api/departements', data).then((response) => response.data),
  modifier: (id, data) =>
    http.put(`/api/departements/${id}`, data).then((response) => response.data),
  activer: (id) => http.post(`/api/departements/${id}/activer`).then((response) => response.data),
  desactiver: (id) =>
    http.post(`/api/departements/${id}/desactiver`).then((response) => response.data),
}
