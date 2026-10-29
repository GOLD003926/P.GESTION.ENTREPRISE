<script setup>
import { STATUT_ENTRETIEN_LABELS } from '@/constants/statuts'
import { ROUTE_NAMES } from '@/constants/routeNames'
defineProps({ entretiens: { type: Array, required: true } })
</script>

<template>
  <div class="recruitment-table-wrap">
    <table class="recruitment-table">
      <thead>
        <tr>
          <th>Candidat</th>
          <th>Niveau</th>
          <th>Date</th>
          <th>Type</th>
          <th>Intervenants</th>
          <th>Statut</th>
          <th />
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in entretiens" :key="item.id">
          <td>{{ item.candidatNom }}</td>
          <td>{{ item.niveau }}</td>
          <td>{{ item.dateHeure ? new Date(item.dateHeure).toLocaleString('fr-FR') : '—' }}</td>
          <td>{{ item.type }}</td>
          <td>{{ item.intervenants?.map((person) => person.utilisateurNom).join(', ') || '—' }}</td>
          <td>{{ STATUT_ENTRETIEN_LABELS[item.statut] ?? item.statut }}</td>
          <td>
            <RouterLink :to="{ name: ROUTE_NAMES.ENTRETIEN_DETAIL, params: { id: item.id } }"
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
.recruitment-table a {
  color: var(--color-primary);
  font-weight: var(--font-weight-semibold);
  text-decoration: none;
  white-space: nowrap;
}
</style>
