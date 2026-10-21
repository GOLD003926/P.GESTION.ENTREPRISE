import http from './http'

export const auditApi = {
  journal: (params = {}) =>
    http.get('/api/audit/journal', { params }).then((response) => response.data),
}
