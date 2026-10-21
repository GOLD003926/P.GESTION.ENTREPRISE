<script setup>
import { onMounted, ref } from 'vue'
import { auditApi } from '@/api/audit'

const entries = ref([])
const loading = ref(true)
const error = ref('')

async function load() {
  loading.value = true
  error.value = ''
  try {
    const page = await auditApi.journal({ page: 0, size: 5, sort: 'dateAction,desc' })
    entries.value = page.content
  } catch (cause) {
    error.value = cause.message
  } finally {
    loading.value = false
  }
}

function formatDate(value) {
  if (!value) return 'Date inconnue'
  return new Intl.DateTimeFormat('fr-FR', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value))
}

onMounted(load)
</script>

<template>
  <section class="widget" aria-labelledby="activity-title">
    <header class="widget__header">
      <div>
        <p class="widget__eyebrow">Système</p>
        <h3 id="activity-title">Activité récente</h3>
      </div>
    </header>

    <p v-if="loading" class="widget__message" role="status">Chargement de l’activité…</p>
    <div v-else-if="error" class="widget__error" role="alert">
      <p>{{ error }}</p>
      <button class="widget__retry" type="button" @click="load">Réessayer</button>
    </div>
    <p v-else-if="entries.length === 0" class="widget__message">
      Aucune activité récente à afficher.
    </p>
    <ol v-else class="widget__list">
      <li v-for="entry in entries" :key="entry.id" class="widget__item">
        <div class="widget__item-main">
          <strong>{{ entry.action }}</strong>
          <span
            >{{ entry.entiteType
            }}<template v-if="entry.entiteId"> · #{{ entry.entiteId }}</template></span
          >
        </div>
        <div class="widget__item-meta">
          <span>{{ entry.auteur || 'Auteur non renseigné' }}</span>
          <time :datetime="entry.dateAction">{{ formatDate(entry.dateAction) }}</time>
        </div>
      </li>
    </ol>
  </section>
</template>
