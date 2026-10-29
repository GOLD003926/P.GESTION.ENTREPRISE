import http from './http'

export const validationsApi = {
  enAttente: (params = {}) =>
    http.get('/api/validations/en-attente', { params }).then((response) => response.data),
  decider: (demandeId, data) =>
    http
      .post(`/api/validations/demandes/${demandeId}/decision`, data)
      .then((response) => response.data),
  historique: (demandeId) =>
    http.get(`/api/validations/demandes/${demandeId}`).then((response) => response.data),
}
