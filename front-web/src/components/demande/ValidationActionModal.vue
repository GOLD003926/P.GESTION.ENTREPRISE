<script setup>
import { computed, ref, watch } from 'vue'
import { DECISION_VALIDATION_LABELS } from '@/constants/statuts'
import AppAlert from '@/components/common/AppAlert.vue'
import AppButton from '@/components/common/AppButton.vue'

const props = defineProps({
  open: { type: Boolean, default: false },
  loading: { type: Boolean, default: false },
  error: { type: String, default: '' },
})

const emit = defineEmits(['close', 'submit'])
const decision = ref('APPROUVEE')
const commentaire = ref('')
const commentRequired = computed(() => ['REJETEE', 'RETOURNEE'].includes(decision.value))
const commentError = computed(() =>
  commentRequired.value && !commentaire.value.trim()
    ? 'Un commentaire est obligatoire pour rejeter ou retourner la demande.'
    : '',
)

watch(
  () => props.open,
  (open) => {
    if (open) {
      decision.value = 'APPROUVEE'
      commentaire.value = ''
    }
  },
)

function submit() {
  if (commentError.value) return
  emit('submit', {
    decision: decision.value,
    commentaire: commentaire.value.trim() || null,
  })
}
</script>

<template>
  <div v-if="open" class="validation-modal-backdrop">
    <section
      class="validation-modal"
      role="dialog"
      aria-modal="true"
      aria-labelledby="validation-modal-title"
    >
      <header class="validation-modal__header">
        <div>
          <p class="validation-modal__eyebrow">Décision de validation</p>
          <h2 id="validation-modal-title">Traiter la demande</h2>
        </div>
        <button
          type="button"
          class="validation-modal__close"
          aria-label="Fermer"
          :disabled="loading"
          @click="emit('close')"
        >
          ×
        </button>
      </header>

      <form class="validation-modal__form" @submit.prevent="submit">
        <AppAlert v-if="error" variant="error">{{ error }}</AppAlert>

        <label class="validation-modal__field" for="validation-decision">
          Décision
          <select id="validation-decision" v-model="decision" :disabled="loading">
            <option value="APPROUVEE">{{ DECISION_VALIDATION_LABELS.APPROUVEE }}</option>
            <option value="RETOURNEE">{{ DECISION_VALIDATION_LABELS.RETOURNEE }}</option>
            <option value="REJETEE">{{ DECISION_VALIDATION_LABELS.REJETEE }}</option>
          </select>
        </label>

        <label class="validation-modal__field" for="validation-comment">
          Commentaire {{ commentRequired ? '(obligatoire)' : '(facultatif)' }}
          <textarea
            id="validation-comment"
            v-model="commentaire"
            maxlength="2000"
            rows="4"
            :required="commentRequired"
            :aria-invalid="Boolean(commentError)"
            :aria-describedby="commentError ? 'validation-comment-error' : undefined"
            :disabled="loading"
          />
          <span
            v-if="commentError"
            id="validation-comment-error"
            class="validation-modal__field-error"
          >
            {{ commentError }}
          </span>
        </label>

        <footer class="validation-modal__actions">
          <AppButton type="button" variant="secondary" :disabled="loading" @click="emit('close')">
            Retour
          </AppButton>
          <AppButton type="submit" :loading="loading">Enregistrer la décision</AppButton>
        </footer>
      </form>
    </section>
  </div>
</template>

<style scoped>
.validation-modal-backdrop {
  position: fixed;
  z-index: 100;
  inset: 0;
  display: grid;
  place-items: center;
  padding: var(--space-4);
  background: var(--color-overlay);
}

.validation-modal {
  width: min(100%, 560px);
  max-height: min(90vh, 740px);
  overflow: auto;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  box-shadow: var(--shadow-card);
}

.validation-modal__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-4);
  padding: var(--space-5);
  border-bottom: 1px solid var(--color-border);
}

.validation-modal__eyebrow {
  margin: 0 0 var(--space-1);
  color: var(--color-primary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
}

.validation-modal__header h2 {
  margin: 0;
  color: var(--color-text);
  font-size: var(--font-size-xl);
}

.validation-modal__close {
  width: 36px;
  height: 36px;
  border: 1px solid var(--color-border);
  border-radius: 50%;
  background: var(--color-surface);
  color: var(--color-text);
  font-size: var(--font-size-xl);
  cursor: pointer;
}

.validation-modal__form {
  display: grid;
  gap: var(--space-4);
  padding: var(--space-5);
}

.validation-modal__field {
  display: grid;
  gap: var(--space-2);
  color: var(--color-text);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
}

.validation-modal__field select,
.validation-modal__field textarea {
  width: 100%;
  padding: var(--space-3);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-text);
  font: inherit;
}

.validation-modal__field select {
  min-height: var(--control-height);
}

.validation-modal__field-error {
  color: var(--color-danger);
  font-weight: var(--font-weight-normal);
}

.validation-modal__actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  padding-top: var(--space-2);
}

.validation-modal__close:focus-visible,
.validation-modal__field select:focus-visible,
.validation-modal__field textarea:focus-visible {
  outline: 3px solid var(--color-focus-ring);
  outline-offset: 2px;
}

@media (max-width: 520px) {
  .validation-modal__actions {
    flex-direction: column-reverse;
  }
}
</style>
