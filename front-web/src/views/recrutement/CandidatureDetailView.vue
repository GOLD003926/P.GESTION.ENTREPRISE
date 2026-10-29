<script setup>
import { computed, onMounted, ref } from 'vue'
import { candidaturesApi } from '@/api/candidatures'
import { entretiensApi } from '@/api/entretiens'
import { evaluationsApi } from '@/api/evaluations'
import CvViewer from '@/components/recrutement/CvViewer.vue'
import DecisionFinaleModal from '@/components/recrutement/DecisionFinaleModal.vue'
import EntretienForm from '@/components/recrutement/EntretienForm.vue'
import PreselectionModal from '@/components/recrutement/PreselectionModal.vue'
import EvaluationList from '@/components/recrutement/EvaluationList.vue'
import { ROUTE_NAMES } from '@/constants/routeNames'
import {
  DECISION_FINALE_LABELS,
  STATUT_CANDIDATURE_LABELS,
  STATUT_ENTRETIEN_LABELS,
} from '@/constants/statuts'
import { ROLES } from '@/constants/roles'
import { useAuthStore } from '@/stores/auth'
import AppAlert from '@/components/common/AppAlert.vue'
import AppButton from '@/components/common/AppButton.vue'

const props = defineProps({ id: { type: String, required: true } })
const auth = useAuthStore()
const candidature = ref(null)
const entretiens = ref([])
const historique = ref([])
const loading = ref(true)
const busy = ref(false)
const error = ref('')
const actionError = ref('')
const preselectionOpen = ref(false)
const decisionOpen = ref(false)
const interviewOpen = ref(false)
const canManage = computed(() => auth.role === ROLES.RH)
const canDecide = computed(() => [ROLES.RH, ROLES.MANAGER].includes(auth.role))
const canSchedule = computed(
  () =>
    canManage.value &&
    ['PRESELECTIONNEE', 'EN_ENTRETIEN'].includes(candidature.value?.statut) &&
    candidature.value?.prochainNiveau != null,
)
const canFinalDecision = computed(
  () =>
    canDecide.value &&
    candidature.value?.statut === 'EN_ENTRETIEN' &&
    candidature.value?.prochainNiveau == null,
)

async function load() {
  loading.value = true
  error.value = ''
  try {
    const [detail, historyPage, interviewPage] = await Promise.all([
      candidaturesApi.obtenir(props.id),
      candidaturesApi.historique(props.id),
      entretiensApi.rechercher({ candidatureId: Number(props.id), page: 0, size: 100 }),
    ])
    candidature.value = detail
    historique.value = historyPage.content ?? []
    const items = interviewPage.content ?? []
    entretiens.value = await Promise.all(
      items.map(async (entretien) => {
        const page = await evaluationsApi.rechercher({
          entretienId: entretien.id,
          page: 0,
          size: 100,
        })
        return { ...entretien, evaluations: page.content ?? [] }
      }),
    )
  } catch (cause) {
    error.value = cause.message
  } finally {
    loading.value = false
  }
}
async function perform(action, ...args) {
  busy.value = true
  actionError.value = ''
  try {
    await candidaturesApi[action](props.id, ...args)
    preselectionOpen.value = false
    decisionOpen.value = false
    interviewOpen.value = false
    await load()
  } catch (cause) {
    actionError.value = cause.message
  } finally {
    busy.value = false
  }
}
async function promote() {
  await perform('promouvoirListeAttente')
}
async function withdraw() {
  if (window.confirm('Confirmer le désistement de cette candidature ?')) await perform('desister')
}
async function schedule(payload) {
  busy.value = true
  actionError.value = ''
  try {
    await entretiensApi.creer(payload)
    interviewOpen.value = false
    await load()
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
      <RouterLink :to="{ name: ROUTE_NAMES.CANDIDATURES }">Candidatures</RouterLink> / Détail
    </p>
    <p v-if="loading" role="status">Chargement de la candidature…</p>
    <AppAlert v-if="error" variant="error">{{ error }}</AppAlert>
    <template v-if="candidature">
      <AppAlert v-if="actionError" variant="error">{{ actionError }}</AppAlert>
      <article class="panel">
        <header>
          <div>
            <p class="eyebrow">
              {{ candidature.offreReference }} ·
              {{ STATUT_CANDIDATURE_LABELS[candidature.statut] ?? candidature.statut }}
            </p>
            <h1>{{ candidature.candidatNom }}</h1>
            <p>{{ candidature.candidatEmail }} · {{ candidature.offreTitre }}</p>
          </div>
          <nav>
            <AppButton
              v-if="canManage && candidature.statut === 'RECUE'"
              @click="preselectionOpen = true"
              >Présélectionner</AppButton
            >
            <AppButton v-if="canSchedule" @click="interviewOpen = true"
              >Planifier le niveau {{ candidature.prochainNiveau }}</AppButton
            >
            <AppButton v-if="canFinalDecision" @click="decisionOpen = true"
              >Décision finale</AppButton
            >
            <AppButton
              v-if="canManage && candidature.statut === 'LISTE_ATTENTE'"
              variant="secondary"
              :loading="busy"
              @click="promote"
              >Promouvoir</AppButton
            >
            <AppButton
              v-if="
                canManage &&
                !['EMBAUCHEE', 'DESISTEE', 'NON_RETENUE', 'REFUSEE_DOSSIER'].includes(
                  candidature.statut,
                )
              "
              variant="secondary"
              :loading="busy"
              @click="withdraw"
              >Désister</AppButton
            >
          </nav>
        </header>
        <dl>
          <div>
            <dt>Date de dépôt</dt>
            <dd>
              {{
                candidature.dateDepot
                  ? new Date(candidature.dateDepot).toLocaleString('fr-FR')
                  : '—'
              }}
            </dd>
          </div>
          <div>
            <dt>Score de présélection</dt>
            <dd>{{ candidature.scorePreselection ?? '—' }}</dd>
          </div>
          <div>
            <dt>Décision finale</dt>
            <dd>
              {{
                DECISION_FINALE_LABELS[candidature.decisionFinale] ??
                candidature.decisionFinale ??
                'En attente'
              }}
            </dd>
          </div>
          <div>
            <dt>Motif / refus</dt>
            <dd>{{ candidature.motifRefus || candidature.motifDecision || '—' }}</dd>
          </div>
          <div>
            <dt>Prochain niveau</dt>
            <dd>{{ candidature.prochainNiveau ?? 'Aucun niveau en attente' }}</dd>
          </div>
          <div>
            <dt>Lettre de motivation</dt>
            <dd>{{ candidature.lettreMotivation || 'Aucune lettre fournie.' }}</dd>
          </div>
        </dl>
      </article>

      <section class="panel">
        <header class="section-heading"><h2>Curriculum vitæ</h2></header>
        <CvViewer :document-id="candidature.cvDocumentId" />
      </section>

      <section class="panel">
        <header class="section-heading"><h2>Entretiens et évaluations</h2></header>
        <p v-if="!entretiens.length" class="muted">Aucun entretien planifié.</p>
        <article v-for="entretien in entretiens" :key="entretien.id" class="interview">
          <header>
            <div>
              <h3>Niveau {{ entretien.niveau }} · {{ entretien.type }}</h3>
              <p>
                {{
                  entretien.dateHeure ? new Date(entretien.dateHeure).toLocaleString('fr-FR') : '—'
                }}
                · {{ STATUT_ENTRETIEN_LABELS[entretien.statut] ?? entretien.statut }}
              </p>
            </div>
            <RouterLink :to="{ name: ROUTE_NAMES.ENTRETIEN_DETAIL, params: { id: entretien.id } }"
              >Détail de l’entretien</RouterLink
            >
          </header>
          <p>
            {{ entretien.lieuOuLien || 'Lieu non précisé' }} · Avis :
            {{ entretien.avisNiveau || 'En attente' }}
          </p>
          <EvaluationList :evaluations="entretien.evaluations" />
        </article>
      </section>

      <section class="panel">
        <header class="section-heading"><h2>Historique des statuts</h2></header>
        <ol v-if="historique.length" class="history">
          <li v-for="item in historique" :key="item.id">
            <strong>{{
              STATUT_CANDIDATURE_LABELS[item.nouveauStatut] ?? item.nouveauStatut
            }}</strong
            ><span
              >{{
                item.dateChangement ? new Date(item.dateChangement).toLocaleString('fr-FR') : ''
              }}
              · {{ item.auteur }}</span
            >
            <p v-if="item.commentaire">{{ item.commentaire }}</p>
          </li>
        </ol>
        <p v-else class="muted">Aucun historique disponible.</p>
      </section>

      <PreselectionModal
        :open="preselectionOpen"
        :loading="busy"
        :error="actionError"
        @close="preselectionOpen = false"
        @submit="perform('preselectionner', $event)"
      />
      <DecisionFinaleModal
        :open="decisionOpen"
        :loading="busy"
        :error="actionError"
        @close="decisionOpen = false"
        @submit="perform('decisionFinale', $event)"
      />
      <div v-if="interviewOpen" class="modal-backdrop">
        <section class="modal" role="dialog" aria-modal="true" aria-labelledby="schedule-title">
          <h2 id="schedule-title">Planifier un entretien</h2>
          <EntretienForm
            :candidature-id="id"
            :niveau="candidature.prochainNiveau"
            :loading="busy"
            :error="actionError"
            @submit="schedule"
            @cancel="interviewOpen = false"
          />
        </section>
      </div>
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
.eyebrow a {
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
.panel > header:first-child {
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
.panel h2 {
  margin: 0;
  color: var(--color-text);
  font-size: var(--font-size-lg);
}
.panel h3 {
  margin: 0;
  color: var(--color-text);
}
.panel header p,
.muted {
  color: var(--color-text-muted);
}
.panel nav {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
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
  white-space: pre-wrap;
}
.section-heading {
  justify-content: flex-start !important;
}
.interview {
  display: grid;
  gap: var(--space-3);
  padding-top: var(--space-4);
  border-top: 1px solid var(--color-border);
}
.interview header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
}
.interview a {
  color: var(--color-primary);
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
.history p {
  margin: var(--space-1) 0;
}
.modal-backdrop {
  position: fixed;
  z-index: 100;
  inset: 0;
  display: grid;
  place-items: center;
  padding: var(--space-4);
  background: var(--color-overlay);
}
.modal {
  width: min(100%, 620px);
  max-height: 90vh;
  overflow-y: auto;
  padding: var(--space-5);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  box-shadow: var(--shadow-card);
}
.modal h2 {
  margin: 0 0 var(--space-4);
  color: var(--color-text);
}
@media (max-width: 700px) {
  .panel > header:first-child,
  .interview header {
    align-items: stretch;
    flex-direction: column;
  }
  .panel dl {
    grid-template-columns: 1fr;
  }
}
</style>
