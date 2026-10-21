import http from './http'

export const onboardingTachesApi = {
  mesTaches: (params = {}) =>
    http.get('/api/onboarding-taches/mes-taches', { params }).then((response) => response.data),
}
