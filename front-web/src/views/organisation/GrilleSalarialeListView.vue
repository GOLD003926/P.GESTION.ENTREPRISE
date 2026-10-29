<script setup>
import { computed, reactive } from 'vue'
import { grillesSalarialesApi } from '@/api/grillesSalariales'
import OrganisationListPanel from '@/components/organisation/OrganisationListPanel.vue'
import { ROLES } from '@/constants/roles'
import { ROUTE_NAMES } from '@/constants/routeNames'
import { useAuthStore } from '@/stores/auth'
import { useOrganisationList } from '@/composables/useOrganisationList'

const auth = useAuthStore()
const canManage = computed(() => [ROLES.RH, ROLES.ADMIN].includes(auth.role))
const list = reactive(
  useOrganisationList({
    rechercher: grillesSalarialesApi.rechercher,
    activer: grillesSalarialesApi.activer,
    desactiver: grillesSalarialesApi.desactiver,
    label: 'la grille salariale',
  }),
)

function formatSalary(value, currency) {
  return new Intl.NumberFormat('fr-FR', {
    style: 'currency',
    currency,
    maximumFractionDigits: 2,
  }).format(value)
}
</script>

<template>
  <OrganisationListPanel
    title="Grilles salariales"
    description="Consultez les niveaux et les fourchettes de rémunération de l’entreprise."
    :create-route="{ name: ROUTE_NAMES.GRILLE_SALARIALE_CREATE }"
    :can-create="canManage"
    :loading="list.loading"
    :error="list.error"
    :has-items="list.hasItems"
    :query-input="list.queryInput"
    :active-filter="list.activeFilter"
    :page="list.page"
    :total-pages="list.totalPages"
    :total-elements="list.totalElements"
    @search-input="list.queryInput = $event"
    @search="list.search"
    @filter-change="list.activeFilter = $event"
    @filter="list.filterByActive"
    @retry="list.load"
    @previous="list.goToPage(list.page - 1)"
    @next="list.goToPage(list.page + 1)"
  >
    <thead>
      <tr>
        <th scope="col">Code</th>
        <th scope="col">Intitulé</th>
        <th scope="col">Niveau</th>
        <th scope="col">Fourchette salariale</th>
        <th scope="col">Statut</th>
        <th v-if="canManage" scope="col"><span class="sr-only">Actions</span></th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="grid in list.items" :key="grid.id">
        <td class="resource-table__code">{{ grid.code }}</td>
        <td>{{ grid.intitule }}</td>
        <td>{{ grid.niveau }}</td>
        <td>
          {{ formatSalary(grid.salaireMin, grid.devise) }} –
          {{ formatSalary(grid.salaireMax, grid.devise) }}
        </td>
        <td>
          <span class="resource-status" :class="{ 'resource-status--inactive': !grid.actif }">
            {{ grid.actif ? 'Actif' : 'Inactif' }}
          </span>
        </td>
        <td v-if="canManage" class="resource-table__actions">
          <RouterLink
            class="resource-table__action"
            :to="{ name: ROUTE_NAMES.GRILLE_SALARIALE_EDIT, params: { id: grid.id } }"
          >
            Modifier
          </RouterLink>
          <button
            class="resource-table__action"
            type="button"
            :disabled="list.busyId === grid.id"
            @click="list.toggleActive(grid)"
          >
            {{ list.busyId === grid.id ? '…' : grid.actif ? 'Désactiver' : 'Activer' }}
          </button>
        </td>
      </tr>
    </tbody>
  </OrganisationListPanel>
</template>
