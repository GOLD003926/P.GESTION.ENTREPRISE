<script setup>
defineProps({
  occupationActuelle: { type: Number, required: true },
  effectifAutorise: { type: Number, required: true },
})
</script>

<template>
  <span class="occupation" :class="{ 'occupation--full': occupationActuelle >= effectifAutorise }">
    {{ occupationActuelle }} / {{ effectifAutorise }}
    <span class="occupation__label">occupés</span>
  </span>
</template>

<style scoped>
.occupation {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  padding: var(--space-1) var(--space-2);
  border-radius: 999px;
  background: var(--color-success-soft);
  color: var(--color-success);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  white-space: nowrap;
}

.occupation--full {
  background: var(--color-warning-soft);
  color: var(--color-warning);
}

.occupation__label {
  font-weight: var(--font-weight-normal);
}
</style>
