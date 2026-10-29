<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { affectationsApi } from '@/api/affectations'
import { contratsApi } from '@/api/contrats'
import { employesApi } from '@/api/employes'
import AffectationForm from '@/components/employe/AffectationForm.vue'
import AffectationHistory from '@/components/employe/AffectationHistory.vue'
import EmployeCard from '@/components/employe/EmployeCard.vue'
import ContratTimeline from '@/components/contrat/ContratTimeline.vue'
import { ROLES } from '@/constants/roles'
import { ROUTE_NAMES } from '@/constants/routeNames'
import { STATUT_EMPLOYE_LABELS } from '@/constants/statuts'
import { useAuthStore } from '@/stores/auth'
import { downloadBlob } from '@/utils/download'
import AppAlert from '@/components/common/AppAlert.vue'
import AppButton from '@/components/common/AppButton.vue'

const props = defineProps({
  id: { type: String, default: null },
  moi: { type: Boolean, default: false },
})
const router = useRouter()
const auth = useAuthStore()
const employe = ref(null)
const affectations = ref([])
const contrats = ref([])
const historique = ref([])
const loading = ref(true)
const busy = ref(false)
const error = ref('')
const supplementalError = ref('')
const actionError = ref('')
const showAffectationForm = ref(false)
const canManage = computed(() => auth.role === ROLES.RH && !props.moi)
const canSeeAffectations = computed(() => [ROLES.RH, ROLES.MANAGER].includes(auth.role))
const canSeeHistory = computed(() => auth.role === ROLES.RH)
const canSeeContracts = computed(
  () => props.moi || [ROLES.RH, ROLES.MANAGER, ROLES.PAIE].includes(auth.role),
)

async function load() {
  loading.value = true
  error.value = ''
  supplementalError.value = ''
  try {
    employe.value = props.moi ? await employesApi.moi() : await employesApi.obtenir(props.id)
  } catch (cause) {
    error.value = cause.message
    loading.value = false
    return
  }

  const requests = []
  if (canSeeAffectations.value && !props.moi) {
    requests.push(
      affectationsApi
        .lister({ employeId: employe.value.id, page: 0, size: 100 })
        .then((page) => {
          affectations.value = page.content ?? []
        })
        .catch((cause) => {
          supplementalError.value = cause.message
        }),
    )
  } else {
    affectations.value = []
  }
  if (canSeeHistory.value) {
    requests.push(
      employesApi
        .historique(employe.value.id, { page: 0, size: 100 })
        .then((page) => {
          historique.value = page.content ?? []
        })
        .catch((cause) => {
          supplementalError.value = cause.message
        }),
    )
  } else {
    historique.value = []
  }
  if (canSeeContracts.value) {
    requests.push(
      (props.moi
        ? contratsApi.mesContrats({ page: 0, size: 100, sort: 'dateDebut,asc' })
        : contratsApi.rechercher({
            employeId: employe.value.id,
            page: 0,
            size: 100,
            sort: 'dateDebut,asc',
          })
      )
        .then((page) => {
          contrats.value = page.content ?? []
        })
        .catch((cause) => {
          supplementalError.value = cause.message
        }),
    )
  } else {
    contrats.value = []
  }
  await Promise.all(requests)
  loading.value = false
}

async function changeStatus(operation, confirmation) {
  if (!window.confirm(confirmation)) return
  busy.value = true
  actionError.value = ''
  try {
    await employesApi[operation](employe.value.id)
    await load()
  } catch (cause) {
    actionError.value = cause.message
  } finally {
    busy.value = false
  }
}

async function downloadPdf() {
  busy.value = true
  actionError.value = ''
  try {
    const response = await employesApi.fichePdf(employe.value.id)
    downloadBlob(response.data, `fiche-salarie-${employe.value.matricule}.pdf`)
  } catch (cause) {
    actionError.value = cause.message
  } finally {
    busy.value = false
  }
}

async function afterAssignmentCreated() {
  showAffectationForm.value = false
  await load()
}

function goBack() {
  router.push({ name: ROUTE_NAMES.EMPLOYES })
}

onMounted(load)
</script>

<template>
  <section class="page">
    <p v-if="moi" class="eyebrow">Mon dossier</p>
    <p v-else class="eyebrow">
      <RouterLink :to="{ name: ROUTE_NAMES.EMPLOYES }">Dossiers employés</RouterLink> / Détail
    </p>
    <p v-if="loading" role="status">Chargement du dossier employé…</p>
    <AppAlert v-if="error" variant="error">{{ error }}</AppAlert>
    <template v-if="employe">
      <AppAlert v-if="supplementalError" variant="error">{{ supplementalError }}</AppAlert>
      <AppAlert v-if="actionError" variant="error">{{ actionError }}</AppAlert>
      <div class="actions">
        <RouterLink
          v-if="canManage"
          class="action-link"
          :to="{ name: ROUTE_NAMES.EMPLOYE_EDIT, params: { id: employe.id } }"
          >Modifier les coordonnées</RouterLink
        >
        <AppButton v-if="canManage" variant="secondary" :loading="busy" @click="downloadPdf"
          >Télécharger la fiche PDF</AppButton
        >
        <AppButton
          v-if="canManage && employe.statut === 'ACTIF'"
          variant="secondary"
          :loading="busy"
          @click="changeStatus('suspendre', 'Confirmer la suspension de cet employé ?')"
          >Suspendre</AppButton
        >
        <AppButton
          v-if="canManage && employe.statut === 'SUSPENDU'"
          :loading="busy"
          @click="changeStatus('reprendre', 'Confirmer la reprise de cet employé ?')"
          >Reprendre</AppButton
        >
      </div>
      <EmployeCard :employe="employe">
        <template #status>{{ STATUT_EMPLOYE_LABELS[employe.statut] ?? employe.statut }}</template>
      </EmployeCard>

      <section v-if="canSeeAffectations && !moi" class="panel">
        <header class="section-header">
          <div>
            <h2>Affectations</h2>
            <p>Historique des postes occupés et des périodes d’affectation.</p>
          </div>
          <AppButton v-if="canManage" @click="showAffectationForm = !showAffectationForm">{{
            showAffectationForm ? 'Fermer' : 'Nouvelle affectation'
          }}</AppButton>
        </header>
        <AffectationForm
          v-if="showAffectationForm"
          :employe-id="employe.id"
          @created="afterAssignmentCreated"
          @cancel="showAffectationForm = false"
        />
        <AffectationHistory :affectations="affectations" :can-manage="canManage" @updated="load" />
      </section>

      <section v-if="canSeeContracts" class="panel">
        <ContratTimeline
          :contrats="contrats"
          :employe-id="employe.id"
          :can-create="canManage"
          :linkable="!moi || auth.role !== ROLES.SALARIE"
        />
      </section>

      <section v-if="canSeeHistory" class="panel">
        <h2>Historique des statuts</h2>
        <ol v-if="historique.length" class="status-history">
          <li v-for="event in historique" :key="event.id">
            <strong>{{ STATUT_EMPLOYE_LABELS[event.nouveauStatut] ?? event.nouveauStatut }}</strong>
            <span
              >{{
                event.dateChangement ? new Date(event.dateChangement).toLocaleString('fr-FR') : ''
              }}
              · {{ event.auteur }}</span
            >
            <p v-if="event.commentaire">{{ event.commentaire }}</p>
          </li>
        </ol>
        <p v-else-if="!supplementalError" class="muted">Aucun changement de statut enregistré.</p>
      </section>

      <AppButton v-if="!moi" variant="secondary" @click="goBack">Retour à la liste</AppButton>
      <p class="future-modules">
        Les documents associés au dossier seront disponibles dans une prochaine étape.
      </p>
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
.action-link {
  color: var(--color-primary);
}
.actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: var(--space-2);
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
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-4);
}
.panel h2 {
  margin: 0;
  color: var(--color-text);
  font-size: var(--font-size-lg);
}
.section-header p {
  margin: var(--space-1) 0 0;
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}
.status-history {
  display: grid;
  gap: var(--space-3);
  margin: 0;
  padding-left: var(--space-5);
}
.status-history li {
  padding-left: var(--space-2);
  color: var(--color-text);
}
.status-history span {
  display: block;
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}
.status-history p,
.muted {
  margin: var(--space-1) 0 0;
  color: var(--color-text-muted);
}
.future-modules {
  margin: 0;
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}
.action-link {
  align-self: center;
  font-weight: var(--font-weight-semibold);
  text-decoration: none;
}
@media (max-width: 680px) {
  .actions,
  .section-header {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
