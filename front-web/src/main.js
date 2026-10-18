import './assets/styles/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import { setUnauthorizedHandler } from '@/api/http'
import { ROUTE_NAMES } from '@/constants/routeNames'
import { useAuthStore } from '@/stores/auth'

const app = createApp(App)
const pinia = createPinia()

// L'ordre compte : Pinia doit être installé AVANT le routeur, car la garde de navigation utilise le store.
app.use(pinia)
app.use(router)

// Jeton refusé par l'API (expiré, falsifié) : on ferme la session et on renvoie au login.
setUnauthorizedHandler(() => {
  useAuthStore(pinia).logout(true)
  if (router.currentRoute.value.name !== ROUTE_NAMES.LOGIN) {
    router.push({
      name: ROUTE_NAMES.LOGIN,
      query: { redirect: router.currentRoute.value.fullPath },
    })
  }
})

app.mount('#app')
