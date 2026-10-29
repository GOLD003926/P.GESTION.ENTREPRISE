<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { modelesOnboardingApi } from '@/api/modelesOnboarding'
import AppAlert from '@/components/common/AppAlert.vue'
import AppButton from '@/components/common/AppButton.vue'
import { ROUTE_NAMES } from '@/constants/routeNames'

const router = useRouter()
const modeles = ref([])
const loading = ref(true)
const error = ref('')

async function load() {
  loading.value = true
  error.value = ''

  try {
    const pageResult = await modelesOnboardingApi.rechercher({
      page: 0,
      size: 50,
      sort: 'nom,asc',
    })
    modeles.value = pageResult.content ?? []
  } catch (cause) {
    error.value = cause.message
  } finally {
    loading.value = false
  }
}

async function toggleActive(modele) {
  try {
    if (modele.actif) {
      await modelesOnboardingApi.desactiver(modele.id)
    } else {
      await modelesOnboardingApi.activer(modele.id)
    }
    await load()
  } catch (cause) {
    error.value = cause.message
  }
}

onMounted(load)
</script>

<template>
  <section class="page">
    <header class="page-header">
      <div>
        <p class="eyebrow">Ressources humaines</p>
        <h1>Modèles onboarding</h1>
        <p>Gérez les checklists de démarrage et les tâches standardisées.</p>
      </div>
      <AppButton @click="router.push({ name: ROUTE_NAMES.MODELE_ONBOARDING_CREATE })">
        Nouveau modèle
      </AppButton>
    </header>

    <AppAlert v-if="error" variant="error">{{ error }}</AppAlert>

    <section class="panel" aria-label="Modèles onboarding">
      <p v-if="loading" class="state" role="status">Chargement des modèles…</p>
      <p v-else-if="!modeles.length" class="state">Aucun modèle de checklist n’a été créé.</p>

      <div v-else class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th>Nom</th>
              <th>Type</th>
              <th>Statut</th>
              <th>Tâches</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="modele in modeles" :key="modele.id">
              <td>{{ modele.nom }}</td>
              <td>{{ modele.typeContrat || 'Par défaut' }}</td>
              <td>
                <span :class="['badge', { 'badge--active': modele.actif, 'badge--inactive': !modele.actif }]">
                  {{ modele.actif ? 'Actif' : 'Inactif' }}
                </span>
              </td>
              <td>{{ modele.taches?.length ?? 0 }}</td>
              <td class="actions">
                <AppButton
                  variant="secondary"
                  @click="router.push({ name: ROUTE_NAMES.MODELE_ONBOARDING_EDIT, params: { id: modele.id } })"
                >
                  Modifier
                </AppButton>
                <AppButton @click="toggleActive(modele)">
                  {{ modele.actif ? 'Désactiver' : 'Activer' }}
                </AppButton>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
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
.state {
  padding: var(--space-8) var(--space-4);
  color: var(--color-text-muted);
  text-align: center;
}
.table-wrap {
  overflow-x: auto;
}
.table {
  width: 100%;
  border-collapse: collapse;
}
.table th,
.table td {
  padding: 0.9rem 0.75rem;
  border-bottom: 1px solid var(--color-border);
  text-align: left;
}
.table th {
  color: var(--color-text-muted);
  font-size: 0.76rem;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}
.badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 1.75rem;
  padding: 0.25rem 0.625rem;
  border-radius: 999px;
  font-size: 0.72rem;
  font-weight: var(--font-weight-semibold);
}
.badge--active {
  background: var(--color-success-soft);
  color: var(--color-success);
}
.badge--inactive {
  background: var(--color-bg);
  color: var(--color-text-muted);
}
.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}
@media (max-width: 680px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
