<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { demandesApi } from '@/api/demandes'
import { postesApi } from '@/api/postes'
import AppAlert from '@/components/common/AppAlert.vue'
import AppButton from '@/components/common/AppButton.vue'
import BudgetSummary from '@/components/demande/BudgetSummary.vue'
import { MOTIF_DEMANDE_LABELS, TYPE_DEMANDE_LABELS } from '@/constants/statuts'
import { ROUTE_NAMES } from '@/constants/routeNames'

const props = defineProps({ id: { type: String, default: null } })

const isEdit = computed(() => Boolean(props.id))
const form = reactive({
  posteId: '',
  typeDemande: 'CREATION',
  employeRemplaceId: '',
  effectifDemande: '1',
  motif: 'CROISSANCE',
  justification: '',
  dateSouhaitee: today(),
})
const posts = ref([])
const savedDemand = ref(null)
const loading = ref(true)
const saving = ref(false)
const loadFailed = ref(false)
const error = ref('')
const fieldErrors = ref({})
const allowedMotifs = computed(() =>
  form.typeDemande === 'CREATION'
    ? ['CROISSANCE', 'NOUVEAU_PROJET', 'RESTRUCTURATION', 'RENFORT_TEMPORAIRE', 'AUTRE']
    : ['DEMISSION', 'LICENCIEMENT', 'RETRAITE', 'MUTATION', 'FIN_CONTRAT', 'DECES', 'AUTRE'],
)
const selectedPost = computed(() => posts.value.find((post) => String(post.id) === form.posteId))
const todayValue = today()
const formLocked = computed(() => saving.value || Boolean(savedDemand.value && !isEdit.value))

function today() {
  const now = new Date()
  const local = new Date(now.getTime() - now.getTimezoneOffset() * 60000)
  return local.toISOString().slice(0, 10)
}

function handleTypeChange() {
  if (!allowedMotifs.value.includes(form.motif)) form.motif = allowedMotifs.value[0]
  if (form.typeDemande === 'CREATION') form.employeRemplaceId = ''
  else form.effectifDemande = '1'
}

function messageFor(field) {
  return fieldErrors.value[field] ?? ''
}

function validate() {
  const fields = {}
  if (!form.posteId) fields.posteId = 'Sélectionnez un poste.'
  if (form.typeDemande === 'REMPLACEMENT' && !form.employeRemplaceId) {
    fields.employeRemplaceId = 'Indiquez le salarié à remplacer.'
  }
  const count = Number(form.effectifDemande)
  if (!Number.isInteger(count) || count < 1 || count > 50) {
    fields.effectifDemande = 'L’effectif demandé doit être compris entre 1 et 50.'
  }
  if (form.typeDemande === 'REMPLACEMENT' && count !== 1) {
    fields.effectifDemande = 'Un remplacement concerne exactement un salarié.'
  }
  if (!form.dateSouhaitee || form.dateSouhaitee < todayValue) {
    fields.dateSouhaitee = 'La date souhaitée doit être aujourd’hui ou dans le futur.'
  }
  if (form.motif === 'AUTRE' && !form.justification.trim()) {
    fields.justification = 'Une justification est obligatoire pour le motif « Autre ».'
  }
  fieldErrors.value = fields
  return Object.keys(fields).length === 0
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const [postsPage, demand] = await Promise.all([
      postesApi.rechercher({ page: 0, size: 100, actif: true, sort: 'intitule,asc' }),
      isEdit.value ? demandesApi.obtenir(props.id) : Promise.resolve(null),
    ])
    posts.value = postsPage.content ?? []
    if (demand) {
      Object.assign(form, {
        posteId: String(demand.posteId),
        typeDemande: demand.typeDemande,
        employeRemplaceId: demand.employeRemplaceId ? String(demand.employeRemplaceId) : '',
        effectifDemande: String(demand.effectifDemande),
        motif: demand.motif,
        justification: demand.justification ?? '',
        dateSouhaitee: demand.dateSouhaitee,
      })
      savedDemand.value = demand
      if (!posts.value.some((post) => String(post.id) === form.posteId)) {
        posts.value.push({
          id: demand.posteId,
          intitule: demand.posteIntitule || `Poste #${demand.posteId}`,
          departementNom: demand.departementNom,
          actif: false,
        })
      }
    }
  } catch (cause) {
    error.value = cause.message
    loadFailed.value = true
  } finally {
    loading.value = false
  }
}

async function submit() {
  error.value = ''
  fieldErrors.value = {}
  savedDemand.value = null
  if (!validate()) return

  saving.value = true
  const payload = {
    posteId: Number(form.posteId),
    typeDemande: form.typeDemande,
    employeRemplaceId: form.typeDemande === 'REMPLACEMENT' ? Number(form.employeRemplaceId) : null,
    effectifDemande: Number(form.effectifDemande),
    motif: form.motif,
    justification: form.justification.trim() || null,
    dateSouhaitee: form.dateSouhaitee,
  }

  try {
    savedDemand.value = isEdit.value
      ? await demandesApi.modifier(props.id, payload)
      : await demandesApi.creer(payload)
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
  <section class="demande-form-page">
    <header class="demande-form-page__header">
      <div>
        <p class="demande-form-page__eyebrow">Expression du besoin</p>
        <h1>{{ isEdit ? 'Modifier la demande' : 'Nouvelle demande de recrutement' }}</h1>
        <p>
          Renseignez le besoin de votre équipe. Le département est déterminé automatiquement par le
          poste sélectionné.
        </p>
      </div>
    </header>

    <AppAlert v-if="error" variant="error">{{ error }}</AppAlert>
    <p v-if="loading" class="demande-form-page__state" role="status">Chargement du formulaire…</p>
    <section v-else-if="loadFailed" class="demande-form-page__card">
      <p>Le formulaire n’a pas pu être chargé.</p>
      <AppButton type="button" variant="secondary" @click="load">Réessayer</AppButton>
    </section>
    <form v-else class="demande-form-page__card" @submit.prevent="submit">
      <div class="demande-form-page__grid">
        <label class="demande-form-page__field" for="request-post">
          Poste
          <select
            id="request-post"
            v-model="form.posteId"
            :disabled="formLocked"
            required
            :aria-invalid="Boolean(messageFor('posteId'))"
            :aria-describedby="messageFor('posteId') ? 'request-post-error' : undefined"
          >
            <option value="" disabled>Sélectionner un poste actif</option>
            <option v-for="post in posts" :key="post.id" :value="String(post.id)">
              {{ post.intitule }}{{ post.departementNom ? ` — ${post.departementNom}` : '' }}
              {{ post.actif === false ? ' (inactif — existant)' : '' }}
            </option>
          </select>
          <span
            v-if="messageFor('posteId')"
            id="request-post-error"
            class="demande-form-page__error"
          >
            {{ messageFor('posteId') }}
          </span>
          <small v-if="selectedPost?.placesLibres !== undefined">
            Places libres : {{ selectedPost.placesLibres }}
          </small>
        </label>

        <label class="demande-form-page__field" for="request-type">
          Type de demande
          <select
            id="request-type"
            v-model="form.typeDemande"
            :disabled="formLocked"
            required
            @change="handleTypeChange"
          >
            <option v-for="(label, type) in TYPE_DEMANDE_LABELS" :key="type" :value="type">
              {{ label }}
            </option>
          </select>
        </label>

        <label
          v-if="form.typeDemande === 'REMPLACEMENT'"
          class="demande-form-page__field"
          for="request-replaced-employee"
        >
          Identifiant du salarié remplacé
          <input
            id="request-replaced-employee"
            v-model="form.employeRemplaceId"
            type="number"
            min="1"
            required
            :disabled="formLocked"
            :aria-invalid="Boolean(messageFor('employeRemplaceId'))"
            :aria-describedby="
              messageFor('employeRemplaceId') ? 'request-replaced-employee-error' : undefined
            "
          />
          <small>Le serveur vérifiera que le salarié existe.</small>
          <span
            v-if="messageFor('employeRemplaceId')"
            id="request-replaced-employee-error"
            class="demande-form-page__error"
          >
            {{ messageFor('employeRemplaceId') }}
          </span>
        </label>

        <label class="demande-form-page__field" for="request-count">
          Effectif demandé
          <input
            id="request-count"
            v-model="form.effectifDemande"
            type="number"
            min="1"
            :max="form.typeDemande === 'REMPLACEMENT' ? 1 : 50"
            required
            :disabled="formLocked || form.typeDemande === 'REMPLACEMENT'"
            :aria-invalid="Boolean(messageFor('effectifDemande'))"
            :aria-describedby="messageFor('effectifDemande') ? 'request-count-error' : undefined"
          />
          <span
            v-if="messageFor('effectifDemande')"
            id="request-count-error"
            class="demande-form-page__error"
          >
            {{ messageFor('effectifDemande') }}
          </span>
        </label>

        <label class="demande-form-page__field" for="request-reason">
          Motif
          <select id="request-reason" v-model="form.motif" :disabled="formLocked" required>
            <option v-for="reason in allowedMotifs" :key="reason" :value="reason">
              {{ MOTIF_DEMANDE_LABELS[reason] }}
            </option>
          </select>
        </label>

        <label class="demande-form-page__field" for="request-date">
          Date souhaitée
          <input
            id="request-date"
            v-model="form.dateSouhaitee"
            type="date"
            :min="todayValue"
            required
            :disabled="formLocked"
            :aria-invalid="Boolean(messageFor('dateSouhaitee'))"
            :aria-describedby="messageFor('dateSouhaitee') ? 'request-date-error' : undefined"
          />
          <span
            v-if="messageFor('dateSouhaitee')"
            id="request-date-error"
            class="demande-form-page__error"
          >
            {{ messageFor('dateSouhaitee') }}
          </span>
        </label>

        <label
          class="demande-form-page__field demande-form-page__field--full"
          for="request-reason-detail"
        >
          Justification {{ form.motif === 'AUTRE' ? '(obligatoire)' : '(facultative)' }}
          <textarea
            id="request-reason-detail"
            v-model="form.justification"
            maxlength="2000"
            rows="5"
            :required="form.motif === 'AUTRE'"
            :disabled="formLocked"
            :aria-invalid="Boolean(messageFor('justification'))"
            :aria-describedby="
              messageFor('justification') ? 'request-reason-detail-error' : undefined
            "
          />
          <span
            v-if="messageFor('justification')"
            id="request-reason-detail-error"
            class="demande-form-page__error"
          >
            {{ messageFor('justification') }}
          </span>
        </label>
      </div>

      <div class="demande-form-page__actions">
        <RouterLink
          :to="{
            name: isEdit ? ROUTE_NAMES.DEMANDE_DETAIL : ROUTE_NAMES.DEMANDES,
            params: isEdit ? { id } : {},
          }"
        >
          Retour
        </RouterLink>
        <AppButton type="submit" :loading="saving" :disabled="formLocked">
          {{ isEdit ? 'Enregistrer les modifications' : 'Enregistrer le brouillon' }}
        </AppButton>
      </div>

      <section v-if="savedDemand" class="demande-form-page__saved" aria-live="polite">
        <AppAlert variant="success">
          {{ isEdit ? 'Les modifications ont été enregistrées.' : 'Le brouillon a été créé.' }}
          Référence : <strong>{{ savedDemand.reference }}</strong>
        </AppAlert>
        <BudgetSummary :amount="savedDemand.budgetEstime" />
        <RouterLink
          v-if="savedDemand.id"
          class="demande-form-page__detail-link"
          :to="{ name: ROUTE_NAMES.DEMANDE_DETAIL, params: { id: savedDemand.id } }"
        >
          Consulter le détail et soumettre la demande
        </RouterLink>
      </section>
    </form>
  </section>
</template>

<style scoped>
.demande-form-page {
  display: grid;
  gap: var(--space-5);
}

.demande-form-page__header h1 {
  margin: 0;
  color: var(--color-text);
  font-size: var(--font-size-2xl);
}

.demande-form-page__header p:last-child {
  margin: var(--space-2) 0 0;
  color: var(--color-text-muted);
}

.demande-form-page__eyebrow {
  margin: 0 0 var(--space-1);
  color: var(--color-primary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
}

.demande-form-page__card {
  display: grid;
  gap: var(--space-5);
  padding: var(--space-5);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  box-shadow: var(--shadow-card);
}

.demande-form-page__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-4);
}

.demande-form-page__field {
  min-width: 0;
  display: grid;
  align-content: start;
  gap: var(--space-2);
  color: var(--color-text);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
}

.demande-form-page__field--full {
  grid-column: 1 / -1;
}

.demande-form-page__field input,
.demande-form-page__field select,
.demande-form-page__field textarea {
  width: 100%;
  min-height: var(--control-height);
  padding: var(--space-3);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-text);
  font: inherit;
  font-weight: var(--font-weight-normal);
}

.demande-form-page__field textarea {
  resize: vertical;
}

.demande-form-page__field small {
  color: var(--color-text-muted);
  font-weight: var(--font-weight-normal);
}

.demande-form-page__field-error {
  color: var(--color-danger);
  font-weight: var(--font-weight-normal);
}

.demande-form-page__field input:focus-visible,
.demande-form-page__field select:focus-visible,
.demande-form-page__field textarea:focus-visible {
  outline: 3px solid var(--color-focus-ring);
  outline-offset: 2px;
}

.demande-form-page__actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: var(--space-4);
  padding-top: var(--space-4);
  border-top: 1px solid var(--color-border);
}

.demande-form-page__actions a,
.demande-form-page__detail-link {
  color: var(--color-primary);
  font-weight: var(--font-weight-semibold);
  text-decoration: none;
}

.demande-form-page__actions a:hover,
.demande-form-page__detail-link:hover {
  text-decoration: underline;
}

.demande-form-page__saved {
  display: grid;
  gap: var(--space-3);
}

.demande-form-page__state {
  margin: 0;
  padding: var(--space-8);
  color: var(--color-text-muted);
  text-align: center;
}

@media (max-width: 700px) {
  .demande-form-page__grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .demande-form-page__field--full {
    grid-column: auto;
  }

  .demande-form-page__actions {
    align-items: stretch;
    flex-direction: column-reverse;
  }
}
</style>
