<script setup>
import { computed } from 'vue'

const props = defineProps({
  current: { type: Number, default: 0 },
  total: { type: Number, default: 0 },
  label: { type: String, default: 'Progression' },
})

const percent = computed(() => {
  if (!props.total) return 0
  return Math.min(100, Math.max(0, (props.current / props.total) * 100))
})
</script>

<template>
  <div class="progress" aria-label="Progression de l’onboarding">
    <div class="progress__header">
      <span>{{ label }}</span>
      <strong>{{ current }}/{{ total }}</strong>
    </div>
    <div class="progress__bar" aria-hidden="true">
      <span :style="{ width: `${percent}%` }" />
    </div>
  </div>
</template>

<style scoped>
.progress {
  display: grid;
  gap: 0.5rem;
}
.progress__header {
  display: flex;
  justify-content: space-between;
  gap: 0.75rem;
  color: var(--color-text-muted);
  font-size: 0.8rem;
}
.progress__bar {
  overflow: hidden;
  height: 0.75rem;
  border-radius: 999px;
  background: var(--color-bg);
}
.progress__bar span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, var(--color-primary), var(--color-success));
}
</style>
