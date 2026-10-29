<script setup>
import { reactive, ref, watch } from 'vue'
import AppAlert from '@/components/common/AppAlert.vue'
import AppButton from '@/components/common/AppButton.vue'

const props = defineProps({
  candidatureId: { type: [String, Number], required: true },
  niveau: { type: [String, Number], default: 1 },
  entretien: { type: Object, default: null },
  loading: { type: Boolean, default: false },
  error: { type: String, default: '' },
})
const emit = defineEmits(['submit', 'cancel'])
const form = reactive({ dateHeure: '', dureeMinutes: 60, lieuOuLien: '', intervenants: '' })
const validationError = ref('')

function localDateTime(value) {
  const date = new Date(value)
  const local = new Date(date.getTime() - date.getTimezoneOffset() * 60_000)
  return local.toISOString().slice(0, 16)
}

watch(
  () => props.entretien,
  (entretien) => {
    form.dateHeure = entretien?.dateHeure ? localDateTime(entretien.dateHeure) : ''
    form.dureeMinutes = entretien?.dureeMinutes ?? 60
    form.lieuOuLien = entretien?.lieuOuLien ?? ''
    form.intervenants = entretien?.intervenants?.map((item) => item.utilisateurId).join(', ') ?? ''
  },
  { immediate: true },
)

function submit() {
  const ids = form.intervenants
    .split(',')
    .map((value) => Number(value.trim()))
    .filter(Number.isInteger)
  if (!form.dateHeure || ids.length === 0) {
    validationError.value = 'Une date et au moins un identifiant d’intervenant sont obligatoires.'
    return
  }
  validationError.value = ''
  const dateHeure = new Date(form.dateHeure)
  emit('submit', {
    ...(props.entretien
      ? {}
      : { candidatureId: Number(props.candidatureId), niveau: Number(props.niveau) }),
    dateHeure: dateHeure.toISOString(),
    dureeMinutes: Number(form.dureeMinutes),
    lieuOuLien: form.lieuOuLien.trim() || null,
    intervenants: ids.map((utilisateurId) => ({ utilisateurId, roleIntervenant: null })),
  })
}
</script>

<template>
  <form class="interview-form" @submit.prevent="submit">
    <AppAlert v-if="error" variant="error">{{ error }}</AppAlert>
    <AppAlert v-if="validationError" variant="error">{{ validationError }}</AppAlert>
    <label
      >Date et heure<input
        v-model="form.dateHeure"
        type="datetime-local"
        :min="localDateTime(new Date())"
        required
    /></label>
    <label
      >Durée (minutes)<input
        v-model.number="form.dureeMinutes"
        type="number"
        min="15"
        max="480"
        required
    /></label>
    <label>Lieu ou lien<input v-model.trim="form.lieuOuLien" maxlength="255" /></label>
    <label
      >Identifiants intervenants (séparés par des virgules)<input
        v-model.trim="form.intervenants"
        inputmode="numeric"
        placeholder="Ex. 12, 18"
        required
      /><small
        >Aucun service de recherche d’utilisateurs n’est prévu dans le contrat Recrutement.</small
      ></label
    >
    <footer>
      <AppButton type="button" variant="secondary" :disabled="loading" @click="emit('cancel')"
        >Annuler</AppButton
      ><AppButton type="submit" :loading="loading">{{
        entretien ? 'Enregistrer' : 'Planifier'
      }}</AppButton>
    </footer>
  </form>
</template>

<style scoped>
.interview-form {
  display: grid;
  gap: var(--space-4);
}
.interview-form label {
  display: grid;
  gap: var(--space-2);
  color: var(--color-text);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
}
.interview-form input {
  min-height: var(--control-height);
  padding: 0 var(--space-3);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-text);
  font: inherit;
  font-weight: var(--font-weight-normal);
}
.interview-form small {
  color: var(--color-text-muted);
  font-weight: var(--font-weight-normal);
}
.interview-form footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
}
</style>
