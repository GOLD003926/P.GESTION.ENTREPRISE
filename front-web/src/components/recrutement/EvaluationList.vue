<script setup>
import { AVIS_LABELS } from '@/constants/statuts'
defineProps({ evaluations: { type: Array, required: true } })
</script>

<template>
  <div v-if="!evaluations.length" class="empty">Aucune évaluation pour cet entretien.</div>
  <ul v-else class="evaluation-list">
    <li v-for="item in evaluations" :key="item.id">
      <header>
        <strong>{{ item.evaluateurNom }}</strong
        ><span>{{ item.noteGlobale }}/20 · {{ AVIS_LABELS[item.avis] ?? item.avis }}</span>
      </header>
      <p>{{ item.commentaire || 'Aucun commentaire.' }}</p>
      <ul v-if="item.criteres?.length" class="criteria">
        <li v-for="criterion in item.criteres" :key="criterion.critere">
          {{ criterion.critere }} : {{ criterion.note }}/20
        </li>
      </ul>
      <small
        >{{ item.dateEvaluation ? new Date(item.dateEvaluation).toLocaleString('fr-FR') : ''
        }}<template v-if="item.verrouillee"> · Verrouillée</template></small
      >
    </li>
  </ul>
</template>

<style scoped>
.evaluation-list {
  display: grid;
  gap: var(--space-3);
  margin: 0;
  padding: 0;
  list-style: none;
}
.evaluation-list > li {
  padding: var(--space-4);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
}
.evaluation-list header {
  display: flex;
  justify-content: space-between;
  gap: var(--space-3);
  color: var(--color-text);
}
.evaluation-list p,
.evaluation-list small,
.empty {
  color: var(--color-text-muted);
}
.criteria {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-3);
  padding-left: var(--space-4);
  color: var(--color-text);
}
</style>
