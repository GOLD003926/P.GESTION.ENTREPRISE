import http from './http'

export const onboardingTachesApi = {
  mesTaches: (params = {}) =>
    http.get('/api/onboarding-taches/mes-taches', { params }).then((response) => response.data),
  obtenir: (id) => http.get(`/api/onboarding-taches/${id}`).then((response) => response.data),
  demarrer: (id, data = {}) => http.post(`/api/onboarding-taches/${id}/demarrer`, data),
  terminer: (id, data = {}) => http.post(`/api/onboarding-taches/${id}/terminer`, data),
  nonApplicable: (id, data = {}) =>
    http.post(`/api/onboarding-taches/${id}/non-applicable`, data),
  assigner: (id, data = {}) => http.post(`/api/onboarding-taches/${id}/assigner`, data),
}
