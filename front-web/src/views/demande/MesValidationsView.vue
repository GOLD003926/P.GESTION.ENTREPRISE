<script setup>
import { onMounted, ref } from 'vue'
import { validationsApi } from '@/api/validations'
import AppAlert from '@/components/common/AppAlert.vue'
import AppButton from '@/components/common/AppButton.vue'
import DemandeTable from '@/components/demande/DemandeTable.vue'

const demandes = ref([])
const loading = ref(true)
const error = ref('')
const page = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)

async function load() {
  loading.value = true
  error.value = ''
  try {
    const result = await validationsApi.enAttente({
      page: page.value,
      size: 10,
    })
    demandes.value = result.content ?? []
    totalPages.value = result.totalPages ?? 0
    totalElements.value = result.totalElements ?? 0
  } catch (cause) {
    error.value = cause.message
  } finally {
    loading.value = false
  }
}

async function changePage(nextPage) {
  if (nextPage < 0 || nextPage >= totalPages.value) return
  page.value = nextPage
  await load()
}

onMounted(load)
</script>

<template>
  <section class="validations-page">
    <header class="validations-page__header">
      <div>
        <p class="validations-page__eyebrow">Circuit RH → Finance → Direction générale</p>
        <h1>Validations en attente</h1>
        <p>Les demandes affichées correspondent à votre étape de validation actuelle.</p>
      </div>
    </header>

    <section class="validations-page__card" aria-label="Demandes à valider">
      <AppAlert v-if="error" variant="error">
        <div class="validations-page__error">
          <span>{{ error }}</span>
          <AppButton type="button" variant="secondary" @click="load">Réessayer</AppButton>
        </div>
      </AppAlert>
      <p v-if="loading" class="validations-page__state" role="status">Chargement des demandes…</p>
      <p v-else-if="!error && demandes.length === 0" class="validations-page__state">
        Aucune demande n’attend votre validation.
      </p>
      <DemandeTable v-else-if="!error" :demandes="demandes" />
      <footer v-if="!loading && totalElements > 0" class="validations-page__pagination">
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
.validations-page {
  display: grid;
  gap: var(--space-5);
}

.validations-page__eyebrow {
  margin: 0 0 var(--space-1);
  color: var(--color-primary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
}

.validations-page__header h1 {
  margin: 0;
  color: var(--color-text);
  font-size: var(--font-size-2xl);
}

.validations-page__header p:last-child {
  margin: var(--space-2) 0 0;
  color: var(--color-text-muted);
}

.validations-page__card {
  min-width: 0;
  overflow: hidden;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  box-shadow: var(--shadow-card);
}

.validations-page__state {
  margin: 0;
  padding: var(--space-8) var(--space-4);
  color: var(--color-text-muted);
  text-align: center;
}

.validations-page__error {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
}

.validations-page__pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  border-top: 1px solid var(--color-border);
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}

.validations-page__pagination div {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.validations-page__pagination button {
  min-height: 36px;
  padding: 0 var(--space-3);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-text);
  font: inherit;
  cursor: pointer;
}

.validations-page__pagination button:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.validations-page__pagination button:focus-visible {
  outline: 3px solid var(--color-focus-ring);
  outline-offset: 2px;
}

@media (max-width: 700px) {
  .validations-page__pagination,
  .validations-page__pagination div {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
