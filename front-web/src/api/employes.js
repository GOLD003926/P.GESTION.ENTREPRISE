import http from './http'

export const employesApi = {
  rechercher: (params = {}) =>
    http.get('/api/employes', { params }).then((response) => response.data),
  obtenir: (id) => http.get(`/api/employes/${id}`).then((response) => response.data),
  moi: () => http.get('/api/employes/moi').then((response) => response.data),
  creer: (data) => http.post('/api/employes', data).then((response) => response.data),
  modifier: (id, data) => http.put(`/api/employes/${id}`, data).then((response) => response.data),
  suspendre: (id) => http.post(`/api/employes/${id}/suspendre`).then((response) => response.data),
  reprendre: (id) => http.post(`/api/employes/${id}/reprendre`).then((response) => response.data),
  fichePdf: (id) => http.get(`/api/employes/${id}/fiche-pdf`, { responseType: 'blob' }),
  historique: (id, params = {}) =>
    http.get(`/api/employes/${id}/historique`, { params }).then((response) => response.data),
}
