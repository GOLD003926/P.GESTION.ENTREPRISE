import http from './http'

export const validationsApi = {
  enAttente: (params = {}) =>
    http.get('/api/validations/en-attente', { params }).then((response) => response.data),
}
