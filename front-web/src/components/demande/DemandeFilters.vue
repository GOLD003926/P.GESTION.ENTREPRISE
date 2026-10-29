<script setup>
import { STATUT_DEMANDE_LABELS } from '@/constants/statuts'

const filters = defineModel('filters', {
  type: Object,
  required: true,
})

defineProps({
  loading: { type: Boolean, default: false },
})

const emit = defineEmits(['submit', 'reset'])
</script>

<template>
  <form class="demande-filters" role="search" @submit.prevent="emit('submit')">
    <label class="demande-filters__field demande-filters__field--query" for="demande-query">
      <span>Référence</span>
      <input
        id="demande-query"
        v-model.trim="filters.q"
        type="search"
        placeholder="Rechercher une référence"
      />
    </label>
    <label class="demande-filters__field" for="demande-status">
      <span>Statut</span>
      <select id="demande-status" v-model="filters.statut">
        <option value="">Tous les statuts</option>
        <option v-for="(label, status) in STATUT_DEMANDE_LABELS" :key="status" :value="status">
          {{ label }}
        </option>
      </select>
    </label>
    <label class="demande-filters__field demande-filters__field--id" for="demande-poste-id">
      <span>ID du poste</span>
      <input id="demande-poste-id" v-model="filters.posteId" type="number" min="1" />
    </label>
    <label class="demande-filters__field demande-filters__field--id" for="demande-department-id">
      <span>ID du département</span>
      <input id="demande-department-id" v-model="filters.departementId" type="number" min="1" />
    </label>
    <div class="demande-filters__actions">
      <button type="submit" :disabled="loading">Filtrer</button>
      <button
        type="button"
        class="demande-filters__reset"
        :disabled="loading"
        @click="emit('reset')"
      >
        Réinitialiser
      </button>
    </div>
  </form>
</template>

<style scoped>
.demande-filters {
  display: grid;
  grid-template-columns: minmax(180px, 1.5fr) repeat(3, minmax(120px, 1fr)) auto;
  align-items: end;
  gap: var(--space-3);
  padding: var(--space-4);
  border-bottom: 1px solid var(--color-border);
}

.demande-filters__field {
  min-width: 0;
  display: grid;
  gap: var(--space-1);
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
}

.demande-filters input,
.demande-filters select {
  width: 100%;
  min-height: var(--control-height);
  padding: 0 var(--space-3);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-text);
  font: inherit;
}

.demande-filters__actions {
  display: flex;
  gap: var(--space-2);
}

.demande-filters__actions button {
  min-height: var(--control-height);
  padding: 0 var(--space-3);
  border: 1px solid var(--color-primary);
  border-radius: var(--radius-sm);
  background: var(--color-primary);
  color: var(--color-on-primary);
  font: inherit;
  font-weight: var(--font-weight-semibold);
  cursor: pointer;
}

.demande-filters__actions .demande-filters__reset {
  border-color: var(--color-border-strong);
  background: var(--color-surface);
  color: var(--color-text);
}

.demande-filters button:disabled {
  cursor: wait;
  opacity: 0.6;
}

.demande-filters input:focus-visible,
.demande-filters select:focus-visible,
.demande-filters button:focus-visible {
  outline: 3px solid var(--color-focus-ring);
  outline-offset: 2px;
}

@media (max-width: 1000px) {
  .demande-filters {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 600px) {
  .demande-filters {
    grid-template-columns: minmax(0, 1fr);
  }

  .demande-filters__actions {
    flex-direction: column;
  }
}
</style>
