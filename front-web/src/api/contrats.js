import http from './http'

export const contratsApi = {
  rechercher: (params = {}) =>
    http.get('/api/contrats', { params }).then((response) => response.data),
  obtenir: (id) => http.get(`/api/contrats/${id}`).then((response) => response.data),
  mesContrats: (params = {}) =>
    http.get('/api/contrats/mes-contrats', { params }).then((response) => response.data),
  creer: (data) => http.post('/api/contrats', data).then((response) => response.data),
  modifier: (id, data) =>
    http.put(`/api/contrats/${id}`, data).then((response) => response.data),
  envoyerPourSignature: (id) => http.post(`/api/contrats/${id}/envoyer-signature`),
  enregistrerSignature: (id, fichier, dateSignature) => {
    const data = new FormData()
    data.append('fichier', fichier)
    data.append('dateSignature', dateSignature)
    return http.post(`/api/contrats/${id}/signature`, data)
  },
  renouveler: (id, data) =>
    http.post(`/api/contrats/${id}/renouveler`, data).then((response) => response.data),
  transformer: (id, data) =>
    http.post(`/api/contrats/${id}/transformer`, data).then((response) => response.data),
  terminer: (id, data) => http.post(`/api/contrats/${id}/terminer`, data),
  annuler: (id) => http.post(`/api/contrats/${id}/annuler`),
  historique: (id) =>
    http.get(`/api/contrats/${id}/historique`).then((response) => response.data),
}