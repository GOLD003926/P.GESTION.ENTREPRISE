<script setup>
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import { ROUTE_NAMES } from '@/constants/routeNames'
import AppStatusBadge from '@/components/common/AppStatusBadge.vue'
import OnboardingProgress from './OnboardingProgress.vue'

const props = defineProps({
  onboardings: { type: Array, default: () => [] },
})

const statusLabels = {
  A_DEMARRER: 'À démarrer',
  EN_COURS: 'En cours',
  TERMINE: 'Terminé',
  ANNULE: 'Annulé',
}

const statusTone = (status) => {
  if (status === 'TERMINE') return 'success'
  if (status === 'EN_COURS') return 'info'
  if (status === 'ANNULE') return 'danger'
  if (status === 'A_DEMARRER') return 'warning'
  return 'neutral'
}

const rows = computed(() =>
  props.onboardings.map((onboarding) => ({
    ...onboarding,
    completed: onboarding.tachesObligatoiresFaites ?? 0,
    total: onboarding.tachesObligatoiresTotal ?? 0,
    label: statusLabels[onboarding.statut] ?? onboarding.statut,
  })),
)
</script>

<template>
  <div class="table-wrap">
    <table class="table" aria-label="Liste des onboardings">
      <thead>
        <tr>
          <th>Employé</th>
          <th>Statut</th>
          <th>Début</th>
          <th>Cible</th>
          <th>Progression</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="onboarding in rows" :key="onboarding.id">
          <td>
            <RouterLink :to="{ name: ROUTE_NAMES.ONBOARDING_DETAIL, params: { id: onboarding.id } }">
              <strong>{{ onboarding.employeNom ?? 'Employé' }}</strong>
              <small v-if="onboarding.matricule">{{ onboarding.matricule }}</small>
            </RouterLink>
          </td>
          <td>
            <AppStatusBadge :label="onboarding.label" :tone="statusTone(onboarding.statut)" />
          </td>
          <td>{{ onboarding.dateDebut || '—' }}</td>
          <td>{{ onboarding.dateCible || '—' }}</td>
          <td>
            <OnboardingProgress
              :current="onboarding.completed"
              :total="onboarding.total"
              :label="`${onboarding.completed}/${onboarding.total}`"
            />
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
.table {
  width: 100%;
  border-collapse: collapse;
}
.table th,
.table td {
  padding: 0.9rem 0.75rem;
  border-bottom: 1px solid var(--color-border);
  text-align: left;
  vertical-align: top;
}
.table th {
  color: var(--color-text-muted);
  font-size: 0.76rem;
  font-weight: var(--font-weight-semibold);
  letter-spacing: 0.04em;
  text-transform: uppercase;
}
.table td a {
  display: grid;
  gap: 0.15rem;
  color: var(--color-text);
  text-decoration: none;
}
.table td a small {
  color: var(--color-text-muted);
}
</style>
