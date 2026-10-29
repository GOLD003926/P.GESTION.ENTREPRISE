<script setup>
import { ROUTE_NAMES } from '@/constants/routeNames'
import {
  STATUT_PROPOSITION_LABELS,
  TYPE_CONTRAT_LABELS,
} from '@/constants/statuts'

defineProps({
  propositions: { type: Array, required: true },
})

function formatDate(value) {
  return value ? new Date(`${value}T00:00:00`).toLocaleDateString('fr-FR') : '—'
}

function formatCurrency(value, devise = 'MGA') {
  if (value == null || Number.isNaN(Number(value))) return '—'
  return new Intl.NumberFormat('fr-FR', {
    style: 'currency',
    currency: devise,
    maximumFractionDigits: 2,
  }).format(Number(value))
}
</script>

<template>
  <div class="table-wrap">
    <table>
      <thead>
        <tr>
          <th>Id</th>
          <th>Candidature</th>
          <th>Poste</th>
          <th>Contrat</th>
          <th>Salaire</th>
          <th>Début</th>
          <th>Limite</th>
          <th>Statut</th>
          <th><span class="sr-only">Détail</span></th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in propositions" :key="item.id">
          <td>#{{ item.id }}</td>
          <td>{{ item.candidatureId ?? '—' }}</td>
          <td>{{ item.posteIntitule || '—' }}</td>
          <td>{{ TYPE_CONTRAT_LABELS[item.typeContratPropose] ?? item.typeContratPropose }}</td>
          <td>{{ formatCurrency(item.salairePropose, item.devise) }}</td>
          <td>{{ formatDate(item.dateDebutPrevue) }}</td>
          <td>{{ formatDate(item.dateLimiteReponse) }}</td>
          <td>
            <span class="status" :class="`status--${item.statut?.toLowerCase()}`">
              {{ STATUT_PROPOSITION_LABELS[item.statut] ?? item.statut }}
            </span>
          </td>
          <td>
            <RouterLink
              :to="{ name: ROUTE_NAMES.PROPOSITION_DETAIL, params: { id: item.id } }"
            >
              Ouvrir
            </RouterLink>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
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
  vertical-align: middle;
}

th {
  background: var(--color-bg);
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
  white-space: nowrap;
}

td {
  color: var(--color-text);
  font-size: var(--font-size-sm);
}

a {
  color: var(--color-primary);
  font-weight: var(--font-weight-semibold);
  text-decoration: none;
}

.status {
  display: inline-flex;
  padding: var(--space-1) var(--space-2);
  border-radius: 999px;
  background: var(--color-info-soft);
  color: var(--color-primary);
  font-size: 0.72rem;
  font-weight: var(--font-weight-semibold);
  white-space: nowrap;
}

.status--brouillon,
.status--retiree {
  background: var(--color-bg);
  color: var(--color-text-muted);
}

.status--envoyee,
.status--delai_accorde {
  background: var(--color-warning-soft);
  color: var(--color-warning);
}

.status--acceptee {
  background: var(--color-success-soft);
  color: var(--color-success);
}

.status--refusee,
.status--expiree {
  background: var(--color-danger-soft);
  color: var(--color-danger);
}
</style>
