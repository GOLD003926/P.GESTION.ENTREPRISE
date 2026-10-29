<script setup>
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { ROLES } from '@/constants/roles'
import { ROUTE_NAMES } from '@/constants/routeNames'
import { STATUT_EMPLOYE_LABELS } from '@/constants/statuts'

defineProps({ employes: { type: Array, required: true } })
const auth = useAuthStore()
const showContact = computed(() => auth.role === ROLES.RH)
</script>

<template>
  <div class="employee-table-wrap">
    <table class="employee-table">
      <thead>
        <tr>
          <th scope="col">Matricule</th>
          <th scope="col">Employé</th>
          <th v-if="showContact" scope="col">E-mail professionnel</th>
          <th scope="col">Poste actuel</th>
          <th scope="col">Date d’embauche</th>
          <th scope="col">Statut</th>
          <th scope="col"><span class="sr-only">Détail</span></th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="employe in employes" :key="employe.id">
          <td class="matricule">{{ employe.matricule }}</td>
          <td>{{ employe.prenom }} {{ employe.nom }}</td>
          <td v-if="showContact">{{ employe.emailPro || '—' }}</td>
          <td>{{ employe.posteActuelIntitule || '—' }}</td>
          <td>{{ employe.dateEmbauche || '—' }}</td>
          <td>
            <span class="status" :class="`status--${employe.statut?.toLowerCase()}`">
              {{ STATUT_EMPLOYE_LABELS[employe.statut] ?? employe.statut }}
            </span>
          </td>
          <td>
            <RouterLink :to="{ name: ROUTE_NAMES.EMPLOYE_DETAIL, params: { id: employe.id } }">
              Ouvrir
            </RouterLink>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.employee-table-wrap {
  overflow-x: auto;
}

.employee-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
}

.employee-table th,
.employee-table td {
  padding: var(--space-3) var(--space-4);
  border-bottom: 1px solid var(--color-border);
  vertical-align: middle;
}

.employee-table th {
  background: var(--color-bg);
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}

.employee-table td {
  color: var(--color-text);
  font-size: var(--font-size-sm);
}

.matricule {
  font-weight: var(--font-weight-semibold);
  white-space: nowrap;
}

.employee-table a {
  color: var(--color-primary);
  font-weight: var(--font-weight-semibold);
  text-decoration: none;
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

.status--suspendu {
  background: var(--color-warning-soft);
  color: var(--color-warning);
}

.status--sorti {
  background: var(--color-bg);
  color: var(--color-text-muted);
}

@media (max-width: 680px) {
  .employee-table th,
  .employee-table td {
    padding: var(--space-3);
  }
}
</style>
