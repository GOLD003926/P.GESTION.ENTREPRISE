<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { modelesOnboardingApi } from '@/api/modelesOnboarding'
import AppAlert from '@/components/common/AppAlert.vue'
import ModeleOnboardingForm from '@/components/onboarding/ModeleOnboardingForm.vue'
import { ROUTE_NAMES } from '@/constants/routeNames'

const props = defineProps({
  id: { type: [String, Number], default: null },
})

const router = useRouter()
const modele = ref(null)
const loading = ref(false)
const saving = ref(false)
const error = ref('')
const success = ref('')

const isEdit = computed(() => !!props.id)

async function load() {
  if (!isEdit.value) return
  loading.value = true
  error.value = ''

  try {
    modele.value = await modelesOnboardingApi.obtenir(props.id)
  } catch (cause) {
    error.value = cause.message
  } finally {
    loading.value = false
  }
}

async function submit(payload) {
  saving.value = true
  success.value = ''
  error.value = ''

  try {
    if (isEdit.value) {
      await modelesOnboardingApi.modifier(props.id, payload)
      success.value = 'Le modèle a été mis à jour.'
    } else {
      await modelesOnboardingApi.creer(payload)
      success.value = 'Le modèle a été créé.'
    }

    await router.push({ name: ROUTE_NAMES.MODELES_ONBOARDING })
  } catch (cause) {
    error.value = cause.message
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<template>
  <section class="page">
    <header class="page-header">
      <div>
        <p class="eyebrow">Onboarding</p>
        <h1>{{ isEdit ? 'Modifier le modèle' : 'Nouveau modèle d’intégration' }}</h1>
      </div>
    </header>

    <AppAlert v-if="error" variant="error">{{ error }}</AppAlert>
    <AppAlert v-if="success" variant="success">{{ success }}</AppAlert>

    <div v-if="loading" class="state" role="status">Chargement du modèle…</div>
    <section v-else class="panel">
      <ModeleOnboardingForm :modele="modele" @submit="submit" />
    </section>
  </section>
</template>

<style scoped>
.page {
  display: grid;
  gap: var(--space-5);
}
.page-header h1 {
  margin: 0;
  color: var(--color-text);
  font-size: var(--font-size-2xl);
}
.eyebrow {
  margin: 0 0 var(--space-1);
  color: var(--color-primary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
}
.panel {
  padding: 1.5rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  box-shadow: var(--shadow-card);
}
.state {
  padding: var(--space-8) var(--space-4);
  color: var(--color-text-muted);
  text-align: center;
}
</style>
