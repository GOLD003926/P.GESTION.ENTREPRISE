<script setup>
defineProps({
  title: { type: String, required: true },
  description: { type: String, required: true },
  createRoute: { type: Object, default: null },
  canCreate: { type: Boolean, default: false },
  loading: { type: Boolean, default: false },
  error: { type: String, default: '' },
  hasItems: { type: Boolean, default: false },
  queryInput: { type: String, default: '' },
  activeFilter: { type: String, default: '' },
  page: { type: Number, default: 0 },
  totalPages: { type: Number, default: 0 },
  totalElements: { type: Number, default: 0 },
})

const emit = defineEmits([
  'search',
  'search-input',
  'filter',
  'filter-change',
  'retry',
  'previous',
  'next',
])

function onFilterChange(event) {
  emit('filter-change', event.target.value)
  emit('filter')
}
</script>

<template>
  <section class="resource-page">
    <header class="resource-page__header">
      <div>
        <p class="resource-page__eyebrow">Référentiel entreprise</p>
        <h1>{{ title }}</h1>
        <p class="resource-page__description">{{ description }}</p>
      </div>
      <RouterLink v-if="canCreate && createRoute" class="resource-page__create" :to="createRoute">
        <span aria-hidden="true">+</span>
        Ajouter
      </RouterLink>
    </header>

    <section class="resource-card" :aria-label="`Liste : ${title}`">
      <form class="resource-filters" role="search" @submit.prevent="emit('search')">
        <label class="resource-filters__search">
          <span>Rechercher</span>
          <input
            :value="queryInput"
            type="search"
            placeholder="Code ou intitulé"
            @input="emit('search-input', $event.target.value)"
          />
        </label>
        <label class="resource-filters__status">
          <span>Statut</span>
          <select :value="activeFilter" @change="onFilterChange">
            <option value="">Tous</option>
            <option value="true">Actifs</option>
            <option value="false">Inactifs</option>
          </select>
        </label>
        <button class="resource-filters__submit" type="submit">Rechercher</button>
      </form>

      <div v-if="error" class="resource-alert" role="alert">
        <p>{{ error }}</p>
        <button type="button" class="resource-alert__retry" @click="emit('retry')">
          Réessayer
        </button>
      </div>

      <p v-if="loading" class="resource-state" role="status">Chargement des données…</p>
      <p v-else-if="!error && !hasItems" class="resource-state">
        Aucun élément ne correspond à votre recherche.
      </p>
      <div v-else-if="!loading && hasItems" class="resource-table-wrap">
        <table class="resource-table">
          <slot />
        </table>
      </div>

      <footer v-if="!loading && totalElements > 0" class="resource-pagination">
        <span>{{ totalElements }} résultat(s)</span>
        <div class="resource-pagination__controls">
          <button
            type="button"
            :disabled="page <= 0"
            aria-label="Page précédente"
            @click="emit('previous')"
          >
            Précédent
          </button>
          <span>Page {{ page + 1 }} sur {{ Math.max(totalPages, 1) }}</span>
          <button
            type="button"
            :disabled="page + 1 >= totalPages"
            aria-label="Page suivante"
            @click="emit('next')"
          >
            Suivant
          </button>
        </div>
      </footer>
    </section>
  </section>
</template>

<style scoped>
.resource-page {
  display: grid;
  gap: var(--space-5);
}

.resource-page__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
}

.resource-page__eyebrow {
  margin: 0 0 var(--space-1);
  color: var(--color-primary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
}

.resource-page__header h1 {
  margin: 0;
  color: var(--color-text);
  font-size: var(--font-size-2xl);
}

.resource-page__description {
  margin: var(--space-2) 0 0;
  color: var(--color-text-muted);
}

.resource-page__create,
.resource-filters__submit {
  min-height: var(--control-height);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  padding: 0 var(--space-4);
  border: 1px solid var(--color-primary);
  border-radius: var(--radius-sm);
  background: var(--color-primary);
  color: var(--color-on-primary);
  font: inherit;
  font-weight: var(--font-weight-semibold);
  text-decoration: none;
  cursor: pointer;
}

.resource-page__create:hover,
.resource-filters__submit:hover {
  background: var(--color-primary-hover);
}

.resource-page__create span {
  font-size: var(--font-size-lg);
}

.resource-card {
  min-width: 0;
  overflow: hidden;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  box-shadow: var(--shadow-card);
}

.resource-filters {
  display: flex;
  align-items: end;
  gap: var(--space-3);
  padding: var(--space-4);
  border-bottom: 1px solid var(--color-border);
}

.resource-filters label {
  display: grid;
  gap: var(--space-1);
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
}

.resource-filters__search {
  min-width: min(100%, 280px);
  flex: 1;
}

.resource-filters input,
.resource-filters select {
  min-height: var(--control-height);
  padding: 0 var(--space-3);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-text);
  font: inherit;
}

.resource-filters input:focus-visible,
.resource-filters select:focus-visible,
.resource-filters__submit:focus-visible,
.resource-page__create:focus-visible,
.resource-pagination button:focus-visible,
.resource-alert__retry:focus-visible {
  outline: 3px solid var(--color-focus-ring);
  outline-offset: 2px;
}

.resource-filters__status {
  min-width: 150px;
}

.resource-state {
  margin: 0;
  padding: var(--space-8) var(--space-4);
  color: var(--color-text-muted);
  text-align: center;
}

.resource-alert {
  margin: var(--space-4);
  padding: var(--space-3) var(--space-4);
  border: 1px solid var(--color-danger-border);
  border-radius: var(--radius-sm);
  background: var(--color-danger-soft);
  color: var(--color-danger);
}

.resource-alert p {
  margin: 0;
}

.resource-alert__retry {
  min-height: 36px;
  margin-top: var(--space-2);
  padding: 0 var(--space-3);
  border: 1px solid var(--color-danger-border);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-danger);
  font: inherit;
  cursor: pointer;
}

.resource-table-wrap {
  overflow-x: auto;
}

.resource-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
}

.resource-pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  border-top: 1px solid var(--color-border);
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}

.resource-pagination__controls {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.resource-pagination button {
  min-height: 36px;
  padding: 0 var(--space-3);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-text);
  font: inherit;
  cursor: pointer;
}

.resource-pagination button:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

@media (max-width: 680px) {
  .resource-page__header {
    align-items: flex-start;
    flex-direction: column;
  }

  .resource-filters {
    align-items: stretch;
    flex-direction: column;
  }

  .resource-filters__search,
  .resource-filters__status {
    min-width: 0;
  }

  .resource-pagination {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
