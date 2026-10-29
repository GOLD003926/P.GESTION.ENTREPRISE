import http from './http'

export const entretiensApi = {
  rechercher: (params = {}) =>
    http.get('/api/entretiens', { params }).then((response) => response.data),
  obtenir: (id) => http.get(`/api/entretiens/${id}`).then((response) => response.data),
  creer: (data) => http.post('/api/entretiens', data).then((response) => response.data),
  modifier: (id, data) => http.put(`/api/entretiens/${id}`, data).then((response) => response.data),
  cloturer: (id) => http.post(`/api/entretiens/${id}/cloturer`).then((response) => response.data),
  annuler: (id, data) =>
    http.post(`/api/entretiens/${id}/annuler`, data).then((response) => response.data),
  marquerAbsent: (id) =>
    http.post(`/api/entretiens/${id}/absent`).then((response) => response.data),
}
