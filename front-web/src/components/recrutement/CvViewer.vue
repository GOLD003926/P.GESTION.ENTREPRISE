<script setup>
import { ref, watch } from 'vue'
import { documentsApi } from '@/api/documents'
import AppAlert from '@/components/common/AppAlert.vue'

const props = defineProps({ documentId: { type: [String, Number], default: null } })
const loading = ref(false)
const error = ref('')
const url = ref('')
const filename = ref('CV')

function clearUrl() {
  if (url.value) URL.revokeObjectURL(url.value)
  url.value = ''
}
async function load(documentId) {
  clearUrl()
  error.value = ''
  if (!documentId) return
  loading.value = true
  try {
    const response = await documentsApi.telecharger(documentId)
    const disposition = response.headers['content-disposition'] ?? ''
    const match = disposition.match(/filename="?([^";]+)"?/i)
    filename.value = match?.[1] ?? `CV-${documentId}`
    url.value = URL.createObjectURL(response.data)
  } catch (cause) {
    error.value = cause.message
  } finally {
    loading.value = false
  }
}
watch(() => props.documentId, load, { immediate: true })
</script>

<template>
  <div class="cv-viewer">
    <p v-if="loading" role="status">Chargement du CV…</p>
    <AppAlert v-if="error" variant="error">{{ error }}</AppAlert>
    <p v-if="!documentId && !loading">Aucun CV déposé.</p>
    <a v-if="url" :href="url" :download="filename">Télécharger le CV</a>
    <iframe v-if="url && /\.pdf$/i.test(filename)" :src="url" title="Aperçu du CV PDF" />
  </div>
</template>

<style scoped>
.cv-viewer {
  display: grid;
  gap: var(--space-3);
}
.cv-viewer a {
  justify-self: start;
  color: var(--color-primary);
  font-weight: var(--font-weight-semibold);
}
.cv-viewer iframe {
  width: 100%;
  min-height: 520px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
}
.cv-viewer p {
  color: var(--color-text-muted);
}
</style>
