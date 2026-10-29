<script setup>
import { computed } from 'vue'
import { ROUTE_NAMES } from '@/constants/routeNames'
import { STATUT_CONTRAT_LABELS, TYPE_CONTRAT_LABELS } from '@/constants/statuts'

const props = defineProps({
  contrats: { type: Array, required: true },
  linkable: { type: Boolean, default: true },
  canCreate: { type: Boolean, default: false },
  employeId: { type: [Number, String], default: null },
})

const orderedContracts = computed(() =>
  [...props.contrats].sort((a, b) => {
    const dateOrder = (a.dateDebut ?? '').localeCompare(b.dateDebut ?? '')
    return dateOrder || (a.numeroRenouvellement ?? 0) - (b.numeroRenouvellement ?? 0)
  }),
)

function formatDate(value) {
  return value ? new Date(`${value}T00:00:00`).toLocaleDateString('fr-FR') : '—'
}
</script>

<template>
  <section class="timeline" aria-label="Historique des contrats">
    <header>
      <div>
        <h2>Parcours contractuel</h2>
        <p>Chaque renouvellement ou transformation conserve le contrat précédent.</p>
      </div>
      <RouterLink
        v-if="canCreate && employeId"
        class="create-link"
        :to="{ name: ROUTE_NAMES.CONTRAT_CREATE, query: { employeId } }"
      >
        Créer un contrat
      </RouterLink>
    </header>
    <ol v-if="orderedContracts.length">
      <li v-for="contract in orderedContracts" :key="contract.id">
        <span class="marker" aria-hidden="true"></span>
        <article>
          <div class="title-row">
            <strong>{{ TYPE_CONTRAT_LABELS[contract.type] ?? contract.type }}</strong>
            <span class="status">{{ STATUT_CONTRAT_LABELS[contract.statut] ?? contract.statut }}</span>
          </div>
          <p>
            {{ contract.numero }} · {{ formatDate(contract.dateDebut) }} –
            {{ formatDate(contract.dateFinPrevue) }}
          </p>
          <RouterLink
            v-if="linkable"
            :to="{ name: ROUTE_NAMES.CONTRAT_DETAIL, params: { id: contract.id } }"
          >
            Consulter le contrat
          </RouterLink>
        </article>
      </li>
    </ol>
    <p v-else class="empty">Aucun contrat enregistré.</p>
  </section>
</template>

<style scoped>
.timeline {
  display: grid;
  gap: var(--space-4);
}
header,
.title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
}
h2 {
  margin: 0;
  color: var(--color-text);
  font-size: var(--font-size-lg);
}
header p,
article p {
  margin: var(--space-1) 0 0;
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}
.create-link,
article a {
  color: var(--color-primary);
  font-weight: var(--font-weight-semibold);
  white-space: nowrap;
}
ol {
  display: grid;
  gap: var(--space-3);
  margin: 0;
  padding: 0;
  list-style: none;
}
li {
  position: relative;
  display: grid;
  grid-template-columns: 16px 1fr;
  gap: var(--space-3);
}
li:not(:last-child)::before {
  position: absolute;
  top: 16px;
  bottom: calc(-1 * var(--space-3));
  left: 7px;
  width: 2px;
  background: var(--color-border);
  content: '';
}
.marker {
  z-index: 1;
  width: 14px;
  height: 14px;
  margin-top: 2px;
  border: 3px solid var(--color-primary);
  border-radius: 50%;
  background: var(--color-surface);
}
article {
  padding: var(--space-3);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
}
.status {
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}
.empty {
  margin: 0;
  padding: var(--space-4);
  border-radius: var(--radius-sm);
  background: var(--color-bg);
  color: var(--color-text-muted);
  text-align: center;
}
@media (max-width: 600px) {
  header,
  .title-row {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>