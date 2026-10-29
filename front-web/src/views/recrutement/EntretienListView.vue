<script setup>
import { onMounted, reactive, ref } from 'vue'
import { entretiensApi } from '@/api/entretiens'
import EntretienTable from '@/components/recrutement/EntretienTable.vue'
import { STATUT_ENTRETIEN_LABELS } from '@/constants/statuts'
import AppAlert from '@/components/common/AppAlert.vue'
import AppButton from '@/components/common/AppButton.vue'

const filters = reactive({ statut: '', du: '', au: '' })
const entretiens = ref([])
const loading = ref(true)
const error = ref('')
async function load() {
  loading.value = true
  error.value = ''
  try {
    const params = Object.fromEntries(Object.entries(filters).filter(([, value]) => value))
    const page = await entretiensApi.rechercher({ ...params, page: 0, size: 100 })
    entretiens.value = page.content ?? []
  } catch (cause) {
    error.value = cause.message
  } finally {
    loading.value = false
  }
}
onMounted(load)
</script>

<template>
  <section class="page">
    <header>
      <p class="eyebrow">Recrutement</p>
      <h1>Entretiens</h1>
      <p>Consultez le calendrier et l’avancement des entretiens.</p>
    </header>
    <AppAlert v-if="error" variant="error">{{ error }}</AppAlert>
    <form class="filters" @submit.prevent="load">
      <label
        >Statut<select v-model="filters.statut">
          <option value="">Tous</option>
          <option v-for="(label, key) in STATUT_ENTRETIEN_LABELS" :key="key" :value="key">
            {{ label }}
          </option>
        </select></label
      ><label>Du<input v-model="filters.du" type="date" /></label
      ><label>Au<input v-model="filters.au" type="date" /></label
      ><AppButton type="submit" :loading="loading">Filtrer</AppButton>
    </form>
    <div class="panel">
      <p v-if="loading" class="empty" role="status">Chargement des entretiens…</p>
      <p v-else-if="!entretiens.length" class="empty">Aucun entretien trouvé.</p>
      <EntretienTable v-else :entretiens="entretiens" />
    </div>
  </section>
</template>

<style scoped>
.page {
  display: grid;
  gap: var(--space-5);
}
header p {
  margin: 0;
  color: var(--color-text-muted);
}
h1 {
  margin: 0;
  color: var(--color-text);
  font-size: var(--font-size-2xl);
}
.eyebrow {
  color: var(--color-primary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
}
.filters {
  display: flex;
  align-items: end;
  gap: var(--space-3);
}
.filters label {
  display: grid;
  flex: 1;
  gap: var(--space-2);
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}
.filters input,
.filters select {
  min-height: var(--control-height);
  padding: 0 var(--space-3);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-text);
  font: inherit;
}
.panel {
  overflow: hidden;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  box-shadow: var(--shadow-card);
}
.empty {
  padding: var(--space-5);
  color: var(--color-text-muted);
  text-align: center;
}
@media (max-width: 650px) {
  .filters {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
