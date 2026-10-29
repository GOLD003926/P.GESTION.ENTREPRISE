<script setup>
import { computed, reactive } from 'vue'
import { postesApi } from '@/api/postes'
import OrganisationListPanel from '@/components/organisation/OrganisationListPanel.vue'
import PosteOccupationBadge from '@/components/organisation/PosteOccupationBadge.vue'
import { ROLES } from '@/constants/roles'
import { ROUTE_NAMES } from '@/constants/routeNames'
import { useAuthStore } from '@/stores/auth'
import { useOrganisationList } from '@/composables/useOrganisationList'

const auth = useAuthStore()
const canManage = computed(() => auth.role === ROLES.RH)
const list = reactive(
  useOrganisationList({
    rechercher: postesApi.rechercher,
    activer: postesApi.activer,
    desactiver: postesApi.desactiver,
    label: 'le poste',
  }),
)

function formatSalary(value) {
  return new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 2 }).format(value)
}
</script>

<template>
  <OrganisationListPanel
    title="Postes"
    description="Consultez les postes, leur rattachement et les effectifs autorisés."
    :create-route="{ name: ROUTE_NAMES.POSTE_CREATE }"
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
        <th scope="col">Poste</th>
        <th scope="col">Département</th>
        <th scope="col">Grille salariale</th>
        <th scope="col">Salaire mensuel</th>
        <th scope="col">Occupation</th>
        <th scope="col">Statut</th>
        <th v-if="canManage" scope="col"><span class="sr-only">Actions</span></th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="position in list.items" :key="position.id">
        <td class="resource-table__code">{{ position.code }}</td>
        <td>{{ position.intitule }}</td>
        <td>{{ position.departementNom }}</td>
        <td>{{ position.grilleSalarialeIntitule }}</td>
        <td>{{ formatSalary(position.salaireMin) }} – {{ formatSalary(position.salaireMax) }}</td>
        <td>
          <PosteOccupationBadge
            :occupation-actuelle="position.occupationActuelle"
            :effectif-autorise="position.effectifAutorise"
          />
          <small class="resource-table__places">
            {{ position.placesLibres }} place{{ position.placesLibres === 1 ? '' : 's' }} libre{{
              position.placesLibres === 1 ? '' : 's'
            }}
          </small>
        </td>
        <td>
          <span class="resource-status" :class="{ 'resource-status--inactive': !position.actif }">
            {{ position.actif ? 'Actif' : 'Inactif' }}
          </span>
        </td>
        <td v-if="canManage" class="resource-table__actions">
          <RouterLink
            class="resource-table__action"
            :to="{ name: ROUTE_NAMES.POSTE_EDIT, params: { id: position.id } }"
          >
            Modifier
          </RouterLink>
          <button
            class="resource-table__action"
            type="button"
            :disabled="list.busyId === position.id"
            @click="list.toggleActive(position)"
          >
            {{ list.busyId === position.id ? '…' : position.actif ? 'Désactiver' : 'Activer' }}
          </button>
        </td>
      </tr>
    </tbody>
  </OrganisationListPanel>
</template>
