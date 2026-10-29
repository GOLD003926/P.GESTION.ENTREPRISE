<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { contratsApi } from '@/api/contrats'
import { employesApi } from '@/api/employes'
import { postesApi } from '@/api/postes'
import { ROUTE_NAMES } from '@/constants/routeNames'
import { TYPE_CONTRAT_LABELS } from '@/constants/statuts'
import AppAlert from '@/components/common/AppAlert.vue'
import AppButton from '@/components/common/AppButton.vue'

const props = defineProps({
  id: { type: [String, Number], default: null },
  employeId: { type: [String, Number], default: null },
})

const router = useRouter()
const form = reactive({
  employeId: props.employeId ? Number(props.employeId) : '',
  type: 'ESSAI',
  posteId: '',
  managerId: '',
  dateDebut: '',
  dureeMois: 3,
  salaireBase: '',
  devise: 'MGA',
  horairesTravail: '',
  lieuTravail: '',
})
const employes = ref([])
const postes = ref([])
const loading = ref(Boolean(props.id))
const saving = ref(false)
const error = ref('')
const fieldErrors = ref({})

function inferDuration(dateDebut, dateFin, maxMonths) {
  if (!dateDebut || !dateFin) return ''
  const [year, month, day] = dateDebut.split('-').map(Number)
  for (let duration = 1; duration <= maxMonths; duration += 1) {
    const targetMonth = month - 1 + duration
    const lastDay = new Date(Date.UTC(year, targetMonth + 1, 0)).getUTCDate()
    const expectedEnd = new Date(Date.UTC(year, targetMonth, Math.min(day, lastDay) - 1))
      .toISOString()
      .slice(0, 10)
    if (expectedEnd === dateFin) return duration
  }
  return ''
}

watch(
  () => form.type,
  (type) => {
    if (type === 'CDI') form.dureeMois = ''
    else if (!form.dureeMois) form.dureeMois = 3
    else form.dureeMois = Math.min(form.dureeMois, type === 'ESSAI' ? 6 : 24)
  },
)

async function loadOptions() {
  try {
    const [employeePage, postPage] = await Promise.all([
      employesApi.rechercher({ page: 0, size: 100, sort: 'nom,asc' }),
      postesApi.rechercher({ actif: true, page: 0, size: 100, sort: 'intitule,asc' }),
    ])
    employes.value = employeePage.content ?? []
    postes.value = postPage.content ?? []
  } catch (cause) {
    error.value = cause.message
  }
}

async function loadContract() {
  if (!props.id) return
  try {
    const contract = await contratsApi.obtenir(props.id)
    const dureeMois =
      contract.type === 'CDI'
        ? ''
        : inferDuration(
            contract.dateDebut,
            contract.dateFinPrevue,
            contract.type === 'ESSAI' ? 6 : 24,
          )
    if (contract.type !== 'CDI' && !dureeMois) {
      error.value =
        'La durée du contrat n’a pas pu être déduite des dates. Renseignez-la avant l’enregistrement.'
    }
    Object.assign(form, {
      employeId: contract.employeId,
      type: contract.type,
      posteId: contract.posteId,
      managerId: contract.managerId ?? '',
      dateDebut: contract.dateDebut,
      dureeMois,
      salaireBase: contract.salaireBase,
      devise: contract.devise,
      horairesTravail: contract.horairesTravail,
      lieuTravail: contract.lieuTravail,
    })
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
    type: form.type,
    posteId: Number(form.posteId),
    managerId: form.managerId ? Number(form.managerId) : null,
    dateDebut: form.dateDebut,
    dureeMois: form.type === 'CDI' ? null : Number(form.dureeMois),
    salaireBase: Number(form.salaireBase),
    devise: form.devise.trim().toUpperCase(),
    horairesTravail: form.horairesTravail.trim(),
    lieuTravail: form.lieuTravail.trim(),
  }

  try {
    const contract = props.id
      ? await contratsApi.modifier(props.id, payload)
      : await contratsApi.creer({ ...payload, employeId: Number(form.employeId) })
    await router.push({ name: ROUTE_NAMES.CONTRAT_DETAIL, params: { id: contract.id } })
  } catch (cause) {
    error.value = cause.message
    fieldErrors.value = cause.fieldErrors ?? {}
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  await loadOptions()
  await loadContract()
})
</script>

<template>
  <section class="page">
    <header>
      <p class="eyebrow">Gestion des contrats</p>
      <h1>{{ id ? 'Modifier un contrat brouillon' : 'Créer un contrat' }}</h1>
      <p>Les renouvellements et transformations créent toujours un nouveau contrat.</p>
    </header>

    <AppAlert v-if="error" variant="error">{{ error }}</AppAlert>
    <p v-if="loading" role="status">Chargement du contrat…</p>
    <form v-else class="form-card" @submit.prevent="submit">
      <label v-if="!id"
        >Employé
        <select v-model="form.employeId" required>
          <option value="" disabled>Sélectionner un employé</option>
          <option v-for="employee in employes" :key="employee.id" :value="employee.id">
            {{ employee.prenom }} {{ employee.nom }} · {{ employee.matricule }}
          </option>
        </select>
        <small v-if="fieldErrors.employeId">{{ fieldErrors.employeId }}</small>
      </label>
      <label
        >Type de contrat
        <select v-model="form.type" required>
          <option v-for="(label, type) in TYPE_CONTRAT_LABELS" :key="type" :value="type">
            {{ label }}
          </option>
        </select>
        <small v-if="fieldErrors.type">{{ fieldErrors.type }}</small>
      </label>
      <label
        >Poste
        <select v-model="form.posteId" required>
          <option value="" disabled>Sélectionner un poste actif</option>
          <option v-for="post in postes" :key="post.id" :value="post.id">
            {{ post.intitule }}
          </option>
        </select>
        <small v-if="fieldErrors.posteId">{{ fieldErrors.posteId }}</small>
      </label>
      <label
        >Manager (facultatif)
        <select v-model="form.managerId">
          <option value="">Aucun manager</option>
          <option v-for="employee in employes" :key="employee.id" :value="employee.id">
            {{ employee.prenom }} {{ employee.nom }} · {{ employee.matricule }}
          </option>
        </select>
      </label>
      <label
        >Date de début
        <input v-model="form.dateDebut" type="date" required />
        <small v-if="fieldErrors.dateDebut">{{ fieldErrors.dateDebut }}</small>
      </label>
      <label v-if="form.type !== 'CDI'"
        >Durée (mois)
        <input
          v-model.number="form.dureeMois"
          type="number"
          min="1"
          :max="form.type === 'ESSAI' ? 6 : 24"
          required
        />
        <small v-if="fieldErrors.dureeMois">{{ fieldErrors.dureeMois }}</small>
      </label>
      <label
        >Salaire de base
        <input
          v-model="form.salaireBase"
          type="number"
          min="0.01"
          step="0.01"
          required
          inputmode="decimal"
        />
        <small v-if="fieldErrors.salaireBase">{{ fieldErrors.salaireBase }}</small>
      </label>
      <label
        >Devise
        <input v-model.trim="form.devise" maxlength="3" minlength="3" required />
        <small v-if="fieldErrors.devise">{{ fieldErrors.devise }}</small>
      </label>
      <label
        >Horaires de travail
        <input v-model.trim="form.horairesTravail" maxlength="255" required />
        <small v-if="fieldErrors.horairesTravail">{{ fieldErrors.horairesTravail }}</small>
      </label>
      <label
        >Lieu de travail
        <input v-model.trim="form.lieuTravail" maxlength="255" required />
        <small v-if="fieldErrors.lieuTravail">{{ fieldErrors.lieuTravail }}</small>
      </label>
      <footer>
        <RouterLink
          :to="
            id
              ? { name: ROUTE_NAMES.CONTRAT_DETAIL, params: { id } }
              : { name: ROUTE_NAMES.CONTRATS }
          "
        >
          Annuler
        </RouterLink>
        <AppButton type="submit" :loading="saving">
          {{ id ? 'Enregistrer le brouillon' : 'Créer le brouillon' }}
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
.form-card input,
.form-card select {
  min-height: var(--control-height);
  padding: 0 var(--space-3);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-text);
  font: inherit;
}
.form-card small {
  color: var(--color-danger);
  font-weight: var(--font-weight-normal);
}
footer {
  display: flex;
  grid-column: 1 / -1;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  margin-top: var(--space-2);
}
footer a {
  color: var(--color-text-muted);
}
@media (max-width: 680px) {
  .form-card {
    grid-template-columns: 1fr;
  }
  footer {
    grid-column: auto;
  }
}
</style>
