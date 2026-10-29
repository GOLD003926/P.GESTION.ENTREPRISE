<script setup>
import { computed, ref, watch } from 'vue'
import { DECISION_FINALE_LABELS } from '@/constants/statuts'
import AppButton from '@/components/common/AppButton.vue'
import AppAlert from '@/components/common/AppAlert.vue'

const props = defineProps({ open: Boolean, loading: Boolean, error: { type: String, default: '' } })
const emit = defineEmits(['close', 'submit'])
const decision = ref('RETENU')
const motif = ref('')
const motifError = computed(() =>
  decision.value === 'NON_RETENU' && !motif.value.trim()
    ? 'Le motif est obligatoire pour un candidat non retenu.'
    : '',
)
watch(
  () => props.open,
  (open) => {
    if (open) {
      decision.value = 'RETENU'
      motif.value = ''
    }
  },
)
function submit() {
  if (!motifError.value)
    emit('submit', { decision: decision.value, motif: motif.value.trim() || null })
}
</script>

<template>
  <div v-if="open" class="modal-backdrop">
    <section class="modal" role="dialog" aria-modal="true" aria-labelledby="decision-title">
      <h2 id="decision-title">Décision finale</h2>
      <AppAlert v-if="error" variant="error">{{ error }}</AppAlert>
      <form @submit.prevent="submit">
        <label
          >Décision<select v-model="decision">
            <option v-for="(label, value) in DECISION_FINALE_LABELS" :key="value" :value="value">
              {{ label }}
            </option>
          </select></label
        >
        <label v-if="decision === 'NON_RETENU'">Motif<textarea v-model="motif" required /></label>
        <p v-if="motifError" class="error">{{ motifError }}</p>
        <footer>
          <AppButton type="button" variant="secondary" :disabled="loading" @click="emit('close')"
            >Retour</AppButton
          ><AppButton type="submit" :loading="loading">Confirmer la décision</AppButton>
        </footer>
      </form>
    </section>
  </div>
</template>

<style scoped>
.modal-backdrop {
  position: fixed;
  z-index: 100;
  inset: 0;
  display: grid;
  place-items: center;
  padding: var(--space-4);
  background: var(--color-overlay);
}
.modal {
  width: min(100%, 520px);
  display: grid;
  gap: var(--space-4);
  padding: var(--space-5);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  box-shadow: var(--shadow-card);
}
.modal h2 {
  margin: 0;
  color: var(--color-text);
}
.modal form {
  display: grid;
  gap: var(--space-4);
}
.modal label {
  display: grid;
  gap: var(--space-2);
  color: var(--color-text);
  font-size: var(--font-size-sm);
}
.modal select,
.modal textarea {
  min-height: var(--control-height);
  padding: var(--space-3);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-text);
  font: inherit;
}
.modal footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
}
.error {
  color: var(--color-danger);
}
</style>
