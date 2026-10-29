<script setup>
import { computed } from 'vue'

const props = defineProps({
  dateLimiteReponse: { type: [String, null], default: null },
})

const daysLeft = computed(() => {
  if (!props.dateLimiteReponse) return null
  const limit = new Date(`${props.dateLimiteReponse}T00:00:00`)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const diffMs = limit.getTime() - today.getTime()
  return Math.ceil(diffMs / (1000 * 60 * 60 * 24))
})

const label = computed(() => {
  if (daysLeft.value == null) return '—'
  if (daysLeft.value < 0) return `${Math.abs(daysLeft.value)} jour(s) dépassé(s)`
  if (daysLeft.value === 0) return 'Dernier jour'
  return `${daysLeft.value} jour(s) restant(s)`
})
</script>

<template>
  <span class="countdown" :class="{ 'countdown--urgent': daysLeft !== null && daysLeft <= 3 }">
    {{ label }}
  </span>
</template>

<style scoped>
.countdown {
  display: inline-flex;
  align-items: center;
  padding: 0.25rem 0.625rem;
  border-radius: 999px;
  background: var(--color-bg);
  color: var(--color-text-muted);
  font-size: 0.72rem;
  font-weight: var(--font-weight-semibold);
}

.countdown--urgent {
  background: var(--color-warning-soft);
  color: var(--color-warning);
}
</style>
