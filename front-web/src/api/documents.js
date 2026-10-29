import http from './http'

export const documentsApi = {
  obtenir: (id) => http.get(`/api/documents/${id}`).then((response) => response.data),
  telecharger: (id) =>
    http.get(`/api/documents/${id}/contenu`, { responseType: 'blob' }).then((response) => response),
}
