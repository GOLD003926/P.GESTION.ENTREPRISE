<script setup>
import { computed, onMounted, ref } from 'vue'
import { entretiensApi } from '@/api/entretiens'
import { evaluationsApi } from '@/api/evaluations'
import EntretienForm from '@/components/recrutement/EntretienForm.vue'
import EvaluationForm from '@/components/recrutement/EvaluationForm.vue'
import EvaluationList from '@/components/recrutement/EvaluationList.vue'
import { ROUTE_NAMES } from '@/constants/routeNames'
import { STATUT_ENTRETIEN_LABELS } from '@/constants/statuts'
import { ROLES } from '@/constants/roles'
import { useAuthStore } from '@/stores/auth'
import AppAlert from '@/components/common/AppAlert.vue'
import AppButton from '@/components/common/AppButton.vue'

const props = defineProps({ id: { type: String, required: true } })
const auth = useAuthStore()
const entretien = ref(null)
const evaluations = ref([])
const loading = ref(true)
const busy = ref(false)
const error = ref('')
const actionError = ref('')
const editing = ref(false)
const evaluating = ref(false)
const cancelOpen = ref(false)
const motifAnnulation = ref('')
const selectedEvaluation = ref(null)
const evaluationMode = ref('create')
const canManage = computed(() => auth.role === ROLES.RH)

async function load() {
  loading.value = true
  error.value = ''
  try {
    const detail = await entretiensApi.obtenir(props.id)
    const page = await evaluationsApi.rechercher({ entretienId: props.id, page: 0, size: 100 })
    entretien.value = detail
    evaluations.value = page.content ?? []
  } catch (cause) {
    error.value = cause.message
  } finally {
    loading.value = false
  }
}
async function run(operation, ...args) {
  busy.value = true
  actionError.value = ''
  try {
    await entretiensApi[operation](props.id, ...args)
    cancelOpen.value = false
    editing.value = false
    await load()
  } catch (cause) {
    actionError.value = cause.message
  } finally {
    busy.value = false
  }
}
async function saveEvaluation(payload) {
  busy.value = true
  actionError.value = ''
  try {
    if (evaluationMode.value === 'correct' && selectedEvaluation.value) {
      await evaluationsApi.corriger(selectedEvaluation.value.id, payload)
    } else if (evaluationMode.value === 'edit' && selectedEvaluation.value) {
      await evaluationsApi.modifier(selectedEvaluation.value.id, payload)
    } else {
      await evaluationsApi.creer(payload)
    }
    evaluating.value = false
    selectedEvaluation.value = null
    await load()
  } catch (cause) {
    actionError.value = cause.message
  } finally {
    busy.value = false
  }
}
function startEvaluation() {
  selectedEvaluation.value = null
  evaluationMode.value = 'create'
  evaluating.value = !evaluating.value
}
function editEvaluation(evaluation, mode) {
  selectedEvaluation.value = evaluation
  evaluationMode.value = mode
  evaluating.value = true
}
function closeEvaluationForm() {
  evaluating.value = false
  selectedEvaluation.value = null
}
async function lockEvaluation(id) {
  busy.value = true
  actionError.value = ''
  try {
    await evaluationsApi.verrouiller(id)
    await load()
  } catch (cause) {
    actionError.value = cause.message
  } finally {
    busy.value = false
  }
}
async function cancelInterview() {
  if (!motifAnnulation.value.trim()) {
    actionError.value = 'Le motif d’annulation est obligatoire.'
    return
  }
  await run('annuler', { motif: motifAnnulation.value.trim() })
}
onMounted(load)
</script>

<template>
  <section class="page">
    <p class="eyebrow">
      <RouterLink :to="{ name: ROUTE_NAMES.ENTRETIENS }">Entretiens</RouterLink> / Détail
    </p>
    <p v-if="loading" role="status">Chargement de l’entretien…</p>
    <AppAlert v-if="error" variant="error">{{ error }}</AppAlert>
    <template v-if="entretien">
      <AppAlert v-if="actionError" variant="error">{{ actionError }}</AppAlert>
      <article class="panel">
        <header>
          <div>
            <p class="eyebrow">
              Niveau {{ entretien.niveau }} ·
              {{ STATUT_ENTRETIEN_LABELS[entretien.statut] ?? entretien.statut }}
            </p>
            <h1>{{ entretien.candidatNom }}</h1>
            <p>{{ entretien.type }}</p>
          </div>
          <nav
            v-if="canManage && !['ANNULE', 'CANDIDAT_ABSENT', 'REALISE'].includes(entretien.statut)"
          >
            <AppButton
              v-if="['PLANIFIE', 'REPORTE'].includes(entretien.statut)"
              variant="secondary"
              @click="editing = !editing"
              >Reprogrammer</AppButton
            >
            <AppButton
              v-if="entretien.statut === 'PLANIFIE' || entretien.statut === 'REPORTE'"
              :loading="busy"
              @click="run('cloturer')"
              >Clôturer</AppButton
            >
            <AppButton
              v-if="['PLANIFIE', 'REPORTE'].includes(entretien.statut)"
              variant="secondary"
              :loading="busy"
              @click="run('marquerAbsent')"
              >Marquer absent</AppButton
            >
            <AppButton
              v-if="['PLANIFIE', 'REPORTE'].includes(entretien.statut)"
              variant="secondary"
              @click="cancelOpen = !cancelOpen"
              >Annuler l’entretien</AppButton
            >
          </nav>
        </header>
        <dl>
          <div>
            <dt>Date et heure</dt>
            <dd>
              {{
                entretien.dateHeure ? new Date(entretien.dateHeure).toLocaleString('fr-FR') : '—'
              }}
            </dd>
          </div>
          <div>
            <dt>Durée</dt>
            <dd>{{ entretien.dureeMinutes }} minutes</dd>
          </div>
          <div>
            <dt>Lieu ou lien</dt>
            <dd>{{ entretien.lieuOuLien || '—' }}</dd>
          </div>
          <div>
            <dt>Avis du niveau</dt>
            <dd>{{ entretien.avisNiveau || 'En attente' }}</dd>
          </div>
          <div>
            <dt>Intervenants</dt>
            <dd>
              {{
                entretien.intervenants
                  ?.map(
                    (item) =>
                      `${item.utilisateurNom}${item.roleIntervenant ? ` (${item.roleIntervenant})` : ''}`,
                  )
                  .join(', ') || '—'
              }}
            </dd>
          </div>
          <div v-if="entretien.motifAnnulation">
            <dt>Motif d’annulation</dt>
            <dd>{{ entretien.motifAnnulation }}</dd>
          </div>
        </dl>
        <form v-if="cancelOpen" class="cancel-form" @submit.prevent="cancelInterview">
          <label
            >Motif d’annulation<textarea
              v-model.trim="motifAnnulation"
              maxlength="500"
              required
            /></label
          ><AppButton type="submit" :loading="busy">Confirmer l’annulation</AppButton>
        </form>
        <EntretienForm
          v-if="editing"
          :candidature-id="entretien.candidatureId"
          :entretien="entretien"
          :loading="busy"
          :error="actionError"
          @submit="run('modifier', $event)"
          @cancel="editing = false"
        />
      </article>
      <section class="panel">
        <header class="section-heading">
          <h2>Évaluations</h2>
          <AppButton
            v-if="
              ['RH', 'MANAGER'].includes(auth.role) &&
              ['PLANIFIE', 'REPORTE', 'REALISE'].includes(entretien.statut)
            "
            @click="startEvaluation"
            >{{ evaluating ? 'Fermer le formulaire' : 'Saisir une évaluation' }}</AppButton
          >
        </header>
        <EvaluationForm
          v-if="evaluating"
          :entretien-id="id"
          :evaluation="selectedEvaluation"
          :loading="busy"
          :error="actionError"
          @submit="saveEvaluation"
          @cancel="closeEvaluationForm"
        />
        <EvaluationList :evaluations="evaluations" />
        <div
          class="evaluation-actions"
          v-for="evaluation in evaluations"
          :key="`actions-${evaluation.id}`"
        >
          <span
            >{{ evaluation.evaluateurNom
            }}<template v-if="evaluation.verrouillee"> · verrouillée</template></span
          >
          <AppButton
            v-if="!evaluation.verrouillee"
            variant="secondary"
            @click="editEvaluation(evaluation, 'edit')"
            >Modifier</AppButton
          >
          <AppButton
            v-if="!evaluation.verrouillee"
            variant="secondary"
            :loading="busy"
            @click="lockEvaluation(evaluation.id)"
            >Verrouiller</AppButton
          >
          <AppButton
            v-if="auth.role === ROLES.RH && evaluation.verrouillee"
            variant="secondary"
            @click="editEvaluation(evaluation, 'correct')"
            >Corriger</AppButton
          >
        </div>
      </section>
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
.panel > header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
}
.panel header p:last-child,
dd {
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
}
.section-heading {
  justify-content: space-between !important;
}
.cancel-form {
  display: grid;
  justify-items: start;
  gap: var(--space-3);
  padding: var(--space-3);
  border: 1px solid var(--color-warning-border);
  border-radius: var(--radius-sm);
}
.cancel-form label {
  display: grid;
  width: 100%;
  gap: var(--space-2);
  color: var(--color-text);
}
.cancel-form textarea {
  min-height: 90px;
  padding: var(--space-3);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-text);
  font: inherit;
}
.evaluation-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-3);
  padding-top: var(--space-3);
  border-top: 1px solid var(--color-border);
  color: var(--color-text-muted);
}
@media (max-width: 700px) {
  .panel > header {
    align-items: stretch;
    flex-direction: column;
  }
  .panel dl {
    grid-template-columns: 1fr;
  }
}
</style>
