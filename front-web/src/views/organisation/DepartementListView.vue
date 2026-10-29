<script setup>
import { computed, reactive } from 'vue'
import { departementsApi } from '@/api/departements'
import OrganisationListPanel from '@/components/organisation/OrganisationListPanel.vue'
import { ROLES } from '@/constants/roles'
import { ROUTE_NAMES } from '@/constants/routeNames'
import { useAuthStore } from '@/stores/auth'
import { useOrganisationList } from '@/composables/useOrganisationList'

const auth = useAuthStore()
const canManage = computed(() => [ROLES.RH, ROLES.ADMIN].includes(auth.role))
const list = reactive(
  useOrganisationList({
    rechercher: departementsApi.rechercher,
    activer: departementsApi.activer,
    desactiver: departementsApi.desactiver,
    label: 'le département',
  }),
)
</script>

<template>
  <OrganisationListPanel
    title="Départements"
    description="Consultez la structure hiérarchique et gérez les départements de l’entreprise."
    :create-route="{ name: ROUTE_NAMES.DEPARTEMENT_CREATE }"
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
        <th scope="col">Département</th>
        <th scope="col">Parent</th>
        <th scope="col">Responsable ID</th>
        <th scope="col">Statut</th>
        <th v-if="canManage" scope="col"><span class="sr-only">Actions</span></th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="department in list.items" :key="department.id">
        <td class="resource-table__code">{{ department.code }}</td>
        <td>{{ department.nom }}</td>
        <td>{{ department.departementParentNom || '—' }}</td>
        <td>{{ department.responsableId ?? '—' }}</td>
        <td>
          <span class="resource-status" :class="{ 'resource-status--inactive': !department.actif }">
            {{ department.actif ? 'Actif' : 'Inactif' }}
          </span>
        </td>
        <td v-if="canManage" class="resource-table__actions">
          <RouterLink
            class="resource-table__action"
            :to="{ name: ROUTE_NAMES.DEPARTEMENT_EDIT, params: { id: department.id } }"
          >
            Modifier
          </RouterLink>
          <button
            class="resource-table__action"
            type="button"
            :disabled="list.busyId === department.id"
            @click="list.toggleActive(department)"
          >
            {{ list.busyId === department.id ? '…' : department.actif ? 'Désactiver' : 'Activer' }}
          </button>
        </td>
      </tr>
    </tbody>
  </OrganisationListPanel>
</template>
