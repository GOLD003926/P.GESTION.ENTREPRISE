<script setup>
import { onMounted, reactive, ref } from 'vue'
import { onboardingTachesApi } from '@/api/onboardingTaches'
import AppAlert from '@/components/common/AppAlert.vue'
import AppButton from '@/components/common/AppButton.vue'
import AppStatusBadge from '@/components/common/AppStatusBadge.vue'

const filters = reactive({ statut: '' })
const page = ref(0)
const response = ref({ content: [], totalElements: 0, totalPages: 0 })
const loading = ref(true)
const busy = ref(false)
const error = ref('')

const statusLabels = {
  A_FAIRE: 'À faire',
  EN_COURS: 'En cours',
  FAIT: 'Fait',
  NON_APPLICABLE: 'Non applicable',
}

function statusTone(status) {
  if (status === 'FAIT') return 'success'
  if (status === 'EN_COURS') return 'info'
  if (status === 'NON_APPLICABLE') return 'neutral'
  if (status === 'A_FAIRE') return 'warning'
  return 'neutral'
}

async function load(nextPage = page.value) {
  loading.value = true
  error.value = ''
  page.value = nextPage

  try {
    response.value = await onboardingTachesApi.mesTaches({
      statut: filters.statut || undefined,
      page: page.value,
      size: 20,
      sort: 'echeance,asc',
    })
  } catch (cause) {
    error.value = cause.message
  } finally {
    loading.value = false
  }
}

async function performAction(taskId, action) {
  busy.value = true
  error.value = ''

  try {
    if (action === 'demarrer') await onboardingTachesApi.demarrer(taskId)
    if (action === 'terminer') await onboardingTachesApi.terminer(taskId)
    if (action === 'non-applicable') await onboardingTachesApi.nonApplicable(taskId)
    await load(page.value)
  } catch (cause) {
    error.value = cause.message
  } finally {
    busy.value = false
  }
}

onMounted(() => load(0))
</script>

<template>
  <section class="page">
    <header class="page-header">
      <div>
        <p class="eyebrow">Onboarding</p>
        <h1>Mes tâches</h1>
        <p>Suivez les tâches d’intégration à votre charge.</p>
      </div>
    </header>

    <AppAlert v-if="error" variant="error">{{ error }}</AppAlert>

    <section class="panel" aria-label="Liste des tâches à traiter">
      <form class="filters" role="search" @submit.prevent="load(0)">
        <label>
          Statut
          <select v-model="filters.statut">
            <option value="">Tous</option>
            <option v-for="(label, value) in statusLabels" :key="value" :value="value">
              {{ label }}
            </option>
          </select>
        </label>
        <AppButton type="submit" :loading="loading">Filtrer</AppButton>
      </form>

      <p v-if="loading" class="state" role="status">Chargement des tâches…</p>
      <p v-else-if="!response.content?.length" class="state">Aucune tâche à traiter.</p>

      <div v-else class="list">
        <article v-for="task in response.content" :key="task.id" class="task-card">
          <div class="task-card__header">
            <div>
              <span class="task-card__code">{{ task.code }}</span>
              <h2>{{ task.libelle }}</h2>
            </div>
            <AppStatusBadge :label="statusLabels[task.statut] ?? task.statut" :tone="statusTone(task.statut)" />
          </div>

          <div class="task-card__meta">
            <span>Responsable : {{ task.roleResponsable }}</span>
            <span>Échéance : {{ task.echeance || '—' }}</span>
            <span v-if="task.enRetard" class="late">En retard</span>
          </div>

          <div class="task-card__actions">
            <AppButton
              v-if="task.statut === 'A_FAIRE'"
              variant="secondary"
              :loading="busy"
              @click="performAction(task.id, 'demarrer')"
            >
              Démarrer
            </AppButton>
            <AppButton
              v-if="task.statut === 'EN_COURS'"
              variant="primary"
              :loading="busy"
              @click="performAction(task.id, 'terminer')"
            >
              Terminer
            </AppButton>
            <AppButton
              v-if="task.statut !== 'FAIT' && task.obligatoire === false"
              variant="secondary"
              :loading="busy"
              @click="performAction(task.id, 'non-applicable')"
            >
              Non applicable
            </AppButton>
          </div>
        </article>
      </div>

      <footer v-if="!loading && response.totalElements" class="pagination">
        <span>{{ response.totalElements }} résultat(s)</span>
        <div>
          <AppButton variant="secondary" :disabled="page === 0" @click="load(page - 1)">Précédent</AppButton>
          <span>Page {{ page + 1 }} sur {{ Math.max(response.totalPages, 1) }}</span>
          <AppButton
            variant="secondary"
            :disabled="page + 1 >= response.totalPages"
            @click="load(page + 1)"
          >
            Suivant
          </AppButton>
        </div>
      </footer>
    </section>
  </section>
</template>

<style scoped>
.page {
  display: grid;
  gap: var(--space-5);
}
.page-header h1 {
  margin: 0;
  color: var(--color-text);
  font-size: var(--font-size-2xl);
}
.page-header p:last-child {
  margin: var(--space-2) 0 0;
  color: var(--color-text-muted);
}
.eyebrow {
  margin: 0 0 var(--space-1);
  color: var(--color-primary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
}
.panel {
  overflow: hidden;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  box-shadow: var(--shadow-card);
}
.filters {
  display: flex;
  align-items: end;
  gap: var(--space-3);
  padding: var(--space-4);
  border-bottom: 1px solid var(--color-border);
}
.filters label {
  display: grid;
  flex: 1;
  gap: var(--space-2);
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}
.filters select {
  min-height: var(--control-height);
  padding: 0 var(--space-3);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-text);
  font: inherit;
}
.state {
  padding: var(--space-8) var(--space-4);
  color: var(--color-text-muted);
  text-align: center;
}
.list {
  display: grid;
  gap: 1rem;
  padding: 1rem;
}
.task-card {
  display: grid;
  gap: 0.9rem;
  padding: 1rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-surface);
}
.task-card__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
}
.task-card__header h2 {
  margin: 0.25rem 0 0;
  color: var(--color-text);
  font-size: 1rem;
}
.task-card__code {
  color: var(--color-primary);
  font-size: 0.72rem;
  font-weight: var(--font-weight-semibold);
  letter-spacing: 0.04em;
  text-transform: uppercase;
}
.task-card__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  color: var(--color-text-muted);
  font-size: 0.8rem;
}
.late {
  color: var(--color-warning);
  font-weight: var(--font-weight-semibold);
}
.task-card__actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 0.75rem;
}
.pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  border-top: 1px solid var(--color-border);
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}
.pagination div {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}
@media (max-width: 680px) {
  .filters,
  .pagination,
  .task-card__header {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
