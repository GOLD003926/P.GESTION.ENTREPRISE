import http from './http'

export const candidatsApi = {
  rechercher: (params = {}) =>
    http.get('/api/candidats', { params }).then((response) => response.data),
  obtenir: (id) => http.get(`/api/candidats/${id}`).then((response) => response.data),
  creer: (data) => http.post('/api/candidats', data).then((response) => response.data),
  modifier: (id, data) => http.put(`/api/candidats/${id}`, data).then((response) => response.data),
}
