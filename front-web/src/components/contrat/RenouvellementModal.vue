<script setup>
import { reactive, watch } from 'vue'
import { TYPE_CONTRAT_LABELS } from '@/constants/statuts'
import AppAlert from '@/components/common/AppAlert.vue'
import AppButton from '@/components/common/AppButton.vue'

const props = defineProps({
  visible: { type: Boolean, default: false },
  contrat: { type: Object, required: true },
  loading: { type: Boolean, default: false },
  error: { type: String, default: '' },
})
const emit = defineEmits(['close', 'submit'])
const form = reactive({
  dureeMois: 1,
  dateDebut: '',
  salaireBase: '',
  horairesTravail: '',
  lieuTravail: '',
})

watch(
  () => props.visible,
  (visible) => {
    if (visible) {
      form.dureeMois = 1
      form.dateDebut = ''
      form.salaireBase = ''
      form.horairesTravail = ''
      form.lieuTravail = ''
    }
  },
)

function submit() {
  const payload = { dureeMois: Number(form.dureeMois) }
  for (const key of ['dateDebut', 'salaireBase', 'horairesTravail', 'lieuTravail']) {
    if (form[key] !== '') payload[key] = key === 'salaireBase' ? Number(form[key]) : form[key]
  }
  emit('submit', payload)
}
</script>

<template>
  <div v-if="visible" class="backdrop" @click.self="emit('close')">
    <section class="modal" role="dialog" aria-modal="true" aria-labelledby="renew-title">
      <header>
        <div>
          <p class="eyebrow">Nouveau contrat</p>
          <h2 id="renew-title">Renouveler {{ TYPE_CONTRAT_LABELS[contrat.type] }}</h2>
        </div>
        <button type="button" class="close" aria-label="Fermer" @click="emit('close')">×</button>
      </header>
      <p class="hint">
        Les valeurs facultatives sont reprises du contrat précédent si elles restent vides.
      </p>
      <AppAlert v-if="error" variant="error">{{ error }}</AppAlert>
      <form @submit.prevent="submit">
        <label
          >Durée (mois)
          <input
            v-model.number="form.dureeMois"
            type="number"
            min="1"
            :max="contrat.type === 'ESSAI' ? 6 : 24"
            required
          />
        </label>
        <label
          >Date de début (facultatif)
          <input v-model="form.dateDebut" type="date" />
        </label>
        <label
          >Salaire de base (facultatif)
          <input v-model="form.salaireBase" type="number" min="0.01" step="0.01" />
        </label>
        <label
          >Horaires (facultatif)
          <input v-model.trim="form.horairesTravail" maxlength="255" />
        </label>
        <label
          >Lieu de travail (facultatif)
          <input v-model.trim="form.lieuTravail" maxlength="255" />
        </label>
        <footer>
          <AppButton type="button" variant="secondary" @click="emit('close')">Annuler</AppButton>
          <AppButton type="submit" :loading="loading">Créer le renouvellement</AppButton>
        </footer>
      </form>
    </section>
  </div>
</template>

<style scoped>
.backdrop {
  position: fixed;
  z-index: 50;
  inset: 0;
  display: grid;
  overflow-y: auto;
  place-items: center;
  padding: var(--space-4);
  background: rgb(15 23 42 / 55%);
}
.modal {
  width: min(100%, 560px);
  padding: var(--space-5);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  box-shadow: var(--shadow-card);
}
header,
footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
}
h2 {
  margin: 0;
  font-size: var(--font-size-xl);
}
.eyebrow {
  margin: 0 0 var(--space-1);
  color: var(--color-primary);
  font-size: var(--font-size-sm);
}
.close {
  border: 0;
  background: transparent;
  color: var(--color-text-muted);
  font-size: 1.75rem;
  cursor: pointer;
}
.hint {
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}
form {
  display: grid;
  gap: var(--space-3);
}
label {
  display: grid;
  gap: var(--space-2);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
}
input {
  min-height: var(--control-height);
  padding: 0 var(--space-3);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-sm);
  font: inherit;
}
footer {
  justify-content: flex-end;
  margin-top: var(--space-2);
}
</style>
