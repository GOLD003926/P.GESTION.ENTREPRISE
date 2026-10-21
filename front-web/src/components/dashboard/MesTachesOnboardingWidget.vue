<script setup>
import { computed, onMounted, ref } from 'vue'
import { onboardingTachesApi } from '@/api/onboardingTaches'
import { onboardingsApi } from '@/api/onboardings'

const props = defineProps({
  mode: {
    type: String,
    default: 'tasks',
    validator: (value) => ['tasks', 'employee'].includes(value),
  },
})

const tasks = ref([])
const totalTasks = ref(0)
const onboarding = ref(null)
const loading = ref(true)
const error = ref('')
const noOnboarding = ref(false)
const isEmployee = computed(() => props.mode === 'employee')

const completedTasks = computed(() => tasks.value.filter((task) => task.statut === 'FAIT').length)

function statusLabel(status) {
  const labels = {
    A_FAIRE: 'À faire',
    EN_COURS: 'En cours',
    FAIT: 'Terminée',
    NON_APPLICABLE: 'Non applicable',
  }
  return labels[status] ?? status
}

function formatDate(value) {
  if (!value) return 'Sans échéance'
  return new Intl.DateTimeFormat('fr-FR', { dateStyle: 'medium' }).format(new Date(value))
}

async function load() {
  loading.value = true
  error.value = ''
  noOnboarding.value = false
  totalTasks.value = 0
  try {
    if (isEmployee.value) {
      onboarding.value = await onboardingsApi.monOnboarding()
      tasks.value = onboarding.value.taches ?? []
      totalTasks.value = tasks.value.length
    } else {
      const page = await onboardingTachesApi.mesTaches({ page: 0, size: 5, sort: 'echeance,asc' })
      tasks.value = page.content
      totalTasks.value = page.totalElements
    }
  } catch (cause) {
    if (isEmployee.value && cause.status === 404) {
      noOnboarding.value = true
      onboarding.value = null
      tasks.value = []
    } else {
      error.value = cause.message
    }
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <section class="widget" aria-labelledby="onboarding-tasks-title">
    <header class="widget__header">
      <div>
        <p class="widget__eyebrow">Intégration</p>
        <h3 id="onboarding-tasks-title">
          {{ isEmployee ? 'Mon onboarding' : 'Tâches à traiter' }}
        </h3>
      </div>
      <span v-if="!loading && !error && !noOnboarding" class="widget__count">
        {{
          isEmployee
            ? `${onboarding?.tachesObligatoiresFaites ?? 0}/${onboarding?.tachesObligatoiresTotal ?? 0}`
            : totalTasks
        }}
      </span>
    </header>

    <p v-if="loading" class="widget__message" role="status">Chargement des tâches…</p>
    <div v-else-if="error" class="widget__error" role="alert">
      <p>{{ error }}</p>
      <button class="widget__retry" type="button" @click="load">Réessayer</button>
    </div>
    <p v-else-if="noOnboarding" class="widget__message">
      Aucun onboarding n’est encore associé à votre dossier.
    </p>
    <p v-else-if="tasks.length === 0" class="widget__message">
      {{
        isEmployee
          ? 'Aucune tâche ne figure dans votre checklist.'
          : 'Aucune tâche à traiter pour le moment.'
      }}
    </p>
    <ul v-else class="widget__list">
      <li v-for="task in tasks.slice(0, 5)" :key="task.id" class="widget__item">
        <div class="widget__item-main">
          <strong>{{ task.libelle }}</strong>
          <span v-if="!isEmployee"
            >{{ task.employeNom
            }}<template v-if="task.matricule"> · {{ task.matricule }}</template></span
          >
          <span v-else>{{ task.obligatoire ? 'Obligatoire' : 'Facultative' }}</span>
        </div>
        <div class="widget__item-meta">
          <span :class="{ widget__late: task.enRetard }">
            {{ task.enRetard ? 'En retard' : formatDate(task.echeance) }}
          </span>
          <span class="widget__status">{{ statusLabel(task.statut) }}</span>
        </div>
      </li>
    </ul>
    <p v-if="isEmployee && onboarding && !loading && !error" class="widget__footnote">
      {{ completedTasks }} tâche(s) terminée(s) sur {{ tasks.length }}.
    </p>
    <p v-if="!isEmployee && !loading && !error && tasks.length > 0" class="widget__footnote">
      La liste affiche les cinq prochaines échéances.
    </p>
  </section>
</template>
