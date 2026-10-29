<script setup>
defineProps({
  label: { type: String, default: '' },
  tone: { type: String, default: 'neutral' },
})
</script>

<template>
  <span class="badge" :class="[`badge--${tone}`]">{{ label }}</span>
</template>

<style scoped>
.badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 1.75rem;
  padding: 0.25rem 0.625rem;
  border-radius: 999px;
  font-size: 0.72rem;
  font-weight: var(--font-weight-semibold);
}

.badge--neutral {
  background: var(--color-bg);
  color: var(--color-text-muted);
}

.badge--info {
  background: var(--color-info-soft);
  color: var(--color-primary);
}

.badge--success {
  background: var(--color-success-soft);
  color: var(--color-success);
}

.badge--warning {
  background: var(--color-warning-soft);
  color: var(--color-warning);
}

.badge--danger {
  background: var(--color-danger-soft);
  color: var(--color-danger);
}
</style>
