import { computed, onMounted, ref } from 'vue'

export function useOrganisationList({ rechercher, activer, desactiver, label }) {
  const items = ref([])
  const loading = ref(true)
  const error = ref('')
  const queryInput = ref('')
  const query = ref('')
  const activeFilter = ref('')
  const page = ref(0)
  const totalPages = ref(0)
  const totalElements = ref(0)
  const size = 10
  const busyId = ref(null)
  const hasItems = computed(() => items.value.length > 0)

  function requestParams() {
    const params = { page: page.value, size }
    if (query.value) params.q = query.value
    if (activeFilter.value) params.actif = activeFilter.value === 'true'
    return params
  }

  async function load() {
    loading.value = true
    error.value = ''
    try {
      const result = await rechercher(requestParams())
      items.value = result.content
      totalPages.value = result.totalPages
      totalElements.value = result.totalElements
    } catch (cause) {
      error.value = cause.message
    } finally {
      loading.value = false
    }
  }

  async function search() {
    query.value = queryInput.value.trim()
    page.value = 0
    await load()
  }

  async function filterByActive() {
    page.value = 0
    await load()
  }

  async function goToPage(nextPage) {
    if (nextPage < 0 || nextPage >= totalPages.value) return
    page.value = nextPage
    await load()
  }

  async function toggleActive(item) {
    const verb = item.actif ? 'désactiver' : 'activer'
    if (!window.confirm(`Voulez-vous ${verb} ${label} « ${item.nom ?? item.intitule} » ?`)) return

    busyId.value = item.id
    error.value = ''
    try {
      await (item.actif ? desactiver : activer)(item.id)
      await load()
    } catch (cause) {
      error.value = cause.message
    } finally {
      busyId.value = null
    }
  }

  onMounted(load)

  return {
    items,
    loading,
    error,
    queryInput,
    activeFilter,
    page,
    totalPages,
    totalElements,
    busyId,
    hasItems,
    load,
    search,
    filterByActive,
    goToPage,
    toggleActive,
  }
}
