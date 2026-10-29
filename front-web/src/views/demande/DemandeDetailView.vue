<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { demandesApi } from '@/api/demandes'
import { validationsApi } from '@/api/validations'
import AppAlert from '@/components/common/AppAlert.vue'
import AppButton from '@/components/common/AppButton.vue'
import BudgetSummary from '@/components/demande/BudgetSummary.vue'
import DemandeStatusStepper from '@/components/demande/DemandeStatusStepper.vue'
import ValidationActionModal from '@/components/demande/ValidationActionModal.vue'
import ValidationTimeline from '@/components/demande/ValidationTimeline.vue'
import { ROLES } from '@/constants/roles'
import { ROUTE_NAMES } from '@/constants/routeNames'
import {
  MOTIF_DEMANDE_LABELS,
  STATUT_DEMANDE_LABELS,
  STATUT_DEMANDE_VARIANTS,
  TYPE_DEMANDE_LABELS,
} from '@/constants/statuts'
import { useAuthStore } from '@/stores/auth'

const props = defineProps({ id: { type: String, required: true } })
const router = useRouter()
const auth = useAuthStore()
const demande = ref(null)
const validations = ref([])
const historique = ref([])
const loading = ref(true)
const actionLoading = ref(false)
const error = ref('')
const actionError = ref('')
const validationModalOpen = ref(false)
const cancelDialogOpen = ref(false)
const cancelMotif = ref('')
const cancelMotifError = ref('')
const cancelFieldError = ref('')
const cancelDialog = reactive({ submitting: false })

const isOwner = computed(
  () => demande.value && String(demande.value.demandeurId) === String(auth.user?.id),
)
const canEdit = computed(
  () =>
    auth.role === ROLES.MANAGER &&
    isOwner.value &&
    ['BROUILLON', 'RETOURNEE'].includes(demande.value?.statut),
)
const canSubmit = computed(
  () =>
    auth.role === ROLES.MANAGER &&
    isOwner.value &&
    ['BROUILLON', 'RETOURNEE'].includes(demande.value?.statut),
)
const canCancel = computed(() => {
  if (!demande.value) return false
  if (
    auth.role === ROLES.MANAGER &&
    isOwner.value &&
    ['BROUILLON', 'RETOURNEE'].includes(demande.value.statut)
  ) {
    return true
  }
  return auth.role === ROLES.RH && ['RETOURNEE', 'EN_RECRUTEMENT'].includes(demande.value.statut)
})
const canDecide = computed(() => {
  const currentRole = demande.value?.etapeCourante
  const statusByRole = {
    RH: 'EN_VALIDATION_RH',
    FINANCE: 'EN_VALIDATION_FINANCE',
    DG: 'EN_VALIDATION_DG',
  }
  return (
    [ROLES.RH, ROLES.FINANCE, ROLES.DG].includes(auth.role) &&
    !isOwner.value &&
    currentRole === auth.role &&
    demande.value?.statut === statusByRole[currentRole]
  )
})

function formatDate(value) {
  if (!value) return '—'
  const date = new Date(value)
  return new Intl.DateTimeFormat('fr-FR', {
    dateStyle: 'medium',
    ...(value.includes?.('T') ? { timeStyle: 'short' } : {}),
  }).format(date)
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const [detail, history, validationHistory] = await Promise.all([
      demandesApi.obtenir(props.id),
      demandesApi.historique(props.id),
      validationsApi.historique(props.id),
    ])
    demande.value = detail
    historique.value = history ?? []
    validations.value = validationHistory ?? []
  } catch (cause) {
    error.value = cause.message
  } finally {
    loading.value = false
  }
}

async function submitDemand() {
  actionLoading.value = true
  actionError.value = ''
  try {
    await demandesApi.soumettre(props.id)
    await load()
  } catch (cause) {
    actionError.value = cause.message
  } finally {
    actionLoading.value = false
  }
}

async function cancelDemand() {
  cancelMotifError.value = ''
  if (!cancelMotif.value.trim()) {
    cancelMotifError.value = 'Le motif d’annulation est obligatoire.'
    return
  }
  cancelDialog.submitting = true
  actionError.value = ''
  try {
    await demandesApi.annuler(props.id, { motif: cancelMotif.value.trim() })
    cancelDialogOpen.value = false
    cancelMotif.value = ''
    await load()
  } catch (cause) {
    actionError.value = cause.message
    cancelFieldError.value = cause.fieldErrors?.motif ?? ''
  } finally {
    cancelDialog.submitting = false
  }
}

async function saveDecision(payload) {
  actionLoading.value = true
  actionError.value = ''
  try {
    await validationsApi.decider(props.id, payload)
    validationModalOpen.value = false
    await load()
  } catch (cause) {
    actionError.value = cause.message
  } finally {
    actionLoading.value = false
  }
}

function goBack() {
  router.push({
    name: auth.role === ROLES.MANAGER ? ROUTE_NAMES.DEMANDES : ROUTE_NAMES.VALIDATIONS,
  })
}

function openCancelDialog() {
  actionError.value = ''
  cancelMotifError.value = ''
  cancelFieldError.value = ''
  cancelMotif.value = ''
  cancelDialogOpen.value = true
}

onMounted(load)
</script>

<template>
  <section class="demande-detail">
    <p v-if="loading" class="demande-detail__state" role="status">Chargement de la demande…</p>
    <div v-else-if="error && !demande" class="demande-detail__error">
      <AppAlert variant="error">{{ error }}</AppAlert>
      <AppButton type="button" variant="secondary" @click="load">Réessayer</AppButton>
      <AppButton type="button" variant="secondary" @click="goBack">Retour</AppButton>
    </div>

    <template v-else-if="demande">
      <header class="demande-detail__header">
        <div>
          <button type="button" class="demande-detail__back" @click="goBack">← Retour</button>
          <p class="demande-detail__eyebrow">Demande de recrutement</p>
          <div class="demande-detail__title-row">
            <h1>{{ demande.reference }}</h1>
            <span
              class="demande-detail__status"
              :class="`demande-detail__status--${STATUT_DEMANDE_VARIANTS[demande.statut] ?? 'neutral'}`"
            >
              {{ STATUT_DEMANDE_LABELS[demande.statut] ?? demande.statut }}
            </span>
          </div>
          <p>{{ demande.posteIntitule }} · {{ demande.departementNom }}</p>
        </div>

        <div class="demande-detail__actions">
          <RouterLink
            v-if="canEdit"
            class="demande-detail__edit"
            :to="{ name: ROUTE_NAMES.DEMANDE_EDIT, params: { id: demande.id } }"
          >
            Modifier
          </RouterLink>
          <AppButton v-if="canSubmit" :loading="actionLoading" @click="submitDemand">
            Soumettre
          </AppButton>
          <AppButton v-if="canDecide" :disabled="actionLoading" @click="validationModalOpen = true">
            Décider
          </AppButton>
          <AppButton
            v-if="canCancel"
            variant="secondary"
            :disabled="actionLoading"
            @click="openCancelDialog"
          >
            Annuler la demande
          </AppButton>
        </div>
      </header>

      <AppAlert v-if="error" variant="error">{{ error }}</AppAlert>
      <AppAlert v-if="actionError && !validationModalOpen && !cancelDialogOpen" variant="error">
        {{ actionError }}
      </AppAlert>

      <section class="demande-detail__card">
        <div class="demande-detail__summary">
          <div>
            <h2>Suivi des validations</h2>
            <p>
              Étape actuelle :
              <strong>{{ demande.etapeCourante || STATUT_DEMANDE_LABELS[demande.statut] }}</strong>
            </p>
          </div>
          <span v-if="demande.cycleCourant" class="demande-detail__cycle">
            Cycle {{ demande.cycleCourant }}
          </span>
        </div>
        <DemandeStatusStepper :demande="demande" :validations="validations" />
      </section>

      <div class="demande-detail__columns">
        <section class="demande-detail__card">
          <header class="demande-detail__section-header">
            <div>
              <h2>Informations de la demande</h2>
              <p>Créée le {{ formatDate(demande.createdAt) }}</p>
            </div>
          </header>
          <dl class="demande-detail__facts">
            <div>
              <dt>Demandeur</dt>
              <dd>{{ demande.demandeurNom || `Utilisateur #${demande.demandeurId}` }}</dd>
            </div>
            <div>
              <dt>Type</dt>
              <dd>{{ TYPE_DEMANDE_LABELS[demande.typeDemande] ?? demande.typeDemande }}</dd>
            </div>
            <div>
              <dt>Motif</dt>
              <dd>{{ MOTIF_DEMANDE_LABELS[demande.motif] ?? demande.motif }}</dd>
            </div>
            <div>
              <dt>Effectif demandé</dt>
              <dd>{{ demande.effectifDemande }}</dd>
            </div>
            <div>
              <dt>Effectif pourvu</dt>
              <dd>{{ demande.effectifPourvu }}</dd>
            </div>
            <div>
              <dt>Date souhaitée</dt>
              <dd>{{ formatDate(demande.dateSouhaitee) }}</dd>
            </div>
            <div v-if="demande.employeRemplaceId">
              <dt>Salarié remplacé</dt>
              <dd>Identifiant #{{ demande.employeRemplaceId }}</dd>
            </div>
            <div v-if="demande.justification" class="demande-detail__fact--full">
              <dt>Justification</dt>
              <dd class="demande-detail__justification">{{ demande.justification }}</dd>
            </div>
          </dl>
          <BudgetSummary :amount="demande.budgetEstime" />
        </section>

        <section class="demande-detail__card">
          <header class="demande-detail__section-header">
            <div>
              <h2>Validations par cycle</h2>
              <p>Les décisions et commentaires sont conservés à chaque retour.</p>
            </div>
          </header>
          <ValidationTimeline :validations="validations" />
        </section>
      </div>

      <section class="demande-detail__card">
        <header class="demande-detail__section-header">
          <div>
            <h2>Historique des statuts</h2>
            <p>Évolution de la demande et commentaires associés.</p>
          </div>
        </header>
        <ol v-if="historique.length" class="demande-detail__history">
          <li v-for="(item, index) in historique" :key="item.id ?? index">
            <strong>
              {{ STATUT_DEMANDE_LABELS[item.nouveauStatut] ?? item.nouveauStatut ?? 'Mise à jour' }}
            </strong>
            <span>{{ formatDate(item.dateChangement) }} · {{ item.auteur || 'Système' }}</span>
            <p v-if="item.commentaire">{{ item.commentaire }}</p>
          </li>
        </ol>
        <p v-else class="demande-detail__empty">Aucun changement de statut enregistré.</p>
      </section>

      <div v-if="cancelDialogOpen" class="demande-detail__overlay">
        <section
          class="demande-detail__dialog"
          role="dialog"
          aria-modal="true"
          aria-labelledby="cancel-dialog-title"
        >
          <h2 id="cancel-dialog-title">Annuler la demande</h2>
          <p>Cette action change le statut de la demande. Indiquez le motif de l’annulation.</p>
          <AppAlert v-if="actionError" variant="error">{{ actionError }}</AppAlert>
          <label for="cancel-reason">
            Motif d’annulation
            <textarea
              id="cancel-reason"
              v-model="cancelMotif"
              maxlength="500"
              rows="4"
              required
              :disabled="cancelDialog.submitting"
              :aria-invalid="Boolean(cancelMotifError || cancelFieldError)"
              :aria-describedby="
                cancelMotifError || cancelFieldError ? 'cancel-reason-error' : undefined
              "
            />
            <span
              v-if="cancelMotifError || cancelFieldError"
              id="cancel-reason-error"
              class="demande-detail__field-error"
            >
              {{ cancelMotifError || cancelFieldError }}
            </span>
          </label>
          <footer>
            <AppButton
              type="button"
              variant="secondary"
              :disabled="cancelDialog.submitting"
              @click="cancelDialogOpen = false"
            >
              Retour
            </AppButton>
            <AppButton :loading="cancelDialog.submitting" @click="cancelDemand">
              Confirmer l’annulation
            </AppButton>
          </footer>
        </section>
      </div>

      <ValidationActionModal
        :open="validationModalOpen"
        :loading="actionLoading"
        :error="actionError"
        @close="validationModalOpen = false"
        @submit="saveDecision"
      />
    </template>
  </section>
</template>

<style scoped>
.demande-detail {
  display: grid;
  gap: var(--space-5);
}

.demande-detail__header,
.demande-detail__summary,
.demande-detail__section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
}

.demande-detail__header {
  align-items: flex-end;
}

.demande-detail__back {
  display: inline-flex;
  margin-bottom: var(--space-3);
  padding: 0;
  border: 0;
  background: transparent;
  color: var(--color-primary);
  font: inherit;
  font-weight: var(--font-weight-semibold);
  cursor: pointer;
}

.demande-detail__eyebrow {
  margin: 0 0 var(--space-1);
  color: var(--color-primary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
}

.demande-detail__title-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-3);
}

.demande-detail__title-row h1 {
  margin: 0;
  color: var(--color-text);
  font-size: var(--font-size-2xl);
}

.demande-detail__header p:last-child {
  margin: var(--space-2) 0 0;
  color: var(--color-text-muted);
}

.demande-detail__status,
.demande-detail__cycle {
  display: inline-flex;
  padding: var(--space-1) var(--space-3);
  border-radius: 999px;
  background: var(--color-bg);
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
}

.demande-detail__status--info {
  background: var(--color-info-soft);
  color: var(--color-info);
}

.demande-detail__status--warning {
  background: var(--color-warning-soft);
  color: var(--color-warning);
}

.demande-detail__status--danger {
  background: var(--color-danger-soft);
  color: var(--color-danger);
}

.demande-detail__status--success {
  background: var(--color-success-soft);
  color: var(--color-success);
}

.demande-detail__actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: var(--space-2);
}

.demande-detail__edit {
  min-height: var(--control-height);
  display: inline-flex;
  align-items: center;
  padding: 0 var(--space-4);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-text);
  font-weight: var(--font-weight-semibold);
  text-decoration: none;
}

.demande-detail__card {
  display: grid;
  gap: var(--space-4);
  padding: var(--space-5);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  box-shadow: var(--shadow-card);
}

.demande-detail__summary h2,
.demande-detail__section-header h2 {
  margin: 0;
  color: var(--color-text);
  font-size: var(--font-size-lg);
}

.demande-detail__summary p,
.demande-detail__section-header p {
  margin: var(--space-1) 0 0;
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}

.demande-detail__columns {
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(0, 0.85fr);
  align-items: start;
  gap: var(--space-4);
}

.demande-detail__facts {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-4);
  margin: 0;
}

.demande-detail__facts div {
  min-width: 0;
}

.demande-detail__facts dt {
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}

.demande-detail__facts dd {
  margin: var(--space-1) 0 0;
  color: var(--color-text);
  font-weight: var(--font-weight-semibold);
  overflow-wrap: anywhere;
}

.demande-detail__facts .demande-detail__fact--full {
  grid-column: 1 / -1;
}

.demande-detail__facts .demande-detail__justification {
  font-weight: var(--font-weight-normal);
  white-space: pre-wrap;
}

.demande-detail__history {
  display: grid;
  gap: var(--space-3);
  margin: 0;
  padding: 0;
  list-style: none;
}

.demande-detail__history li {
  display: grid;
  gap: var(--space-1);
  padding-left: var(--space-4);
  border-left: 2px solid var(--color-info-border);
}

.demande-detail__history li strong {
  color: var(--color-text);
}

.demande-detail__history li span,
.demande-detail__history li p,
.demande-detail__empty {
  margin: 0;
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}

.demande-detail__state {
  margin: 0;
  padding: var(--space-8);
  color: var(--color-text-muted);
  text-align: center;
}

.demande-detail__error {
  display: grid;
  justify-items: start;
  gap: var(--space-3);
}

.demande-detail__overlay {
  position: fixed;
  z-index: 90;
  inset: 0;
  display: grid;
  place-items: center;
  padding: var(--space-4);
  background: var(--color-overlay);
}

.demande-detail__dialog {
  width: min(100%, 520px);
  display: grid;
  gap: var(--space-4);
  padding: var(--space-5);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  box-shadow: var(--shadow-card);
}

.demande-detail__dialog h2,
.demande-detail__dialog p {
  margin: 0;
}

.demande-detail__dialog h2 {
  color: var(--color-text);
  font-size: var(--font-size-xl);
}

.demande-detail__dialog p {
  color: var(--color-text-muted);
}

.demande-detail__dialog label {
  display: grid;
  gap: var(--space-2);
  color: var(--color-text);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
}

.demande-detail__dialog textarea {
  width: 100%;
  padding: var(--space-3);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-text);
  font: inherit;
}

.demande-detail__field-error {
  color: var(--color-danger);
  font-weight: var(--font-weight-normal);
}

.demande-detail__dialog footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
}

.demande-detail__back:focus-visible,
.demande-detail__edit:focus-visible,
.demande-detail__dialog textarea:focus-visible {
  outline: 3px solid var(--color-focus-ring);
  outline-offset: 2px;
}

@media (max-width: 900px) {
  .demande-detail__columns {
    grid-template-columns: minmax(0, 1fr);
  }
}

@media (max-width: 700px) {
  .demande-detail__header,
  .demande-detail__summary,
  .demande-detail__section-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .demande-detail__actions {
    width: 100%;
    justify-content: stretch;
  }

  .demande-detail__actions > * {
    flex: 1;
  }
}

@media (max-width: 520px) {
  .demande-detail__facts {
    grid-template-columns: minmax(0, 1fr);
  }

  .demande-detail__facts .demande-detail__fact--full {
    grid-column: auto;
  }

  .demande-detail__dialog footer {
    flex-direction: column-reverse;
  }
}
</style>
