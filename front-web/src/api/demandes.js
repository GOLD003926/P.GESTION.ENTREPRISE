import http from './http'

export const demandesApi = {
  rechercher: (params = {}) =>
    http.get('/api/demandes', { params }).then((response) => response.data),
  obtenir: (id) => http.get(`/api/demandes/${id}`).then((response) => response.data),
  creer: (data) => http.post('/api/demandes', data).then((response) => response.data),
  modifier: (id, data) => http.put(`/api/demandes/${id}`, data).then((response) => response.data),
  soumettre: (id) => http.post(`/api/demandes/${id}/soumettre`).then((response) => response.data),
  annuler: (id, data) =>
    http.post(`/api/demandes/${id}/annuler`, data).then((response) => response.data),
  historique: (id) => http.get(`/api/demandes/${id}/historique`).then((response) => response.data),
}
