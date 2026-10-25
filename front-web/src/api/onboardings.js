import http from './http'

export const onboardingsApi = {
  monOnboarding: () =>
    http.get('/api/onboardings/mon-onboarding').then((response) => response.data),
}
