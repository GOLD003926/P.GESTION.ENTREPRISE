<script setup>
import { onMounted, reactive, ref } from 'vue'
import { onboardingsApi } from '@/api/onboardings'
import AppAlert from '@/components/common/AppAlert.vue'
import AppButton from '@/components/common/AppButton.vue'
import OnboardingTable from '@/components/onboarding/OnboardingTable.vue'

const filters = reactive({ employeId: '', statut: '' })
const page = ref(0)
const response = ref({ content: [], totalElements: 0, totalPages: 0 })
const loading = ref(true)
const error = ref('')

const statutLabels = {
  A_DEMARRER: 'À démarrer',
  EN_COURS: 'En cours',
  TERMINE: 'Terminé',
  ANNULE: 'Annulé',
}

async function load(nextPage = page.value) {
  loading.value = true
  error.value = ''
  page.value = nextPage

  try {
    response.value = await onboardingsApi.rechercher({
      employeId: filters.employeId ? Number(filters.employeId) : undefined,
      statut: filters.statut || undefined,
      page: page.value,
      size: 20,
      sort: 'dateDebut,desc',
    })
  } catch (cause) {
    error.value = cause.message
  } finally {
    loading.value = false
  }
}

onMounted(() => load(0))
</script>

<template>
  <section class="page">
    <header class="page-header">
      <div>
        <p class="eyebrow">Ressources humaines</p>
        <h1>Onboardings</h1>
        <p>Suivez le parcours d’intégration des nouveaux salariés.</p>
      </div>
    </header>

    <AppAlert v-if="error" variant="error">{{ error }}</AppAlert>

    <section class="panel" aria-label="Liste des onboardings">
      <form class="filters" role="search" @submit.prevent="load(0)">
        <label>
          Identifiant employé
          <input v-model.trim="filters.employeId" type="number" min="1" placeholder="Tous" />
        </label>
        <label>
          Statut
          <select v-model="filters.statut">
            <option value="">Tous</option>
            <option v-for="(label, value) in statutLabels" :key="value" :value="value">
              {{ label }}
            </option>
          </select>
        </label>
        <AppButton type="submit" :loading="loading">Rechercher</AppButton>
      </form>

      <p v-if="loading" class="state" role="status">Chargement des onboardings…</p>
      <p v-else-if="!response.content?.length" class="state">Aucun onboarding trouvé.</p>
      <OnboardingTable v-else :onboardings="response.content" />

      <footer v-if="!loading && response.totalElements" class="pagination">
        <span>{{ response.totalElements }} résultat(s)</span>
        <div>
          <AppButton variant="secondary" :disabled="page === 0" @click="load(page - 1)"
            >Précédent</AppButton
          >
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
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
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
.filters input,
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
  .page-header,
  .filters,
  .pagination {
    align-items: stretch;
    flex-direction: column;
  }
  .pagination div {
    justify-content: space-between;
  }
}
</style>
