<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { contratsApi } from '@/api/contrats'
import ContratTimeline from '@/components/contrat/ContratTimeline.vue'
import RenouvellementModal from '@/components/contrat/RenouvellementModal.vue'
import TransformationModal from '@/components/contrat/TransformationModal.vue'
import {
  MOTIF_FIN_CONTRAT_LABELS,
  STATUT_CONTRAT_LABELS,
  TYPE_CONTRAT_LABELS,
} from '@/constants/statuts'
import { ROLES } from '@/constants/roles'
import { ROUTE_NAMES } from '@/constants/routeNames'
import { useAuthStore } from '@/stores/auth'
import AppAlert from '@/components/common/AppAlert.vue'
import AppButton from '@/components/common/AppButton.vue'
import { documentsApi } from '@/api/documents'
import { downloadBlob } from '@/utils/download'

const props = defineProps({ id: { type: String, required: true } })
const router = useRouter()
const auth = useAuthStore()
const contrat = ref(null)
const chaine = ref([])
const historique = ref([])
const loading = ref(true)
const busy = ref(false)
const error = ref('')
const actionError = ref('')
const success = ref('')
const renewalOpen = ref(false)
const transformationOpen = ref(false)
const signatureOpen = ref(false)
const terminationOpen = ref(false)
const signatureFile = ref(null)
const dateSignature = ref(new Date().toISOString().slice(0, 10))
const fin = ref({
  motifFin: 'ECHEANCE',
  dateFin: new Date().toISOString().slice(0, 10),
  commentaire: '',
})

const roleCanRead = computed(() => [ROLES.RH, ROLES.MANAGER, ROLES.PAIE].includes(auth.role))
const isHr = computed(() => auth.role === ROLES.RH)
const canRenew = computed(
  () =>
    isHr.value &&
    contrat.value?.statut === 'ACTIF' &&
    ['ESSAI', 'CDD'].includes(contrat.value.type) &&
    contrat.value.numeroRenouvellement < 1,
)
const canTransform = computed(
  () =>
    isHr.value &&
    contrat.value?.statut === 'ACTIF' &&
    ['ESSAI', 'CDD'].includes(contrat.value.type),
)
const canTerminate = computed(() => isHr.value && contrat.value?.statut === 'ACTIF')
const canCancel = computed(
  () => isHr.value && ['BROUILLON', 'A_SIGNER'].includes(contrat.value?.statut),
)
const canEdit = computed(() => isHr.value && contrat.value?.statut === 'BROUILLON')

function formatDate(value) {
  return value ? new Date(`${value}T00:00:00`).toLocaleDateString('fr-FR') : '—'
}
function formatSalary(value, currency) {
  return new Intl.NumberFormat('fr-FR', { style: 'currency', currency: currency || 'MGA' }).format(
    value,
  )
}
function receiveSignature(event) {
  signatureFile.value = event.target.files?.[0] ?? null
}

async function load() {
  loading.value = true
  error.value = ''
  actionError.value = ''
  try {
    const detail = await contratsApi.obtenir(props.id)
    contrat.value = detail
    const requests = [
      contratsApi.rechercher({
        employeId: detail.employeId,
        page: 0,
        size: 100,
        sort: 'dateDebut,asc',
      }),
    ]
    if (isHr.value) requests.push(contratsApi.historique(props.id))
    const [contractsPage, history] = await Promise.all(requests)
    chaine.value = contractsPage.content ?? []
    historique.value = isHr.value ? (history ?? []) : []
  } catch (cause) {
    error.value = cause.message
  } finally {
    loading.value = false
  }
}

async function runAction(operation, ...args) {
  busy.value = true
  actionError.value = ''
  success.value = ''
  try {
    await contratsApi[operation](props.id, ...args)
    renewalOpen.value = false
    transformationOpen.value = false
    signatureOpen.value = false
    terminationOpen.value = false
    success.value = 'L’opération a été effectuée.'
    await load()
  } catch (cause) {
    actionError.value = cause.message
  } finally {
    busy.value = false
  }
}

async function submitSignature() {
  if (!signatureFile.value) {
    actionError.value = 'Sélectionnez le document signé à déposer.'
    return
  }
  busy.value = true
  actionError.value = ''
  success.value = ''
  try {
    await contratsApi.enregistrerSignature(props.id, signatureFile.value, dateSignature.value)
    signatureOpen.value = false
    signatureFile.value = null
    success.value = 'Le document signé a été enregistré.'
    await load()
  } catch (cause) {
    actionError.value = cause.message
  } finally {
    busy.value = false
  }
}

async function createSuccessor(operation, payload) {
  busy.value = true
  actionError.value = ''
  success.value = ''
  try {
    const created = await contratsApi[operation](props.id, payload)
    await router.push({ name: ROUTE_NAMES.CONTRAT_DETAIL, params: { id: created.id } })
  } catch (cause) {
    actionError.value =
      operation === 'renouveler' && cause.status === 422
        ? `Le renouvellement n’est pas possible : ${cause.message}`
        : cause.message
    busy.value = false
  }
}

function cancelContract() {
  if (window.confirm('Annuler ce contrat ? Cette action ne peut pas être annulée.')) {
    runAction('annuler')
  }
}

async function downloadSignedDocument() {
  if (!contrat.value?.documentSigneId) return
  busy.value = true
  actionError.value = ''
  try {
    const response = await documentsApi.telecharger(contrat.value.documentSigneId)
    const disposition = response.headers['content-disposition'] ?? ''
    const filename =
      disposition.match(/filename="?([^";]+)"?/i)?.[1] ?? `${contrat.value.numero}-signe`
    downloadBlob(response.data, filename)
  } catch (cause) {
    actionError.value = cause.message
  } finally {
    busy.value = false
  }
}

onMounted(load)
</script>

<template>
  <section class="page">
    <p class="eyebrow">
      <RouterLink
        :to="{ name: isHr || roleCanRead ? ROUTE_NAMES.CONTRATS : ROUTE_NAMES.MES_CONTRATS }"
        >Contrats</RouterLink
      >
      / Détail
    </p>
    <p v-if="loading" role="status">Chargement du contrat…</p>
    <AppAlert v-if="error" variant="error">{{ error }}</AppAlert>
    <template v-if="contrat">
      <AppAlert v-if="actionError && !renewalOpen && !transformationOpen" variant="error">{{
        actionError
      }}</AppAlert>
      <AppAlert v-if="success" variant="success">{{ success }}</AppAlert>

      <article class="panel">
        <header>
          <div>
            <p class="eyebrow">
              {{ contrat.numero }} · {{ STATUT_CONTRAT_LABELS[contrat.statut] ?? contrat.statut }}
            </p>
            <h1>{{ TYPE_CONTRAT_LABELS[contrat.type] ?? contrat.type }}</h1>
            <p>
              {{ contrat.employeNom }}
              <span v-if="contrat.matricule">· {{ contrat.matricule }}</span>
            </p>
          </div>
          <nav v-if="isHr">
            <RouterLink
              v-if="canEdit"
              class="link-button"
              :to="{ name: ROUTE_NAMES.CONTRAT_EDIT, params: { id } }"
              >Modifier le brouillon</RouterLink
            >
            <AppButton
              v-if="contrat.statut === 'BROUILLON'"
              :loading="busy"
              @click="runAction('envoyerPourSignature')"
              >Envoyer pour signature</AppButton
            >
            <AppButton
              v-if="contrat.statut === 'A_SIGNER'"
              :loading="busy"
              @click="signatureOpen = !signatureOpen"
              >Déposer le contrat signé</AppButton
            >
            <AppButton v-if="canRenew" variant="secondary" @click="renewalOpen = true"
              >Renouveler</AppButton
            >
            <AppButton v-if="canTransform" variant="secondary" @click="transformationOpen = true"
              >Transformer</AppButton
            >
            <AppButton
              v-if="canTerminate"
              variant="secondary"
              @click="terminationOpen = !terminationOpen"
              >Terminer</AppButton
            >
            <AppButton v-if="canCancel" variant="secondary" @click="cancelContract"
              >Annuler</AppButton
            >
          </nav>
        </header>

        <dl>
          <div>
            <dt>Employé</dt>
            <dd>
              {{ contrat.employeNom }}
              <span v-if="contrat.matricule">· {{ contrat.matricule }}</span>
            </dd>
          </div>
          <div>
            <dt>Poste</dt>
            <dd>{{ contrat.posteIntitule || '—' }}</dd>
          </div>
          <div>
            <dt>Début</dt>
            <dd>{{ formatDate(contrat.dateDebut) }}</dd>
          </div>
          <div>
            <dt>Fin prévue</dt>
            <dd>{{ formatDate(contrat.dateFinPrevue) }}</dd>
          </div>
          <div>
            <dt>Fin réelle</dt>
            <dd>{{ formatDate(contrat.dateFinReelle) }}</dd>
          </div>
          <div>
            <dt>Salaire de base</dt>
            <dd>{{ formatSalary(contrat.salaireBase, contrat.devise) }}</dd>
          </div>
          <div>
            <dt>Horaires</dt>
            <dd>{{ contrat.horairesTravail }}</dd>
          </div>
          <div>
            <dt>Lieu de travail</dt>
            <dd>{{ contrat.lieuTravail }}</dd>
          </div>
          <div>
            <dt>Renouvellement</dt>
            <dd>{{ contrat.numeroRenouvellement }}</dd>
          </div>
          <div v-if="contrat.joursRestants != null">
            <dt>Échéance</dt>
            <dd>
              {{
                contrat.joursRestants < 0
                  ? `Dépassée de ${Math.abs(contrat.joursRestants)} jour(s)`
                  : contrat.joursRestants === 0
                    ? 'Aujourd’hui'
                    : `Dans ${contrat.joursRestants} jour(s)`
              }}
            </dd>
          </div>
          <div v-if="contrat.dateSignature">
            <dt>Date de signature</dt>
            <dd>{{ formatDate(contrat.dateSignature) }}</dd>
          </div>
          <div v-if="contrat.motifFin">
            <dt>Motif de fin</dt>
            <dd>{{ MOTIF_FIN_CONTRAT_LABELS[contrat.motifFin] ?? contrat.motifFin }}</dd>
          </div>
        </dl>

        <form v-if="signatureOpen" class="inline-form" @submit.prevent="submitSignature">
          <h2>Enregistrer la signature</h2>
          <label>Date de signature<input v-model="dateSignature" type="date" required /></label>
          <label
            >Document signé (PDF)<input
              type="file"
              accept="application/pdf,.pdf"
              required
              @change="receiveSignature"
          /></label>
          <footer>
            <AppButton type="button" variant="secondary" @click="signatureOpen = false"
              >Annuler</AppButton
            ><AppButton type="submit" :loading="busy">Déposer le document</AppButton>
          </footer>
        </form>

        <form
          v-if="terminationOpen"
          class="inline-form"
          @submit.prevent="
            runAction('terminer', { ...fin, commentaire: fin.commentaire.trim() || null })
          "
        >
          <h2>Terminer le contrat</h2>
          <label
            >Motif de fin<select v-model="fin.motifFin" required>
              <option
                v-for="(label, value) in MOTIF_FIN_CONTRAT_LABELS"
                :key="value"
                :value="value"
              >
                {{ label }}
              </option>
            </select></label
          >
          <label>Date de fin<input v-model="fin.dateFin" type="date" required /></label>
          <label
            >Commentaire<textarea v-model.trim="fin.commentaire" maxlength="500" rows="3" />
          </label>
          <footer>
            <AppButton type="button" variant="secondary" @click="terminationOpen = false"
              >Annuler</AppButton
            ><AppButton type="submit" :loading="busy">Confirmer la fin du contrat</AppButton>
          </footer>
        </form>

        <div v-if="contrat.documentSigneId" class="document-row">
          <span>Document signé enregistré</span>
          <AppButton
            type="button"
            variant="secondary"
            :loading="busy"
            @click="downloadSignedDocument"
            >Télécharger</AppButton
          >
        </div>
      </article>

      <section class="panel">
        <ContratTimeline :contrats="chaine" :employe-id="contrat.employeId" :can-create="isHr" />
      </section>

      <section v-if="isHr" class="panel">
        <h2>Historique des statuts</h2>
        <ol v-if="historique.length" class="history">
          <li v-for="event in historique" :key="event.id">
            <strong>{{ STATUT_CONTRAT_LABELS[event.nouveauStatut] ?? event.nouveauStatut }}</strong>
            <span
              >{{
                event.dateChangement ? new Date(event.dateChangement).toLocaleString('fr-FR') : ''
              }}
              · {{ event.auteur }}</span
            >
            <p v-if="event.commentaire">{{ event.commentaire }}</p>
          </li>
        </ol>
        <p v-else class="muted">Aucun changement de statut enregistré.</p>
      </section>

      <RenouvellementModal
        :visible="renewalOpen"
        :contrat="contrat"
        :loading="busy"
        :error="actionError"
        @close="renewalOpen = false"
        @submit="createSuccessor('renouveler', $event)"
      />
      <TransformationModal
        :visible="transformationOpen"
        :contrat="contrat"
        :loading="busy"
        :error="actionError"
        @close="transformationOpen = false"
        @submit="createSuccessor('transformer', $event)"
      />
    </template>
  </section>
</template>

<style scoped>
.page {
  display: grid;
  gap: var(--space-5);
}
.eyebrow {
  margin: 0;
  color: var(--color-primary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
}
.eyebrow a,
.link-button {
  color: var(--color-primary);
}
.panel {
  display: grid;
  gap: var(--space-4);
  padding: var(--space-5);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  box-shadow: var(--shadow-card);
}
.panel header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
}
.panel h1 {
  margin: var(--space-2) 0;
  color: var(--color-text);
  font-size: var(--font-size-2xl);
}
.panel header p:last-child {
  color: var(--color-text-muted);
}
.panel nav {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-2);
}
.link-button {
  font-weight: var(--font-weight-semibold);
  text-decoration: none;
  white-space: nowrap;
}
.panel dl {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-3);
  margin: 0;
}
.panel dl div {
  padding: var(--space-3);
  border-radius: var(--radius-sm);
  background: var(--color-bg);
}
.panel dt {
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}
.panel dd {
  margin: var(--space-1) 0 0;
  color: var(--color-text);
  overflow-wrap: anywhere;
}
.inline-form {
  display: grid;
  gap: var(--space-3);
  padding: var(--space-4);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-bg);
}
.inline-form h2,
.panel > h2 {
  margin: 0;
  color: var(--color-text);
  font-size: var(--font-size-lg);
}
.inline-form label {
  display: grid;
  gap: var(--space-2);
  color: var(--color-text);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
}
.inline-form input,
.inline-form select,
.inline-form textarea {
  min-height: var(--control-height);
  padding: var(--space-3);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-text);
  font: inherit;
  font-weight: var(--font-weight-normal);
}
.inline-form footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
}
.document-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  padding-top: var(--space-3);
  border-top: 1px solid var(--color-border);
  color: var(--color-text);
}
.history {
  display: grid;
  gap: var(--space-3);
  margin: 0;
  padding-left: var(--space-5);
}
.history li {
  padding-left: var(--space-2);
  color: var(--color-text);
}
.history span {
  display: block;
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}
.history p,
.muted {
  margin: var(--space-1) 0 0;
  color: var(--color-text-muted);
}
@media (max-width: 720px) {
  .panel header,
  .document-row {
    align-items: stretch;
    flex-direction: column;
  }
  .panel dl {
    grid-template-columns: 1fr;
  }
}
</style>
