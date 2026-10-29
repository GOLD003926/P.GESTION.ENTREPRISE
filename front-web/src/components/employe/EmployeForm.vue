<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { employesApi } from '@/api/employes'
import { ROUTE_NAMES } from '@/constants/routeNames'
import AppAlert from '@/components/common/AppAlert.vue'
import AppButton from '@/components/common/AppButton.vue'

const props = defineProps({ id: { type: String, default: null } })
const router = useRouter()
const form = reactive({
  nom: '',
  prenom: '',
  emailPro: '',
  emailPerso: '',
  telephone: '',
  dateNaissance: '',
  adresse: '',
  numeroSecuriteSociale: '',
})
const loading = ref(Boolean(props.id))
const saving = ref(false)
const error = ref('')
const fieldErrors = ref({})

async function load() {
  if (!props.id) return
  try {
    Object.assign(form, await employesApi.obtenir(props.id))
  } catch (cause) {
    error.value = cause.message
  } finally {
    loading.value = false
  }
}

async function submit() {
  error.value = ''
  fieldErrors.value = {}
  saving.value = true
  const payload = {
    nom: form.nom.trim(),
    prenom: form.prenom.trim(),
    emailPro: form.emailPro.trim() || null,
    emailPerso: form.emailPerso.trim() || null,
    telephone: form.telephone.trim() || null,
    dateNaissance: form.dateNaissance || null,
    adresse: form.adresse.trim() || null,
    numeroSecuriteSociale: form.numeroSecuriteSociale.trim() || null,
  }
  try {
    const employe = props.id
      ? await employesApi.modifier(props.id, payload)
      : await employesApi.creer(payload)
    await router.push({ name: ROUTE_NAMES.EMPLOYE_DETAIL, params: { id: employe.id } })
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
  <section class="page">
    <header>
      <p class="eyebrow">Dossiers employés</p>
      <h1>{{ id ? 'Modifier les coordonnées' : 'Créer un dossier employé' }}</h1>
      <p>Le matricule est généré automatiquement et ne peut pas être modifié.</p>
    </header>
    <AppAlert v-if="error" variant="error">{{ error }}</AppAlert>
    <p v-if="loading" role="status">Chargement du dossier…</p>
    <form v-else class="form-card" @submit.prevent="submit">
      <label
        >Nom
        <input v-model.trim="form.nom" maxlength="100" autocomplete="family-name" required />
        <small v-if="fieldErrors.nom">{{ fieldErrors.nom }}</small>
      </label>
      <label
        >Prénom
        <input v-model.trim="form.prenom" maxlength="100" autocomplete="given-name" required />
        <small v-if="fieldErrors.prenom">{{ fieldErrors.prenom }}</small>
      </label>
      <label
        >E-mail professionnel
        <input
          v-model.trim="form.emailPro"
          type="email"
          maxlength="150"
          autocomplete="work email"
        />
        <small v-if="fieldErrors.emailPro">{{ fieldErrors.emailPro }}</small>
      </label>
      <label
        >E-mail personnel
        <input v-model.trim="form.emailPerso" type="email" maxlength="150" autocomplete="email" />
        <small v-if="fieldErrors.emailPerso">{{ fieldErrors.emailPerso }}</small>
      </label>
      <label
        >Téléphone
        <input v-model.trim="form.telephone" type="tel" maxlength="30" autocomplete="tel" />
        <small v-if="fieldErrors.telephone">{{ fieldErrors.telephone }}</small>
      </label>
      <label
        >Date de naissance
        <input
          v-model="form.dateNaissance"
          type="date"
          :max="new Date().toISOString().slice(0, 10)"
        />
        <small v-if="fieldErrors.dateNaissance">{{ fieldErrors.dateNaissance }}</small>
      </label>
      <label class="wide"
        >Adresse
        <input v-model.trim="form.adresse" maxlength="255" autocomplete="street-address" />
        <small v-if="fieldErrors.adresse">{{ fieldErrors.adresse }}</small>
      </label>
      <label class="wide"
        >Numéro de sécurité sociale
        <input v-model.trim="form.numeroSecuriteSociale" maxlength="30" />
        <small v-if="fieldErrors.numeroSecuriteSociale">{{
          fieldErrors.numeroSecuriteSociale
        }}</small>
      </label>
      <footer>
        <RouterLink
          :to="
            id
              ? { name: ROUTE_NAMES.EMPLOYE_DETAIL, params: { id } }
              : { name: ROUTE_NAMES.EMPLOYES }
          "
          >Retour</RouterLink
        >
        <AppButton type="submit" :loading="saving">{{
          id ? 'Enregistrer les modifications' : 'Créer le dossier'
        }}</AppButton>
      </footer>
    </form>
  </section>
</template>

<style scoped>
.page {
  display: grid;
  gap: var(--space-5);
}
header h1 {
  margin: 0;
  color: var(--color-text);
  font-size: var(--font-size-2xl);
}
header p:last-child {
  margin: var(--space-2) 0 0;
  color: var(--color-text-muted);
}
.eyebrow {
  margin: 0 0 var(--space-1);
  color: var(--color-primary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
}
.form-card {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-4);
  padding: var(--space-5);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  box-shadow: var(--shadow-card);
}
.form-card label {
  display: grid;
  gap: var(--space-2);
  color: var(--color-text);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
}
.form-card input {
  min-height: var(--control-height);
  padding: 0 var(--space-3);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-text);
  font: inherit;
  font-weight: var(--font-weight-normal);
}
.form-card small {
  color: var(--color-danger);
  font-weight: var(--font-weight-normal);
}
.wide,
.form-card footer {
  grid-column: 1 / -1;
}
.form-card footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: var(--space-4);
  padding-top: var(--space-4);
  border-top: 1px solid var(--color-border);
}
.form-card footer a {
  color: var(--color-primary);
  font-weight: var(--font-weight-semibold);
  text-decoration: none;
}
@media (max-width: 680px) {
  .form-card {
    grid-template-columns: 1fr;
  }
  .wide,
  .form-card footer {
    grid-column: auto;
  }
  .form-card footer {
    align-items: stretch;
    flex-direction: column-reverse;
  }
}
</style>
