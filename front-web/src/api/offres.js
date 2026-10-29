import http from './http'

export const offresApi = {
  rechercher: (params = {}) =>
    http.get('/api/offres', { params }).then((response) => response.data),
  obtenir: (id) => http.get(`/api/offres/${id}`).then((response) => response.data),
  creer: (data) => http.post('/api/offres', data).then((response) => response.data),
  modifier: (id, data) => http.put(`/api/offres/${id}`, data).then((response) => response.data),
  publier: (id) => http.post(`/api/offres/${id}/publier`).then((response) => response.data),
  cloturer: (id) => http.post(`/api/offres/${id}/cloturer`).then((response) => response.data),
  annuler: (id) => http.post(`/api/offres/${id}/annuler`).then((response) => response.data),
}
