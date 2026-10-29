<script setup>
import { computed } from 'vue'

const props = defineProps({
  amount: { type: [Number, String], default: null },
})

const formattedAmount = computed(() => {
  if (props.amount === null || props.amount === undefined) return '—'
  const amount = Number(props.amount)
  if (!Number.isFinite(amount)) return '—'
  return new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 2 }).format(amount)
})
</script>

<template>
  <section class="budget-summary" aria-label="Budget estimé">
    <div>
      <p class="budget-summary__label">Budget estimé</p>
      <p class="budget-summary__amount">{{ formattedAmount }}</p>
    </div>
    <p class="budget-summary__hint">
      Montant calculé par le système à partir de la grille salariale, non modifiable.
    </p>
  </section>
</template>

<style scoped>
.budget-summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  padding: var(--space-4);
  border: 1px solid var(--color-info-border);
  border-radius: var(--radius-md);
  background: var(--color-info-soft);
}

.budget-summary__label,
.budget-summary__amount,
.budget-summary__hint {
  margin: 0;
}

.budget-summary__label {
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}

.budget-summary__amount {
  margin-top: var(--space-1);
  color: var(--color-primary);
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-bold);
}

.budget-summary__hint {
  max-width: 260px;
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
  text-align: right;
}

@media (max-width: 600px) {
  .budget-summary {
    align-items: flex-start;
    flex-direction: column;
  }

  .budget-summary__hint {
    max-width: none;
    text-align: left;
  }
}
</style>
