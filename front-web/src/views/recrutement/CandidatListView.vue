<script setup>
import { onMounted, ref } from 'vue'
import { candidatsApi } from '@/api/candidats'
import { ROUTE_NAMES } from '@/constants/routeNames'
import AppAlert from '@/components/common/AppAlert.vue'
import AppButton from '@/components/common/AppButton.vue'

const candidats = ref([])
const query = ref('')
const loading = ref(true)
const error = ref('')
async function load() {
  loading.value = true
  error.value = ''
  try {
    const page = await candidatsApi.rechercher({ page: 0, size: 100, q: query.value || undefined })
    candidats.value = page.content ?? []
  } catch (cause) {
    error.value = cause.message
  } finally {
    loading.value = false
  }
}
onMounted(load)
</script>

<template>
  <section class="page">
    <header>
      <div>
        <p class="eyebrow">Recrutement</p>
        <h1>Candidats</h1>
        <p>Gérez les profils enregistrés dans le vivier de candidats.</p>
      </div>
      <RouterLink class="primary-link" :to="{ name: ROUTE_NAMES.CANDIDAT_CREATE }"
        >Ajouter un candidat</RouterLink
      >
    </header>
    <AppAlert v-if="error" variant="error">{{ error }}</AppAlert>
    <form class="filters" @submit.prevent="load">
      <label>Recherche<input v-model.trim="query" placeholder="Nom ou adresse e-mail" /></label
      ><AppButton type="submit" :loading="loading">Rechercher</AppButton>
    </form>
    <div class="panel">
      <p v-if="loading" role="status">Chargement des candidats…</p>
      <p v-else-if="!candidats.length" class="empty">Aucun candidat trouvé.</p>
      <div v-else class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Nom</th>
              <th>E-mail</th>
              <th>Téléphone</th>
              <th>Consentement</th>
              <th />
            </tr>
          </thead>
          <tbody>
            <tr v-for="candidat in candidats" :key="candidat.id">
              <td>{{ candidat.prenom }} {{ candidat.nom }}</td>
              <td>{{ candidat.email }}</td>
              <td>{{ candidat.telephone || '—' }}</td>
              <td>{{ candidat.consentementDonnees ? 'Accordé' : 'Non accordé' }}</td>
              <td>
                <RouterLink :to="{ name: ROUTE_NAMES.CANDIDAT_DETAIL, params: { id: candidat.id } }"
                  >Ouvrir</RouterLink
                >
              </td>
            </tr>
          </tbody>
        </table>
      </div>
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
.filters input {
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
.table-wrap {
  overflow-x: auto;
}
table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
}
th,
td {
  padding: var(--space-3) var(--space-4);
  border-bottom: 1px solid var(--color-border);
  color: var(--color-text);
}
th {
  background: var(--color-bg);
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}
td a {
  color: var(--color-primary);
  font-weight: var(--font-weight-semibold);
  text-decoration: none;
}
.empty {
  padding: var(--space-5);
  color: var(--color-text-muted);
  text-align: center;
}
@media (max-width: 650px) {
  header,
  .filters {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
