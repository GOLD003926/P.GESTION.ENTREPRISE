<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { candidaturesApi } from '@/api/candidatures'
import CandidatureFilters from '@/components/recrutement/CandidatureFilters.vue'
import CandidaturePipeline from '@/components/recrutement/CandidaturePipeline.vue'
import CandidatureTable from '@/components/recrutement/CandidatureTable.vue'
import AppAlert from '@/components/common/AppAlert.vue'
import { ROUTE_NAMES } from '@/constants/routeNames'
import { useAuthStore } from '@/stores/auth'
import { ROLES } from '@/constants/roles'

const route = useRoute()
const auth = useAuthStore()
const filters = reactive({ offreId: route.query.offreId ?? '', statut: '', candidatId: '' })
const candidatures = ref([])
const loading = ref(true)
const error = ref('')
const mode = ref('table')
const canCreate = auth.role === ROLES.RH

async function load() {
  loading.value = true
  error.value = ''
  const params = Object.fromEntries(Object.entries(filters).filter(([, value]) => value !== ''))
  try {
    const page = await candidaturesApi.rechercher({ ...params, page: 0, size: 100 })
    candidatures.value = page.content ?? []
  } catch (cause) {
    error.value = cause.message
  } finally {
    loading.value = false
  }
}
function reset() {
  Object.assign(filters, { offreId: '', statut: '', candidatId: '' })
  load()
}
onMounted(load)
</script>

<template>
  <section class="page">
    <header>
      <div>
        <p class="eyebrow">Recrutement</p>
        <h1>Candidatures</h1>
        <p>Suivez les candidatures et les étapes de sélection.</p>
      </div>
      <RouterLink
        v-if="canCreate"
        class="primary-link"
        :to="{ name: ROUTE_NAMES.CANDIDATURE_CREATE }"
        >Enregistrer une candidature</RouterLink
      >
    </header>
    <AppAlert v-if="error" variant="error">{{ error }}</AppAlert>
    <div class="panel">
      <CandidatureFilters
        v-model:filters="filters"
        :loading="loading"
        @submit="load"
        @reset="reset"
      />
      <div class="toolbar">
        <div role="group" aria-label="Mode d’affichage">
          <button :aria-pressed="mode === 'table'" @click="mode = 'table'">Liste</button
          ><button :aria-pressed="mode === 'pipeline'" @click="mode = 'pipeline'">Pipeline</button>
        </div>
        <span>{{ candidatures.length }} résultat(s)</span>
      </div>
      <p v-if="loading" class="empty" role="status">Chargement des candidatures…</p>
      <p v-else-if="!candidatures.length" class="empty">Aucune candidature trouvée.</p>
      <CandidatureTable
        v-else-if="mode === 'table'"
        :candidatures="candidatures"
      /><CandidaturePipeline v-else :candidatures="candidatures" />
    </div>
  </section>
</template>

<style scoped>
.page {
  display: grid;
  gap: var(--space-5);
}
header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-4);
}
h1 {
  margin: 0;
  color: var(--color-text);
  font-size: var(--font-size-2xl);
}
header p:not(.eyebrow) {
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
.panel {
  overflow: hidden;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  box-shadow: var(--shadow-card);
}
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-3) var(--space-4);
  border-bottom: 1px solid var(--color-border);
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}
.toolbar div {
  display: flex;
  gap: var(--space-2);
}
.toolbar button {
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-text);
  font: inherit;
  cursor: pointer;
}
.toolbar button[aria-pressed='true'] {
  border-color: var(--color-primary);
  color: var(--color-primary);
}
.empty {
  padding: var(--space-5);
  color: var(--color-text-muted);
  text-align: center;
}
@media (max-width: 650px) {
  header {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
