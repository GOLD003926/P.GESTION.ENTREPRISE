<script setup>
import { ROUTE_NAMES } from '@/constants/routeNames'
import { STATUT_CONTRAT_LABELS, TYPE_CONTRAT_LABELS } from '@/constants/statuts'

defineProps({
  contrats: { type: Array, required: true },
  linkable: { type: Boolean, default: true },
})

function formatDate(value) {
  return value ? new Date(`${value}T00:00:00`).toLocaleDateString('fr-FR') : '—'
}

function formatSalary(contract) {
  return new Intl.NumberFormat('fr-FR', {
    style: 'currency',
    currency: contract.devise || 'MGA',
  }).format(contract.salaireBase)
}
</script>

<template>
  <div class="table-wrap">
    <table>
      <thead>
        <tr>
          <th>Référence</th>
          <th>Employé</th>
          <th>Type</th>
          <th>Poste</th>
          <th>Début</th>
          <th>Fin prévue</th>
          <th>Salaire de base</th>
          <th>Statut</th>
          <th><span class="sr-only">Détail</span></th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="contract in contrats" :key="contract.id">
          <td class="reference">{{ contract.numero }}</td>
          <td>
            <span>{{ contract.employeNom }}</span>
            <small v-if="contract.matricule">{{ contract.matricule }}</small>
          </td>
          <td>{{ TYPE_CONTRAT_LABELS[contract.type] ?? contract.type }}</td>
          <td>{{ contract.posteIntitule || '—' }}</td>
          <td>{{ formatDate(contract.dateDebut) }}</td>
          <td>{{ formatDate(contract.dateFinPrevue) }}</td>
          <td>{{ formatSalary(contract) }}</td>
          <td>
            <span class="status" :class="`status--${contract.statut?.toLowerCase()}`">
              {{ STATUT_CONTRAT_LABELS[contract.statut] ?? contract.statut }}
            </span>
          </td>
          <td>
            <RouterLink
              v-if="linkable"
              :to="{ name: ROUTE_NAMES.CONTRAT_DETAIL, params: { id: contract.id } }"
            >
              Ouvrir
            </RouterLink>
            <span v-else>—</span>
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
td small {
  display: block;
  margin-top: var(--space-1);
  color: var(--color-text-muted);
}
.reference {
  font-weight: var(--font-weight-semibold);
  white-space: nowrap;
}
a {
  color: var(--color-primary);
  font-weight: var(--font-weight-semibold);
}
.status {
  display: inline-flex;
  padding: var(--space-1) var(--space-2);
  border-radius: 999px;
  background: var(--color-info-soft);
  color: var(--color-info);
  font-size: 0.75rem;
  font-weight: var(--font-weight-semibold);
  white-space: nowrap;
}
.status--actif {
  background: var(--color-success-soft);
  color: var(--color-success);
}
.status--termine,
.status--annule {
  background: var(--color-bg);
  color: var(--color-text-muted);
}
.status--a_signer {
  background: var(--color-warning-soft);
  color: var(--color-warning);
}
</style>