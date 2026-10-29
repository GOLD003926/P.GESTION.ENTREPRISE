<script setup>
import { ROUTE_NAMES } from '@/constants/routeNames'
import {
  STATUT_DEMANDE_LABELS,
  STATUT_DEMANDE_VARIANTS,
  TYPE_DEMANDE_LABELS,
} from '@/constants/statuts'

defineProps({
  demandes: { type: Array, required: true },
})

function formatDate(value) {
  if (!value) return '—'
  return new Intl.DateTimeFormat('fr-FR', { dateStyle: 'medium' }).format(
    new Date(`${value}T00:00:00`),
  )
}

function formatBudget(value) {
  if (value === null || value === undefined) return '—'
  const amount = Number(value)
  if (!Number.isFinite(amount)) return '—'
  return new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 2 }).format(amount)
}
</script>

<template>
  <div class="demande-table-wrap">
    <table class="demande-table">
      <thead>
        <tr>
          <th scope="col">Référence</th>
          <th scope="col">Poste</th>
          <th scope="col">Type</th>
          <th scope="col">Effectif</th>
          <th scope="col">Date souhaitée</th>
          <th scope="col">Budget estimé</th>
          <th scope="col">Statut</th>
          <th scope="col"><span class="sr-only">Détail</span></th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="demande in demandes" :key="demande.id">
          <td class="demande-table__reference">{{ demande.reference }}</td>
          <td>
            <span>{{ demande.posteIntitule || `Poste #${demande.posteId}` }}</span>
            <small>{{ demande.departementNom || `Département #${demande.departementId}` }}</small>
          </td>
          <td>{{ TYPE_DEMANDE_LABELS[demande.typeDemande] ?? demande.typeDemande }}</td>
          <td>{{ demande.effectifDemande }}</td>
          <td>{{ formatDate(demande.dateSouhaitee) }}</td>
          <td>{{ formatBudget(demande.budgetEstime) }}</td>
          <td>
            <span
              class="demande-status"
              :class="`demande-status--${STATUT_DEMANDE_VARIANTS[demande.statut] ?? 'neutral'}`"
            >
              {{ STATUT_DEMANDE_LABELS[demande.statut] ?? demande.statut }}
            </span>
          </td>
          <td>
            <RouterLink
              class="demande-table__link"
              :to="{ name: ROUTE_NAMES.DEMANDE_DETAIL, params: { id: demande.id } }"
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
.demande-table-wrap {
  overflow-x: auto;
}

.demande-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
}

.demande-table th,
.demande-table td {
  padding: var(--space-3) var(--space-4);
  border-bottom: 1px solid var(--color-border);
  vertical-align: middle;
}

.demande-table th {
  background: var(--color-bg);
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  white-space: nowrap;
}

.demande-table td {
  color: var(--color-text);
  font-size: var(--font-size-sm);
}

.demande-table td small {
  display: block;
  margin-top: var(--space-1);
  color: var(--color-text-muted);
}

.demande-table__reference {
  font-weight: var(--font-weight-semibold);
  white-space: nowrap;
}

.demande-status {
  display: inline-flex;
  padding: var(--space-1) var(--space-2);
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: var(--font-weight-semibold);
  white-space: nowrap;
}

.demande-status--neutral {
  background: var(--color-bg);
  color: var(--color-text-muted);
}

.demande-status--info {
  background: var(--color-info-soft);
  color: var(--color-info);
}

.demande-status--warning {
  background: var(--color-warning-soft);
  color: var(--color-warning);
}

.demande-status--danger {
  background: var(--color-danger-soft);
  color: var(--color-danger);
}

.demande-status--success {
  background: var(--color-success-soft);
  color: var(--color-success);
}

.demande-table__link {
  display: inline-flex;
  align-items: center;
  min-height: 36px;
  color: var(--color-primary);
  font-weight: var(--font-weight-semibold);
  text-decoration: none;
  white-space: nowrap;
}

.demande-table__link:hover {
  text-decoration: underline;
}

.demande-table__link:focus-visible {
  outline: 3px solid var(--color-focus-ring);
  outline-offset: 2px;
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  clip-path: inset(50%);
}
</style>
