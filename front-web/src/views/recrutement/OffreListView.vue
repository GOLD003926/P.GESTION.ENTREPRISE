<script setup>
import { computed, onMounted, ref } from 'vue'
import { offresApi } from '@/api/offres'
import OffreTable from '@/components/recrutement/OffreTable.vue'
import { ROUTE_NAMES } from '@/constants/routeNames'
import { STATUT_OFFRE_LABELS } from '@/constants/statuts'
import { useAuthStore } from '@/stores/auth'
import { ROLES } from '@/constants/roles'
import AppAlert from '@/components/common/AppAlert.vue'
import AppButton from '@/components/common/AppButton.vue'

const auth = useAuthStore()
const offres = ref([])
const loading = ref(true)
const error = ref('')
const query = ref('')
const statut = ref('')
const canCreate = computed(() => auth.role === ROLES.RH)

async function load() {
  loading.value = true
  error.value = ''
  try {
    const page = await offresApi.rechercher({
      page: 0,
      size: 100,
      q: query.value || undefined,
      statut: statut.value || undefined,
    })
    offres.value = page.content ?? []
  } catch (cause) {
    error.value = cause.message
  } finally {
    loading.value = false
  }
}
onMounted(load)
</script>

<template>
  <section class="recruitment-page">
    <header class="page-heading">
      <div>
        <p class="eyebrow">Recrutement</p>
        <h1>Offres d’emploi</h1>
        <p>Suivez les offres ouvertes et leur cycle de publication.</p>
      </div>
      <RouterLink v-if="canCreate" class="primary-link" :to="{ name: ROUTE_NAMES.OFFRE_CREATE }"
        >Créer une offre</RouterLink
      >
    </header>
    <AppAlert v-if="error" variant="error">{{ error }}</AppAlert>
    <form class="filters" @submit.prevent="load">
      <label>Recherche<input v-model.trim="query" placeholder="Titre ou référence" /></label
      ><label
        >Statut<select v-model="statut">
          <option value="">Tous les statuts</option>
          <option v-for="(label, key) in STATUT_OFFRE_LABELS" :key="key" :value="key">
            {{ label }}
          </option>
        </select></label
      ><AppButton type="submit" :loading="loading">Rechercher</AppButton>
    </form>
    <div class="panel">
      <p v-if="loading" role="status">Chargement des offres…</p>
      <p v-else-if="!offres.length" class="empty">Aucune offre trouvée.</p>
      <OffreTable v-else :offres="offres" />
    </div>
  </section>
</template>

<style scoped>
.recruitment-page {
  display: grid;
  gap: var(--space-5);
}
.page-heading {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-4);
}
.page-heading h1 {
  margin: 0;
  color: var(--color-text);
  font-size: var(--font-size-2xl);
}
.page-heading p:not(.eyebrow) {
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
  align-items: center;
  min-height: var(--control-height);
  padding: 0 var(--space-4);
  border-radius: var(--radius-sm);
  background: var(--color-primary);
  color: var(--color-on-primary);
  font-weight: var(--font-weight-semibold);
  text-decoration: none;
}
.filters {
  display: flex;
  align-items: end;
  gap: var(--space-3);
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
.panel {
  overflow: hidden;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  box-shadow: var(--shadow-card);
}
.empty {
  padding: var(--space-5);
  color: var(--color-text-muted);
  text-align: center;
}
@media (max-width: 650px) {
  .page-heading,
  .filters {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
