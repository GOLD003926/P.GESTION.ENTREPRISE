<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { candidatsApi } from '@/api/candidats'
import { ROUTE_NAMES } from '@/constants/routeNames'
import AppAlert from '@/components/common/AppAlert.vue'
import AppButton from '@/components/common/AppButton.vue'

const props = defineProps({ id: { type: String, default: null } })
const router = useRouter()
const form = reactive({
  nom: '',
  prenom: '',
  email: '',
  telephone: '',
  dateNaissance: '',
  adresse: '',
  nationalite: '',
  consentementDonnees: false,
})
const loading = ref(Boolean(props.id))
const saving = ref(false)
const error = ref('')
const fieldErrors = ref({})

async function load() {
  if (!props.id) return
  try {
    Object.assign(form, await candidatsApi.obtenir(props.id))
  } catch (cause) {
    error.value = cause.message
  } finally {
    loading.value = false
  }
}

async function submit() {
  saving.value = true
  error.value = ''
  fieldErrors.value = {}
  const payload = {
    nom: form.nom.trim(),
    prenom: form.prenom.trim(),
    email: form.email.trim().toLowerCase(),
    telephone: form.telephone.trim() || null,
    dateNaissance: form.dateNaissance || null,
    adresse: form.adresse.trim() || null,
    nationalite: form.nationalite.trim() || null,
    consentementDonnees: form.consentementDonnees,
  }
  try {
    const result = props.id
      ? await candidatsApi.modifier(props.id, payload)
      : await candidatsApi.creer(payload)
    await router.push({ name: ROUTE_NAMES.CANDIDAT_DETAIL, params: { id: result.id } })
  } catch (cause) {
    error.value = cause.message
    fieldErrors.value = cause.fieldErrors ?? {}
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<template>
  <section class="candidate-form-page">
    <header>
      <p class="recruitment-eyebrow">Recrutement</p>
      <h1>{{ id ? 'Modifier le candidat' : 'Créer un candidat' }}</h1>
    </header>
    <AppAlert v-if="error" variant="error">{{ error }}</AppAlert>
    <p v-if="loading" role="status">Chargement du candidat…</p>
    <form v-else class="recruitment-card candidate-form" @submit.prevent="submit">
      <label
        >Nom<input v-model.trim="form.nom" maxlength="100" required /><small>{{
          fieldErrors.nom
        }}</small></label
      >
      <label
        >Prénom<input v-model.trim="form.prenom" maxlength="100" required /><small>{{
          fieldErrors.prenom
        }}</small></label
      >
      <label
        >Email<input v-model.trim="form.email" type="email" maxlength="150" required /><small>{{
          fieldErrors.email
        }}</small></label
      >
      <label
        >Téléphone<input v-model.trim="form.telephone" maxlength="30" /><small>{{
          fieldErrors.telephone
        }}</small></label
      >
      <label
        >Date de naissance<input
          v-model="form.dateNaissance"
          type="date"
          :max="new Date().toISOString().slice(0, 10)"
      /></label>
      <label>Nationalité<input v-model.trim="form.nationalite" maxlength="60" /></label>
      <label class="candidate-form__wide"
        >Adresse<input v-model.trim="form.adresse" maxlength="255"
      /></label>
      <label class="candidate-form__consent"
        ><input v-model="form.consentementDonnees" type="checkbox" />Consentement au traitement des
        données</label
      >
      <footer>
        <RouterLink :to="{ name: ROUTE_NAMES.CANDIDATS }">Retour</RouterLink
        ><AppButton type="submit" :loading="saving">Enregistrer</AppButton>
      </footer>
    </form>
  </section>
</template>

<style scoped>
.candidate-form-page {
  display: grid;
  gap: var(--space-5);
}
.candidate-form-page h1 {
  margin: 0;
  color: var(--color-text);
  font-size: var(--font-size-2xl);
}
.recruitment-eyebrow {
  margin: 0 0 var(--space-1);
  color: var(--color-primary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
}
.recruitment-card {
  padding: var(--space-5);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  box-shadow: var(--shadow-card);
}
.candidate-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-4);
}
.candidate-form label {
  display: grid;
  gap: var(--space-2);
  color: var(--color-text);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
}
.candidate-form input:not([type='checkbox']) {
  min-height: var(--control-height);
  padding: 0 var(--space-3);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-text);
  font: inherit;
  font-weight: var(--font-weight-normal);
}
.candidate-form small {
  color: var(--color-danger);
  font-weight: var(--font-weight-normal);
}
.candidate-form__wide,
.candidate-form footer {
  grid-column: 1 / -1;
}
.candidate-form .candidate-form__consent {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}
.candidate-form footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: var(--space-4);
  padding-top: var(--space-4);
  border-top: 1px solid var(--color-border);
}
.candidate-form a {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: var(--font-weight-semibold);
}
@media (max-width: 700px) {
  .candidate-form {
    grid-template-columns: 1fr;
  }
  .candidate-form__wide,
  .candidate-form footer {
    grid-column: auto;
  }
  .candidate-form footer {
    align-items: stretch;
    flex-direction: column-reverse;
  }
}
</style>
