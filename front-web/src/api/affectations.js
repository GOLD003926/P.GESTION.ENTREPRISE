import http from './http'

export const affectationsApi = {
  lister: (params = {}) =>
    http.get('/api/affectations', { params }).then((response) => response.data),
  creer: (data) => http.post('/api/affectations', data).then((response) => response.data),
  cloturer: (id, data) =>
    http.post(`/api/affectations/${id}/cloturer`, data).then((response) => response.data),
}
