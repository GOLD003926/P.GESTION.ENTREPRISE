import http from './http'

export const propositionsApi = {
  rechercher: (params = {}) =>
    http.get('/api/propositions', { params }).then((response) => response.data),
  obtenir: (id) => http.get(`/api/propositions/${id}`).then((response) => response.data),
  creer: (data) => http.post('/api/propositions', data).then((response) => response.data),
  modifier: (id, data) =>
    http.put(`/api/propositions/${id}`, data).then((response) => response.data),
  derogation: (id, data) =>
    http.post(`/api/propositions/${id}/derogation`, data).then((response) => response.data),
  envoyer: (id) => http.post(`/api/propositions/${id}/envoyer`).then((response) => response.data),
  repondre: (id, data) =>
    http.post(`/api/propositions/${id}/reponse`, data).then((response) => response.data),
  retirer: (id) => http.post(`/api/propositions/${id}/retirer`).then((response) => response.data),
  historique: (id) =>
    http.get(`/api/propositions/${id}/historique`).then((response) => response.data),
}
