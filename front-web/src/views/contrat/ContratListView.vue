<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { contratsApi } from '@/api/contrats'
import ContratAlertBanner from '@/components/contrat/ContratAlertBanner.vue'
import ContratTable from '@/components/contrat/ContratTable.vue'
import ContratTimeline from '@/components/contrat/ContratTimeline.vue'
import AppAlert from '@/components/common/AppAlert.vue'
import AppButton from '@/components/common/AppButton.vue'
import { ROLES } from '@/constants/roles'
import { ROUTE_NAMES } from '@/constants/routeNames'
import {
  STATUT_CONTRAT_LABELS,
  TYPE_CONTRAT_LABELS,
} from '@/constants/statuts'
import { useAuthStore } from '@/stores/auth'

const props = defineProps({ personnels: { type: Boolean, default: false } })
const auth = useAuthStore()
const router = useRouter()
const filters = reactive({ employeId: '', type: '', statut: '', finAvant: '' })
const page = ref(0)
const response = ref({ content: [], totalElements: 0, totalPages: 0 })
const deadlines = ref([])
const loading = ref(true)
const alertLoading = ref(false)
const error = ref('')
const alertError = ref('')
const canCreate = auth.role === ROLES.RH && !props.personnels
const canOpenDetail = auth.role !== ROLES.SALARIE

async function load(nextPage = page.value) {
  loading.value = true
  error.value = ''
  page.value = nextPage
  try {
    response.value = props.personnels
      ? await contratsApi.mesContrats({ page: page.value, size: 20, sort: 'dateDebut,desc' })
      : await contratsApi.rechercher({
          employeId: filters.employeId ? Number(filters.employeId) : undefined,
          type: filters.type || undefined,
          statut: filters.statut || undefined,
          finAvant: filters.finAvant || undefined,
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

async function loadDeadlines() {
  if (props.personnels) return
  alertLoading.value = true
  alertError.value = ''
  try {
    const pageResult = await contratsApi.rechercher({
      statut: 'ACTIF',
      page: 0,
      size: 100,
      sort: 'dateFinPrevue,asc',
    })
    deadlines.value = pageResult.content ?? []
  } catch (cause) {
    alertError.value = cause.message
  } finally {
    alertLoading.value = false
  }
}

function openCreate() {
  router.push({ name: ROUTE_NAMES.CONTRAT_CREATE })
}

onMounted(() => {
  load(0)
  loadDeadlines()
})
</script>

<template>
  <section class="page">
    <header class="page-header">
      <div>
        <p class="eyebrow">{{ personnels ? 'Espace collaborateur' : 'Ressources humaines' }}</p>
        <h1>{{ personnels ? 'Mes contrats' : 'Gestion des contrats' }}</h1>
        <p>
          {{
            personnels
              ? 'Consultez les contrats associés à votre dossier.'
              : 'Consultez les contrats et leur historique sans écraser les versions précédentes.'
          }}
        </p>
      </div>
      <AppButton v-if="canCreate" @click="openCreate">Créer un contrat</AppButton>
    </header>

    <AppAlert v-if="error" variant="error">{{ error }}</AppAlert>
    <template v-if="personnels">
      <p v-if="loading" class="state" role="status">Chargement de vos contrats…</p>
      <ContratTimeline v-else :contrats="response.content" :linkable="canOpenDetail" />
    </template>
    <template v-else>
      <AppAlert v-if="alertError" variant="error">{{ alertError }}</AppAlert>
      <ContratAlertBanner v-if="!alertLoading" :contrats="deadlines" />
      <section class="panel" aria-label="Liste des contrats">
        <form class="filters" role="search" @submit.prevent="load(0)">
          <label
            >Identifiant employé
            <input
              v-model.trim="filters.employeId"
              type="number"
              min="1"
              placeholder="Tous les employés"
            />
          </label>
          <label
            >Type
            <select v-model="filters.type">
              <option value="">Tous les types</option>
              <option v-for="(label, type) in TYPE_CONTRAT_LABELS" :key="type" :value="type">
                {{ label }}
              </option>
            </select>
          </label>
          <label
            >Statut
            <select v-model="filters.statut">
              <option value="">Tous les statuts</option>
              <option v-for="(label, status) in STATUT_CONTRAT_LABELS" :key="status" :value="status">
                {{ label }}
              </option>
            </select>
          </label>
          <label
            >Fin prévue avant
            <input v-model="filters.finAvant" type="date" />
          </label>
          <AppButton type="submit" :loading="loading">Rechercher</AppButton>
        </form>
        <p v-if="loading" class="state" role="status">Chargement des contrats…</p>
        <p v-else-if="!response.content?.length" class="state">Aucun contrat trouvé.</p>
        <ContratTable v-else :contrats="response.content" :linkable="canOpenDetail" />
        <footer v-if="!loading && response.totalElements" class="pagination">
          <span>{{ response.totalElements }} résultat(s)</span>
          <div>
            <AppButton variant="secondary" :disabled="page === 0" @click="load(page - 1)">
              Précédent
            </AppButton>
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
    </template>
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
  min-width: 130px;
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
@media (max-width: 760px) {
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