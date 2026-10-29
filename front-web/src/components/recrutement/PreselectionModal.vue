<script setup>
import { computed, ref, watch } from 'vue'
import AppButton from '@/components/common/AppButton.vue'
import AppAlert from '@/components/common/AppAlert.vue'

const props = defineProps({ open: Boolean, loading: Boolean, error: { type: String, default: '' } })
const emit = defineEmits(['close', 'submit'])
const accepter = ref(true)
const score = ref('')
const motif = ref('')
const motifError = computed(() =>
  !accepter.value && !motif.value.trim() ? 'Le motif de refus est obligatoire.' : '',
)
watch(
  () => props.open,
  (open) => {
    if (open) {
      accepter.value = true
      score.value = ''
      motif.value = ''
    }
  },
)
function submit() {
  if (motifError.value) return
  emit('submit', {
    accepter: accepter.value,
    score: score.value === '' ? null : Number(score.value),
    motif: motif.value.trim() || null,
  })
}
</script>

<template>
  <div v-if="open" class="modal-backdrop">
    <section class="modal" role="dialog" aria-modal="true" aria-labelledby="preselection-title">
      <h2 id="preselection-title">Présélection sur dossier</h2>
      <AppAlert v-if="error" variant="error">{{ error }}</AppAlert>
      <form @submit.prevent="submit">
        <label
          >Décision<select v-model="accepter">
            <option :value="true">Présélectionner</option>
            <option :value="false">Refuser sur dossier</option>
          </select></label
        >
        <label>Score (0 à 100)<input v-model="score" type="number" min="0" max="100" /></label>
        <label v-if="!accepter"
          >Motif du refus<textarea v-model="motif" maxlength="500" required />
        </label>
        <p v-if="motifError" class="error">{{ motifError }}</p>
        <footer>
          <AppButton type="button" variant="secondary" :disabled="loading" @click="emit('close')"
            >Retour</AppButton
          ><AppButton type="submit" :loading="loading">Enregistrer</AppButton>
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
.modal input,
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
