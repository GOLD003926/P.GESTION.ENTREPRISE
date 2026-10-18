<script setup>
import { computed, ref, useId } from 'vue'

const props = defineProps({
  label: { type: String, required: true },
  type: { type: String, default: 'text' },
  error: { type: String, default: '' },
  autocomplete: { type: String, default: 'off' },
  placeholder: { type: String, default: '' },
  disabled: { type: Boolean, default: false },
})

const model = defineModel({ type: String, default: '' })

const id = useId()
const errorId = `${id}-error`
const inputRef = ref(null)
const revealed = ref(false)

const isPassword = computed(() => props.type === 'password')
const inputType = computed(() => (isPassword.value && revealed.value ? 'text' : props.type))

defineExpose({ focus: () => inputRef.value?.focus() })
</script>

<template>
  <div class="field">
    <label class="field__label" :for="id">{{ label }}</label>

    <div class="field__control" :class="{ 'field__control--error': error }">
      <input
        :id="id"
        ref="inputRef"
        v-model="model"
        class="field__input"
        :type="inputType"
        :autocomplete="autocomplete"
        :placeholder="placeholder"
        :disabled="disabled"
        :aria-invalid="error ? 'true' : 'false'"
        :aria-describedby="error ? errorId : undefined"
      />
      <button
        v-if="isPassword"
        type="button"
        class="field__toggle"
        :aria-label="revealed ? 'Masquer le mot de passe' : 'Afficher le mot de passe'"
        :aria-pressed="revealed"
        :disabled="disabled"
        @click="revealed = !revealed"
      >
        {{ revealed ? 'Masquer' : 'Afficher' }}
      </button>
    </div>

    <p v-if="error" :id="errorId" class="field__error" role="alert">{{ error }}</p>
  </div>
</template>

<style scoped>
.field {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.field__label {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
}

.field__control {
  display: flex;
  align-items: stretch;
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  transition:
    border-color var(--transition),
    box-shadow var(--transition);
}

.field__control:focus-within {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--color-focus-ring);
}

.field__control--error {
  border-color: var(--color-danger);
}

.field__control--error:focus-within {
  border-color: var(--color-danger);
  box-shadow: 0 0 0 3px var(--color-focus-ring-danger);
}

.field__input {
  flex: 1;
  min-width: 0;
  height: var(--control-height);
  padding: 0 var(--space-3);
  border: 0;
  border-radius: var(--radius-sm);
  background: transparent;
  outline: none;
}

.field__input:disabled {
  cursor: not-allowed;
  color: var(--color-text-muted);
}

.field__toggle {
  padding: 0 var(--space-3);
  border: 0;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--color-primary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
}

.field__toggle:hover:not(:disabled) {
  text-decoration: underline;
}

.field__error {
  font-size: var(--font-size-sm);
  color: var(--color-danger);
}
</style>
