<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { candidaturesApi } from '@/api/candidatures'
import { propositionsApi } from '@/api/propositions'
import AppAlert from '@/components/common/AppAlert.vue'
import AppButton from '@/components/common/AppButton.vue'
import { ROUTE_NAMES } from '@/constants/routeNames'
import { TYPE_CONTRAT_LABELS } from '@/constants/statuts'

const props = defineProps({
  id: { type: String, default: null },
  candidatureId: { type: String, default: null },
})

const router = useRouter()
const isEdit = computed(() => Boolean(props.id))
const form = reactive({
  candidatureId: props.candidatureId ?? '',
  typeContratPropose: 'ESSAI',
  salairePropose: '',
  devise: 'MGA',
  horairesTravail: '',
  lieuTravail: '',
  dateDebutPrevue: '',
  dureeMois: '',
  dateLimiteReponse: '',
})
const candidatures = ref([])
const loading = ref(true)
const saving = ref(false)
const error = ref('')
const fieldErrors = ref({})

function validate() {
  const errors = {}
  if (!isEdit.value && !form.candidatureId) errors.candidatureId = 'La candidature est obligatoire.'
  if (!form.typeContratPropose) errors.typeContratPropose = 'Le type de contrat est obligatoire.'
  if (!form.salairePropose || Number(form.salairePropose) <= 0) {
    errors.salairePropose = 'Le salaire proposé doit être strictement positif.'
  }
  if (!form.devise || form.devise.length !== 3) errors.devise = 'La devise doit contenir 3 lettres.'
  if (!form.horairesTravail.trim()) errors.horairesTravail = 'Les horaires de travail sont obligatoires.'
  if (!form.lieuTravail.trim()) errors.lieuTravail = 'Le lieu de travail est obligatoire.'
  if (!form.dateDebutPrevue) errors.dateDebutPrevue = 'La date de début est obligatoire.'
  if (form.typeContratPropose !== 'CDI' && (!form.dureeMois || Number(form.dureeMois) < 1)) {
    errors.dureeMois = 'La durée est obligatoire pour un CDD ou un essai.'
  }
  if (form.typeContratPropose === 'CDI' && form.dureeMois) {
    errors.dureeMois = 'La durée n’est pas autorisée pour un CDI.'
  }
  if (form.dateLimiteReponse && new Date(`${form.dateLimiteReponse}T00:00:00`) <= new Date()) {
    errors.dateLimiteReponse = 'La date limite doit être future.'
  }

  fieldErrors.value = errors
  return Object.keys(errors).length === 0
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const [page, current] = await Promise.all([
      isEdit.value ? Promise.resolve({ content: [] }) : candidaturesApi.rechercher({ statut: 'RETENUE', page: 0, size: 200 }),
      isEdit.value ? propositionsApi.obtenir(props.id) : Promise.resolve(null),
    ])

    candidatures.value = page.content ?? []

    if (current) {
      Object.assign(form, {
        candidatureId: String(current.candidatureId ?? ''),
        typeContratPropose: current.typeContratPropose ?? 'ESSAI',
        salairePropose: current.salairePropose ?? '',
        devise: current.devise ?? 'MGA',
        horairesTravail: current.horairesTravail ?? '',
        lieuTravail: current.lieuTravail ?? '',
        dateDebutPrevue: current.dateDebutPrevue ?? '',
        dureeMois: current.dureeMois ?? '',
        dateLimiteReponse: current.dateLimiteReponse ?? '',
      })
    } else if (props.candidatureId) {
      form.candidatureId = props.candidatureId
    }
  } catch (cause) {
    error.value = cause.message
  } finally {
    loading.value = false
  }
}

async function submit() {
  error.value = ''
  if (!validate()) return

  saving.value = true
  const payload = {
    candidatureId: Number(form.candidatureId),
    typeContratPropose: form.typeContratPropose,
    salairePropose: Number(form.salairePropose),
    devise: form.devise.trim().toUpperCase(),
    horairesTravail: form.horairesTravail.trim(),
    lieuTravail: form.lieuTravail.trim(),
    dateDebutPrevue: form.dateDebutPrevue,
    dureeMois: form.typeContratPropose === 'CDI' ? null : Number(form.dureeMois || 0),
    dateLimiteReponse: form.dateLimiteReponse || null,
  }

  try {
    const result = isEdit.value
      ? await propositionsApi.modifier(props.id, payload)
      : await propositionsApi.creer(payload)
    await router.push({ name: ROUTE_NAMES.PROPOSITION_DETAIL, params: { id: result.id ?? props.id } })
  } catch (cause) {
    error.value = cause.message
    if (cause.fieldErrors) fieldErrors.value = cause.fieldErrors
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<template>
  <section class="page">
    <header>
      <div>
        <p class="eyebrow">Embauche</p>
        <h1>{{ isEdit ? 'Modifier la proposition' : 'Préparer une proposition' }}</h1>
      </div>
    </header>

    <AppAlert v-if="error" variant="error">{{ error }}</AppAlert>
    <p v-if="loading" class="state" role="status">Chargement du formulaire…</p>

    <form v-else class="card" @submit.prevent="submit">
      <label v-if="!isEdit">
        Candidature retenue
        <select v-model="form.candidatureId" required>
          <option value="" disabled>Sélectionner une candidature</option>
          <option v-for="item in candidatures" :key="item.id" :value="String(item.id)">
            {{ item.candidatNom }} — {{ item.offreReference }}
          </option>
        </select>
        <small v-if="fieldErrors.candidatureId">{{ fieldErrors.candidatureId }}</small>
      </label>

      <label>
        Type de contrat
        <select v-model="form.typeContratPropose" required>
          <option v-for="(label, value) in TYPE_CONTRAT_LABELS" :key="value" :value="value">
            {{ label }}
          </option>
        </select>
        <small v-if="fieldErrors.typeContratPropose">{{ fieldErrors.typeContratPropose }}</small>
      </label>

      <label>
        Salaire proposé
        <input v-model.number="form.salairePropose" min="1" type="number" step="0.01" required />
        <small v-if="fieldErrors.salairePropose">{{ fieldErrors.salairePropose }}</small>
      </label>

      <label>
        Devise
        <input v-model.trim="form.devise" maxlength="3" required />
        <small v-if="fieldErrors.devise">{{ fieldErrors.devise }}</small>
      </label>

      <label>
        Horaires de travail
        <input v-model.trim="form.horairesTravail" maxlength="255" required />
        <small v-if="fieldErrors.horairesTravail">{{ fieldErrors.horairesTravail }}</small>
      </label>

      <label>
        Lieu de travail
        <input v-model.trim="form.lieuTravail" maxlength="255" required />
        <small v-if="fieldErrors.lieuTravail">{{ fieldErrors.lieuTravail }}</small>
      </label>

      <label>
        Date de début prévue
        <input v-model="form.dateDebutPrevue" type="date" required />
        <small v-if="fieldErrors.dateDebutPrevue">{{ fieldErrors.dateDebutPrevue }}</small>
      </label>

      <label>
        Durée (mois)
        <input v-model.number="form.dureeMois" type="number" min="1" max="60" />
        <small v-if="fieldErrors.dureeMois">{{ fieldErrors.dureeMois }}</small>
      </label>

      <label>
        Date limite de réponse
        <input v-model="form.dateLimiteReponse" type="date" />
        <small v-if="fieldErrors.dateLimiteReponse">{{ fieldErrors.dateLimiteReponse }}</small>
      </label>

      <footer>
        <RouterLink :to="{ name: ROUTE_NAMES.PROPOSITIONS }">Retour</RouterLink>
        <AppButton type="submit" :loading="saving">
          {{ isEdit ? 'Enregistrer' : 'Créer la proposition' }}
        </AppButton>
      </footer>
    </form>
  </section>
</template>

<style scoped>
.page {
  display: grid;
  gap: var(--space-5);
}

header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

h1 {
  margin: 0;
  color: var(--color-text);
  font-size: var(--font-size-2xl);
}

.eyebrow {
  margin: 0 0 var(--space-1);
  color: var(--color-primary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
}

.card {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-4);
  padding: var(--space-5);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  box-shadow: var(--shadow-card);
}

label {
  display: grid;
  gap: var(--space-2);
  color: var(--color-text);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
}

label:nth-child(1),
label:nth-child(2),
label:nth-child(8),
label:nth-child(9),
footer {
  grid-column: 1 / -1;
}

input,
select {
  min-height: var(--control-height);
  width: 100%;
  padding: 0 var(--space-3);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-text);
  font: inherit;
}

small {
  color: var(--color-danger);
  font-weight: var(--font-weight-normal);
}

footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: var(--space-4);
  padding-top: var(--space-4);
  border-top: 1px solid var(--color-border);
}

a {
  color: var(--color-primary);
  font-weight: var(--font-weight-semibold);
  text-decoration: none;
}

.state {
  padding: var(--space-4);
  color: var(--color-text-muted);
}
@media (max-width: 700px) {
  .card {
    grid-template-columns: 1fr;
  }
}
</style>
