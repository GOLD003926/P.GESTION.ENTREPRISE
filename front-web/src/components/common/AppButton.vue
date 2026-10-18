<script setup>
defineProps({
  type: { type: String, default: 'button' },
  variant: { type: String, default: 'primary' }, // 'primary' | 'secondary'
  loading: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false },
  block: { type: Boolean, default: false },
})
</script>

<template>
  <button
    class="btn"
    :class="[`btn--${variant}`, { 'btn--block': block }]"
    :type="type"
    :disabled="disabled || loading"
    :aria-busy="loading"
  >
    <span v-if="loading" class="btn__spinner" aria-hidden="true" />
    <span><slot /></span>
  </button>
</template>

<style scoped>
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  height: var(--control-height);
  padding: 0 var(--space-5);
  border: 1px solid transparent;
  border-radius: var(--radius-sm);
  font-weight: var(--font-weight-semibold);
  transition:
    background-color var(--transition),
    box-shadow var(--transition);
}

.btn:focus-visible {
  outline: none;
  box-shadow: 0 0 0 3px var(--color-focus-ring);
}

.btn:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

.btn--primary {
  background: var(--color-primary);
  color: var(--color-on-primary);
}

.btn--primary:hover:not(:disabled) {
  background: var(--color-primary-hover);
}

.btn--secondary {
  background: var(--color-surface);
  border-color: var(--color-border-strong);
  color: var(--color-text);
}

.btn--secondary:hover:not(:disabled) {
  background: var(--color-bg);
}

.btn--block {
  width: 100%;
}

.btn__spinner {
  width: 1em;
  height: 1em;
  border: 2px solid currentColor;
  border-right-color: transparent;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (prefers-reduced-motion: reduce) {
  .btn__spinner {
    animation-duration: 2s;
  }
}
</style>
