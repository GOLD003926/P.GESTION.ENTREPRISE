<script setup>
import { STATUT_CANDIDATURE_LABELS } from '@/constants/statuts'
import { ROUTE_NAMES } from '@/constants/routeNames'

defineProps({ candidatures: { type: Array, required: true } })
const stages = [
  'RECUE',
  'PRESELECTIONNEE',
  'EN_ENTRETIEN',
  'RETENUE',
  'PROPOSITION_ENVOYEE',
  'EMBAUCHEE',
  'REFUSEE_DOSSIER',
  'NON_RETENUE',
  'LISTE_ATTENTE',
  'DESISTEE',
]
</script>

<template>
  <section class="pipeline" aria-label="Candidatures par statut">
    <div v-for="status in stages" :key="status" class="pipeline__column">
      <h2>
        {{ STATUT_CANDIDATURE_LABELS[status] }}
        <span>{{ candidatures.filter((item) => item.statut === status).length }}</span>
      </h2>
      <RouterLink
        v-for="item in candidatures.filter((candidate) => candidate.statut === status)"
        :key="item.id"
        class="pipeline__card"
        :to="{ name: ROUTE_NAMES.CANDIDATURE_DETAIL, params: { id: item.id } }"
      >
        <strong>{{ item.candidatNom }}</strong>
        <span>{{ item.offreTitre }}</span>
        <small>{{ item.offreReference }}</small>
      </RouterLink>
      <p v-if="!candidatures.some((item) => item.statut === status)" class="pipeline__empty">
        Aucune
      </p>
    </div>
  </section>
</template>

<style scoped>
.pipeline {
  display: grid;
  grid-auto-columns: minmax(190px, 1fr);
  grid-auto-flow: column;
  gap: var(--space-3);
  overflow-x: auto;
  padding: var(--space-4);
}
.pipeline__column {
  min-height: 220px;
  padding: var(--space-3);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-bg);
}
.pipeline__column h2 {
  display: flex;
  justify-content: space-between;
  gap: var(--space-2);
  margin: 0 0 var(--space-3);
  color: var(--color-text);
  font-size: var(--font-size-sm);
}
.pipeline__column h2 span {
  color: var(--color-text-muted);
}
.pipeline__card {
  display: grid;
  gap: var(--space-1);
  margin-bottom: var(--space-2);
  padding: var(--space-3);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-text);
  text-decoration: none;
}
.pipeline__card:hover {
  border-color: var(--color-primary);
}
.pipeline__card span,
.pipeline__card small,
.pipeline__empty {
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}
.pipeline__empty {
  text-align: center;
}
</style>
