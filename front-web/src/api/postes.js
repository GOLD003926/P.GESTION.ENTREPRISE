import http from './http'

export const postesApi = {
  rechercher: (params = {}) =>
    http.get('/api/postes', { params }).then((response) => response.data),
  obtenir: (id) => http.get(`/api/postes/${id}`).then((response) => response.data),
  creer: (data) => http.post('/api/postes', data).then((response) => response.data),
  modifier: (id, data) => http.put(`/api/postes/${id}`, data).then((response) => response.data),
  activer: (id) => http.post(`/api/postes/${id}/activer`).then((response) => response.data),
  desactiver: (id) => http.post(`/api/postes/${id}/desactiver`).then((response) => response.data),
}
