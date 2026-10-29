import http from './http'

export const candidaturesApi = {
  rechercher: (params = {}) =>
    http.get('/api/candidatures', { params }).then((response) => response.data),
  obtenir: (id) => http.get(`/api/candidatures/${id}`).then((response) => response.data),
  creer: (data) => http.post('/api/candidatures', data).then((response) => response.data),
  televerserCv: (id, fichier) => {
    const formData = new FormData()
    formData.append('fichier', fichier)
    return http
      .post(`/api/candidatures/${id}/cv`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      })
      .then((response) => response.data)
  },
  preselectionner: (id, data) =>
    http.post(`/api/candidatures/${id}/preselection`, data).then((response) => response.data),
  decisionFinale: (id, data) =>
    http.post(`/api/candidatures/${id}/decision-finale`, data).then((response) => response.data),
  promouvoirListeAttente: (id) =>
    http.post(`/api/candidatures/${id}/promouvoir-liste-attente`).then((response) => response.data),
  desister: (id) => http.post(`/api/candidatures/${id}/desister`).then((response) => response.data),
  historique: (id) =>
    http.get(`/api/candidatures/${id}/historique`).then((response) => response.data),
}
