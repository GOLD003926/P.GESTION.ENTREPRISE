<script setup>
import { STATUT_CANDIDATURE_LABELS } from '@/constants/statuts'

const filters = defineModel('filters', { type: Object, required: true })
defineProps({ loading: { type: Boolean, default: false } })
const emit = defineEmits(['submit', 'reset'])
</script>

<template>
  <form class="candidature-filters" role="search" @submit.prevent="emit('submit')">
    <label>Offre ID<input v-model="filters.offreId" type="number" min="1" /></label>
    <label
      >Statut<select v-model="filters.statut">
        <option value="">Tous</option>
        <option v-for="(label, key) in STATUT_CANDIDATURE_LABELS" :key="key" :value="key">
          {{ label }}
        </option>
      </select></label
    >
    <label>Candidat ID<input v-model="filters.candidatId" type="number" min="1" /></label>
    <div>
      <button type="submit" :disabled="loading">Filtrer</button
      ><button type="button" :disabled="loading" @click="emit('reset')">Réinitialiser</button>
    </div>
  </form>
</template>

<style scoped>
.candidature-filters {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr)) auto;
  align-items: end;
  gap: var(--space-3);
  padding: var(--space-4);
  border-bottom: 1px solid var(--color-border);
}
.candidature-filters label {
  display: grid;
  gap: var(--space-1);
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}
.candidature-filters input,
.candidature-filters select {
  min-height: var(--control-height);
  padding: 0 var(--space-3);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-text);
  font: inherit;
}
.candidature-filters div {
  display: flex;
  gap: var(--space-2);
}
.candidature-filters button {
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
.candidature-filters button + button {
  border-color: var(--color-border-strong);
  background: var(--color-surface);
  color: var(--color-text);
}
@media (max-width: 800px) {
  .candidature-filters {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
@media (max-width: 520px) {
  .candidature-filters {
    grid-template-columns: 1fr;
  }
}
</style>
