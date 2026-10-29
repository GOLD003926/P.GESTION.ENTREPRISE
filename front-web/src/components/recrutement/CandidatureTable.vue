<script setup>
import { ROUTE_NAMES } from '@/constants/routeNames'
import { STATUT_CANDIDATURE_LABELS } from '@/constants/statuts'
defineProps({ candidatures: { type: Array, required: true } })
</script>

<template>
  <div class="recruitment-table-wrap">
    <table class="recruitment-table">
      <thead>
        <tr>
          <th>Candidat</th>
          <th>Offre</th>
          <th>Déposée le</th>
          <th>Statut</th>
          <th>Score</th>
          <th />
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in candidatures" :key="item.id">
          <td>
            <strong>{{ item.candidatNom }}</strong
            ><small>{{ item.candidatEmail }}</small>
          </td>
          <td>
            {{ item.offreReference }}<small>{{ item.offreTitre }}</small>
          </td>
          <td>{{ item.dateDepot ? new Date(item.dateDepot).toLocaleDateString('fr-FR') : '—' }}</td>
          <td>{{ STATUT_CANDIDATURE_LABELS[item.statut] ?? item.statut }}</td>
          <td>{{ item.scorePreselection ?? '—' }}</td>
          <td>
            <RouterLink :to="{ name: ROUTE_NAMES.CANDIDATURE_DETAIL, params: { id: item.id } }"
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
}
</style>
