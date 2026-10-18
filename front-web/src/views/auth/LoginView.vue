<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppAlert from '@/components/common/AppAlert.vue'
import AppButton from '@/components/common/AppButton.vue'
import AppInput from '@/components/common/AppInput.vue'
import heroImage from '@/assets/images/login-image.webp'
import { ROUTE_NAMES } from '@/constants/routeNames'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()

const form = reactive({ email: '', motDePasse: '' })
const errors = reactive({ email: '', motDePasse: '' })
const formError = ref('')
const submitting = ref(false)
const emailField = ref(null)

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

// Validation sur place, avant tout appel réseau (l'API revalide de toute façon).
function validate() {
  errors.email = ''
  errors.motDePasse = ''

  const email = form.email.trim()
  if (!email) errors.email = "L'email est obligatoire"
  else if (!EMAIL_PATTERN.test(email)) errors.email = "L'email n'est pas valide"

  if (!form.motDePasse) errors.motDePasse = 'Le mot de passe est obligatoire'

  return !errors.email && !errors.motDePasse
}

// Redirection sûre : seul un chemin interne est accepté (jamais une adresse vers un autre site).
function destinationAfterLogin() {
  const redirect = route.query.redirect
  if (typeof redirect === 'string' && redirect.startsWith('/') && !redirect.startsWith('//')) {
    return redirect
  }
  return { name: ROUTE_NAMES.DASHBOARD }
}

async function submit() {
  if (submitting.value) return
  formError.value = ''
  if (!validate()) return

  submitting.value = true
  try {
    await auth.login(form.email.trim(), form.motDePasse)
    await router.replace(destinationAfterLogin())
  } catch (error) {
    const fieldErrors = error.fieldErrors ?? {}
    if (fieldErrors.email) errors.email = fieldErrors.email
    if (fieldErrors.motDePasse) errors.motDePasse = fieldErrors.motDePasse
    if (!fieldErrors.email && !fieldErrors.motDePasse) {
      formError.value = error.message ?? 'Une erreur est survenue. Réessayez plus tard.'
    }
    // Après un mauvais mot de passe on le vide ; après une panne réseau on le garde.
    if (error.code === 'BAD_CREDENTIALS') form.motDePasse = ''
  } finally {
    submitting.value = false
  }
}

onMounted(() => emailField.value?.focus())
</script>

<template>
  <div class="login">
    <!-- Panneau d'image : purement décoratif, masqué sur petit écran -->
    <aside class="hero">
      <img class="hero__image" :src="heroImage" alt="" decoding="async" />

      <div class="hero__inner">
        <div class="brand brand--light">
          <span class="brand__mark" aria-hidden="true">S</span>
          <span class="brand__name">SIRH</span>
        </div>

        <div class="hero__body">
          <p class="hero__title">Pilotez le cycle de vie de vos salariés</p>
          <p class="hero__text">
            Du besoin de recrutement à l'intégration du nouvel arrivant, suivez chaque étape au même
            endroit, avec un historique complet.
          </p>
        </div>

        <p class="hero__footer">Espace réservé aux utilisateurs autorisés de l'entreprise.</p>
      </div>
    </aside>

    <main class="panel">
      <section class="panel__content" aria-labelledby="login-title">
        <!-- Marque affichée uniquement quand le panneau d'image est masqué -->
        <div class="brand brand--dark brand--mobile">
          <span class="brand__mark" aria-hidden="true">S</span>
          <span class="brand__name">SIRH</span>
        </div>

        <header class="panel__header">
          <p class="panel__eyebrow">Espace collaborateurs</p>
          <h1 id="login-title" class="panel__title">Connexion</h1>
          <p class="panel__subtitle">Entrez vos identifiants pour accéder à votre espace.</p>
        </header>

        <AppAlert v-if="auth.sessionExpired" variant="info">
          Votre session a expiré. Veuillez vous reconnecter.
        </AppAlert>
        <AppAlert v-if="formError" variant="error">{{ formError }}</AppAlert>

        <form class="panel__form" novalidate @submit.prevent="submit">
          <AppInput
            ref="emailField"
            v-model="form.email"
            label="Adresse email"
            type="email"
            autocomplete="username"
            placeholder="prenom.nom@entreprise.com"
            :error="errors.email"
          />
          <AppInput
            v-model="form.motDePasse"
            label="Mot de passe"
            type="password"
            autocomplete="current-password"
            :error="errors.motDePasse"
          />
          <AppButton type="submit" block :loading="submitting">
            {{ submitting ? 'Connexion…' : 'Se connecter' }}
          </AppButton>
        </form>

        <p class="panel__help">
          Mot de passe oublié ou compte bloqué ? Contactez l'administrateur pour le réinitialiser.
        </p>
      </section>
    </main>
  </div>
</template>

<style scoped>
.login {
  min-height: 100dvh;
  display: grid;
  grid-template-columns: minmax(0, 1fr) clamp(440px, 40vw, 640px);
}
/* ---------- Panneau d'image ---------- */
.hero {
  position: relative;
  isolation: isolate;
  overflow: hidden;
  background: var(--color-brand-900); /* visible tant que l'image charge */
}

.hero__image {
  position: absolute;
  inset: 0;
  z-index: -2;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* Voile bleu foncé : garantit la lisibilité du texte quelle que soit l'image */
.hero::after {
  content: '';
  position: absolute;
  inset: 0;
  z-index: -1;
  background: linear-gradient(
    to top,
    rgb(12 45 77 / 0.95) 0%,
    rgb(12 45 77 / 0.78) 50%,
    rgb(12 45 77 / 0.6) 100%
  );
}

.hero__inner {
  min-height: 100%;
  display: flex;
  flex-direction: column;
  padding: var(--space-10) clamp(var(--space-6), 4vw, 3.5rem);
}

.hero__body {
  margin-block: auto; /* centre le texte verticalement entre la marque et le pied */
  max-width: 34rem;
  padding-block: var(--space-8);
}

.hero__title {
  font-size: clamp(1.75rem, 2.6vw, 2.5rem);
  font-weight: var(--font-weight-bold);
  line-height: 1.15;
  letter-spacing: -0.01em;
  color: var(--color-on-dark);
}

.hero__text {
  margin-top: var(--space-4);
  font-size: var(--font-size-lg);
  color: var(--color-on-dark-muted);
}

.hero__footer {
  font-size: var(--font-size-sm);
  color: var(--color-on-dark-muted);
}

/* ---------- Marque ---------- */
.brand {
  display: inline-flex;
  align-items: center;
  gap: var(--space-3);
}

.brand__mark {
  width: 2.5rem;
  height: 2.5rem;
  display: grid;
  place-items: center;
  border-radius: var(--radius-md);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
}

.brand__name {
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-bold);
  letter-spacing: 0.02em;
}

.brand--light .brand__mark {
  background: var(--color-surface);
  color: var(--color-primary);
}

.brand--light .brand__name {
  color: var(--color-on-dark);
}

.brand--dark .brand__mark {
  background: var(--color-primary);
  color: var(--color-on-primary);
}

.brand--dark .brand__name {
  color: var(--color-text);
}

.brand--mobile {
  display: none;
}

/* ---------- Panneau du formulaire ---------- */
.panel {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-8) var(--space-6);
  background: var(--color-bg-warm);
}

.panel__content {
  width: 100%;
  max-width: 420px;
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.panel__header {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.panel__eyebrow {
  font-size: 0.75rem;
  font-weight: var(--font-weight-bold);
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--color-accent);
}

.panel__title {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  line-height: 1.2;
}

.panel__subtitle {
  color: var(--color-text-muted);
}

.panel__form {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.panel__help {
  padding-top: var(--space-4);
  border-top: 1px solid var(--color-border);
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}

/* ---------- Petit écran : on ne garde que le formulaire ---------- */
@media (max-width: 900px) {
  .login {
    grid-template-columns: 1fr;
  }

  .hero {
    display: none;
  }

  .brand--mobile {
    display: inline-flex;
  }
}
@media (max-width: 480px) {
  .panel {
    padding: var(--space-6) var(--space-4);
  }

  .panel__title {
    font-size: var(--font-size-xl);
  }
}
</style>
