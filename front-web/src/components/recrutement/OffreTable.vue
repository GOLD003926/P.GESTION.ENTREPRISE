<script setup>
import { STATUT_OFFRE_LABELS } from '@/constants/statuts'
import { ROUTE_NAMES } from '@/constants/routeNames'

defineProps({ offres: { type: Array, required: true } })
</script>

<template>
  <div class="recruitment-table-wrap">
    <table class="recruitment-table">
      <thead>
        <tr>
          <th>Référence</th>
          <th>Offre</th>
          <th>Demande</th>
          <th>Postes</th>
          <th>Clôture</th>
          <th>Statut</th>
          <th />
        </tr>
      </thead>
      <tbody>
        <tr v-for="offre in offres" :key="offre.id">
          <td>
            <strong>{{ offre.reference }}</strong>
          </td>
          <td>
            {{ offre.titre }}<small>{{ offre.posteIntitule }}</small>
          </td>
          <td>{{ offre.demandeReference }}</td>
          <td>{{ offre.nombrePostes }}</td>
          <td>{{ offre.dateCloture || '—' }}</td>
          <td>{{ STATUT_OFFRE_LABELS[offre.statut] ?? offre.statut }}</td>
          <td>
            <RouterLink :to="{ name: ROUTE_NAMES.OFFRE_DETAIL, params: { id: offre.id } }"
              >Ouvrir</RouterLink
            >
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.recruitment-table-wrap {
  overflow-x: auto;
}
.recruitment-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
}
.recruitment-table th,
.recruitment-table td {
  padding: var(--space-3) var(--space-4);
  border-bottom: 1px solid var(--color-border);
  vertical-align: middle;
}
.recruitment-table th {
  background: var(--color-bg);
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}
.recruitment-table td {
  color: var(--color-text);
  font-size: var(--font-size-sm);
}
.recruitment-table small {
  display: block;
  margin-top: var(--space-1);
  color: var(--color-text-muted);
}
.recruitment-table a {
  color: var(--color-primary);
  font-weight: var(--font-weight-semibold);
  text-decoration: none;
  white-space: nowrap;
}
.recruitment-table a:hover {
  text-decoration: underline;
}
</style>
