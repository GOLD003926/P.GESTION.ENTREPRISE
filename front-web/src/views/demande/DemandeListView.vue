<script setup>
import { onMounted, ref } from 'vue'
import { demandesApi } from '@/api/demandes'
import AppAlert from '@/components/common/AppAlert.vue'
import AppButton from '@/components/common/AppButton.vue'
import DemandeFilters from '@/components/demande/DemandeFilters.vue'
import DemandeTable from '@/components/demande/DemandeTable.vue'
import { ROUTE_NAMES } from '@/constants/routeNames'

const demandes = ref([])
const filters = ref({ q: '', statut: '', posteId: '', departementId: '' })
const loading = ref(true)
const error = ref('')
const page = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)
const pageSize = 10

async function load() {
  loading.value = true
  error.value = ''
  const params = {
    page: page.value,
    size: pageSize,
    sort: 'createdAt,desc',
  }
  if (filters.value.q) params.q = filters.value.q
  if (filters.value.statut) params.statut = filters.value.statut
  if (filters.value.posteId) params.posteId = Number(filters.value.posteId)
  if (filters.value.departementId) params.departementId = Number(filters.value.departementId)

  try {
    const result = await demandesApi.rechercher(params)
    demandes.value = result.content ?? []
    totalPages.value = result.totalPages ?? 0
    totalElements.value = result.totalElements ?? 0
  } catch (cause) {
    error.value = cause.message
  } finally {
    loading.value = false
  }
}

async function applyFilters() {
  page.value = 0
  await load()
}

async function resetFilters() {
  Object.assign(filters.value, { q: '', statut: '', posteId: '', departementId: '' })
  page.value = 0
  await load()
}

async function changePage(nextPage) {
  if (nextPage < 0 || nextPage >= totalPages.value) return
  page.value = nextPage
  await load()
}

onMounted(load)
</script>

<template>
  <section class="demande-list">
    <header class="demande-list__header">
      <div>
        <p class="demande-list__eyebrow">Expression du besoin</p>
        <h1>Mes demandes</h1>
        <p>Créez et suivez les demandes de recrutement de votre équipe.</p>
      </div>
      <RouterLink class="demande-list__create" :to="{ name: ROUTE_NAMES.DEMANDE_CREATE }">
        Nouvelle demande
      </RouterLink>
    </header>

    <section class="demande-list__card" aria-label="Demandes de recrutement">
      <DemandeFilters
        v-model:filters="filters"
        :loading="loading"
        @submit="applyFilters"
        @reset="resetFilters"
      />
      <AppAlert v-if="error" variant="error">
        <div class="demande-list__error">
          <span>{{ error }}</span>
          <AppButton type="button" variant="secondary" @click="load">Réessayer</AppButton>
        </div>
      </AppAlert>
      <p v-if="loading" class="demande-list__state" role="status">Chargement des demandes…</p>
      <p v-else-if="!error && demandes.length === 0" class="demande-list__state">
        Aucune demande ne correspond aux filtres sélectionnés.
      </p>
      <DemandeTable v-else-if="!error" :demandes="demandes" />
      <footer v-if="!loading && totalElements > 0" class="demande-list__pagination">
        <span>{{ totalElements }} demande(s)</span>
        <div>
          <button type="button" :disabled="page <= 0" @click="changePage(page - 1)">
            Précédent
          </button>
          <span>Page {{ page + 1 }} sur {{ Math.max(totalPages, 1) }}</span>
          <button type="button" :disabled="page + 1 >= totalPages" @click="changePage(page + 1)">
            Suivant
          </button>
        </div>
      </footer>
    </section>
  </section>
</template>

<style scoped>
.demande-list {
  display: grid;
  gap: var(--space-5);
}

.demande-list__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
}

.demande-list__eyebrow {
  margin: 0 0 var(--space-1);
  color: var(--color-primary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
}

.demande-list__header h1 {
  margin: 0;
  color: var(--color-text);
  font-size: var(--font-size-2xl);
}

.demande-list__header p:last-child {
  margin: var(--space-2) 0 0;
  color: var(--color-text-muted);
}

.demande-list__create {
  min-height: var(--control-height);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0 var(--space-4);
  border: 1px solid var(--color-primary);
  border-radius: var(--radius-sm);
  background: var(--color-primary);
  color: var(--color-on-primary);
  font-weight: var(--font-weight-semibold);
  text-decoration: none;
  white-space: nowrap;
}

.demande-list__create:hover {
  background: var(--color-primary-hover);
}

.demande-list__card {
  min-width: 0;
  overflow: hidden;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  box-shadow: var(--shadow-card);
}

.demande-list__state {
  margin: 0;
  padding: var(--space-8) var(--space-4);
  color: var(--color-text-muted);
  text-align: center;
}

.demande-list__error {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
}

.demande-list__pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  border-top: 1px solid var(--color-border);
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}

.demande-list__pagination div {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.demande-list__pagination button {
  min-height: 36px;
  padding: 0 var(--space-3);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-text);
  font: inherit;
  cursor: pointer;
}

.demande-list__pagination button:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.demande-list__pagination button:focus-visible,
.demande-list__create:focus-visible {
  outline: 3px solid var(--color-focus-ring);
  outline-offset: 2px;
}

@media (max-width: 700px) {
  .demande-list__header,
  .demande-list__pagination {
    align-items: flex-start;
    flex-direction: column;
  }

  .demande-list__pagination div {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
