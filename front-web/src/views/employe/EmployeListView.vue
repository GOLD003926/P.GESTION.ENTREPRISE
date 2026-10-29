<script setup>
import { onMounted, reactive, ref } from 'vue'
import { employesApi } from '@/api/employes'
import EmployeTable from '@/components/employe/EmployeTable.vue'
import { ROUTE_NAMES } from '@/constants/routeNames'
import { STATUT_EMPLOYE_LABELS } from '@/constants/statuts'
import { useAuthStore } from '@/stores/auth'
import { ROLES } from '@/constants/roles'
import AppAlert from '@/components/common/AppAlert.vue'
import AppButton from '@/components/common/AppButton.vue'

const auth = useAuthStore()
const filters = reactive({ q: '', statut: '' })
const page = ref(0)
const response = ref({ content: [], totalElements: 0, totalPages: 0 })
const loading = ref(true)
const error = ref('')
const canCreate = auth.role === ROLES.RH

async function load(nextPage = page.value) {
  loading.value = true
  error.value = ''
  page.value = nextPage
  try {
    response.value = await employesApi.rechercher({
      q: filters.q.trim() || undefined,
      statut: filters.statut || undefined,
      page: page.value,
      size: 20,
      sort: 'matricule,asc',
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
        <h1>Dossiers employés</h1>
        <p>Recherchez un salarié et consultez son affectation actuelle.</p>
      </div>
      <RouterLink v-if="canCreate" class="primary-link" :to="{ name: ROUTE_NAMES.EMPLOYE_CREATE }">
        Créer un dossier
      </RouterLink>
    </header>

    <AppAlert v-if="error" variant="error">{{ error }}</AppAlert>
    <section class="panel" aria-label="Liste des employés">
      <form class="filters" role="search" @submit.prevent="load(0)">
        <label
          >Nom, prénom ou matricule
          <input v-model.trim="filters.q" type="search" placeholder="Rechercher un employé" />
        </label>
        <label
          >Statut
          <select v-model="filters.statut">
            <option value="">Tous les statuts</option>
            <option v-for="(label, status) in STATUT_EMPLOYE_LABELS" :key="status" :value="status">
              {{ label }}
            </option>
          </select>
        </label>
        <AppButton type="submit" :loading="loading">Rechercher</AppButton>
      </form>

      <p v-if="loading" class="state" role="status">Chargement des employés…</p>
      <p v-else-if="!response.content?.length" class="state">Aucun employé trouvé.</p>
      <EmployeTable v-else :employes="response.content" />

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
            >Suivant</AppButton
          >
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
.primary-link {
  display: inline-flex;
  min-height: var(--control-height);
  align-items: center;
  justify-content: center;
  padding: 0 var(--space-4);
  border-radius: var(--radius-sm);
  background: var(--color-primary);
  color: var(--color-on-primary);
  font-weight: var(--font-weight-semibold);
  text-decoration: none;
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
  min-width: 180px;
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
