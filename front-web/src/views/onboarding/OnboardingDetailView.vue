<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { onboardingsApi } from '@/api/onboardings'
import { onboardingTachesApi } from '@/api/onboardingTaches'
import AppAlert from '@/components/common/AppAlert.vue'
import AppButton from '@/components/common/AppButton.vue'
import AppStatusBadge from '@/components/common/AppStatusBadge.vue'
import OnboardingChecklist from '@/components/onboarding/OnboardingChecklist.vue'
import OnboardingProgress from '@/components/onboarding/OnboardingProgress.vue'
import { ROUTE_NAMES } from '@/constants/routeNames'
import { ROLES } from '@/constants/roles'
import { useAuthStore } from '@/stores/auth'

const props = defineProps({
  id: { type: [String, Number], default: null },
  mode: { type: String, default: 'detail' },
})

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const onboarding = ref(null)
const loading = ref(true)
const error = ref('')
const taskLoading = ref(false)
const taskError = ref('')

const statusLabels = {
  A_DEMARRER: 'À démarrer',
  EN_COURS: 'En cours',
  TERMINE: 'Terminé',
  ANNULE: 'Annulé',
}

const canSeeHistory = computed(() => auth.role === ROLES.RH || auth.role === ROLES.MANAGER)
const effectiveId = computed(() => props.id ?? route.params.id ?? null)

function statusTone(status) {
  if (status === 'TERMINE') return 'success'
  if (status === 'EN_COURS') return 'info'
  if (status === 'ANNULE') return 'danger'
  if (status === 'A_DEMARRER') return 'warning'
  return 'neutral'
}

function formatDate(value) {
  if (!value) return '—'
  return new Date(`${value}T00:00:00`).toLocaleDateString('fr-FR')
}

async function load() {
  loading.value = true
  error.value = ''
  taskError.value = ''

  try {
    if (props.mode === 'self') {
      onboarding.value = await onboardingsApi.monOnboarding()
    } else {
      onboarding.value = await onboardingsApi.obtenir(effectiveId.value)
    }
  } catch (cause) {
    error.value = cause.message
  } finally {
    loading.value = false
  }
}

async function markTask(taskId, action) {
  try {
    taskLoading.value = true
    taskError.value = ''

    if (action === 'demarrer') {
      await onboardingTachesApi.demarrer(taskId)
    }
    if (action === 'terminer') {
      await onboardingTachesApi.terminer(taskId)
    }

    await load()
  } catch (cause) {
    taskError.value = cause.message
  } finally {
    taskLoading.value = false
  }
}

onMounted(load)
</script>

<template>
  <section class="page">
    <header class="page-header">
      <div>
        <p class="eyebrow">
          <RouterLink :to="{ name: ROUTE_NAMES.ONBOARDINGS }">Onboardings</RouterLink>
          <span> / Détail</span>
        </p>
        <h1>{{ onboarding?.employeNom ?? 'Mon onboarding' }}</h1>
      </div>
      <AppButton v-if="onboarding && canSeeHistory" variant="secondary" @click="router.back()">
        Retour
      </AppButton>
    </header>

    <AppAlert v-if="error" variant="error">{{ error }}</AppAlert>
    <AppAlert v-if="taskError" variant="error">{{ taskError }}</AppAlert>

    <p v-if="loading" class="state" role="status">Chargement de l’onboarding…</p>

    <article v-else-if="onboarding" class="panel">
      <header class="panel__header">
        <div>
          <AppStatusBadge :label="statusLabels[onboarding.statut] ?? onboarding.statut" :tone="statusTone(onboarding.statut)" />
          <h2>{{ onboarding.employeNom }}</h2>
        </div>
        <div class="panel__meta">
          <span>{{ onboarding.matricule || '—' }}</span>
          <span>Contrat {{ onboarding.contratId }}</span>
        </div>
      </header>

      <section class="summary">
        <div class="summary__card">
          <span>Date de début</span>
          <strong>{{ formatDate(onboarding.dateDebut) }}</strong>
        </div>
        <div class="summary__card">
          <span>Date cible</span>
          <strong>{{ formatDate(onboarding.dateCible) }}</strong>
        </div>
        <div class="summary__card">
          <span>Responsable RH</span>
          <strong>{{ onboarding.responsableRhId || 'Non désigné' }}</strong>
        </div>
      </section>

      <OnboardingProgress
        :current="onboarding.tachesObligatoiresFaites ?? 0"
        :total="onboarding.tachesObligatoiresTotal ?? 0"
        label="Tâches obligatoires"
      />

      <div class="content-grid">
        <section class="section">
          <h3>Checklist</h3>
          <OnboardingChecklist :tasks="onboarding.taches ?? []" />
        </section>

        <aside v-if="onboarding.historique?.length" class="section">
          <h3>Historique</h3>
          <ul class="history">
            <li v-for="entry in onboarding.historique" :key="`${entry.date}-${entry.statut}`">
              <AppStatusBadge :label="entry.statut" :tone="statusTone(entry.statut)" />
              <small>{{ entry.date }}</small>
            </li>
          </ul>
        </aside>
      </div>

      <div v-if="onboarding.taches?.length" class="task-actions">
        <AppButton
          v-if="onboarding.taches.some((task) => task.statut === 'A_FAIRE')"
          :loading="taskLoading"
          variant="secondary"
          @click="markTask(onboarding.taches.find((task) => task.statut === 'A_FAIRE')?.id, 'demarrer')"
        >
          Démarrer la prochaine tâche
        </AppButton>
      </div>
    </article>
  </section>
</template>

<style scoped>
.page {
  display: grid;
  gap: var(--space-5);
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-4);
}
.page-header h1 {
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
.eyebrow a {
  color: var(--color-primary);
  text-decoration: none;
}
.panel {
  display: grid;
  gap: 1.5rem;
  padding: 1.5rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  box-shadow: var(--shadow-card);
}
.panel__header {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  align-items: center;
}
.panel__header h2 {
  margin: 0.5rem 0 0;
  color: var(--color-text);
}
.panel__meta {
  display: flex;
  flex-direction: column;
  color: var(--color-text-muted);
  font-size: 0.8rem;
}
.summary {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 1rem;
}
.summary__card {
  display: grid;
  gap: 0.35rem;
  padding: 1rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-bg);
}
.summary__card span {
  color: var(--color-text-muted);
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}
.content-grid {
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(220px, 1fr);
  gap: 1.25rem;
}
.section {
  display: grid;
  gap: 1rem;
}
.section h3 {
  margin: 0;
  color: var(--color-text);
}
.history {
  margin: 0;
  padding-left: 1rem;
  display: grid;
  gap: 0.75rem;
  color: var(--color-text-muted);
}
.history li {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.75rem;
}
.task-actions {
  display: flex;
  justify-content: flex-end;
}
.state {
  padding: var(--space-8) var(--space-4);
  color: var(--color-text-muted);
  text-align: center;
}
@media (max-width: 760px) {
  .content-grid,
  .panel__header,
  .page-header {
    grid-template-columns: 1fr;
    display: grid;
  }
}
</style>
