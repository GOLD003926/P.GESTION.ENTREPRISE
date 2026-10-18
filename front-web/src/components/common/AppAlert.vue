<script setup>
import { computed } from 'vue'

const props = defineProps({
  variant: { type: String, default: 'info' }, // 'error' | 'info' | 'success' | 'warning'
})

// Une erreur est annoncée immédiatement aux lecteurs d'écran, le reste poliment.
const role = computed(() => (props.variant === 'error' ? 'alert' : 'status'))
</script>

<template>
  <div class="alert" :class="`alert--${variant}`" :role="role">
    <slot />
  </div>
</template>

<style scoped>
.alert {
  padding: var(--space-3) var(--space-4);
  border: 1px solid;
  border-radius: var(--radius-sm);
  font-size: var(--font-size-sm);
}

.alert--error {
  background: var(--color-danger-soft);
  border-color: var(--color-danger-border);
  color: var(--color-danger);
}

.alert--info {
  background: var(--color-info-soft);
  border-color: var(--color-info-border);
  color: var(--color-info);
}

.alert--success {
  background: var(--color-success-soft);
  border-color: var(--color-success-border);
  color: var(--color-success);
}

.alert--warning {
  background: var(--color-warning-soft);
  border-color: var(--color-warning-border);
  color: var(--color-warning);
}
</style>
