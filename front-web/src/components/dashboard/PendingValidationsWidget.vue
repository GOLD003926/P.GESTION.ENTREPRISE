<script setup>
import { onMounted, ref } from 'vue'
import { validationsApi } from '@/api/validations'

const items = ref([])
const total = ref(0)
const loading = ref(true)
const error = ref('')

async function load() {
  loading.value = true
  error.value = ''
  try {
    const page = await validationsApi.enAttente({ page: 0, size: 5 })
    items.value = page.content
    total.value = page.totalElements
  } catch (cause) {
    error.value = cause.message
  } finally {
    loading.value = false
  }
}

function formatDate(value) {
  if (!value) return 'Date non définie'
  return new Intl.DateTimeFormat('fr-FR', { dateStyle: 'medium' }).format(new Date(value))
}

onMounted(load)
</script>

<template>
  <section class="widget" aria-labelledby="validations-title">
    <header class="widget__header">
      <div>
        <p class="widget__eyebrow">Recrutement</p>
        <h3 id="validations-title">Validations en attente</h3>
      </div>
      <span v-if="!loading && !error" class="widget__count widget__count--warning">
        {{ total }}
      </span>
    </header>

    <p v-if="loading" class="widget__message" role="status">Chargement des validations…</p>
    <div v-else-if="error" class="widget__error" role="alert">
      <p>{{ error }}</p>
      <button class="widget__retry" type="button" @click="load">Réessayer</button>
    </div>
    <p v-else-if="items.length === 0" class="widget__message">
      Aucune demande n’attend votre validation.
    </p>
    <ul v-else class="widget__list">
      <li v-for="item in items" :key="item.id" class="widget__item">
        <div class="widget__item-main">
          <strong>{{ item.reference }}</strong>
          <span>{{ item.posteIntitule }}</span>
        </div>
        <div class="widget__item-meta">
          <span>{{ item.departementNom }}</span>
          <time :datetime="item.dateSouhaitee">{{ formatDate(item.dateSouhaitee) }}</time>
        </div>
      </li>
    </ul>
    <p v-if="!loading && !error && total > items.length" class="widget__footnote">
      {{ total - items.length }} autre(s) demande(s) en attente.
    </p>
  </section>
</template>
