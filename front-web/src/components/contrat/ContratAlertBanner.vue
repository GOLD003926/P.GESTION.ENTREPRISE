<script setup>
import { computed } from 'vue'
import { ROUTE_NAMES } from '@/constants/routeNames'

const props = defineProps({ contrats: { type: Array, required: true } })
const contractsWithEndDate = computed(() =>
  props.contrats
    .filter((contract) => contract.dateFinPrevue && contract.joursRestants != null)
    .sort((a, b) => a.joursRestants - b.joursRestants),
)

function deadlineText(days) {
  if (days < 0) return `Échéance dépassée de ${Math.abs(days)} jour(s)`
  if (days === 0) return 'Échéance aujourd’hui'
  return `Échéance dans ${days} jour(s)`
}
</script>

<template>
  <section v-if="contractsWithEndDate.length" class="banner" aria-labelledby="contract-deadline-title">
    <header>
      <div>
        <h2 id="contract-deadline-title">Suivi des échéances contractuelles</h2>
        <p>Les jours restants sont calculés par l’API à partir de la date de fin prévue.</p>
      </div>
    </header>
    <ul>
      <li v-for="contract in contractsWithEndDate" :key="contract.id">
        <RouterLink :to="{ name: ROUTE_NAMES.CONTRAT_DETAIL, params: { id: contract.id } }">
          {{ contract.numero }} · {{ contract.employeNom }}
        </RouterLink>
        <span>{{ deadlineText(contract.joursRestants) }}</span>
      </li>
    </ul>
  </section>
</template>

<style scoped>
.banner {
  padding: var(--space-4);
  border: 1px solid var(--color-warning-border);
  border-radius: var(--radius-md);
  background: var(--color-warning-soft);
  color: var(--color-text);
}
h2 {
  margin: 0;
  font-size: var(--font-size-base);
}
p {
  margin: var(--space-1) 0 0;
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}
ul {
  display: grid;
  gap: var(--space-2);
  margin: var(--space-3) 0 0;
  padding-left: var(--space-5);
}
li {
  display: flex;
  justify-content: space-between;
  gap: var(--space-3);
  font-size: var(--font-size-sm);
}
a {
  color: var(--color-primary);
  font-weight: var(--font-weight-semibold);
}
@media (max-width: 600px) {
  li {
    flex-direction: column;
  }
}
</style>